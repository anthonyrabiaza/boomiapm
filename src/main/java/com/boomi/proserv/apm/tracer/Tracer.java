package com.boomi.proserv.apm.tracer;

import com.boomi.proserv.apm.BoomiContext;

import java.util.Map;
import java.util.logging.Logger;

public interface Tracer {
    public void start(Logger logger, BoomiContext context);
    public void stop(Logger logger, BoomiContext context);
    public void error(Logger logger, BoomiContext context);

    public void start(Logger logger, BoomiContext context, String document, Map<String, String> properties);
    public void stop(Logger logger, BoomiContext context, String document, Map<String, String> properties);
    public void error(Logger logger, BoomiContext context, String document, Map<String, String> properties);
}
