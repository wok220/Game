package com.ok.game.zombies;

import com.ok.game.entities.Plant;
import com.ok.game.entities.Zombie;
import com.ok.game.plants.TallNut;
import com.ok.game.systems.SunSystem;
import com.ok.utils.Constants;

import java.awt.*;
import java.util.Random;

/**
 * 撑杆跳僵尸
 * 可以跳过遇到的第一个植物，跳跃后速度变慢
 */
public class PoleVaultingZombie extends Zombie {

    /** 随机数生成器（用于掉落阳光） */
    private static Random random = new Random();

    /** 阳光掉落概率（百分比） */
    private static final int SUN_DROP_CHANCE = 30;

    /** 是否已经跳过 */
    private boolean hasJumped;

    /** 是否正在跳跃 */
    private boolean isJumping;

    /** 跳跃动画计时器 */
    private int jumpTimer;

    /** 跳跃动画持续时间（毫秒） */
    private static final int JUMP_DURATION = 500;

    /** 跳跃起始X坐标 */
    private int jumpStartX;

    /** 跳跃目标X坐标 */
    private int jumpTargetX;

    /** 跳跃起始Y坐标 */
    private int jumpStartY;

    /** 跳跃高度 */
    private int jumpHeight;

    /** 撑杆是否还在（跳跃后撑杆消失） */
    private boolean hasPole;

    /** 跳跃后的移动速度（变慢） */
    private int slowedSpeed;

    /** 原始移动速度 */
    private int originalSpeed;

    /**
     * 构造函数
     * @param row 所在行
     * @param x 屏幕X坐标
     * @param y 屏幕Y坐标
     */
    public PoleVaultingZombie(int row, int x, int y) {
        super("PoleVaultingZombie", row, x, y);

        this.hasJumped = false;
        this.isJumping = false;
        this.hasPole = true;
        this.jumpTimer = 0;
        this.jumpHeight = 60;

        // 设置跳跃后速度（原始速度的60%）
        this.originalSpeed = this.speed;
        this.slowedSpeed = (int)(originalSpeed * 0.6f);
    }

    @Override
    protected void loadImage() {
        // 从资源管理器加载图片
        // this.image = ResourceManager.getInstance().getZombieImage("PoleVaultingZombie");

        // 临时：如果没有图片资源，使用默认绘制（由父类处理）
    }

    @Override
    public void update(float deltaTime) {
        if (state == ZombieState.DYING) {
            updateDying(deltaTime);
            return;
        }

        // 更新闪烁效果
        updateBlink(deltaTime);

        // 更新跳跃动画
        if (isJumping) {
            updateJumping(deltaTime);
            return;
        }

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
        // 使用当前速度移动
        x -= getCurrentSpeed() * deltaTime;

        // 检查是否到达房子
        if (x <= 0) {
            onReachHouse();
        }
    }

    /**
     * 更新跳跃动画
     */
    private void updateJumping(float deltaTime) {
        jumpTimer += deltaTime * 1000;
        float progress = (float) jumpTimer / JUMP_DURATION;

        if (progress >= 1.0f) {
            // 跳跃完成
            isJumping = false;
            hasJumped = true;
            hasPole = false;
            x = jumpTargetX;
            y = jumpStartY;

            // 跳跃后速度变慢
            speed = slowedSpeed;

            // 继续行走
            state = ZombieState.WALKING;
            return;
        }

        // 抛物线运动
        float t = progress;
        x = (int)(jumpStartX + (jumpTargetX - jumpStartX) * t);

        // 抛物线高度: y = 起始Y + 高度 * sin(π * t)
        float sinValue = (float)Math.sin(Math.PI * t);
        y = (int)(jumpStartY - jumpHeight * sinValue);
    }

    @Override
    public void setTargetPlant(Plant plant) {
        // 如果还没跳过，尝试跳过植物
        if (!hasJumped && !isJumping && plant != null) {
            // 检查植物是否可以被跳过
            if (canJumpOver(plant)) {
                startJump(plant);
                return;
            }
        }

        // 不能跳过或已经跳过，正常攻击
        super.setTargetPlant(plant);
    }

    /**
     * 检查是否可以跳过该植物
     */
    private boolean canJumpOver(Plant plant) {
        // 高坚果不能跳过
        if (plant instanceof TallNut) {
            return false;
        }
        return true;
    }

    /**
     * 开始跳跃
     */
    private void startJump(Plant plant) {
        isJumping = true;
        hasJumped = true;
        jumpTimer = 0;

        // 计算跳跃起始和目标位置
        jumpStartX = x;
        jumpStartY = y;

        // 跳到植物前方
        jumpTargetX = plant.getX() + plant.getWidth() + 20;

        // 清除攻击目标
        targetPlant = null;
        state = ZombieState.WALKING;

        // 播放跳跃音效
        // AudioManager.getInstance().playSound("jump");
    }

    /**
     * 获取当前移动速度（考虑减速效果）
     */
    private int getCurrentSpeed() {
        // 如果有减速效果，使用减速速度
        // 这里可以扩展减速效果系统
        return speed;
    }

    @Override
    public boolean tryJump(Plant plant) {
        if (!hasJumped && !isJumping && canJumpOver(plant)) {
            startJump(plant);
            return true;
        }
        return false;
    }

    @Override
    public boolean canJump() {
        return !hasJumped && !isJumping;
    }

    @Override
    public boolean hasJumped() {
        return hasJumped;
    }

    @Override
    public void setHasJumped(boolean hasJumped) {
        this.hasJumped = hasJumped;
        if (hasJumped) {
            hasPole = false;
            speed = slowedSpeed;
        }
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

        // 绘制僵尸身体
        if (image != null) {
            g.drawImage(image, x, y, width, height, null);
        } else {
            drawZombieBody(g);
        }

        // 绘制撑杆（如果还在）
        if (hasPole && !isJumping) {
            drawPole(g);
        }

        // 绘制跳跃轨迹（跳跃时）
        if (isJumping) {
            drawJumpTrail(g);
        }

        // 绘制血条
        drawHealthBar(g);
    }

    /**
     * 绘制僵尸身体（默认图形）
     */
    private void drawZombieBody(Graphics2D g) {
        // 身体
        g.setColor(new Color(80, 100, 60));
        g.fillRect(x + 15, y + 30, width - 30, height - 40);

        // 头部
        g.setColor(new Color(100, 120, 70));
        g.fillOval(x + 20, y + 10, width - 40, height - 30);

        // 眼睛
        g.setColor(Color.WHITE);
        g.fillOval(x + 25, y + 25, 8, 8);
        g.fillOval(x + 45, y + 25, 8, 8);
        g.setColor(Color.BLACK);
        g.fillOval(x + 27, y + 27, 4, 4);
        g.fillOval(x + 47, y + 27, 4, 4);

        // 嘴巴（自信表情）
        g.drawArc(x + 32, y + 38, 16, 8, 0, 180);

        // 手臂（持杆姿势）
        g.setColor(new Color(70, 90, 50));
        if (hasPole) {
            // 持杆姿势
            g.fillRect(x + 5, y + 45, 20, 10);
            g.fillRect(x + width - 25, y + 45, 20, 10);
        } else {
            // 普通姿势
            g.fillRect(x + 5, y + 45, 15, 12);
            g.fillRect(x + width - 20, y + 45, 15, 12);
        }
    }

    /**
     * 绘制撑杆
     */
    private void drawPole(Graphics2D g) {
        g.setColor(new Color(139, 69, 19));  // 棕色
        g.setStroke(new BasicStroke(4));
        g.drawLine(x + 55, y + 25, x + 65, y + 55);
        g.drawLine(x + 60, y + 40, x + 70, y + 65);

        g.setColor(new Color(101, 67, 33));
        g.fillOval(x + 62, y + 48, 8, 8);
    }

    /**
     * 绘制跳跃轨迹
     */
    private void drawJumpTrail(Graphics2D g) {
        float progress = (float) jumpTimer / JUMP_DURATION;
        int alpha = (int)(150 * (1 - progress));

        // 绘制残影效果
        g.setColor(new Color(255, 255, 200, alpha));
        for (int i = 0; i < 5; i++) {
            float t = Math.max(0, progress - i * 0.05f);
            if (t > 0) {
                float sinValue = (float)Math.sin(Math.PI * t);
                int trailX = (int)(jumpStartX + (jumpTargetX - jumpStartX) * t);
                int trailY = (int)(jumpStartY - jumpHeight * sinValue);
                g.fillOval(trailX - 10, trailY - 10, 20, 20);
            }
        }

        // 绘制弧线
        g.setColor(new Color(255, 200, 100, 100));
        for (float t = 0; t <= 1; t += 0.05f) {
            float sinValue = (float)Math.sin(Math.PI * t);
            int trailX = (int)(jumpStartX + (jumpTargetX - jumpStartX) * t);
            int trailY = (int)(jumpStartY - jumpHeight * sinValue);
            g.fillOval(trailX - 3, trailY - 3, 6, 6);
        }
    }

    @Override
    protected void drawHealthBar(Graphics2D g) {
        int barWidth = width - 10;
        int barHeight = 6;
        int barX = x + 5;
        int barY = y - 10;

        float percent = (float) health / maxHealth;

        g.setColor(new Color(60, 60, 60));
        g.fillRect(barX, barY, barWidth, barHeight);

        int healthWidth = (int) (barWidth * percent);
        if (healthWidth > 0) {
            if (percent > 0.6f) {
                g.setColor(Color.GREEN);
            } else if (percent > 0.3f) {
                g.setColor(Color.YELLOW);
            } else {
                g.setColor(Color.RED);
            }
            g.fillRect(barX, barY, healthWidth, barHeight);
        }

        g.setColor(Color.BLACK);
        g.drawRect(barX, barY, barWidth, barHeight);
    }

    // ==================== Getters ====================

    /**
     * 是否正在跳跃
     */
    public boolean isJumping() {
        return isJumping;
    }

    /**
     * 是否还有撑杆
     */
    public boolean hasPole() {
        return hasPole;
    }

    /**
     * 获取跳跃进度
     */
    public float getJumpProgress() {
        return (float) jumpTimer / JUMP_DURATION;
    }
}