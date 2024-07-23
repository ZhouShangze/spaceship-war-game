package com.ex.airplane;

import com.ex.airplane.GameObject.Bullet;
import com.ex.airplane.GameObject.Enemy;
import com.ex.airplane.GameObject.Reward;
import com.ex.airplane.multiplayer.MultiPlayer;

import java.util.List;

/**
 * 碰撞处理类，负责检测并处理游戏中对象的碰撞。
 */



public class CollisionHandler {
    private static AudioPlayer explosionMusic; // 爆炸音乐
    static {
        explosionMusic = new AudioPlayer(CollisionHandler.class.getResource("/explosion.wav"));
    }
    /**
     * 处理游戏中的所有碰撞。
     * @param player 玩家对象
     * @param enemies 敌人列表
     * @param bullets 子弹列表
     * @param rewards 奖励列表
     * @param scoreManager 分数管理器
     */

    // 处理单机玩家与敌人、子弹、奖励的碰撞
    public static void handleCollisions(com.ex.airplane.Player player, List<Enemy> enemies, List<Bullet> bullets, List<Reward> rewards, ScoreManager scoreManager) {

        for (Enemy enemy : enemies) {
            if (player.getBounds().intersects(enemy.getBounds())) {
                player.setAlive(false); // 玩家与敌人碰撞，玩家死亡
                explosionMusic.play();

            }
            for (Bullet bullet : bullets) {
                if (bullet.getBounds().intersects(enemy.getBounds())) {
                    bullet.setAlive(false); // 子弹与敌人碰撞，子弹消失
                    enemy.setAlive(false); // 敌人死亡
                    explosionMusic.play();
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


    // 处理多个玩家与敌人、子弹、奖励的碰撞
    public static void handleCollisions(MultiPlayer player, List<Enemy> enemies, List<Bullet> bullets, List<Reward> rewards, ScoreManager scoreManager) {
        for (Enemy enemy : enemies) {
            if (player.getBounds().intersects(enemy.getBounds())) {
                player.setAlive(false); // 玩家与敌人碰撞，玩家死亡
                explosionMusic.play();
            }

            for (Bullet bullet : bullets) {
                if (bullet.getBounds().intersects(enemy.getBounds())) {
                    bullet.setAlive(false); // 子弹与敌人碰撞，子弹消失
                    enemy.setAlive(false); // 敌人死亡
                    explosionMusic.play();
                    //子弹对应的玩家加分
                    if(player.getUsername().equals(bullet.getUsername())) {
                        player.setScore(player.getScore() + 1);// 增加分数
                    }
                }
            }
        }

        // 处理玩家与奖励的碰撞
        for (Reward reward : rewards) {
            if (reward.isColliding(player.getBounds())) {
                rewards.remove(reward); // 移除被碰撞的奖励
                player.setScore(player.getScore() + 1); // 增加分数
                break; // 假设一个奖励只触发一次
            }
        }
    }
}

