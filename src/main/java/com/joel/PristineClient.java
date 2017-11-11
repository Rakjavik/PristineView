package com.joel;

import com.joel.misc.Utils;
import com.joel.model.PristineHost;
import com.joel.model.PristineRequest;
import com.joel.model.PristineWebcam;
import com.joel.threads.PollQueueThread;
import com.joel.threads.PristineWritingThread;

import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

/**
 * Created by rakjavik on 9/23/2017.
 */
public class PristineClient {

    private Logger logger;

    public static int buffersize;
    public static String serverIP;
    public static Dimension resolution;
    public static Properties properties;
    public static int serverAudioPort;

    private int serverPort;

    private Socket socket;
    private static String hostname;
    private boolean running;
    private int delay;
    private LinkedList<PristineRequest> sendQueue;
    private static PristineWebcam webcam;



    private String myIP = null;
    private PollQueueThread pollQueueThread;


    public PristineClient() throws InterruptedException, IOException {
        properties = new Properties();
        sendQueue = new LinkedList<>();
        running = true;
        logger = Logger.getLogger(this.getClass().getName());
        logger.addHandler(new FileHandler(System.getProperty("user.dir") + "/ClientLog.log"));
        logger.info(new String("Looking in {0} for properties").replace("{0}", System.getProperty("user.dir")));
        properties.load(new FileInputStream(System.getProperty("user.dir") + "/pristine.properties"));
        Utils.setLoggingLevel(Integer.parseInt(properties.getProperty("loggingLevel")));
        serverIP = properties.getProperty("serverAddress");
        serverPort = Integer.parseInt(properties.getProperty("serverPort"));
        buffersize = Integer.parseInt(properties.getProperty("bufferSize"));
        delay = Integer.parseInt(properties.getProperty("clientLoopDelay"));
        serverAudioPort = Integer.parseInt(properties.getProperty("serverAudioPort"));
        resolution = Toolkit.getDefaultToolkit().getScreenSize();
        myIP = InetAddress.getLocalHost().getHostAddress();
        // Virtual box adapter //
        if(myIP.contains("192.168.56.1")) {
            myIP = InetAddress.getAllByName(hostname)[1].getHostAddress();
        }
        pollQueueThread = new PollQueueThread(hostname,logger);
        pollQueueThread.start();

        try {
            loop();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void makeSocketConnection(String host, int port) throws IOException {
        socket = new Socket(host,port);
    }

    private void loop() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        PristineHost me = new PristineHost(hostname,myIP);

        webcam = PristineWebcam.getInstance(me,logger);
        webcam.setViewSize(new Dimension(320, 240));
        webcam.open();
        Utils.debug("Webcam found", this.getClass().getName(), logger);
        if (webcam.isDetectMotion()) {
            webcam.startMotionListener();
        }
        long delta = 0;
        while(running) {
            long currentTime = System.currentTimeMillis();
            // Send queue for Socket connection to server //
            if (sendQueue.size() > 0) {
                try {
                    makeSocketConnection(serverIP, serverPort);
                } catch (IOException e) {
                    Utils.log(e.getMessage(),logger);
                    continue;
                }

                PristineWritingThread writingThread;
                try {
                    writingThread = new PristineWritingThread(socket.getOutputStream(), sendQueue.getFirst(),logger);
                    writingThread.start();
                    writingThread.join();
                    sendQueue.removeFirst();
                    socket.close();
                } catch (IOException e) {
                    Utils.log(e.getMessage(),logger);
                } catch (InterruptedException e) {
                    Utils.log(e.getMessage(),logger);
                }
            }
            // Generate request to send to server //
            PristineRequest request = new PristineRequest();
            if(webcam.isEnabled()) {
                request = webcam.loop(me,request, (int) delta);
            }
            // If no webcam then just write a blank request to let the server know we exist //
            if(request.isEmpty() && currentTime % 5000 == 0) {
                request.setHost(me);
            }
            if(!request.isEmpty()) {
                sendQueue.add(request);
            }
            delta = System.currentTimeMillis()-currentTime;
        }
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(webcam != null) {
            webcam.close();
        }
        Utils.debug("Client End Run Loop",this.getClass().getName(),logger);
    }

    public static void setSendWebcam(boolean sendWebcam) {
        if(webcam != null) {
            webcam.setStreamingToServer(sendWebcam);
        }
    }

    public static void main (String[] args) throws InterruptedException, IOException {
        System.out.println("Starting Client");
        hostname = System.getenv("userdomain");
        if(hostname == null || hostname.equals("null")) {
            //Try Linux //
            hostname = Runtime.getRuntime().exec("hostname").toString();
            if(hostname == null) {
                hostname = InetAddress.getLocalHost().getHostName();
            }
        }
        PristineClient client = new PristineClient();
    }
}
