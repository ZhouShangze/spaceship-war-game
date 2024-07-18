package com.example.airplane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

/**
 * MultiplayerDialog 类表示一个用于多人游戏设置的对话框。
 * 它允许用户输入服务器地址、用户名、密码和房间号，以加入或创建游戏。
 */
public class MultiplayerDialog extends JDialog {

    private JTextField serverAddressField; // 服务器地址输入框
    private JTextField usernameField; // 用户名输入框
    private JPasswordField passwordField; // 密码输入框
    private JTextField roomNumberField; // 房间号输入框
    private Socket socket; // 套接字
    private PrintWriter out; // 输出流
    private BufferedReader in; // 输入流

    /**
     * 构造函数，初始化多人游戏对话框。
     *
     * @param owner 父窗口
     */
    public MultiplayerDialog(JFrame owner) {
        super(owner, "Multiplayer Mode", true); // 初始化对话框，设置为模态

        // 创建内容面板并设置其布局
        JPanel contentPane = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // 服务器地址标签和输入框
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        contentPane.add(new JLabel("Server Address:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        serverAddressField = new JTextField("127.0.0.1:8888");
        contentPane.add(serverAddressField, gbc);

        // 用户名标签和输入框
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        contentPane.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        usernameField = new JTextField();
        contentPane.add(usernameField, gbc);

        // 密码标签和输入框
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        contentPane.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        passwordField = new JPasswordField();
        contentPane.add(passwordField, gbc);

        // 房间号标签和输入框
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        contentPane.add(new JLabel("Room Number:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        roomNumberField = new JTextField();
        contentPane.add(roomNumberField, gbc);

        // 按钮面板
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        JPanel buttonPanel = new JPanel();
        contentPane.add(buttonPanel, gbc);

        // 创建和加入按钮
        JButton createButton = new JButton("Create");
        JButton joinButton = new JButton("Join");
        buttonPanel.add(createButton);
        buttonPanel.add(joinButton);

        setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE); // 设置对话框模态排斥类型
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // 设置默认关闭操作

        setContentPane(contentPane); // 设置内容面板
        pack(); // 调整窗口大小
        setLocationRelativeTo(owner); // 居中显示

        // 创建按钮的动作监听器
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String serverAddress = serverAddressField.getText();
                String username = usernameField.getText();
                char[] passwordChars = passwordField.getPassword();
                String password = new String(passwordChars);
                String roomNumber = roomNumberField.getText();
                connectToServer(serverAddress, username, password, roomNumber);
            }
        });

        // 加入按钮的动作监听器
        joinButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String serverAddress = serverAddressField.getText();
                String username = usernameField.getText();
                char[] passwordChars = passwordField.getPassword();
                String password = new String(passwordChars);
                String roomNumber = roomNumberField.getText();
                connectToServer(serverAddress, username, password, roomNumber);
            }
        });

        setVisible(true); // 显示对话框
    }

    /**
     * 连接到服务器并发送加入房间的消息。
     *
     * @param serverAddress 服务器地址
     * @param username      用户名
     * @param password      密码
     * @param roomNumber    房间号
     */
    private void connectToServer(String serverAddress, String username, String password, String roomNumber) {
        try {
            // 解析服务器地址和端口号
            String[] addressParts = serverAddress.split(":");
            String host = addressParts[0];
            int port = Integer.parseInt(addressParts[1]);
            socket = new Socket(host, port); // 创建套接字连接
            out = new PrintWriter(socket.getOutputStream(), true); // 初始化输出流
            in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // 初始化输入流

            // 发送加入房间的消息
            out.println("JOIN;" + serverAddress + ";" + username + ";" + password + ";" + roomNumber);

            // 接收服务器响应
            String response = in.readLine();
            if ("SUCCESS".equals(response)) {
                dispose(); // 关闭对话框
                startGameClient(username); // 启动游戏客户端
            } else {
                JOptionPane.showMessageDialog(this, "Failed to join/create room", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 启动游戏客户端。
     *
     * @param username 用户名
     */
    private void startGameClient(String username) {
        JFrame gameFrame = new JFrame("Multiplayer Game");
        MultiplayerGamePanel MgamePanel = new MultiplayerGamePanel(username, socket, in, out);
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setSize(800, 600);
        gameFrame.add(MgamePanel);
        gameFrame.setVisible(true);
        MgamePanel.startGame();
    }
}
