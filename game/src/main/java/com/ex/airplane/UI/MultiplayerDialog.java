package com.ex.airplane.UI;

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

    private JFrame frame;
    /**
     * 构造函数，初始化多人游戏对话框。
     *
     * @param owner 父窗口
     */
    public MultiplayerDialog(JFrame owner) {
        super(owner, "Multiplayer Mode", true); // 初始化对话框，设置为模态

        this.frame = owner;

        // 创建内容面板并设置其布局
        JPanel contentPane = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // 服务器地址标签和输入框
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        contentPane.add(new JLabel("服务器地址:"), gbc);
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
        contentPane.add(new JLabel("用户名:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        usernameField = new JTextField();
        usernameField.setText("user1");
        contentPane.add(usernameField, gbc);

        // 密码标签和输入框
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        contentPane.add(new JLabel("密码:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        passwordField = new JPasswordField();
        passwordField.setText("123456"); //方便调试
        contentPane.add(passwordField, gbc);

        // 房间号标签和输入框
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        contentPane.add(new JLabel("房间号:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        roomNumberField = new JTextField();
        roomNumberField.setText("666");
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
        JButton createButton = new JButton("创建房间");
        JButton joinButton = new JButton("加入房间");
        buttonPanel.add(createButton);
        buttonPanel.add(joinButton);

        setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE); // 设置对话框模态排斥类型
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // 设置默认关闭操作

        setContentPane(contentPane); // 设置内容面板
        pack(); // 调整窗口大小
        setLocationRelativeTo(owner); // 居中显示

        // 创建房间按钮的动作监听器
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateInputs()) {
                    String serverAddress = serverAddressField.getText();
                    String username = usernameField.getText();
                    char[] passwordChars = passwordField.getPassword();
                    String password = new String(passwordChars);
                    String roomNumber = roomNumberField.getText();
                    connectToServer(serverAddress, username, password, roomNumber, true); // 创建房间
                }
            }
        });

        // 加入房间按钮的动作监听器
        joinButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateInputs()) {
                    String serverAddress = serverAddressField.getText();
                    String username = usernameField.getText();
                    char[] passwordChars = passwordField.getPassword();
                    String password = new String(passwordChars);
                    String roomNumber = roomNumberField.getText();
                    connectToServer(serverAddress, username, password, roomNumber, false); // 加入房间
                }
            }
        });
    }

    /**
     * 验证用户输入是否完整。
     *
     * @return 如果输入完整返回 true，否则返回 false。
     */
    private boolean validateInputs() {
        String serverAddress = serverAddressField.getText();
        String username = usernameField.getText();
        char[] passwordChars = passwordField.getPassword();
        String password = new String(passwordChars);
        String roomNumber = roomNumberField.getText();

        if (serverAddress.isEmpty() || username.isEmpty() || password.isEmpty() || roomNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写所有字段", "错误", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String[] addressParts = serverAddress.split(":");
        if (addressParts.length != 2) {
            JOptionPane.showMessageDialog(this, "服务器地址格式错误，应为：hostname:port", "错误", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            Integer.parseInt(addressParts[1]);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "端口号必须是数字", "错误", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    /**
     * 连接到服务器并发送房间请求消息。
     *
     * @param serverAddress 服务器地址
     * @param username      用户名
     * @param password      密码
     * @param roomNumber    房间号
     * @param createRoom    是否创建房间
     */
    private void connectToServer(String serverAddress, String username, String password, String roomNumber, boolean createRoom) {
        try {
            // 解析服务器地址和端口号
            String[] addressParts = serverAddress.split(":");
            String host = addressParts[0];
            int port = Integer.parseInt(addressParts[1]);
            socket = new Socket(host, port); // 创建套接字连接
            out = new PrintWriter(socket.getOutputStream(), true); // 初始化输出流
            in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // 初始化输入流

            // 发送加入或创建房间的消息
            if (createRoom) {
                out.println("CREATE;" + serverAddress + ";" + username + ";" + password + ";" + roomNumber);
            } else {
                out.println("JOIN;" + serverAddress + ";" + username + ";" + password + ";" + roomNumber);
            }

            // 接收服务器响应
            String response = in.readLine();
            if (response.startsWith("ERROR;")) {
                // 处理错误消息
                String errorMessage = response.substring("ERROR;".length());
                JOptionPane.showMessageDialog(this, "错误: " + errorMessage, "错误", JOptionPane.ERROR_MESSAGE);

            } else if ("SUCCESS".equals(response)) {
                dispose(); // 关闭对话框
                startGameClient(username); // 启动游戏客户端
            } else {
                JOptionPane.showMessageDialog(this, "未知的服务器响应: " + response, "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "连接服务器失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 启动游戏客户端并进行进一步的设置。
     *
     * @param username 用户名
     */
    private void startGameClient(String username) {
        dispose();

        // 启动游戏客户端逻辑
        // 例如，可以创建一个新的窗口来显示游戏画面
        MultiplayerGamePanel panel = new MultiplayerGamePanel(username,socket,in,out);
        panel.setVisible(true);


        frame.getContentPane().removeAll(); // 清除之前的内容

        frame.add(panel); // 添加游戏面板

        frame.setSize(800, 600); // 设置游戏界面尺寸
        frame.setVisible(true); // 显示游戏界面

        panel.requestFocusInWindow(); // 确保游戏面板获得键盘焦点
        panel.startGame(); // 启动游戏相关计时器和定时器

    }
}
