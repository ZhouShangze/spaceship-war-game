package com.example.airplane.GameObject;

import java.awt.*;
import javax.swing.ImageIcon;

/**
 * 子弹类，负责子弹的属性、移动和绘制。
 */
public class Bullet extends GameObject {
    private final Image image; // 子弹图片

    public Bullet(int x, int y) {
        this.x = x; // 设置子弹初始x位置
        this.y = y; // 设置子弹初始y位置
        width = 5; // 设置子弹宽度
        height = 10; // 设置子弹高度
        image = new ImageIcon(getClass().getResource("/resources/bullet.png")).getImage(); // 加载子弹图片
    }

    @Override
    public void move() {
        y -= 2; // 子弹向上移动
        if (y < 0) setAlive(false); // 超出屏幕上边界，设置为不存活
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(image, x, y, width, height, null); // 绘制子弹图片
    }
}
