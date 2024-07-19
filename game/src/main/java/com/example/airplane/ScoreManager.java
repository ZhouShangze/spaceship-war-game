package com.example.airplane;

/**
 * 分数管理类，负责游戏分数的增加和获取。
 */
public class ScoreManager {
    private int score; // 当前分数

    public void incrementScore() {
        score++; // 增加分数
    }

    public int getScore() {
        return score; // 获取当前分数
    }

    public void resetScore() {
        score = 0; // 重置分数
    }
}
