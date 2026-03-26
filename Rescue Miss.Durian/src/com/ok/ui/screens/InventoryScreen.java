package com.ok.ui.screens;

import com.ok.data.DataManager;
import com.ok.data.GameConfig;
import com.ok.data.PlayerProgress;
import com.ok.ui.GameFrame;
import com.ok.ui.Refreshable;
import com.ok.utils.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 * 物品栏界面
 * 展示植物、僵尸、工具的图鉴信息
 */
public class InventoryScreen extends JPanel implements Refreshable {

    /** 主窗口引用 */
    private GameFrame gameFrame;

    /** 当前分类（plants/zombies/tools） */
    private String category;

    /** 物品列表 */
    private List<InventoryItem> items;

    /** 当前选中的物品 */
    private InventoryItem selectedItem;

    // ==================== UI区域 ====================

    /** 返回按钮区域 */
    private Rectangle backButtonRect;

    /** 分类标签区域（植物） */
    private Rectangle plantsTabRect;

    /** 分类标签区域（僵尸） */
    private Rectangle zombiesTabRect;

    /** 分类标签区域（工具） */
    private Rectangle toolsTabRect;

    /** 物品网格区域 */
    private Rectangle gridRect;

    /** 放大图区域 */
    private Rectangle largeImageRect;

    /** 介绍文本区域 */
    private Rectangle descriptionRect;

    /** 网格滚动偏移 */
    private int scrollOffset;

    /** 网格列数 */
    private int gridCols;

    /** 网格行数（可见） */
    private int gridRows;

    /** 物品单元格大小 */
    private int cellSize;

    /** 是否正在拖拽滚动 */
    private boolean isDragging;

    /** 拖拽起始Y坐标 */
    private int dragStartY;

    /** 拖拽起始滚动偏移 */
    private int dragStartOffset;

    /**
     * 物品栏物品类
     */
    private static class InventoryItem {
        String id;
        String name;
        String type;  // plant/zombie/tool
        String description;
        Image icon;
        Image largeIcon;
        boolean isUnlocked;

        InventoryItem(String id, String name, String type, String description, boolean isUnlocked) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.description = description;
            this.isUnlocked = isUnlocked;
        }
    }

    /**
     * 构造函数
     * @param gameFrame 主窗口引用
     */
    public InventoryScreen(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
        this.category = "plants";
        this.items = new ArrayList<>();
        this.selectedItem = null;
        this.scrollOffset = 0;
        this.isDragging = false;

        setLayout(null);
        setBackground(new Color(20, 20, 40));

        initRects();
        initListeners();
        loadItems();
    }

    /**
     * 初始化区域矩形
     */
    private void initRects() {
        int centerX = Constants.WINDOW_WIDTH / 2;
        int startY = 80;
        int tabWidth = 120;
        int tabHeight = 45;

        // 返回按钮
        backButtonRect = new Rectangle(20, 20, 80, 35);

        // 分类标签
        plantsTabRect = new Rectangle(centerX - 180, startY, tabWidth, tabHeight);
        zombiesTabRect = new Rectangle(centerX - 60, startY, tabWidth, tabHeight);
        toolsTabRect = new Rectangle(centerX + 60, startY, tabWidth, tabHeight);

        // 网格区域
        int gridX = 50;
        int gridY = startY + tabHeight + 30;
        int gridWidth = Constants.WINDOW_WIDTH - 100;
        int gridHeight = 280;
        gridRect = new Rectangle(gridX, gridY, gridWidth, gridHeight);

        // 放大图区域
        largeImageRect = new Rectangle(50, gridY + gridHeight + 20, 200, 200);

        // 介绍文本区域
        descriptionRect = new Rectangle(280, gridY + gridHeight + 20,
                Constants.WINDOW_WIDTH - 330, 200);

        // 网格配置
        cellSize = 80;
        gridCols = (gridRect.width - 40) / cellSize;
        if (gridCols < 1) gridCols = 1;
        gridRows = (gridRect.height - 20) / cellSize;
        if (gridRows < 1) gridRows = 1;
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
            public void mouseDragged(MouseEvent e) {
                handleDrag(e.getX(), e.getY());
            }
        });
    }

    /**
     * 加载物品数据
     */
    private void loadItems() {
        items.clear();

        GameConfig config = GameConfig.getInstance();
        PlayerProgress progress = DataManager.getInstance().getProvider().getProgress();

        if ("plants".equals(category)) {
            // 加载植物
            for (String plantId : PlayerProgress.getAllPlants()) {
                GameConfig.PlantConfig plantConfig = config.getPlantConfig(plantId);
                if (plantConfig != null) {
                    boolean unlocked = progress.isPlantUnlocked(plantId);
                    items.add(new InventoryItem(
                            plantId,
                            plantConfig.getName(),
                            "plant",
                            getPlantDescription(plantConfig),
                            unlocked
                    ));
                }
            }
        } else if ("zombies".equals(category)) {
            // 加载僵尸
            for (GameConfig.ZombieConfig zombieConfig : config.getAllZombies().values()) {
                items.add(new InventoryItem(
                        zombieConfig.getId(),
                        zombieConfig.getName(),
                        "zombie",
                        getZombieDescription(zombieConfig),
                        true  // 僵尸图鉴默认全部解锁
                ));
            }
        } else if ("tools".equals(category)) {
            // 加载工具
            loadTools();
        }
    }

    /**
     * 获取植物描述文本
     */
    private String getPlantDescription(GameConfig.PlantConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append("阳光消耗: ").append(config.getSunCost()).append("\n");
        sb.append("生命值: ").append(config.getHealth()).append("\n");

        if (config.isAttacker()) {
            sb.append("伤害: ").append(config.getDamage()).append("\n");
            sb.append("攻击速度: ").append(config.getAttackCooldown() / 1000).append("秒\n");
        }

        if (config.isProducer()) {
            sb.append("生产阳光间隔: ").append(Constants.SUNFLOWER_PRODUCE_INTERVAL / 1000).append("秒\n");
            sb.append("每次生产: ").append(Constants.SUN_PRODUCE_AMOUNT).append("阳光\n");
        }

        if (config.getId().equals("WallNut")) {
            sb.append("高生命值，阻挡僵尸前进");
        } else if (config.getId().equals("TallNut")) {
            sb.append("极高生命值，可阻挡跳跃僵尸");
        } else if (config.getId().equals("PotatoMine")) {
            sb.append("种植后需10秒激活，触碰后爆炸秒杀僵尸");
        } else if (config.getId().equals("CherryBomb")) {
            sb.append("种植后立即爆炸，大范围秒杀僵尸");
        }

        return sb.toString();
    }

    /**
     * 获取僵尸描述文本
     */
    private String getZombieDescription(GameConfig.ZombieConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append("生命值: ").append(config.getHealth()).append("\n");
        sb.append("攻击伤害: ").append(config.getAttackDamage()).append("\n");
        sb.append("移动速度: ").append(config.getSpeed()).append("像素/秒\n");

        if (config.getId().equals("PoleVaultingZombie")) {
            sb.append("特殊能力: 可跳过遇到的第一个植物\n");
            sb.append("跳跃后速度变慢");
        } else if (config.getId().equals("BalloonZombie")) {
            sb.append("特殊能力: 飞行单位，需要先打破气球\n");
            sb.append("气球打破后落地行走");
        } else if (config.getId().equals("DiggerZombie")) {
            sb.append("特殊能力: 从地下挖掘到后方出现\n");
            sb.append("挖掘时无法被攻击");
        } else if (config.getId().equals("Gargantuar")) {
            sb.append("特殊能力: 极高生命值，会投掷小鬼僵尸\n");
            sb.append("生命值低于50%时狂暴加速");
        }

        return sb.toString();
    }

    /**
     * 加载工具数据
     */
    private void loadTools() {
        // 铲子
        items.add(new InventoryItem(
                "shovel",
                "铲子",
                "tool",
                "铲除已种植的植物\n铲除后返还50%阳光消耗",
                true
        ));

        // 小车
        items.add(new InventoryItem(
                "cart",
                "救援小车",
                "tool",
                "每行一辆\n僵尸到达最左侧时自动触发\n消灭该行所有僵尸\n通关时剩余小车可获得鸽子",
                true
        ));

        // 阳光
        items.add(new InventoryItem(
                "sun",
                "阳光",
                "tool",
                "游戏中的基础资源\n用于种植植物\n可通过向日葵生产或天上掉落获得",
                true
        ));

        // 鸽子
        items.add(new InventoryItem(
                "pigeon",
                "鸽子",
                "tool",
                "通关奖励\n用于兑换拼图碎片\n通关时剩余小车数量=获得鸽子数",
                true
        ));
    }

    /**
     * 处理点击事件
     */
    private void handleClick(int x, int y) {
        // 返回按钮
        if (backButtonRect.contains(x, y)) {
            gameFrame.showMainScreen();
            return;
        }

        // 分类标签
        if (plantsTabRect.contains(x, y)) {
            setCategory("plants");
            return;
        }
        if (zombiesTabRect.contains(x, y)) {
            setCategory("zombies");
            return;
        }
        if (toolsTabRect.contains(x, y)) {
            setCategory("tools");
            return;
        }

        // 物品网格点击
        handleGridClick(x, y);
    }

    /**
     * 处理网格点击
     */
    private void handleGridClick(int x, int y) {
        if (!gridRect.contains(x, y)) return;

        int startX = gridRect.x + 20;
        int startY = gridRect.y + 10;

        int col = (x - startX) / cellSize;
        int row = (y - startY) / cellSize + scrollOffset;

        if (col >= 0 && col < gridCols && row >= 0 && row < items.size()) {
            selectedItem = items.get(row);
            repaint();
        }
    }

    /**
     * 处理按下事件
     */
    private void handlePress(int x, int y) {
        if (gridRect.contains(x, y)) {
            isDragging = true;
            dragStartY = y;
            dragStartOffset = scrollOffset;
        }
    }

    /**
     * 处理释放事件
     */
    private void handleRelease() {
        isDragging = false;
    }

    /**
     * 处理拖拽事件
     */
    private void handleDrag(int x, int y) {
        if (!isDragging) return;

        int deltaY = dragStartY - y;
        int maxOffset = Math.max(0, items.size() - gridRows);
        int newOffset = dragStartOffset + deltaY / cellSize;

        scrollOffset = Math.max(0, Math.min(maxOffset, newOffset));
        repaint();
    }

    /**
     * 设置分类
     */
    public void setCategory(String category) {
        this.category = category;
        this.selectedItem = null;
        this.scrollOffset = 0;
        loadItems();
        repaint();
    }

    @Override
    public void refresh() {
        loadItems();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 绘制背景
        drawBackground(g2d);

        // 绘制标题
        drawTitle(g2d);

        // 绘制返回按钮
        drawBackButton(g2d);

        // 绘制分类标签
        drawTabs(g2d);

        // 绘制物品网格
        drawGrid(g2d);

        // 绘制选中物品详情
        if (selectedItem != null) {
            drawItemDetail(g2d);
        }
    }

    /**
     * 绘制背景
     */
    private void drawBackground(Graphics2D g) {
        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(30, 30, 50),
                Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT, new Color(20, 20, 40)
        );
        g.setPaint(gradient);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    /**
     * 绘制标题
     */
    private void drawTitle(Graphics2D g) {
        String title = "物品栏";
        g.setFont(new Font("微软雅黑", Font.BOLD, 32));
        FontMetrics fm = g.getFontMetrics();
        int titleX = (getWidth() - fm.stringWidth(title)) / 2;

        g.setColor(new Color(0, 0, 0, 100));
        g.drawString(title, titleX + 2, 62);
        g.setColor(new Color(255, 215, 0));
        g.drawString(title, titleX, 60);
    }

    /**
     * 绘制返回按钮
     */
    private void drawBackButton(Graphics2D g) {
        g.setColor(new Color(80, 70, 60));
        g.fillRoundRect(backButtonRect.x, backButtonRect.y, backButtonRect.width, backButtonRect.height, 10, 10);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("← 返回", backButtonRect.x + 15, backButtonRect.y + 24);
    }

    /**
     * 绘制分类标签
     */
    private void drawTabs(Graphics2D g) {
        drawTab(g, plantsTabRect, "🌱 植物", category.equals("plants"));
        drawTab(g, zombiesTabRect, "🧟 僵尸", category.equals("zombies"));
        drawTab(g, toolsTabRect, "🔧 工具", category.equals("tools"));
    }

    /**
     * 绘制单个标签
     */
    private void drawTab(Graphics2D g, Rectangle rect, String text, boolean selected) {
        if (selected) {
            g.setColor(new Color(100, 80, 50));
            g.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 15, 15);
            g.setColor(new Color(255, 200, 100));
        } else {
            g.setColor(new Color(60, 50, 40));
            g.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 15, 15);
            g.setColor(new Color(180, 160, 130));
        }

        g.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        FontMetrics fm = g.getFontMetrics();
        int textX = rect.x + (rect.width - fm.stringWidth(text)) / 2;
        int textY = rect.y + (rect.height + fm.getAscent() - fm.getDescent()) / 2;
        g.drawString(text, textX, textY);
    }

    /**
     * 绘制物品网格
     */
    private void drawGrid(Graphics2D g) {
        // 网格背景
        g.setColor(new Color(40, 40, 60, 200));
        g.fillRoundRect(gridRect.x, gridRect.y, gridRect.width, gridRect.height, 15, 15);
        g.setColor(new Color(80, 70, 60));
        g.drawRoundRect(gridRect.x, gridRect.y, gridRect.width, gridRect.height, 15, 15);

        // 绘制滚动条
        drawScrollBar(g);

        // 裁剪区域
        Shape clip = g.getClip();
        g.setClip(gridRect.x, gridRect.y, gridRect.width, gridRect.height);

        int startX = gridRect.x + 20;
        int startY = gridRect.y + 10;

        // 计算可见范围
        int startRow = scrollOffset;
        int endRow = Math.min(startRow + gridRows, items.size());

        for (int i = startRow; i < endRow; i++) {
            InventoryItem item = items.get(i);
            int row = i - startRow;
            int x = startX;
            int y = startY + row * cellSize;

            // 绘制物品单元格
            drawItemCell(g, x, y, cellSize, cellSize, item);
        }

        g.setClip(clip);
    }

    /**
     * 绘制单个物品单元格
     */
    private void drawItemCell(Graphics2D g, int x, int y, int width, int height, InventoryItem item) {
        // 背景
        if (selectedItem == item) {
            g.setColor(new Color(100, 80, 50, 200));
        } else {
            g.setColor(new Color(60, 50, 40, 180));
        }
        g.fillRoundRect(x, y, width, height, 10, 10);

        // 边框
        g.setColor(new Color(120, 100, 80));
        g.drawRoundRect(x, y, width, height, 10, 10);

        // 图标区域
        int iconSize = width - 20;
        int iconX = x + 10;
        int iconY = y + 10;

        // 绘制默认图标
        g.setColor(item.isUnlocked ? new Color(100, 150, 100) : new Color(80, 80, 80));
        g.fillOval(iconX, iconY, iconSize, iconSize);

        // 绘制名称
        g.setFont(new Font("微软雅黑", Font.PLAIN, 10));
        FontMetrics fm = g.getFontMetrics();
        String name = item.name;
        if (fm.stringWidth(name) > width - 10) {
            name = name.substring(0, Math.min(name.length(), 4)) + "..";
        }
        int textX = x + (width - fm.stringWidth(name)) / 2;
        int textY = y + height - 8;

        g.setColor(item.isUnlocked ? Color.WHITE : new Color(150, 150, 150));
        g.drawString(name, textX, textY);

        // 未解锁遮罩
        if (!item.isUnlocked) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRoundRect(x, y, width, height, 10, 10);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("?", x + width / 2 - 5, y + height / 2 + 8);
        }
    }

    /**
     * 绘制滚动条
     */
    private void drawScrollBar(Graphics2D g) {
        int totalItems = items.size();
        if (totalItems <= gridRows) return;

        int scrollBarWidth = 8;
        int scrollBarX = gridRect.x + gridRect.width - scrollBarWidth - 5;
        int scrollBarY = gridRect.y + 5;
        int scrollBarHeight = gridRect.height - 10;

        // 背景
        g.setColor(new Color(80, 70, 60));
        g.fillRoundRect(scrollBarX, scrollBarY, scrollBarWidth, scrollBarHeight, 4, 4);

        // 滑块
        float visibleRatio = (float) gridRows / totalItems;
        int thumbHeight = (int)(scrollBarHeight * visibleRatio);
        int thumbY = scrollBarY + (int)(scrollBarHeight * ((float) scrollOffset / (totalItems - gridRows)));

        g.setColor(new Color(200, 180, 100));
        g.fillRoundRect(scrollBarX, thumbY, scrollBarWidth, thumbHeight, 4, 4);
    }

    /**
     * 绘制选中物品详情
     */
    private void drawItemDetail(Graphics2D g) {
        // 放大图区域
        g.setColor(new Color(50, 40, 50, 200));
        g.fillRoundRect(largeImageRect.x, largeImageRect.y, largeImageRect.width, largeImageRect.height, 15, 15);
        g.setColor(new Color(120, 100, 80));
        g.drawRoundRect(largeImageRect.x, largeImageRect.y, largeImageRect.width, largeImageRect.height, 15, 15);

        // 绘制放大图标
        int iconSize = largeImageRect.width - 40;
        int iconX = largeImageRect.x + (largeImageRect.width - iconSize) / 2;
        int iconY = largeImageRect.y + (largeImageRect.height - iconSize) / 2;

        g.setColor(selectedItem.isUnlocked ? new Color(100, 150, 100) : new Color(80, 80, 80));
        g.fillOval(iconX, iconY, iconSize, iconSize);

        // 名称
        g.setFont(new Font("微软雅黑", Font.BOLD, 18));
        FontMetrics fm = g.getFontMetrics();
        int nameX = largeImageRect.x + (largeImageRect.width - fm.stringWidth(selectedItem.name)) / 2;
        g.setColor(Color.WHITE);
        g.drawString(selectedItem.name, nameX, largeImageRect.y + largeImageRect.height - 15);

        // 介绍文本区域
        g.setColor(new Color(50, 40, 50, 200));
        g.fillRoundRect(descriptionRect.x, descriptionRect.y, descriptionRect.width, descriptionRect.height, 15, 15);
        g.setColor(new Color(120, 100, 80));
        g.drawRoundRect(descriptionRect.x, descriptionRect.y, descriptionRect.width, descriptionRect.height, 15, 15);

        // 绘制介绍文本
        g.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        g.setColor(new Color(220, 200, 150));

        String[] lines = selectedItem.description.split("\n");
        int lineY = descriptionRect.y + 30;
        for (String line : lines) {
            g.drawString(line, descriptionRect.x + 15, lineY);
            lineY += 22;
        }

        // 未解锁提示
        if (!selectedItem.isUnlocked) {
            g.setFont(new Font("微软雅黑", Font.BOLD, 16));
            g.setColor(Color.RED);
            g.drawString("【未解锁】", descriptionRect.x + 15, lineY + 10);
        }
    }
}