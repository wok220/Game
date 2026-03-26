package com.ok.ui.components;

import com.ok.utils.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * 阳光计数器组件
 * 显示当前阳光数量，支持闪烁效果
 */
public class SunCounter extends JPanel {

    /** 当前阳光数量 */
    private int sunAmount;

    /** 目标阳光数量（用于动画） */
    private int targetSunAmount;

    /** 动画计时器 */
    private int animationTimer;

    /** 动画持续时间（毫秒） */
    private static final int ANIMATION_DURATION = 300;

    /** 是否闪烁（阳光不足时） */
    private boolean isBlinking;

    /** 闪烁计时器 */
    private int blinkTimer;

    /** 闪烁持续时间（毫秒） */
    private static final int BLINK_DURATION = 800;

    /** 闪烁间隔（毫秒） */
    private static final int BLINK_INTERVAL = 100;

    /** 是否显示增加效果 */
    private boolean showIncreaseEffect;

    /** 增加数值 */
    private int increaseValue;

    /** 增加效果计时器 */
    private int increaseTimer;

    /** 增加效果持续时间（毫秒） */
    private static final int INCREASE_EFFECT_DURATION = 500;

    /** 组件宽度 */
    private int width;

    /** 组件高度 */
    private int height;

    /** 阳光图标 */
    private Image sunIcon;

    /** 是否悬停 */
    private boolean isHovering;

    /** 是否可点击（收集阳光动画用） */
    private boolean clickable;

    /** 点击监听器 */
    private SunCounterListener listener;

    /**
     * 阳光计数器点击监听器接口
     */
    public interface SunCounterListener {
        void onSunCounterClicked();
    }

    /**
     * 构造函数
     * @param width 宽度
     * @param height 高度
     */
    public SunCounter(int width, int height) {
        this.width = width;
        this.height = height;
        this.sunAmount = Constants.START_SUN;
        this.targetSunAmount = sunAmount;
        this.animationTimer = 0;
        this.isBlinking = false;
        this.blinkTimer = 0;
        this.showIncreaseEffect = false;
        this.increaseValue = 0;
        this.increaseTimer = 0;
        this.isHovering = false;
        this.clickable = true;

        setPreferredSize(new Dimension(width, height));
        setOpaque(false);

        loadIcon();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (clickable && listener != null) {
                    listener.onSunCounterClicked();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                isHovering = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovering = false;
                repaint();
            }
        });
    }

    /**
     * 加载阳光图标
     */
    private void loadIcon() {
        // 从资源管理器加载
        // sunIcon = ResourceManager.getInstance().getSunImage();

        // 临时：如果没有图片，创建默认图标
        if (sunIcon == null) {
            sunIcon = createDefaultIcon();
        }
    }

    /**
     * 创建默认图标
     */
    private Image createDefaultIcon() {
        int iconSize = Math.min(width, height) - 20;
        BufferedImage img = new BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 外圈光晕
        g.setColor(new Color(255, 255, 200, 100));
        g.fillOval(2, 2, iconSize - 4, iconSize - 4);

        // 主体
        g.setColor(new Color(255, 215, 0));
        g.fillOval(5, 5, iconSize - 10, iconSize - 10);

        // 高光
        g.setColor(new Color(255, 255, 150));
        g.fillOval(8, 8, 8, 8);

        // 光线
        g.setColor(new Color(255, 200, 0, 150));
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI * 2 / 8;
            int cx = iconSize / 2;
            int cy = iconSize / 2;
            int x1 = cx + (int)(Math.cos(angle) * 12);
            int y1 = cy + (int)(Math.sin(angle) * 12);
            int x2 = cx + (int)(Math.cos(angle) * 20);
            int y2 = cy + (int)(Math.sin(angle) * 20);
            g.drawLine(x1, y1, x2, y2);
        }

        g.dispose();
        return img;
    }

    /**
     * 设置阳光数量
     * @param amount 新数量
     */
    public void setSunAmount(int amount) {
        int oldAmount = this.sunAmount;
        this.targetSunAmount = amount;

        // 启动数值变化动画
        animationTimer = ANIMATION_DURATION;

        // 检查是否需要显示增加效果
        if (amount > oldAmount) {
            showIncreaseEffect = true;
            increaseValue = amount - oldAmount;
            increaseTimer = INCREASE_EFFECT_DURATION;
        }

        repaint();
    }

    /**
     * 增加阳光
     * @param value 增加值
     */
    public void addSun(int value) {
        setSunAmount(sunAmount + value);
    }

    /**
     * 减少阳光
     * @param value 减少值
     * @return 是否成功（阳光不足返回false）
     */
    public boolean subtractSun(int value) {
        if (sunAmount >= value) {
            setSunAmount(sunAmount - value);
            return true;
        }
        return false;
    }

    /**
     * 开始闪烁（阳光不足时）
     */
    public void startBlink() {
        isBlinking = true;
        blinkTimer = BLINK_DURATION;
        repaint();
    }

    /**
     * 停止闪烁
     */
    public void stopBlink() {
        isBlinking = false;
        blinkTimer = 0;
        repaint();
    }

    /**
     * 获取当前阳光数量
     */
    public int getSunAmount() {
        return sunAmount;
    }

    /**
     * 设置点击监听器
     */
    public void setListener(SunCounterListener listener) {
        this.listener = listener;
    }

    /**
     * 设置是否可点击
     */
    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }

    /**
     * 更新动画（每帧调用）
     * @param deltaTime 帧间隔时间（秒）
     */
    public void update(float deltaTime) {
        // 更新数值动画
        if (animationTimer > 0) {
            animationTimer -= deltaTime * 1000;
            if (animationTimer < 0) {
                animationTimer = 0;
                sunAmount = targetSunAmount;
            } else {
                // 线性插值
                float progress = 1.0f - (float) animationTimer / ANIMATION_DURATION;
                sunAmount = (int)(targetSunAmount * progress + (targetSunAmount - (targetSunAmount - sunAmount)) * (1 - progress));
            }
            repaint();
        }

        // 更新闪烁
        if (isBlinking) {
            blinkTimer -= deltaTime * 1000;
            if (blinkTimer <= 0) {
                isBlinking = false;
                repaint();
            } else {
                // 每隔 BLINK_INTERVAL 切换一次
                int blinkCycle = (int)(blinkTimer / BLINK_INTERVAL);
                if (blinkCycle % 2 == 0) {
                    repaint();
                }
            }
        }

        // 更新增加效果
        if (showIncreaseEffect) {
            increaseTimer -= deltaTime * 1000;
            if (increaseTimer <= 0) {
                showIncreaseEffect = false;
                increaseValue = 0;
                repaint();
            } else {
                repaint();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 绘制背景
        drawBackground(g2d);

        // 绘制阳光图标
        drawSunIcon(g2d);

        // 绘制阳光数字
        drawSunAmount(g2d);

        // 绘制增加效果
        if (showIncreaseEffect) {
            drawIncreaseEffect(g2d);
        }

        // 绘制悬停效果
        if (isHovering && clickable) {
            drawHoverEffect(g2d);
        }
    }

    /**
     * 绘制背景
     */
    private void drawBackground(Graphics2D g) {
        // 半透明背景
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRoundRect(0, 0, width, height, 15, 15);

        // 边框
        if (isBlinking) {
            // 闪烁时边框变红
            g.setColor(new Color(255, 100, 100));
        } else {
            g.setColor(new Color(255, 215, 0, 150));
        }
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(1, 1, width - 3, height - 3, 15, 15);
    }

    /**
     * 绘制阳光图标
     */
    private void drawSunIcon(Graphics2D g) {
        int iconSize = Math.min(width, height) - 20;
        int iconX = 10;
        int iconY = (height - iconSize) / 2;

        if (sunIcon != null) {
            g.drawImage(sunIcon, iconX, iconY, iconSize, iconSize, null);
        } else {
            // 默认绘制
            g.setColor(new Color(255, 215, 0));
            g.fillOval(iconX + 5, iconY + 5, iconSize - 10, iconSize - 10);
        }
    }

    /**
     * 绘制阳光数字
     */
    private void drawSunAmount(Graphics2D g) {
        String amountText = String.valueOf(sunAmount);

        // 根据动画进度调整字体大小
        float fontSize = 24;
        if (animationTimer > 0) {
            float progress = 1.0f - (float) animationTimer / ANIMATION_DURATION;
            fontSize = 24 + 4 * (1 - progress);
        }

        g.setFont(new Font("Arial", Font.BOLD, (int)fontSize));
        FontMetrics fm = g.getFontMetrics();

        int textX = width - fm.stringWidth(amountText) - 15;
        int textY = (height + fm.getAscent() - fm.getDescent()) / 2;

        // 阴影
        g.setColor(new Color(0, 0, 0, 100));
        g.drawString(amountText, textX + 2, textY + 2);

        // 文字颜色（闪烁时变红）
        if (isBlinking) {
            g.setColor(new Color(255, 100, 100));
        } else {
            g.setColor(Color.YELLOW);
        }
        g.drawString(amountText, textX, textY);
    }

    /**
     * 绘制增加效果
     */
    private void drawIncreaseEffect(Graphics2D g) {
        float progress = 1.0f - (float) increaseTimer / INCREASE_EFFECT_DURATION;
        int offsetY = (int)(30 * (1 - progress));
        int alpha = (int)(255 * (1 - progress));

        g.setColor(new Color(255, 215, 0, alpha));
        g.setFont(new Font("Arial", Font.BOLD, 18));

        String text = "+" + increaseValue;
        FontMetrics fm = g.getFontMetrics();
        int textX = width - fm.stringWidth(String.valueOf(sunAmount - increaseValue)) - 15;
        int textY = (height + fm.getAscent() - fm.getDescent()) / 2 - offsetY;

        g.drawString(text, textX, textY);

        // 绘制飘浮的阳光粒子
        for (int i = 0; i < 3; i++) {
            int particleX = textX - 20 + i * 15;
            int particleY = textY - 10 - i * 5;
            g.fillOval(particleX, particleY, 4, 4);
        }
    }

    /**
     * 绘制悬停效果
     */
    private void drawHoverEffect(Graphics2D g) {
        g.setColor(new Color(255, 255, 200, 50));
        g.fillRoundRect(0, 0, width, height, 15, 15);

        // 发光效果
        g.setColor(new Color(255, 215, 0, 80));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(1, 1, width - 3, height - 3, 15, 15);
    }

    /**
     * 获取悬停提示文本
     */
    public String getTooltipText() {
        return "<html>阳光数量: " + sunAmount + "<br>点击可查看阳光详情</html>";
    }
}