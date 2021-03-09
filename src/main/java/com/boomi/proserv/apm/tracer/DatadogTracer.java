package com.boomi.proserv.apm.tracer;

import com.boomi.execution.ExecutionUtil;
import com.boomi.proserv.apm.BoomiContext;

import java.util.Map;
import java.util.logging.Logger;

public class DatadogTracer implements Tracer {
    @Override
    public void start(Logger logger, BoomiContext context) {
        try {
            logger.info("Adding OpenTracing trace ...");
            io.opentracing.Span span = io.opentracing.util.GlobalTracer.get().activeSpan();
            if(span==null) {
                io.opentracing.Tracer tracer = io.opentracing.util.GlobalTracer.get();
                span = tracer.buildSpan(context.getProcessName()).withTag("service", System.getProperty("dd.service")).start();
                tracer.activateSpan(span);
            }
            ExecutionUtil.setDynamicProcessProperty("DPP_traceID", span.context().toTraceId(), false);
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
    public void stop(Logger logger, BoomiContext context) {
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
    public void error(Logger logger, BoomiContext context) {
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

    @Override
    public void start(Logger logger, BoomiContext context, String document, Map<String, String> properties) {

    }

    @Override
    public void stop(Logger logger, BoomiContext context, String document, Map<String, String> properties) {

    }

    @Override
    public void error(Logger logger, BoomiContext context, String document, Map<String, String> properties) {

    }
}
