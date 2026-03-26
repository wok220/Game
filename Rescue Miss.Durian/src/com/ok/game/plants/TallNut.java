package com.ok.game.plants;

import com.ok.game.entities.Plant;

import java.awt.*;

/**
 * 高坚果
 * 防御型植物，比普通坚果墙更高，可以阻挡跳跃类僵尸
 */
public class TallNut extends Plant {

    /** 是否阻挡跳跃僵尸（撑杆跳僵尸） */
    private boolean canBlockJump;

    /** 阻挡跳跃的动画计时器 */
    private int blockAnimationTimer;

    /** 阻挡跳跃动画持续时间 */
    private static final int BLOCK_ANIMATION_DURATION = 300;

    /**
     * 构造函数
     * @param row 网格行
     * @param col 网格列
     * @param x 屏幕X坐标
     * @param y 屏幕Y坐标
     */
    public TallNut(int row, int col, int x, int y) {
        super("TallNut", row, col, x, y);

        // 高坚果特殊属性
        this.canBlockJump = true;
        this.blockAnimationTimer = 0;
    }

    @Override
    protected void loadImage() {
        // 从资源管理器加载图片
        // this.image = ResourceManager.getInstance().getPlantImage("TallNut");

        // 临时：如果没有图片资源，使用默认绘制（由父类处理）
    }

    @Override
    protected boolean hasTargetInRange() {
        // 高坚果不是攻击型植物
        return false;
    }

    @Override
    protected void onAttack() {
        // 高坚果不是攻击型植物
    }

    @Override
    protected void updateProduction(float deltaTime) {
        // 高坚果不是生产型植物
    }

    @Override
    protected void onProduce() {
        // 高坚果不是生产型植物
    }

    @Override
    public void update(float deltaTime) {
        // 更新阻挡动画计时器
        if (blockAnimationTimer > 0) {
            blockAnimationTimer -= deltaTime * 1000;
            if (blockAnimationTimer < 0) {
                blockAnimationTimer = 0;
            }
        }

        // 调用父类更新
        super.update(deltaTime);
    }

    // ==================== 特殊能力 ====================

    /**
     * 尝试阻挡跳跃僵尸
     * @return 是否成功阻挡
     */
    public boolean tryBlockJump() {
        if (!canBlockJump) {
            return false;
        }

        // 触发阻挡动画
        blockAnimationTimer = BLOCK_ANIMATION_DURATION;

        // 播放阻挡音效
        // AudioManager.getInstance().playSound("block");

        return true;
    }

    /**
     * 是否可以阻挡跳跃
     */
    public boolean canBlockJump() {
        return canBlockJump;
    }

    // ==================== 渲染 ====================

    @Override
    public void render(Graphics2D g) {
        if (!visible) return;

        // 绘制高坚果
        if (image != null) {
            // 绘制阻挡动画效果（闪烁）
            if (blockAnimationTimer > 0) {
                float alpha = 0.5f + 0.5f * (float) Math.sin(blockAnimationTimer * 0.02f);
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            }
            g.drawImage(image, x, y, width, height, null);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        } else {
            // 默认绘制：更高的坚果墙
            drawTallNut(g);
        }

        // 绘制血条
        if (health < maxHealth && maxHealth > 0) {
            drawHealthBar(g);
        }

        // 绘制阻挡提示（如果有阻挡效果）
        if (canBlockJump && blockAnimationTimer > 0) {
            drawBlockEffect(g);
        }
    }

    /**
     * 绘制高坚果（默认图形）
     */
    private void drawTallNut(Graphics2D g) {
        // 主体（更高）
        g.setColor(new Color(139, 90, 43));  // 棕色
        g.fillRect(x + 15, y + 20, width - 30, height - 30);

        // 纹理
        g.setColor(new Color(101, 67, 33));
        for (int i = 0; i < 4; i++) {
            g.fillOval(x + 20 + i * 15, y + 40, 8, 8);
        }

        // 顶部叶子
        g.setColor(new Color(50, 100, 50));
        g.fillOval(x + 25, y + 10, 15, 12);
        g.fillOval(x + 40, y + 8, 15, 12);

        // 眼睛（更高位置）
        g.setColor(Color.WHITE);
        g.fillOval(x + 25, y + 45, 10, 10);
        g.fillOval(x + 45, y + 45, 10, 10);
        g.setColor(Color.BLACK);
        g.fillOval(x + 28, y + 48, 4, 4);
        g.fillOval(x + 48, y + 48, 4, 4);

        // 嘴巴（严肃表情）
        g.drawArc(x + 32, y + 58, 16, 10, 0, -180);

        // 眉毛（威严）
        g.drawLine(x + 25, y + 42, x + 30, y + 44);
        g.drawLine(x + 45, y + 44, x + 50, y + 42);
    }

    /**
     * 绘制阻挡效果
     */
    private void drawBlockEffect(Graphics2D g) {
        float progress = (float) blockAnimationTimer / BLOCK_ANIMATION_DURATION;
        int shieldSize = (int)(width * (1 + (1 - progress) * 0.5));

        // 绘制护盾效果
        g.setColor(new Color(100, 200, 255, 100));
        g.fillOval(x + (width - shieldSize) / 2,
                y + (height - shieldSize) / 2,
                shieldSize, shieldSize);

        // 绘制冲击波
        for (int i = 0; i < 3; i++) {
            int radius = (int)(shieldSize * (0.5 + i * 0.3) * (1 - progress));
            g.setColor(new Color(255, 255, 100, 80 - i * 20));
            g.drawOval(x + (width - radius) / 2,
                    y + (height - radius) / 2,
                    radius, radius);
        }
    }

    // ==================== Getters ====================

    /**
     * 是否正在播放阻挡动画
     */
    public boolean isBlocking() {
        return blockAnimationTimer > 0;
    }

    /**
     * 获取阻挡动画进度
     */
    public float getBlockProgress() {
        return (float) blockAnimationTimer / BLOCK_ANIMATION_DURATION;
    }
}