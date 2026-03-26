package com.ok.game.plants;

import com.ok.game.entities.Plant;
import com.ok.game.entities.Zombie;
import com.ok.game.core.GameManager;
import com.ok.utils.Constants;

import java.awt.*;
import java.util.List;

/**
 * 土豆雷
 * 陷阱型植物，需要时间激活，僵尸触碰后爆炸造成范围伤害
 */
public class PotatoMine extends Plant {

    /** 是否已激活（准备就绪） */
    private boolean isArmed;

    /** 激活计时器（毫秒） */
    private int armTimer;

    /** 激活所需时间（毫秒） */
    private int armTime;

    /** 爆炸范围（像素） */
    private int explosionRange;

    /** 爆炸伤害 */
    private int explosionDamage;

    /**
     * 构造函数
     * @param row 网格行
     * @param col 网格列
     * @param x 屏幕X坐标
     * @param y 屏幕Y坐标
     */
    public PotatoMine(int row, int col, int x, int y) {
        super("PotatoMine", row, col, x, y);

        // 土豆雷特殊属性
        this.isArmed = false;
        this.armTime = 10000;  // 10秒激活时间
        this.armTimer = 0;
        this.explosionRange = Constants.GRID_WIDTH;  // 一个格子范围
        this.explosionDamage = 1800;  // 秒杀大多数僵尸
    }

    @Override
    protected void loadImage() {
        // 从资源管理器加载图片（未激活和已激活两种状态）
        // this.unarmedImage = ResourceManager.getInstance().getPlantImage("PotatoMine");
        // this.armedImage = ResourceManager.getInstance().getPlantImage("PotatoMine_Armed");

        // 临时：如果没有图片资源，使用默认绘制（由父类处理）
    }

    @Override
    public void update(float deltaTime) {
        // 如果已经激活，不需要更新激活计时
        if (isArmed) {
            return;
        }

        // 更新激活计时
        armTimer += deltaTime * 1000;

        // 检查是否激活
        if (armTimer >= armTime) {
            isArmed = true;
        }

        // 调用父类更新（处理伤害闪烁等）
        super.update(deltaTime);
    }

    @Override
    protected boolean hasTargetInRange() {
        // 未激活时不检测
        if (!isArmed) {
            return false;
        }

        // 检查是否有僵尸在爆炸范围内
        GameManager gm = GameManager.getInstance();
        List<Zombie> zombies = gm.getZombiesInRow(row);

        for (Zombie zombie : zombies) {
            if (!zombie.isAlive() || zombie.isDying()) {
                continue;
            }

            // 检查僵尸是否在爆炸范围内
            int zombieCenterX = zombie.getX() + zombie.getWidth() / 2;
            int mineCenterX = x + width / 2;
            int distance = Math.abs(zombieCenterX - mineCenterX);

            if (distance <= explosionRange) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void onAttack() {
        // 土豆雷爆炸
        explode();
    }

    /**
     * 爆炸
     * 对范围内的所有僵尸造成伤害
     */
    private void explode() {
        GameManager gm = GameManager.getInstance();
        List<Zombie> zombies = gm.getZombiesInRow(row);

        int mineCenterX = x + width / 2;

        for (Zombie zombie : zombies) {
            if (!zombie.isAlive() || zombie.isDying()) {
                continue;
            }

            // 检查僵尸是否在爆炸范围内
            int zombieCenterX = zombie.getX() + zombie.getWidth() / 2;
            int distance = Math.abs(zombieCenterX - mineCenterX);

            if (distance <= explosionRange) {
                // 造成伤害
                zombie.takeDamage(explosionDamage);
            }
        }

        // 爆炸后土豆雷死亡
        die();
    }

    @Override
    protected void updateAttack(float deltaTime) {
        // 土豆雷的攻击逻辑：检测到僵尸时立即爆炸
        if (isArmed && hasTargetInRange()) {
            onAttack();
        }
    }

    @Override
    protected void updateProduction(float deltaTime) {
        // 土豆雷不是生产型植物
    }

    @Override
    protected void onProduce() {
        // 土豆雷不是生产型植物
    }

    // ==================== 渲染 ====================

    @Override
    public void render(Graphics2D g) {
        if (!visible) return;

        // 根据激活状态显示不同颜色
        if (isArmed) {
            // 已激活：绿色高亮
            g.setColor(new Color(0, 200, 0, 100));
            g.fillOval(x - 5, y - 5, width + 10, height + 10);
        } else {
            // 未激活：显示进度圈
            float progress = (float) armTimer / armTime;
            int angle = (int)(360 * progress);

            g.setColor(new Color(255, 200, 0, 150));
            g.fillArc(x - 5, y - 5, width + 10, height + 10, 90, -angle);
        }

        // 绘制植物图片
        if (image != null) {
            g.drawImage(image, x, y, width, height, null);
        } else {
            // 默认绘制
            g.setColor(new Color(139, 69, 19));  // 棕色
            g.fillOval(x + 10, y + 30, width - 20, height - 40);
            g.setColor(new Color(101, 67, 33));
            g.fillOval(x + 15, y + 25, width - 30, height - 50);

            // 绘制引线
            g.setColor(Color.BLACK);
            g.drawLine(x + width / 2, y + 10, x + width / 2, y + 25);
            g.fillOval(x + width / 2 - 2, y + 5, 4, 8);
        }

        // 绘制血条（如果有受伤）
        if (health < maxHealth && maxHealth > 0) {
            drawHealthBar(g);
        }
    }

    // ==================== Getters ====================

    /**
     * 是否已激活
     */
    public boolean isArmed() {
        return isArmed;
    }

    /**
     * 获取激活进度（0-1）
     */
    public float getArmProgress() {
        return (float) armTimer / armTime;
    }
}