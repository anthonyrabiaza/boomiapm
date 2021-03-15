package com.boomi.proserv.apm.tracer;

import com.boomi.connector.api.PayloadMetadata;
import com.boomi.proserv.apm.BoomiContext;

import com.newrelic.api.agent.ConcurrentHashMapHeaders;
import com.newrelic.api.agent.HeaderType;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.TransportType;

import java.util.Base64;
import java.util.Map;
import java.util.logging.Logger;

public class NewRelicTracer extends Tracer {
    @Override
    public void start(Logger logger, BoomiContext context, String rtProcess, String document, Map<String, String> dynProps, Map<String, String> properties, PayloadMetadata metadata) {
        super.start(logger, context, rtProcess, document, dynProps, properties, metadata);

        RealTimeProcessing realTimeProcessing = RealTimeProcessing.getValue(rtProcess);
        switch (realTimeProcessing) {
            case payload:
                String newrelic = properties.get("inheader_newrelic");//HTTP
                if (newrelic == null || newrelic.equals("")) {
                    newrelic = properties.get("newrelic");//JMS
                }

                if (newrelic!=null && !"".equals(newrelic)) {
                    try {
                        logger.info("Continuing transaction using newrelic payload");
                        NewRelic.getAgent().getTransaction().acceptDistributedTracePayload(newrelic);
                        NewRelic.addCustomParameter("boomi.executionID", context.getExecutionId());
                        NewRelic.addCustomParameter("boomi.processName", context.getProcessName());
                        NewRelic.addCustomParameter("boomi.processID", context.getProcessId());
                        metadata.setTrackedProperty("tracePayload", newrelic);
                    } catch (Exception e) {
                        logger.severe("NewRelic trace not added " + e);
                    }
                } else {
                    logger.warning("NewRelic trace not found ");
                }
                break;
            case w3c://TO BE TESTED
                String traceparent  = getTraceparent(properties);
                String tracestate   = getTracestate(properties);

                if (traceparent!=null && !"".equals(traceparent) && tracestate!=null && !"".equals(tracestate)) {
                    try {
                        logger.info("Continuing transaction using newrelic w3c");
                        ConcurrentHashMapHeaders headers = ConcurrentHashMapHeaders.build(HeaderType.HTTP);
                        headers.addHeader("traceparent", traceparent);
                        headers.addHeader("tracestate", tracestate);
                        NewRelic.getAgent().getTransaction().acceptDistributedTraceHeaders(TransportType.HTTP, headers);
                        NewRelic.addCustomParameter("boomi.executionID", context.getExecutionId());
                        NewRelic.addCustomParameter("boomi.processName", context.getProcessName());
                        NewRelic.addCustomParameter("boomi.processID", context.getProcessId());
                        //metadata.setTrackedProperty("tracePayload", newrelic);
                    } catch (Exception e) {
                        logger.severe("NewRelic trace not added " + e);
                    }
                } else {
                    logger.warning("NewRelic trace not found ");
                }
                break;
            case parentid:
                String parentID = properties.get("inheader_parentid");
                String traceID  = "";

                if(parentID!=null && !"".equals(parentID)) {
                    try {
                        logger.info("Continuing transaction using newrelic parentId");
                        NewRelic.addCustomParameter("parentId", parentID);
                        metadata.setTrackedProperty("parentID", parentID);

                        traceID = NewRelic.getAgent().getTraceMetadata().getTraceId();
                        setTraceId(traceID);
                        metadata.setTrackedProperty("traceID", traceID);

                        logger.info("traceID:" + traceID);
                    } catch (Exception e) {
                        logger.severe("NewRelic trace not added " + e);
                    }
                } else {
                    logger.warning("NewRelic parentid not found ");
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void stop(Logger logger, BoomiContext context, String rtProcess, String document, Map<String, String> dynProps, Map<String, String> properties, PayloadMetadata metadata) {
        super.stop(logger, context, rtProcess, document, dynProps, properties, metadata);

        RealTimeProcessing realTimeProcessing = RealTimeProcessing.getValue(rtProcess);
        switch (realTimeProcessing) {
            case payload:
                String newrelic = NewRelic.getAgent().getTransaction().createDistributedTracePayload().text();
                if(!"".equals(newrelic)){
                    metadata.setTrackedProperty("tracePayload", Base64.getEncoder().encodeToString(newrelic.getBytes()));
                }
                break;
            case parentid:
                metadata.setTrackedProperty("traceID", NewRelic.getAgent().getTraceMetadata().getTraceId());
                break;
            default:
                break;
        }
    }

    @Override
    public void error(Logger logger, BoomiContext context, String rtProcess, String document, Map<String, String> dynProps, Map<String, String> properties, PayloadMetadata metadata) {
        super.error(logger, context, rtProcess, document, dynProps, properties, metadata);

        RealTimeProcessing realTimeProcessing = RealTimeProcessing.getValue(rtProcess);
        switch (realTimeProcessing) {
            case payload:
                String newrelic = NewRelic.getAgent().getTransaction().createDistributedTracePayload().text();
                if(!"".equals(newrelic)){
                    metadata.setTrackedProperty("tracePayload", Base64.getEncoder().encodeToString(newrelic.getBytes()));
                }
                break;
            case parentid:
                metadata.setTrackedProperty("traceID", NewRelic.getAgent().getTraceMetadata().getTraceId());
                break;
            default:
                break;
        }
    }

    protected void addTags(Map<String, String> dynProps) {
        Map<String, String> tags = getTags(dynProps);
        if(tags.size()>0) {
            for (Map.Entry<String, String> entry : tags.entrySet()) {
                NewRelic.addCustomParameter(entry.getKey(), entry.getValue());
            }
        }
    }
}
