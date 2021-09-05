package com.boomi.proserv.apm.tracer;

import com.appdynamics.agent.api.AppdynamicsAgent;
import com.appdynamics.agent.api.EntryTypes;
import com.appdynamics.agent.api.Transaction;
import com.appdynamics.agent.api.impl.NoOpTransaction;
import com.appdynamics.apm.appagent.api.DataScope;
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
            if(transaction == null || transaction instanceof NoOpTransaction) {
                logger.info("Starting new AppDynamics transaction");
                transaction = AppdynamicsAgent.startTransaction(context.getProcessNameAlphanum(), null, EntryTypes.POJO, false);
            } else {
                logger.info("Continuing AppDynamics transaction");
                AppdynamicsAgent.setCurrentTransactionName(context.getProcessNameAlphanum());
            }

            Set<DataScope> dataScopes = getAllScopes();
            transaction.collectData(BOOMI_EXECUTION_ID, context.getExecutionId(), dataScopes);
            transaction.collectData(BOOMI_PROCESS_NAME, context.getProcessName(), dataScopes);
            transaction.collectData(BOOMI_PROCESS_ID, context.getProcessId(), dataScopes);
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
                transaction.end();
            } else {
                logger.info("AppDynamics trace not found");
            }
            logger.info("AppDynamics trace ended");
        } catch (Exception e) {
            logger.severe("AppDynamics trace not added " + e);
        }
        super.stop(logger, context, rtProcess, document, dynProps, properties, metadata);
    }

    @Override
    public void error(Logger logger, BoomiContext context, String rtProcess, String document, Map<String, String> dynProps, Map<String, String> properties, PayloadMetadata metadata) {
        try {
            Transaction transaction = AppdynamicsAgent.getTransaction();
            if(transaction != null) {
                transaction.collectData(BOOMI_ERROR_MESSAGE, getErrorMessage(), getAllScopes());
            }
        } catch (Exception e) {
            logger.severe("Error adding tags:" + e.getMessage());
        }
        super.error(logger, context, rtProcess, document, dynProps, properties, metadata);
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
