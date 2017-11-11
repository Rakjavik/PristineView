package com.joel.threads;

import com.joel.PristineClient;
import com.joel.PristineServer;
import com.joel.misc.Utils;
import com.joel.model.AbstractAudioThread;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Created by rakjavik on 10/10/2017.
 */
public class ReadAudioThread extends AbstractAudioThread {

    private ServerSocket serverSocket;

    public ReadAudioThread(Logger logger,int portNumber) {
        super(logger,portNumber);
    }

    @Override
    public void run() {
        Utils.log("Read Audio Thread started",logger);
        try {
            serverSocket = new ServerSocket(PristineServer.readAudioPort);
            Socket clientSocket = serverSocket.accept();

            SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(sourceInfo);
            sourceDataLine.open();
            sourceDataLine.start();

            byte[] buffer = new byte[PristineClient.buffersize];
            while(clientSocket.getInputStream().read(buffer) != -1) {
                sourceDataLine.write(buffer,0,buffer.length);
            }
            clientSocket.getInputStream().close();
            sourceDataLine.drain();
            sourceDataLine.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public static void main (String[] args) {
        //ReadAudioThread readAudioThread = new ReadAudioThread(Logger.getLogger("name"));
        //readAudioThread.start();
    }
}
