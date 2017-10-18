package com.joel.model;

import java.awt.*;

/**
 * Created by rakjavik on 9/23/2017.
 */
public class PristineHost {
    private String hostname;
    private String ip;
    private transient Image currentShot;
    private String cpuUsage;
    private Long freeSpace;
    private Long freeMemory;
    private boolean recording = false;




    public PristineHost() {}

    public PristineHost(String hostname, String ip) {
        this.ip = ip;
        this.hostname = hostname;
    }

    public void setCpuUsage(String cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getHostname() {
        return hostname;
    }

    public void setCurrentShot(Image currentShot) {
        this.currentShot = currentShot;
    }

    public Image getCurrentShot() {
        return currentShot;
    }

    public void setFreeMemory(Long freeMemory) {
        this.freeMemory = freeMemory;
    }

    public String getCpuUsage() {
        return cpuUsage;
    }

    public Long getFreeSpace() {
        return freeSpace;
    }

    public Long getFreeMemory() {
        return freeMemory;
    }

    public boolean isRecording() {
        return recording;
    }

    public void setRecording(boolean recording) {
        this.recording = recording;
    }

}
