package com.ok.game.entities;

import com.ok.utils.Constants;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * 阳光类
 * 游戏中收集的资源，用于种植植物
 */
public class Sun extends GameObject {

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

    // ==================== 收集动画相关 ====================

    /** 收集动画计时器 */
    protected int collectTimer;

    /** 收集动画持续时间（毫秒） */
    protected int collectDuration;

    /** 收集目标点X坐标（阳光计数器的位置） */
    protected int targetX;

    /** 收集目标点Y坐标（阳光计数器的位置） */
    protected int targetYForCollect;

    /** 起始X坐标（收集动画开始位置） */
    protected int startCollectX;

    /** 起始Y坐标（收集动画开始位置） */
    protected int startCollectY;

    // ==================== 动画效果 ====================

    /** 飘浮动画相位 */
    protected float floatPhase;

    /** 飘浮幅度 */
    protected float floatAmplitude;

    /** 缩放动画（收集时缩小） */
    protected float scale;

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
        this.scale = 1.0f;

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
        // this.image = ResourceManager.getInstance().getSunImage();

        // 临时：如果没有图片，创建默认阳光图片
        createDefaultImage();
    }

    /**
     * 创建默认阳光图片（临时使用）
     */
    protected void createDefaultImage() {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        // 绘制圆形阳光
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 外圈光晕
        g.setColor(new Color(255, 255, 200, 100));
        g.fillOval(2, 2, width - 4, height - 4);

        // 主体
        g.setColor(new Color(255, 215, 0));  // 金色
        g.fillOval(5, 5, width - 10, height - 10);

        // 高光
        g.setColor(new Color(255, 255, 150));
        g.fillOval(10, 10, 8, 8);

        // 光线效果
        g.setColor(new Color(255, 200, 0, 150));
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI * 2 / 8;
            int x1 = width / 2 + (int)(Math.cos(angle) * 15);
            int y1 = height / 2 + (int)(Math.sin(angle) * 15);
            int x2 = width / 2 + (int)(Math.cos(angle) * 25);
            int y2 = height / 2 + (int)(Math.sin(angle) * 25);
            g.drawLine(x1, y1, x2, y2);
        }

        g.dispose();
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
                updateCollecting(deltaTime);
                break;
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

    /**
     * 更新收集动画状态
     */
    protected void updateCollecting(float deltaTime) {
        collectTimer += deltaTime * 1000;

        // 计算插值进度（0-1）
        float progress = (float) collectTimer / collectDuration;

        if (progress >= 1.0f) {
            // 动画完成，阳光消失
            die();
            return;
        }

        // 缓动效果（先快后慢）
        float easedProgress = 1 - (float)Math.pow(1 - progress, 2);

        // 计算当前位置（线性插值）
        x = (int)(startCollectX + (targetX - startCollectX) * easedProgress);
        y = (int)(startCollectY + (targetYForCollect - startCollectY) * easedProgress);

        // 缩放效果（逐渐缩小）
        scale = 1 - easedProgress * 0.5f;
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
        this.startCollectX = this.x;
        this.startCollectY = this.y;
        this.targetX = targetX;
        this.targetYForCollect = targetY;
        this.collectTimer = 0;
        this.collectDuration = Constants.SUN_COLLECT_ANIMATION;

        // 播放收集音效
        // AudioManager.getInstance().playSound("sun_collect");
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

        // 保存原始变换
        Graphics2D g2d = (Graphics2D) g.create();

        // 应用缩放
        if (scale != 1.0f) {
            int centerX = x + width / 2;
            int centerY = drawY + height / 2;
            g2d.translate(centerX, centerY);
            g2d.scale(scale, scale);
            g2d.translate(-centerX, -centerY);
        }

        // 绘制阳光
        g2d.drawImage(image, x, drawY, width, height, null);

        // 绘制光晕效果（收集动画时加强）
        if (state == SunState.COLLECTING) {
            float alpha = 0.5f * (1 - (float)collectTimer / collectDuration);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.setColor(new Color(255, 255, 100));
            g2d.fillOval(x - 10, drawY - 10, width + 20, height + 20);
        }

        g2d.dispose();
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