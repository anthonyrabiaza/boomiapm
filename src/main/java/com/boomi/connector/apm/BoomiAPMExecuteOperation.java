package com.boomi.connector.apm;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.boomi.connector.api.*;
import com.boomi.connector.generic.TrackedBaseData;
import com.boomi.connector.util.BaseUpdateOperation;

import com.boomi.execution.ExecutionManager;
import com.boomi.proserv.apm.BoomiContext;
import com.boomi.proserv.apm.events.EventsPublisher;
import com.boomi.proserv.apm.events.EventsPublisherFactory;
import com.boomi.proserv.apm.tracer.Tracer;
import com.boomi.proserv.apm.tracer.TracerFactory;
import com.boomi.store.BaseData;
import com.boomi.store.MetaData;

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

		String platform 			= getContext().getConnectionProperties().getProperty("platform");
		String action 				= getContext().getOperationProperties().getProperty("action");

		boolean sendEvent 			= getContext().getOperationProperties().getBooleanProperty("sendEvent");
		String apiURL 				= getContext().getConnectionProperties().getProperty("eventsAPIURL");
		String apiKey 				= getContext().getConnectionProperties().getProperty("apiKey");
		String appKey 				= getContext().getConnectionProperties().getProperty("appKey");
		String serviceName			= getContext().getConnectionProperties().getProperty("serviceName");
		String rtProcess			= getContext().getOperationProperties().getProperty("realTimeProcessing");

		String executionID  		= "N/A";
		String processName  		= "N/A";
		String currentProcessName  	= "N/A";
		String processID    		= "N/A";
		String accountID    		= "N/A";

		executionID = ExecutionManager.getCurrent().getTopLevelExecutionId();

		if(ExecutionManager.getCurrent().getParent() != null){
			processName = ExecutionManager.getCurrent().getParent().getProcessName();
			processID   = ExecutionManager.getCurrent().getParent().getProcessId();
		} else {
			processName = ExecutionManager.getCurrent().getProcessName();
			processID   = ExecutionManager.getCurrent().getProcessId();
		}

		currentProcessName 	= ExecutionManager.getCurrent().getProcessName();
		accountID			= ExecutionManager.getCurrent().getAccountId();

		log(logger, log, "ARA: action is " + action + ", platform is " + platform);

		BoomiContext boomiContext 	= new BoomiContext(serviceName, executionID, processName, currentProcessName, processID, accountID);
		PayloadMetadata metadata 	= response.createMetadata();
		Tracer tracer				= null;
		try {
			tracer = TracerFactory.getTracer(platform);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "ARA: Error initializing the tracer:", e);
		}

		int payloadSize				= 1;
		if (request instanceof Collection) {
			payloadSize = ((Collection<?>) request).size();
		}
		int currentPayloadIndex		= 0;

		for (ObjectData input : request) {
			try {
				log(logger, log, "ARA: Processing documents ...");

				String message 					= BoomiAPMConnector.inputStreamToString(input.getData());
				InputStream result  			= BoomiAPMConnector.stringToInputStream(message);
				Map<String, String> dynProps	= input.getDynamicProperties();
				Map<String, String> props 		= input.getUserDefinedProperties();

				if(message!=null) {
					try {
						//Only run the Tracer on the first document
						if(currentPayloadIndex == 0 && tracer!=null) {
							switch (action) {
								case "start":
									tracer.start(logger, boomiContext, rtProcess, message, dynProps, props, metadata);
									break;
								case "save":
									tracer.save(logger, boomiContext, rtProcess, message, dynProps, props, metadata);
									break;
								case "stop":
									tracer.stop(logger, boomiContext, rtProcess, message, dynProps, props, metadata);
									if (sendEvent) {
										EventsPublisher eventsPublisher = EventsPublisherFactory.getEventPublisher(platform);
										if (eventsPublisher != null) {
											eventsPublisher.sendEvents(logger, boomiContext, apiURL, apiKey, appKey, tracer.getTraceId(), false);
										}
									}
									break;
								case "error":
									String errorMessage = getErrorMessage(logger, input);
									tracer.setErrorMessage(errorMessage);
									tracer.error(logger, boomiContext, rtProcess, message, dynProps, props, metadata);
									metadata.setTrackedProperty("errorMessage", errorMessage);
									if (sendEvent) {
										EventsPublisher eventsPublisher = EventsPublisherFactory.getEventPublisher(platform);
										if (eventsPublisher != null) {
											eventsPublisher.sendEvents(logger, boomiContext, apiURL, apiKey, appKey, tracer.getTraceId(),true);
										}
									}
									break;
								default:
									break;
							}
						}
						response.addResult(input, OperationStatus.SUCCESS, "200", "OK", PayloadUtil.toPayload(result, metadata));
					} catch (Exception e) {
						logger.severe(e.getMessage());
						e.printStackTrace();
						response.addResult(input, OperationStatus.SUCCESS, "200", "OK", PayloadUtil.toPayload(result, metadata));
						//throw e;
					}
				}
				log(logger, log, "ARA: Document processed");

			} catch (Exception e) {
				logger.log(Level.SEVERE, "ARA: Details of Exception:", e);
				ResponseUtil.addExceptionFailure(response, input, e);
			}
		}
		currentPayloadIndex++;
	}

	private String getErrorMessage(Logger logger, ObjectData input) {
		String errorMessage = "Error";
		try {
			BaseData baseData = ((TrackedBaseData) input).getBaseData();
			Collection<MetaData> internalMetadata = baseData.getDataRef().getMetaData(baseData);
			for (MetaData metadata:internalMetadata) {
				String tempErrorMessage = metadata.getRawField("catcherrorsmessage_s");
				if (tempErrorMessage!=null && !tempErrorMessage.equals("")) {
					errorMessage = tempErrorMessage;
				}
			}
		} catch (Exception e) {
			logger.warning("ARA: No able to get the error message: " + e.getMessage());
		}
		return errorMessage;
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