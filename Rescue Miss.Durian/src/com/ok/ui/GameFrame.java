package com.ok.ui;

import com.ok.ui.screens.*;
import com.ok.utils.Constants;

import javax.swing.*;
import java.awt.*;

/**
 * 游戏主窗口
 * 负责窗口创建和界面切换
 */
public class GameFrame extends JFrame {

    /** 当前显示的界面 */
    private JPanel currentScreen;

    /** 加载页面 */
    private LoadScreen loadScreen;

    /** 主界面 */
    private MainScreen mainScreen;

    /** 战斗场景 */
    private GameScreen gameScreen;

    /** 物品栏界面 */
    private InventoryScreen inventoryScreen;

    /** 拼图系统界面 */
    private PuzzleScreen puzzleScreen;

    /**
     * 构造函数
     */
    public GameFrame() {
        initFrame();
        initScreens();
        showLoadScreen();
    }

    /**
     * 初始化窗口
     */
    private void initFrame() {
        setTitle(Constants.WINDOW_TITLE);
        setSize(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);  // 居中显示
        setResizable(false);          // 固定大小
        setLayout(new CardLayout());
    }

    /**
     * 初始化所有界面
     */
    private void initScreens() {
        loadScreen = new LoadScreen(this);
        mainScreen = new MainScreen(this);
        gameScreen = new GameScreen(this);
        inventoryScreen = new InventoryScreen(this);
        puzzleScreen = new PuzzleScreen(this);

        // 添加到窗口
        add(loadScreen, "load");
        add(mainScreen, "main");
        add(gameScreen, "game");
        add(inventoryScreen, "inventory");
        add(puzzleScreen, "puzzle");
    }

    // ==================== 界面切换方法 ====================

    /**
     * 显示加载页面
     */
    public void showLoadScreen() {
        switchTo("load");
        currentScreen = loadScreen;
    }

    /**
     * 显示主界面
     */
    public void showMainScreen() {
        mainScreen.refresh();
        switchTo("main");
        currentScreen = mainScreen;
    }

    /**
     * 显示战斗场景
     * @param mode 游戏模式（"campaign" 闯关模式 / "endless" 无限模式）
     * @param levelIndex 关卡索引（闯关模式使用）
     */
    public void showGameScreen(String mode, int levelIndex) {
        gameScreen.initGame(mode, levelIndex);
        gameScreen.startGame();
        switchTo("game");
        currentScreen = gameScreen;
    }

    /**
     * 显示物品栏界面
     * @param category 初始分类（"plants"/"zombies"/"tools"）
     */
    public void showInventoryScreen(String category) {
        inventoryScreen.setCategory(category);
        inventoryScreen.refresh();
        switchTo("inventory");
        currentScreen = inventoryScreen;
    }

    /**
     * 显示拼图系统界面
     */
    public void showPuzzleScreen() {
        puzzleScreen.refresh();
        switchTo("puzzle");
        currentScreen = puzzleScreen;
    }

    /**
     * 切换界面
     * @param name 界面名称
     */
    private void switchTo(String name) {
        CardLayout layout = (CardLayout) getContentPane().getLayout();
        layout.show(getContentPane(), name);
    }

    // ==================== 游戏状态回调 ====================

    /**
     * 游戏胜利回调
     * @param unlockedPlant 解锁的植物ID（可能为null）
     * @param pigeonsEarned 获得的鸽子数量
     */
    public void onGameVictory(String unlockedPlant, int pigeonsEarned) {
        // 返回主界面
        showMainScreen();

        // 显示胜利提示（可通过对话框或通知）
        if (unlockedPlant != null) {
            JOptionPane.showMessageDialog(this,
                    "通关成功！\n获得 " + pigeonsEarned + " 只鸽子\n解锁新植物：" + getPlantName(unlockedPlant),
                    "胜利", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "通关成功！\n获得 " + pigeonsEarned + " 只鸽子",
                    "胜利", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * 游戏失败回调
     */
    public void onGameDefeat() {
        showMainScreen();
        JOptionPane.showMessageDialog(this,
                "僵尸进入了房子！\n游戏失败",
                "失败", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * 返回主界面（从暂停菜单调用）
     */
    public void backToMain() {
        showMainScreen();
    }

    /**
     * 重新开始当前关卡（从暂停菜单调用）
     */
    public void restartLevel() {
        if (currentScreen == gameScreen) {
            gameScreen.restartLevel();
        }
    }

    /**
     * 获取植物名称（用于显示）
     */
    private String getPlantName(String plantId) {
        // 后期从 GameConfig 获取
        switch (plantId) {
            case "PeaShooter": return "豌豆射手";
            case "Sunflower": return "向日葵";
            case "WallNut": return "坚果墙";
            case "SnowPea": return "寒冰射手";
            case "PotatoMine": return "土豆雷";
            case "CherryBomb": return "樱桃炸弹";
            case "Repeater": return "双发射手";
            case "TallNut": return "高坚果";
            default: return plantId;
        }
    }
}