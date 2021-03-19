package com.boomi.proserv.apm.events;

import com.boomi.proserv.apm.BoomiContext;
import com.boomi.proserv.apm.Observer;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public abstract class EventsPublisher extends Observer {
    public static String EVENT_TITLE_ERROR      = "Boomi Process Execution - Error";
    public static String EVENT_TEXT_ERROR       = "Error during execution of ";
    public static String EVENT_ERROR            = "error";

    public static String EVENT_TITLE_SUCCESS    = "Boomi Process Execution - Success";
    public static String EVENT_TEXT_SUCCESS     = "Successful execution of ";
    public static String EVENT_SUCCESS          = "success";

    public abstract void sendEvents(Logger logger, BoomiContext boomiContext, String url, String apiKey, String appKey, String traceId, boolean error);
}
