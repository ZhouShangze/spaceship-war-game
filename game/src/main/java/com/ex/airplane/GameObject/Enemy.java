package com.ex.airplane.GameObject;

import java.awt.*;
import java.util.Objects;
import javax.swing.ImageIcon;

/**
 * 敌机类，负责敌机的属性、移动和绘制。
 */
public class Enemy extends GameObject {
    private final Image image; // 敌机图片

    public Enemy(int x, int y) {
        this.x = x; // 设置敌机初始x位置
        this.y = y; // 设置敌机初始y位置
        width = 50; // 设置敌机宽度
        height = 50; // 设置敌机高度
        image = new ImageIcon(Objects.requireNonNull(getClass().getResource("/images/dj.png"))).getImage(); // 加载敌机图片
    }

    @Override
    public void move() {
        y += 1; // 敌机向下移动
        if (y > 600) setAlive(false); // 超出屏幕下边界，设置为不存活
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(image, x, y, width, height, null); // 绘制敌机图片
    }
}
