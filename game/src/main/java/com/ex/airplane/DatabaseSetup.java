package com.ex.airplane;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseSetup {
    // Logger用于记录日志信息
    private static final Logger logger = LoggerFactory.getLogger(DatabaseSetup.class);
    // 数据库连接URL
    private static final String DB_URL = "jdbc:sqlite:game.db";

    public static void main(String[] args) {
        // 尝试建立数据库连接
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            if (conn != null) {
                logger.info("Successfully connected to the database.");

                // 创建Statement用于执行SQL语句
                Statement stmt = conn.createStatement();

                // 创建用户表（如果不存在）
                String createTableSQL = "CREATE TABLE IF NOT EXISTS users (" +
                        "username TEXT PRIMARY KEY," +
                        "password TEXT NOT NULL" +
                        ");";
                stmt.execute(createTableSQL);
                logger.info("Users table created or already exists.");

                // 插入测试数据
                insertUser(conn, "user1", "password1");
                insertUser(conn, "user2", "password2");

                logger.info("Database setup complete.");
            } else {
                logger.error("Failed to make connection to the database.");
            }
        } catch (SQLException e) {
            // 捕获SQL异常并记录错误信息
            logger.error("Database setup error: " + e.getMessage());
        }
    }

    /**
     * 插入用户数据的方法
     *
     * @param conn     数据库连接
     * @param username 用户名
     * @param password 密码
     */
    private static void insertUser(Connection conn, String username, String password) {
        // 检查用户名是否已经存在
        String checkQuery = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setString(1, username);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    // 如果用户名存在，记录警告日志并跳过插入操作
                    logger.warn("Username '{}' already exists. Skipping insertion.", username);
                    return;
                }
            }
        } catch (SQLException e) {
            // 捕获SQL异常并记录错误信息
            logger.error("Error checking username '{}': {}", username, e.getMessage());
        }

        // 插入新用户数据
        String insertQuery = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
            insertStmt.setString(1, username);
            insertStmt.setString(2, password);
            insertStmt.executeUpdate();
            // 记录插入成功的信息
            logger.info("User '{}' inserted successfully.", username);
        } catch (SQLException e) {
            // 捕获SQL异常并记录错误信息
            logger.error("Error inserting user '{}': {}", username, e.getMessage());
        }
    }
}
