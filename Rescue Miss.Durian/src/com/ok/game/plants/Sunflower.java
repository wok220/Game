package com.ok.game.plants;

import com.ok.game.entities.Plant;
import com.ok.game.systems.SunSystem;

/**
 * 向日葵
 * 生产型植物，定期产生阳光
 */
public class Sunflower extends Plant {

    /**
     * 构造函数
     * @param row 网格行
     * @param col 网格列
     * @param x 屏幕X坐标
     * @param y 屏幕Y坐标
     */
    public Sunflower(int row, int col, int x, int y) {
        super("Sunflower", row, col, x, y);
    }

    @Override
    protected void loadImage() {
        // 从资源管理器加载图片
        // this.image = ResourceManager.getInstance().getPlantImage("Sunflower");

        // 临时：如果没有图片资源，使用默认绘制（由父类处理）
    }

    @Override
    protected boolean hasTargetInRange() {
        // 向日葵不是攻击型植物，不需要检测目标
        return false;
    }

    @Override
    protected void onAttack() {
        // 向日葵不是攻击型植物，不需要攻击逻辑
        // 空实现
    }

    @Override
    protected void updateProduction(float deltaTime) {
        // 只有生产型植物才需要生产逻辑
        if (!isProducer) {
            return;
        }

        // 更新生产计时器
        produceTimer += deltaTime * 1000;

        // 检查是否到达生产间隔
        if (produceTimer >= produceInterval) {
            produceTimer = 0;
            onProduce();
        }
    }

    @Override
    protected void onProduce() {
        // 产生阳光
        SunSystem sunSystem = SunSystem.getInstance();
        sunSystem.onSunflowerProduce(row, col);
    }
}