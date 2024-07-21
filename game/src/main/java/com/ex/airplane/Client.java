package com.ex.airplane;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.logging.*;

public class Client {
    private static final Logger logger = Logger.getLogger(Client.class.getName());
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }

    public void start() {
        try {
            connectToServer("localhost", 8888); // 连接到服务器
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("Enter command (CREATE/JOIN), username, password, room:");
                String input = scanner.nextLine();
                sendMessage(input); // 发送消息到服务器
                String response = in.readLine(); // 读取服务器响应
                if (response != null) {
                    handleServerResponse(response); // 处理服务器响应
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error connecting to server: " + e.getMessage(), e); // 记录连接服务器时的错误
            System.out.println("Failed to connect to server: " + e.getMessage());
        } finally {
            closeResources(); // 关闭资源
        }
    }

    /**
     * 连接到服务器
     *
     * @param serverAddress 服务器地址
     * @param port 端口号
     * @throws IOException 如果连接失败
     */
    private void connectToServer(String serverAddress, int port) throws IOException {
        socket = new Socket(serverAddress, port);
        out = new PrintWriter(socket.getOutputStream(), true); // 初始化输出流，并开启自动刷新
        in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // 初始化输入流
    }

    /**
     * 发送消息到服务器
     *
     * @param message 要发送的消息
     */
    private void sendMessage(String message) {
        out.println(message);
    }

    /**
     * 处理服务器响应
     *
     * @param response 服务器的响应消息
     */
    private void handleServerResponse(String response) {
        if (response.startsWith("ERROR")) {
            System.out.println("Server error: " + response); // 如果服务器返回错误，输出错误信息
        } else {
            System.out.println("Server response: " + response); // 输出服务器响应
        }
    }

    /**
     * 关闭资源，确保连接正确关闭
     */
    private void closeResources() {
        try {
            if (socket != null) {
                socket.close(); // 关闭套接字
            }
            if (out != null) {
                out.close(); // 关闭输出流
            }
            if (in != null) {
                in.close(); // 关闭输入流
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error closing resources: " + e.getMessage(), e); // 记录关闭资源时的异常
        }
    }
}
