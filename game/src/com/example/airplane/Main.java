package com.example.airplane;

import javax.swing.*;
import java.awt.*;

/**
 * 游戏的主入口类，包括主界面和游戏启动逻辑。
 */
public class Main extends JFrame{
    private static JFrame frame; // 主界面窗口

    public static void main(String[] args) {
        frame = new JFrame("星际空战");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 设置窗口关闭时退出程序
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

        JButton startButton = new JButton("单人模式");
        startButton.addActionListener(e -> startGame()); // 添加按钮点击事件监听器
        panel.add(startButton);

        JButton multiplayerButton = new JButton("多人模式");
        multiplayerButton.addActionListener(e -> multiplayerstartGame());  // 添加按钮点击事件监听器
        panel.add(multiplayerButton);

        JButton exitButton = new JButton("退出");
        exitButton.addActionListener(e -> exitGame());  // 添加按钮点击事件监听器
        panel.add(exitButton);

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

    /**
     * 开始多人游戏的界面初始化方法。
     * 该方法用于设置多人游戏开始时的用户界面，提供创建房间和加入房间的功能选项。
     * 当用户点击相应按钮时，会弹出多人游戏对话框，允许用户进行进一步的操作。
     */
    /**
     * 开始多人游戏的界面初始化方法。
     * 直接弹出创建房间的对话框。
     */
    private static void multiplayerstartGame() {
        // 直接实例化并显示创建和加入房间的对话框
        MultiplayerDialog dialog = new MultiplayerDialog(frame);
        dialog.setVisible(true); //可视化
    }




    /**
     * 退出游戏，关闭窗口，并执行必要的清理工作。
     */
    public static void exitGame() {
        frame.dispose(); // 关闭窗口
        // 这里可以添加其他的清理工作，例如停止游戏逻辑、保存游戏状态等
    }

}
