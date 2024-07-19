package com.example.airplane;

import java.util.List;

/**
 * 碰撞处理类，负责检测并处理游戏中对象的碰撞。
 */
public class CollisionHandler {

    public static void handleCollisions(Player player, List<Enemy> enemies, List<Bullet> bullets, ScoreManager scoreManager) {
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
    }
}
