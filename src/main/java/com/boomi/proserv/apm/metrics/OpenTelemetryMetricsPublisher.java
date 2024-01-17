package com.boomi.proserv.apm.metrics;

import com.boomi.proserv.apm.BoomiContext;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;

import java.util.logging.Logger;

public class OpenTelemetryMetricsPublisher extends MetricsPublisher {

    public static final String IO_OPENTELEMETRY_BOOMI_METRICS   = "io.opentelemetry.boomi.metrics";
    public static final String IO_OPENTELEMETRY_BOOMI_VERSION   = "1.0.0";
    public static final String UNIT                             = "unit";

    @Override
    public void sendMetrics(Logger logger, BoomiContext boomiContext, String url, String apiKey, String appKey, String metric, String type, String value) {
        try {
            logger.info("Sending metrics to OpenTelemetry...");

            //Meter meter = GlobalMeterProvider.get().get(IO_OPENTELEMETRY_BOOMI_METRICS);//1.4.1

            OpenTelemetry openTelemetry = GlobalOpenTelemetry.get();

            Meter meter = openTelemetry.meterBuilder(IO_OPENTELEMETRY_BOOMI_METRICS)
                    .setInstrumentationVersion(IO_OPENTELEMETRY_BOOMI_VERSION)
                    .build();

            LongCounter recorder = meter
                            .counterBuilder(s_prefix + metric)
                            .setDescription("Reports Boomi metrics for " + s_prefix + metric)
                            .setUnit(UNIT)
                            .build();
            recorder.add(Long.valueOf(value));

            logger.info("OpenTelemetry metrics sent");
        } catch (Exception e) {
            logger.severe("Error when Sending metrics to OpenTelemetry: " + e.getMessage());
            logger.severe(convertStackTraceToString(e));
        }
    }
}
