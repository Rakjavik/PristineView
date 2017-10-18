package com.joel;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

/**
 * Created by rakjavik on 10/12/2017.
 */
public class TTS {
    private static String voiceName = "kevin16";
    private static String textToSay;

    public TTS(String textToSay) {
        this.textToSay = textToSay;
    }
    public void say() {
        VoiceManager vm = VoiceManager.getInstance();
        Voice voice = vm.getVoice(voiceName);
        voice.allocate();
        voice.speak(textToSay);
    }

    public void setTextToSay(String textToSay) {
        this.textToSay = textToSay;
    }

    public static void main(String[] args) {
        TTS tts = new TTS("This is a text to speech");
        tts.say();
    }
}
