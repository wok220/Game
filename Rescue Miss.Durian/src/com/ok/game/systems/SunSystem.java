package com.ok.game.systems;

import com.ok.game.core.GameManager;
import com.ok.game.entities.Sun;
import com.ok.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 阳光系统
 * 负责阳光的产生（天上掉落 + 向日葵生产）和收集动画
 */
public class SunSystem {

    /** 单例实例 */
    private static SunSystem instance;

    /** 游戏管理器引用 */
    private GameManager gameManager;

    /** 所有阳光列表 */
    private List<Sun> suns;

    /** 待移除的阳光列表 */
    private List<Sun> sunsToRemove;

    /** 随机数生成器 */
    private Random random;

    // ==================== 天上掉落计时 ====================

    /** 下次掉落计时器（毫秒） */
    private int nextSkySunTimer;

    /** 下次掉落间隔（毫秒） */
    private int nextSkySunInterval;

    // ==================== 阳光计数器位置 ====================

    /** 阳光计数器X坐标（收集动画目标点） */
    private int sunCounterX;

    /** 阳光计数器Y坐标（收集动画目标点） */
    private int sunCounterY;

    /**
     * 私有构造函数
     */
    private SunSystem() {
        this.suns = new ArrayList<>();
        this.sunsToRemove = new ArrayList<>();
        this.random = new Random();
        this.nextSkySunTimer = 0;
        this.nextSkySunInterval = getRandomSkySunInterval();
    }

    /**
     * 获取单例实例
     */
    public static SunSystem getInstance() {
        if (instance == null) {
            instance = new SunSystem();
        }
        return instance;
    }

    /**
     * 初始化
     * @param gameManager 游戏管理器
     */
    public void init(GameManager gameManager) {
        this.gameManager = gameManager;
        resetSkySunTimer();
    }

    /**
     * 设置阳光计数器位置
     * @param x X坐标
     * @param y Y坐标
     */
    public void setSunCounterPosition(int x, int y) {
        this.sunCounterX = x;
        this.sunCounterY = y;
    }

    // ==================== 更新逻辑 ====================

    /**
     * 更新阳光系统
     * @param deltaTime 帧间隔时间（秒）
     */
    public void update(float deltaTime) {
        // 1. 更新天上掉落计时
        updateSkySunTimer(deltaTime);

        // 2. 更新所有阳光
        updateAllSuns(deltaTime);

        // 3. 处理待移除的阳光
        processRemovals();
    }

    /**
     * 更新天上掉落计时
     */
    private void updateSkySunTimer(float deltaTime) {
        if (gameManager == null || !gameManager.isGameActive()) {
            return;
        }

        nextSkySunTimer -= deltaTime * 1000;
        if (nextSkySunTimer <= 0) {
            // 产生天上掉落的阳光
            createSkySun();

            // 重置计时器
            resetSkySunTimer();
        }
    }

    /**
     * 更新所有阳光
     */
    private void updateAllSuns(float deltaTime) {
        for (Sun sun : suns) {
            sun.update(deltaTime);
            if (!sun.isAlive()) {
                markForRemoval(sun);
            }
        }
    }

    /**
     * 处理待移除的阳光
     */
    private void processRemovals() {
        suns.removeAll(sunsToRemove);
        sunsToRemove.clear();
    }

    // ==================== 阳光产生 ====================

    /**
     * 产生天上掉落的阳光
     */
    private void createSkySun() {
        if (gameManager == null) return;

        // 随机X坐标（在网格范围内）
        int minX = Constants.GRID_OFFSET_X;
        int maxX = Constants.GRID_OFFSET_X + Constants.GRID_COLS * Constants.GRID_WIDTH;
        int x = minX + random.nextInt(maxX - minX);

        // 起始Y坐标（窗口顶部附近）
        int startY = 50;

        // 目标Y坐标（网格上方）
        int targetY = Constants.GRID_OFFSET_Y + Constants.GRID_ROWS * Constants.GRID_HEIGHT / 2;

        // 创建阳光
        Sun sun = new Sun(x, startY, targetY, Constants.SUN_PRODUCE_AMOUNT);
        addSun(sun);
    }

    /**
     * 向日葵生产阳光
     * @param plantX 植物X坐标
     * @param plantY 植物Y坐标
     */
    public void createSunflowerSun(int plantX, int plantY) {
        Sun sun = new Sun(plantX, plantY);
        addSun(sun);
    }

    /**
     * 僵尸死亡掉落阳光
     * @param x 僵尸X坐标
     * @param y 僵尸Y坐标
     * @param value 阳光价值
     */
    public void createDropSun(int x, int y, int value) {
        // 目标Y坐标（僵尸死亡位置下方）
        int targetY = y + Constants.ZOMBIE_HEIGHT / 2;
        Sun sun = new Sun(x, y, targetY, value);
        addSun(sun);
    }

    /**
     * 添加阳光
     */
    public void addSun(Sun sun) {
        suns.add(sun);
    }

    // ==================== 阳光收集 ====================

    /**
     * 尝试收集阳光
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     * @return 是否收集到阳光
     */
    public boolean tryCollectSun(int mouseX, int mouseY) {
        for (Sun sun : suns) {
            if (sun.isAlive() && !sun.isCollecting() && sun.contains(mouseX, mouseY)) {
                collectSun(sun);
                return true;
            }
        }
        return false;
    }

    /**
     * 收集指定阳光
     * @param sun 阳光对象
     */
    public void collectSun(Sun sun) {
        if (sun == null || !sun.isAlive() || sun.isCollecting()) return;

        // 开始收集动画
        sun.collect(sunCounterX, sunCounterY);

        // 增加阳光值
        if (gameManager != null) {
            gameManager.addSunAmount(sun.getValue());
        }

        // 标记待移除（动画结束后移除）
        markForRemoval(sun);
    }

    // ==================== 计时器管理 ====================

    /**
     * 重置天上掉落计时器
     */
    private void resetSkySunTimer() {
        nextSkySunTimer = nextSkySunInterval;
        nextSkySunInterval = getRandomSkySunInterval();
    }

    /**
     * 获取随机的天上掉落间隔
     */
    private int getRandomSkySunInterval() {
        int min = Constants.SKY_SUN_MIN_INTERVAL;
        int max = Constants.SKY_SUN_MAX_INTERVAL;
        return min + random.nextInt(max - min);
    }

    // ==================== 列表操作 ====================

    /**
     * 标记阳光待移除
     */
    private void markForRemoval(Sun sun) {
        if (!sunsToRemove.contains(sun)) {
            sunsToRemove.add(sun);
        }
    }

    /**
     * 获取所有阳光
     */
    public List<Sun> getSuns() {
        return suns;
    }

    /**
     * 清空所有阳光
     */
    public void clear() {
        suns.clear();
        sunsToRemove.clear();
        resetSkySunTimer();
    }

    // ==================== 向日葵生产管理 ====================

    /**
     * 向日葵生产阳光（由向日葵植物调用）
     * @param plantRow 植物所在行
     * @param plantCol 植物所在列
     */
    public void onSunflowerProduce(int plantRow, int plantCol) {
        if (gameManager == null) return;

        // 计算植物屏幕坐标
        int plantX = Constants.GRID_OFFSET_X + plantCol * Constants.GRID_WIDTH;
        int plantY = Constants.GRID_OFFSET_Y + plantRow * Constants.GRID_HEIGHT;

        createSunflowerSun(plantX, plantY);
    }

    // ==================== 调试方法 ====================

    /**
     * 获取当前阳光数量（调试用）
     */
    public int getSunCount() {
        return suns.size();
    }
}