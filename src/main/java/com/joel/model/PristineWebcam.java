package com.joel.model;

import com.github.sarxos.webcam.*;
import com.joel.misc.Utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by Tyrion on 10/17/2017.
 */
public class PristineWebcam implements WebcamMotionListener {

    private static PristineWebcam instance;

    private PristineHost host;
    private boolean enabled = true;
    private boolean detectMotion = true;
    private boolean recordStarted = false;
    private int sendFrameEvery = 200;
    private int motionRecordTimeout = 5000; // Time until a notice is sent to server that there is no more motion
    private long timeSinceLastMotion = 0;
    private boolean streamingToServer = true;
    private int timePassedSinceLastFrameSent = 0;

    private Logger logger;

    private Webcam webcam;
    private WebcamMotionDetector detector;

    public static PristineWebcam getInstance(PristineHost host,Logger logger){
        if (instance == null) {
            instance = new PristineWebcam(host,logger);
        }
        return instance;
    }

    private PristineWebcam(PristineHost host,Logger logger){
        this.logger = logger;
        this.host = host;
        try {
            webcam = Webcam.getDefault();
            if(webcam != null) {
                detector = new WebcamMotionDetector(webcam);
                detector.setInterval(sendFrameEvery);
                detector.addMotionListener(this);
            }
            else {
                enabled = false;
            }
        } catch (WebcamLockException ex) {
            enabled = false;
            throw ex;
        }
    }

    public BufferedImage getImage(){
        return webcam.getImage();
    }

    public void startMotionListener(){
        if(enabled) detector.start();
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

    public PristineRequest loop(PristineHost me, PristineRequest request,int delta){
        if(streamingToServer && timePassedSinceLastFrameSent > sendFrameEvery) {
            timePassedSinceLastFrameSent = 0;
            BufferedImage camShot = webcam.getImage();
            request.setFilename("png");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                ImageIO.write(camShot, "png", outputStream);
            } catch (IOException e) {
                Utils.log(e.getMessage(), logger);
            }
            request.setFile64(Utils.encode(outputStream.toByteArray()));
            request.setHost(me);
        }
        timePassedSinceLastFrameSent += delta;
        timeSinceLastMotion += delta;
        if(timeSinceLastMotion > motionRecordTimeout && recordStarted) {
            recordStarted = false;
            setClientRecording(false);
            sendFrameEvery = 200;
            me.setRecording(false);
            request.setHost(me);
        }
        return request;
    }

    public void open(){
        if(enabled) webcam.open();
    }

    public boolean close(){
        return webcam.close();
    }

    public void setViewSize(Dimension dimension) {
        if(enabled) webcam.setViewSize(dimension);
    }

    public boolean isDetectMotion() {
        return detectMotion;
    }

    public void setClientRecording(boolean status) {
        host.setRecording(status);
    }

    public void setStreamingToServer(boolean streamingToServer) {
        this.streamingToServer = streamingToServer;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
