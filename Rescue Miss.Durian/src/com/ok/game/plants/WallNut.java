package com.ok.game.plants;

import com.ok.game.entities.Plant;

/**
 * 坚果墙
 * 防御型植物，高生命值，用于阻挡僵尸前进
 */
public class WallNut extends Plant {

    /**
     * 构造函数
     * @param row 网格行
     * @param col 网格列
     * @param x 屏幕X坐标
     * @param y 屏幕Y坐标
     */
    public WallNut(int row, int col, int x, int y) {
        super("WallNut", row, col, x, y);
    }

    @Override
    protected void loadImage() {
        // 从资源管理器加载图片
        // this.image = ResourceManager.getInstance().getPlantImage("WallNut");

        // 临时：如果没有图片资源，使用默认绘制（由父类处理）
    }

    @Override
    protected boolean hasTargetInRange() {
        // 坚果墙不是攻击型植物，不需要检测目标
        return false;
    }

    @Override
    protected void onAttack() {
        // 坚果墙不是攻击型植物，不需要攻击逻辑
        // 空实现
    }

    @Override
    protected void updateProduction(float deltaTime) {
        // 坚果墙不是生产型植物，不需要生产逻辑
        // 空实现
    }

    @Override
    protected void onProduce() {
        // 坚果墙不是生产型植物，不需要生产逻辑
        // 空实现
    }
}