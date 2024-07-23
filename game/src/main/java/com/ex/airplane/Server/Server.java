package com.ex.airplane.Server;

import com.ex.airplane.CollisionHandler;
import com.ex.airplane.GameObject.Bullet;
import com.ex.airplane.GameObject.Enemy;
import com.ex.airplane.GameObject.Reward;
import com.ex.airplane.ScoreManager;
import com.ex.airplane.multiplayer.MultiPlayer;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

public class Server implements ActionListener {
    private static final int PORT = 8888; // 服务器端口
    private static final String DB_URL = "jdbc:sqlite:game.db"; // 数据库 URL

    public static final int PANEL_WIDTH = 800; // 面板宽度
    public static final int PANEL_HEIGHT = 600; // 面板高度

    // 在Printwriter里面加player信息
    private static final ConcurrentHashMap<String, Set<PrintWriter>> rooms = new ConcurrentHashMap<>(); // 房间列表
    private static final ExecutorService executor = Executors.newFixedThreadPool(10); // 线程池
    private static final Logger logger = Logger.getLogger(Server.class.getName()); // 日志记录器


    private final ScoreManager scoreManager = new ScoreManager(); // 分数管理器
    private javax.swing.Timer enemySpawnTimer; // 敌人生成计时器
    private Timer rewardSpawnTimer; // 奖励生成计时器

    private static final int ENEMY_SPAWN_INTERVAL  = 5000;  // 敌人生成间隔（毫秒）
    private static final int REWARD_SPAWN_INTERVAL = 10000; // 奖励生成间隔（毫秒）
    private static final int TIMER_DELAY = 20; // 游戏主计时器延迟（毫秒）

    private Timer timer; // 游戏主计时器
    protected final Vector<MultiPlayer> players = new Vector<>(); // 玩家列表
    protected final Vector<Enemy> enemies = new Vector<>(); // 敌人列表
    protected final Vector<Bullet> bullets = new Vector<>(); // 子弹列表
    protected final Vector<Reward> rewards = new Vector<>(); // 奖励列表



    public static void main(String[] args) throws IOException{
        new Server().startServer();
    }

    public void startServer() throws IOException {
        logger.info("Server started on port " + PORT); // 记录服务器启动日志
        try (ServerSocket serverSocket = new ServerSocket(PORT)) { // 使用try-with-resources语句自动关闭ServerSocket
            // 加载数据库驱动
            Class.forName("org.sqlite.JDBC");

            // 初始化数据库连接
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                // 创建数据库表（如果尚不存在）
                createTables(conn);

                while (true) {
                    Socket socket = serverSocket.accept(); // 等待客户端连接
                    executor.execute(new Handler(socket, conn)); // 为每个客户端连接创建一个新的Handler线程
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Database error: " + e.getMessage(), e); // 记录数据库错误
            }



        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Server exception: " + e.getMessage(), e); // 记录服务器异常日志
        }
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isGameOver()) {
            enemies.forEach(Enemy::move); // 移动所有敌人
            bullets.forEach(Bullet::move); // 移动所有子弹
            rewards.forEach(Reward::move); // 移动所有奖励

            for(MultiPlayer player:players) {
                CollisionHandler.handleCollisions(player, enemies, bullets, rewards, scoreManager); // 处理碰撞
            }

            removeDeadObjects(); // 移除死亡的敌人、子弹和奖励
        } else {
            stopGame(); // 游戏结束时停止游戏逻辑
        }

        notifyGameObjects();
    }

    private void notifyGameObjects() {
        StringBuffer sb = new StringBuffer();
        for(MultiPlayer player: players){
            sb.append("player,"+player.getUsername()+","+player.getX()+","+player.getY()+","+player.getScore()+","+player.isAlive()+";");
        }

        for(Enemy enemy:enemies){
            sb.append("enemy,"+enemy.getX()+","+enemy.getY()+";");
        }

        for(Bullet bullet:bullets){
            sb.append("bullet,"+bullet.getX()+","+bullet.getY()+";");
        }

        for(Reward reward:rewards){
            sb.append("reward,"+reward.getX()+","+reward.getY()+";");
        }

        broadcastMessage(sb.toString());
    }

    private void stopGame(){
        started = false;

        if(timer!=null){
            timer.stop(); // 停止游戏主计时器
        }
        stopEnemySpawnTimer(); // 停止敌人生成计时器
        stopRewardSpawnTimer(); // 停止奖励生成计时器

        broadcastMessage("GAMEOVER;");
    }

    /**
     * 移除死亡的敌人、子弹和奖励。
     */
    private void removeDeadObjects() {
        enemies.removeIf(enemy -> !enemy.isAlive()); // 移除死亡的敌人
        bullets.removeIf(bullet -> !bullet.isAlive()); // 移除消失的子弹
        rewards.removeIf(reward -> !reward.isAlive()); // 移除消失的奖励
    }

    private boolean isGameOver(){
        for(MultiPlayer player:players){
            if(player.isAlive()){
                return false;
            }
        }

        return true;
    }


    /**
     * 创建数据库表（如果尚不存在）。
     *
     * @param conn 数据库连接
     * @throws SQLException 如果数据库操作失败
     */
    private static void createTables(Connection conn) throws SQLException {
        String createUsersTableSQL = "CREATE TABLE IF NOT EXISTS users (" +
                "username TEXT PRIMARY KEY," +
                "password TEXT NOT NULL" +
                ");";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTableSQL); // 执行创建表 SQL

            for(int i=1;i<=5;i++) {
                stmt.execute("replace INTO users(username,password) VALUES('user"+i+"', '123456');");
            }
        }
    }


    private boolean started = false;

    /**
     * 初始化游戏设置，包括启动游戏主计时器和敌人生成计时器。
     */
    private void startGame() {
        if(started){
            return;
        }

        started = true;

        timer = new Timer(TIMER_DELAY, this); // 启动游戏主计时器
        timer.start();

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
        if (!isGameOver()) {
            enemies.add(new Enemy((int) (Math.random() * (PANEL_WIDTH - 50)), 0)); // 随机生成敌人位置
        }
    }

    /**
     * 生成奖励，随机位置。
     */
    private void spawnReward() {
        if (!isGameOver()) {
            rewards.add(new Reward((int) (Math.random() * (PANEL_WIDTH - 50)), 0)); // 随机生成奖励位置
        }
    }


    //默认为第一个房间
    public void broadcastMessage(  String message) {
        if(!rooms.isEmpty()) {
            broadcastMessage(rooms.keys().nextElement(),message);
        }
    }

    /**
     * 向指定房间内的所有客户端广播消息
     *
     * @param room 房间名
     * @param message 消息内容
     */
    public void broadcastMessage(String room, String message) {
        System.out.println("broadcastMessage("+rooms.size()+"): "+room+", "+message);
        Set<PrintWriter> writers = rooms.get(room);
        if (writers != null) {
            for (PrintWriter writer : writers) {
                writer.println(message); // 向每个客户端发送消息
            }
        }
    }



    private void keyPressed(MultiPlayer player,int keyEventCode) {
        KeyEvent event = new KeyEvent(new JLabel(),0,0,0,keyEventCode);

        if (player.isAlive()) {
            player.getKeyAdapter(bullets).keyPressed(event); // 处理玩家按键事件
        }
    }

    private void keyReleased(MultiPlayer player, int keyEventCode) {
        if (player.isAlive()) {
            KeyEvent event = new KeyEvent(new JLabel(),0,0,0,keyEventCode);

            player.getKeyAdapter(bullets).keyReleased(event); // 处理玩家按键释放事件
        }
    }


    private MultiPlayer getPlayer(String username) {
        for(MultiPlayer player:players){
            if(player.getUsername().equals(username)){
                return player;
            }
        }

        return null;
    }

    /**
     * Handler 类表示一个处理客户端连接的线程。
     */
    private class Handler implements Runnable {
        private final Socket socket; // 客户端套接字
        private final Connection dbConnection; // 数据库连接
        private PrintWriter out; // 客户端输出流
        private BufferedReader in; // 客户端输入流
        private String room; // 客户端所在的房间

        private String username;

        public Handler(Socket socket, Connection dbConnection) {
            this.socket = socket;
            this.dbConnection = dbConnection;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // 初始化输入流
                out = new PrintWriter(socket.getOutputStream(), true); // 初始化输出流，并开启自动刷新

                // 读取客户端发送的第一条消息，包含操作命令、用户名、房间信息等
                String message = in.readLine();
                if (message == null) {
                    return; // 如果读取为空，则直接返回
                }

                // 解析消息格式
                String[] parts = message.split(";");
                if (parts.length < 5) {
                    out.println("ERROR;Invalid message format"); // 如果消息格式不正确，向客户端发送错误消息
                    return;
                }

                String command = parts[0]; // 消息命令
                String username = parts[2]; // 用户名
                String password = parts[3]; // 密码
                this.room = parts[4]; // 房间名

                this.username = username;

                // 根据命令处理不同的请求
                switch (command) {
                    case "CREATE":
                        handleCreateRoom(username, password); // 处理创建房间请求
                        break;
                    case "JOIN":
                        handleJoinRoom(username, password); // 处理加入房间请求
                        break;
                    default:
                        out.println("ERROR;Unknown command"); // 未知命令
                        return;
                }


                // 循环读取客户端发送的消息，并广播到房间内
                String input;
                while ((input = in.readLine()) != null) {
                    System.out.println(">>> "+username+" :: "+input);

                     MultiPlayer player = getPlayer(username);
                    if(player!=null) {
                        if (input.startsWith("KEY_PRESSED;")) {
                            keyPressed(player, Integer.parseInt(input.split(";")[1]));
                        } else if (input.startsWith("KEY_RELEASED;")) {
                            keyReleased(player, Integer.parseInt(input.split(";")[1]));
                        }
                    }
                }
            } catch (IOException e) {
                logger.log(Level.WARNING, "IO exception handling client: " + e.getMessage(), e); // 记录处理客户端时的IO异常
            } finally {
                closeResources(); // 确保资源被正确释放
            }
        }

        /**
         * 处理创建房间的请求。
         *
         * @param username 用户名
         * @param password 密码
         */
        private boolean handleCreateRoom(String username, String password) {
            try {
                if (validateUser(username, password)) { // 验证用户
                    synchronized (rooms) {
                        if (rooms.containsKey(room)) {
                            out.println("ERROR;Room already exists"); // 如果房间已存在，向客户端发送错误消息
                        } else {
                            rooms.put(room, ConcurrentHashMap.newKeySet()); // 创建新房间
                            rooms.get(room).add(out); // 添加客户端的输出流到房间

                            addPlayer(username);
                            out.println("GAME_STARTED"); //主机进入

                            return true;
                        }
                    }
                } else {
                    out.println("ERROR;Invalid username or password"); // 如果用户名或密码无效，向客户端发送错误消息
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Database error: " + e.getMessage(), e); // 记录数据库错误
                out.println("ERROR;Database error");
            }

            return  false;
        }

        /**
         * 处理加入房间的请求。
         *
         * @param username 用户名
         * @param password 密码
         */
        private boolean handleJoinRoom(String username, String password) {
            try {
                if (validateUser(username, password)) { // 验证用户
                    synchronized (rooms) {
                        if (!rooms.containsKey(room)) {
                            out.println("ERROR;Room does not exist"); // 如果房间不存在，向客户端发送错误消息
                        } else {
                            if(getMultiPlayer(username) != null){
                                out.println("ERROR;Username already exists: "+username);
                                return false;
                            }

                            rooms.get(room).add(out); // 将客户端添加到房间
                            addPlayer(username);

                            out.println("GAME_STARTED"); //从机进入

                            //如果房间加入其它玩家, 则开始游戏
                            if(!started) {
                                startGame();
                            }

                            return true;
                        }
                    }
                } else {
                    out.println("ERROR;Invalid username or password"); // 如果用户名或密码无效，向客户端发送错误消息
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Database error: " + e.getMessage(), e); // 记录数据库错误
                out.println("ERROR;Database error");
            }

            return false;
        }


        private void addPlayer(String username){
            out.println("SUCCESS");
            // 向房间内的所有客户端广播新用户加入的消息
            int x = players.size() * 80;
            int y = 500;
            players.add(new MultiPlayer(x,y,username,0));
            broadcastMessage(room, "NEW_USER;" + username+";"+x+";"+y);
        }

        /**
         * 验证用户的用户名和密码是否有效。
         *
         * @param username 用户名
         * @param password 密码
         * @return 如果验证成功返回 true，否则返回 false
         * @throws SQLException 如果数据库操作失败
         */
        private boolean validateUser(String username, String password) throws SQLException {
            String query = "SELECT password FROM users WHERE username = ?";
            try (PreparedStatement stmt = dbConnection.prepareStatement(query)) {
                stmt.setString(1, username);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("password").equals(password); // 比较密码
                    } else {
                        return false; // 用户名不存在
                    }
                }
            }
        }



        /**
         * 关闭资源，确保连接正确关闭
         */
        private void closeResources() {
            synchronized (rooms) {
                if (out != null && room != null) {
                    Set<PrintWriter> writers = rooms.get(room);
                    if (writers != null) {
                        writers.remove(out);
                        if (writers.isEmpty()) {
                            rooms.remove(room); // 如果房间为空，则删除房间
                        }
                    }
                }

                if(this.username!=null) {
                    MultiPlayer player =  getMultiPlayer(this.username);
                    players.remove(player);
                }
            }
            try {
                socket.close(); // 关闭套接字
            } catch (IOException e) {
                logger.log(Level.WARNING, "Error closing socket: " + e.getMessage(), e); // 记录关闭套接字时的异常
            }finally {
                if(rooms.isEmpty()){
                    players.clear();
                    enemies.clear();
                    bullets.clear();
                    rewards.clear();
                }
            }
        }
    }

    private MultiPlayer getMultiPlayer(String username) {
        for(MultiPlayer player:players){
            if(player.getUsername().equals(username)){
                return player;
            }
        }

        return null;
    }
}
