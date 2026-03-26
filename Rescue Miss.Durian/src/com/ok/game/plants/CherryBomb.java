package com.ok.game.plants;

import com.ok.game.entities.Plant;
import com.ok.game.entities.Zombie;
import com.ok.game.core.GameManager;
import com.ok.utils.Constants;

import java.awt.*;
import java.util.List;

/**
 * 樱桃炸弹
 * 一次性爆炸植物，放置后立即爆炸，对周围大范围僵尸造成伤害
 */
public class CherryBomb extends Plant {

    /** 爆炸范围半径（格子数） */
    private int explosionRadius;

    /** 爆炸范围（像素） */
    private int explosionRangePx;

    /** 爆炸伤害 */
    private int explosionDamage;

    /** 是否已爆炸 */
    private boolean hasExploded;

    /** 爆炸动画计时器 */
    private int explosionTimer;

    /** 爆炸动画持续时间 */
    private int explosionDuration;

    /**
     * 构造函数
     * @param row 网格行
     * @param col 网格列
     * @param x 屏幕X坐标
     * @param y 屏幕Y坐标
     */
    public CherryBomb(int row, int col, int x, int y) {
        super("CherryBomb", row, col, x, y);

        // 樱桃炸弹特殊属性
        this.explosionRadius = 2;  // 2格范围（上下左右各2格）
        this.explosionRangePx = explosionRadius * Constants.GRID_WIDTH;
        this.explosionDamage = 1800;  // 秒杀所有僵尸
        this.hasExploded = false;
        this.explosionTimer = 0;
        this.explosionDuration = 300;  // 0.3秒爆炸动画
    }

    @Override
    protected void loadImage() {
        // 从资源管理器加载图片
        // this.image = ResourceManager.getInstance().getPlantImage("CherryBomb");

        // 临时：如果没有图片资源，使用默认绘制（由父类处理）
    }

    @Override
    public void update(float deltaTime) {
        // 如果已经爆炸，播放爆炸动画
        if (hasExploded) {
            explosionTimer += deltaTime * 1000;
            if (explosionTimer >= explosionDuration) {
                die();  // 动画结束，植物消失
            }
            return;
        }

        // 樱桃炸弹种植后立即爆炸
        explode();

        // 调用父类更新
        super.update(deltaTime);
    }

    @Override
    protected boolean hasTargetInRange() {
        // 樱桃炸弹不需要检测目标，种植后立即爆炸
        return false;
    }

    @Override
    protected void onAttack() {
        // 樱桃炸弹不需要攻击逻辑
    }

    /**
     * 爆炸
     * 对周围大范围的所有僵尸造成伤害
     */
    private void explode() {
        GameManager gm = GameManager.getInstance();
        List<Zombie> allZombies = gm.getZombies();

        int centerX = x + width / 2;
        int centerY = y + height / 2;

        int zombiesKilled = 0;

        for (Zombie zombie : allZombies) {
            if (!zombie.isAlive() || zombie.isDying()) {
                continue;
            }

            // 计算僵尸中心坐标
            int zombieCenterX = zombie.getX() + zombie.getWidth() / 2;
            int zombieCenterY = zombie.getY() + zombie.getHeight() / 2;

            // 计算距离（欧几里得距离）
            double dx = zombieCenterX - centerX;
            double dy = zombieCenterY - centerY;
            double distance = Math.sqrt(dx * dx + dy * dy);

            // 检查是否在爆炸范围内
            if (distance <= explosionRangePx) {
                zombie.takeDamage(explosionDamage);
                zombiesKilled++;
            }
        }

        // 标记已爆炸，开始播放爆炸动画
        hasExploded = true;
        explosionTimer = 0;

        // 播放爆炸音效
        // AudioManager.getInstance().playSound("cherry_bomb");
    }

    @Override
    protected void updateProduction(float deltaTime) {
        // 樱桃炸弹不是生产型植物
    }

    @Override
    protected void onProduce() {
        // 樱桃炸弹不是生产型植物
    }

    // ==================== 渲染 ====================

    @Override
    public void render(Graphics2D g) {
        if (!visible) return;

        if (hasExploded) {
            // 绘制爆炸效果
            drawExplosion(g);
        } else {
            // 绘制樱桃炸弹
            drawCherryBomb(g);
        }
    }

    /**
     * 绘制樱桃炸弹
     */
    private void drawCherryBomb(Graphics2D g) {
        if (image != null) {
            g.drawImage(image, x, y, width, height, null);
        } else {
            // 默认绘制：两颗樱桃
            // 左樱桃
            g.setColor(new Color(200, 50, 50));
            g.fillOval(x + 10, y + 20, 30, 30);
            // 右樱桃
            g.fillOval(x + 40, y + 20, 30, 30);
            // 叶子
            g.setColor(new Color(50, 150, 50));
            g.fillOval(x + 35, y + 10, 20, 15);
            // 梗
            g.setColor(new Color(100, 80, 40));
            g.drawLine(x + 45, y + 10, x + 45, y + 18);
            // 眼睛
            g.setColor(Color.WHITE);
            g.fillOval(x + 20, y + 28, 6, 6);
            g.fillOval(x + 50, y + 28, 6, 6);
            g.setColor(Color.BLACK);
            g.fillOval(x + 22, y + 30, 3, 3);
            g.fillOval(x + 52, y + 30, 3, 3);
            // 表情
            g.drawArc(x + 35, y + 38, 10, 8, 0, -180);
        }
    }

    /**
     * 绘制爆炸效果
     */
    private void drawExplosion(Graphics2D g) {
        float progress = (float) explosionTimer / explosionDuration;
        int size = (int)(width * (1 + progress * 3));
        int alpha = (int)(255 * (1 - progress));

        // 爆炸闪光
        g.setColor(new Color(255, 200, 50, alpha));
        g.fillOval(x - (size - width) / 2, y - (size - height) / 2, size, size);

        // 爆炸外圈
        g.setColor(new Color(255, 100, 0, alpha / 2));
        g.fillOval(x - size / 2, y - size / 2, size * 2, size * 2);

        // 火星效果
        int particleCount = 12;
        for (int i = 0; i < particleCount; i++) {
            double angle = i * Math.PI * 2 / particleCount + progress * 10;
            int radius = (int)(width * (1 + progress * 2));
            int px = x + width / 2 + (int)(Math.cos(angle) * radius);
            int py = y + height / 2 + (int)(Math.sin(angle) * radius);
            g.setColor(new Color(255, 100 + (int)(progress * 100), 0, alpha));
            g.fillOval(px - 3, py - 3, 6, 6);
        }
    }

    // ==================== Getters ====================

    /**
     * 是否已爆炸
     */
    public boolean hasExploded() {
        return hasExploded;
    }

    /**
     * 获取爆炸动画进度
     */
    public float getExplosionProgress() {
        return (float) explosionTimer / explosionDuration;
    }
}