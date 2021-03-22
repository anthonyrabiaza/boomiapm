package com.boomi.proserv.apm.metrics;

import com.boomi.proserv.apm.BoomiContext;
import com.boomi.proserv.apm.Observer;

import java.util.logging.Logger;

public abstract class MetricsPublisher extends Observer {
    public abstract void sendMetrics(Logger logger, BoomiContext boomiContext, String url, String apiKey, String appKey, String metric, String type, String value);
    public String cleanUp(String str) {
        str = str.trim();
        str = str.replaceAll("[^a-zA-Z0-9\\.]+", "");
        return str;
    }
}
