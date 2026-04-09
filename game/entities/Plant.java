package com.ok.game.entities;

import com.ok.data.GameConfig;
import com.ok.resource.ResourceManager;
import com.ok.utils.Constants;

import java.awt.*;

/**
 * 植物基类
 * 所有植物的父类，定义植物的通用属性和行为
 */
public abstract class Plant extends GameObject {

    // ==================== 基础属性 ====================

    /** 植物ID（用于配置查询） */
    protected String plantId;

    /** 植物名称 */
    protected String plantName;

    /** 阳光消耗 */
    protected int sunCost;

    /** 当前生命值 */
    protected int health;

    /** 最大生命值 */
    protected int maxHealth;

    /** 所在行（0-5） */
    protected int row;

    /** 所在列（0-9） */
    protected int col;

    // ==================== 攻击相关 ====================

    /** 攻击伤害 */
    protected int damage;

    /** 攻击冷却时间（毫秒） */
    protected int attackCooldown;

    /** 当前攻击冷却计时（毫秒） */
    protected int attackTimer;

    /** 攻击范围（像素），默认全行 */
    protected int attackRange;

    /** 是否正在攻击 */
    protected boolean isAttacking;

    // ==================== 生产相关 ====================

    /** 是否生产型植物（向日葵等） */
    protected boolean isProducer;

    /** 生产间隔（毫秒） */
    protected int produceInterval;

    /** 当前生产计时（毫秒） */
    protected int produceTimer;

    // ==================== 状态相关 ====================

    /** 是否处于冷却中（种植后等待） */
    protected boolean isCooldown;

    /** 冷却计时（毫秒） */
    protected int cooldownTimer;

    // ==================== 图片资源 ====================

    /** 植物图片（用于随鼠标移动） */
    protected Image image;

    /** 种植后图片（GIF，用于在草坪上显示） */
    protected Image plantedImage;

    // ==================== 构造函数 ====================

    /**
     * 构造函数
     * @param plantId 植物ID
     * @param row 所在行
     * @param col 所在列
     * @param x X坐标（像素）
     * @param y Y坐标（像素）
     */
    public Plant(String plantId, int row, int col, int x, int y) {
        super(x, y, Constants.PLANT_WIDTH, Constants.PLANT_HEIGHT);

        this.plantId = plantId;
        this.row = row;
        this.col = col;

        // 从配置加载属性
        loadConfig();

        // 初始化计时器
        this.attackTimer = 0;
        this.produceTimer = 0;
        this.cooldownTimer = 0;
        this.isAttacking = false;
        this.isCooldown = false;

        // 加载图片
        loadImage();
        loadPlantedImage();
    }

    /**
     * 从配置加载植物属性
     */
    protected void loadConfig() {
        GameConfig config = GameConfig.getInstance();
        GameConfig.PlantConfig plantConfig = config.getPlantConfig(plantId);

        if (plantConfig != null) {
            this.plantName = plantConfig.getName();
            this.sunCost = plantConfig.getSunCost();
            this.maxHealth = plantConfig.getHealth();
            this.health = maxHealth;
            this.damage = plantConfig.getDamage();
            this.attackCooldown = plantConfig.getAttackCooldown();
            this.isProducer = plantConfig.isProducer();

            // 生产型植物设置生产间隔
            if (isProducer) {
                this.produceInterval = Constants.SUNFLOWER_PRODUCE_INTERVAL;
            }
        } else {
            // 默认值
            this.plantName = plantId;
            this.sunCost = 100;
            this.maxHealth = Constants.PLANT_DEFAULT_HEALTH;
            this.health = maxHealth;
            this.damage = 0;
            this.attackCooldown = Constants.PLANT_ATTACK_COOLDOWN;
            this.isProducer = false;
        }

        this.attackRange = Constants.PLANT_ATTACK_RANGE;
    }

    /**
     * 加载植物图片（用于随鼠标移动）
     */
    protected abstract void loadImage();

    /**
     * 加载种植后图片（GIF，用于在草坪上显示）
     */
    protected void loadPlantedImage() {
        String imageName = getPlantImageName();
        this.plantedImage = ResourceManager.getInstance().getPlantImage(imageName);
    }

    /**
     * 获取植物图片名称（用于加载种植后GIF）
     * @return 图片名称
     */
    protected abstract String getPlantImageName();

    // ==================== 更新逻辑 ====================

    @Override
    public void update(float deltaTime) {
        // 如果植物死亡，标记移除
        if (health <= 0) {
            die();
            return;
        }

        // 更新种植冷却
        if (isCooldown) {
            cooldownTimer -= deltaTime * 1000;
            if (cooldownTimer <= 0) {
                isCooldown = false;
                cooldownTimer = 0;
            }
        }

        // 生产型植物更新生产计时
        if (isProducer && isAlive()) {
            updateProduction(deltaTime);
        }

        // 攻击型植物更新攻击计时
        if (damage > 0 && isAlive()) {
            updateAttack(deltaTime);
        }
    }

    /**
     * 更新生产逻辑（子类可重写）
     */
    protected void updateProduction(float deltaTime) {
        if (produceInterval > 0) {
            produceTimer += deltaTime * 1000;
            if (produceTimer >= produceInterval) {
                produceTimer = 0;
                onProduce();
            }
        }
    }

    /**
     * 更新攻击逻辑（子类可重写）
     */
    protected void updateAttack(float deltaTime) {
        if (attackCooldown > 0) {
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
    // ==================== 行为方法 ====================

    /**
     * 检查攻击范围内是否有僵尸
     * @return 是否有目标
     */
    protected boolean hasTargetInRange() {
        // 由子类实现或通过GameManager获取同行的僵尸
        // 这里返回false，子类需要重写
        return false;
    }

    /**
     * 攻击行为（子类重写）
     */
    protected void onAttack() {
        // 子类实现，如发射子弹
        isAttacking = true;
    }

    /**
     * 生产行为（子类重写）
     */
    protected void onProduce() {
        // 子类实现，如产生阳光
    }

    /**
     * 受到伤害
     * @param damage 伤害值
     */
    public void takeDamage(int damage) {
        this.health -= damage;

        if (health <= 0) {
            die();
            return;
        }
    }

    // ==================== 渲染 ====================

    @Override
    public void render(Graphics2D g) {
        if (plantedImage != null) {
            g.drawImage(plantedImage, x, y, width, height, null);
        }
    }

    // ==================== Getters ====================

    public String getPlantId() {
        return plantId;
    }

    public Image getImage() {
        return image;
    }

    public Image getPlantedImage() {
        return plantedImage;
    }

    public int getSunCost() {
        return sunCost;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getDamage() {
        return damage;
    }

    public boolean isProducer() {
        return isProducer;
    }

    public boolean isAttacker() {
        return damage > 0;
    }

    public boolean isOnCooldown() {
        return isCooldown;
    }

    // ==================== Setters ====================

    /**
     * 设置所在行
     * @param row 网格行（0-5）
     */
    public void setRow(int row) {
        this.row = row;
    }

    /**
     * 设置所在列
     * @param col 网格列（0-9）
     */
    public void setCol(int col) {
        this.col = col;
    }

    /**
     * 设置种植冷却
     * @param cooldownMs 冷却时间（毫秒）
     */
    public void startCooldown(int cooldownMs) {
        this.isCooldown = true;
        this.cooldownTimer = cooldownMs;
    }

    public abstract Point getPosition();
}