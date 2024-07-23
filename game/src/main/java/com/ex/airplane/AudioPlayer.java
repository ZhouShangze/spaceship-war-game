package com.ex.airplane;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class AudioPlayer {  //音效播放
    private Clip clip;

    public AudioPlayer(URL url) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(url);
            AudioFormat format = audioInputStream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            clip = (Clip) AudioSystem.getLine(info);
            clip.open(audioInputStream);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void play() {    //播放音效
        if (clip != null) {
            clip.start();
        }
    }

    public void stop() {  //停止音效
        if (clip != null) {
            clip.stop();
            clip.setFramePosition(0); //重置
        }
    }

    public void loop(int count) {  //循环播放
        if (clip != null) {
            clip.loop(count);
        }
    }
}
