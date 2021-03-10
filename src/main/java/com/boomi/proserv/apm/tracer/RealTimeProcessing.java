package com.boomi.proserv.apm.tracer;

public enum RealTimeProcessing {
    payload,
    parentid,
    ignore;

    static RealTimeProcessing getValue(String rtProcess) {
        RealTimeProcessing realTimeProcessing;
        try {
            realTimeProcessing = RealTimeProcessing.valueOf(rtProcess);
        } catch (Exception e) {
            realTimeProcessing = ignore;
        }
        return realTimeProcessing;
    }
}
