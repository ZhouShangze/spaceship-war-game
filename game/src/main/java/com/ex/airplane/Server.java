package com.ex.airplane;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

public class Server {
    private static final int PORT = 8888; // 服务器端口
    private static final String DB_URL = "jdbc:sqlite:game.db"; // 数据库 URL
    private static final ConcurrentHashMap<String, Set<PrintWriter>> rooms = new ConcurrentHashMap<>(); // 房间列表
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
                logger.log(Level.SEVERE, "Database error: " + e.getMessage()); // 记录数据库错误
                e.printStackTrace(); // 打印异常堆栈信息
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Server exception: " + e.getMessage()); // 记录服务器异常日志
            e.printStackTrace(); // 打印异常堆栈信息
        }
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
        }
    }

    /**
     * Handler 类表示一个处理客户端连接的线程。
     */
    private static class Handler implements Runnable {
        private final Socket socket; // 客户端套接字
        private final Connection dbConnection; // 数据库连接
        private PrintWriter out; // 客户端输出流
        private BufferedReader in; // 客户端输入流
        private String room; // 客户端所在的房间

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

                // 根据命令处理不同的请求
                switch (command) {
                    case "CREATE":
                        handleCreateRoom(username, password);
                        break;
                    case "JOIN":
                        handleJoinRoom(username, password);
                        break;
                    default:
                        out.println("ERROR;Unknown command");
                        return;
                }

                // 向房间内的所有客户端广播新用户加入的消息
                broadcastMessage(room, "NEW_USER;" + username);

                out.println("SUCCESS"); // 向客户端发送成功消息

                // 循环读取客户端发送的消息，并广播到房间内
                String input;
                while ((input = in.readLine()) != null) {
                    broadcastMessage(room, input);
                }
            } catch (IOException e) {
                logger.log(Level.WARNING, "IO exception handling client: " + e.getMessage()); // 记录处理客户端时的IO异常
            } finally {
                // 在finally块中确保资源被正确释放
                synchronized (rooms) {
                    if (out != null && room != null) {
                        rooms.get(room).remove(out); // 从房间中移除客户端的输出流
                    }
                }
                try {
                    socket.close(); // 关闭套接字
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Error closing socket: " + e.getMessage()); // 记录关闭套接字时的异常
                }
            }
        }

        /**
         * 处理创建房间的请求。
         *
         * @param username 用户名
         * @param password 密码
         */
        private void handleCreateRoom(String username, String password) {
            try {
                if (validateUser(username, password)) {
                    synchronized (rooms) {
                        if (rooms.containsKey(room)) {
                            out.println("ERROR;Room already exists"); // 如果房间已存在，向客户端发送错误消息
                        } else {
                            rooms.put(room, ConcurrentHashMap.newKeySet()); // 创建新房间
                            rooms.get(room).add(out); // 添加客户端的输出流到房间
                        }
                    }
                } else {
                    out.println("ERROR;Invalid username or password"); // 如果用户名或密码无效，向客户端发送错误消息
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Database error: " + e.getMessage()); // 记录数据库错误
                out.println("ERROR;Database error");
            }
        }

        /**
         * 处理加入房间的请求。
         *
         * @param username 用户名
         * @param password 密码
         */
        private void handleJoinRoom(String username, String password) {
            try {
                if (validateUser(username, password)) {
                    synchronized (rooms) {
                        if (!rooms.containsKey(room)) {
                            out.println("ERROR;Room does not exist"); // 如果房间不存在，向客户端发送错误消息
                        } else {
                            rooms.get(room).add(out); // 将客户端添加到房间
                        }
                    }
                } else {
                    out.println("ERROR;Invalid username or password"); // 如果用户名或密码无效，向客户端发送错误消息
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Database error: " + e.getMessage()); // 记录数据库错误
                out.println("ERROR;Database error");
            }
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

        // 向指定房间内的所有客户端广播消息
        private void broadcastMessage(String room, String message) {
            Set<PrintWriter> writers = rooms.get(room);
            if (writers != null) {
                for (PrintWriter writer : writers) {
                    writer.println(message); // 向每个客户端发送消息
                }
            }
        }
    }
}
