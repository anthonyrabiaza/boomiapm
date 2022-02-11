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
                        addContext(logger, context, metadata);
                        setTracePayload(logger, newrelic, metadata);
                    } catch (Exception e) {
                        logger.severe("NewRelic trace not added " + e);
                    }
                } else {
                    logger.warning("NewRelic trace not found ");
                }
                break;
            case w3c:
                String traceparent  = getTraceparent(properties);
                String tracestate   = getTracestate(properties);

                if (traceparent!=null && !"".equals(traceparent) && tracestate!=null && !"".equals(tracestate)) {
                    try {
                        logger.info("Continuing transaction using newrelic w3c headers");
                        HeaderType headerType;
                        TransportType transportType;
                        switch (getComponentType()){
                            case JMS:
                                headerType      = HeaderType.MESSAGE;
                                transportType   = TransportType.JMS;
                                break;
                            case HTTP:
                            default:
                                headerType      = HeaderType.HTTP;
                                transportType   = TransportType.HTTPS;
                                break;
                        }
                        ConcurrentHashMapHeaders headers = ConcurrentHashMapHeaders.build(headerType);
                        headers.addHeader(TRACEPARENT, traceparent);
                        headers.addHeader(TRACESTATE,  tracestate);
                        NewRelic.getAgent().getTransaction().acceptDistributedTraceHeaders(transportType, headers);
                        addContext(logger, context, metadata);
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
                        setParentId(logger, parentID, metadata);
                        addContext(logger, context, metadata);
                    } catch (Exception e) {
                        logger.severe("NewRelic trace not added " + e);
                    }
                } else {
                    logger.warning("NewRelic parentid not found ");
                }
                break;
            default://TO BE TESTED
                try {
                    NewRelic.setTransactionName("Custom", context.getProcessName());
                    addContext(logger, context, metadata);
                } catch (Exception e) {
                    logger.severe("NewRelic context not added " + e);
                }
                break;
        }
        super.start(logger, context, rtProcess, document, dynProps, properties, metadata);
    }

    @Override
    public void stop(Logger logger, BoomiContext context, String rtProcess, String document, Map<String, String> dynProps, Map<String, String> properties, PayloadMetadata metadata) {
        super.stop(logger, context, rtProcess, document, dynProps, properties, metadata);
        RealTimeProcessing realTimeProcessing = RealTimeProcessing.getValue(rtProcess);
        switch (realTimeProcessing) {
            case payload:
                String newrelic = NewRelic.getAgent().getTransaction().createDistributedTracePayload().text();
                if(!"".equals(newrelic)){
                    setTracePayload(logger, Base64.getEncoder().encodeToString(newrelic.getBytes()), metadata);
                }
                break;
            case parentid:
                setTraceId(logger, NewRelic.getAgent().getTraceMetadata().getTraceId(), metadata);
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
                    setTracePayload(logger, Base64.getEncoder().encodeToString(newrelic.getBytes()), metadata);
                }
                break;
            case parentid:
                setTraceId(logger, NewRelic.getAgent().getTraceMetadata().getTraceId(), metadata);
                break;
            default:
                break;
        }
        NewRelic.addCustomParameter(getBoomiErrorMessageKey(), getErrorMessage());
    }

    protected void addContext(Logger logger, BoomiContext context, PayloadMetadata metadata) {
        NewRelic.addCustomParameter(getBoomiExecutionIdKey(), context.getExecutionId());
        NewRelic.addCustomParameter(getBoomiProcessNameKey(), context.getProcessName());
        NewRelic.addCustomParameter(getBoomiProcessIdKey(), context.getProcessId());
        setTraceId(logger, NewRelic.getAgent().getTraceMetadata().getTraceId(), metadata);
    }

    @Override
    protected void addTags(Map<String, String> dynProps) {
        Map<String, String> tags = getTags(dynProps);
        if(tags.size()>0) {
            for (Map.Entry<String, String> entry : tags.entrySet()) {
                NewRelic.addCustomParameter(entry.getKey(), entry.getValue());
            }
        }
    }
}
