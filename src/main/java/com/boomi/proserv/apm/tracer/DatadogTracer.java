package com.boomi.proserv.apm.tracer;

import com.boomi.proserv.apm.ComponentType;

import java.util.Map;

public class DatadogTracer extends OpenTracingTracer {

    public static final String DD_TRACEID   = "x-datadog-trace-id";
    public static final String DD_PARENTID  = "x-datadog-parent-id";
    public static final String DD_PREFIX    = "x-datadog";

    @Override
    protected boolean buildNewSpanWhenIgnoreTag() {
        return false;
    }

    @Override
    protected String getTraceparent(Map<String, String> properties){
        String traceparent = super.getTraceparent(properties);
        if(traceparent==null || "".equals(traceparent)) {
            traceparent = getDatadogParentId(properties);
        }

        return traceparent;
    }

    protected String getDatadogParentId(Map<String, String> properties) {
        String traceId = properties.get(HTTP_DOC_PREFIX + DD_TRACEID);
        setComponentType(ComponentType.HTTP);
        if(traceId == null || "".equals(traceId)) {
            traceId = properties.get(DD_TRACEID);
            setComponentType(ComponentType.JMS);
        }
        return traceId;
    }

    protected String getDatadogSpanId(Map<String, String> properties) {
        String traceId = properties.get(HTTP_DOC_PREFIX + DD_PARENTID);
        setComponentType(ComponentType.HTTP);
        if(traceId == null || "".equals(traceId)) {
            traceId = properties.get(DD_PARENTID);
            setComponentType(ComponentType.JMS);
        }
        return traceId;
    }

    @Override
    protected void enrich(Map<String, String> map, Map<String, String> properties) {
        super.enrich(map, properties);

        for (Map.Entry<String,String> entry: properties.entrySet()) {
            if(entry.getKey().startsWith(DD_PREFIX)) {
                map.put(entry.getKey(), entry.getValue());
            }
        }
    }

}
