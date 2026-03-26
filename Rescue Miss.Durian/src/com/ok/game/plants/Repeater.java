package com.ok.game.plants;

import com.ok.game.core.GameManager;
import com.ok.game.entities.Plant;
import com.ok.game.entities.Zombie;
import com.ok.game.systems.BulletManager;

import java.awt.*;

/**
 * 双发射手
 * 攻击型植物，每次攻击发射两颗豌豆子弹
 */
public class Repeater extends Plant {

    /** 每次攻击发射的子弹数量 */
    private static final int BULLET_COUNT = 2;

    /** 子弹间隔时间（毫秒） */
    private static final int BULLET_INTERVAL = 100;

    /** 子弹发射计数器 */
    private int bulletCounter;

    /** 子弹发射计时器 */
    private int burstTimer;

    /** 是否正在发射连发 */
    private boolean isBursting;

    /**
     * 构造函数
     * @param row 网格行
     * @param col 网格列
     * @param x 屏幕X坐标
     * @param y 屏幕Y坐标
     */
    public Repeater(int row, int col, int x, int y) {
        super("Repeater", row, col, x, y);

        this.bulletCounter = 0;
        this.burstTimer = 0;
        this.isBursting = false;
    }

    @Override
    protected void loadImage() {
        // 从资源管理器加载图片
        // this.image = ResourceManager.getInstance().getPlantImage("Repeater");

        // 临时：如果没有图片资源，使用默认绘制（由父类处理）
    }

    @Override
    protected boolean hasTargetInRange() {
        // 获取游戏管理器实例
        GameManager gm = GameManager.getInstance();

        // 获取同一行中，植物右侧最近的僵尸
        int plantX = x + width;  // 植物右侧位置
        Zombie closestZombie = gm.getClosestZombieInRow(row, plantX);

        return closestZombie != null;
    }

    @Override
    protected void onAttack() {
        // 开始连发模式
        isBursting = true;
        bulletCounter = 0;
        burstTimer = 0;

        // 发射第一颗子弹
        shootBullet();
        bulletCounter++;
    }

    @Override
    protected void updateAttack(float deltaTime) {
        // 如果正在连发模式
        if (isBursting) {
            burstTimer += deltaTime * 1000;

            // 检查是否需要发射下一颗子弹
            if (burstTimer >= BULLET_INTERVAL && bulletCounter < BULLET_COUNT) {
                burstTimer = 0;
                shootBullet();
                bulletCounter++;
            }

            // 连发完成
            if (bulletCounter >= BULLET_COUNT) {
                isBursting = false;
                bulletCounter = 0;
                burstTimer = 0;
            }
        } else {
            // 正常攻击冷却
            attackTimer += deltaTime * 1000;
            if (attackTimer >= attackCooldown) {
                attackTimer = 0;
                // 检查是否有僵尸在攻击范围内
                if (hasTargetInRange()) {
                    onAttack();
                }
            }
        }
    }

    /**
     * 发射一颗子弹
     */
    private void shootBullet() {
        // 计算子弹起始位置
        int bulletX = x + width;
        int bulletY = y + height / 2;

        // 获取子弹伤害
        int bulletDamage = this.damage;

        // 发射普通豌豆子弹
        BulletManager.getInstance().shootPea(bulletX, bulletY, row, bulletDamage);
    }

    @Override
    protected void updateProduction(float deltaTime) {
        // 双发射手不是生产型植物
    }

    @Override
    protected void onProduce() {
        // 双发射手不是生产型植物
    }

    // ==================== 渲染 ====================

    @Override
    public void render(Graphics2D g) {
        super.render(g);

        // 如果是连发模式，显示攻击特效
        if (isBursting) {
            drawAttackEffect(g);
        }
    }

    /**
     * 绘制攻击特效
     */
    private void drawAttackEffect(Graphics2D g) {
        // 绘制枪口闪光效果
        float progress = (float) burstTimer / BULLET_INTERVAL;
        int flashSize = (int)(15 * (1 - progress));

        g.setColor(new Color(255, 200, 50, 150));
        g.fillOval(x + width - 10, y + height / 2 - flashSize / 2, flashSize, flashSize);

        // 绘制连发轨迹
        g.setColor(new Color(255, 100, 0, 100));
        for (int i = 0; i < BULLET_COUNT; i++) {
            if (i <= bulletCounter) {
                int offsetX = 10 + i * 15;
                g.fillOval(x + width + offsetX, y + height / 2 - 3, 6, 6);
            }
        }
    }

    // ==================== Getters ====================

    /**
     * 是否正在连发中
     */
    public boolean isBursting() {
        return isBursting;
    }

    /**
     * 获取当前连发进度（0-1）
     */
    public float getBurstProgress() {
        return (float) bulletCounter / BULLET_COUNT;
    }
}