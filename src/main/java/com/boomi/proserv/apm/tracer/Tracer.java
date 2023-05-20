package com.boomi.proserv.apm.tracer;

import com.boomi.connector.api.PayloadMetadata;
import com.boomi.proserv.apm.BoomiContext;
import com.boomi.proserv.apm.ComponentType;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public abstract class Tracer {

    protected static final String BOOMI_EXECUTION_ID            = "boomi.executionID";
    protected static final String BOOMI_PROCESS_NAME            = "boomi.processName";
    protected static final String BOOMI_CURRENT_PROCESS_NAME    = "currentProcessName";
    protected static final String BOOMI_PROCESS_ID              = "boomi.processID";
    protected static final String BOOMI_ERROR_MESSAGE           = "boomi.errorMessage";
    protected static final String BOOMI_ATTRIBUTES_PREFIX       = "boomi.";

    public static final String HTTP_DOC_PREFIX                  = "inheader_";

    /*w3c*/
    public static final String TRACEPARENT                      = "traceparent";
    public static final String TRACESTATE                       = "tracestate";

    //zipking b3
    /*
    * X-b3-traceid:25677a8c888a34b55925ac74318ba920
    * X-b3-parentspanid:ce535784dd999b75
    * X-b3-sampled:0
    * X-b3-spanid:725f63dfee03e834
    * */
    public static final String B3_TRACEID           = "x-b3-traceid";
    public static final String B3_SPANID            = "x-b3-spanid";

    /*APM Connector Tracked Property*/
    public static final String TRACEID              = "traceID";
    public static final String TRACEIDFORMATTED     = "traceIDFormatted";
    public static final String PARENTID             = "parentID";
    public static final String SPANID               = "spanID";

    public static final String SPANIDFORMATTED      = "spanIDFormatted";
    public static final String TRACEPAYLOAD         = "tracePayload";
    public static final String KEYVALUE_TAGS        = "keyvalueTags";
    public static final String KEYVALUE_SEPARATOR   = ";";
    public static final String KEYVALUE_EQUALS      = "=";

    /*protected static String s_serviceName;
    protected static String s_serviceVersion;*/

    /*Local variables*/
    private String platform;
    private ComponentType componentType;
    private String traceId;
    private String parentId;
    private String spanId;
    private String errorMessage;
    /*End Local variables*/

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public ComponentType getComponentType(){
        return componentType;
    }

    public void setComponentType(ComponentType componentType){
        this.componentType = componentType;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public void setTraceId(Logger logger, String traceId, PayloadMetadata metadata) {
        setTraceId(traceId);
        if(metadata!=null) {
            logger.info(TRACEID + ":" + traceId);
            metadata.setTrackedProperty(TRACEID, traceId);
            metadata.setTrackedProperty(TRACEIDFORMATTED, formatTraceId(traceId));
        }
    }

    public void setSpanId(Logger logger, String spanId, PayloadMetadata metadata) {
        setSpanId(spanId);
        if(metadata!=null) {
            logger.info(SPANID + ":" + spanId);
            metadata.setTrackedProperty(SPANID, spanId);
            metadata.setTrackedProperty(SPANIDFORMATTED, formatTraceId(SPANID));
        }
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public void setParentId(Logger logger, String parentId, PayloadMetadata metadata) {
        setParentId(parentId);
        if(metadata!=null) {
            logger.info(PARENTID + ":" + parentId);
            metadata.setTrackedProperty(PARENTID, parentId);
        }
    }

    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(String traceId) {
        this.spanId = traceId;
    }

    public void setTracePayload(Logger logger, String tracePayload, PayloadMetadata metadata) {
        if(metadata!=null) {
            logger.info(TRACEPAYLOAD + ":" + tracePayload);
            metadata.setTrackedProperty(TRACEPAYLOAD, tracePayload);
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

    public void save(Logger logger, BoomiContext context, String rtProcess, String document, Map<String, String> dynProps, Map<String, String> properties, PayloadMetadata metadata) {
        try {
            String currentProcessValue = BOOMI_CURRENT_PROCESS_NAME + KEYVALUE_EQUALS + context.getCurrentProcessName();
            if(dynProps==null || dynProps.size()==0 || dynProps.isEmpty()){
                dynProps = new HashMap<>();
                dynProps.put(KEYVALUE_TAGS, currentProcessValue);
            } else {
                String tags = dynProps.get(KEYVALUE_TAGS);
                tags = tags + KEYVALUE_SEPARATOR + currentProcessValue;
                dynProps.put(KEYVALUE_TAGS, tags);
            }
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

    protected boolean getDefaultValueBuildNewSpanWhenIgnoreTag() {
        return true;
    }

    protected boolean buildNewSpanWhenIgnoreTag() {
        String buildnewspanwhenignoretag = System.getProperty("boomiapm.buildnewspanwhenignoretag");
        if(buildnewspanwhenignoretag != null && !"".equals(buildnewspanwhenignoretag)) {
            return Boolean.parseBoolean(buildnewspanwhenignoretag);
        } else {
            return getDefaultValueBuildNewSpanWhenIgnoreTag();
        }
    }

    public String getBoomiExecutionIdKey() {
        return BOOMI_EXECUTION_ID;
    }

    public String getBoomiProcessNameKey() {
        return BOOMI_PROCESS_NAME;
    }

    public String getBoomiProcessIdKey() {
        return BOOMI_PROCESS_ID;
    }

    public String getBoomiErrorMessageKey() {
        return BOOMI_ERROR_MESSAGE;
    }

    public String getBoomiAttributesPrefix() {
        return BOOMI_ATTRIBUTES_PREFIX;
    }

    protected String getTraceparent(Map<String, String> properties){
        String traceparent = properties.get(HTTP_DOC_PREFIX + TRACEPARENT);//HTTP
        setComponentType(ComponentType.HTTP);
        if (traceparent == null || traceparent.equals("")) {
            traceparent = properties.get(TRACEPARENT);//JMS
            setComponentType(ComponentType.JMS);
        }
        return traceparent;
    }

    protected String getTracestate(Map<String, String> properties) {
        String tracestate = properties.get(HTTP_DOC_PREFIX + TRACESTATE);//HTTP
        setComponentType(ComponentType.HTTP);
        if (tracestate == null || tracestate.equals("")) {
            tracestate = properties.get(TRACESTATE);//JMS
            setComponentType(ComponentType.JMS);
        }
        return tracestate;
    }

    protected String getB3TraceId(Map<String, String> properties) {
        String traceId = properties.get(HTTP_DOC_PREFIX + B3_TRACEID);
        setComponentType(ComponentType.HTTP);
        if(traceId == null || "".equals(traceId)) {
            traceId = properties.get(B3_TRACEID);
            setComponentType(ComponentType.JMS);
        }
        return traceId;
    }

    protected String getB3SpanId(Map<String, String> properties) {
        String spanId = properties.get(HTTP_DOC_PREFIX + B3_SPANID);
        if(spanId == null || "".equals(spanId)) {
            spanId = properties.get(B3_SPANID);
        }
        return spanId;
    }

    protected Map<String, String> getTags(Map<String, String> dynProps) {
        Map<String, String> kvMap = new HashMap<String, String>();
        if(dynProps!=null) {
            String kvs = dynProps.get(KEYVALUE_TAGS);
            if (kvs != null && !"".equals(kvs)) {
                String[] kvArray = kvs.split(KEYVALUE_SEPARATOR);
                for (int i = 0; i < kvArray.length; i++) {
                    String kv = kvArray[i];
                    String[] kva = kv.split(KEYVALUE_EQUALS);
                    kvMap.put(getBoomiAttributesPrefix() + kva[0], kva[1]);
                }
            }
        }
        return kvMap;
    }

    static public String convertStringToUUID(String str) {
        if(str.length() == 16) {
            return "00000000-0000-0000-" + str.substring(0, 4) + "-" + str.substring(4, str.length());
        } else if (str.length() == 32){
            return str.substring(0, 8) + "-" + str.substring(8, 12) + "-" + str.substring(12, 16) + "-" + str.substring(16, 20) + "-" + str.substring(20, 32);
        } else if (str.length() == 36) {
            return str;
        } else {
            return "00000000-0000-0000-0000-000000000000";
        }
    }

    public String formatTraceId(String str) {
        return str;
    }

    protected Object invokeMethodIfExists(Object o, String methodName){
        Object result;
        Method[] methods = o.getClass().getMethods();
        for (Method m : methods) {
            if (m.getName().equals(methodName)) {
                try {
                    result = m.invoke(o, null);
                    return result;
                } catch (Exception ex) {
                    return null;
                }
            }
        }
        return null;
    }

    protected String getStringObject(Object o, String methodName){
        Object result = invokeMethodIfExists(o, methodName);
        if(result != null) {
            return result.toString();
        } else {
            return null;
        }
    }

    protected String getParentId(Object o){
        return getStringObject(o, "getParentId");
    }
}
