package com.arise.weland.desk;

import com.arise.core.tools.Util;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;

/**
 * Created by Alexandru on 8/27/2025.
 */
public class JavaxClip {
    private Clip c;
    private LineListener lineListener;

    public JavaxClip(Clip clip){
        this.c = clip;
    }

    public Clip clip(){
        return c;
    }

    public void setLineListener(LineListener param){
        c.addLineListener(param);
        lineListener = param;
    }

    public void removeListeners(){
        if(lineListener != null) {
            c.removeLineListener(lineListener);
        }
    }

    public void stop() {
        if(c != null){
            try {
                c.stop();
                Util.close(c);
                c.drain();
                c.flush();
            } catch (Exception e){

            }
        }

        c = null;
    }

    public void open(AudioInputStream aStream) throws IOException, LineUnavailableException {
        c.open(aStream);
    }

    public void start() {
        c.start();
    }
}
