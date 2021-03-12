package com.boomi.proserv.apm.tracer;

import com.boomi.connector.api.PayloadMetadata;
import com.boomi.proserv.apm.BoomiContext;
import io.opentelemetry.api.trace.SpanKind;

import java.util.Map;
import java.util.logging.Logger;

public class OpenTelemetryTracer extends Tracer {

    public void start(Logger logger, BoomiContext context, PayloadMetadata metadata) {
        try {
            logger.info("Adding OpenTelemetry trace ...");
            io.opentelemetry.api.trace.Tracer otracer = io.opentelemetry.api.GlobalOpenTelemetry.getTracer("boomi");
            io.opentelemetry.api.trace.Span span = io.opentelemetry.api.trace.Span.current();
            if(span==null || !span.getSpanContext().isValid()) {
                span = otracer.spanBuilder(context.getProcessName()).setSpanKind(SpanKind.CLIENT).startSpan();
                span.makeCurrent();
            }
            span.setAttribute("boomi.executionID", context.getExecutionId());
            span.setAttribute("boomi.processName", context.getProcessName());
            span.setAttribute("boomi.processID", context.getProcessId());
            logger.info("OpenTelemetry trace added");
        } catch (Exception e) {
            logger.severe("OpenTelemetry trace not added " + e);
        }
    }

    public void stop(Logger logger, BoomiContext context, PayloadMetadata metadata) {
        try {
            logger.info("Closing OpenTelemetry trace ...");
            io.opentelemetry.api.trace.Span span = io.opentelemetry.api.trace.Span.current();
            if(span!=null && span.getSpanContext().isValid()) {
                span.end();
                logger.info("OpenTelemetry trace closed");
            } else {
                logger.severe("OpenTelemetry trace not found");
            }
        } catch (Exception e) {
            logger.severe("OpenTelemetry trace not closed " + e);
        }
    }
    public void error(Logger logger, BoomiContext context, PayloadMetadata metadata) {
        try {
            logger.info("Closing OpenTelemetry trace ...");
            io.opentelemetry.api.trace.Span span = io.opentelemetry.api.trace.Span.current();
            if(span!=null && span.getSpanContext().isValid()) {
                span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, "error");
                span.end();
                logger.info("OpenTelemetry trace closed with Error");
            } else {
                logger.severe("OpenTelemetry trace not found");
            }
        } catch (Exception e) {
            logger.severe("OpenTelemetry trace not closed " + e);
        }
    }
    @Override
    protected void addTags(Map<String, String> dynProps) {
        Map<String, String> tags = getTags(dynProps);
        if(tags.size()>0) {
            io.opentelemetry.api.trace.Span span = io.opentelemetry.api.trace.Span.current();
            if(span!=null && span.getSpanContext().isValid()) {
                for (Map.Entry<String, String> entry : tags.entrySet()) {
                    span.setAttribute(entry.getKey(), entry.getValue());
                }
            }
        }
    }
}
