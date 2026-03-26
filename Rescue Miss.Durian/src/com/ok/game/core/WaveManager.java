package com.ok.game.core;

import com.ok.data.GameConfig;
import com.ok.game.entities.Zombie;
import com.ok.game.zombies.*;
import com.ok.utils.Constants;

import java.util.*;

/**
 * 波次管理器
 * 负责僵尸波次的生成和管理
 */
public class WaveManager {

    /** 单例实例 */
    private static WaveManager instance;

    /** 游戏管理器引用 */
    private GameManager gameManager;

    /** 游戏配置引用 */
    private GameConfig gameConfig;

    /** 当前关卡配置（闯关模式） */
    private GameConfig.LevelConfig currentLevelConfig;

    /** 当前波次索引 */
    private int currentWaveIndex;

    /** 当前波次内已生成的僵尸数量 */
    private int spawnedInWave;

    /** 当前波次的僵尸生成列表 */
    private List<GameConfig.ZombieSpawn> currentWaveSpawns;

    /** 下一个僵尸生成的延迟计时器（毫秒） */
    private int nextSpawnTimer;

    /** 波次间隔计时器（毫秒） */
    private int waveIntervalTimer;

    /** 是否在波次间隔中 */
    private boolean isBetweenWaves;

    /** 所有波次是否已完成 */
    private boolean allWavesCompleted;

    // ==================== 无限模式相关 ====================

    /** 是否无限模式 */
    private boolean isEndlessMode;

    /** 当前波次编号（无限模式） */
    private int endlessWaveNumber;

    /** 当前难度系数 */
    private double currentDifficulty;

    /** 无限模式基础僵尸生成间隔 */
    private int endlessBaseSpawnInterval;

    /** 无限模式每波僵尸数量基数 */
    private int endlessBaseZombieCount;

    // ==================== 随机数 ====================

    /** 随机数生成器 */
    private Random random;

    /**
     * 私有构造函数
     */
    private WaveManager() {
        this.random = new Random();
        this.isBetweenWaves = false;
        this.allWavesCompleted = false;
        this.endlessWaveNumber = 0;
        this.currentDifficulty = Constants.ENDLESS_START_DIFFICULTY;
        this.endlessBaseSpawnInterval = Constants.ZOMBIE_SPAWN_INTERVAL;
        this.endlessBaseZombieCount = 3;
    }

    /**
     * 获取单例实例
     */
    public static WaveManager getInstance() {
        if (instance == null) {
            instance = new WaveManager();
        }
        return instance;
    }

    /**
     * 初始化闯关模式
     * @param levelIndex 关卡索引
     * @param gameManager 游戏管理器
     */
    public void initCampaign(int levelIndex, GameManager gameManager) {
        this.gameManager = gameManager;
        this.gameConfig = GameConfig.getInstance();
        this.isEndlessMode = false;
        this.currentWaveIndex = 0;
        this.spawnedInWave = 0;
        this.nextSpawnTimer = 0;
        this.waveIntervalTimer = 0;
        this.isBetweenWaves = false;
        this.allWavesCompleted = false;

        // 获取关卡配置
        this.currentLevelConfig = gameConfig.getLevelConfig(levelIndex);

        if (currentLevelConfig != null) {
            // 加载第一波
            loadWave(0);
        }
    }

    /**
     * 初始化无限模式
     * @param gameManager 游戏管理器
     */
    public void initEndless(GameManager gameManager) {
        this.gameManager = gameManager;
        this.gameConfig = GameConfig.getInstance();
        this.isEndlessMode = true;
        this.endlessWaveNumber = 0;
        this.currentDifficulty = Constants.ENDLESS_START_DIFFICULTY;
        this.currentWaveIndex = 0;
        this.spawnedInWave = 0;
        this.nextSpawnTimer = 0;
        this.waveIntervalTimer = 0;
        this.isBetweenWaves = false;
        this.allWavesCompleted = false;

        // 生成第一波
        generateEndlessWave();
    }

    /**
     * 加载指定波次
     * @param waveIndex 波次索引
     */
    private void loadWave(int waveIndex) {
        if (currentLevelConfig == null) return;

        List<GameConfig.WaveConfig> waves = currentLevelConfig.getWaves();
        if (waveIndex >= waves.size()) {
            // 所有波次完成
            allWavesCompleted = true;
            return;
        }

        GameConfig.WaveConfig wave = waves.get(waveIndex);
        this.currentWaveSpawns = new ArrayList<>(wave.getSpawns());
        this.spawnedInWave = 0;
        this.nextSpawnTimer = 0;
        this.isBetweenWaves = false;

        // 如果有立即生成的僵尸（delay=0），立即生成
        spawnImmediateZombies();
    }

    /**
     * 生成立即生成的僵尸（delay=0）
     */
    private void spawnImmediateZombies() {
        if (currentWaveSpawns == null) return;

        Iterator<GameConfig.ZombieSpawn> iterator = currentWaveSpawns.iterator();
        while (iterator.hasNext()) {
            GameConfig.ZombieSpawn spawn = iterator.next();
            if (spawn.getDelay() <= 0) {
                spawnZombie(spawn);
                iterator.remove();
                spawnedInWave++;
            }
        }
    }

    /**
     * 生成无限模式波次
     */
    private void generateEndlessWave() {
        endlessWaveNumber++;

        // 计算难度
        currentDifficulty = Constants.ENDLESS_START_DIFFICULTY +
                (endlessWaveNumber - 1) * Constants.ENDLESS_DIFFICULTY_INCREMENT;

        // 计算本波僵尸数量（随难度增加）
        int zombieCount = (int)(endlessBaseZombieCount + endlessWaveNumber * 0.5 * currentDifficulty);
        if (zombieCount > Constants.ENDLESS_MAX_ZOMBIES) {
            zombieCount = Constants.ENDLESS_MAX_ZOMBIES;
        }

        // 生成僵尸列表
        currentWaveSpawns = new ArrayList<>();
        for (int i = 0; i < zombieCount; i++) {
            String zombieType = getRandomZombieTypeForEndless();
            int row = random.nextInt(Constants.GRID_ROWS);
            int delay = i * (int)(endlessBaseSpawnInterval / currentDifficulty);
            currentWaveSpawns.add(new GameConfig.ZombieSpawn(zombieType, row, delay));
        }

        this.spawnedInWave = 0;
        this.nextSpawnTimer = 0;
        this.isBetweenWaves = false;

        // 生成立即生成的僵尸
        spawnImmediateZombies();
    }

    /**
     * 获取无限模式的随机僵尸类型
     */
    private String getRandomZombieTypeForEndless() {
        // 根据难度选择僵尸类型
        List<GameConfig.ZombieConfig> availableZombies = gameConfig.getZombiesByLevel(endlessWaveNumber);

        if (availableZombies.isEmpty()) {
            return "NormalZombie";
        }

        // 根据难度权重随机选择
        double totalWeight = 0;
        for (GameConfig.ZombieConfig zombie : availableZombies) {
            totalWeight += zombie.getDifficultyWeight();
        }

        double randomValue = random.nextDouble() * totalWeight;
        double cumulative = 0;
        for (GameConfig.ZombieConfig zombie : availableZombies) {
            cumulative += zombie.getDifficultyWeight();
            if (randomValue <= cumulative) {
                return zombie.getId();
            }
        }

        return "NormalZombie";
    }

    /**
     * 生成单个僵尸
     * @param spawn 僵尸生成配置
     */
    private void spawnZombie(GameConfig.ZombieSpawn spawn) {
        if (gameManager == null) return;

        // 计算僵尸起始X坐标（屏幕右侧）
        int startX = Constants.WINDOW_WIDTH;

        // 计算僵尸Y坐标
        int startY = Constants.GRID_OFFSET_Y + spawn.getRow() * Constants.GRID_HEIGHT;

        // 根据类型创建僵尸
        Zombie zombie = createZombieByType(spawn.getZombieType(), spawn.getRow(), startX, startY);

        if (zombie != null) {
            gameManager.addZombie(zombie);
        }
    }

    /**
     * 根据类型创建僵尸
     */
    private Zombie createZombieByType(String type, int row, int x, int y) {
        switch (type) {
            case "NormalZombie":
                return new NormalZombie(row, x, y);
            case "ConeheadZombie":
                return new ConeheadZombie(row, x, y);
            case "BucketheadZombie":
                return new BucketheadZombie(row, x, y);
            case "PoleVaultingZombie":
                return new PoleVaultingZombie(row, x, y);
            case "BalloonZombie":
                return new BalloonZombie(row, x, y);
            case "DiggerZombie":
                return new DiggerZombie(row, x, y);
            case "Gargantuar":
                return new Gargantuar(row, x, y);
            default:
                return new NormalZombie(row, x, y);
        }
    }

    // ==================== 更新逻辑 ====================

    /**
     * 更新波次管理器
     * @param deltaTime 帧间隔时间（秒）
     */
    public void update(float deltaTime) {
        if (gameManager == null) return;
        if (allWavesCompleted) return;

        // 处理波次间隔
        if (isBetweenWaves) {
            waveIntervalTimer -= deltaTime * 1000;
            if (waveIntervalTimer <= 0) {
                // 波次间隔结束，加载下一波
                isBetweenWaves = false;
                nextWave();
            }
            return;
        }

        // 当前波次还有僵尸需要生成
        if (currentWaveSpawns != null && !currentWaveSpawns.isEmpty()) {
            nextSpawnTimer -= deltaTime * 1000;

            while (nextSpawnTimer <= 0 && !currentWaveSpawns.isEmpty()) {
                // 获取下一个要生成的僵尸
                GameConfig.ZombieSpawn nextSpawn = currentWaveSpawns.get(0);

                // 如果延迟时间还没到，等待
                if (nextSpawn.getDelay() > 0) {
                    nextSpawnTimer = nextSpawn.getDelay();
                    break;
                }

                // 生成僵尸
                spawnZombie(nextSpawn);
                currentWaveSpawns.remove(0);
                spawnedInWave++;

                // 设置下一个延迟（如果有下一个）
                if (!currentWaveSpawns.isEmpty()) {
                    nextSpawnTimer = currentWaveSpawns.get(0).getDelay();
                } else {
                    nextSpawnTimer = 0;
                }
            }
        }

        // 检查当前波次是否完成
        if (currentWaveSpawns != null && currentWaveSpawns.isEmpty()) {
            // 当前波次所有僵尸已生成，等待波次间隔
            isBetweenWaves = true;
            waveIntervalTimer = Constants.WAVE_INTERVAL;
        }
    }

    /**
     * 加载下一波
     */
    private void nextWave() {
        currentWaveIndex++;

        if (isEndlessMode) {
            // 无限模式：生成新波次
            generateEndlessWave();
        } else {
            // 闯关模式：加载下一波
            if (currentLevelConfig != null && currentWaveIndex < currentLevelConfig.getWaves().size()) {
                loadWave(currentWaveIndex);
            } else {
                allWavesCompleted = true;
            }
        }
    }

    // ==================== 查询方法 ====================

    /**
     * 所有波次是否已完成
     */
    public boolean isAllWavesCompleted() {
        return allWavesCompleted;
    }

    /**
     * 获取当前进度（0-1）
     */
    public float getProgress() {
        if (isEndlessMode) {
            return 0; // 无限模式无进度
        }

        if (currentLevelConfig == null) return 0;

        int totalWaves = currentLevelConfig.getWaves().size();
        if (totalWaves == 0) return 0;

        return (float) currentWaveIndex / totalWaves;
    }

    /**
     * 获取当前波次编号
     */
    public int getCurrentWave() {
        if (isEndlessMode) {
            return endlessWaveNumber;
        }
        return currentWaveIndex + 1;
    }

    /**
     * 获取总波次（闯关模式）
     */
    public int getTotalWaves() {
        if (isEndlessMode) {
            return -1; // 无限模式无上限
        }
        if (currentLevelConfig == null) return 0;
        return currentLevelConfig.getWaves().size();
    }

    /**
     * 获取当前难度系数（无限模式）
     */
    public double getCurrentDifficulty() {
        return currentDifficulty;
    }

    /**
     * 获取关卡通关奖励植物
     */
    public String getLevelReward() {
        if (currentLevelConfig != null) {
            return currentLevelConfig.getUnlockReward();
        }
        return null;
    }

    /**
     * 重置波次管理器
     */
    public void reset() {
        this.currentWaveIndex = 0;
        this.spawnedInWave = 0;
        this.nextSpawnTimer = 0;
        this.waveIntervalTimer = 0;
        this.isBetweenWaves = false;
        this.allWavesCompleted = false;
        this.currentWaveSpawns = null;

        if (isEndlessMode) {
            this.endlessWaveNumber = 0;
            this.currentDifficulty = Constants.ENDLESS_START_DIFFICULTY;
        }
    }
}