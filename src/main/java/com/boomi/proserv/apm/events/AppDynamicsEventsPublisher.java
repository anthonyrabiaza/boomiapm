package com.boomi.proserv.apm.events;

import com.appdynamics.agent.api.AppdynamicsAgent;
import com.boomi.proserv.apm.BoomiContext;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class AppDynamicsEventsPublisher extends EventsPublisher {
    @Override
    public void sendEvents(Logger logger, BoomiContext boomiContext, String url, String apiKey, String appKey, String traceId, boolean error) {
        Map<String, String> details = new HashMap<String, String>();
        details.put("processName", boomiContext.getProcessName());
        details.put("executionId", boomiContext.getExecutionId());
        details.put("traceId", traceId);
        if(error) {
            AppdynamicsAgent.getEventPublisher().publishErrorEvent(EVENT_TITLE_SUCCESS, details, true);
        } else {
            AppdynamicsAgent.getEventPublisher().publishInfoEvent(EVENT_TITLE_SUCCESS, details);
        }
    }
}
