package com.boomi.proserv.apm.tracer;

import com.boomi.connector.api.PayloadMetadata;
import com.boomi.proserv.apm.BoomiContext;

import java.util.Map;
import java.util.logging.Logger;

public interface Tracer {
    public void start(Logger logger, BoomiContext context, PayloadMetadata metadata);
    public void stop(Logger logger, BoomiContext context, PayloadMetadata metadata);
    public void error(Logger logger, BoomiContext context, PayloadMetadata metadata);

    public void start(Logger logger, BoomiContext context, String rtProcess, String document, Map<String, String> properties, PayloadMetadata metadata);
    public void stop(Logger logger, BoomiContext context, String rtProcess, String document, Map<String, String> properties, PayloadMetadata metadata);
    public void error(Logger logger, BoomiContext context, String rtProcess, String document, Map<String, String> properties, PayloadMetadata metadata);
}
