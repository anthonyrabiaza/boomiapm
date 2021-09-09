package com.boomi.proserv.apm.tracer;

import io.opentracing.Span;
import io.opentracing.contrib.tracerresolver.TracerResolver;

import java.util.logging.Logger;

public class OpenTracingResolver extends OpenTracingTracer {

    @Override
    protected io.opentracing.Tracer getOpenTracingTracer(Logger logger) {
        logger.info("Getting ElasticAPMTracer tracer ...");
        return TracerResolver.resolveTracer();
    }

    @Override
    protected Span getSpan() {
        return TracerResolver.resolveTracer().activeSpan();
    }
}
