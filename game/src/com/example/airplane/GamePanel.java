package com.example.airplane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * 游戏面板类，负责游戏的主要逻辑和绘制。
 */
public class GamePanel extends JPanel implements ActionListener {
    private static final int PLAYER_START_X = 400; // 玩家起始x位置
    private static final int PLAYER_START_Y = 500; // 玩家起始y位置
    private static final int ENEMY_SPAWN_INTERVAL = 1000; // 敌人生成间隔（毫秒）
    private static final int TIMER_DELAY = 10; // 游戏主计时器延迟（毫秒）

    private final Timer timer = new Timer(TIMER_DELAY, this); // 游戏主计时器
    private final Timer enemySpawnTimer = new Timer(ENEMY_SPAWN_INTERVAL, e -> spawnEnemy()); // 敌人生成计时器
    private final Player player = new Player(PLAYER_START_X, PLAYER_START_Y); // 玩家对象
    private final List<Enemy> enemies = new ArrayList<>(); // 敌人列表
    private final List<Bullet> bullets = new ArrayList<>(); // 子弹列表
    private final ScoreManager scoreManager = new ScoreManager(); // 分数管理器

    public GamePanel() {
        setFocusable(true); // 设置面板可以获得键盘焦点
        setBackground(Color.BLACK); // 设置背景颜色
        addKeyListener(new GameKeyAdapter()); // 添加键盘事件监听器
        timer.start(); // 启动游戏主计时器
        enemySpawnTimer.start(); // 启动敌人生成计时器
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // 调用父类的绘制方法
        if (player.isAlive()) {
            player.draw(g); // 绘制玩家
        } else {
            drawGameOverScreen(g); // 绘制游戏结束屏幕
        }
        drawGameObjects(g); // 绘制游戏中的所有对象
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (player.isAlive()) {
            player.move(); // 移动玩家
            enemies.forEach(Enemy::move); // 移动所有敌人
            bullets.forEach(Bullet::move); // 移动所有子弹
            CollisionHandler.handleCollisions(player, enemies, bullets, scoreManager); // 处理碰撞
            enemies.removeIf(enemy -> !enemy.isAlive()); // 移除死亡的敌人
            bullets.removeIf(bullet -> !bullet.isAlive()); // 移除消失的子弹
        }
        repaint(); // 重新绘制面板
    }

    private void drawGameOverScreen(Graphics g) {
        g.setColor(Color.RED); // 设置游戏结束文字颜色
        g.drawString("Game Over", 350, 300); // 显示游戏结束
        g.drawString("Score: " + scoreManager.getScore(), 350, 320); // 显示分数
        g.drawString("Press R to Restart", 350, 340); // 提示重新开始
    }

    private void drawGameObjects(Graphics g) {
        for (Enemy enemy : enemies) {
            enemy.draw(g); // 绘制敌人
        }
        for (Bullet bullet : bullets) {
            bullet.draw(g); // 绘制子弹
        }
        g.setColor(Color.WHITE); // 设置分数文字颜色
        g.drawString("Score: " + scoreManager.getScore(), 10, 10); // 显示当前分数
    }

    private void spawnEnemy() {
        enemies.add(new Enemy((int) (Math.random() * 760), 0)); // 随机生成敌人位置
    }

    private class GameKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (player.isAlive()) {
                player.getKeyAdapter(bullets).keyPressed(e); // 处理玩家按键事件
            } else if (e.getKeyCode() == KeyEvent.VK_R) {
                restartGame(); // 处理重新开始游戏
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (player.isAlive()) {
                player.getKeyAdapter(bullets).keyReleased(e); // 处理玩家按键释放事件
            }
        }

        private void restartGame() {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(GamePanel.this);
            frame.remove(GamePanel.this); // 移除当前游戏面板
            GamePanel newPanel = new GamePanel(); // 创建新的游戏面板
            frame.add(newPanel); // 添加新的游戏面板
            frame.validate(); // 重新验证组件
        }
    }
}
