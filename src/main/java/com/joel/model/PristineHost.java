package com.joel.model;

import java.awt.*;

/**
 * Created by rakjavik on 9/23/2017.
 */
public class PristineHost {
    private String hostname;
    private transient Image currentShot;
    private boolean recording = false;
    private long timeOfLastSentRequest = System.currentTimeMillis();
    private boolean connected;
    private String ip;


    public PristineHost(String hostname, String ip) {
        this.hostname = hostname;
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

    public boolean isRecording() {
        return recording;
    }

    public void setRecording(boolean recording) {
        this.recording = recording;
    }

    public void setTimeOfLastSentRequest(long timeOfLastSentRequest) {
        this.timeOfLastSentRequest = timeOfLastSentRequest;
    }

    public long getTimeOfLastSentRequest() {
        return timeOfLastSentRequest;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }
}
