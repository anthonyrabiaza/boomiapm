package com.boomi.proserv.apm.metrics;

import com.appdynamics.agent.api.AppdynamicsAgent;
import com.appdynamics.agent.api.MetricPublisher;
import com.boomi.proserv.apm.BoomiContext;

import java.util.logging.Logger;

public class AppDynamicsMetricsPublisher extends MetricsPublisher {

    @Override
    public void sendMetrics(Logger logger, BoomiContext boomiContext, String url, String apiKey, String appKey, String metric, String type, String value) {
        try {
            logger.info("Sending metrics to AppDynamics...");
            MetricPublisher metricPublisher = AppdynamicsAgent.getMetricPublisher();
            String metricName =  "Server|Component:" + System.getProperty("appdynamics.agent.tierName") +"|Custom Metrics|" + s_prefix + metric;
            switch(type) {
                case "average":
                    metricPublisher.reportAverageMetric(metricName, Long.valueOf(value));
                    break;
                case "sum":
                    metricPublisher.reportSumMetric(metricName, Long.valueOf(value));
                    break;
                case "observed":
                    metricPublisher.reportObservedMetric(metricName, Long.valueOf(value));
                    break;
            }

        } catch (Exception e) {
            logger.severe("Error when Sending metrics to AppDynamics: " + e.getMessage());
            logger.severe(convertStackTraceToString(e));
        }
    }
}
