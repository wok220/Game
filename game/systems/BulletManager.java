package com.ok.game.systems;

import com.ok.game.core.GameManager;
import com.ok.game.entities.Bullet;
import com.ok.game.entities.Zombie;

import java.util.ArrayList;
import java.util.List;

/**
 * 子弹管理器
 * 负责管理所有子弹的创建、更新、移除
 */
public class BulletManager {

    /** 单例实例 */
    private static BulletManager instance;

    /** 所有子弹列表 */
    private List<Bullet> bullets;

    /** 待移除的子弹列表 */
    private List<Bullet> bulletsToRemove;

    /** 游戏管理器引用 */
    private GameManager gameManager;

    /**
     * 私有构造函数
     */
    private BulletManager() {
        this.bullets = new ArrayList<>();
        this.bulletsToRemove = new ArrayList<>();
    }

    /**
     * 获取单例实例
     */
    public static BulletManager getInstance() {
        if (instance == null) {
            instance = new BulletManager();
        }
        return instance;
    }

    /**
     * 初始化
     * @param gameManager 游戏管理器
     */
    public void init(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    /**
     * 更新所有子弹
     * @param deltaTime 帧间隔时间（秒）
     */
    public void update(float deltaTime) {
        if (gameManager == null) return;

        // 1. 更新所有子弹位置
        for (Bullet bullet : bullets) {
            bullet.update(deltaTime);

            // 检查子弹是否超出屏幕或死亡
            if (!bullet.isAlive()) {
                markForRemoval(bullet);
            }
        }

        // 2. 处理子弹与僵尸的碰撞
        checkCollisions();

        // 3. 移除待删除的子弹
        removeMarkedBullets();
    }

    /**
     * 检测子弹与僵尸的碰撞
     */
    private void checkCollisions() {
        if (gameManager == null) return;

        List<Zombie> zombies = gameManager.getZombies();

        // 遍历子弹（使用迭代器，因为可能边遍历边移除）
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            if (!bullet.isAlive()) continue;

            int bulletRow = bullet.getRow();
            boolean bulletHit = false;

            // 遍历僵尸，只检测同一行的
            for (Zombie zombie : zombies) {
                if (!zombie.isAlive()) continue;
                if (zombie.getRow() != bulletRow) continue;
                if (zombie.isDying()) continue;

                // 检测碰撞
                if (bullet.collidesWith(zombie)) {
                    // 子弹命中僵尸
                    boolean shouldRemove = bullet.onHit(zombie);

                    if (shouldRemove) {
                        markForRemoval(bullet);
                        bulletHit = true;
                        break; // 子弹已移除，跳出内层循环
                    }
                }
            }
        }
    }

    /**
     * 添加子弹
     * @param bullet 子弹对象
     */
    public void addBullet(Bullet bullet) {
        if (bullet != null) {
            bullets.add(bullet);
        }
    }

    /**
     * 发射子弹（由植物调用）
     * @param x 起始X坐标
     * @param y 起始Y坐标
     * @param row 所在行
     * @param damage 伤害值
     */
    public void shootPea(int x, int y, int row, int damage) {
        Bullet bullet = new Bullet(x, y, row, damage);
        addBullet(bullet);
    }

    /**
     * 发射寒冰子弹
     * @param x 起始X坐标
     * @param y 起始Y坐标
     * @param row 所在行
     * @param damage 伤害值
     */
    public void shootSnowPea(int x, int y, int row, int damage) {
        Bullet bullet = new Bullet(x, y, row, damage, Bullet.BulletType.SNOW);
        addBullet(bullet);
    }

    /**
     * 移除所有子弹
     */
    public void clear() {
        bullets.clear();
        bulletsToRemove.clear();
    }

    /**
     * 标记子弹待移除
     */
    private void markForRemoval(Bullet bullet) {
        if (!bulletsToRemove.contains(bullet)) {
            bulletsToRemove.add(bullet);
        }
    }

    /**
     * 移除标记的子弹
     */
    private void removeMarkedBullets() {
        bullets.removeAll(bulletsToRemove);
        bulletsToRemove.clear();
    }

    /**
     * 获取所有子弹
     */
    public List<Bullet> getBullets() {
        return bullets;
    }

    /**
     * 获取指定行的子弹
     * @param row 行
     * @return 该行的子弹列表
     */
    public List<Bullet> getBulletsInRow(int row) {
        List<Bullet> result = new ArrayList<>();
        for (Bullet bullet : bullets) {
            if (bullet.getRow() == row && bullet.isAlive()) {
                result.add(bullet);
            }
        }
        return result;
    }

    /**
     * 获取当前子弹数量
     */
    public int getBulletCount() {
        return bullets.size();
    }

    /**
     * 移除超出屏幕的子弹
     */
    public void removeOutOfBounds() {
        for (Bullet bullet : bullets) {
            if (!bullet.isAlive()) {
                markForRemoval(bullet);
            }
        }
        removeMarkedBullets();
    }
}