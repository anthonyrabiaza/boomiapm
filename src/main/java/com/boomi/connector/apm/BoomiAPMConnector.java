package com.boomi.connector.apm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import com.boomi.connector.api.BrowseContext;
import com.boomi.connector.api.Browser;
import com.boomi.connector.api.Operation;
import com.boomi.connector.api.OperationContext;
import com.boomi.connector.util.BaseConnector;

/**
 * BoomiDataProcessorConnector class with utilities
 * @author Anthony Rabiaza
 *
 */
public class BoomiAPMConnector extends BaseConnector {

    @Override
    public Browser createBrowser(BrowseContext context) {
        return new BoomiAPMBrowser(createConnection(context));
    }    

    @Override
    protected Operation createExecuteOperation(OperationContext context) {
        return new BoomiAPMExecuteOperation(createConnection(context));
    }

    @Override
    protected Operation createUpdateOperation(OperationContext context) {
        return new BoomiAPMUpdateOperation(createConnection(context));
    }

    private BoomiAPMConnection createConnection(BrowseContext context) {
        return new BoomiAPMConnection(context);
    }
    
	/**
	 * Utility to convert InputStream to String
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static String inputStreamToString(InputStream is) throws IOException {
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(is))) {
			return buffer.lines().collect(Collectors.joining("\n"));
		}
	}
}