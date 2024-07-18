package com.example.airplane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 多人游戏对话框类，用于创建或加入多人游戏房间。
 * 通过此对话框，用户可以输入服务器地址、用户名、密码和房间号。
 */
public class MultiplayerDialog extends JDialog {

    // 输入服务器地址的文本字段
    private JTextField serverAddressField;
    // 输入用户名的文本字段
    private JTextField usernameField;
    // 输入密码的密码字段
    private JPasswordField passwordField;
    // 输入房间号的文本字段
    private JTextField roomNumberField;

    /**
     * 构造函数，初始化多人游戏对话框。
     * @param owner 主对话框的拥有者，通常是一个 JFrame。
     */
    public MultiplayerDialog(JFrame owner) {
        // 初始化对话框，设置模态性和标题
        super(owner, "Multiplayer Mode", true); // 模态对话框

        // 创建内容面板并设置其布局为网格布局
        // 创建布局
        JPanel contentPane = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // 添加服务器地址标签和输入字段
        // 服务器地址
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

        // 添加用户名标签和输入字段
        // 用户名
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

        // 添加密码标签和输入字段
        // 密码
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

        // 添加房间号标签和输入字段
        // 房间号
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

        // 创建按钮面板，用于放置“创建”和“加入”按钮
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

        // 创建“创建”和“加入”按钮
        JButton createButton = new JButton("Create");
        JButton joinButton = new JButton("Join");

        // 将按钮添加到按钮面板
        // 添加按钮到面板
        buttonPanel.add(createButton);
        buttonPanel.add(joinButton);

        // 设置对话框的模态排斥类型和关闭操作
        // 设置对话框的默认关闭操作
        setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // 设置内容面板、对话框大小和位置
        // 设置内容面板
        setContentPane(contentPane);
        // 设置对话框大小
        pack();
        setLocationRelativeTo(owner); // 居中显示

        // 为“创建”按钮添加动作监听器，处理创建房间的逻辑
        // 添加按钮监听器
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 获取并处理输入的服务器地址、用户名、密码和房间号
                // 创建房间的逻辑
                String serverAddress = serverAddressField.getText();
                String username = usernameField.getText();
                char[] passwordChars = passwordField.getPassword();
                String password = new String(passwordChars);
                String roomNumber = roomNumberField.getText();
                // 处理这些数据...
                dispose(); // 关闭对话框
            }
        });

        // 为“加入”按钮添加动作监听器，处理加入房间的逻辑
        joinButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 获取并处理输入的服务器地址、用户名、密码和房间号
                // 加入房间的逻辑
                String serverAddress = serverAddressField.getText();
                String username = usernameField.getText();
                char[] passwordChars = passwordField.getPassword();
                String password = new String(passwordChars);
                String roomNumber = roomNumberField.getText();
                // 处理这些数据...
                dispose(); // 关闭对话框
            }
        });

        // 显示对话框
        setVisible(true);
    }
}

