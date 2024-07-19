package com.ex.airplane;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSetup {
    private static final String DB_URL = "jdbc:sqlite:game.db";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            if (conn != null) {
                Statement stmt = conn.createStatement();
                // 创建用户表
                String createTableSQL = "CREATE TABLE IF NOT EXISTS users (" +
                        "username TEXT PRIMARY KEY," +
                        "password TEXT NOT NULL" +
                        ");";
                stmt.execute(createTableSQL);

                // 插入测试数据
                String insertDataSQL = "INSERT INTO users (username, password) VALUES ('user1', 'password1');" +
                        "INSERT INTO users (username, password) VALUES ('user2', 'password2');";
                stmt.execute(insertDataSQL);

                System.out.println("Database setup complete.");
            }
        } catch (SQLException e) {
            System.out.println("Database setup error: " + e.getMessage());
        }
    }
}
