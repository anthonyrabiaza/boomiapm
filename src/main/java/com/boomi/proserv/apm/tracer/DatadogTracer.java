package com.boomi.proserv.apm.tracer;

public class DatadogTracer extends OpenTracingTracer{

    @Override
    protected boolean buildNewSpanWhenIgnoreTag() {
        return false;
    }

}
