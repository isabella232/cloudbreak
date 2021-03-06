package com.sequenceiq.cloudbreak.orchestrator.model;

public class BootstrapParams {
    private String cloud;

    private String os;

    private boolean saltBootstrapFpSupported;

    private boolean restartNeeded;

    public String getCloud() {
        return cloud;
    }

    public void setCloud(String cloud) {
        this.cloud = cloud;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public boolean isSaltBootstrapFpSupported() {
        return saltBootstrapFpSupported;
    }

    public void setSaltBootstrapFpSupported(boolean saltBootstrapFpSupported) {
        this.saltBootstrapFpSupported = saltBootstrapFpSupported;
    }

    public boolean isRestartNeeded() {
        return restartNeeded;
    }

    public void setRestartNeeded(boolean restartNeeded) {
        this.restartNeeded = restartNeeded;
    }

    @Override
    public String toString() {
        return "BootstrapParams{" +
                "cloud='" + cloud + '\'' +
                ", os='" + os + '\'' +
                ", saltBootstrapFpSupported=" + saltBootstrapFpSupported +
                '}';
    }
}
