package com.ex.airplane.GameObject;

import com.ex.airplane.UI.GamePanel;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * 奖励类，用于表示游戏中的奖励对象。
 */
public class Reward {
    private static final int WIDTH = 20; // 奖励宽度
    private static final int HEIGHT = 20; // 奖励高度
    private static final Color COLOR = Color.YELLOW; // 奖励颜色

    private int x; // 奖励的x位置
    private int y; // 奖励的y位置
    private boolean alive = true; // 奖励是否存在

    public Reward(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * 绘制奖励。
     * @param g 绘图对象
     */
    public void draw(Graphics g) {
        if (alive) {
            g.setColor(COLOR);
            g.fillRect(x, y, WIDTH, HEIGHT); // 绘制奖励矩形
        }
    }

    /**
     * 移动奖励。
     */
    public void move() {
        y += 2; // 奖励下落的速度
        if (y > GamePanel.PANEL_HEIGHT) {
            alive = false; // 奖励超出屏幕下边界，标记为不可见
        }
    }

    /**
     * 判断奖励是否与指定的矩形相撞。
     * @param rect 矩形区域
     * @return 是否相撞
     */
    public boolean isColliding(Rectangle rect) {
        Rectangle rewardRect = new Rectangle(x, y, WIDTH, HEIGHT);
        return rewardRect.intersects(rect);
    }

    /**
     * 返回奖励的边界矩形。
     * @return 奖励的边界矩形
     */
    public Rectangle getBounds() {
        return new Rectangle(x, y, WIDTH, HEIGHT);
    }

    /**
     * 判断奖励是否存在。
     * @return 是否存在
     */
    public boolean isAlive() {
        return alive;
    }

    /**
     * 设置奖励的存在状态。
     * @param alive 是否存在
     */
    public void setAlive(boolean alive) {
        this.alive = alive;
    }
}
