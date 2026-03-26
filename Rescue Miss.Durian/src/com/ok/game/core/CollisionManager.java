package com.ok.game.core;

import com.ok.game.entities.*;
import com.ok.game.grid.GridManager;

import java.awt.*;
import java.util.List;

/**
 * 碰撞检测管理器
 * 负责检测游戏中所有实体之间的碰撞
 */
public class CollisionManager {

    /** 单例实例 */
    private static CollisionManager instance;

    /** 游戏管理器引用 */
    private GameManager gameManager;

    /** 网格管理器引用 */
    private GridManager gridManager;

    /**
     * 私有构造函数
     */
    private CollisionManager() {
    }

    /**
     * 获取单例实例
     */
    public static CollisionManager getInstance() {
        if (instance == null) {
            instance = new CollisionManager();
        }
        return instance;
    }

    /**
     * 初始化
     * @param gameManager 游戏管理器
     */
    public void init(GameManager gameManager) {
        this.gameManager = gameManager;
        this.gridManager = gameManager.getGridManager();
    }

    /**
     * 执行所有碰撞检测
     */
    public void checkCollisions() {
        if (gameManager == null) return;

        // 1. 子弹 vs 僵尸
        checkBulletZombieCollisions();

        // 2. 僵尸 vs 植物
        checkZombiePlantCollisions();

        // 3. 僵尸 vs 小车
        checkZombieCartCollisions();

        // 4. 僵尸 vs 房子（失败判定）
        checkZombieHouseCollisions();
    }

    // ==================== 子弹 vs 僵尸 ====================

    /**
     * 检测子弹与僵尸的碰撞
     */
    private void checkBulletZombieCollisions() {
        List<Bullet> bullets = gameManager.getBullets();
        List<Zombie> zombies = gameManager.getZombies();

        // 遍历子弹（使用索引，避免并发修改）
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            if (!bullet.isAlive()) continue;

            // 只检测同一行的僵尸
            int bulletRow = bullet.getRow();

            for (Zombie zombie : zombies) {
                if (!zombie.isAlive()) continue;
                if (zombie.getRow() != bulletRow) continue;

                // 检测碰撞
                if (bullet.collidesWith(zombie)) {
                    // 子弹命中僵尸
                    boolean shouldRemove = bullet.onHit(zombie);

                    if (shouldRemove) {
                        bullet.die();
                        break; // 子弹已移除，跳出内层循环
                    }
                }
            }
        }
    }

    // ==================== 僵尸 vs 植物 ====================

    /**
     * 检测僵尸与植物的碰撞
     */
    private void checkZombiePlantCollisions() {
        List<Zombie> zombies = gameManager.getZombies();

        for (Zombie zombie : zombies) {
            if (!zombie.isAlive()) continue;
            if (zombie.isDying()) continue;

            int row = zombie.getRow();
            int zombieX = zombie.getX();

            // 获取僵尸前方的植物
            Plant frontPlant = gridManager.getPlantInFront(row, zombieX);

            if (frontPlant != null && frontPlant.isAlive()) {
                // 僵尸遇到植物，开始攻击
                zombie.setTargetPlant(frontPlant);

                // 检查是否需要跳过（撑杆跳僵尸）
                if (zombie.canJump() && !zombie.hasJumped()) {
                    // 尝试跳过植物
                    if (zombie.tryJump(frontPlant)) {
                        // 跳过成功，继续行走
                        zombie.clearTargetPlant();
                        continue;
                    }
                }
            } else {
                // 前方没有植物，清除攻击目标
                if (zombie.getTargetPlant() != null) {
                    zombie.clearTargetPlant();
                }
            }
        }
    }

    // ==================== 僵尸 vs 小车 ====================

    /**
     * 检测僵尸与小车的碰撞
     */
    private void checkZombieCartCollisions() {
        List<Zombie> zombies = gameManager.getZombies();

        for (Zombie zombie : zombies) {
            if (!zombie.isAlive()) continue;
            if (zombie.isDying()) continue;

            int row = zombie.getRow();
            int zombieX = zombie.getX();

            // 检查僵尸是否到达小车触发位置
            if (zombieX <= gridManager.getOffsetX()) {
                // 触发小车
                gameManager.triggerCart(row);
                break; // 一行一次只触发一辆车
            }
        }
    }

    // ==================== 僵尸 vs 房子 ====================

    /**
     * 检测僵尸是否到达房子
     */
    private void checkZombieHouseCollisions() {
        List<Zombie> zombies = gameManager.getZombies();

        for (Zombie zombie : zombies) {
            if (!zombie.isAlive()) continue;

            // 检查僵尸是否到达房子（X坐标 <= 0）
            if (zombie.getX() <= 0) {
                gameManager.onZombieReachHouse();
                return; // 游戏结束，不再继续检测
            }
        }
    }

    // ==================== 额外碰撞检测方法 ====================

    /**
     * 检测指定子弹是否命中指定僵尸（单次检测）
     * @param bullet 子弹
     * @param zombie 僵尸
     * @return 是否命中
     */
    public boolean checkBulletHitZombie(Bullet bullet, Zombie zombie) {
        if (!bullet.isAlive() || !zombie.isAlive()) return false;
        if (bullet.getRow() != zombie.getRow()) return false;

        return bullet.collidesWith(zombie);
    }

    /**
     * 检测指定僵尸是否与指定植物碰撞
     * @param zombie 僵尸
     * @param plant 植物
     * @return 是否碰撞
     */
    public boolean checkZombieHitPlant(Zombie zombie, Plant plant) {
        if (!zombie.isAlive() || !plant.isAlive()) return false;
        if (zombie.getRow() != plant.getRow()) return false;

        // 僵尸在植物的右侧（前方）才可能碰撞
        if (zombie.getX() + zombie.getWidth() >= plant.getX()) {
            return true;
        }
        return false;
    }

    /**
     * 检测两个矩形是否碰撞
     * @param rect1 矩形1
     * @param rect2 矩形2
     * @return 是否碰撞
     */
    public boolean checkRectCollision(Rectangle rect1, Rectangle rect2) {
        return rect1.intersects(rect2);
    }

    /**
     * 检测点是否在矩形内
     * @param point 点
     * @param rect 矩形
     * @return 是否在内部
     */
    public boolean checkPointInRect(Point point, Rectangle rect) {
        return rect.contains(point);
    }

    // ==================== 阳光收集检测 ====================

    /**
     * 检测点击是否收集到阳光
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     * @return 被收集的阳光，没有返回null
     */
    public Sun checkSunCollection(int mouseX, int mouseY) {
        List<Sun> suns = gameManager.getSuns();

        for (Sun sun : suns) {
            if (sun.isAlive() && sun.contains(mouseX, mouseY)) {
                return sun;
            }
        }
        return null;
    }
}