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
        contentPane.add(roomNumberField, gbc);

        // 按钮面板
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton joinButton = new JButton("加入游戏");
        JButton cancelButton = new JButton("取消");
        buttonPanel.add(joinButton);
        buttonPanel.add(cancelButton);
        contentPane.add(buttonPanel, gbc);

        // 设置内容面板
        setContentPane(contentPane);
        pack();
        setLocationRelativeTo(owner);

        // 加入游戏按钮的事件处理
        joinButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String serverAddress = serverAddressField.getText();
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                String roomNumber = roomNumberField.getText();

                // 检查输入是否为空
                if (serverAddress.isEmpty() || username.isEmpty() || password.isEmpty() || roomNumber.isEmpty()) {
                    JOptionPane.showMessageDialog(MultiplayerDialog.this, "请填写所有字段。", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 连接服务器
                try {
                    String[] addressParts = serverAddress.split(":");
                    String host = addressParts[0];
                    int port = Integer.parseInt(addressParts[1]);
                    socket = new Socket(host, port);
                    out = new PrintWriter(socket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    // 发送加入房间的消息
                    out.println("JOIN;USERNAME;" + username + ";PASSWORD;" + password + ";ROOM;" + roomNumber);

                    // 读取服务器的响应
                    String response = in.readLine();
                    if ("SUCCESS".equals(response)) {
                        // 连接成功，关闭对话框，打开游戏面板
                        dispose();
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                JFrame frame = new JFrame("多人游戏 - " + username);
                                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                                frame.setContentPane(new MultiplayerGamePanel(username, socket, in, out));
                                frame.setSize(800, 600);
                                frame.setVisible(true);
                            }
                        });
                    } else {
                        // 连接失败，显示错误消息
                        JOptionPane.showMessageDialog(MultiplayerDialog.this, "无法加入房间: " + response, "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(MultiplayerDialog.this, "无法连接到服务器。", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 取消按钮的事件处理
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // 关闭对话框
            }
        });
    }
}

