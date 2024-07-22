package com.ex.airplane.multiplayer;

import com.ex.airplane.GameObject.Bullet;
import com.ex.airplane.GameObject.GameObject;
import com.ex.airplane.UI.GamePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * 玩家飞机类，负责玩家飞机的属性、移动和绘制。
 */
public class MultiPlayer extends GameObject {
    private static final int MOVE_STEP = 5; // 玩家每次移动的步长

    private String username;  //玩家用户名。。。。
    private int score;  //玩家分数。。。。。。

    private int dx, dy; // 移动方向
    private boolean alive = true; // 存活状态
    private final Image image; // 玩家图片

    public void setDx(int dx) {  //控制x轴移动方向
        this.dx = dx;
    }

    public void setDy(int dy) {  //控制Y轴移动方向
        this.dy = dy;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }

    public MultiPlayer(int startX, int startY, String username, int score) {
        this.username = username;
        this.score = score;
        x = startX; // 设置玩家初始x位置
        y = startY; // 设置玩家初始y位置
        width = 50; // 设置玩家宽度
        height = 50; // 设置玩家高度
        image = new ImageIcon(getClass().getResource("/player.png")).getImage(); // 加载玩家图片
    }

    @Override
    public void move() {
        x += dx; // 根据dx更新x位置
        y += dy; // 根据dy更新y位置

        // 限制玩家在面板内移动
        if (x < 0) x = 0;
        if (x > GamePanel.PANEL_WIDTH - width) x = GamePanel.PANEL_WIDTH - width;
        if (y < 0) y = 0;
        if (y > GamePanel.PANEL_HEIGHT - height) y = GamePanel.PANEL_HEIGHT - height;
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(image, x, y, width, height, null); // 绘制玩家图片
        g.drawString(username, x, y+10); // 绘制玩家图片
    }

    public void fire(List<Bullet> bullets) {
        bullets.add(new Bullet(x + width / 2 - 2, y)); // 创建并添加子弹
    }

    public KeyAdapter getKeyAdapter(List<Bullet> bullets) {
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT -> dx = -MOVE_STEP;
                    case KeyEvent.VK_RIGHT -> dx = MOVE_STEP;
                    case KeyEvent.VK_UP -> dy = -MOVE_STEP;
                    case KeyEvent.VK_DOWN -> dy = MOVE_STEP;
                    case KeyEvent.VK_SPACE -> fire(bullets); // 发射子弹
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT -> dx = 0;
                    case KeyEvent.VK_UP, KeyEvent.VK_DOWN -> dy = 0;
                }
            }
        };
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }
}
