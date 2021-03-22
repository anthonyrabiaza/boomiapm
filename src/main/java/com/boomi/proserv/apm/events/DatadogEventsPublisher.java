package com.boomi.proserv.apm.events;

import com.boomi.proserv.apm.BoomiContext;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class DatadogEventsPublisher extends EventsPublisher {

    @Override
    public void sendEvents(Logger logger, BoomiContext boomiContext, String url, String apiKey, String appKey, String traceId, boolean error) {
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
            body.append(traceId);
            body.append("\\n");
            body.append("%%% \\n");//Markdown start
            body.append("[See APM Trace]");
            body.append("(");
            body.append("/apm/trace/");body.append(traceId);
            body.append(") | ");
            body.append("[See Process Execution]");
            body.append("(");
            body.append("https://platform.boomi.com/#search;accountId=");body.append(boomiContext.getAccountId());body.append(";executionId=");body.append(boomiContext.getExecutionId());
            body.append(") | ");
            body.append("[See Logs]");
            body.append("(");
            body.append("/logs?from_ts=");body.append(getTimestampMinusOrPlusSeconds(-10));body.append("000");
            body.append("&to_ts=");body.append(getTimestampMinusOrPlusSeconds(+10));body.append("999");
            body.append("&index=%2A");//*
            body.append("&query=service%3A");body.append(boomiContext.getServiceName());
            body.append("+host%3A");body.append(getHostname());
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
            body.append(getTimestamp());
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
            String response = postRequest(url, body.toString(), headers);
            logger.info("Response:" + response);
        } catch (Exception e) {
            logger.severe("Error when Sending events to Datadog: " + e.getMessage());
            logger.severe(convertStackTraceToString(e));
        }
    }
}
