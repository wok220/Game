package com.ok.game.plants;

import com.ok.game.core.GameManager;
import com.ok.game.entities.Plant;
import com.ok.game.entities.Zombie;
import com.ok.game.systems.BulletManager;
import com.ok.resource.ResourceManager;

import java.awt.*;

/**
 * 豌豆射手
 * 基础攻击型植物，向前方发射豌豆子弹
 */
public class PeaShooter extends Plant {

    /**
     * 构造函数
     * @param row 网格行
     * @param col 网格列
     * @param x 屏幕X坐标
     * @param y 屏幕Y坐标
     */
    public PeaShooter(int row, int col, int x, int y) {
        super("PeaShooter", row, col, x, y);
    }

    @Override
    public Point getPosition() {
        return new Point(x, y);
    }

    @Override
    protected void loadImage() {
        // 加载 PNG 图片（用于随鼠标移动）
        this.image = ResourceManager.getInstance().getPlantImage("豌豆射手1");
    }

    @Override
    protected String getPlantImageName() {
        return "豌豆射手"; // 不带"1"后缀，会加载GIF
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

        // 获取子弹伤害
        int bulletDamage = this.damage;

        // 发射子弹
        BulletManager.getInstance().shootPea(bulletX, bulletY, row, bulletDamage);
    }

    @Override
    protected void updateProduction(float deltaTime) {
        // 豌豆射手不是生产型植物，不需要生产逻辑
        // 空实现
    }
}