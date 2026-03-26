package com.ok.game.entities;

import com.ok.data.GameConfig;
import com.ok.utils.Constants;

import java.awt.*;

/**
 * 僵尸基类
 * 所有僵尸的父类，定义僵尸的通用属性和行为
 */
public abstract class Zombie extends GameObject {

    // ==================== 状态枚举 ====================

    /**
     * 僵尸状态
     */
    public enum ZombieState {
        WALKING,    // 行走
        ATTACKING,  // 攻击植物
        EATING,     // 啃食植物
        DYING       // 死亡
    }

    // ==================== 基础属性 ====================

    /** 僵尸ID（用于配置查询） */
    protected String zombieId;

    /** 僵尸名称 */
    protected String zombieName;

    /** 当前生命值 */
    protected int health;

    /** 最大生命值 */
    protected int maxHealth;

    /** 攻击伤害 */
    protected int attackDamage;

    /** 攻击间隔（毫秒） */
    protected int attackCooldown;

    /** 移动速度（像素/秒） */
    protected int speed;

    /** 所在行（0-5） */
    protected int row;

    // ==================== 状态相关 ====================

    /** 当前状态 */
    protected ZombieState state;

    /** 攻击计时器（毫秒） */
    protected int attackTimer;

    /** 死亡动画计时器（毫秒） */
    protected int deathTimer;

    /** 被攻击闪烁计时器（毫秒） */
    protected int hurtBlinkTimer;

    /** 是否显示闪烁效果 */
    protected boolean showBlink;

    /** 当前攻击的目标植物 */
    protected Plant targetPlant;

    // ==================== 特殊能力相关 ====================

    /** 是否跳过第一个植物（撑杆跳） */
    protected boolean canJump;

    /** 是否已跳过 */
    protected boolean hasJumped;

    /** 是否飞行单位（气球僵尸） */
    protected boolean isFlying;

    /** 是否从后方出现（矿工僵尸） */
    protected boolean isDigger;

    // ==================== 构造函数 ====================

    /**
     * 构造函数
     * @param zombieId 僵尸ID
     * @param row 所在行
     * @param x X坐标（像素）
     * @param y Y坐标（像素）
     */
    public Zombie(String zombieId, int row, int x, int y) {
        super(x, y, Constants.ZOMBIE_WIDTH, Constants.ZOMBIE_HEIGHT);

        this.zombieId = zombieId;
        this.row = row;

        // 从配置加载属性
        loadConfig();

        // 初始化状态
        this.state = ZombieState.WALKING;
        this.attackTimer = 0;
        this.deathTimer = 0;
        this.hurtBlinkTimer = 0;
        this.showBlink = false;
        this.targetPlant = null;

        // 特殊能力初始化
        this.hasJumped = false;

        // 加载图片
        loadImage();
    }

    /**
     * 从配置加载僵尸属性
     */
    protected void loadConfig() {
        GameConfig config = GameConfig.getInstance();
        GameConfig.ZombieConfig zombieConfig = config.getZombieConfig(zombieId);

        if (zombieConfig != null) {
            this.zombieName = zombieConfig.getName();
            this.maxHealth = zombieConfig.getHealth();
            this.health = maxHealth;
            this.attackDamage = zombieConfig.getAttackDamage();
            this.attackCooldown = zombieConfig.getAttackCooldown();
            this.speed = zombieConfig.getSpeed();
        } else {
            // 默认值
            this.zombieName = zombieId;
            this.maxHealth = 100;
            this.health = maxHealth;
            this.attackDamage = 25;
            this.attackCooldown = Constants.ZOMBIE_ATTACK_INTERVAL;
            this.speed = Constants.ZOMBIE_WALK_SPEED;
        }
    }

    /**
     * 加载僵尸图片
     */
    protected void loadImage() {
        // 由子类实现，或使用ResourceManager
        // this.image = ResourceManager.getInstance().getZombieImage(zombieId);
    }

    // ==================== 更新逻辑 ====================

    @Override
    public void update(float deltaTime) {
        // 更新闪烁效果
        updateBlink(deltaTime);

        // 根据状态更新
        switch (state) {
            case WALKING:
                updateWalking(deltaTime);
                break;
            case ATTACKING:
            case EATING:
                updateAttacking(deltaTime);
                break;
            case DYING:
                updateDying(deltaTime);
                break;
        }
    }

    /**
     * 更新行走状态
     */
    protected void updateWalking(float deltaTime) {
        // 向左移动
        x -= speed * deltaTime;

        // 检查是否到达房子（游戏失败）
        if (x <= 0) {
            onReachHouse();
        }
    }

    /**
     * 更新攻击状态
     */
    protected void updateAttacking(float deltaTime) {
        // 检查目标植物是否还存在
        if (targetPlant == null || !targetPlant.isAlive()) {
            state = ZombieState.WALKING;
            targetPlant = null;
            return;
        }

        // 攻击冷却
        attackTimer += deltaTime * 1000;
        if (attackTimer >= attackCooldown) {
            attackTimer = 0;
            onAttack();
        }
    }

    /**
     * 更新死亡状态
     */
    protected void updateDying(float deltaTime) {
        deathTimer += deltaTime * 1000;
        if (deathTimer >= Constants.ZOMBIE_DEATH_ANIMATION) {
            die();  // 动画结束，真正移除
        }
    }

    /**
     * 更新闪烁效果
     */
    protected void updateBlink(float deltaTime) {
        if (hurtBlinkTimer > 0) {
            hurtBlinkTimer -= deltaTime * 1000;
            showBlink = (hurtBlinkTimer / 100) % 2 == 0;
            if (hurtBlinkTimer <= 0) {
                hurtBlinkTimer = 0;
                showBlink = false;
            }
        }
    }

    // ==================== 行为方法 ====================

    /**
     * 攻击行为
     */
    protected void onAttack() {
        if (targetPlant != null && targetPlant.isAlive()) {
            targetPlant.takeDamage(attackDamage);
        }
    }

    /**
     * 到达房子（游戏失败）
     */
    protected void onReachHouse() {
        // 由 GameManager 处理游戏失败
        // GameManager.getInstance().gameDefeat();
    }

    /**
     * 受到伤害
     * @param damage 伤害值
     */
    public void takeDamage(int damage) {
        if (state == ZombieState.DYING) return;

        this.health -= damage;

        // 触发闪烁效果
        hurtBlinkTimer = 300;

        if (health <= 0) {
            onDeath();
        }
    }

    /**
     * 死亡行为
     */
    protected void onDeath() {
        state = ZombieState.DYING;
        deathTimer = 0;

        // 停止攻击动画
        attackTimer = 0;

        // 播放死亡音效
        // AudioManager.getInstance().playSound("zombie_die");

        // 通知管理器击杀统计
        // GameManager.getInstance().onZombieKilled(this);

        // 有一定几率掉落阳光
        maybeDropSun();
    }

    /**
     * 掉落阳光（子类可重写概率）
     */
    protected void maybeDropSun() {
        // 默认50%概率掉落阳光
        // 由 SunSystem 处理
        // SunSystem.getInstance().createSun(x, y, 25);
    }

    /**
     * 设置攻击目标植物
     * @param plant 目标植物
     */
    public void setTargetPlant(Plant plant) {
        this.targetPlant = plant;
        if (plant != null && state != ZombieState.DYING) {
            this.state = ZombieState.ATTACKING;
            this.attackTimer = 0;
        }
    }

    /**
     * 清除攻击目标
     */
    public void clearTargetPlant() {
        this.targetPlant = null;
        if (state == ZombieState.ATTACKING || state == ZombieState.EATING) {
            this.state = ZombieState.WALKING;
        }
    }

    /**
     * 尝试跳过植物（撑杆跳）
     * @param plant 要跳过的植物
     * @return 是否成功跳过
     */
    public boolean tryJump(Plant plant) {
        if (canJump && !hasJumped) {
            hasJumped = true;
            // 跳过植物，直接移动到植物前方
            // x = plant.getX() + plant.getWidth() + 10;
            return true;
        }
        return false;
    }

    /**
     * 获取当前生命值百分比
     */
    public float getHealthPercent() {
        return (float) health / maxHealth;
    }

    // ==================== 渲染 ====================

    @Override
    public void render(Graphics2D g) {
        if (!visible) return;

        // 闪烁效果（被攻击时闪白）
        if (showBlink) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            g.setColor(Color.WHITE);
            g.fillRect(x, y, width, height);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        // 绘制僵尸图片
        if (image != null) {
            g.drawImage(image, x, y, width, height, null);
        }

        // 绘制血条
        drawHealthBar(g);
    }

    /**
     * 绘制血条
     */
    protected void drawHealthBar(Graphics2D g) {
        if (health >= maxHealth) return;

        int barWidth = width - 10;
        int barHeight = 6;
        int barX = x + 5;
        int barY = y - 10;

        // 背景
        g.setColor(new Color(60, 60, 60));
        g.fillRect(barX, barY, barWidth, barHeight);

        // 当前血量
        int healthWidth = (int) (barWidth * getHealthPercent());
        if (healthWidth > 0) {
            if (getHealthPercent() > 0.6f) {
                g.setColor(Color.GREEN);
            } else if (getHealthPercent() > 0.3f) {
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

    public String getZombieId() {
        return zombieId;
    }

    public String getZombieName() {
        return zombieName;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getAttackDamage() {
        return attackDamage;
    }

    public int getSpeed() {
        return speed;
    }

    public int getRow() {
        return row;
    }

    public ZombieState getState() {
        return state;
    }

    public Plant getTargetPlant() {
        return targetPlant;
    }

    public boolean isFlying() {
        return isFlying;
    }

    public boolean isDigger() {
        return isDigger;
    }

    public boolean isDying() {
        return state == ZombieState.DYING;
    }

    /**
     * 是否可以跳跃
     */
    public boolean canJump() {
        return canJump;
    }

    /**
     * 是否已经跳过
     */
    public boolean hasJumped() {
        return hasJumped;
    }

    // ==================== Setters ====================

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    /**
     * 设置是否可以跳跃
     */
    public void setCanJump(boolean canJump) {
        this.canJump = canJump;
    }

    /**
     * 设置是否已经跳过
     */
    public void setHasJumped(boolean hasJumped) {
        this.hasJumped = hasJumped;
    }

    public void setFlying(boolean flying) {
        isFlying = flying;
    }

    public void setDigger(boolean digger) {
        isDigger = digger;
    }
}