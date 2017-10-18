package com.joel.model;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.util.logging.Logger;

/**
 * Created by rakjavik on 10/10/2017.
 */
public abstract class AbstractAudioThread extends Thread {
    protected static AudioFormat format = new AudioFormat(44100, 16, 2, true, true);
    protected static DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, format);
    protected static DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class, format);
    protected Logger logger;
    protected int portNumber;

    public AbstractAudioThread(Logger logger,int portNumber) {
        this.logger = logger;
        this.portNumber = portNumber;
    }
}
