package com.ok.game.zombies;

import com.ok.game.entities.Zombie;
import com.ok.resource.ResourceManager;

import java.awt.*;
import java.util.Random;

/**
 * 路障僵尸
 * 普通僵尸的强化版，头上戴着路障，生命值更高
 */
public class ConeheadZombie extends Zombie {
    @Override
    public Point getPosition() {
        return null;
    }

    /** 随机数生成器（用于掉落阳光） */
    private static Random random = new Random();

    /** 阳光掉落概率（百分比） */
    private static final int SUN_DROP_CHANCE = 30;

    /** 路障是否还在（未被打掉） */
    private boolean hasCone;

    /** 路障生命值 */
    private int coneHealth;

    /** 路障最大生命值 */
    private static final int CONE_MAX_HEALTH = 100;

    /**
     * 构造函数
     * @param row 所在行
     * @param x 屏幕X坐标
     * @param y 屏幕Y坐标
     */
    public ConeheadZombie(int row, int x, int y) {
        super("ConeheadZombie", row, x, y);

        this.hasCone = true;
        this.coneHealth = CONE_MAX_HEALTH;
    }

    @Override
    protected void loadImage() {
        // 从资源管理器加载图片
        this.image = ResourceManager.getInstance().getZombieImage("ConeheadZombie");
    }

    @Override
    public void takeDamage(int damage) {
        if (state == ZombieState.DYING) return;

        // 先扣路障生命值
        if (hasCone && coneHealth > 0) {
            coneHealth -= damage;
            if (coneHealth <= 0) {
                hasCone = false;
                coneHealth = 0;
                onConeDestroyed();
            }
            return;
        }

        // 路障已损坏，扣本体生命值
        super.takeDamage(damage);
    }

    /**
     * 路障被破坏时的回调
     */
    protected void onConeDestroyed() {
        // 播放路障破碎音效
        // AudioManager.getInstance().playSound("cone_break");

        // 可以添加视觉效果
        // 例如：路障破碎的粒子效果
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

        // 绘制路障（如果还在）
        if (hasCone) {
            drawCone(g);
        }
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

        // 嘴巴
        g.drawArc(x + 32, y + 38, 16, 10, 0, -180);

        // 手臂
        g.setColor(new Color(70, 90, 50));
        g.fillRect(x + 5, y + 45, 15, 12);
        g.fillRect(x + width - 20, y + 45, 15, 12);
    }

    /**
     * 绘制路障
     */
    private void drawCone(Graphics2D g) {
        // 根据剩余血量设置颜色
        float healthPercent = (float) coneHealth / CONE_MAX_HEALTH;
        Color coneColor;
        if (healthPercent > 0.6f) {
            coneColor = new Color(255, 100, 0);  // 橙色
        } else if (healthPercent > 0.3f) {
            coneColor = new Color(200, 80, 0);   // 深橙色
        } else {
            coneColor = new Color(150, 60, 0);   // 棕色
        }

        // 绘制路障（圆锥形）
        int[] xPoints = {
                x + width / 2,
                x + width / 2 - 15,
                x + width / 2 + 15
        };
        int[] yPoints = {
                y - 5,
                y + 25,
                y + 25
        };

        g.setColor(coneColor);
        g.fillPolygon(xPoints, yPoints, 3);

        // 路障边缘
        g.setColor(new Color(180, 80, 0));
        g.drawPolygon(xPoints, yPoints, 3);

        // 路障条纹
        g.setColor(new Color(120, 50, 0));
        g.drawLine(x + width / 2 - 10, y + 10, x + width / 2 + 10, y + 10);
        g.drawLine(x + width / 2 - 8, y + 18, x + width / 2 + 8, y + 18);
    }

    // ==================== Getters ====================

    /**
     * 是否还有路障
     */
    public boolean hasCone() {
        return hasCone;
    }

    /**
     * 获取路障剩余生命值
     */
    public int getConeHealth() {
        return coneHealth;
    }

    /**
     * 获取路障血量百分比
     */
    public float getConeHealthPercent() {
        return (float) coneHealth / CONE_MAX_HEALTH;
    }
}