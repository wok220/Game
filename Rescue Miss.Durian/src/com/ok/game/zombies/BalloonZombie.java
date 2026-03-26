package com.ok.game.zombies;

import com.ok.game.entities.Plant;
import com.ok.game.entities.Zombie;
import com.ok.game.systems.SunSystem;
import com.ok.utils.Constants;

import java.awt.*;
import java.util.Random;

/**
 * 气球僵尸
 * 飞行单位，漂浮在空中，只有特定植物（如仙人掌）才能攻击到
 */
public class BalloonZombie extends Zombie {

    /** 随机数生成器（用于掉落阳光） */
    private static Random random = new Random();

    /** 阳光掉落概率（百分比） */
    private static final int SUN_DROP_CHANCE = 30;

    /** 是否还有气球 */
    private boolean hasBalloon;

    /** 气球被击破后的移动速度（变慢） */
    private int groundSpeed;

    /** 原始移动速度（飞行速度） */
    private int flyingSpeed;

    /** 气球浮动相位 */
    private float floatPhase;

    /** 气球浮动幅度 */
    private float floatAmplitude;

    /** 气球高度偏移 */
    private int balloonOffsetY;

    /** 气球被击破计时器（动画） */
    private int balloonPopTimer;

    /** 气球被击破动画持续时间 */
    private static final int POP_DURATION = 300;

    /**
     * 构造函数
     * @param row 所在行
     * @param x 屏幕X坐标
     * @param y 屏幕Y坐标
     */
    public BalloonZombie(int row, int x, int y) {
        super("BalloonZombie", row, x, y);

        this.hasBalloon = true;
        this.isFlying = true;
        this.flyingSpeed = this.speed;
        this.groundSpeed = (int)(this.speed * 0.7f);  // 落地后速度变慢
        this.speed = flyingSpeed;

        this.floatPhase = 0;
        this.floatAmplitude = 5;
        this.balloonOffsetY = -30;
        this.balloonPopTimer = 0;
    }

    @Override
    protected void loadImage() {
        // 从资源管理器加载图片
        // this.image = ResourceManager.getInstance().getZombieImage("BalloonZombie");

        // 临时：如果没有图片资源，使用默认绘制（由父类处理）
    }

    @Override
    public void update(float deltaTime) {
        if (state == ZombieState.DYING) {
            updateDying(deltaTime);
            return;
        }

        // 更新气球击破动画
        if (balloonPopTimer > 0) {
            balloonPopTimer -= deltaTime * 1000;
            if (balloonPopTimer <= 0) {
                balloonPopTimer = 0;
            }
        }

        // 更新浮动效果
        floatPhase += deltaTime * 3;
        if (floatPhase > Math.PI * 2) {
            floatPhase -= Math.PI * 2;
        }

        // 更新闪烁效果
        updateBlink(deltaTime);

        // 更新攻击状态
        if (state == ZombieState.ATTACKING || state == ZombieState.EATING) {
            updateAttacking(deltaTime);
            return;
        }

        // 行走状态
        if (state == ZombieState.WALKING) {
            updateWalking(deltaTime);
        }
    }

    @Override
    protected void updateWalking(float deltaTime) {
        // 向左移动
        x -= speed * deltaTime;

        // 检查是否到达房子
        if (x <= 0) {
            onReachHouse();
        }
    }

    @Override
    public void takeDamage(int damage) {
        if (state == ZombieState.DYING) return;

        // 如果有气球，先打气球
        if (hasBalloon) {
            // 气球被击破
            hasBalloon = false;
            isFlying = false;
            speed = groundSpeed;

            // 触发气球击破效果
            onBalloonPopped();

            // 气球被击中，触发闪烁效果
            hurtBlinkTimer = 300;
            return;
        }

        // 气球已破，扣本体生命值
        super.takeDamage(damage);
    }

    /**
     * 气球被击破时的回调
     */
    protected void onBalloonPopped() {
        // 开始击破动画
        balloonPopTimer = POP_DURATION;

        // 播放气球破裂音效
        // AudioManager.getInstance().playSound("balloon_pop");
    }

    @Override
    public void setTargetPlant(Plant plant) {
        // 气球僵尸在空中时不能攻击植物（需要落地后才能攻击）
        if (hasBalloon) {
            // 在空中时，忽略植物，继续前进
            if (targetPlant != null) {
                clearTargetPlant();
            }
            return;
        }

        // 落地后正常攻击
        super.setTargetPlant(plant);
    }

    @Override
    protected void maybeDropSun() {
        // 30%概率掉落阳光
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

        // 保存原始变换
        Graphics2D g2d = (Graphics2D) g.create();

        // 应用浮动偏移
        float floatOffset = (float)(Math.sin(floatPhase) * floatAmplitude);

        // 绘制僵尸身体（带浮动效果）
        if (image != null) {
            g2d.drawImage(image, x, y + (int)floatOffset, width, height, null);
        } else {
            drawZombieBody(g2d, floatOffset);
        }

        // 绘制气球（如果还在）
        if (hasBalloon) {
            drawBalloon(g2d, floatOffset);
        }

        // 绘制气球击破效果
        if (balloonPopTimer > 0) {
            drawBalloonPopEffect(g2d);
        }

        // 绘制血条
        drawHealthBar(g2d);

        g2d.dispose();
    }

    /**
     * 绘制僵尸身体（默认图形）
     */
    private void drawZombieBody(Graphics2D g, float floatOffset) {
        int drawY = y + (int)floatOffset;

        // 身体
        g.setColor(new Color(80, 100, 60));
        g.fillRect(x + 15, drawY + 30, width - 30, height - 40);

        // 头部
        g.setColor(new Color(100, 120, 70));
        g.fillOval(x + 20, drawY + 10, width - 40, height - 30);

        // 眼睛（抬头看气球）
        g.setColor(Color.WHITE);
        g.fillOval(x + 25, drawY + 22, 8, 8);
        g.fillOval(x + 45, drawY + 22, 8, 8);
        g.setColor(Color.BLACK);
        g.fillOval(x + 27, drawY + 24, 4, 4);
        g.fillOval(x + 47, drawY + 24, 4, 4);

        // 嘴巴（快乐表情）
        g.drawArc(x + 32, drawY + 35, 16, 8, 0, 180);

        // 手臂
        g.setColor(new Color(70, 90, 50));
        g.fillRect(x + 5, drawY + 45, 15, 12);
        g.fillRect(x + width - 20, drawY + 45, 15, 12);
    }

    /**
     * 绘制气球
     */
    private void drawBalloon(Graphics2D g, float floatOffset) {
        int balloonX = x + width / 2 - 15;
        int balloonY = y + (int)floatOffset + balloonOffsetY;

        // 气球主体
        g.setColor(new Color(255, 100, 100));  // 红色
        g.fillOval(balloonX, balloonY, 30, 35);

        // 高光
        g.setColor(new Color(255, 200, 200));
        g.fillOval(balloonX + 8, balloonY + 8, 8, 8);

        // 气球结
        g.setColor(new Color(150, 50, 50));
        g.fillRect(balloonX + 13, balloonY + 32, 4, 8);

        // 绳子
        g.setColor(new Color(100, 70, 40));
        g.setStroke(new BasicStroke(2));
        g.drawLine(balloonX + 15, balloonY + 40, x + width / 2, y + (int)floatOffset + 15);

        // 气球上的表情
        g.setColor(Color.BLACK);
        g.drawArc(balloonX + 10, balloonY + 20, 10, 8, 0, -180);

        // 浮动光晕
        float pulse = 0.5f + 0.5f * (float)Math.sin(floatPhase * 2);
        g.setColor(new Color(255, 200, 100, (int)(50 * pulse)));
        g.fillOval(balloonX - 5, balloonY - 5, 40, 45);
    }

    /**
     * 绘制气球击破效果
     */
    private void drawBalloonPopEffect(Graphics2D g) {
        float progress = (float) balloonPopTimer / POP_DURATION;
        int alpha = (int)(200 * (1 - progress));
        int size = (int)(40 * (1 + (1 - progress) * 2));

        int centerX = x + width / 2;
        int centerY = y + balloonOffsetY - 10;

        // 爆炸碎片
        g.setColor(new Color(255, 100, 100, alpha));
        for (int i = 0; i < 12; i++) {
            double angle = i * Math.PI * 2 / 12 + progress * 10;
            int radius = (int)(size * (0.5f + progress * 0.5f));
            int px = centerX + (int)(Math.cos(angle) * radius);
            int py = centerY + (int)(Math.sin(angle) * radius);
            g.fillOval(px - 3, py - 3, 6, 6);
        }

        // 冲击波
        g.setColor(new Color(255, 200, 100, alpha / 2));
        g.drawOval(centerX - size / 2, centerY - size / 2, size, size);
    }

    @Override
    protected void drawHealthBar(Graphics2D g) {
        // 如果有气球，显示气球血条（实际上是气球本身）
        if (hasBalloon) {
            int barWidth = width - 10;
            int barHeight = 4;
            int barX = x + 5;
            int barY = y - 15;

            g.setColor(new Color(60, 60, 60));
            g.fillRect(barX, barY, barWidth, barHeight);

            // 气球血条（总是满的，或者显示特殊颜色）
            g.setColor(new Color(255, 100, 100));
            g.fillRect(barX, barY, barWidth, barHeight);
            g.setColor(Color.BLACK);
            g.drawRect(barX, barY, barWidth, barHeight);
        } else {
            // 落地后显示正常血条
            super.drawHealthBar(g);
        }
    }

    // ==================== Getters ====================

    /**
     * 是否还有气球
     */
    public boolean hasBalloon() {
        return hasBalloon;
    }

    /**
     * 是否正在飞行
     */
    public boolean isFlying() {
        return isFlying;
    }

    /**
     * 获取气球浮动偏移
     */
    public float getFloatOffset() {
        return (float)(Math.sin(floatPhase) * floatAmplitude);
    }

    /**
     * 获取气球击破动画进度
     */
    public float getPopProgress() {
        return (float) balloonPopTimer / POP_DURATION;
    }
}