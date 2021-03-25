package com.boomi.proserv.apm.tracer;

public class TracerFactory {
    static Tracer s_tracer     = null;
    static String s_platform   = null;

    static public Tracer getTracer(String platform) {
        if(s_tracer != null && platform.equals(s_platform)) {
            return s_tracer;
        }

        switch(platform) {
            case "appdynamics":
                s_tracer = new AppDynamicsTracer();
                break;
            case "datadog":
            case "opentracing":
                s_tracer = new OpenTracingTracer();
                break;
            case "elasticapm":
                s_tracer = new ElasticAPMTracer();
                break;
            case "lightstep":
            case "opentelemetry":
                s_tracer = new OpenTelemetryTracer();
                break;
            case "newrelic":
                s_tracer = new NewRelicTracer();
                break;
        }

        s_platform = platform;
        return s_tracer;
    }
}
