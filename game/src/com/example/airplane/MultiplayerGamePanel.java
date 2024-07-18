package com.example.airplane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MultiplayerGamePanel extends GamePanel {

    private String username; // 用户名
    private Socket socket; // 套接字
    private BufferedReader in; // 输入流
    private PrintWriter out; // 输出流
    private String[] players; // 玩家列表
    private Timer timer; // 定时器

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

        timer = new Timer(100, new ActionListener() { // 初始化定时器
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

    public void startGame() {
        // 游戏开始的逻辑
        // 这里可以调用 GamePanel 中的游戏开始逻辑，如果有的话
        super.startGame();

        if (timer != null && timer.isRunning()) {
            timer.stop(); // 停止定时器
        }
    }

}
