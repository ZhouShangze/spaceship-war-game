package com.example.airplane.UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * MultiplayerGamePanel 类表示多人游戏模式下的游戏面板。
 * 它处理与服务器的通信并显示游戏状态。
 */
public class MultiplayerGamePanel extends GamePanel {

    private final String username; // 用户名
    private final Socket socket; // 套接字
    private final BufferedReader in; // 输入流
    private final PrintWriter out; // 输出流
    private String[] players; // 玩家列表
    private Timer timer; // 定时器

    /**
     * 构造函数，初始化多人游戏面板。
     *
     * @param username 用户名
     * @param socket   套接字
     * @param in       输入流
     * @param out      输出流
     */
    public MultiplayerGamePanel(String username, Socket socket, BufferedReader in, PrintWriter out) {
        super(); // 调用 GamePanel 的构造函数
        this.username = username;
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.players = new String[]{username}; // 初始化玩家列表

        setLayout(null); // 取消布局管理

        JButton startButton = new JButton("开始"); // 创建开始按钮
        startButton.setBounds(350, 500, 100, 50); // 设置按钮位置和大小
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                out.println("START;" + username); // 向服务器发送开始游戏的消息
                startGame(); // 启动游戏
            }
        });
        add(startButton); // 将按钮添加到面板

        // 初始化定时器，处理服务器消息
        timer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String message = in.readLine(); // 读取服务器发送的消息
                    if (message != null) {
                        String[] parts = message.split(";"); // 分割消息字符串
                        if ("NEW_USER".equals(parts[0])) {
                            String newUser = parts[1]; // 获取新用户的用户名
                            String[] newPlayers = new String[players.length + 1];
                            System.arraycopy(players, 0, newPlayers, 0, players.length);
                            newPlayers[players.length] = newUser;
                            players = newPlayers; // 更新玩家列表
                            repaint(); // 重新绘制面板
                        } else if ("GAME_STARTING".equals(parts[0])) {
                            // 处理游戏即将开始的逻辑
                            displayCountdown();
                        } else if ("GAME_STARTED".equals(parts[0])) {
                            // 处理游戏开始的逻辑
                            startGame();
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        timer.start(); // 启动定时器
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 画星空背景
        g.setColor(Color.WHITE);
        for (int i = 0; i < 100; i++) {
            int x = (int) (Math.random() * getWidth());
            int y = (int) (Math.random() * getHeight());
            g.drawLine(x, y, x, y);
        }

        // 画战机
        for (int i = 0; i < players.length; i++) {
            int x = 100 + i * 150;
            int y = 400;
            g.setColor(Color.RED);
            g.fillRect(x, y, 50, 50);
            g.setColor(Color.WHITE);
            g.drawString(players[i], x + 5, y + 25);
        }
    }

    /**
     * 显示3秒倒计时。
     */
    private void displayCountdown() {
        // 创建一个新的线程来显示倒计时，以避免阻塞主线程
        new Thread(() -> {
            for (int i = 3; i > 0; i--) {
                System.out.println("游戏将在 " + i + " 秒后开始...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            // 通知游戏开始
            out.println("GAME_STARTED;" + username);
        }).start();
    }

    /**
     * 开始游戏的逻辑。
     */
    @Override
    public void startGame() {
        // 游戏开始的逻辑
        super.startGame();

        if (timer != null && timer.isRunning()) {
            timer.stop(); // 停止定时器
        }
    }
}
