package com.boomi.proserv.apm.tracer;

import com.boomi.connector.api.PayloadMetadata;
import com.boomi.proserv.apm.BoomiContext;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public abstract class Tracer {

    protected static String s_serviceName;
    protected static String s_serviceVersion;

    private String traceId;

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public void setTraceId(Logger logger, String traceId, PayloadMetadata metadata) {
        this.traceId = traceId;
        if(metadata!=null) {
            logger.info("traceID:" + traceId);
            metadata.setTrackedProperty("traceID", getTraceId());
        }
    }

    public void start(Logger logger, BoomiContext context, String rtProcess, String document, Map<String, String> dynProps, Map<String, String> properties, PayloadMetadata metadata) {
        try {
            addTags(dynProps);
        } catch (Exception e) {
            logger.severe("Error adding tags:" + e.getMessage());
        }
    }
    public void stop(Logger logger, BoomiContext context, String rtProcess, String document, Map<String, String> dynProps, Map<String, String> properties, PayloadMetadata metadata) {
        try {
            addTags(dynProps);
        } catch (Exception e) {
            logger.severe("Error adding tags:" + e.getMessage());
        }
    }
    public void error(Logger logger, BoomiContext context, String rtProcess, String document, Map<String, String> dynProps, Map<String, String> properties, PayloadMetadata metadata) {
        try {
            addTags(dynProps);
        } catch (Exception e) {
            logger.severe("Error adding tags:" + e.getMessage());
        }
    }

    protected abstract void addTags(Map<String, String> dynProps);

    protected String getTraceparent(Map<String, String> properties){
        String traceparent = properties.get("inheader_traceparent");//HTTP
        if (traceparent == null || traceparent.equals("")) {
            traceparent = properties.get("traceparent");//JMS
        }
        return traceparent;
    }

    protected String getTracestate(Map<String, String> properties) {
        String tracestate = properties.get("inheader_tracestate");//HTTP
        if (tracestate == null || tracestate.equals("")) {
            tracestate = properties.get("tracestate");//JMS
        }
        return tracestate;
    }

    protected Map<String, String> getTags(Map<String, String> dynProps) {
        Map<String, String> kvMap = new HashMap<String, String>();
        if(dynProps!=null) {
            String kvs = dynProps.get("keyvalueTags");
            if (kvs != null && !"".equals(kvs)) {
                String[] kvArray = kvs.split(";");
                for (int i = 0; i < kvArray.length; i++) {
                    String kv = kvArray[i];
                    String[] kva = kv.split("=");
                    kvMap.put("boomi." + kva[0], kva[1]);
                }
            }
        }
        return kvMap;
    }
}
