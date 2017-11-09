package com.joel.servlets;

import com.google.gson.Gson;
import com.joel.PristineServer;
import com.joel.misc.Utils;
import com.joel.model.PristineHost;
import com.joel.model.PristineRequest;
import com.joel.threads.ReadAudioThread;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.RenderedImage;
import java.io.*;
import java.util.Base64;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

/**
 * Created by rakjavik on 9/23/2017.
 */
public class PristineServlet extends HttpServlet {

    private Logger logger;
    public static Thread serverThread = null;
    private PristineServer server = null;
    private static Gson gson = new Gson();

    public static final String PROPS_REQUEST_TYPE = "requestType";
    public static final String PROPS_HOST_NAME = "hostname";
    public static final String PROPS_HOST = "host";
    public static final String PROPS_UPDATE = "update";
    public static final String CONTEXT_ROOT = "";

    public PristineServlet() throws IOException, InterruptedException {
        super();
        //test();
        logger = Logger.getLogger(this.getClass().getName());
        logger.addHandler(new FileHandler("c:/temp/log.log"));
        if(serverThread == null) {
            serverThread = new Thread(() -> {
                try {
                    server = new PristineServer();
                } catch (IOException e) {
                    Utils.log(e.getMessage(), logger);
                } catch (InterruptedException e) {
                    Utils.log(e.getMessage(), logger);
                }
            });
            serverThread.start();
        }

    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Utils.debug("Do get called", getClass().getName(), logger);
        if(req.getQueryString() == null) {
            logger.info("Query string null");
            return;
        }
        Map<String, String> arguments = Utils.splitQuery(req.getQueryString());
        Utils.log("Arguments - " + arguments.toString(),logger);
        if(arguments.get(PROPS_REQUEST_TYPE).equals(PristineRequest.RT_IMAGE_UPLOAD)) {
            Image image = null;
            PristineHost host = null;
            if(arguments.get(PROPS_HOST) != null) {
                host = PristineServer.getHost(arguments.get(PROPS_HOST));
                if(host != null) {
                    image = host.getCurrentShot();
                }
            }
            if(image != null) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ImageIO.write((RenderedImage) image, "png",outputStream );
                String encoded = Base64.getEncoder().encodeToString(outputStream.toByteArray());
                PristineRequest request = new PristineRequest(host);
                request.setFile64(encoded);
                request.setRequestType(PristineRequest.RT_IMAGE_UPLOAD);
                String toWrite = gson.toJson(request);
                writeString(toWrite,resp);
            }
        } else if (arguments.get(PROPS_REQUEST_TYPE).equals(PristineRequest.RT_LIST)) {
            PristineHost[] hosts = PristineServer.getHostLists();
            String hostsJSON = gson.toJson(hosts);
            writeString(hostsJSON,resp);
        } else if(arguments.get(PROPS_REQUEST_TYPE).equals(PristineRequest.RT_GET_FROM_QUEUE)) {
            PristineRequest request = PristineServer.getRequestFromQueue(arguments.get("hostname"));
            if(request != null) {
                writeString(gson.toJson(request),resp);
            }
        } else if (arguments.get(PROPS_REQUEST_TYPE).equals(PristineRequest.RT_NOTIFY_COMMAND)) {
            String hostname = arguments.get(PROPS_HOST_NAME);
            Utils.log("Notify from " + hostname,logger);
            PristineRequest request = new PristineRequest(new PristineHost("server",null));
            request.setRequestType(PristineRequest.RT_NOTIFY_COMMAND);
            request.setAdditionalInfo(hostname + ":" + arguments.get(PROPS_UPDATE));
            PristineServer.addRequestToQueue(request);
        }
        else if(arguments.get(PROPS_REQUEST_TYPE).equals(PristineRequest.RT_PUT_IN_QUEUE)) {
            PristineRequest request = gson.fromJson(arguments.get("json"), PristineRequest.class);
            PristineServer.addRequestToQueue(request);
            if(request.getRequestType().equals(PristineRequest.RT_LISTEN)) {
                Utils.debug("Mic start request from" + request.getHost().getHostname(), getClass().getName(), logger);
                server.setReadAudioThread(new ReadAudioThread(logger, 4445), true);
            }
            else if (request.getRequestType().equals(PristineRequest.RT_MIC_STOP)) {
                Utils.debug("Mic stop request from" + request.getHost().getHostname(),getClass().getName(),logger);
            }
            else if (request.getRequestType().equals(PristineRequest.RT_TEXT_SAY)) {
                Utils.debug("Request for text to speech received for " + request.getHost().getHostname(),getServletName(),logger);
            }
        }
    }

    private void writeString(String json,HttpServletResponse response) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(json));
        String line;
        while((line = reader.readLine()) != null) {
            response.getOutputStream().println(line);
        }
        response.getOutputStream().close();
    }
}
