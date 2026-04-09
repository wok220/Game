package com.ok.game.plants;

import com.ok.game.entities.Plant;
import com.ok.game.systems.SunSystem;
import com.ok.resource.ResourceManager;

import java.awt.*;

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
    public Point getPosition() {
        return new Point(x, y);
    }

    @Override
    protected void loadImage() {
        // 加载 PNG 图片（用于随鼠标移动）
        this.image = ResourceManager.getInstance().getPlantImage("向日葵1");
    }

    @Override
    protected String getPlantImageName() {
        return "向日葵"; // 不带"1"后缀，会加载GIF
    }

    @Override
    protected boolean hasTargetInRange() {
        // 向日葵不是攻击型植物，返回false
        return false;
    }

    @Override
    protected void onAttack() {
        // 向日葵不是攻击型植物，空实现
    }

    @Override
    protected void onProduce() {
        // 产生阳光，使用 SunSystem
        SunSystem.getInstance().createSunflowerSun(x, y);
    }
}