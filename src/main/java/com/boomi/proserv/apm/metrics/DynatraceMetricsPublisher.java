package com.boomi.proserv.apm.metrics;

import com.boomi.proserv.apm.BoomiContext;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class DynatraceMetricsPublisher extends MetricsPublisher {

    @Override
    public void sendMetrics(Logger logger, BoomiContext boomiContext, String url, String apiKey, String appKey, String metric, String type, String value) {
        try {
            logger.info("Sending metrics to Dynatrace...");
            StringBuffer body = new StringBuffer();
            body.append(s_prefix);
            body.append(metric.trim());
            switch (type) {
                case "gauge":
                    body.append(" ");
                    body.append(value);
                    break;
                case "count":
                    body.append(" count,delta=");
                    body.append(value);
                    break;
                case "dimension":
                    body.append(" ");
                    body.append(value);
                    body.append(" ");
                    body.append(System.currentTimeMillis());
                    break;
            }

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "text/plain; charset=utf-8");
            String response = postRequest(url, body.toString(), headers);
            logger.info("Response:" + response);
        } catch (Exception e) {
            logger.severe("Error when Sending metrics to Dynatrace: " + e.getMessage());
            logger.severe(convertStackTraceToString(e));
        }
    }
}
