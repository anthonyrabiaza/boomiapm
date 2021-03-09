package com.boomi.proserv.apm.tracer;

import com.boomi.execution.ExecutionUtil;
import com.boomi.proserv.apm.BoomiContext;

import java.util.Map;
import java.util.logging.Logger;

public class NewRelicPayloadTracer implements Tracer {
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
        String newrelic = properties.get("document.dynamic.userdefined.inheader_newrelic");//HTTP
        if(newrelic == null && newrelic.equals("")) {
            newrelic = properties.get("document.dynamic.userdefined.newrelic");//JMS
        }

        if(!"".equals(newrelic)) {
            try {
                logger.info("Continuing transaction using newrelic payload");
                com.newrelic.api.agent.NewRelic.getAgent().getTransaction().acceptDistributedTracePayload(newrelic);
                com.newrelic.api.agent.NewRelic.addCustomParameter("boomi.executionID", context.getExecutionId());
                com.newrelic.api.agent.NewRelic.addCustomParameter("boomi.processName", context.getProcessName());
                com.newrelic.api.agent.NewRelic.addCustomParameter("boomi.processID", context.getProcessId());
                ExecutionUtil.setDynamicProcessProperty("DPP_newrelic", newrelic, false);
            } catch (Exception e) {
                logger.severe("NewRelic trace not added " + e);
            }
        }
    }

    @Override
    public void stop(Logger logger, BoomiContext context, String document, Map<String, String> properties) {
        String newrelic = ExecutionUtil.getDynamicProcessProperty("DPP_newrelic");
        if(!"".equals(newrelic)){
            properties.put("document.dynamic.userdefined.outheader_newrelic", newrelic);
        }
    }

    @Override
    public void error(Logger logger, BoomiContext context, String document, Map<String, String> properties) {
        String newrelic = ExecutionUtil.getDynamicProcessProperty("DPP_newrelic");
        if(!"".equals(newrelic)){
            properties.put("document.dynamic.userdefined.outheader_newrelic", newrelic);
        }
    }
}
