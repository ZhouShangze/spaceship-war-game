package com.ex.airplane;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class AudioPlayer { // 音频播放器
    private Clip clip;

    public AudioPlayer(String filePath) {
        try {
            // 打开音频输入流
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(filePath));
            // 获取音频格式和数据
            AudioFormat format = audioInputStream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            // 打开音频剪辑并加载样本数据
            clip = (Clip) AudioSystem.getLine(info);
            clip.open(audioInputStream);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void play() {
        if (clip != null) {
            clip.start();
        }
    }

    public void stop() {
        if (clip != null) {
            clip.stop();
        }
    }

    public void loop(int count) {
        if (clip != null) {
            clip.loop(count);
        }
    }

    public static void main(String[] args) {
        AudioPlayer player = new AudioPlayer("path/to/your/soundfile.wav");
        player.play();
    }
}
