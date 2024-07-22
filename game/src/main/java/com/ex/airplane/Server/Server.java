package com.ex.airplane.Server;

import com.ex.airplane.GameObject.Bullet;
import com.ex.airplane.GameObject.Enemy;
import com.ex.airplane.GameObject.Reward;
import com.ex.airplane.CollisionHandler;
import com.ex.airplane.ScoreManager;
import com.ex.airplane.multiplayer.MultiPlayer;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

public class Server {
    private static final int PORT = 8888; // 服务器端口
    private static final String DB_URL = "jdbc:sqlite:game.db"; // 数据库 URL
    private static final ConcurrentHashMap<String, GameRoom> rooms = new ConcurrentHashMap<>(); // 房间列表
    private static final ExecutorService executor = Executors.newFixedThreadPool(10); // 线程池
    private static final Logger logger = Logger.getLogger(Server.class.getName()); // 日志记录器

    public static void main(String[] args) {
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

    private static void createTables(Connection conn) throws SQLException {
        String createUsersTableSQL = "CREATE TABLE IF NOT EXISTS users (" +
                "username TEXT PRIMARY KEY," +
                "password TEXT NOT NULL" +
                ");";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTableSQL); // 执行创建表 SQL
        }
    }

    private static class Handler implements Runnable {
        private final Socket socket; // 客户端套接字
        private final Connection dbConnection; // 数据库连接
        private PrintWriter out; // 客户端输出流
        private BufferedReader in; // 客户端输入流
        private String room; // 客户端所在的房间
        private MultiPlayer player; // 当前客户端玩家对象

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

                out.println("SUCCESS"); // 向客户端发送成功消息

                // 向房间内的所有客户端广播新用户加入的消息
                broadcastMessage(room, "NEW_USER;" + username);

                // 将玩家对象添加到房间，并更新房间状态
                GameRoom gameRoom = rooms.get(room);
                if (gameRoom != null) {
                    player = new MultiPlayer(generateRandomX(), generateRandomY(), username, 0);
                    gameRoom.addPlayer(player);
                }

                // 循环读取客户端发送的消息，并广播到房间内
                String input;
                while ((input = in.readLine()) != null) {
                    broadcastMessage(room, input);
                }
            } catch (IOException e) {
                logger.log(Level.WARNING, "IO exception handling client: " + e.getMessage(), e); // 记录处理客户端时的IO异常
            } finally {
                closeResources(); // 确保资源被正确释放
            }
        }

        private void handleCreateRoom(String username, String password) {
            try {
                if (validateUser(username, password)) { // 验证用户
                    synchronized (rooms) {
                        if (rooms.containsKey(room)) {
                            out.println("ERROR;Room already exists"); // 如果房间已存在，向客户端发送错误消息
                        } else {
                            GameRoom newRoom = new GameRoom(room);
                            rooms.put(room, newRoom); // 创建新房间
                            newRoom.startGame(); // 开始游戏
                            out.println("SUCCESS;Room created and game started"); // 通知客户端房间创建成功并开始游戏
                        }
                    }
                } else {
                    out.println("ERROR;Invalid username or password"); // 如果用户名或密码无效，向客户端发送错误消息
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Database error: " + e.getMessage(), e); // 记录数据库错误
                out.println("ERROR;Database error");
            }
        }

        private void handleJoinRoom(String username, String password) {
            try {
                if (validateUser(username, password)) { // 验证用户
                    synchronized (rooms) {
                        GameRoom gameRoom = rooms.get(room);
                        if (gameRoom == null) {
                            out.println("ERROR;Room does not exist"); // 如果房间不存在，向客户端发送错误消息
                        } else {
                            player = new MultiPlayer(generateRandomX(), generateRandomY(), username, 0);
                            gameRoom.addPlayer(player); // 将客户端添加到房间
                        }
                    }
                } else {
                    out.println("ERROR;Invalid username or password"); // 如果用户名或密码无效，向客户端发送错误消息
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Database error: " + e.getMessage(), e); // 记录数据库错误
                out.println("ERROR;Database error");
            }
        }

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

        private void broadcastMessage(String room, String message) {
            GameRoom gameRoom = rooms.get(room);
            if (gameRoom != null) {
                gameRoom.broadcast(message); // 向房间内的所有客户端广播消息
            }
        }

        private void closeResources() {
            synchronized (rooms) {
                if (player != null && room != null) {
                    GameRoom gameRoom = rooms.get(room);
                    if (gameRoom != null) {
                        gameRoom.removePlayer(player);
                        if (gameRoom.isEmpty()) {
                            rooms.remove(room); // 如果房间为空，则删除房间
                        }
                    }
                }
            }
            try {
                socket.close(); // 关闭套接字
            } catch (IOException e) {
                logger.log(Level.WARNING, "Error closing socket: " + e.getMessage(), e); // 记录关闭套接字时的异常
            }
        }

        private int generateRandomX() {
            return new Random().nextInt(800); // 随机生成 x 坐标（假设屏幕宽度为 800）
        }

        private int generateRandomY() {
            return new Random().nextInt(600); // 随机生成 y 坐标（假设屏幕高度为 600）
        }
    }
}
