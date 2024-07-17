package com.example.airplane;

import javax.swing.*;

/**
 * 程序入口类，用于启动游戏。
 */
public class Main {
    public static void main(String[] args) {
        // 创建窗口并设置标题
        JFrame frame = new JFrame("Airplane Battle");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 设置关闭操作
        frame.setSize(800, 600); // 设置窗口尺寸
        frame.add(new GamePanel()); // 添加游戏面板
        frame.setVisible(true); // 显示窗口
    }
}
