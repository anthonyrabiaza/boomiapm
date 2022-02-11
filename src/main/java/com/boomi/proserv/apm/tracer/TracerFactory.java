package com.boomi.proserv.apm.tracer;

public class TracerFactory {

    static public Tracer getTracer(String platform) {
        Tracer tracer = null;

        switch(platform) {
            case "appdynamics":
                tracer = new AppDynamicsTracer();
                break;
            case "awsxray_opentelemetry":
                tracer = new AWSXRayOpenTelemetryTracer();
                break;
            case "datadog":
                tracer = new DatadogTracer();
                break;
            case "dynatrace":
                tracer = new DynatraceTracer();
                break;
            case "opentracing":
                tracer = new OpenTracingTracer();
                break;
            case "elasticapm":
            case "opentracing_resolver":
                tracer = new OpenTracingResolver();
                break;
            case "lightstep":
            case "opentelemetry":
                tracer = new OpenTelemetryTracer();
                break;
            case "newrelic":
                tracer = new NewRelicTracer();
                break;
            case "wavefront":
                tracer = new WavefrontTracer();
                break;
        }

        if(tracer != null) {
            tracer.setPlatform(platform);
        }
        return tracer;
    }
}
