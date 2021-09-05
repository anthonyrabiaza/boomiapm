package com.boomi.proserv.apm;

public class BoomiContext {
    public String serviceName;
    public String executionId;
    public String processName;
    private String processNameClean;
    public String processId;
    public String accountId;

    public BoomiContext(String serviceName, String executionId, String processName, String processId, String accountId) {
        this.serviceName    = serviceName;
        this.executionId    = executionId;
        this.processName    = processName;
        this.processId      = processId;
        this.accountId      = accountId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
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

    public String getProcessNameAlphanum() {
        if(processNameClean == null) {
            processNameClean = processName.replaceAll("[^a-zA-Z0-9]", " ");
            processNameClean = processNameClean.trim().replaceAll("\\s+", " ");
        }
        return processNameClean;
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
