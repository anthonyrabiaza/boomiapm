package com.boomi.proserv.apm.events;

public class EventsPublisherFactory {
    static EventsPublisher s_eventsPublisher    = null;
    static String s_eventPlatform               = null;

    static public EventsPublisher getEventPublisher(String eventPlatform) {
        if(s_eventsPublisher != null && eventPlatform.equals(s_eventPlatform)) {
            return s_eventsPublisher;
        }

        switch(eventPlatform) {
            case "datadog":
                s_eventsPublisher = new DatadogEventsPublisher();
                break;
            default:
                s_eventsPublisher = null;
                break;
        }

        s_eventPlatform = eventPlatform;
        return s_eventsPublisher;
    }
}
