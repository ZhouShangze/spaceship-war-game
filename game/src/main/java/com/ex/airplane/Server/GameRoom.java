package com.ex.airplane.Server;

import com.ex.airplane.GameObject.Bullet;
import com.ex.airplane.GameObject.Enemy;
import com.ex.airplane.GameObject.Reward;
import com.ex.airplane.multiplayer.MultiPlayer;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * 游戏房间类，负责管理房间中的玩家、游戏对象和游戏逻辑。
 */
public class GameRoom {
    private static final Logger logger = Logger.getLogger(GameRoom.class.getName());
    private final String roomName; // 房间名称
    private final List<MultiPlayer> players = new CopyOnWriteArrayList<>(); // 玩家列表
    private final List<Enemy> enemies = new ArrayList<>(); // 敌人列表
    private final List<Bullet> bullets = new ArrayList<>(); // 子弹列表
    private final List<Reward> rewards = new ArrayList<>(); // 奖励列表
    private boolean gameStarted = false; // 游戏是否已开始
    private final Timer gameTimer = new Timer(); // 定时器用于定期更新游戏状态

    public GameRoom(String roomName) {
        this.roomName = roomName;
    }

    public void addPlayer(MultiPlayer player) {
        players.add(player);
        logger.info("Player " + player.getUsername() + " added to room " + roomName);
    }

    public void removePlayer(MultiPlayer player) {
        players.remove(player);
        logger.info("Player " + player.getUsername() + " removed from room " + roomName);
    }

    public boolean isEmpty() {
        return players.isEmpty(); // 判断房间是否为空
    }

    public void startGame() {
        if (gameStarted) return;
        gameStarted = true;

        // 初始化游戏状态
        initializeGameObjects();

        // 启动游戏循环定时器
        gameTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateGame(); // 更新游戏状态
                broadcastGameState(); // 广播游戏状态
            }
        }, 0, 100); // 每100毫秒更新一次
    }

    private void initializeGameObjects() {
        // 初始化敌人、奖励等游戏对象
        for (int i = 0; i < 5; i++) {
            enemies.add(new Enemy(new Random().nextInt(800), -50)); // 随机位置
        }
        rewards.add(new Reward(new Random().nextInt(800), -20)); // 随机位置
    }

    private void updateGame() {
        for (MultiPlayer player : players) {
            player.move(); // 移动玩家
        }
        for (Enemy enemy : enemies) {
            enemy.move(); // 移动敌人
        }
        for (Bullet bullet : bullets) {
            bullet.move(); // 移动子弹
        }
        for (Reward reward : rewards) {
            reward.move(); // 移动奖励
        }

        // 处理碰撞
        CollisionHandler.handleCollisions(players.get(0), enemies, bullets, rewards, new ScoreManager());
    }

    private void broadcastGameState() {  // 广播游戏状态a
        StringBuilder state = new StringBuilder(); // 构建游戏状态字符串
        for (MultiPlayer player : players) {   // 遍历玩家列表
            state.append("PLAYER;").append(player.getUsername()).append(";").append(player.getX()).append(";").append(player.getY()).append("\n");
        }
        for (Enemy enemy : enemies) {   // 遍历敌人列表
            state.append("ENEMY;").append(enemy.getX()).append(";").append(enemy.getY()).append("\n");
        }
        for (Bullet bullet : bullets) {   // 遍历子弹列表
            state.append("BULLET;").append(bullet.getX()).append(";").append(bullet.getY()).append("\n");
        }
        for (Reward reward : rewards) {   // 遍历奖励列表
            state.append("REWARD;").append(reward.getX()).append(";").append(reward.getY()).append("\n");
        }

        // 向所有玩家广播游戏状态
        for (MultiPlayer player : players) {
            sendToPlayer(player, state.toString()); // 发送游戏状态消息
        }
    }

    private void sendToPlayer(MultiPlayer player, String message) {
        Socket playerSocket = player.getSocket(); // 获取玩家的Socket对象
        if (playerSocket != null && !playerSocket.isClosed()) {
            try (PrintWriter out = new PrintWriter(playerSocket.getOutputStream(), true)) {
                out.println(message); // 发送游戏状态消息
            } catch (IOException e) {
                logger.warning("Error sending message to player " + player.getUsername() + ": " + e.getMessage());
            }
        }
    }
}
