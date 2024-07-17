package com.example.airplane;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.ImageIcon;

/**
 * 玩家类，负责玩家飞机的属性、移动和发射子弹。
 */
public class Player extends GameObject {
    private int dx, dy; // 水平和垂直方向的移动量
    private boolean alive = true; // 玩家存活状态
    private final Image image; // 玩家飞机图片

    public Player(int startX, int startY) {
        x = startX; // 设置玩家初始x位置
        y = startY; // 设置玩家初始y位置
        width = 50; // 设置玩家宽度
        height = 50; // 设置玩家高度
        image = new ImageIcon(getClass().getResource("/resources/player.png")).getImage(); // 加载玩家图片
    }

    @Override
    public void move() {
        x += dx; // 更新x位置
        y += dy; // 更新y位置
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(image, x, y, width, height, null); // 绘制玩家图片
    }

    public void fire(List<Bullet> bullets) {
        bullets.add(new Bullet(x + width / 2 - 2, y)); // 创建并添加子弹
    }

    public KeyAdapter getKeyAdapter(List<Bullet> bullets) {
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT -> dx = -1; // 左箭头键，向左移动
                    case KeyEvent.VK_RIGHT -> dx = 1; // 右箭头键，向右移动
                    case KeyEvent.VK_UP -> dy = -1; // 上箭头键，向上移动
                    case KeyEvent.VK_DOWN -> dy = 1; // 下箭头键，向下移动
                    case KeyEvent.VK_SPACE -> fire(bullets); // 空格键，发射子弹
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT -> dx = 0; // 停止水平移动
                    case KeyEvent.VK_UP, KeyEvent.VK_DOWN -> dy = 0; // 停止垂直移动
                }
            }
        };
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive; // 设置存活状态
    }
}
