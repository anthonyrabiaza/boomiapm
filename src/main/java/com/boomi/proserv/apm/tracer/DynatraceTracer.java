package com.boomi.proserv.apm.tracer;

import com.boomi.connector.api.PayloadMetadata;
import com.boomi.proserv.apm.BoomiContext;
import com.dynatrace.oneagent.sdk.OneAgentSDKFactory;
import com.dynatrace.oneagent.sdk.api.CustomServiceTracer;
import com.dynatrace.oneagent.sdk.api.OneAgentSDK;

import java.util.Map;
import java.util.logging.Logger;

public class DynatraceTracer extends Tracer {

    protected static final String SERVICE = "Boomi Scheduled Processes";

    protected static OneAgentSDK s_oneAgentSdk;
    protected static ThreadLocal<CustomServiceTracer> tracer = new ThreadLocal<CustomServiceTracer>();

    @Override
    public void start(Logger logger, BoomiContext context, String rtProcess, String document, Map<String, String> dynProps, Map<String, String> properties, PayloadMetadata metadata) {
        try {
            RealTimeProcessing realTimeProcessing = RealTimeProcessing.getValue(rtProcess);
            if(RealTimeProcessing.ignore.equals(realTimeProcessing)){
                logger.info("Creating Dynatrace trace ...");
                CustomServiceTracer tracer = getDynatraceTracer(logger, context, SERVICE);
                tracer.start();
                getAgent(logger).addCustomRequestAttribute(getBoomiExecutionIdKey(), context.getExecutionId());
                getAgent(logger).addCustomRequestAttribute(getBoomiProcessNameKey(), context.getProcessName());
                getAgent(logger).addCustomRequestAttribute(getBoomiProcessIdKey(), context.getProcessId());
                logger.info("Dynatrace trace added");
            }
        } catch (Exception e) {
            logger.severe("Error adding creating Dynatrace trace:" + e.getMessage());
        }
        super.start(logger, context, rtProcess, document, dynProps, properties, metadata);
    }

    @Override
    public void stop(Logger logger, BoomiContext context, String rtProcess, String document, Map<String, String> dynProps, Map<String, String> properties, PayloadMetadata metadata) {
        super.stop(logger, context, rtProcess, document, dynProps, properties, metadata);
        try {
            RealTimeProcessing realTimeProcessing = RealTimeProcessing.getValue(rtProcess);
            if(RealTimeProcessing.ignore.equals(realTimeProcessing)) {
                logger.info("Closing Dynatrace trace ...");
                CustomServiceTracer tracer = getDynatraceTracer(logger, context, SERVICE);
                tracer.end();
            }
        } catch (Exception e) {
            logger.severe("Dynatrace trace not closed:" + e.getMessage());
        }
    }

    @Override
    public void error(Logger logger, BoomiContext context, String rtProcess, String document, Map<String, String> dynProps, Map<String, String> properties, PayloadMetadata metadata) {
        super.error(logger, context, rtProcess, document, dynProps, properties, metadata);
        try {
            RealTimeProcessing realTimeProcessing = RealTimeProcessing.getValue(rtProcess);
            if(RealTimeProcessing.ignore.equals(realTimeProcessing)) {
                logger.info("Closing Dynatrace trace with Error...");
                CustomServiceTracer tracer = getDynatraceTracer(logger, context, SERVICE);
                tracer.error(getErrorMessage());
            }
        } catch (Exception e) {
            logger.severe("Dynatrace trace not closed:" + e.getMessage());
        }
    }

    @Override
    protected void addTags(Map<String, String> dynProps) {
        Map<String, String> tags = getTags(dynProps);
        if(tags.size()>0) {
            for (Map.Entry<String, String> entry : tags.entrySet()) {
                getAgent(null).addCustomRequestAttribute(entry.getKey(), entry.getValue());
            }
        }
    }

    protected static OneAgentSDK getAgent(Logger logger) {
        if(s_oneAgentSdk == null) {
            s_oneAgentSdk = OneAgentSDKFactory.createInstance();
        }

        switch (s_oneAgentSdk.getCurrentState()) {
            case ACTIVE:
                break;
            case PERMANENTLY_INACTIVE:
                if(logger!=null) {
                    logger.severe("OneAgentSDK is permanently inactive!");
                }
                break;
            case TEMPORARILY_INACTIVE:
                if(logger!=null) {
                    logger.warning("OneAgentSDK is temporarily inactive!");
                }
                break;
            default:
                break;
        }
        return s_oneAgentSdk;
    }

    protected CustomServiceTracer getDynatraceTracer(Logger logger, BoomiContext context, String service) {
        CustomServiceTracer localTracer = tracer.get();
        if(localTracer == null) {
            localTracer = getAgent(logger).traceCustomService(context.getProcessName(), service);
            tracer.set(localTracer);
        }
        return localTracer;
    }
}
