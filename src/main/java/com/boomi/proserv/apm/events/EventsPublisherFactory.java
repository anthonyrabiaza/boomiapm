package com.boomi.proserv.apm.events;

public class EventsPublisherFactory {
    static EventsPublisher s_eventsPublisher    = null;
    static String s_platform                    = null;

    static public EventsPublisher getEventPublisher(String platform) {
        if(s_eventsPublisher != null && platform.equals(s_platform)) {
            return s_eventsPublisher;
        }

        switch(platform) {
            case "datadog":
                s_eventsPublisher = new DatadogEventsPublisher();
                break;
            case "newrelic_payload":
            case "newrelic_parentId":
                s_eventsPublisher = null;
                break;
        }

        s_platform = platform;
        return s_eventsPublisher;
    }
}
