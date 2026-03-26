package com.ok.ui.screens;

import com.ok.game.core.GameLoop;
import com.ok.game.core.GameManager;
import com.ok.game.core.WaveManager;
import com.ok.game.entities.Bullet;
import com.ok.game.entities.Plant;
import com.ok.game.entities.Sun;
import com.ok.game.entities.Zombie;
import com.ok.game.grid.GridManager;
import com.ok.game.systems.BulletManager;
import com.ok.game.systems.PlantSelector;
import com.ok.game.systems.ShovelSystem;
import com.ok.game.systems.SunSystem;
import com.ok.ui.GameFrame;
import com.ok.ui.Refreshable;
import com.ok.ui.components.PlantCard;
import com.ok.ui.components.ProgressBar;
import com.ok.ui.components.SunCounter;
import com.ok.ui.dialogs.MenuDialog;
import com.ok.utils.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 * 游戏战斗场景
 * 核心玩法界面，整合所有游戏系统
 */
public class GameScreen extends JPanel implements Refreshable {

    /** 主窗口引用 */
    private GameFrame gameFrame;

    /** 游戏管理器 */
    private GameManager gameManager;

    /** 游戏循环 */
    private GameLoop gameLoop;

    /** 网格管理器 */
    private GridManager gridManager;

    /** 植物选择器 */
    private PlantSelector plantSelector;

    /** 铲子系统 */
    private ShovelSystem shovelSystem;

    /** 阳光系统 */
    private SunSystem sunSystem;

    /** 子弹管理器 */
    private BulletManager bulletManager;

    /** 波次管理器 */
    private WaveManager waveManager;

    // ==================== UI组件 ====================

    /** 阳光计数器 */
    private SunCounter sunCounter;

    /** 波次进度条 */
    private ProgressBar waveProgressBar;

    /** 植物卡片列表 */
    private List<PlantCard> plantCards;

    /** 铲子按钮 */
    private Rectangle shovelButtonRect;

    /** 菜单按钮 */
    private Rectangle menuButtonRect;

    /** 是否显示铲子模式指示器 */
    private boolean showShovelIndicator;

    /** 铲子模式指示器位置 */
    private Point shovelIndicatorPos;

    // ==================== 游戏状态 ====================

    /** 游戏模式（"campaign" 闯关 / "endless" 无限） */
    private String gameMode;

    /** 当前关卡索引 */
    private int currentLevelIndex;

    /** 是否显示开场动画 */
    private boolean showIntro;

    /** 开场倒计时 */
    private int countdownTimer;

    /** 开场倒计时文字 */
    private String countdownText;

    /** 是否游戏结束（胜利/失败） */
    private boolean gameEnded;

    // ==================== 构造函数 ====================

    /**
     * 构造函数
     * @param gameFrame 主窗口引用
     */
    public GameScreen(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
        this.plantCards = new ArrayList<>();
        this.shovelButtonRect = new Rectangle(
                Constants.SHOVEL_BUTTON_X, Constants.SHOVEL_BUTTON_Y, 40, 40
        );
        this.menuButtonRect = new Rectangle(
                Constants.MENU_BUTTON_X, Constants.MENU_BUTTON_Y, 30, 30
        );
        this.showShovelIndicator = false;
        this.shovelIndicatorPos = new Point(0, 0);
        this.showIntro = true;
        this.countdownTimer = Constants.COUNTDOWN_SECONDS;
        this.countdownText = "3";
        this.gameEnded = false;

        setLayout(null);
        setBackground(new Color(50, 80, 50));
        setFocusable(true);

        initSystems();
        initUI();
        initListeners();
    }

    /**
     * 初始化系统
     */
    private void initSystems() {
        gameManager = GameManager.getInstance();
        gameLoop = GameLoop.getInstance();
        gridManager = GridManager.getInstance();
        plantSelector = PlantSelector.getInstance();
        shovelSystem = ShovelSystem.getInstance();
        sunSystem = SunSystem.getInstance();
        bulletManager = BulletManager.getInstance();
        waveManager = WaveManager.getInstance();

        // 初始化各系统
        plantSelector.init(gameManager);
        shovelSystem.init(gameManager);
        sunSystem.init(gameManager);
        bulletManager.init(gameManager);
        gameLoop.init(gameManager);
    }

    /**
     * 初始化UI组件
     */
    private void initUI() {
        // 阳光计数器
        sunCounter = new SunCounter(150, 50);
        sunCounter.setBounds(Constants.SUN_COUNTER_X, Constants.SUN_COUNTER_Y, 150, 50);
        sunSystem.setSunCounterPosition(Constants.SUN_COUNTER_X + 120, Constants.SUN_COUNTER_Y + 25);
        add(sunCounter);

        // 波次进度条
        waveProgressBar = new ProgressBar(ProgressBar.ProgressType.WAVE, 200, 30);
        waveProgressBar.setBounds(Constants.PROGRESS_BAR_X, Constants.PROGRESS_BAR_Y, 200, 30);
        waveProgressBar.setShowLabel(true);
        waveProgressBar.setShowPercentage(true);
        add(waveProgressBar);

        // 植物卡片栏
        createPlantCards();
    }

    /**
     * 创建植物卡片
     */
    private void createPlantCards() {
        // 获取已解锁植物
        List<String> unlockedPlants = gameManager.getUnlockedPlants();

        int cardX = Constants.PLANT_BAR_X;
        int cardY = Constants.PLANT_BAR_Y;

        for (int i = 0; i < unlockedPlants.size() && i < 6; i++) {
            String plantId = unlockedPlants.get(i);
            PlantCard card = new PlantCard(plantId);
            card.setBounds(cardX + i * (Constants.PLANT_CARD_WIDTH + Constants.PLANT_CARD_SPACING),
                    cardY, Constants.PLANT_CARD_WIDTH, Constants.PLANT_CARD_HEIGHT);
            card.setListener(plantId1 -> onPlantCardClick(plantId1));
            add(card);
            plantCards.add(card);
        }
    }

    /**
     * 初始化鼠标监听器
     */
    private void initListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e.getX(), e.getY());
            }

            @Override
            public void mousePressed(MouseEvent e) {
                handleMousePress(e.getX(), e.getY());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handleMouseRelease();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                handleMouseMove(e.getX(), e.getY());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseMove(e.getX(), e.getY());
            }
        });
    }

    /**
     * 初始化游戏
     * @param mode 游戏模式
     * @param levelIndex 关卡索引
     */
    public void initGame(String mode, int levelIndex) {
        this.gameMode = mode;
        this.currentLevelIndex = levelIndex;
        this.gameEnded = false;
        this.showIntro = true;
        this.countdownTimer = Constants.COUNTDOWN_SECONDS;
        this.countdownText = String.valueOf(Constants.COUNTDOWN_SECONDS);

        // 初始化游戏管理器
        gameManager.initGame(mode, levelIndex, this);

        // 刷新植物卡片
        refreshPlantCards();

        // 更新阳光显示
        sunCounter.setSunAmount(Constants.START_SUN);

        // 开始开场动画
        startIntro();
    }

    /**
     * 开始开场动画
     */
    private void startIntro() {
        // TODO: 实现开场预览动画
        // 从左边房子平移到右边街道
        // 显示本关僵尸阵容

        // 开始倒计时
        Thread countdownThread = new Thread(() -> {
            for (int i = Constants.COUNTDOWN_SECONDS; i > 0; i--) {
                countdownText = String.valueOf(i);
                repaint();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
            countdownText = "START!";
            repaint();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            showIntro = false;
            startGame();
        });
        countdownThread.start();
    }

    /**
     * 开始游戏
     */
    public void startGame() {
        gameLoop.start();
    }

    /**
     * 重新开始当前关卡
     */
    public void restartLevel() {
        gameLoop.stop();
        gameManager.restartLevel();
        initGame(gameMode, currentLevelIndex);
    }

    /**
     * 植物卡片点击处理
     */
    private void onPlantCardClick(String plantId) {
        if (gameEnded) return;

        // 选择植物
        int currentSun = gameManager.getCurrentSun();
        boolean success = plantSelector.selectPlant(plantId, currentSun);

        if (!success) {
            // 阳光不足，闪烁阳光计数器
            sunCounter.startBlink();
        }

        // 更新所有卡片选中状态
        updateCardSelection();
    }

    /**
     * 更新植物卡片选中状态
     */
    private void updateCardSelection() {
        String selectedPlant = plantSelector.getSelectedPlantId();
        for (PlantCard card : plantCards) {
            boolean isSelected = selectedPlant != null && selectedPlant.equals(card.getPlantId());
            card.updateState(
                    gameManager.getCurrentSun(),
                    plantSelector.isOnCooldown(card.getPlantId()),
                    plantSelector.getCooldownProgress(card.getPlantId(), 1500),
                    isSelected
            );
        }
    }

    /**
     * 刷新植物卡片（解锁新植物时）
     */
    private void refreshPlantCards() {
        // 移除现有卡片
        for (PlantCard card : plantCards) {
            remove(card);
        }
        plantCards.clear();

        // 重新创建
        createPlantCards();
        repaint();
    }

    /**
     * 处理鼠标点击
     */
    private void handleMouseClick(int x, int y) {
        if (showIntro || gameEnded) return;

        // 检查铲子按钮
        if (shovelButtonRect.contains(x, y)) {
            toggleShovelMode();
            return;
        }

        // 检查菜单按钮
        if (menuButtonRect.contains(x, y)) {
            showMenuDialog();
            return;
        }

        // 检查阳光收集
        if (sunSystem.tryCollectSun(x, y)) {
            sunCounter.setSunAmount(gameManager.getCurrentSun());
            return;
        }

        // 检查铲子模式
        if (shovelSystem.isShovelMode()) {
            boolean success = shovelSystem.tryShovel(x, y);
            if (success) {
                sunCounter.setSunAmount(gameManager.getCurrentSun());
                shovelSystem.deactivate();
                showShovelIndicator = false;
            }
            return;
        }

        // 检查种植
        if (plantSelector.isSelecting()) {
            Point gridPos = gridManager.screenToGrid(x, y);
            if (gridPos != null) {
                boolean success = plantSelector.tryPlantAt(gridPos.x, gridPos.y);
                if (success) {
                    sunCounter.setSunAmount(gameManager.getCurrentSun());
                }
                updateCardSelection();
            }
        }
    }

    /**
     * 处理鼠标按下
     */
    private void handleMousePress(int x, int y) {
        if (shovelSystem.isShovelMode()) {
            showShovelIndicator = true;
            shovelIndicatorPos.setLocation(x, y);
            repaint();
        } else if (plantSelector.isSelecting()) {
            // 种植模式，显示跟随图标
            plantSelector.updateMousePosition(x, y);
            repaint();
        }
    }

    /**
     * 处理鼠标释放
     */
    private void handleMouseRelease() {
        if (showShovelIndicator) {
            showShovelIndicator = false;
            repaint();
        }
    }

    /**
     * 处理鼠标移动
     */
    private void handleMouseMove(int x, int y) {
        if (shovelSystem.isShovelMode()) {
            shovelSystem.updateMousePosition(x, y);
            repaint();
        } else if (plantSelector.isSelecting()) {
            plantSelector.updateMousePosition(x, y);
            repaint();
        }

        // 更新网格悬停效果
        repaint();
    }

    /**
     * 切换铲子模式
     */
    private void toggleShovelMode() {
        if (shovelSystem.isShovelMode()) {
            shovelSystem.deactivate();
            showShovelIndicator = false;
        } else {
            // 如果正在种植模式，先取消
            if (plantSelector.isSelecting()) {
                plantSelector.deselect();
                updateCardSelection();
            }
            shovelSystem.activate();
        }
        repaint();
    }

    /**
     * 显示菜单弹窗
     */
    private void showMenuDialog() {
        gameLoop.pause();
        MenuDialog dialog = new MenuDialog(gameFrame, this);
        dialog.setVisible(true);
    }

    /**
     * 游戏胜利回调
     */
    public void onGameVictory(String unlockPlant, int pigeonsEarned) {
        gameEnded = true;
        gameLoop.stop();

        // 保存进度
        if ("campaign".equals(gameMode) && unlockPlant != null) {
            gameManager.unlockPlant(unlockPlant);
        }
        gameManager.addPigeons(pigeonsEarned);

        // 通知主窗口
        gameFrame.onGameVictory(unlockPlant, pigeonsEarned);
    }

    /**
     * 游戏失败回调
     */
    public void onGameDefeat() {
        gameEnded = true;
        gameLoop.stop();
        gameFrame.onGameDefeat();
    }

    /**
     * 更新阳光显示
     */
    public void updateSunDisplay(int sunAmount) {
        sunCounter.setSunAmount(sunAmount);
        updateCardSelection();
    }

    /**
     * 更新进度条
     */
    public void updateProgressBar(float progress) {
        waveProgressBar.setProgress(progress);
    }

    @Override
    public void refresh() {
        // 刷新界面数据
        sunCounter.setSunAmount(gameManager.getCurrentSun());
        waveProgressBar.setProgress(waveManager.getProgress());
        updateCardSelection();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 绘制背景
        drawBackground(g2d);

        // 绘制网格
        drawGrid(g2d);

        // 绘制所有游戏实体
        drawGameEntities(g2d);

        // 绘制UI元素
        drawUI(g2d);

        // 绘制铲子模式指示器
        if (showShovelIndicator) {
            drawShovelIndicator(g2d);
        }

        // 绘制植物跟随图标
        if (plantSelector.isSelecting()) {
            drawPlantFollowIcon(g2d);
        }

        // 绘制开场动画
        if (showIntro) {
            drawIntro(g2d);
        }

        // 绘制网格悬停高亮
        drawHoverHighlight(g2d);
    }

    /**
     * 绘制背景
     */
    private void drawBackground(Graphics2D g) {
        // 草地背景
        g.setColor(new Color(70, 110, 70));
        g.fillRect(0, 0, getWidth(), getHeight());

        // 街道区域（右侧）
        g.setColor(new Color(80, 80, 100));
        g.fillRect(Constants.WINDOW_WIDTH - 100, 0, 100, getHeight());

        // 房子区域（左侧）
        g.setColor(new Color(150, 100, 70));
        g.fillRect(0, 0, 50, getHeight());
    }

    /**
     * 绘制网格
     */
    private void drawGrid(Graphics2D g) {
        int rows = Constants.GRID_ROWS;
        int cols = Constants.GRID_COLS;
        int offsetX = Constants.GRID_OFFSET_X;
        int offsetY = Constants.GRID_OFFSET_Y;
        int cellW = Constants.GRID_WIDTH;
        int cellH = Constants.GRID_HEIGHT;

        g.setColor(Constants.GRID_COLOR);
        g.setStroke(new BasicStroke(1));

        // 绘制网格线
        for (int row = 0; row <= rows; row++) {
            int y = offsetY + row * cellH;
            g.drawLine(offsetX, y, offsetX + cols * cellW, y);
        }

        for (int col = 0; col <= cols; col++) {
            int x = offsetX + col * cellW;
            g.drawLine(x, offsetY, x, offsetY + rows * cellH);
        }
    }

    /**
     * 绘制所有游戏实体
     */
    private void drawGameEntities(Graphics2D g) {
        // 绘制植物
        for (Plant plant : gridManager.getAllPlants()) {
            plant.render(g);
        }

        // 绘制僵尸
        for (Zombie zombie : gameManager.getZombies()) {
            zombie.render(g);
        }

        // 绘制子弹
        for (Bullet bullet : bulletManager.getBullets()) {
            bullet.render(g);
        }

        // 绘制阳光
        for (Sun sun : sunSystem.getSuns()) {
            sun.render(g);
        }
    }

    /**
     * 绘制UI元素
     */
    private void drawUI(Graphics2D g) {
        // 铲子按钮
        g.setColor(shovelSystem.isShovelMode() ? new Color(255, 200, 100) : new Color(200, 180, 100));
        g.fillRoundRect(shovelButtonRect.x, shovelButtonRect.y, shovelButtonRect.width, shovelButtonRect.height, 10, 10);
        g.setColor(Color.BLACK);
        g.drawRoundRect(shovelButtonRect.x, shovelButtonRect.y, shovelButtonRect.width, shovelButtonRect.height, 10, 10);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("🔧", shovelButtonRect.x + 12, shovelButtonRect.y + 28);

        // 菜单按钮
        g.setColor(new Color(100, 100, 120));
        g.fillRoundRect(menuButtonRect.x, menuButtonRect.y, menuButtonRect.width, menuButtonRect.height, 8, 8);
        g.setColor(Color.WHITE);
        g.fillRect(menuButtonRect.x + 8, menuButtonRect.y + 10, 14, 3);
        g.fillRect(menuButtonRect.x + 8, menuButtonRect.y + 18, 14, 3);

        // 关卡信息
        String levelText = "关卡: " + (currentLevelIndex + 1);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString(levelText, Constants.WINDOW_WIDTH - 150, 30);
    }

    /**
     * 绘制铲子模式指示器
     */
    private void drawShovelIndicator(Graphics2D g) {
        int x = shovelSystem.getMouseX();
        int y = shovelSystem.getMouseY();

        g.setColor(new Color(100, 80, 60, 180));
        g.fillRoundRect(x - 15, y - 15, 30, 30, 8, 8);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("🔧", x - 8, y + 8);
    }

    /**
     * 绘制植物跟随图标
     */
    private void drawPlantFollowIcon(Graphics2D g) {
        int x = plantSelector.getMouseX();
        int y = plantSelector.getMouseY();

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
        g.setColor(new Color(100, 150, 100, 150));
        g.fillRoundRect(x - 20, y - 20, 40, 40, 8, 8);
        g.setColor(Color.WHITE);
        g.drawString("🌱", x - 8, y + 8);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    /**
     * 绘制开场动画
     */
    private void drawIntro(Graphics2D g) {
        // 半透明遮罩
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, getWidth(), getHeight());

        // 倒计时文字
        g.setFont(new Font("Arial", Font.BOLD, 80));
        FontMetrics fm = g.getFontMetrics();
        int textX = (getWidth() - fm.stringWidth(countdownText)) / 2;
        int textY = getHeight() / 2;

        g.setColor(Color.WHITE);
        g.drawString(countdownText, textX + 3, textY + 3);
        g.setColor(Color.YELLOW);
        g.drawString(countdownText, textX, textY);
    }

    /**
     * 绘制网格悬停高亮
     */
    private void drawHoverHighlight(Graphics2D g) {
        if (showIntro || gameEnded) return;

        Point mousePos = getMousePosition();
        if (mousePos != null) {
            Point gridPos = gridManager.screenToGrid(mousePos.x, mousePos.y);
            if (gridPos != null) {
                Rectangle cellBounds = gridManager.getCellBounds(gridPos.x, gridPos.y);
                g.setColor(Constants.GRID_HOVER_COLOR);
                g.fillRect(cellBounds.x, cellBounds.y, cellBounds.width, cellBounds.height);
            }
        }
    }

    /**
     * 获取游戏管理器（供外部调用）
     */
    public GameManager getGameManager() {
        return gameManager;
    }

    /**
     * 暂停游戏
     */
    public void pauseGame() {
        gameLoop.pause();
    }

    /**
     * 恢复游戏
     */
    public void resumeGame() {
        gameLoop.resume();
    }
}