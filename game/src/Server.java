import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

public class Server {
    private static final int PORT = 8888; // 服务器端口号
    private static final ExecutorService executor = Executors.newFixedThreadPool(10); // 线程池，用于处理客户端连接
    private static final Map<String, Set<PrintWriter>> rooms = new ConcurrentHashMap<>(); // 房间与对应客户端输出流的映射
    private static final Logger logger = Logger.getLogger(Server.class.getName()); // 日志记录器

    public static void main(String[] args) {
        logger.info("Server started on port " + PORT); // 记录服务器启动日志
        try (ServerSocket serverSocket = new ServerSocket(PORT)) { // 使用try-with-resources语句自动关闭ServerSocket
            while (true) {
                Socket socket = serverSocket.accept(); // 等待客户端连接
                executor.execute(new Handler(socket)); // 为每个客户端连接创建一个新的Handler线程
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Server exception: " + e.getMessage()); // 记录服务器异常日志
            e.printStackTrace(); // 打印异常堆栈信息
        }
    }

    private static class Handler implements Runnable {
        private final Socket socket; // 客户端套接字
        private PrintWriter out; // 客户端输出流
        private BufferedReader in; // 客户端输入流
        private String room; // 客户端所在的房间

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // 初始化输入流
                out = new PrintWriter(socket.getOutputStream(), true); // 初始化输出流，并开启自动刷新

                // 读取客户端发送的第一条消息，包含用户名和房间信息
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
                this.room = parts[4]; // 房间名

                if ("JOIN".equals(command)) {
                    // 处理加入房间的逻辑
                    handleJoinRoom(username);
                } else if ("START_GAME".equals(command)) {
                    // 处理开始游戏的逻辑
                    handleStartGame();
                } else {
                    out.println("ERROR;Unknown command"); // 如果是未知命令，发送错误消息
                }

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
         * 处理客户端加入房间的逻辑
         *
         * @param username 用户名
         */
        private void handleJoinRoom(String username) {
            synchronized (rooms) {
                rooms.computeIfAbsent(room, k -> ConcurrentHashMap.newKeySet()).add(out);
            }
            // 向房间内的所有客户端广播新用户加入的消息
            broadcastMessage(room, "NEW_USER;" + username);
            out.println("SUCCESS"); // 向客户端发送成功消息
        }

        /**
         * 处理房主开始游戏的逻辑
         */
        private void handleStartGame() {
            // 向房间内的所有客户端广播游戏开始的消息，并进行3秒倒计时
            broadcastMessage(room, "GAME_STARTING");
            try {
                Thread.sleep(3000); // 等待3秒
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 处理中断
            }
            broadcastMessage(room, "GAME_STARTED");
        }

        /**
         * 向指定房间内的所有客户端广播消息
         *
         * @param room    房间名
         * @param message 消息内容
         */
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
