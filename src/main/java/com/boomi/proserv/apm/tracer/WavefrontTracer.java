package com.boomi.proserv.apm.tracer;

import com.boomi.connector.api.PayloadMetadata;
import com.boomi.proserv.apm.BoomiContext;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapAdapter;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class WavefrontTracer extends OpenTracingTracer {

    public static final String WF_TRACEID   = "wf-ot-traceid";
    public static final String WF_SPANID    = "wf-ot-spanid";
    public static final String[] CANDIDATES = {"parent-id", "parentid", "inheader_parent-id", "inheader_parentid", "span-id", "spanid", "inheader_span-id", "inheader_spanid", B3_SPANID, HTTP_DOC_PREFIX + B3_SPANID};

    @Override
    public void start(Logger logger, BoomiContext context, String rtProcess, String document, Map<String, String> dynProps, Map<String, String> properties, PayloadMetadata metadata) {
        super.start(logger, context, rtProcess, document, dynProps, properties, metadata);
    }

    @Override
    protected String getTraceparent(Map<String, String> properties){
        String traceparent = super.getTraceparent(properties);
        if(traceparent==null || "".equals(traceparent)) {
            traceparent = getB3TraceId(properties);
        }

        return traceparent;
    }

    @Override
    protected String getTraceparentKey() {
        return WF_TRACEID;
    }

    @Override
    protected void enrich(Map<String, String> map, Map<String, String> properties) {
        super.enrich(map, properties);

        //Correct traceId if required
        String traceId = map.get(getTraceparentKey());
        if(traceId != null && !traceId.contains("-")) {
            traceId = convertStringToUUID(traceId);
            map.put(getTraceparentKey(), traceId);
        }
        //Inject spanId
        String[] candidates = CANDIDATES;
        for (int i = 0; i < candidates.length; i++) {
            String parentId = properties.get(candidates[i]);
            if(parentId != null) {
                if(!parentId.contains("-")) {
                    parentId = convertStringToUUID(parentId);
                }
                map.put(WF_SPANID, parentId);
                break;
            }
        }
    }
}
