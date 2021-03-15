package com.boomi.proserv.apm.tracer;

import com.appdynamics.apm.appagent.api.AgentDelegate;
import com.appdynamics.apm.appagent.api.DataScope;
import com.appdynamics.apm.appagent.api.IMetricAndEventReporter;
import com.boomi.connector.api.PayloadMetadata;
import com.boomi.proserv.apm.BoomiContext;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class AppDynamicsTracer extends Tracer {

    public void start(Logger logger, BoomiContext context, PayloadMetadata metadata) {
        try {
            logger.info("Adding AppDynamics trace ...");
            IMetricAndEventReporter dataReporter    = getDataReporter();
            Set<DataScope> dataScopes               = getAllScopes();

            dataReporter.addSnapshotData("boomi.executionID", context.getExecutionId(), dataScopes);
            dataReporter.addSnapshotData("boomi.processName", context.getProcessName(), dataScopes);
            dataReporter.addSnapshotData("boomi.processID", context.getProcessId(), dataScopes);
            logger.info("AppDynamics trace added");
        } catch (Exception e) {
            logger.severe("AppDynamics trace not added " + e);
        }
    }

    protected IMetricAndEventReporter getDataReporter() {
        IMetricAndEventReporter reporter = AgentDelegate.getMetricAndEventPublisher();

        return reporter;
    }

    protected Set<DataScope> getAllScopes() {
        Set<DataScope> allScopes = new HashSet<DataScope>();
        allScopes.add(DataScope.ANALYTICS);
        allScopes.add(DataScope.SNAPSHOTS);

        return allScopes;
    }

    @Override
    protected void addTags(Map<String, String> dynProps) {
        Map<String, String> tags = getTags(dynProps);
        if(tags.size()>0) {
            IMetricAndEventReporter dataReporter    = getDataReporter();
            Set<DataScope> dataScopes               = getAllScopes();
            for (Map.Entry<String, String> entry : tags.entrySet()) {
                dataReporter.addSnapshotData(entry.getKey(), entry.getValue(), dataScopes);
            }
        }
    }
}
