package com.boomi.proserv.apm.tracer;

import com.boomi.execution.ExecutionUtil;
import com.boomi.proserv.apm.BoomiContext;

import java.util.Map;
import java.util.logging.Logger;

public class NewRelicParentIdTracer implements Tracer {
    @Override
    public void start(Logger logger, BoomiContext context) {

    }

    @Override
    public void stop(Logger logger, BoomiContext context) {

    }

    @Override
    public void error(Logger logger, BoomiContext context) {

    }

    @Override
    public void start(Logger logger, BoomiContext context, String document, Map<String, String> properties) {
        String parentID = properties.get("document.dynamic.userdefined.inheader_parentid");
        String traceID  = "";

        if(!"".equals(parentID)) {
            try {
                logger.info("Continuing transaction using newrelic parentId");
                com.newrelic.api.agent.NewRelic.addCustomParameter("parentId", parentID);
                ExecutionUtil.setDynamicProcessProperty("DPP_parentID", parentID, false);

                traceID = com.newrelic.api.agent.NewRelic.getAgent().getTraceMetadata().getTraceId();
                ExecutionUtil.setDynamicProcessProperty("DPP_traceID", traceID, false);

                logger.info("DPP_traceID:" + traceID);
            } catch (Exception e) {
                logger.severe("NewRelic trace not added " + e);
            }
        }
    }

    @Override
    public void stop(Logger logger, BoomiContext context, String document, Map<String, String> properties) {
        properties.put("document.dynamic.userdefined.outheader_trace", ExecutionUtil.getDynamicProcessProperty("DPP_traceID"));
    }

    @Override
    public void error(Logger logger, BoomiContext context, String document, Map<String, String> properties) {
        properties.put("document.dynamic.userdefined.outheader_trace", ExecutionUtil.getDynamicProcessProperty("DPP_traceID"));
    }
}
