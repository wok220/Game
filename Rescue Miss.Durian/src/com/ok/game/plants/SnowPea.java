package com.ok.game.plants;

import com.ok.game.core.GameManager;
import com.ok.game.entities.Plant;
import com.ok.game.entities.Zombie;
import com.ok.game.systems.BulletManager;

/**
 * 寒冰射手
 * 攻击型植物，发射寒冰子弹，命中后减速僵尸
 */
public class SnowPea extends Plant {

    /**
     * 构造函数
     * @param row 网格行
     * @param col 网格列
     * @param x 屏幕X坐标
     * @param y 屏幕Y坐标
     */
    public SnowPea(int row, int col, int x, int y) {
        super("SnowPea", row, col, x, y);
    }

    @Override
    protected void loadImage() {
        // 从资源管理器加载图片
        // this.image = ResourceManager.getInstance().getPlantImage("SnowPea");

        // 临时：如果没有图片资源，使用默认绘制（由父类处理）
    }

    @Override
    protected boolean hasTargetInRange() {
        // 获取游戏管理器实例
        GameManager gm = GameManager.getInstance();

        // 获取同一行中，植物右侧最近的僵尸
        int plantX = x + width;  // 植物右侧位置
        Zombie closestZombie = gm.getClosestZombieInRow(row, plantX);

        return closestZombie != null;
    }

    @Override
    protected void onAttack() {
        // 计算子弹起始位置（植物右侧中央）
        int bulletX = x + width;
        int bulletY = y + height / 2;

        // 获取子弹伤害（寒冰射手伤害较低，但有减速效果）
        int bulletDamage = this.damage;

        // 发射寒冰子弹
        BulletManager.getInstance().shootSnowPea(bulletX, bulletY, row, bulletDamage);
    }

    @Override
    protected void updateProduction(float deltaTime) {
        // 寒冰射手不是生产型植物，不需要生产逻辑
        // 空实现
    }
}