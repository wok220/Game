package com.ok.ui;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 界面管理器
 * 单例模式，统一管理所有界面的注册、切换和刷新
 */
public class ScreenManager {

    /** 单例实例 */
    private static ScreenManager instance;

    /** 主窗口引用 */
    private GameFrame gameFrame;

    /** 界面缓存（界面名称 -> 界面对象） */
    private Map<String, Refreshable> screens;

    /** 当前显示的界面名称 */
    private String currentScreenName;

    /**
     * 私有构造函数
     */
    private ScreenManager() {
        screens = new HashMap<>();
    }

    /**
     * 获取单例实例
     */
    public static ScreenManager getInstance() {
        if (instance == null) {
            instance = new ScreenManager();
        }
        return instance;
    }

    /**
     * 初始化界面管理器
     * @param gameFrame 主窗口
     */
    public void init(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
    }

    /**
     * 注册界面
     * @param name 界面唯一标识
     * @param screen 界面对象（需实现 Refreshable）
     */
    public void registerScreen(String name, Refreshable screen) {
        screens.put(name, screen);
    }

    /**
     * 获取界面
     * @param name 界面名称
     * @return 界面对象，不存在返回null
     */
    public Refreshable getScreen(String name) {
        return screens.get(name);
    }

    /**
     * 切换界面
     * @param name 界面名称
     */
    public void switchTo(String name) {
        Refreshable screen = screens.get(name);
        if (screen == null) {
            System.err.println("界面不存在: " + name);
            return;
        }

        // 刷新目标界面
        screen.refresh();

        // 执行切换
        if (gameFrame != null) {
            CardLayout layout = (CardLayout) gameFrame.getContentPane().getLayout();
            layout.show(gameFrame.getContentPane(), name);
        }

        currentScreenName = name;
    }

    /**
     * 切换界面并传递参数
     * @param name 界面名称
     * @param params 参数（由具体界面解析）
     */
    public void switchTo(String name, Object... params) {
        Refreshable screen = screens.get(name);
        if (screen == null) {
            System.err.println("界面不存在: " + name);
            return;
        }

        // 如果界面实现了 ParamRefreshable，传递参数
        if (screen instanceof ParamRefreshable) {
            ((ParamRefreshable) screen).refreshWithParams(params);
        } else {
            screen.refresh();
        }

        // 执行切换
        if (gameFrame != null) {
            CardLayout layout = (CardLayout) gameFrame.getContentPane().getLayout();
            layout.show(gameFrame.getContentPane(), name);
        }

        currentScreenName = name;
    }

    /**
     * 刷新所有已注册的界面
     */
    public void refreshAllScreens() {
        for (Map.Entry<String, Refreshable> entry : screens.entrySet()) {
            entry.getValue().refresh();
        }
    }

    /**
     * 刷新指定界面
     * @param name 界面名称
     */
    public void refreshScreen(String name) {
        Refreshable screen = screens.get(name);
        if (screen != null) {
            screen.refresh();
        }
    }

    /**
     * 获取当前界面名称
     */
    public String getCurrentScreenName() {
        return currentScreenName;
    }

    /**
     * 获取当前界面对象
     */
    public Refreshable getCurrentScreen() {
        return screens.get(currentScreenName);
    }

    /**
     * 检查界面是否已注册
     */
    public boolean isScreenRegistered(String name) {
        return screens.containsKey(name);
    }

    /**
     * 获取所有已注册界面名称
     */
    public String[] getAllScreenNames() {
        return screens.keySet().toArray(new String[0]);
    }

    /**
     * 获取已注册界面数量
     */
    public int getScreenCount() {
        return screens.size();
    }
}