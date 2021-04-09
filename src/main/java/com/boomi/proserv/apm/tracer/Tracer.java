package com.boomi.proserv.apm.tracer;

import com.boomi.connector.api.PayloadMetadata;
import com.boomi.proserv.apm.BoomiContext;
import com.boomi.proserv.apm.ComponentType;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public abstract class Tracer {

    public static final String BOOMI_EXECUTION_ID   = "boomi.executionID";
    public static final String BOOMI_PROCESS_NAME   = "boomi.processName";
    public static final String BOOMI_PROCESS_ID     = "boomi.processID";
    public static final String BOOMI_ERROR_MESSAGE  = "boomi.errorMessage";

    public static final String TRACEPARENT          = "traceparent";
    public static final String TRACESTATE           = "tracestate";
    protected static String s_serviceName;
    protected static String s_serviceVersion;

    protected ComponentType componentType = ComponentType.HTTP;

    public ComponentType getComponentType(){
        return componentType;
    }

    private String traceId;

    private String errorMessage;

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

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
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
            traceparent = properties.get(TRACEPARENT);//JMS
            componentType = ComponentType.JMS;
        }
        return traceparent;
    }

    protected String getTracestate(Map<String, String> properties) {
        String tracestate = properties.get("inheader_tracestate");//HTTP
        if (tracestate == null || tracestate.equals("")) {
            tracestate = properties.get(TRACESTATE);//JMS
            componentType = ComponentType.JMS;
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
