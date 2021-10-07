package com.boomi.proserv.apm.metrics;

import com.boomi.proserv.apm.BoomiContext;
import io.opentelemetry.api.metrics.GlobalMeterProvider;
import io.opentelemetry.api.metrics.LongValueRecorder;
import io.opentelemetry.api.metrics.Meter;

import java.util.logging.Logger;

public class OpenTelemetryMetricsPublisher extends MetricsPublisher {

    public static final String IO_OPENTELEMETRY_BOOMI_METRICS   = "io.opentelemetry.boomi.metrics";
    public static final String UNIT                             = "unit";

    @Override
    public void sendMetrics(Logger logger, BoomiContext boomiContext, String url, String apiKey, String appKey, String metric, String type, String value) {
        try {
            logger.info("Sending metrics to OpenTelemetry...");

            Meter meter = GlobalMeterProvider.get().get(IO_OPENTELEMETRY_BOOMI_METRICS);
            LongValueRecorder recorder = meter
                            .longValueRecorderBuilder(s_prefix + metric)
                            .setDescription("Reports Boomi metrics for " + s_prefix + metric)
                            .setUnit(UNIT)
                            .build();
            recorder.record(Long.valueOf(value));

            logger.info("OpenTelemetry metrics sent");
        } catch (Exception e) {
            logger.severe("Error when Sending metrics to OpenTelemetry: " + e.getMessage());
            logger.severe(convertStackTraceToString(e));
        }
    }
}
