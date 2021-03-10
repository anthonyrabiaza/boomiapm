package com.boomi.connector.apm;

import java.io.InputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.boomi.connector.api.*;
import com.boomi.connector.util.BaseUpdateOperation;

import com.boomi.execution.ExecutionManager;
import com.boomi.execution.ExecutionUtil;
import com.boomi.proserv.apm.BoomiContext;
import com.boomi.proserv.apm.events.EventsPublisher;
import com.boomi.proserv.apm.events.EventsPublisherFactory;
import com.boomi.proserv.apm.tracer.Tracer;
import com.boomi.proserv.apm.tracer.TracerFactory;

/**
 * Execute the Operation, no profile need to be "imported" to the operation (as input are binaries)
 * @author Anthony Rabiaza 
 *
 */
public class BoomiAPMExecuteOperation extends BaseUpdateOperation {

	protected BoomiAPMExecuteOperation(BoomiAPMConnection conn) {
		super(conn);
	}

	@Override
	protected void executeUpdate(UpdateRequest request, OperationResponse response) {
		Logger logger 	= response.getLogger();
		boolean log 	= true;

		log(logger, log, "ARA: executeUpdate received");

		String platform 	= getContext().getConnectionProperties().getProperty("platform");
		String action 		= getContext().getOperationProperties().getProperty("action");
		boolean sendEvent 	= getContext().getOperationProperties().getBooleanProperty("sendEvent");
		String apiURL 		= getContext().getConnectionProperties().getProperty("apiURL");
		String apiKey 		= getContext().getConnectionProperties().getProperty("apiKey");
		String appKey 		= getContext().getConnectionProperties().getProperty("appKey");
		String rtProcess	= getContext().getOperationProperties().getProperty("realTimeProcessing");

		String executionID  = "N/A";
		String processName  = "N/A";
		String processID    = "N/A";
		String accountID    = "N/A";

		executionID = ExecutionManager.getCurrent().getTopLevelExecutionId();

		if(ExecutionManager.getCurrent().getParent() != null){
			processName = ExecutionManager.getCurrent().getParent().getProcessName();
			processID   = ExecutionManager.getCurrent().getParent().getProcessId();
		} else {
			processName = ExecutionManager.getCurrent().getProcessName();
			processID   = ExecutionManager.getCurrent().getProcessId();
		}

		accountID		= ExecutionManager.getCurrent().getAccountId();

		/*ExecutionUtil.setDynamicProcessProperty("DPP_executionID", executionID, false);
		ExecutionUtil.setDynamicProcessProperty("DPP_processName", processName, false);
		ExecutionUtil.setDynamicProcessProperty("DPP_processID", processID, false);
		ExecutionUtil.setDynamicProcessProperty("DPP_accountID", accountID, false);*/

		log(logger, log, "ARA: action is " + action + ", platform is " + platform);

		BoomiContext boomiContext 	= new BoomiContext(executionID, processName, processID, accountID);
		PayloadMetadata metadata 	= response.createMetadata();
		Tracer tracer 				= TracerFactory.getTracer(platform);

		if(tracer != null) {
			switch (action) {
				case "start":
					tracer.start(logger, boomiContext, metadata);
					break;
				case "stop":
					tracer.stop(logger, boomiContext, metadata);
					if (sendEvent) {
						EventsPublisher eventsPublisher = EventsPublisherFactory.getEventPublisher(platform);
						if (eventsPublisher != null) {
							eventsPublisher.sendEvents(logger, boomiContext, apiURL, apiKey, appKey, false);
						}
					}
					break;
				case "error":
					tracer.error(logger, boomiContext, metadata);
					if (sendEvent) {
						EventsPublisher eventsPublisher = EventsPublisherFactory.getEventPublisher(platform);
						if (eventsPublisher != null) {
							eventsPublisher.sendEvents(logger, boomiContext, apiURL, apiKey, appKey, true);
						}
					}
					break;
				default:
					break;
			}
		}

		for (ObjectData input : request) {
			try {
				log(logger, log, "ARA: Processing documents ...");

				String message 				= BoomiAPMConnector.inputStreamToString(input.getData());
				InputStream result  		= input.getData();
				Map<String, String> props 	= input.getUserDefinedProperties();//input.getDynamicProperties();

				if(message!=null) {
					try {
						if(tracer!=null) {
							switch (action) {
								case "start":
									tracer.start(logger, boomiContext, rtProcess, message, props, metadata);
									break;
								case "stop":
									tracer.stop(logger, boomiContext, rtProcess, message, props, metadata);
									break;
								case "error":
									tracer.error(logger, boomiContext, rtProcess, message, props, metadata);
									break;
								default:
									break;
							}
						}
						response.addResult(input, OperationStatus.SUCCESS, "200", "OK", PayloadUtil.toPayload(result, metadata));
					} catch (Exception e) {
						logger.severe(e.getMessage());
						e.printStackTrace();
						throw e;
					}
				}
				log(logger, log, "ARA: Document processed");

			} catch (Exception e) {
				logger.log(Level.SEVERE, "Details of Exception:", e);
				ResponseUtil.addExceptionFailure(response, input, e);
			}
		}
	}

	@Override
	public BoomiAPMConnection getConnection() {
		return (BoomiAPMConnection) super.getConnection();
	}

	private void log(Logger logger, boolean log, String message) {
		if(log) {
			logger.fine(message);
		}
	}
	
}