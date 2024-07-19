package src.com.ex.airplane.GameObject;

import java.awt.*;

/**
 * 游戏对象的抽象类，提供基本属性和方法。
 */
public abstract class GameObject {
    protected int x, y, width, height; // 位置和尺寸
    protected boolean alive = true; // 存活状态

    public boolean isAlive() {
        return alive; // 返回存活状态
    }

    public void setAlive(boolean alive) {
        this.alive = alive; // 设置存活状态
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height); // 获取对象的边界矩形
    }

    public abstract void move(); // 抽象移动方法
    public abstract void draw(Graphics g); // 抽象绘制方法
}
