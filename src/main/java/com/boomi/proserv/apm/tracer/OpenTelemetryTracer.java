package com.boomi.proserv.apm.tracer;

import com.boomi.connector.api.PayloadMetadata;
import com.boomi.proserv.apm.BoomiContext;
import com.boomi.proserv.apm.ComponentType;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Map;
import java.util.logging.Logger;


public class OpenTelemetryTracer extends Tracer {

    /*https://www.skypack.dev/view/@opentelemetry/propagator-ot-trace*/
    public static final String OT_TRACER_TRACEID    = "ot-tracer-traceid";
    public static final String OT_TRACER_SPANID     = "ot-tracer-spanid";

    protected enum OpenTelemetryTraceType {
        none,
        traceparent,
        ottracer,
        b3propagation
    }

    private static OpenTelemetryTraceType openTelemetryTraceType;

    public OpenTelemetryTraceType getOpenTelemetryTraceType() {
        return openTelemetryTraceType;
    }

    public void start(Logger logger, BoomiContext context, String rtProcess, String document, Map<String, String> dynProps, Map<String, String> properties, PayloadMetadata metadata) {
        try {
            logger.info("Looking for OpenTelemetry trace ...");
            Span span                                   = getSpan();
            RealTimeProcessing realTimeProcessing       = RealTimeProcessing.getValue(rtProcess);
            OpenTelemetry openTelemetry                 = getOpenTelemetry(logger, realTimeProcessing);
            io.opentelemetry.api.trace.Tracer tracer    = openTelemetry.getTracer(context.getServiceName(), context.getServiceVersion());

            if (RealTimeProcessing.w3c.equals(realTimeProcessing) && getTraceparent(properties) != null) {
                logger.info("Continuing transaction using w3c header (" + getOpenTelemetryTraceType().name() + ") ...");
                Context extractedContext    = openTelemetry.getPropagators().getTextMapPropagator().extract(Context.current(), getExchange(this, logger, properties), getGetter());
                Scope scope                 = extractedContext.makeCurrent();
                span                        = tracer.spanBuilder(context.getProcessName()).setParent(extractedContext).setSpanKind(SpanKind.SERVER).startSpan();
            } else if(RealTimeProcessing.ignore.equals(realTimeProcessing) || !isValid(span)) {
                if(buildNewSpanWhenIgnoreTag() || !isValid(span)) {
                    logger.info("Trace not found/ignored. Creating OpenTelemetry trace ...");
                    span = tracer.spanBuilder(context.getProcessName()).setNoParent().setSpanKind(SpanKind.SERVER).startSpan();
                    span.makeCurrent();
                } else {
                    logger.info("Trace found/reused, setting tags ...");
                }
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

    private Span getSpan() {
        return Span.current();
    }

    protected boolean buildNewSpanWhenIgnoreTag() {
        String buildnewspanwhenignoretag = System.getProperty("boomiapm.buildnewspanwhenignoretag");
        if(buildnewspanwhenignoretag != null && !"".equals(buildnewspanwhenignoretag)) {
            return Boolean.getBoolean(buildnewspanwhenignoretag);
        } else {
            return true;
        }
    }

    public void stop(Logger logger, BoomiContext context, String rtProcess, String document, Map<String, String> dynProps, Map<String, String> properties, PayloadMetadata metadata) {
        super.stop(logger, context, rtProcess, document, dynProps, properties, metadata);
        try {
            logger.info("Closing OpenTelemetry trace ...");
            Span span = getSpan();
            RealTimeProcessing realTimeProcessing = RealTimeProcessing.getValue(rtProcess);

            if(span!=null && span.getSpanContext().isValid()) {
                setTraceId(logger, span.getSpanContext().getTraceId(), metadata);
                span.end();
                logger.info("OpenTelemetry trace closed");
                if(RealTimeProcessing.ignore.equals(realTimeProcessing)){
                    //Do we need to force the parentSpan to close?
                }
            } else {
                logger.severe("OpenTelemetry trace not found");
            }
        } catch (Exception e) {
            logger.severe("OpenTelemetry trace not closed " + e);
        }
    }

    public void error(Logger logger, BoomiContext context, String rtProcess, String document, Map<String, String> dynProps, Map<String, String> properties, PayloadMetadata metadata) {
        super.error(logger, context, rtProcess, document, dynProps, properties, metadata);
        try {
            logger.info("Closing OpenTelemetry trace ...");
            Span span = getSpan();
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
    }

    protected OpenTelemetry getOpenTelemetry(Logger logger, RealTimeProcessing realTimeProcessing) {
        OpenTelemetry openTelemetry = null;

        try {
            logger.info("Using GlobalOpenTelemetry ...");
            openTelemetry = GlobalOpenTelemetry.get();
        } catch (Exception e){
            logger.severe("Unable to use GlobalOpenTelemetry. Defaulting to OpenTelemetry noop  " + e);
            openTelemetry = OpenTelemetry.noop();
        }

        return openTelemetry;
    }

    protected boolean isValid(Span span) {
        return span != null && span.getSpanContext().isValid();
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

    protected String getOTTracePropagatorTraceId(Map<String, String> properties) {
        String traceId = properties.get(HTTP_DOC_PREFIX + OT_TRACER_TRACEID);
        setComponentType(ComponentType.HTTP);
        if(traceId == null || "".equals(traceId)) {
            traceId = properties.get(OT_TRACER_TRACEID);
            setComponentType(ComponentType.JMS);
        }
        return traceId;
    }

    protected String getOTTracePropagatorSpanId(Map<String, String> properties) {
        String spanId = properties.get(HTTP_DOC_PREFIX + OT_TRACER_SPANID);
        if(spanId == null || "".equals(spanId)) {
            spanId = properties.get(OT_TRACER_SPANID);
        }
        if(spanId == null || "".equals(spanId)) {
            spanId = getB3SpanId(properties);
        }

        return spanId;
    }

    @Override
    protected String getTraceparent(Map<String, String> properties){
        String traceparent = super.getTraceparent(properties);
        openTelemetryTraceType = OpenTelemetryTraceType.traceparent;
        if(traceparent==null || "".equals(traceparent)) {
            traceparent = getOTTracePropagatorTraceId(properties);
            openTelemetryTraceType = OpenTelemetryTraceType.ottracer;
        }
        if(traceparent==null || "".equals(traceparent)) {
            traceparent = getB3TraceId(properties);
            openTelemetryTraceType = OpenTelemetryTraceType.b3propagation;
        }
        if(traceparent==null || "".equals(traceparent)) {
            openTelemetryTraceType = OpenTelemetryTraceType.none;
        }

        return traceparent;
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

    protected static HttpExchange getExchange(OpenTelemetryTracer tracer, Logger logger, Map<String, String> properties) {
        return new HttpExchange() {
            @Override
            public Headers getRequestHeaders() {
                Headers headers = new Headers();
                String traceparent = tracer.getTraceparent(properties);
                if((tracer.getOpenTelemetryTraceType() == OpenTelemetryTraceType.b3propagation || tracer.getOpenTelemetryTraceType() == OpenTelemetryTraceType.ottracer) && traceparent.length()==16) {
                    String spanId = tracer.getOTTracePropagatorSpanId(properties);
                    headers.add("X-B3-TraceId", traceparent);
                    headers.add("X-B3-SpanId", spanId);
                } else {
                    headers.add(tracer.TRACEPARENT, traceparent);
                }
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
