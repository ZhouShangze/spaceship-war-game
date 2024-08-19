package com.ex.airplane.UI;

import com.ex.airplane.AudioPlayer;
import com.ex.airplane.GameObject.Bullet;
import com.ex.airplane.GameObject.Enemy;
import com.ex.airplane.GameObject.Reward;
import com.ex.airplane.ScoreManager;
import com.ex.airplane.CollisionHandler;
import com.ex.airplane.multiplayer.MultiPlayer;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.ArrayList;
import java.io.File;

/**
 * 游戏面板类，负责游戏的主要逻辑和绘制。
 */
public class BaseGamePanel extends JPanel implements ActionListener {
    private JButton exportButton; // 导出最终成绩按钮
    private AudioPlayer backgroundMusic;// 背景音乐
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
        backgroundMusic = new AudioPlayer(getClass().getResource("/BGM.wav"));// 初始化背景音乐
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
        Vector<MultiPlayer> players = new Vector<>(); // 玩家列表

        //player,username,x1,y2,score,alive;player,username,x1,y2,score,alive;enemy,x1,y2;reward,x1,y2;bullet:x1,y2;
        String[] gameObjects = notifiedGameObjects.split(";");   // 分割字符串
        for (String gameObject : gameObjects){   // 遍历游戏对象列表
            if (gameObject.startsWith("player,")){
                String[] playerInfo = gameObject.split(",");
                String username = playerInfo[1];
                int x = Integer.parseInt(playerInfo[2]);
                int y = Integer.parseInt(playerInfo[3]);
                int score = Integer.parseInt(playerInfo[4]);
                boolean alive = Boolean.parseBoolean(playerInfo[5]);

                players.add(new MultiPlayer(x,y,username,score, alive));
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
    protected synchronized void paintComponent(Graphics g) {
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


        if (aliveaccount == 0) {   // 如果没有玩家存活，则显示游戏结束屏幕
            drawGameOverScreen(g); // 绘制游戏结束屏幕
            backgroundMusic.stop(); // 停止背景音乐
            exportButton.setVisible(true);
        }else {
            drawGameObjects(g); // 绘制游戏中的所有对象
        }
    }

    /**
     * 绘制游戏结束屏幕，显示游戏结束文字、分数
     */
    private synchronized void drawGameOverScreen(Graphics g) {  // 绘制游戏结束屏幕
        g.setColor(Color.RED); // 设置游戏结束文字颜色
        g.drawString("Game Over", 300, 200); // 显示游戏结束

        int deltaY = 30;

        //按分数排序
        players.sort((p1,p2)-> p2.getScore() - p1.getScore() );
        int rank = 1;
        //显示表头
        g.drawString("排名    玩家       分数", 300, 250); // 显示分数
        for(MultiPlayer player:players) {
            g.drawString(rank + "         " + player.getUsername()+"     " + player.getScore(), 300, 250+deltaY); // 显示分数

            deltaY+=30; // 调整下一个玩家分数的显示位置
            rank++;
        }

        exportButton = new JButton("导出成绩"); // 导出成绩按钮
        exportButton.setBounds(325, 100, 100, 50); // 设置按钮位置
        exportButton.setVisible(false);
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {  //
                exportScores();
            }
        });
        add(exportButton);

        //暂时不支持Restart.
        //g.drawString("Press R to Restart", 350, 340); // 提示重新开始
        //g.drawString("Press E to Exit", 350, 360); // 提示可以退出
    }

    /**
     * 导出玩家成绩到Excel文件。
     */
    private void exportScores() {
        // 获取所有玩家并按成绩降序排序
        List<MultiPlayer> playersList = new ArrayList<>(players);   // 存储玩家的数组转化为列表
        playersList.sort(Comparator.comparingInt(MultiPlayer::getScore).reversed());

        // 创建Excel工作簿和表格
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("《星际空战》得分表");

        // 创建表头行
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("用户名");
        header.createCell(1).setCellValue("得分");

        // 填充玩家数据
        int rowNum = 1;
        for (MultiPlayer player : playersList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(player.getUsername());
            row.createCell(1).setCellValue(player.getScore());
        }

        // 定义文件路径
        String filePath = "C:/GameScores/scores.xlsx";
        File file = new File(filePath);

        // 创建目录（如果不存在）
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs(); // 创建目录
        }

        // 将工作簿写入文件
        try (FileOutputStream fileOut = new FileOutputStream(file)) {
            workbook.write(fileOut); // 写入文件
        } catch (IOException ex) {
            ex.printStackTrace(); // 异常处理
        } finally {
            try {
                workbook.close(); // 关闭工作簿
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }




    /**
     * 绘制游戏中的所有对象，包括敌人、子弹、奖励和分数显示。
     */
    private synchronized void drawGameObjects(Graphics g) {
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
        g.setColor(Color.RED); // 设置分数文字颜色

        for(MultiPlayer player:players) {
            g.drawString(player.getUsername()+": " + player.getScore(), deltaX, 30); // 显示当前分数
            deltaX+=80;  // 调整下一个玩家分数的显示位置
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
        backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);// 启动背景音乐(循环)
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
