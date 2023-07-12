package com.boomi.proserv.apm.tracer;

import com.boomi.connector.api.PayloadMetadata;
import com.boomi.proserv.apm.BoomiContext;
import com.manageengine.apminsight.agent.JavaAgent;
import com.manageengine.apminsight.agent.api.CustomTracker;
import com.manageengine.apminsight.agent.sequence.SequenceTrace;
import com.manageengine.apminsight.agent.tracing.TrackerService;
import io.opentracing.Scope;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ManageEngineAppManagerTracer extends Tracer {

    public static final String TRACEID      = "X-Appmanager-Id"; //X-Site24x7-Id
    public static final String TRACEDATA    = "X-Appmanager-DT-Data"; //X-Site24x7-DT-Data

    protected static ThreadLocal<CustomTracker> customTracker = new ThreadLocal<CustomTracker>();
    @Override
    public void start(Logger logger, BoomiContext context, String rtProcess, String document, Map<String, String> dynProps, Map<String, String> properties, PayloadMetadata metadata) {
        if(customTracker.get()==null) {
            customTracker.set(new CustomTracker(context.getCurrentProcessNameAlphanum()));
        }
        TrackerService tracer       = JavaAgent.getInstance().getAgentService().getTrackerService();
        List<SequenceTrace> traces  = JavaAgent.getInstance().getAgentService().getSequenceService().getSequenceTraceStore().getSequenceTracesToSend();//Current traces, is this one is embedded, the parent will be present (lookup using headers), if it's the case, we can add parameter with Thread Id, Thread Name
        CustomTracker.setTransactionName(context.getProcessName());
        CustomTracker.addParameter(getBoomiExecutionIdKey(), context.getExecutionId());
        CustomTracker.addParameter(getBoomiProcessNameKey(), context.getProcessName());
        CustomTracker.addParameter(getBoomiProcessIdKey(), context.getProcessId());
        super.start(logger, context, rtProcess, document, dynProps, properties, metadata);
    }

    public void stop(Logger logger, BoomiContext context, String rtProcess, String document, Map<String, String> dynProps, Map<String, String> properties, PayloadMetadata metadata) {
        if(customTracker.get()!=null) {
            customTracker.get().stop();
            customTracker.remove();
        }
        super.stop(logger, context, rtProcess, document, dynProps, properties, metadata);
    }

    public void error(Logger logger, BoomiContext context, String rtProcess, String document, Map<String, String> dynProps, Map<String, String> properties, PayloadMetadata metadata) {
        Throwable throwable = new Throwable(getErrorMessage());
        CustomTracker.trackException(getErrorMessage(), throwable);
        super.error(logger, context, rtProcess, document, dynProps, properties, metadata);
        if(customTracker.get()!=null) {
            customTracker.get().stop(throwable);
            customTracker.remove();
        }
    }

    @Override
    protected void addTags(Map<String, String> dynProps) {
        Map<String, String> tags = getTags(dynProps);
        if(tags.size()>0) {
            for (Map.Entry<String, String> entry : tags.entrySet()) {
                CustomTracker.addParameter(entry.getKey(), entry.getValue());
            }
        }
    }
}
