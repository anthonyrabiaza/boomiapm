package com.boomi.proserv.apm.tracer;

import com.boomi.connector.api.PayloadMetadata;
import com.boomi.proserv.apm.BoomiContext;

import java.util.Map;
import java.util.logging.Logger;

public class OpenTracingTracer extends Tracer {
    @Override
    public void start(Logger logger, BoomiContext context, PayloadMetadata metadata) {
        try {
            logger.info("Adding OpenTracing trace ...");
            io.opentracing.Span span = io.opentracing.util.GlobalTracer.get().activeSpan();
            if(span==null) {
                io.opentracing.Tracer tracer = io.opentracing.util.GlobalTracer.get();
                span = tracer.buildSpan(context.getProcessName()).withTag("service", context.getServiceName()).start();
                tracer.activateSpan(span);
            }
            String traceId = span.context().toTraceId();
            setTraceId(traceId);
            metadata.setTrackedProperty("traceID", traceId);
            span.setOperationName(context.getProcessName());
            span.setTag("boomi.executionID", context.getExecutionId());
            span.setTag("boomi.processName", context.getProcessName());
            span.setTag("boomi.processID", context.getProcessId());
            logger.info("OpenTracing trace added");
        } catch (Exception e) {
            logger.severe("OpenTracing trace not added " + e);
        }
    }

    @Override
    public void stop(Logger logger, BoomiContext context, PayloadMetadata metadata) {
        try {
            logger.info("Closing OpenTracing trace ...");
            io.opentracing.Span span = io.opentracing.util.GlobalTracer.get().activeSpan();
            if(span!=null) {
                span.finish();
                logger.info("OpenTracing trace closed");
            } else {
                logger.severe("OpenTracing trace not found");
            }
        } catch (Exception e) {
            logger.severe("OpenTracing trace not closed " + e);
        }
    }

    @Override
    public void error(Logger logger, BoomiContext context, PayloadMetadata metadata) {
        try {
            logger.info("Closing OpenTracing trace ...");
            io.opentracing.Span span = io.opentracing.util.GlobalTracer.get().activeSpan();
            if(span!=null) {
                span.setTag(io.opentracing.tag.Tags.ERROR, true);
                span.finish();
                logger.info("OpenTracing trace closed with Error");
            } else {
                logger.severe("OpenTracing trace not found");
            }
        } catch (Exception e) {
            logger.severe("OpenTracing trace not closed " + e);
        }
    }

    protected void addTags(Map<String, String> dynProps) {
        Map<String, String> tags = getTags(dynProps);
        if(tags.size()>0) {
            io.opentracing.Span span = io.opentracing.util.GlobalTracer.get().activeSpan();
            for (Map.Entry<String, String> entry : tags.entrySet()) {
                span.setTag(entry.getKey(), entry.getValue());
            }
        }
    }
}
