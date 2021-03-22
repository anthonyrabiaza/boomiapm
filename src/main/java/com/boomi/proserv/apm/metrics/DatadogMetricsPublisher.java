package com.boomi.proserv.apm.metrics;

import com.boomi.proserv.apm.BoomiContext;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class DatadogMetricsPublisher extends MetricsPublisher {
    @Override
    public void sendMetrics(Logger logger, BoomiContext boomiContext, String url, String apiKey, String appKey, String metric, String type, String value) {
        try {
            logger.info("Sending metrics to Datadog...");
            StringBuffer body = new StringBuffer();
            body.append("{");

            body.append("\"series\": [");
            body.append("{");

            body.append("\"metric\": \"");
            body.append(s_prefix);
            body.append(cleanUp(metric));
            body.append("\",");
            body.append("\"points\": [[\"");
            body.append(getTimestamp());
            body.append("\",");
            body.append(cleanUp(value));
            body.append("]],");//points

            body.append("\"host\": \"");
            body.append(getHostname());
            body.append("\"");

            body.append("}");
            body.append("]");//series

            body.append("}");

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json");
            headers.put("DD-API-KEY", apiKey);
            headers.put("DD-APPLICATION-KEY", appKey);
            String response = postRequest(url, body.toString(), headers);
            logger.info("Response:" + response);
        } catch (Exception e) {
            logger.severe("Error when Sending metrics to Datadog: " + e.getMessage());
            logger.severe(convertStackTraceToString(e));
        }
    }
}
