package com.ok.game.zombies;

import com.ok.game.core.GameManager;
import com.ok.game.entities.Zombie;
import com.ok.resource.ResourceManager;

import java.awt.*;
import java.util.Random;

/**
 * 普通僵尸
 * 最基础的僵尸类型，移动速度中等，生命值中等
 */
public class NormalZombie extends Zombie {
    @Override
    public Point getPosition() {
        return null;
    }

    /** 随机数生成器（用于掉落阳光） */
    private static Random random = new Random();

    /** 阳光掉落概率（百分比） */
    private static final int SUN_DROP_CHANCE = 25;

    /**
     * 构造函数
     * @param row 所在行
     * @param x 屏幕X坐标
     * @param y 屏幕Y坐标
     */
    public NormalZombie(int row, int x, int y) {
        super("NormalZombie", row, x, y);
    }

    protected void loadImage(Graphics2D g) {
        // 从资源管理器加载图片
        this.image = ResourceManager.getInstance().getZombieImage("NormalZombie");
    }

    @Override
    protected void onDeath() {
        // 调用父类死亡逻辑（会触发maybeDropSun）
        super.onDeath();

        // 播放死亡音效
        // AudioManager.getInstance().playSound("zombie_die");

        // 通知游戏管理器统计击杀
        GameManager gm = GameManager.getInstance();
        // gm.addZombieKills(1); // 如果有统计方法
    }
}