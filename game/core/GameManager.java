package com.ok.game.core;

import com.ok.data.DataManager;
import com.ok.game.entities.Bullet;
import com.ok.game.entities.Plant;
import com.ok.game.entities.Sun;
import com.ok.game.entities.Zombie;
import com.ok.game.grid.GridManager;
import com.ok.game.systems.BulletManager;
import com.ok.game.systems.CartSystem;
import com.ok.game.systems.SunSystem;
import com.ok.ui.screens.GameScreen;
import com.ok.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * 游戏管理器
 * 游戏核心管理类，协调所有子系统
 */
public class GameManager {

    /** 单例实例 */
    private static GameManager instance;

    // ==================== 核心组件 ====================

    /** 网格管理器 */
    private GridManager gridManager;

    /** 碰撞检测管理器 */
    private CollisionManager collisionManager;

    /** 波次管理器 */
    private WaveManager waveManager;

    /** 阳光系统 */
    private SunSystem sunSystem;

    /** 子弹管理器 */
    private BulletManager bulletManager;

    /** 小车系统 */
    private CartSystem cartSystem;

    /** 游戏界面引用 */
    private GameScreen gameScreen;

    // ==================== 游戏实体列表 ====================

    /** 所有僵尸列表 */
    private List<Zombie> zombies;

    /** 所有阳光列表 */
    private List<Sun> suns;

    /** 待移除的僵尸列表（防止并发修改） */
    private List<Zombie> zombiesToRemove;

    /** 待移除的阳光列表 */
    private List<Sun> sunsToRemove;

    // ==================== 游戏状态 ====================

    /** 游戏模式（"campaign" 闯关 / "endless" 无限） */
    private String gameMode;

    /** 当前关卡索引（闯关模式使用） */
    private int currentLevelIndex;

    /** 游戏是否活跃（未胜利/未失败） */
    private boolean gameActive;

    /** 游戏是否胜利 */
    private boolean gameVictory;

    /** 游戏是否失败 */
    private boolean gameDefeat;

    /** 游戏是否暂停 */
    private boolean isPaused;

    /** 当前阳光数量 */
    private int currentSun;

    // ==================== 统计信息 ====================

    /** 本关击杀僵尸数 */
    private int killsInLevel;

    /** 本关种植植物数 */
    private int plantsPlantedInLevel;

    /**
     * 私有构造函数
     */
    private GameManager() {
        this.gridManager = GridManager.getInstance();
        this.collisionManager = CollisionManager.getInstance();
        this.waveManager = WaveManager.getInstance();
        this.sunSystem = SunSystem.getInstance();
        this.bulletManager = BulletManager.getInstance();
        this.cartSystem = CartSystem.getInstance();

        this.zombies = new ArrayList<>();
        this.suns = new ArrayList<>();
        this.zombiesToRemove = new ArrayList<>();
        this.sunsToRemove = new ArrayList<>();

        this.gameActive = false;
        this.gameVictory = false;
        this.gameDefeat = false;
        this.isPaused = false;
        this.currentSun = Constants.START_SUN;
        this.killsInLevel = 0;
        this.plantsPlantedInLevel = 0;
    }

    /**
     * 获取单例实例
     */
    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    // ==================== 初始化 ====================

    /**
     * 初始化游戏
     * @param mode 游戏模式（"campaign" / "endless"）
     * @param levelIndex 关卡索引（闯关模式使用）
     * @param gameScreen 游戏界面引用
     */
    public void initGame(String mode, int levelIndex, GameScreen gameScreen) {
        this.gameMode = mode;
        this.currentLevelIndex = levelIndex;
        this.gameScreen = gameScreen;

        // 清空所有数据
        clearAll();

        // 初始化阳光
        this.currentSun = Constants.START_SUN;

        // 初始化子系统
        sunSystem.init(this);
        bulletManager.init(this);
        cartSystem.init(gridManager);

        // 初始化波次
        if ("campaign".equals(mode)) {
            waveManager.initCampaign(levelIndex, this);
        } else {
            waveManager.initEndless(this);
        }

        // 启动游戏
        this.gameActive = true;
        this.gameVictory = false;
        this.gameDefeat = false;
        this.isPaused = false;
        this.killsInLevel = 0;
        this.plantsPlantedInLevel = 0;
    }

    /**
     * 清空所有游戏数据
     */
    private void clearAll() {
        gridManager.clear();
        zombies.clear();
        suns.clear();
        zombiesToRemove.clear();
        sunsToRemove.clear();
        bulletManager.clear();
        sunSystem.clear();
    }

    /**
     * 重置当前关卡
     */
    public void restartLevel() {
        initGame(gameMode, currentLevelIndex, gameScreen);
    }

    // ==================== 更新逻辑 ====================

    /**
     * 更新游戏逻辑
     * @param deltaTime 帧间隔时间（秒）
     */
    public void update(float deltaTime) {
        if (!gameActive || isPaused) {
            return;
        }

        // 1. 更新波次系统
        waveManager.update(deltaTime);

        // 2. 更新所有植物（通过 GridManager 获取所有植物）
        for (Plant plant : gridManager.getAllPlants()) {
            plant.update(deltaTime);
        }

        // 3. 更新所有僵尸
        for (Zombie zombie : zombies) {
            zombie.update(deltaTime);
        }

        // 4. 更新所有子弹
        bulletManager.update(deltaTime);

        // 5. 更新所有阳光
        sunSystem.update(deltaTime);

        // 6. 更新小车系统
        cartSystem.update(deltaTime, zombies);

        // 7. 碰撞检测
        collisionManager.checkCollisions();

        // 7. 处理待移除的实体
        processRemovals();

        // 8. 检查胜负条件
        checkVictory();
        checkDefeat();
    }

    /**
     * 处理待移除的实体
     */
    private void processRemovals() {
        // 移除僵尸
        zombies.removeAll(zombiesToRemove);
        zombiesToRemove.clear();

        // 移除阳光
        suns.removeAll(sunsToRemove);
        sunsToRemove.clear();
    }

    /**
     * 检查胜利条件
     */
    private void checkVictory() {
        if (!gameActive || gameVictory) return;

        // 闯关模式：所有波次完成且没有僵尸
        if ("campaign".equals(gameMode)) {
            if (waveManager.isAllWavesCompleted() && zombies.isEmpty()) {
                onVictory();
            }
        }
        // 无限模式：没有胜利条件，继续游戏
    }

    /**
     * 检查失败条件
     */
    private void checkDefeat() {
        if (!gameActive || gameDefeat) return;

        // 僵尸到达房子
        // 由 Zombie 在到达时调用 onZombieReachHouse()
    }

    /**
     * 胜利处理
     */
    private void onVictory() {
        gameActive = false;
        gameVictory = true;

        // 计算获得的鸽子数（剩余小车数量）
        int remainingCarts = cartSystem.getRemainingCartCount();

        // 获取通关奖励
        String unlockPlant = null;
        if ("campaign".equals(gameMode)) {
            unlockPlant = waveManager.getLevelReward();
        }

    }

    /**
     * 失败处理
     */
    public void onDefeat() {
        if (!gameActive || gameDefeat) return;

        gameActive = false;
        gameDefeat = true;
    }

    /**
     * 僵尸到达房子时调用
     */
    public void onZombieReachHouse() {
        onDefeat();
    }

    // ==================== 实体添加 ====================

    /**
     * 添加僵尸
     */
    public void addZombie(Zombie zombie) {
        zombies.add(zombie);
    }

    /**
     * 添加阳光
     */
    public void addSun(Sun sun) {
        suns.add(sun);
        sunSystem.addSun(sun);
    }

    /**
     * 移除僵尸（标记待移除）
     */
    public void removeZombie(Zombie zombie) {
        if (!zombiesToRemove.contains(zombie)) {
            zombiesToRemove.add(zombie);
            killsInLevel++;
        }
    }

    /**
     * 移除阳光（标记待移除）
     */
    public void removeSun(Sun sun) {
        if (!sunsToRemove.contains(sun)) {
            sunsToRemove.add(sun);
        }
    }

    // ==================== 阳光系统 ====================

    /**
     * 增加阳光
     */
    public void addSunAmount(int amount) {
        currentSun += amount;
    }

    /**
     * 减少阳光（种植消耗）
     * @return 是否成功（阳光不足返回false）
     */
    public boolean spendSun(int amount) {
        if (currentSun >= amount) {
            currentSun -= amount;
            return true;
        }
        return false;
    }

    /**
     * 获取当前阳光数量
     */
    public int getCurrentSun() {
        return currentSun;
    }

    // ==================== 植物种植 ====================

    /**
     * 尝试种植植物
     * @param plant 植物对象
     * @param row 网格行
     * @param col 网格列
     * @return 是否种植成功
     */
    public boolean tryPlant(Plant plant, int row, int col) {
        // 检查阳光是否足够
        if (currentSun < plant.getSunCost()) {
            return false;
        }

        // 尝试种植到网格
        if (gridManager.plant(plant, row, col)) {
            // 扣除阳光
            spendSun(plant.getSunCost());
            plantsPlantedInLevel++;
            return true;
        }

        return false;
    }

    /**
     * 铲除植物
     * @param row 网格行
     * @param col 网格列
     * @return 是否铲除成功
     */
    public boolean shovelPlant(int row, int col) {
        Plant plant = gridManager.removePlant(row, col);
        if (plant != null) {
            // 返还阳光（消耗的50%）
            int refund = plant.getSunCost() * Constants.SHOVEL_REFUND_RATIO / 100;
            addSunAmount(refund);
            return true;
        }
        return false;
    }

    // ==================== 子弹系统 ====================

    /**
     * 发射子弹
     */
    public void shootBullet(Bullet bullet) {
        bulletManager.addBullet(bullet);
    }

    /**
     * 获取所有子弹
     */
    public List<Bullet> getBullets() {
        return bulletManager.getBullets();
    }

    // ==================== 僵尸查询 ====================

    /**
     * 获取指定行的所有僵尸
     */
    public List<Zombie> getZombiesInRow(int row) {
        List<Zombie> result = new ArrayList<>();
        for (Zombie zombie : zombies) {
            if (zombie.getRow() == row && zombie.isAlive()) {
                result.add(zombie);
            }
        }
        return result;
    }

    /**
     * 获取指定行最近的僵尸
     * @param row 行
     * @param minX 最小X坐标（植物位置）
     * @return 最近的僵尸，没有返回null
     */
    public Zombie getClosestZombieInRow(int row, int minX) {
        Zombie closest = null;
        int closestDistance = Integer.MAX_VALUE;

        for (Zombie zombie : zombies) {
            if (zombie.getRow() == row && zombie.isAlive() && zombie.getX() > minX) {
                int distance = zombie.getX() - minX;
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closest = zombie;
                }
            }
        }
        return closest;
    }

    /**
     * 检查指定行是否有僵尸在植物前方
     */
    public boolean hasZombieInFront(int row, int plantX) {
        for (Zombie zombie : zombies) {
            if (zombie.getRow() == row && zombie.isAlive() && zombie.getX() > plantX) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取所有僵尸
     */
    public List<Zombie> getZombies() {
        return zombies;
    }

    /**
     * 获取所有阳光
     */
    public List<Sun> getSuns() {
        return suns;
    }

    // ==================== 小车系统 ====================

    /**
     * 触发小车
     * @param row 行
     */
    public void triggerCart(int row) {
        cartSystem.triggerCart(row, zombies);
    }

    // ==================== 请求重绘 ====================

    /**
     * 请求重绘界面
     */
    public void requestRepaint() {
        if (gameScreen != null) {
            gameScreen.repaint();
        }
    }

    // ==================== 植物解锁 ====================

    /**
     * 获取已解锁植物列表
     */
    public List<String> getUnlockedPlants() {
        return DataManager.getInstance().getProvider().getUnlockedPlants();
    }

    /**
     * 解锁新植物
     * @param plantId 植物ID
     * @return 是否解锁成功
     */
    public boolean unlockPlant(String plantId) {
        return DataManager.getInstance().getProvider().unlockPlant(plantId);
    }

    // ==================== 鸽子系统 ====================

    /**
     * 增加鸽子
     * @param amount 增加数量
     */
    public void addPigeons(int amount) {
        DataManager.getInstance().getProvider().addPigeons(amount);
    }

    // ==================== Getters ====================

    public boolean isGameActive() {
        return gameActive && !isPaused && !gameVictory && !gameDefeat;
    }

    public boolean isGameVictory() {
        return gameVictory;
    }

    public boolean isGameDefeat() {
        return gameDefeat;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        this.isPaused = paused;
    }

    public GridManager getGridManager() {
        return gridManager;
    }

    public WaveManager getWaveManager() {
        return waveManager;
    }

    public CartSystem getCartSystem() {
        return cartSystem;
    }

    public int getKillsInLevel() {
        return killsInLevel;
    }

    public int getPlantsPlantedInLevel() {
        return plantsPlantedInLevel;
    }

    public void init(GameScreen gameScreen) {
    }

    public void tryCollectSun(int x, int y) {
    }

    public Plant[] getPlants() {
        return null;
    }
}