package com.boomi.connector.apm;

import com.boomi.connector.api.*;
import com.boomi.connector.util.BaseUpdateOperation;

import com.boomi.proserv.apm.Observer;
import com.boomi.proserv.apm.metrics.MetricsPublisher;
import com.boomi.proserv.apm.metrics.MetricsPublisherFactory;
import org.w3c.dom.Document;

import java.util.logging.Level;
import java.util.logging.Logger;

public class BoomiAPMUpdateOperation extends BaseUpdateOperation {

    protected BoomiAPMUpdateOperation(BoomiAPMConnection conn) {
        super(conn);
    }

    @Override
    protected void executeUpdate(UpdateRequest request, OperationResponse response) {
        Logger logger 	= response.getLogger();
        boolean log 	= true;

        String platform = getContext().getConnectionProperties().getProperty("platform");
        String apiURL 	= getContext().getConnectionProperties().getProperty("metricsAPIURL");
        String apiKey 	= getContext().getConnectionProperties().getProperty("apiKey");
        String appKey 	= getContext().getConnectionProperties().getProperty("appKey");

        for (ObjectData input : request) {
            try {
                log(logger, log, "ARA: Processing documents ...");

                Document doc    = Observer.parse(input.getData());
                String metric   = Observer.getFirstNodeTextContent(doc, "//publish_metrics/metric");
                String type     = Observer.getFirstNodeTextContent(doc, "//publish_metrics/type");
                String value    = Observer.getFirstNodeTextContent(doc, "//publish_metrics/value");

                MetricsPublisher publisher = MetricsPublisherFactory.getMetricsPublisher(platform);
                if(publisher != null) {
                    publisher.sendMetrics(logger, null, apiURL, apiKey, appKey, metric, type, value);
                } else {
                    logger.warning("No Metrics Publisher defined");
                }

                response.addEmptyResult(input, OperationStatus.SUCCESS, "200", "OK");
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Details of Exception:", e);
                ResponseUtil.addExceptionFailure(response, input, e);
            }
        }
    }

    private void log(Logger logger, boolean log, String message) {
        if(log) {
            logger.fine(message);
        }
    }
}
