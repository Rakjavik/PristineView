package com.joel.threads;

import com.joel.misc.Utils;
import com.joel.model.PristineRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * Created by rakjavik on 9/23/2017.
 */
public class PristineReadingThread extends Thread {

    private InputStream inputStream;
    private PristineRequest request;
    private Logger logger;

    public PristineReadingThread(InputStream inputStream,Logger logger) {
        this.inputStream = inputStream;
        this.logger = logger;
    }

    public PristineRequest getRequest() {
        return request;
    }

    @Override
    public void run() {
        super.run();
        try {
            request = Utils.readPristineRequest(inputStream,logger);
            if(request != null) {
                Utils.log("Read request from " + request.getHost().getHostname(),logger);
            }
            inputStream.close();
        } catch (IOException e) {
            Utils.log(e.getMessage(),logger);
        }
    }
}
