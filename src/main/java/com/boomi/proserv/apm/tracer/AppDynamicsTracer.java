package com.boomi.proserv.apm.tracer;

import com.appdynamics.agent.api.AppdynamicsAgent;
import com.appdynamics.agent.api.EntryTypes;
import com.appdynamics.agent.api.Transaction;
import com.appdynamics.agent.api.impl.NoOpTransaction;
import com.appdynamics.apm.appagent.api.DataScope;
import com.boomi.connector.api.PayloadMetadata;
import com.boomi.proserv.apm.BoomiContext;
import com.boomi.proserv.apm.ComponentType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class AppDynamicsTracer extends Tracer {

    public static final String SINGULARITYHEADER = "singularityheader";

    @Override
    public void start(Logger logger, BoomiContext context, String rtProcess, String document, Map<String, String> dynProps, Map<String, String> properties, PayloadMetadata metadata) {
        try {
            logger.info("Adding AppDynamics trace ...");
            Transaction transaction = getTransaction();
            if(transaction == null || transaction instanceof NoOpTransaction) {
                String singularityheader = getSingularityheader(properties);
                if(singularityheader == null || singularityheader.equals("")) {
                    singularityheader = null;
                    logger.info("Starting new AppDynamics transaction");
                } else {
                    logger.info("Header found, try to continue AppDynamics transaction");
                }
                transaction = AppdynamicsAgent.startTransaction(context.getProcessNameAlphanum(), singularityheader, EntryTypes.POJO, false);
            } else {
                logger.info("Continuing AppDynamics transaction");
                AppdynamicsAgent.setCurrentTransactionName(context.getProcessNameAlphanum());
            }

            Set<DataScope> dataScopes = getAllScopes();
            transaction.collectData(getBoomiExecutionIdKey(), context.getExecutionId(), dataScopes);
            transaction.collectData(getBoomiProcessNameKey(), context.getProcessName(), dataScopes);
            transaction.collectData(getBoomiProcessIdKey(), context.getProcessId(), dataScopes);
            logger.info("AppDynamics trace added");
        } catch (Exception e) {
            logger.severe("AppDynamics trace not added " + e);
        }
        super.start(logger, context, rtProcess, document, dynProps, properties, metadata);
    }

    @Override
    public void stop(Logger logger, BoomiContext context, String rtProcess, String document, Map<String, String> dynProps, Map<String, String> properties, PayloadMetadata metadata) {
        super.stop(logger, context, rtProcess, document, dynProps, properties, metadata);
        try {
            logger.info("Ending AppDynamics trace ...");
            Transaction transaction = getTransaction();
            if(transaction != null) {
                transaction.end();
            } else {
                logger.info("AppDynamics trace not found");
            }
            logger.info("AppDynamics trace ended");
        } catch (Exception e) {
            logger.severe("AppDynamics trace not added " + e);
        }
    }

    @Override
    public void error(Logger logger, BoomiContext context, String rtProcess, String document, Map<String, String> dynProps, Map<String, String> properties, PayloadMetadata metadata) {
        super.error(logger, context, rtProcess, document, dynProps, properties, metadata);
        try {
            logger.info("Ending AppDynamics trace with error...");
            Transaction transaction = getTransaction();
            if(transaction != null) {
                transaction.collectData(getBoomiErrorMessageKey(), getErrorMessage(), getAllScopes());
                transaction.end();
            }
            logger.info("AppDynamics trace ended");
        } catch (Exception e) {
            logger.severe("Error adding tags:" + e.getMessage());
        }
    }

    private Transaction getTransaction() {
        return AppdynamicsAgent.getTransaction();
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

    protected String getSingularityheader(Map<String, String> properties){
        String singularityheader = properties.get(HTTP_DOC_PREFIX + SINGULARITYHEADER);//HTTP
        setComponentType(ComponentType.HTTP);
        if (singularityheader == null || singularityheader.equals("")) {
            singularityheader = properties.get(SINGULARITYHEADER);//JMS
            setComponentType(ComponentType.JMS);
        }
        return singularityheader;
    }
}
