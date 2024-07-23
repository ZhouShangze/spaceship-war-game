package com.ex.airplane.UI;

import com.ex.airplane.multiplayer.MultiPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * MultiplayerGamePanel 类表示多人游戏模式下的游戏面板。
 * 它处理与服务器的通信并显示游戏状态。
 */
public class MultiplayerGamePanel extends BaseGamePanel {

     // 用户名
    private final Socket socket; // 套接字
    private final BufferedReader in; // 输入流
    private final PrintWriter out; // 输出流
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
        super(username); // 调用 MultiplayerGamePanel 的构造函数
        this.socket = socket;
        this.in = in;
        this.out = out;

        setLayout(null); // 取消布局管理

        addKeyListener(new GameKeyAdapter()); // 添加键盘事件监听器

        //开启线程读取服务器的消息
        new Thread(){
            public void run(){
                try {
                    while(true) {
                        String message = in.readLine(); // 读取服务器发送的消息
                        if (message != null) {
                            System.out.println(":: " + message);

                            onMessage(message);

                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.start();

        // 初始化定时器，处理服务器消息
        timer = new Timer(10, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                repaint(); // 重新绘制面板
            }
        });
        timer.start(); // 启动定时器

        startGame();
    }


    private synchronized void onMessage(String message){
        String[] parts = message.split(";"); // 分割消息字符串
        if ("NEW_USER".equals(parts[0])) {
            String newUserName = parts[1]; // 获取新用户的用户名
            int x = Integer.parseInt(parts[2]);
            int y = Integer.parseInt(parts[3]);
            players.add(new MultiPlayer(x, y, newUserName, 0));
        }else if (message.startsWith("player")) {
            parseGameObjects(message);
        }else if(message.startsWith("GAME-OVER")) {

        }
    }

    /**
     * 处理按键事件，包括游戏重启与退出功能。
     */
    private class GameKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if(out != null) {
                out.println("KEY_PRESSED;"+e.getKeyCode());
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if(out != null) {
                out.println("KEY_RELEASED;"+e.getKeyCode());
            }
        }
    }

    @Override
    protected synchronized void paintComponent(Graphics g) {
        super.paintComponent(g);
    }


    /**
     * 开始游戏的逻辑。
     */
    @Override
    public void startGame() {
        // 游戏开始的逻辑
        super.startGame();
    }
}
