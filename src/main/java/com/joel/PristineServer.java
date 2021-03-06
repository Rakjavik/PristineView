package com.joel;

import com.joel.misc.Utils;
import com.joel.model.PristineHost;
import com.joel.model.PristineRequest;
import com.joel.threads.PristineReadingThread;
import com.joel.threads.ReadAudioThread;

import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

/**
 * Created by rakjavik on 9/23/2017.
 */
public class PristineServer {

    private int delay;
    private ServerSocket serverSocket;

    private Socket clientSocket;
    private boolean running;
    private static Logger logger;
    private static ArrayList<PristineHost> hosts;
    private static List<PristineRequest> clientRequests;
    private static ReadAudioThread readAudioThread;

    public static Properties properties = new Properties();
    public static int bufferSize;
    public static int readAudioPort;

    public PristineServer() throws IOException, InterruptedException {
        hosts = new ArrayList<>();
        clientRequests = new ArrayList<>();
        running = true;
        logger = Logger.getLogger(this.getClass().getName());
        logger.addHandler(new FileHandler(System.getProperty("user.dir") + "/ServerLog.log"));
        properties.load(new FileInputStream(System.getProperty("user.dir") + "/pristine.properties"));
        delay = Integer.parseInt(properties.getProperty("serverLoopDelay"));
        bufferSize = Integer.parseInt(properties.getProperty("bufferSize"));
        readAudioPort = Integer.parseInt(properties.getProperty("serverAudioPort"));
        Utils.setLoggingLevel(Integer.parseInt(properties.getProperty("loggingLevel")));
        test();
        loop();
    }

    private void loop() throws IOException, InterruptedException {
        int socketPort = Integer.parseInt(properties.getProperty("serverPort"));
        while (running) {
            // Open socket //
            serverSocket = new ServerSocket(socketPort);

            Utils.log("Listening for connection", logger);
            clientSocket = serverSocket.accept();
            // Start reading //
            PristineReadingThread readingThread = new PristineReadingThread(clientSocket.getInputStream(), logger);
            readingThread.start();
            readingThread.join();
            // Reading complete //
            if (readingThread.getRequest() != null) {
                Utils.debug("Request received " + readingThread.getRequest(), this.getClass().getName(), logger);
                PristineRequest request = readingThread.getRequest();
                PristineHost host = getHost(request.getHost().getHostname());
                logHost(request);
                if (request.getHost() != null) {
                    Image image = request.decodeAndGetImage();
                    if (image != null && host != null) {
                        host.setCurrentShot(image);
                    }
                }
                serverSocket.close();
                Thread.sleep(delay);
            }
        }
    }

    public static synchronized boolean addRequestToQueue(PristineRequest request) {
        return clientRequests.add(request);
    }
    public static synchronized PristineRequest getRequestFromQueue(String hostname) {
        if(clientRequests != null && clientRequests.size() > 0) {
            for (int count = 0; count < clientRequests.size(); count++) {
                if (clientRequests.get(count).getHost().getHostname().toLowerCase().equals(hostname.toLowerCase())) {
                    PristineRequest request = clientRequests.get(count);
                    clientRequests.remove(count);
                    return request;
                }
            }
        }
        return null;
    }

    public static void main(String[] args) throws InterruptedException {
        try {
            PristineServer server = new PristineServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static PristineHost getHost(String hostname) {
        for (PristineHost host : hosts) {
            if (host.getHostname().toLowerCase().equals(hostname.toLowerCase())) {
                return host;
            }
        }
        return null;
    }

    private boolean logHost(PristineRequest request) {
        boolean entryExists = false;
        // Loop through known hosts //
        for(int count = 0; count < hosts.size(); count++){
            // Request is for currently known host //
            if(request.getHost().getHostname().equals(hosts.get(count).getHostname())) {
                hosts.get(count).setTimeOfLastSentRequest(System.currentTimeMillis());
                hosts.get(count).setConnected(true);
                hosts.get(count).setRecording(request.getHost().isRecording());
                entryExists = true;
            }
            // Request is for other logged host //
            else {
                if(hosts.get(count).isConnected() && (System.currentTimeMillis() - hosts.get(count).getTimeOfLastSentRequest()) > 30000) {
                    hosts.get(count).setConnected(false);
                    logger.info(hosts.get(count).getHostname() + " - DISCONNECTED");
                }
            }
        }
        if(!entryExists) {
            hosts.add(request.getHost());
            logger.info(request.getHost().getHostname() + " - ADDED TO host list");
        }
        return true;
    }


    public static PristineHost[] getHostLists() {
        List<PristineHost> hostsList = new LinkedList<>();
        for (int count = 0; count < hosts.size(); count++) {
            if (hosts.get(count).isConnected()) {
                hostsList.add(hosts.get(count));
            }
        }
        PristineHost[] returnArray = new PristineHost[hostsList.size()];
        return hostsList.toArray(returnArray);
    }

    public static void setReadAudioThread(ReadAudioThread readAudioThread,boolean overrideCurrentThread) {
        if(!overrideCurrentThread) {
            if (readAudioThread != null && readAudioThread.isAlive()) {
                Utils.log("ReadAudioThread already running",logger);
                return;
            }
        }
        PristineServer.readAudioThread = readAudioThread;
        PristineServer.readAudioThread.start();
    }

    private void test() {
    }
}
