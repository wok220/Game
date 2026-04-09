package com.ok.game.entities;

import com.ok.resource.ResourceManager;
import com.ok.utils.Constants;

import java.awt.*;

/**
 * 阳光类
 * 游戏中收集的资源，用于种植植物
 */
public class Sun extends GameObject {
    @Override
    public Point getPosition() {
        return new Point(x, y);
    }

// ==================== 阳光状态枚举 ====================

    /**
     * 阳光状态
     */
    public enum SunState {
        FALLING,    // 正在掉落
        FLOATING,   // 停留在地面附近飘浮
        COLLECTING  // 正在被收集（飞向计数器）
    }

    // ==================== 基础属性 ====================

    /** 阳光价值（默认25） */
    protected int value;

    /** 当前状态 */
    protected SunState state;

    /** 存在时间（毫秒），超时后消失 */
    protected int lifeTimer;

    /** 最大存在时间（毫秒） */
    protected int maxLifeTime;

    // ==================== 掉落相关 ====================

    /** 掉落速度（像素/秒） */
    protected float fallSpeed;

    /** 起始掉落Y坐标 */
    protected int startY;

    /** 目标Y坐标（掉落停止位置） */
    protected int targetY;

    // ==================== 动画效果 ====================

    /** 飘浮动画相位 */
    protected float floatPhase;

    /** 飘浮幅度 */
    protected float floatAmplitude;

    // ==================== 构造函数 ====================

    /**
     * 构造函数（天上掉落）
     * @param x X坐标
     * @param y 起始Y坐标
     * @param targetY 掉落目标Y坐标
     */
    public Sun(int x, int y, int targetY) {
        this(x, y, targetY, Constants.SUN_PRODUCE_AMOUNT);
    }

    /**
     * 构造函数（天上掉落，指定价值）
     * @param x X坐标
     * @param y 起始Y坐标
     * @param targetY 掉落目标Y坐标
     * @param value 阳光价值
     */
    public Sun(int x, int y, int targetY, int value) {
        super(x, y, 40, 40);  // 阳光尺寸40x40

        this.value = value;
        this.state = SunState.FALLING;
        this.startY = y;
        this.targetY = targetY;
        this.fallSpeed = Constants.SUN_FALL_SPEED;
        this.maxLifeTime = Constants.SUN_LIFESPAN;
        this.lifeTimer = 0;

        // 动画效果初始化
        this.floatPhase = 0;
        this.floatAmplitude = 3;

        // 加载阳光图片
        loadImage();

        // 设置初始位置
        this.y = y;
    }

    /**
     * 构造函数（向日葵生产）
     * @param x X坐标（植物位置）
     * @param y Y坐标（植物位置）
     */
    public Sun(int x, int y) {
        this(x, y - 30, y - 50, Constants.SUN_PRODUCE_AMOUNT);
        // 向日葵生产的阳光从植物上方出现并向上飘一段再掉落
    }

    /**
     * 加载阳光图片
     */
    protected void loadImage() {
        this.image = ResourceManager.getInstance().getSunImage();
    }

    // ==================== 更新逻辑 ====================

    @Override
    public void update(float deltaTime) {
        // 更新存在时间
        lifeTimer += deltaTime * 1000;
        if (lifeTimer >= maxLifeTime) {
            die();
            return;
        }

        // 根据状态更新
        switch (state) {
            case FALLING:
                updateFalling(deltaTime);
                break;
            case FLOATING:
                updateFloating(deltaTime);
                break;
            case COLLECTING:
                // 直接消失，不需要动画
                die();
                return;
        }

        // 更新飘浮动画相位
        floatPhase += deltaTime * 3;
        if (floatPhase > Math.PI * 2) {
            floatPhase -= Math.PI * 2;
        }
    }

    /**
     * 更新掉落状态
     */
    protected void updateFalling(float deltaTime) {
        // 向下移动
        y += fallSpeed * deltaTime;

        // 到达目标位置，转为飘浮状态
        if (y >= targetY) {
            y = targetY;
            state = SunState.FLOATING;
        }
    }

    /**
     * 更新飘浮状态
     */
    protected void updateFloating(float deltaTime) {
        // 飘浮效果（上下轻微移动）
        float offset = (float)(Math.sin(floatPhase) * floatAmplitude);
        // 注意：实际y坐标在渲染时应用偏移，不修改实际y值
        // 这里只做逻辑更新，渲染时计算偏移
    }

    // ==================== 收集逻辑 ====================

    /**
     * 收集阳光
     * @param targetX 目标X坐标（阳光计数器位置）
     * @param targetY 目标Y坐标（阳光计数器位置）
     */
    public void collect(int targetX, int targetY) {
        if (state == SunState.COLLECTING) return;

        this.state = SunState.COLLECTING;
        // 直接消失，不需要动画
    }

    /**
     * 检查点是否在阳光范围内
     * @param px 点X坐标
     * @param py 点Y坐标
     * @return 是否在范围内
     */
    @Override
    public boolean contains(int px, int py) {
        // 扩大点击范围，便于收集
        int expandedSize = 10;
        return px >= x - expandedSize && px <= x + width + expandedSize &&
                py >= y - expandedSize && py <= y + height + expandedSize;
    }

    // ==================== 渲染 ====================

    @Override
    public void render(Graphics2D g) {
        if (!visible || image == null) return;

        // 计算飘浮偏移
        int drawY = y;
        if (state == SunState.FLOATING) {
            float offset = (float)(Math.sin(floatPhase) * floatAmplitude);
            drawY = y + (int)offset;
        }

        // 绘制阳光
        g.drawImage(image, x, drawY, width, height, null);
    }

    // ==================== Getters ====================

    public int getValue() {
        return value;
    }

    public SunState getState() {
        return state;
    }

    public boolean isCollecting() {
        return state == SunState.COLLECTING;
    }

    public float getLifePercent() {
        return 1 - (float) lifeTimer / maxLifeTime;
    }
}