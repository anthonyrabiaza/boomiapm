package com.boomi.proserv.apm.tracer;

import com.boomi.proserv.apm.ComponentType;

import java.util.Map;

public class ElasticAPMTracer extends OpenTracingResolver {

    public static final String ELASTIC_TRACEPARENT   = "elastic_apm_traceparent";

    @Override
    protected String getTraceparent(Map<String, String> properties){
        String traceparent = properties.get(HTTP_DOC_PREFIX + ELASTIC_TRACEPARENT);//HTTP
        setComponentType(ComponentType.HTTP);
        if (traceparent == null || traceparent.equals("")) {
            traceparent = properties.get(ELASTIC_TRACEPARENT);//JMS
            setComponentType(ComponentType.JMS);
        }

        //fallback
        if (traceparent == null || traceparent.equals("")) {
            traceparent = super.getTraceparent(properties);
        }
        return traceparent;
    }

}
