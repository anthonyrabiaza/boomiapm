package com.boomi.proserv.apm.tracer;

import com.boomi.connector.api.PayloadMetadata;
import com.boomi.proserv.apm.BoomiContext;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.logging.Logger;

public class NewRelicTracer implements Tracer {
    @Override
    public void start(Logger logger, BoomiContext context, PayloadMetadata metadata) {

    }

    @Override
    public void stop(Logger logger, BoomiContext context, PayloadMetadata metadata) {

    }

    @Override
    public void error(Logger logger, BoomiContext context, PayloadMetadata metadata) {

    }

    @Override
    public void start(Logger logger, BoomiContext context, String rtProcess, String document, Map<String, String> properties, PayloadMetadata metadata) {
        RealTimeProcessing realTimeProcessing = RealTimeProcessing.getValue(rtProcess);
        switch (realTimeProcessing) {
            case payload:
                String newrelic = properties.get("inheader_newrelic");//HTTP
                if (newrelic == null || newrelic.equals("")) {
                    newrelic = properties.get("newrelic");//JMS
                }

                if (!"".equals(newrelic)) {
                    try {
                        logger.info("Continuing transaction using newrelic payload");
                        com.newrelic.api.agent.NewRelic.getAgent().getTransaction().acceptDistributedTracePayload(newrelic);
                        com.newrelic.api.agent.NewRelic.addCustomParameter("boomi.executionID", context.getExecutionId());
                        com.newrelic.api.agent.NewRelic.addCustomParameter("boomi.processName", context.getProcessName());
                        com.newrelic.api.agent.NewRelic.addCustomParameter("boomi.processID", context.getProcessId());
                        metadata.setTrackedProperty("tracePayload", newrelic);
                    } catch (Exception e) {
                        logger.severe("NewRelic trace not added " + e);
                    }
                }
                break;
            case parentid:
                String parentID = properties.get("inheader_parentid");
                String traceID  = "";

                if(!"".equals(parentID)) {
                    try {
                        logger.info("Continuing transaction using newrelic parentId");
                        com.newrelic.api.agent.NewRelic.addCustomParameter("parentId", parentID);
                        metadata.setTrackedProperty("parentID", parentID);

                        traceID = com.newrelic.api.agent.NewRelic.getAgent().getTraceMetadata().getTraceId();
                        metadata.setTrackedProperty("traceID", traceID);

                        logger.info("traceID:" + traceID);
                    } catch (Exception e) {
                        logger.severe("NewRelic trace not added " + e);
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void stop(Logger logger, BoomiContext context, String rtProcess, String document, Map<String, String> properties, PayloadMetadata metadata) {
        RealTimeProcessing realTimeProcessing = RealTimeProcessing.getValue(rtProcess);
        switch (realTimeProcessing) {
            case payload:
                String newrelic = com.newrelic.api.agent.NewRelic.getAgent().getTransaction().createDistributedTracePayload().text();
                if(!"".equals(newrelic)){
                    metadata.setTrackedProperty("tracePayload", Base64.getEncoder().encodeToString(newrelic.getBytes()));
                }
                break;
            case parentid:
                metadata.setTrackedProperty("traceID", com.newrelic.api.agent.NewRelic.getAgent().getTraceMetadata().getTraceId());
                break;
            default:
                break;
        }

    }

    @Override
    public void error(Logger logger, BoomiContext context, String rtProcess, String document, Map<String, String> properties, PayloadMetadata metadata) {
        RealTimeProcessing realTimeProcessing = RealTimeProcessing.getValue(rtProcess);
        switch (realTimeProcessing) {
            case payload:
                String newrelic = com.newrelic.api.agent.NewRelic.getAgent().getTransaction().createDistributedTracePayload().text();
                if(!"".equals(newrelic)){
                    metadata.setTrackedProperty("tracePayload", Base64.getEncoder().encodeToString(newrelic.getBytes()));
                }
                break;
            case parentid:
                metadata.setTrackedProperty("traceID", com.newrelic.api.agent.NewRelic.getAgent().getTraceMetadata().getTraceId());
                break;
            default:
                break;
        }

    }
}
