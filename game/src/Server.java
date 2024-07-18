
import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 8888; // 服务器端口号
    private static Map<String, List<PrintWriter>> rooms = new HashMap<>(); // 房间列表，每个房间存储一个输出流列表

    public static void main(String[] args) throws IOException {
        System.out.println("Server started...");
        ServerSocket serverSocket = new ServerSocket(PORT); // 创建服务器套接字

        while (true) {
            new Handler(serverSocket.accept()).start(); // 接受客户端连接并启动新的处理线程
        }
    }

    private static class Handler extends Thread {
        private Socket socket; // 客户端套接字
        private PrintWriter out; // 输出流
        private BufferedReader in; // 输入流
        private String room; // 房间号

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // 初始化输入流
                out = new PrintWriter(socket.getOutputStream(), true); // 初始化输出流

                // 读取客户端发送的消息
                String message = in.readLine();
                String[] parts = message.split(";"); // 分割消息字符串
                String command = parts[0];
                String serverAddress = parts[1];
                String username = parts[2];
                String password = parts[3];
                room = parts[4];

                synchronized (rooms) {
                    // 如果房间不存在则创建
                    if (!rooms.containsKey(room)) {
                        rooms.put(room, new ArrayList<>());
                    }
                    rooms.get(room).add(out); // 将输出流添加到房间的输出流列表中
                }

                // 向房间中的所有用户发送新用户加入的消息
                for (PrintWriter writer : rooms.get(room)) {
                    writer.println("NEW_USER;" + username);
                }

                out.println("SUCCESS"); // 向客户端发送成功消息

                String input;
                // 持续监听客户端发送的消息
                while ((input = in.readLine()) != null) {
                    // 将接收到的消息广播给房间中的所有用户
                    for (PrintWriter writer : rooms.get(room)) {
                        writer.println(input);
                    }
                }

            } catch (IOException e) {
                System.out.println(e.getMessage());
            } finally {
                // 客户端断开连接后，移除输出流并关闭套接字
                if (out != null && room != null) {
                    synchronized (rooms) {
                        rooms.get(room).remove(out);
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
}
