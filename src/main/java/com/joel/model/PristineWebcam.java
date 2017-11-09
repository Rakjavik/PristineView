package com.joel.model;

import com.github.sarxos.webcam.*;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by Tyrion on 10/17/2017.
 */
public class PristineWebcam implements WebcamMotionListener {

    private static PristineWebcam instance;

    private PristineHost host;
    private boolean detectMotion = true;
    private boolean recordStarted = false;
    private int sendFrameEvery = 200;
    private int motionRecordTimeout = 5000; // Time until a notice is sent to server that there is no more motion
    private long timeSinceLastMotion = 0;
    private boolean streamingToServer = true;

    private Webcam webcam;
    private WebcamMotionDetector detector;

    public static PristineWebcam getInstance(PristineHost host){
        if (instance == null) {
            instance = new PristineWebcam(host);
        }
        return instance;
    }

    private PristineWebcam(PristineHost host){
        this.host = host;
        try {
            webcam = Webcam.getDefault();
            detector = new WebcamMotionDetector(webcam);
            detector.setInterval(sendFrameEvery);
            detector.addMotionListener(this);
        } catch (WebcamLockException ex) {
            throw ex;
        }
    }

    public void incrementTimeSinceLastMotion(long delta) {
        timeSinceLastMotion += delta;
    }

    public BufferedImage getImage(){
        return webcam.getImage();
    }

    public void startMotionListener(){
        detector.start();
    }

    @Override
    public void motionDetected(WebcamMotionEvent webcamMotionEvent) {
        if(detectMotion) {
            timeSinceLastMotion = 0;
            host.setRecording(true);
            if(!recordStarted) {
                recordStarted = true;
                sendFrameEvery = 50;
            }
        }
    }

    public void open(){
        webcam.open();
    }

    public boolean close(){
        return webcam.close();
    }

    public void setViewSize(Dimension dimension) {
        webcam.setViewSize(dimension);
    }

    public long getTimeSinceLastMotion() {
        return timeSinceLastMotion;
    }

    public int getMotionRecordTimeout() {
        return motionRecordTimeout;
    }

    public int getSendFrameEvery() {
        return sendFrameEvery;
    }

    public void setRecordStarted(boolean recordStarted) {
        this.recordStarted = recordStarted;
    }

    public boolean isDetectMotion() {
        return detectMotion;
    }

    public boolean isRecordStarted(){
        return recordStarted;
    }

    public void setClientRecording(boolean status) {
        host.setRecording(status);
    }

    public void setSendFrameEvery(int sendFrameEvery) {
        this.sendFrameEvery = sendFrameEvery;
    }

    public void setStreamingToServer(boolean streamingToServer) {
        this.streamingToServer = streamingToServer;
    }

    public boolean isStreamingToServer() {
        return streamingToServer;
    }
}
