package com.ok.game.entities;

import com.ok.data.GameConfig;
import com.ok.game.core.GameManager;
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

    /** 当前攻击的目标植物 */
    protected Plant targetPlant;

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
        this.targetPlant = null;

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
     * 僵尸死亡，从游戏中移除
     */
    @Override
    public void die() {
        visible = false;
        alive = false;
        GameManager.getInstance().removeZombie(this);
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
        GameManager.getInstance().onZombieReachHouse();
    }

    /**
     * 受到伤害
     * @param damage 伤害值
     */
    public void takeDamage(int damage) {
        if (state == ZombieState.DYING) return;

        this.health -= damage;

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
     * 获取当前生命值百分比
     */
    public float getHealthPercent() {
        return (float) health / maxHealth;
    }

    // ==================== 渲染 ====================

    @Override
    public void render(Graphics2D g) {
        if (!visible) return;
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

    public boolean isDying() {
        return state == ZombieState.DYING;
    }

    // ==================== Setters ====================

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public abstract Point getPosition();
}