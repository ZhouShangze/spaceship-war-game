package com.ex.airplane.UI;

import com.ex.airplane.GameObject.Bullet;
import com.ex.airplane.GameObject.Enemy;
import com.ex.airplane.GameObject.Reward;
import com.ex.airplane.ScoreManager;
import com.ex.airplane.CollisionHandler;
import com.ex.airplane.multiplayer.MultiPlayer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * 游戏面板类，负责游戏的主要逻辑和绘制。
 */
public class BaseGamePanel extends JPanel implements ActionListener {
    public static final int PANEL_WIDTH = 800; // 面板宽度
    public static final int PANEL_HEIGHT = 600; // 面板高度
    private static final int PLAYER_START_X = 400; // 玩家起始x位置
    private static final int PLAYER_START_Y = 500; // 玩家起始y位置
    private static final int ENEMY_SPAWN_INTERVAL = 1000; // 敌人生成间隔（毫秒）
    private static final int REWARD_SPAWN_INTERVAL = 5000; // 奖励生成间隔（毫秒）

    protected final Vector<Enemy> enemies = new Vector<>(); // 敌人列表
    protected final Vector<Bullet> bullets = new Vector<>(); // 子弹列表
    protected final Vector<Reward> rewards = new Vector<>(); // 奖励列表

    protected final Vector<MultiPlayer> players = new Vector<>(); // 玩家列表

    protected final String username; //用户名

    public BaseGamePanel(String username) { // 构造函数
        this.username = username;
        setFocusable(true); // 设置面板可以获得键盘焦点
        setBackground(Color.BLACK); // 设置背景颜色

        initUI(); // 初始化界面元素

    }

    public synchronized boolean isMeAlive() {
        for (MultiPlayer player : players){
            if (player.isAlive() && player.getUsername().equals(username)){
                return true;
            }
        }
        return false;
    }



    public synchronized void parseGameObjects(String notifiedGameObjects){ // 解析服务器发来的游戏对象
        final Vector<Enemy> enemies = new Vector<>(); // 敌人列表
        final Vector<Bullet> bullets = new Vector<>(); // 子弹列表
        final Vector<Reward> rewards = new Vector<>(); // 奖励列表
        Vector<MultiPlayer> players = new Vector<>();

        //player,username,x1,y2,score,alive;player,username,x1,y2,score,alive;enemy,x1,y2;reward,x1,y2;bullet:x1,y2;
        String[] gameObjects = notifiedGameObjects.split(";");
        for (String gameObject : gameObjects){
            if (gameObject.startsWith("player,")){
                String[] playerInfo = gameObject.split(",");
                String username = playerInfo[1];
                int x = Integer.parseInt(playerInfo[2]);
                int y = Integer.parseInt(playerInfo[3]);
                int score = Integer.parseInt(playerInfo[4]);
                boolean alive = Boolean.parseBoolean(playerInfo[5]);
                players.add(new MultiPlayer(x,y,username,score));
           }else if (gameObject.startsWith("enemy,")){
                String[] enemyInfo = gameObject.split(",");
                int x = Integer.parseInt(enemyInfo[1]);
                int y = Integer.parseInt(enemyInfo[2]);
                enemies.add(new Enemy(x,y));
            }
            else if (gameObject.startsWith("reward,")){
                String[] rewardInfo = gameObject.split(",");
                int x = Integer.parseInt(rewardInfo[1]);
                int y = Integer.parseInt(rewardInfo[2]);
                rewards.add(new Reward(x,y));
            }else if (gameObject.startsWith("bullet,")){
                String[] bulletInfo = gameObject.split(",");
                int x = Integer.parseInt(bulletInfo[1]);
                int y = Integer.parseInt(bulletInfo[2]);
                bullets.add(new Bullet(x,y));
            }
        }


        this.players.clear();
        this.enemies.clear();
        this.rewards.clear();
        this.bullets.clear();

        this.players.addAll(players);
        this.enemies.addAll(enemies);
        this.rewards.addAll(rewards);
        this.bullets.addAll(bullets);
    }

    /**
     * 初始化界面元素，包括开始游戏按钮。
     */
    private void initUI() {
        setLayout(new BorderLayout());
    }


    /**
     * 绘制游戏界面，包括玩家、敌人、子弹、奖励以及分数显示。
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // 调用父类的绘制方法

        g.setColor(Color.BLACK);
        g.fillRect(0,0,getWidth(),getHeight());

        // 画星空背景
        g.setColor(Color.WHITE);
        for (int i = 0; i < 100; i++) {
            int x = (int) (Math.random() * getWidth());
            int y = (int) (Math.random() * getHeight());
            g.drawLine(x, y, x, y);
        }

        int aliveaccount = 0; // 存活玩家数量
        for (MultiPlayer player : players) {
            if (player.isAlive()) {
                aliveaccount++;
                player.draw(g); // 绘制玩家
            }
        }
        if (aliveaccount == 0) {
            drawGameOverScreen(g); // 绘制游戏结束屏幕
        }else {
            drawGameObjects(g); // 绘制游戏中的所有对象
        }
    }

    /**
     * 绘制游戏结束屏幕，显示游戏结束文字、分数和重新开始提示。
     */
    private void drawGameOverScreen(Graphics g) {
        g.setColor(Color.RED); // 设置游戏结束文字颜色
        g.drawString("Game Over", 350, 300); // 显示游戏结束

        int deltaY = 30;
        for(MultiPlayer player:players) {
            g.drawString("Score: " + player.getScore(), 350, 200+deltaY); // 显示分数
            deltaY+=30;
        }

        g.drawString("Press R to Restart", 350, 340); // 提示重新开始
        g.drawString("Press E to Exit", 350, 360); // 提示可以退出
    }

    /**
     * 绘制游戏中的所有对象，包括敌人、子弹、奖励和分数显示。
     */
    private void drawGameObjects(Graphics g) {
        // 绘制敌人
        for (Enemy enemy : enemies) {
            enemy.draw(g);
        }
        // 绘制子弹
        for (Bullet bullet : bullets) {
            bullet.draw(g);
        }
        // 绘制奖励
        for (Reward reward : rewards) {
            reward.draw(g);
        }
        // 绘制分数
        int deltaX = 30;
        g.setColor(Color.WHITE); // 设置分数文字颜色
        for(MultiPlayer player:players) {
            g.drawString(player.getUsername()+": " + player.getScore(), deltaX, 30); // 显示当前分数
            deltaX+=80;
        }
    }

    /**
     * 处理游戏逻辑，包括玩家移动、敌人移动、奖励移动、碰撞检测和对象清理。
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        repaint(); // 重新绘制面板
    }

    /**
     * 移除死亡的敌人、子弹和奖励。
     */
    private void removeDeadObjects() {
        enemies.removeIf(enemy -> !enemy.isAlive()); // 移除死亡的敌人
        bullets.removeIf(bullet -> !bullet.isAlive()); // 移除消失的子弹
        rewards.removeIf(reward -> !reward.isAlive()); // 移除消失的奖励
    }


    

    /**
     * 重新开始游戏，重置游戏状态并重新启动游戏相关的计时器和定时器。
     */
    private void restartGame() {

    }

    /**
     * 停止游戏，包括停止计时器和敌人生成。
     */
    private void stopGame() {

    }

    /**
     * 启动游戏，包括设置游戏运行标志和重新启动敌人生成计时器。
     */
    public void startGame() {
        requestFocusInWindow(); // 请求焦点，以便接收键盘事件
    }


    /**
     * 退出游戏，关闭窗口，并执行必要的清理工作。
     */
    public void exitGame() {

        // 关闭窗口
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();
            System.exit(0);    // 窗口关闭后退出程序
        }
    }
}
