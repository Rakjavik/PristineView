package com.joel.model;

import com.joel.misc.Utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * Created by rakjavik on 9/23/2017.
 */
public class PristineRequest {

    public static final String RT_LIST = "list";
    public static final String RT_GET_FROM_QUEUE = "getfromqueue";
    public static final String RT_PUT_IN_QUEUE = "putinqueue";
    public static final String RT_IMAGE_UPLOAD = "image";
    public static final String RT_COMMAND = "command";
    public static final String RT_GET_SCREENSHOT = "screenshot";
    public static final String RT_LISTEN = "miclisten";
    public static final String RT_MIC_STOP = "micstop";
    public static final String RT_TEXT_SAY = "talk";
    public static final String RT_CAM_ON = "webcamon";
    public static final String RT_CAM_OFF = "webcamoff";
    public static final String RT_RESUME_NETFLIX = "ResumeNetflix";
    public static final String RT_NOTIFY_COMMAND = "notify";
    public static final String RT_VOLUME_DOWN = "volumedown";
    public static final String RT_VOLUME_UP = "volumeup";
    public static final String RT_RESTART_VR = "restartVR";

    public static final String UPDATE_CAM_STOPPED_RECORDING = "recordStop";
    public static final String UPDATE_CAM_STARTED_RECORDING = "recordStart";
    public static final String UPDATE_CAM_GET_REC_STATUS = "recordStatus";

    private String file64;
    private String requestType;
    private String additionalInfo;
    private String filename;
    private PristineHost host;

    public PristineRequest(){}

    public PristineRequest(PristineHost host) {
        this.host = host;
    }

    public byte[] getAndDecode() {
        return Utils.decode(file64);
    }

    public Image decodeAndGetImage() throws IOException {
        if(file64 != null) {
            byte[] bytes = Base64.getDecoder().decode(file64);
            return ImageIO.read(new ByteArrayInputStream(bytes));
        } else {
            return null;
        }
    }

    public String getFile64() {
        return file64;
    }

    public void setFile64(String file64) {
        this.file64 = file64;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public PristineHost getHost() {
        return host;
    }

    public void setHost(PristineHost host) {
        this.host = host;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public boolean isEmpty() {
        if( file64 == null && requestType == null && additionalInfo == null && filename == null && host == null) {
            return true;
        }
        return false;
    }
}
