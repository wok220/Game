package com.ok.ui.screens;

import com.ok.data.DataManager;
import com.ok.data.PlayerProgress;
import com.ok.ui.GameFrame;
import com.ok.ui.Refreshable;
import com.ok.ui.components.AccountAvatar;
import com.ok.utils.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

/**
 * 主界面
 * 显示墓碑菜单（闯关模式、无限模式、物品栏、拼图系统）
 * 左下角显示账号头像
 */
public class MainScreen extends JPanel implements Refreshable {

    /** 主窗口引用 */
    private GameFrame gameFrame;

    /** 账号头像组件 */
    private AccountAvatar accountAvatar;

    // ==================== 墓碑菜单区域 ====================

    /** 墓碑按钮区域（RESCUE） */
    private Rectangle rescueRect;

    /** 墓碑按钮区域（CHALLENGE） */
    private Rectangle challengeRect;

    /** 墓碑按钮区域（INVENTORY） */
    private Rectangle inventoryRect;

    /** 墓碑按钮区域（JIGSAW） */
    private Rectangle jigsawRect;

    /** 当前悬停的按钮 */
    private String hoverButton;

    /** 当前关卡显示文本 */
    private String levelText;

    // ==================== 音效/音乐开关 ====================

    /** 音效开关按钮区域 */
    private Rectangle soundRect;

    /** 音乐开关按钮区域 */
    private Rectangle musicRect;

    /** 音效是否开启 */
    private boolean soundEnabled;

    /** 音乐是否开启 */
    private boolean musicEnabled;

    // ==================== 物品栏预览区域 ====================

    /** 物品栏是否展开 */
    private boolean inventoryExpanded;

    /** 物品栏当前分类（plants/zombies/tools） */
    private String inventoryCategory;

    /** 物品栏按钮区域（植物） */
    private Rectangle plantsTabRect;

    /** 物品栏按钮区域（僵尸） */
    private Rectangle zombiesTabRect;

    /** 物品栏按钮区域（工具） */
    private Rectangle toolsTabRect;

    /** 物品栏网格区域 */
    private Rectangle inventoryGridRect;

    /** 选中的物品 */
    private String selectedItem;

    /** 选中物品的放大图位置 */
    private Rectangle largeImageRect;

    /** 选中物品的介绍文本位置 */
    private Rectangle descriptionRect;

    /**
     * 构造函数
     * @param gameFrame 主窗口引用
     */
    public MainScreen(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
        this.hoverButton = null;
        this.levelText = "LEVEL 1-1";
        this.soundEnabled = Constants.DEFAULT_SOUND_ENABLED;
        this.musicEnabled = Constants.DEFAULT_MUSIC_ENABLED;
        this.inventoryExpanded = false;
        this.inventoryCategory = "plants";
        this.selectedItem = null;

        setLayout(null);
        setBackground(new Color(30, 30, 40));

        initUI();
        initListeners();
        loadPlayerData();
    }

    /**
     * 初始化UI组件
     */
    private void initUI() {
        // 创建账号头像
        accountAvatar = new AccountAvatar(Constants.AVATAR_SIZE);
        accountAvatar.setBounds(Constants.AVATAR_X, Constants.AVATAR_Y,
                Constants.AVATAR_SIZE + 20, Constants.AVATAR_SIZE + 30);
        accountAvatar.setListener(() -> {
            // TODO: 后期实现账号管理界面
            JOptionPane.showMessageDialog(this,
                    "账号系统开发中\n当前为默认账号",
                    "账号管理",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        add(accountAvatar);

        // 初始化按钮区域
        int centerX = Constants.WINDOW_WIDTH / 2;
        int startY = 200;
        int buttonWidth = 200;
        int buttonHeight = 60;
        int spacing = 20;

        rescueRect = new Rectangle(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight);
        challengeRect = new Rectangle(centerX - buttonWidth / 2, startY + buttonHeight + spacing, buttonWidth, buttonHeight);
        inventoryRect = new Rectangle(centerX - buttonWidth / 2, startY + (buttonHeight + spacing) * 2, buttonWidth, buttonHeight);
        jigsawRect = new Rectangle(centerX - buttonWidth / 2, startY + (buttonHeight + spacing) * 3, buttonWidth, buttonHeight);

        // 音效/音乐开关按钮
        soundRect = new Rectangle(Constants.WINDOW_WIDTH - 120, Constants.WINDOW_HEIGHT - 80, 50, 30);
        musicRect = new Rectangle(Constants.WINDOW_WIDTH - 60, Constants.WINDOW_HEIGHT - 80, 50, 30);

        // 物品栏标签
        int tabWidth = 100;
        int tabHeight = 40;
        int tabsStartX = (Constants.WINDOW_WIDTH - 3 * tabWidth) / 2;
        int tabsStartY = Constants.WINDOW_HEIGHT - 250;

        plantsTabRect = new Rectangle(tabsStartX, tabsStartY, tabWidth, tabHeight);
        zombiesTabRect = new Rectangle(tabsStartX + tabWidth, tabsStartY, tabWidth, tabHeight);
        toolsTabRect = new Rectangle(tabsStartX + tabWidth * 2, tabsStartY, tabWidth, tabHeight);

        // 物品栏网格区域
        inventoryGridRect = new Rectangle(200, tabsStartY + tabHeight + 20,
                Constants.WINDOW_WIDTH - 400, 300);

        // 放大图区域
        largeImageRect = new Rectangle(50, tabsStartY + tabHeight + 50, 200, 200);

        // 介绍文本区域
        descriptionRect = new Rectangle(300, tabsStartY + tabHeight + 80,
                Constants.WINDOW_WIDTH - 400, 150);
    }

    /**
     * 初始化鼠标监听器
     */
    private void initListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }

            @Override
            public void mousePressed(MouseEvent e) {
                handlePress(e.getX(), e.getY());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handleRelease();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                handleHover(e.getX(), e.getY());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                handleHover(e.getX(), e.getY());
            }
        });
    }

    /**
     * 加载玩家数据
     */
    private void loadPlayerData() {
        PlayerProgress progress = DataManager.getInstance().getProvider().getProgress();
        if (progress != null) {
            levelText = "LEVEL " + progress.getCurrentLevel();
        }
        repaint();
    }

    /**
     * 处理点击事件
     */
    private void handleClick(int x, int y) {
        // 检查墓碑按钮
        if (rescueRect.contains(x, y)) {
            // 闯关模式
            PlayerProgress progress = DataManager.getInstance().getProvider().getProgress();
            int levelIndex = progress != null ? progress.getCurrentLevelIndex() : 0;
            gameFrame.showGameScreen("campaign", levelIndex);
            return;
        }

        if (challengeRect.contains(x, y)) {
            // 无限模式
            gameFrame.showGameScreen("endless", 0);
            return;
        }

        if (inventoryRect.contains(x, y)) {
            // 物品栏
            toggleInventory();
            return;
        }

        if (jigsawRect.contains(x, y)) {
            // 拼图系统
            gameFrame.showPuzzleScreen();
            return;
        }

        // 检查音效/音乐开关
        if (soundRect.contains(x, y)) {
            soundEnabled = !soundEnabled;
            // TODO: 应用音效开关
            repaint();
            return;
        }

        if (musicRect.contains(x, y)) {
            musicEnabled = !musicEnabled;
            // TODO: 应用音乐开关
            repaint();
            return;
        }

        // 检查物品栏标签（如果展开）
        if (inventoryExpanded) {
            if (plantsTabRect.contains(x, y)) {
                inventoryCategory = "plants";
                selectedItem = null;
                repaint();
                return;
            }
            if (zombiesTabRect.contains(x, y)) {
                inventoryCategory = "zombies";
                selectedItem = null;
                repaint();
                return;
            }
            if (toolsTabRect.contains(x, y)) {
                inventoryCategory = "tools";
                selectedItem = null;
                repaint();
                return;
            }

            // 检查网格中的物品点击
            checkInventoryItemClick(x, y);
        }
    }

    /**
     * 处理按下事件（用于拖拽）
     */
    private void handlePress(int x, int y) {
        // 预留：用于拖拽滚动物品栏
    }

    /**
     * 处理释放事件
     */
    private void handleRelease() {
        // 预留
    }

    /**
     * 处理悬停事件
     */
    private void handleHover(int x, int y) {
        String oldHover = hoverButton;

        if (rescueRect.contains(x, y)) {
            hoverButton = "rescue";
        } else if (challengeRect.contains(x, y)) {
            hoverButton = "challenge";
        } else if (inventoryRect.contains(x, y)) {
            hoverButton = "inventory";
        } else if (jigsawRect.contains(x, y)) {
            hoverButton = "jigsaw";
        } else {
            hoverButton = null;
        }

        if (oldHover != hoverButton) {
            repaint();
        }
    }

    /**
     * 检查物品栏中的物品点击
     */
    private void checkInventoryItemClick(int x, int y) {
        // TODO: 实现物品栏网格点击逻辑
        // 根据inventoryCategory显示不同的物品网格
        // 点击后设置selectedItem并显示放大图和介绍
    }

    /**
     * 切换物品栏显示
     */
    private void toggleInventory() {
        inventoryExpanded = !inventoryExpanded;
        repaint();
    }

    @Override
    public void refresh() {
        loadPlayerData();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 绘制背景
        drawBackground(g2d);

        // 绘制游戏标题
        drawTitle(g2d);

        // 绘制墓碑菜单
        drawTombstoneMenu(g2d);

        // 绘制音效/音乐开关
        drawAudioButtons(g2d);

        // 绘制物品栏（如果展开）
        if (inventoryExpanded) {
            drawInventory(g2d);
        }
    }

    /**
     * 绘制背景
     */
    private void drawBackground(Graphics2D g) {
        // 渐变背景
        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(40, 40, 60),
                Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT, new Color(20, 20, 40)
        );
        g.setPaint(gradient);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    /**
     * 绘制游戏标题
     */
    private void drawTitle(Graphics2D g) {
        String title = "解救开朗榴莲头";
        String subTitle = "Rescue Miss.Durian";

        // 阴影
        g.setFont(new Font("微软雅黑", Font.BOLD, Constants.TITLE_FONT_SIZE));
        FontMetrics fm = g.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        int titleX = (Constants.WINDOW_WIDTH - titleWidth) / 2;

        g.setColor(new Color(0, 0, 0, 100));
        g.drawString(title, titleX + 3, 80 + 3);

        // 主标题
        GradientPaint gradient = new GradientPaint(
                titleX, 80, new Color(255, 215, 0),
                titleX + titleWidth, 80, new Color(255, 100, 0)
        );
        g.setPaint(gradient);
        g.drawString(title, titleX, 80);

        // 副标题
        g.setFont(new Font("Arial", Font.PLAIN, Constants.NORMAL_FONT_SIZE));
        fm = g.getFontMetrics();
        int subWidth = fm.stringWidth(subTitle);
        int subX = (Constants.WINDOW_WIDTH - subWidth) / 2;

        g.setColor(new Color(200, 200, 150));
        g.drawString(subTitle, subX, 120);
    }

    /**
     * 绘制墓碑菜单
     */
    private void drawTombstoneMenu(Graphics2D g) {
        // RESCUE 按钮
        drawTombstoneButton(g, rescueRect, "RESCUE", hoverButton == "rescue");

        // 关卡显示（在RESCUE按钮下方）
        g.setFont(new Font("Arial", Font.PLAIN, Constants.SMALL_FONT_SIZE));
        FontMetrics fm = g.getFontMetrics();
        int levelWidth = fm.stringWidth(levelText);
        int levelX = rescueRect.x + (rescueRect.width - levelWidth) / 2;
        int levelY = rescueRect.y + rescueRect.height + 5;

        g.setColor(new Color(200, 200, 150));
        g.drawString(levelText, levelX, levelY);

        // CHALLENGE 按钮
        drawTombstoneButton(g, challengeRect, "CHALLENGE", hoverButton == "challenge");

        // INVENTORY 按钮
        drawTombstoneButton(g, inventoryRect, "INVENTORY", hoverButton == "inventory");

        // JIGSAW 按钮
        drawTombstoneButton(g, jigsawRect, "JIGSAW", hoverButton == "jigsaw");
    }

    /**
     * 绘制墓碑样式按钮
     */
    private void drawTombstoneButton(Graphics2D g, Rectangle rect, String text, boolean hover) {
        // 墓碑形状
        int[] xPoints = {
                rect.x + rect.width / 2,
                rect.x + rect.width - 20,
                rect.x + rect.width - 20,
                rect.x + rect.width / 2,
                rect.x + 20,
                rect.x + 20
        };
        int[] yPoints = {
                rect.y,
                rect.y,
                rect.y + rect.height - 20,
                rect.y + rect.height,
                rect.y + rect.height - 20,
                rect.y
        };

        // 背景颜色
        if (hover) {
            g.setColor(new Color(100, 90, 70));
        } else {
            g.setColor(new Color(70, 60, 50));
        }
        g.fillPolygon(xPoints, yPoints, 6);

        // 边框
        g.setColor(new Color(50, 40, 30));
        g.drawPolygon(xPoints, yPoints, 6);

        // 文字
        g.setFont(new Font("Arial", Font.BOLD, 18));
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textX = rect.x + (rect.width - textWidth) / 2;
        int textY = rect.y + (rect.height + fm.getAscent() - fm.getDescent()) / 2;

        if (hover) {
            g.setColor(Color.YELLOW);
        } else {
            g.setColor(new Color(220, 200, 150));
        }
        g.drawString(text, textX, textY);

        // 墓碑裂纹效果
        g.setColor(new Color(40, 30, 20));
        g.drawLine(rect.x + rect.width / 2, rect.y + 10, rect.x + rect.width / 2 + 5, rect.y + 25);
        g.drawLine(rect.x + rect.width / 2 + 5, rect.y + 25, rect.x + rect.width / 2, rect.y + 40);
    }

    /**
     * 绘制音效/音乐开关
     */
    private void drawAudioButtons(Graphics2D g) {
        // 音效按钮
        g.setColor(soundEnabled ? new Color(80, 120, 80) : new Color(80, 80, 80));
        g.fillRoundRect(soundRect.x, soundRect.y, soundRect.width, soundRect.height, 10, 10);
        g.setColor(Color.WHITE);
        g.drawString("🔊", soundRect.x + 15, soundRect.y + 22);

        // 音乐按钮
        g.setColor(musicEnabled ? new Color(80, 120, 80) : new Color(80, 80, 80));
        g.fillRoundRect(musicRect.x, musicRect.y, musicRect.width, musicRect.height, 10, 10);
        g.setColor(Color.WHITE);
        g.drawString("🎵", musicRect.x + 15, musicRect.y + 22);
    }

    /**
     * 绘制物品栏
     */
    private void drawInventory(Graphics2D g) {
        // 半透明背景
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, getWidth(), getHeight());

        // 标题
        g.setFont(new Font("微软雅黑", Font.BOLD, 24));
        g.setColor(Color.WHITE);
        String title = "物品栏";
        FontMetrics fm = g.getFontMetrics();
        int titleX = (getWidth() - fm.stringWidth(title)) / 2;
        g.drawString(title, titleX, 80);

        // 分类标签
        drawTab(g, plantsTabRect, "植物", inventoryCategory.equals("plants"));
        drawTab(g, zombiesTabRect, "僵尸", inventoryCategory.equals("zombies"));
        drawTab(g, toolsTabRect, "工具", inventoryCategory.equals("tools"));

        // 绘制物品网格
        drawInventoryGrid(g);

        // 绘制选中物品的放大图
        if (selectedItem != null) {
            drawLargeItem(g);
        }

        // 绘制关闭按钮
        int closeX = getWidth() - 50;
        int closeY = 20;
        g.setColor(new Color(200, 100, 100));
        g.fillRoundRect(closeX, closeY, 30, 30, 8, 8);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("X", closeX + 10, closeY + 22);
    }

    /**
     * 绘制分类标签
     */
    private void drawTab(Graphics2D g, Rectangle rect, String text, boolean selected) {
        if (selected) {
            g.setColor(new Color(100, 80, 50));
        } else {
            g.setColor(new Color(60, 50, 40));
        }
        g.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);

        g.setFont(new Font("Arial", Font.PLAIN, 16));
        FontMetrics fm = g.getFontMetrics();
        int textX = rect.x + (rect.width - fm.stringWidth(text)) / 2;
        int textY = rect.y + (rect.height + fm.getAscent() - fm.getDescent()) / 2;

        if (selected) {
            g.setColor(Color.YELLOW);
        } else {
            g.setColor(new Color(200, 180, 150));
        }
        g.drawString(text, textX, textY);
    }

    /**
     * 绘制物品网格
     */
    private void drawInventoryGrid(Graphics2D g) {
        // TODO: 根据inventoryCategory显示不同的物品网格
        // 植物: 显示所有植物（已解锁正常，未解锁灰色）
        // 僵尸: 显示所有僵尸图鉴
        // 工具: 显示工具列表

        g.setColor(new Color(80, 70, 60));
        g.drawRect(inventoryGridRect.x, inventoryGridRect.y,
                inventoryGridRect.width, inventoryGridRect.height);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.drawString("物品栏内容开发中...", inventoryGridRect.x + 20, inventoryGridRect.y + 50);
    }

    /**
     * 绘制选中物品的放大图
     */
    private void drawLargeItem(Graphics2D g) {
        g.setColor(new Color(50, 50, 70));
        g.fillRoundRect(largeImageRect.x, largeImageRect.y, largeImageRect.width, largeImageRect.height, 15, 15);
        g.setColor(Color.WHITE);
        g.drawRoundRect(largeImageRect.x, largeImageRect.y, largeImageRect.width, largeImageRect.height, 15, 15);

        // TODO: 显示选中物品的放大图

        // 介绍文本区域
        g.setColor(new Color(50, 50, 70));
        g.fillRoundRect(descriptionRect.x, descriptionRect.y, descriptionRect.width, descriptionRect.height, 15, 15);
        g.setColor(Color.WHITE);
        g.drawRoundRect(descriptionRect.x, descriptionRect.y, descriptionRect.width, descriptionRect.height, 15, 15);

        g.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        g.drawString("物品介绍...", descriptionRect.x + 10, descriptionRect.y + 30);
    }
}