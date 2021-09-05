package com.boomi.proserv;

import com.boomi.proserv.apm.BoomiContext;
import com.boomi.proserv.apm.tracer.Tracer;
import com.boomi.proserv.apm.tracer.TracerFactory;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

public class OpenTelemetryTracerTest {

    @Test
    void test() {
        if(1==1) {
            Tracer tracer = TracerFactory.getTracer("opentelemetry");
            BoomiContext boomiContext = new BoomiContext("testService", "execution123", "processABC", "processABC", "account123");
            Logger logger = Logger.getLogger(this.toString());
            tracer.start(
                    logger,
                    boomiContext,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }
    }
}
