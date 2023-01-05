package com.boomi.proserv.apm;

public class BoomiContext {
    public String serviceName;
    public String executionId;
    public String processName;
    private String processNameClean;
    public String currentProcessName;
    private String currentProcessNameClean;
    public String processId;
    public String accountId;

    public BoomiContext(String serviceName, String executionId, String processName, String currentProcessName, String processId, String accountId) {
        this.serviceName            = serviceName;
        this.executionId            = executionId;
        this.processName            = processName;
        this.currentProcessName     = currentProcessName;
        this.processId              = processId;
        this.accountId              = accountId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceVersion() {
        return "1.0";
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getProcessName() {
        return processName;
    }

    public String getCurrentProcessName() {
        return currentProcessName;
    }

    protected String cleanString(String str) {
        str = str.replaceAll("[^a-zA-Z0-9]", " ");
        str = str.trim().replaceAll("\\s+", " ");
        return str;
    }
    public String getProcessNameAlphanum() {
        if(processNameClean == null) {
            processNameClean = cleanString(processName);
        }
        return processNameClean;
    }

    public String getCurrentProcessNameAlphanum() {
        if(currentProcessNameClean == null) {
            currentProcessNameClean = cleanString(currentProcessName);
        }
        return currentProcessNameClean;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
}
