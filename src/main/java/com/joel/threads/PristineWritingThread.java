package com.joel.threads;

import com.joel.misc.Utils;
import com.joel.model.PristineRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

/**
 * Created by rakjavik on 9/23/2017.
 */
public class PristineWritingThread extends Thread {
    private OutputStream outputStream;
    private PristineRequest request;
    private Logger logger;

    public PristineWritingThread(OutputStream outputStream,PristineRequest request,Logger logger) {
        this.outputStream = outputStream;
        this.request = request;
        this.logger = logger;
    }

    @Override
    public void run() {
        super.run();
        try {
            if(Utils.writePristineRequest(request,outputStream,logger)) {
                outputStream.close();
            }
        } catch (IOException e) {
            Utils.debug(e.getMessage(),this.getClass().getName(),logger);
        }
    }

}
