package com.example.airplane;

import com.example.airplane.GameObject.Bullet;
import com.example.airplane.GameObject.Enemy;
import com.example.airplane.GameObject.Reward;

import java.util.List;

/**
 * 碰撞处理类，负责检测并处理游戏中对象的碰撞。
 */
public class CollisionHandler {

    /**
     * 处理游戏中的所有碰撞。
     * @param player 玩家对象
     * @param enemies 敌人列表
     * @param bullets 子弹列表
     * @param rewards 奖励列表
     * @param scoreManager 分数管理器
     */
    public static void handleCollisions(Player player, List<Enemy> enemies, List<Bullet> bullets, List<Reward> rewards, ScoreManager scoreManager) {
        for (Enemy enemy : enemies) {
            if (player.getBounds().intersects(enemy.getBounds())) {
                player.setAlive(false); // 玩家与敌人碰撞，玩家死亡
            }
            for (Bullet bullet : bullets) {
                if (bullet.getBounds().intersects(enemy.getBounds())) {
                    bullet.setAlive(false); // 子弹与敌人碰撞，子弹消失
                    enemy.setAlive(false); // 敌人死亡
                    scoreManager.incrementScore(); // 增加分数
                }
            }
        }

        // 处理玩家与奖励的碰撞
        for (Reward reward : rewards) {
            if (reward.isColliding(player.getBounds())) {
                rewards.remove(reward); // 移除被碰撞的奖励
                scoreManager.incrementScore(); // 增加分数
                break; // 假设一个奖励只触发一次
            }
        }
    }
}
