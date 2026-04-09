package com.ok.ui.screens;

import com.ok.ui.GameFrame;
import com.ok.ui.Refreshable;
import com.ok.resource.ResourceManager;
import com.ok.utils.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * 图鉴界面
 * 显示植物和僵尸的介绍
 */
public class InventoryScreen extends JPanel implements Refreshable {

    /** 主窗口引用 */
    private GameFrame gameFrame;

    /** 背景图片（植物介绍） */
    private BufferedImage plantBackground;

    /** 背景图片（僵尸介绍） */
    private BufferedImage zombieBackground;

    /** 当前背景图片 */
    private BufferedImage currentBackground;

    /** 右侧箭头图片 */
    private BufferedImage rightArrowImage;

    /** 左侧箭头图片 */
    private BufferedImage leftArrowImage;

    /** 返回按钮图片 */
    private BufferedImage backButtonImage;

    /** 右侧箭头区域 */
    private Rectangle rightArrowRect;

    /** 左侧箭头区域 */
    private Rectangle leftArrowRect;

    /** 返回按钮区域 */
    private Rectangle backButtonRect;

    /** 当前显示模式（true: 植物介绍, false: 僵尸介绍） */
    private boolean isPlantMode;

    /**
     * 构造函数
     * @param gameFrame 主窗口引用
     */
    public InventoryScreen(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
        this.isPlantMode = true;

        setLayout(null);
        setBackground(new Color(70, 110, 70));

        loadImages();
        initUI();
        initListeners();
    }

    /**
     * 加载图片
     */
    private void loadImages() {
        // 加载背景图片
        plantBackground = ResourceManager.getInstance().getBackgroundImage("植物介绍");
        zombieBackground = ResourceManager.getInstance().getBackgroundImage("僵尸介绍");
        currentBackground = plantBackground;

        // 加载箭头图片
        rightArrowImage = ResourceManager.getInstance().getUIImage("箭头");
        leftArrowImage = ResourceManager.getInstance().getUIImage("箭头左");

        // 加载返回按钮图片
        backButtonImage = ResourceManager.getInstance().getUIImage("back");
    }

    /**
     * 初始化UI组件
     */
    private void initUI() {
        // 初始化箭头区域
        int arrowWidth = 70;
        int arrowHeight = 53;
        rightArrowRect = new Rectangle(Constants.WINDOW_WIDTH - arrowWidth - 35, 25, arrowWidth, arrowHeight);
        leftArrowRect = new Rectangle(30, 25, arrowWidth, arrowHeight);

        // 初始化返回按钮区域
        int backButtonWidth = 90;
        int backButtonHeight = 70;
        backButtonRect = new Rectangle(Constants.WINDOW_WIDTH - backButtonWidth - 30, Constants.WINDOW_HEIGHT - backButtonHeight - 45, backButtonWidth, backButtonHeight);
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
        });
    }

    /**
     * 处理鼠标点击
     * @param x 鼠标X坐标
     * @param y 鼠标Y坐标
     */
    private void handleMouseClick(int x, int y) {
        // 检查右侧箭头
        if (isPlantMode && rightArrowRect.contains(x, y)) {
            // 切换到僵尸介绍
            isPlantMode = false;
            currentBackground = zombieBackground;
            repaint();
            return;
        }

        // 检查左侧箭头
        if (!isPlantMode && leftArrowRect.contains(x, y)) {
            // 切换到植物介绍
            isPlantMode = true;
            currentBackground = plantBackground;
            repaint();
            return;
        }

        // 检查返回按钮
        if (backButtonRect.contains(x, y)) {
            // 返回主菜单
            gameFrame.showMainScreen();
            return;
        }
    }

    @Override
    public void refresh() {
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 绘制背景
        if (currentBackground != null) {
            g2d.drawImage(currentBackground, 0, 0, getWidth(), getHeight(), null);
        }

        // 绘制箭头
        if (isPlantMode) {
            // 绘制右侧箭头
            if (rightArrowImage != null) {
                g2d.drawImage(rightArrowImage, rightArrowRect.x, rightArrowRect.y, rightArrowRect.width, rightArrowRect.height, null);
            }
        } else {
            // 绘制左侧箭头
            if (leftArrowImage != null) {
                g2d.drawImage(leftArrowImage, leftArrowRect.x, leftArrowRect.y, leftArrowRect.width, leftArrowRect.height, null);
            }
        }

        // 绘制返回按钮
        if (backButtonImage != null) {
            g2d.drawImage(backButtonImage, backButtonRect.x, backButtonRect.y, backButtonRect.width, backButtonRect.height, null);
        }
    }
}