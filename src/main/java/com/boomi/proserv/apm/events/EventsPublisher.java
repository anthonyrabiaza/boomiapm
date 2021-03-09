package com.boomi.proserv.apm.events;

import com.boomi.proserv.apm.BoomiContext;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public abstract class EventsPublisher {
    public static String EVENT_TITLE_ERROR      = "Boomi Process Execution - Error";
    public static String EVENT_TEXT_ERROR       = "Error during execution of ";
    public static String EVENT_ERROR            = "error";

    public static String EVENT_TITLE_SUCCESS    = "Boomi Process Execution - Success";
    public static String EVENT_TEXT_SUCCESS     = "Successful execution of ";
    public static String EVENT_SUCCESS          = "success";

    public abstract void sendEvents(Logger logger, BoomiContext boomiContext, String url, String apiKey, String appKey, boolean error);

    protected void postRequest(String url, String body, Map<String, String> headers) throws Exception {
        URL urlTarget = new URL(url);
        HttpURLConnection httpConnection = (HttpURLConnection) urlTarget.openConnection();
        httpConnection.setUseCaches(false);
        httpConnection.setDoOutput(true);
        httpConnection.setRequestMethod("POST");
        httpConnection.setFixedLengthStreamingMode(body.length());
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            httpConnection.setRequestProperty(entry.getKey(), entry.getValue());
        }
        OutputStreamWriter writer = new OutputStreamWriter(httpConnection.getOutputStream());
        writer.write(body);
        writer.flush();
        httpConnection.disconnect();
    }

    protected String getHostname() {
        try {
            String os       = System.getProperty("os.name").toLowerCase();
            String hostname = "localhost";

            if (os.contains("win")) {
                hostname = System.getenv("COMPUTERNAME");
            }else if (os.contains("nix") || os.contains("nux") || os.contains("mac os x")) {
                hostname = System.getenv("HOSTNAME");
            }
            if(hostname == null || "".equals(hostname) || "null".equals(hostname)){
                hostname = InetAddress.getLocalHost().getHostName();
            }
            return hostname;
        } catch (Exception e){
            return "unknown";
        }
    }

    protected String convertStackTraceToString(Throwable throwable) {
        try (StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw))
        {
            throwable.printStackTrace(pw);
            return sw.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
