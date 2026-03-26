package com.ok.ui.screens;

import com.ok.ui.GameFrame;
import com.ok.ui.Refreshable;
import com.ok.utils.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 加载页面
 * 显示游戏名称和 CLICK TO START
 */
public class LoadScreen extends JPanel implements Refreshable {

    /** 主窗口引用 */
    private GameFrame gameFrame;

    /** 按钮文字是否悬停 */
    private boolean isHovering = false;

    /** 按钮区域（用于点击检测） */
    private Rectangle buttonRect;

    /**
     * 构造函数
     * @param gameFrame 主窗口引用
     */
    public LoadScreen(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
        setLayout(null);
        setBackground(Color.BLACK);

        // 计算按钮区域（在paintComponent中绘制，这里先初始化）
        buttonRect = new Rectangle();

        // 添加鼠标监听
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (buttonRect.contains(e.getPoint())) {
                    // 点击CLICK TO START，进入主界面
                    gameFrame.showMainScreen();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                boolean wasHovering = isHovering;
                isHovering = buttonRect.contains(e.getPoint());
                if (wasHovering != isHovering) {
                    repaint();
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                boolean wasHovering = isHovering;
                isHovering = buttonRect.contains(e.getPoint());
                if (wasHovering != isHovering) {
                    repaint();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 计算按钮位置和大小
        int buttonWidth = 300;
        int buttonHeight = 50;
        int buttonX = (getWidth() - buttonWidth) / 2;
        int buttonY = getHeight() * 2 / 3;
        buttonRect.setBounds(buttonX, buttonY, buttonWidth, buttonHeight);

        // 绘制游戏名称
        drawGameTitle(g2d);

        // 绘制CLICK TO START按钮
        drawStartButton(g2d, buttonX, buttonY, buttonWidth, buttonHeight);

        // 绘制右下角版本信息
        drawVersion(g2d);
    }

    /**
     * 绘制游戏标题
     */
    private void drawGameTitle(Graphics2D g2d) {
        String title = "Rescue Miss.Durian";
        String chineseTitle = "解救开朗榴莲头";

        // 设置英文字体
        Font englishFont = new Font("Arial", Font.BOLD, Constants.TITLE_FONT_SIZE);
        g2d.setFont(englishFont);
        FontMetrics fm = g2d.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        int titleX = (getWidth() - titleWidth) / 2;
        int titleY = getHeight() / 3;

        // 绘制阴影效果
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawString(title, titleX + 2, titleY + 2);

        // 绘制主标题
        GradientPaint gradient = new GradientPaint(
                titleX, titleY, new Color(255, 215, 0),  // 金色
                titleX + titleWidth, titleY, new Color(255, 100, 0)  // 橙色
        );
        g2d.setPaint(gradient);
        g2d.drawString(title, titleX, titleY);

        // 绘制中文标题
        Font chineseFont = new Font("微软雅黑", Font.PLAIN, Constants.NORMAL_FONT_SIZE);
        g2d.setFont(chineseFont);
        fm = g2d.getFontMetrics();
        int chineseWidth = fm.stringWidth(chineseTitle);
        int chineseX = (getWidth() - chineseWidth) / 2;
        int chineseY = titleY + Constants.TITLE_FONT_SIZE + 10;

        g2d.setColor(new Color(255, 200, 100));
        g2d.drawString(chineseTitle, chineseX, chineseY);
    }

    /**
     * 绘制开始按钮
     */
    private void drawStartButton(Graphics2D g2d, int x, int y, int width, int height) {
        // 绘制按钮背景
        if (isHovering) {
            g2d.setColor(new Color(100, 100, 100, 200));
        } else {
            g2d.setColor(new Color(60, 60, 60, 180));
        }
        g2d.fillRoundRect(x, y, width, height, 20, 20);

        // 绘制边框
        g2d.setColor(isHovering ? Color.RED : Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(x, y, width, height, 20, 20);

        // 绘制文字
        String text = "CLICK TO START";
        Font font = new Font("Arial", Font.BOLD, Constants.NORMAL_FONT_SIZE);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textX = x + (width - textWidth) / 2;
        int textY = y + (height + fm.getAscent() - fm.getDescent()) / 2;

        // 悬停时文字变红色
        if (isHovering) {
            g2d.setColor(Color.RED);
        } else {
            g2d.setColor(Color.WHITE);
        }
        g2d.drawString(text, textX, textY);
    }

    /**
     * 绘制版本信息
     */
    private void drawVersion(Graphics2D g2d) {
        String version = "o";
        Font font = new Font("Arial", Font.PLAIN, Constants.SMALL_FONT_SIZE);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(version);
        int textX = getWidth() - textWidth - 20;
        int textY = getHeight() - 20;

        g2d.setColor(new Color(150, 150, 150));
        g2d.drawString(version, textX, textY);
    }

    @Override
    public void refresh() {
        // 加载页面无需刷新数据，但需要重置悬停状态
        isHovering = false;
        repaint();
    }
}