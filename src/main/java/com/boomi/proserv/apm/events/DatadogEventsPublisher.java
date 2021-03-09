package com.boomi.proserv.apm.events;

import com.boomi.execution.ExecutionUtil;
import com.boomi.proserv.apm.BoomiContext;
import datadog.trace.api.DisableTestTrace;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class DatadogEventsPublisher extends EventsPublisher {

    @DisableTestTrace
    @Override
    public void sendEvents(Logger logger, BoomiContext boomiContext, String url, String apiKey, String appKey, boolean error) {
        try {
            logger.info("Sending events to Datadog...");
            StringBuffer body = new StringBuffer();
            body.append("{");

            body.append("\"title\": \"");
            if(error) {
                body.append(EVENT_TITLE_ERROR);
            } else {
                body.append(EVENT_TITLE_SUCCESS);
            }
            body.append("\",");

            body.append("\"text\": \"");
            if(error) {
                body.append(EVENT_TEXT_ERROR);
            } else {
                body.append(EVENT_TEXT_SUCCESS);
            }
            body.append(boomiContext.getProcessName());
            body.append(" , executionId is ");
            body.append(boomiContext.getExecutionId());
            body.append(" , traceId is ");
            body.append(ExecutionUtil.getDynamicProcessProperty("DPP_traceID"));
            body.append("\\n");
            body.append("%%% \\n");//Markdown start
            body.append("[See APM Trace]");
            body.append("(");
            body.append("/apm/trace/");body.append(ExecutionUtil.getDynamicProcessProperty("DPP_traceID"));
            body.append(") · ");
            body.append("[See Process Execution]");
            body.append("(");
            body.append("https://platform.boomi.com/#search;accountId=");body.append(boomiContext.getAccountId());body.append(";executionId=");body.append(boomiContext.getExecutionId());
            body.append(")");
            body.append("\\n %%%");//Markdown end
            body.append("\",");

            body.append("\"aggregation_key\": \"");
            body.append(boomiContext.getExecutionId());
            body.append("\",");

            body.append("\"alert_type\": \"");
            if(error) {
                body.append(EVENT_ERROR);
            } else {
                body.append(EVENT_SUCCESS);
            }
            body.append("\",");

            body.append("\"date_happened\": ");
            body.append(System.currentTimeMillis() / 1000L);
            body.append(",");

            body.append("\"device_name\": \"");
            body.append("boomi");
            body.append("\",");

            body.append("\"host\": \"");
            body.append(getHostname());
            body.append("\",");

            body.append("\"priority\": \"");
            body.append("normal");
            body.append("\",");

            body.append("\"source_type_name\": \"");
            body.append("JAVA");
            body.append("\",");

            body.append("\"tags\": [");
            body.append("\"env:");
            body.append(System.getProperty("dd.env"));
            body.append("\",");
            body.append("\"boomiProcess:");
            body.append(boomiContext.getProcessName());
            body.append("\"");
            body.append("]");

            body.append("}");

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json");
            headers.put("DD-API-KEY", apiKey);
            headers.put("DD-APPLICATION-KEY", appKey);
            postRequest(url, body.toString(), headers);
        } catch (Exception e) {
            logger.severe("Error when Sending events to Datadog: " + e.getMessage());
            logger.severe(convertStackTraceToString(e));
        }
    }
}
