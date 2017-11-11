package com.joel.threads;

import com.google.gson.Gson;
import com.joel.PristineClient;
import com.joel.TTS;
import com.joel.misc.Utils;
import com.joel.model.PristineRequest;

import javax.sound.sampled.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

/**
 * Created by rakjavik on 9/25/2017.
 */
public class PollQueueThread extends Thread {

    private int delay = 2000;
    private boolean running;
    private Logger logger;
    private SendAudioThread sendAudioThread;
    private ReadAudioThread readAudioThread;

    private String hostname;
    public PollQueueThread(String hostname,Logger logger){
        this.hostname = hostname;
        this.logger = logger;
        running = true;
    }

    @Override
    public void run() {
        while(running) {
            try {
                String response = Utils.getHTTP("http://" + PristineClient.serverIP + ":8080/pristine?requestType=getfromqueue&hostname=" + hostname);
                PristineRequest request = new Gson().fromJson(response,PristineRequest.class);
                processRequest(request);
            } catch (Exception e) {
                logger.info("Problem requesting queue from http server");
            }
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void processRequest(PristineRequest request) throws Exception {
        if(request == null || request.getRequestType() == null) {
            return;
        }
        Utils.sendNotify(hostname,"received",logger);
        if(request.getRequestType().equals(PristineRequest.RT_RESUME_NETFLIX)) {
            logger.info("Request to resume Netflix");
            try {
                Robot robot = new Robot();
                BufferedImage screen = robot.createScreenCapture(new Rectangle(PristineClient.resolution.width, PristineClient.resolution.height));
                for(int x = 0; x < screen.getWidth();x++) {
                    for(int y = 0; y < screen.getHeight(); y++) {
                        int rgb = screen.getRGB(x,y);
                        Color color = new Color(rgb);
                        if(color.equals(new Color(107,107,107))) {
                            rgb = screen.getRGB(x,y+25);
                            color = new Color(rgb);
                            if(color.equals(new Color(255,255,255))) {
                                rgb = screen.getRGB(x+32,y+25);
                                color = new Color(rgb);
                                if(color.equals(new Color(255,255,255))) {
                                    robot.mouseMove(x+14,y+10);
                                    robot.mousePress(InputEvent.BUTTON1_MASK);
                                    robot.delay(20);
                                    robot.mouseRelease(InputEvent.BUTTON1_MASK);
                                }
                            }
                        }
                    }
                }
            } catch (AWTException e) {
                e.printStackTrace();
            }
        } else if(request.getRequestType().equals(PristineRequest.RT_LISTEN)) {
            logger.info("Listen command received, connecting to server");
            sendAudioThread = new SendAudioThread(logger,PristineClient.serverAudioPort);
            sendAudioThread.start();
            logger.info("Making connection");
        } else if (request.getRequestType().equals(PristineRequest.RT_MIC_STOP)) {
            if(sendAudioThread != null && sendAudioThread.isAlive()) {
                Utils.debug("Request to stop mic",getName(),logger);
            } else {
                Utils.debug("Request to stop mic, but it's already stopped",getName(),logger);
            }
            sendAudioThread.setRunning(false);
        } else if(request.getRequestType().equals(PristineRequest.RT_TEXT_SAY)) {
            logger.info("Request to say received");
            String say = request.getAdditionalInfo();
            TTS textToSpeech = new TTS(say);
            textToSpeech.say();
        } else if (request.getRequestType().equals(PristineRequest.RT_CAM_OFF)) {
            Utils.log("Request to turn off webcam",logger);
            PristineClient.setSendWebcam(false);
        } else if(request.getRequestType().equals(PristineRequest.RT_CAM_ON)) {
            Utils.log("Request to turn on webcam",logger);
            PristineClient.setSendWebcam(true);
        } else if (request.getRequestType().equals(PristineRequest.RT_VOLUME_UP)) {
            Utils.log("Request to raise volume",logger);
            try {
                SendAudioThread.raiseVolume();
            } catch(IllegalArgumentException e) {
                logger.warning(e.getMessage());
            }

        } else if (request.getRequestType().equals(PristineRequest.RT_VOLUME_DOWN)) {
            Utils.log("Request to lower volume",logger);
            try {
                SendAudioThread.lowerVolume();
            } catch(IllegalArgumentException ex) {
                logger.warning(ex.getMessage());
            }
        }
        Utils.sendNotify(hostname,"complete",logger);
    }
}
