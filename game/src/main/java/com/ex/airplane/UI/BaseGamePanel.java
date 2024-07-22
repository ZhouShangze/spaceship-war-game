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

/**
 * 游戏面板类，负责游戏的主要逻辑和绘制。
 */
public class BaseGamePanel extends JPanel implements ActionListener {  //供多人游戏面板继承
    public static final int PANEL_WIDTH = 800; // 面板宽度
    public static final int PANEL_HEIGHT = 600; // 面板高度
    private static final int PLAYER_START_X = 400; // 玩家起始x位置
    private static final int PLAYER_START_Y = 500; // 玩家起始y位置
    private static final int ENEMY_SPAWN_INTERVAL = 1000; // 敌人生成间隔（毫秒）
    private static final int REWARD_SPAWN_INTERVAL = 5000; // 奖励生成间隔（毫秒）
    private static final int TIMER_DELAY = 10; // 游戏主计时器延迟（毫秒）

    private final Timer timer = new Timer(TIMER_DELAY, this); // 游戏主计时器

    private final List<Enemy> enemies = new ArrayList<>(); // 敌人列表
    private final List<Bullet> bullets = new ArrayList<>(); // 子弹列表
    private final List<Reward> rewards = new ArrayList<>(); // 奖励列表



    private final List<MultiPlayer> players = new ArrayList<>(); // 玩家列表

    protected final String username; //用户名


    private final ScoreManager scoreManager = new ScoreManager(); // 分数管理器
    private Timer enemySpawnTimer; // 敌人生成计时器
    private Timer rewardSpawnTimer; // 奖励生成计时器

    private boolean gameRunning; // 游戏运行状态标志

    private JButton startButton; // 开始游戏按钮

    public BaseGamePanel(String username) { // 构造函数
        this.username = username;
        setFocusable(true); // 设置面板可以获得键盘焦点
        setBackground(Color.BLACK); // 设置背景颜色
        addKeyListener(new GameKeyAdapter()); // 添加键盘事件监听器
        initUI(); // 初始化界面元素
        initGame(); // 初始化游戏设置
    }

    public boolean isMeAlive() {
        for (MultiPlayer player : players){
            if (player.isAlive() && player.getUsername().equals(username)){
                return true;
            }
        }
        return false;
    }

    public void parseGameObjects(String notifiedGameObjects){ // 解析服务器发来的游戏对象
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
    }

    /**
     * 初始化界面元素，包括开始游戏按钮。
     */
    private void initUI() {
        setLayout(new BorderLayout());

        startButton = new JButton("Start Game");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startGame(); // 点击按钮开始游戏
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * 初始化游戏设置，包括启动游戏主计时器和敌人生成计时器。
     */
    private void initGame() {
        gameRunning = true;
        timer.start(); // 启动游戏主计时器
        startEnemySpawnTimer(); // 启动敌人生成计时器
        startRewardSpawnTimer(); // 启动奖励生成计时器
    }

    /**
     * 启动敌人生成计时器，定期生成敌人。
     */
    private void startEnemySpawnTimer() {
        enemySpawnTimer = new Timer(ENEMY_SPAWN_INTERVAL, e -> spawnEnemy());
        enemySpawnTimer.start();
    }

    /**
     * 停止敌人生成计时器。
     */
    private void stopEnemySpawnTimer() {
        if (enemySpawnTimer != null) {
            enemySpawnTimer.stop();
        }
    }

    /**
     * 启动奖励生成计时器，定期生成奖励。
     */
    private void startRewardSpawnTimer() {
        rewardSpawnTimer = new Timer(REWARD_SPAWN_INTERVAL, e -> spawnReward());
        rewardSpawnTimer.start();
    }

    /**
     * 停止奖励生成计时器。
     */
    private void stopRewardSpawnTimer() {
        if (rewardSpawnTimer != null) {
            rewardSpawnTimer.stop();
        }
    }

    /**
     * 生成敌人，随机位置。
     */
    private void spawnEnemy() {
        if (gameRunning) {
            enemies.add(new Enemy((int) (Math.random() * (PANEL_WIDTH - 50)), 0)); // 随机生成敌人位置
        }
    }

    /**
     * 生成奖励，随机位置。
     */
    private void spawnReward() {
        if (gameRunning) {
            rewards.add(new Reward((int) (Math.random() * (PANEL_WIDTH - 50)), 0)); // 随机生成奖励位置
        }
    }

    /**
     * 绘制游戏界面，包括玩家、敌人、子弹、奖励以及分数显示。
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // 调用父类的绘制方法
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
        g.drawString("Score: " + scoreManager.getScore(), 350, 320); // 显示分数
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
        g.setColor(Color.WHITE); // 设置分数文字颜色
        g.drawString("Score: " + scoreManager.getScore(), 10, 10); // 显示当前分数
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
     * 处理按键事件，包括游戏重启与退出功能。
     */
    private class GameKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (isMeAlive()) {
               //TODO: out key event
                // player.getKeyAdapter(bullets).keyPressed(e); // 处理玩家按键事件(要输出给服务器)out输出
            } else if (e.getKeyCode() == KeyEvent.VK_R) {
                restartGame(); // 处理重新开始游戏
            } else if (e.getKeyCode() == KeyEvent.VK_E) {
                exitGame();    // 处理退出游戏
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (isMeAlive()) {
                //TODO: out key event
                //player.getKeyAdapter(bullets).keyReleased(e); // 处理玩家按键释放事件(要输出给服务器)
            }
        }
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
        startButton.setVisible(false); // 隐藏开始游戏按钮
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
