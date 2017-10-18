package com.joel.threads;

import com.joel.PristineClient;
import com.joel.misc.Utils;
import com.joel.model.AbstractAudioThread;
import sun.audio.AudioStream;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Created by rakjavik on 10/10/2017.
 */
public class SendAudioThread extends AbstractAudioThread {

    private boolean running = true;
    private static TargetDataLine targetDataLine;

    public SendAudioThread(Logger logger,int portNumber) {
        super(logger,portNumber);
    }

    @Override
    public void run() {

        targetDataLine = null;
        Socket socket = null;
        try {
            socket = new Socket(PristineClient.serverIP,4445);
            socket.setSoTimeout(1000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            targetDataLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
            targetDataLine.open();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        targetDataLine.start();

        int numBytesRead;
        byte[] targetData = new byte[PristineClient.buffersize];
        Utils.log("Starting stream",logger);

        while (running) {
            numBytesRead = targetDataLine.read(targetData, 0, targetData.length);

            if (numBytesRead == -1)	break;

            if (socket != null) {
                try {
                    socket.getOutputStream().write(targetData, 0, numBytesRead);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        targetDataLine.drain();
        targetDataLine.close();
        Utils.log("Ending Stream",logger);
    }

    public synchronized void setRunning(boolean running) {
        this.running = running;
        Utils.debug("Stopping send audio thread",getName(),logger);
    }

    public static void lowerVolume() {
        if(targetDataLine == null) {
            getTargetDataLine();
        }
        FloatControl gainControl = (FloatControl) targetDataLine.getControl(FloatControl.Type.MASTER_GAIN);
        gainControl.setValue(-10);
    }
    public static void raiseVolume() {
        if(targetDataLine == null) {
            getTargetDataLine();
        }
        FloatControl gainControl = (FloatControl) targetDataLine.getControl(FloatControl.Type.MASTER_GAIN);
        gainControl.setValue(+10);
    }

    private static void getTargetDataLine() {
        try {
            targetDataLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

}
