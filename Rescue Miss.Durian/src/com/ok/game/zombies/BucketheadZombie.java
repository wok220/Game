package com.ok.game.zombies;

import com.ok.game.entities.Zombie;
import com.ok.game.systems.SunSystem;
import com.ok.utils.Constants;

import java.awt.*;
import java.util.Random;

/**
 * 铁桶僵尸
 * 路障僵尸的强化版，头上戴着铁桶，生命值更高
 */
public class BucketheadZombie extends Zombie {

    /** 随机数生成器（用于掉落阳光） */
    private static Random random = new Random();

    /** 阳光掉落概率（百分比） */
    private static final int SUN_DROP_CHANCE = 35;

    /** 铁桶是否还在（未被打掉） */
    private boolean hasBucket;

    /** 铁桶生命值 */
    private int bucketHealth;

    /** 铁桶最大生命值 */
    private static final int BUCKET_MAX_HEALTH = 200;

    /**
     * 构造函数
     * @param row 所在行
     * @param x 屏幕X坐标
     * @param y 屏幕Y坐标
     */
    public BucketheadZombie(int row, int x, int y) {
        super("BucketheadZombie", row, x, y);

        this.hasBucket = true;
        this.bucketHealth = BUCKET_MAX_HEALTH;
    }

    @Override
    protected void loadImage() {
        // 从资源管理器加载图片
        // this.image = ResourceManager.getInstance().getZombieImage("BucketheadZombie");

        // 临时：如果没有图片资源，使用默认绘制（由父类处理）
    }

    @Override
    public void takeDamage(int damage) {
        if (state == ZombieState.DYING) return;

        // 先扣铁桶生命值
        if (hasBucket && bucketHealth > 0) {
            bucketHealth -= damage;
            if (bucketHealth <= 0) {
                hasBucket = false;
                bucketHealth = 0;
                onBucketDestroyed();
            }
            // 铁桶被击中，触发闪烁效果
            hurtBlinkTimer = 300;
            return;
        }

        // 铁桶已损坏，扣本体生命值
        super.takeDamage(damage);
    }

    /**
     * 铁桶被破坏时的回调
     */
    protected void onBucketDestroyed() {
        // 播放铁桶破碎音效
        // AudioManager.getInstance().playSound("bucket_break");

        // 可以添加视觉效果
        // 例如：铁桶破碎的粒子效果
    }

    @Override
    protected void maybeDropSun() {
        // 35%概率掉落阳光
        if (random.nextInt(100) < SUN_DROP_CHANCE) {
            SunSystem sunSystem = SunSystem.getInstance();
            int sunX = x + width / 2;
            int sunY = y + height / 2;
            sunSystem.createDropSun(sunX, sunY, Constants.SUN_PRODUCE_AMOUNT);
        }
    }

    @Override
    protected void onDeath() {
        super.onDeath();
        // AudioManager.getInstance().playSound("zombie_die");
    }

    // ==================== 渲染 ====================

    @Override
    public void render(Graphics2D g) {
        if (!visible) return;

        // 绘制僵尸身体
        if (image != null) {
            g.drawImage(image, x, y, width, height, null);
        } else {
            // 默认绘制
            drawZombieBody(g);
        }

        // 绘制铁桶（如果还在）
        if (hasBucket) {
            drawBucket(g);
        }

        // 绘制血条
        drawHealthBar(g);
    }

    /**
     * 绘制僵尸身体（默认图形）
     */
    private void drawZombieBody(Graphics2D g) {
        // 身体
        g.setColor(new Color(70, 90, 50));
        g.fillRect(x + 15, y + 30, width - 30, height - 40);

        // 头部
        g.setColor(new Color(90, 110, 60));
        g.fillOval(x + 20, y + 10, width - 40, height - 30);

        // 眼睛
        g.setColor(Color.WHITE);
        g.fillOval(x + 25, y + 25, 8, 8);
        g.fillOval(x + 45, y + 25, 8, 8);
        g.setColor(Color.BLACK);
        g.fillOval(x + 27, y + 27, 4, 4);
        g.fillOval(x + 47, y + 27, 4, 4);

        // 嘴巴
        g.drawArc(x + 32, y + 38, 16, 10, 0, -180);

        // 手臂
        g.setColor(new Color(60, 80, 40));
        g.fillRect(x + 5, y + 45, 15, 12);
        g.fillRect(x + width - 20, y + 45, 15, 12);
    }

    /**
     * 绘制铁桶
     */
    private void drawBucket(Graphics2D g) {
        // 根据剩余血量设置颜色
        float healthPercent = (float) bucketHealth / BUCKET_MAX_HEALTH;
        Color bucketColor;
        if (healthPercent > 0.6f) {
            bucketColor = new Color(150, 150, 160);  // 银灰色
        } else if (healthPercent > 0.3f) {
            bucketColor = new Color(120, 120, 130);  // 深灰色
        } else {
            bucketColor = new Color(90, 90, 100);    // 铁锈色
        }

        // 绘制铁桶（圆柱形）
        int bucketWidth = width - 20;
        int bucketHeight = 35;
        int bucketX = x + 10;
        int bucketY = y + 5;

        // 桶身
        g.setColor(bucketColor);
        g.fillRoundRect(bucketX, bucketY, bucketWidth, bucketHeight, 15, 15);

        // 桶顶边缘
        g.setColor(new Color(100, 100, 110));
        g.fillRoundRect(bucketX - 3, bucketY - 2, bucketWidth + 6, 8, 8, 8);

        // 桶把手
        g.setColor(new Color(80, 80, 90));
        g.drawArc(bucketX + bucketWidth / 2 - 15, bucketY - 8, 30, 15, 0, 180);

        // 桶身纹理（划痕效果）
        g.setColor(new Color(70, 70, 80));
        g.drawLine(bucketX + 10, bucketY + 12, bucketX + bucketWidth - 10, bucketY + 12);
        g.drawLine(bucketX + 15, bucketY + 20, bucketX + bucketWidth - 15, bucketY + 20);
        g.drawLine(bucketX + 12, bucketY + 28, bucketX + bucketWidth - 12, bucketY + 28);

        // 高光效果
        g.setColor(new Color(200, 200, 210, 100));
        g.fillRect(bucketX + 5, bucketY + 3, 8, bucketHeight - 10);

        // 如果血量较低，添加锈迹
        if (healthPercent < 0.5f) {
            g.setColor(new Color(139, 69, 19, 80));
            g.fillOval(bucketX + 15, bucketY + 15, 12, 12);
            g.fillOval(bucketX + bucketWidth - 25, bucketY + 22, 10, 10);
        }
    }

    @Override
    protected void drawHealthBar(Graphics2D g) {
        // 计算总生命值百分比（铁桶 + 本体）
        int totalMaxHealth = maxHealth + BUCKET_MAX_HEALTH;
        int totalCurrentHealth = health + (hasBucket ? bucketHealth : 0);
        float totalPercent = (float) totalCurrentHealth / totalMaxHealth;

        int barWidth = width - 10;
        int barHeight = 6;
        int barX = x + 5;
        int barY = y - 10;

        // 背景
        g.setColor(new Color(60, 60, 60));
        g.fillRect(barX, barY, barWidth, barHeight);

        // 当前总血量
        int healthWidth = (int) (barWidth * totalPercent);
        if (healthWidth > 0) {
            if (totalPercent > 0.6f) {
                g.setColor(Color.GREEN);
            } else if (totalPercent > 0.3f) {
                g.setColor(Color.YELLOW);
            } else {
                g.setColor(Color.RED);
            }
            g.fillRect(barX, barY, healthWidth, barHeight);
        }

        // 边框
        g.setColor(Color.BLACK);
        g.drawRect(barX, barY, barWidth, barHeight);
    }

    // ==================== Getters ====================

    /**
     * 是否还有铁桶
     */
    public boolean hasBucket() {
        return hasBucket;
    }

    /**
     * 获取铁桶剩余生命值
     */
    public int getBucketHealth() {
        return bucketHealth;
    }

    /**
     * 获取铁桶血量百分比
     */
    public float getBucketHealthPercent() {
        return (float) bucketHealth / BUCKET_MAX_HEALTH;
    }
}