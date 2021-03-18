package com.boomi.proserv.apm.tracer;

import com.appdynamics.agent.api.AppdynamicsAgent;
import com.appdynamics.agent.api.EntryTypes;
import com.appdynamics.agent.api.Transaction;
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

    @Override
    public void start(Logger logger, BoomiContext context, String rtProcess, String document, Map<String, String> dynProps, Map<String, String> properties, PayloadMetadata metadata) {
        try {
            logger.info("Adding AppDynamics trace ...");
            Transaction transaction = AppdynamicsAgent.getTransaction();
            if(transaction == null) {
                logger.info("Starting new AppDynamics transaction");
                transaction = AppdynamicsAgent.startTransaction(context.getProcessName(), null, EntryTypes.POJO, true);
            } else {
                logger.info("Continuing AppDynamics transaction");
                AppdynamicsAgent.setCurrentTransactionName(context.getProcessName());
            }

            Set<DataScope> dataScopes = getAllScopes();
            transaction.collectData("boomi.executionID", context.getExecutionId(), dataScopes);
            transaction.collectData("boomi.processName", context.getProcessName(), dataScopes);
            transaction.collectData("boomi.processID", context.getProcessId(), dataScopes);
            logger.info("AppDynamics trace added");
        } catch (Exception e) {
            logger.severe("AppDynamics trace not added " + e);
        }
        super.start(logger, context, rtProcess, document, dynProps, properties, metadata);
    }

    @Override
    public void stop(Logger logger, BoomiContext context, String rtProcess, String document, Map<String, String> dynProps, Map<String, String> properties, PayloadMetadata metadata) {
        try {
            logger.info("Ending AppDynamics trace ...");
            Transaction transaction = AppdynamicsAgent.getTransaction();
            if(transaction != null) {
                transaction.endSegment();
            } else {
                logger.info("AppDynamics trace not found");
            }
            logger.info("AppDynamics trace ended");
        } catch (Exception e) {
            logger.severe("AppDynamics trace not added " + e);
        }
        super.stop(logger, context, rtProcess, document, dynProps, properties, metadata);
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
            Transaction transaction = AppdynamicsAgent.getTransaction();
            if(transaction != null) {
                Set<DataScope> dataScopes = getAllScopes();
                for (Map.Entry<String, String> entry : tags.entrySet()) {
                    transaction.collectData(entry.getKey(), entry.getValue(), dataScopes);
                }
            }
        }
    }
}
