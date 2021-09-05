package com.boomi.proserv.apm.tracer;

import com.boomi.connector.api.PayloadMetadata;
import com.boomi.proserv.apm.BoomiContext;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.OpenTelemetrySdkAutoConfiguration;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Map;
import java.util.logging.Logger;


public class OpenTelemetryTracer extends Tracer {

    public void start(Logger logger, BoomiContext context, String rtProcess, String document, Map<String, String> dynProps, Map<String, String> properties, PayloadMetadata metadata) {
        try {
            logger.info("Looking for OpenTelemetry trace ...");
            OpenTelemetry openTelemetry;

            Span span = Span.current();
            RealTimeProcessing realTimeProcessing = RealTimeProcessing.getValue(rtProcess);

            try {
                logger.info("Using GlobalOpenTelemetry ...");
                openTelemetry = GlobalOpenTelemetry.get();
            } catch (Exception e ){
                logger.severe("Unable to use GlobalOpenTelemetry " + e);
                openTelemetry = OpenTelemetry.noop();
            }

            io.opentelemetry.api.trace.Tracer tracer = openTelemetry.getTracer(s_serviceName, s_serviceVersion);
            if (RealTimeProcessing.w3c.equals(realTimeProcessing) && getTraceparent(properties) != null) {
                logger.info("Continuing transaction using w3c header ...");
                Context extractedContext    = openTelemetry.getPropagators().getTextMapPropagator().extract(Context.current(), getExchange(this, properties), getGetter());
                Scope scope                 = extractedContext.makeCurrent();
                span                        = tracer.spanBuilder(context.getProcessName()).setParent(extractedContext).setSpanKind(SpanKind.SERVER).startSpan();
            } else if(span==null || !span.getSpanContext().isValid()) {
                logger.info("Trace not found...");
                logger.info("Creating OpenTelemetry trace ...");
                span = tracer.spanBuilder(context.getProcessName()).setSpanKind(SpanKind.SERVER).startSpan();
                span.makeCurrent();
            } else {
                logger.info("Trace found, setting tags ...");
            }
            setTraceId(logger, span.getSpanContext().getTraceId(), metadata);
            span.setAttribute(BOOMI_EXECUTION_ID, context.getExecutionId());
            span.setAttribute(BOOMI_PROCESS_NAME, context.getProcessName());
            span.setAttribute(BOOMI_PROCESS_ID, context.getProcessId());
            logger.info("OpenTelemetry trace added");
        } catch (Exception e) {
            logger.severe("OpenTelemetry trace not added " + e);
        }
        super.start(logger, context, rtProcess, document, dynProps, properties, metadata);
    }

    public void stop(Logger logger, BoomiContext context, String rtProcess, String document, Map<String, String> dynProps, Map<String, String> properties, PayloadMetadata metadata) {
        try {
            logger.info("Closing OpenTelemetry trace ...");
            Span span = Span.current();
            if(span!=null && span.getSpanContext().isValid()) {
                setTraceId(logger, span.getSpanContext().getTraceId(), metadata);
                span.end();
                logger.info("OpenTelemetry trace closed");
            } else {
                logger.severe("OpenTelemetry trace not found");
            }
        } catch (Exception e) {
            logger.severe("OpenTelemetry trace not closed " + e);
        }
        super.stop(logger, context, rtProcess, document, dynProps, properties, metadata);
    }
    public void error(Logger logger, BoomiContext context, String rtProcess, String document, Map<String, String> dynProps, Map<String, String> properties, PayloadMetadata metadata) {
        try {
            logger.info("Closing OpenTelemetry trace ...");
            Span span = Span.current();
            if(span!=null && span.getSpanContext().isValid()) {
                setTraceId(logger, span.getSpanContext().getTraceId(), metadata);
                span.setStatus(StatusCode.ERROR, getErrorMessage());
                span.end();
                logger.info("OpenTelemetry trace closed with Error");
            } else {
                logger.severe("OpenTelemetry trace not found");
            }
        } catch (Exception e) {
            logger.severe("OpenTelemetry trace not closed " + e);
        }
        super.error(logger, context, rtProcess, document, dynProps, properties, metadata);
    }

    @Override
    protected void addTags(Map<String, String> dynProps) {
        Map<String, String> tags = getTags(dynProps);
        if(tags.size()>0) {
            Span span = Span.current();
            if(span!=null && span.getSpanContext().isValid()) {
                for (Map.Entry<String, String> entry : tags.entrySet()) {
                    span.setAttribute(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    protected static TextMapGetter getGetter(){
        TextMapGetter<HttpExchange> getter =
            new TextMapGetter<HttpExchange>() {
                @Override
                public String get(HttpExchange carrier, String key) {
                    if (carrier.getRequestHeaders().containsKey(key)) {
                        return carrier.getRequestHeaders().get(key).get(0);
                    }
                    return null;
                }

                @Override
                public Iterable<String> keys(HttpExchange carrier) {
                    return carrier.getRequestHeaders().keySet();
                }
            };
        return getter;
    }

    protected static HttpExchange getExchange(Tracer tracer, Map<String, String> properties) {
        return new HttpExchange() {
            @Override
            public Headers getRequestHeaders() {
                Headers headers = new Headers();
                String traceparent = tracer.getTraceparent(properties);
                headers.add(tracer.TRACEPARENT, traceparent);
                return headers;
            }
            @Override
            public Headers getResponseHeaders() {
                return null;
            }
            @Override
            public URI getRequestURI() {
                return null;
            }
            @Override
            public String getRequestMethod() {
                return null;
            }
            @Override
            public HttpContext getHttpContext() {
                return null;
            }
            @Override
            public void close() {
            }
            @Override
            public InputStream getRequestBody() {
                return null;
            }
            @Override
            public OutputStream getResponseBody() {
                return null;
            }
            @Override
            public void sendResponseHeaders(int i, long l) throws IOException {

            }
            @Override
            public InetSocketAddress getRemoteAddress() {
                return null;
            }
            @Override
            public int getResponseCode() {
                return 0;
            }
            @Override
            public InetSocketAddress getLocalAddress() {
                return null;
            }
            @Override
            public String getProtocol() {
                return null;
            }
            @Override
            public Object getAttribute(String s) {
                return null;
            }
            @Override
            public void setAttribute(String s, Object o) {
            }
            @Override
            public void setStreams(InputStream inputStream, OutputStream outputStream) {
            }
            @Override
            public HttpPrincipal getPrincipal() {
                return null;
            }
        };
    }
}
