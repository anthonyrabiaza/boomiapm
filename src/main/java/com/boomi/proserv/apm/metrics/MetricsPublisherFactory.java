package com.boomi.proserv.apm.metrics;

public class MetricsPublisherFactory {
    static MetricsPublisher s_metricsPublisher = null;
    static String platform = null;

    static public MetricsPublisher getMetricsPublisher(String platform) {
        if(s_metricsPublisher != null && platform.equals(MetricsPublisherFactory.platform)) {
            return s_metricsPublisher;
        }

        switch(platform) {
            case "datadog":
                s_metricsPublisher = new DatadogMetricsPublisher();
                break;
            default:
                s_metricsPublisher = null;
                break;
        }

        MetricsPublisherFactory.platform = platform;
        return s_metricsPublisher;
    }
}
