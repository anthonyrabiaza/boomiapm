package com.boomi.proserv.apm.tracer;

public class AWSXRayOpenTelemetryTracer extends OpenTelemetryTracer {

    private static final String BOOMI_EXECUTION_ID      = "boomi_executionID";
    private static final String BOOMI_PROCESS_NAME      = "boomi_processName";
    private static final String BOOMI_PROCESS_ID        = "boomi_processID";
    private static final String BOOMI_ERROR_MESSAGE     = "boomi_errorMessage";
    private static final String BOOMI_ATTRIBUTES_PREFIX = "boomi_";

    @Override
    protected boolean addProcessNameAsURLWhenIgnoreTag() {
        return true;
    }

    @Override
    public String formatTraceId(String str) {
        if(str.length() == 32){
            return "1-" + str.substring(0, 8) + "-" + str.substring(8, 32);
        } else if (str.length() == 35) {
            return str;
        } else {
            return "1-00000000-000000000000000000000000";
        }
    }

    @Override
    public String getBoomiExecutionIdKey() {
        return BOOMI_EXECUTION_ID;
    }

    @Override
    public String getBoomiProcessNameKey() {
        return BOOMI_PROCESS_NAME;
    }

    @Override
    public String getBoomiProcessIdKey() {
        return BOOMI_PROCESS_ID;
    }

    @Override
    public String getBoomiErrorMessageKey() {
        return BOOMI_ERROR_MESSAGE;
    }

    @Override
    public String getBoomiAttributesPrefix() {
        return BOOMI_ATTRIBUTES_PREFIX;
    }
}
