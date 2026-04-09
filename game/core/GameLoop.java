package com.ok.game.core;

import com.ok.utils.Constants;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 游戏循环
 * 使用 Swing Timer 实现稳定的帧率更新
 * 每帧调用 update() 更新游戏逻辑，然后触发界面重绘
 */
public class GameLoop {

    /** 单例实例 */
    private static GameLoop instance;

    /** Swing Timer，用于驱动游戏循环 */
    private Timer timer;

    /** 游戏管理器引用 */
    private GameManager gameManager;

    /** 上一帧的时间（秒） */
    private long lastTime;

    /** 是否正在运行 */
    private boolean isRunning;

    /** 是否暂停 */
    private boolean isPaused;

    /**
     * 私有构造函数
     */
    private GameLoop() {
        this.isRunning = false;
        this.isPaused = false;
        this.lastTime = System.nanoTime();

        // 创建 Timer，在 EDT 线程中执行
        timer = new Timer(Constants.FRAME_TIME, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateFrame();
            }
        });
    }

    /**
     * 获取单例实例
     */
    public static GameLoop getInstance() {
        if (instance == null) {
            instance = new GameLoop();
        }
        return instance;
    }

    /**
     * 初始化游戏循环
     * @param gameManager 游戏管理器
     */
    public void init(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    /**
     * 开始游戏循环
     */
    public void start() {
        if (isRunning) {
            return;
        }
        isRunning = true;
        isPaused = false;
        lastTime = System.nanoTime();
        timer.start();
    }

    /**
     * 停止游戏循环
     */
    public void stop() {
        if (!isRunning) {
            return;
        }
        isRunning = false;
        timer.stop();
    }

    /**
     * 暂停游戏循环（逻辑更新暂停，但帧循环继续）
     */
    public void pause() {
        isPaused = true;
    }

    /**
     * 恢复游戏循环
     */
    public void resume() {
        isPaused = false;
        lastTime = System.nanoTime(); // 重置时间，避免 deltaTime 过大
    }

    /**
     * 每帧更新（由 Timer 调用）
     */
    private void updateFrame() {
        if (!isRunning) {
            return;
        }

        // 计算 deltaTime（秒）
        long currentTime = System.nanoTime();
        float deltaTime = (currentTime - lastTime) / 1_000_000_000.0f;
        lastTime = currentTime;

        // 限制最大 deltaTime，避免卡顿时跳跃过大
        if (deltaTime > 0.1f) {
            deltaTime = 0.1f;
        }

        // 更新游戏逻辑（未暂停时）
        if (!isPaused && gameManager != null && gameManager.isGameActive()) {
            gameManager.update(deltaTime);
        }

        // 请求重绘（由 GameFrame 或 GameScreen 处理）
        if (gameManager != null) {
            gameManager.requestRepaint();
        }
    }

    // ==================== Getters ====================

    /**
     * 是否正在运行
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * 是否暂停
     */
    public boolean isPaused() {
        return isPaused;
    }

    /**
     * 获取当前帧率（调试用）
     */
    public int getCurrentFPS() {
        // 简单实现，实际可以维护一个计数器
        return Constants.TARGET_FPS;
    }
}