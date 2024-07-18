package com.example.airplane;

import javax.swing.*;

/**
 * 游戏的主入口类，包括主界面和游戏启动逻辑。
 */
public class Main {
    private static JFrame frame; // 主界面窗口

    public static void main(String[] args) {
        frame = new JFrame("星际空战");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 设置关闭操作
        frame.setSize(400, 300); // 设置窗口尺寸

        showMainPanel(); // 显示主界面
    }

    /**
     * 显示主界面，包括 "开始游戏" 按钮。
     */
    private static void showMainPanel() {
        JPanel panel = new JPanel();
        frame.getContentPane().removeAll(); // 清除之前的内容
        frame.add(panel);

        JButton startButton = new JButton("开始游戏");
        startButton.addActionListener(e -> startGame()); // 添加按钮点击事件监听器
        panel.add(startButton);

        frame.setVisible(true); // 显示主界面
    }

    /**
     * 启动游戏的方法，创建并显示游戏界面。
     */
    private static void startGame() {
        frame.getContentPane().removeAll(); // 清除之前的内容

        GamePanel gamePanel = new GamePanel(); // 创建游戏面板
        frame.add(gamePanel); // 添加游戏面板

        frame.setSize(800, 600); // 设置游戏界面尺寸
        frame.setVisible(true); // 显示游戏界面

        gamePanel.requestFocusInWindow(); // 确保游戏面板获得键盘焦点
        gamePanel.startGame(); // 启动游戏相关计时器和定时器
    }
}
