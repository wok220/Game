package com.ok.ui.components;

import com.ok.data.DataManager;
import com.ok.data.GameConfig;
import com.ok.data.PlayerProgress;
import com.ok.game.systems.PlantSelector;
import com.ok.utils.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * 植物卡片组件
 * 显示植物图标、名称、阳光消耗、冷却状态等
 */
public class PlantCard extends JPanel {

    /** 植物ID */
    private String plantId;

    /** 植物配置 */
    private GameConfig.PlantConfig plantConfig;

    /** 是否已解锁 */
    private boolean unlocked;

    /** 当前阳光数量（从外部传入） */
    private int currentSun;

    /** 是否在冷却中 */
    private boolean onCooldown;

    /** 冷却进度（0-1） */
    private float cooldownProgress;

    /** 是否被选中（种植模式） */
    private boolean isSelected;

    /** 鼠标是否悬停 */
    private boolean isHovering;

    /** 卡片宽度 */
    private int cardWidth;

    /** 卡片高度 */
    private int cardHeight;

    /** 植物图标 */
    private Image plantImage;

    /** 卡片背景颜色 */
    private Color bgColor;

    /** 卡片边框颜色 */
    private Color borderColor;

    /** 文字颜色 */
    private Color textColor;

    /** 是否可点击 */
    private boolean clickable;

    /** 点击监听器 */
    private PlantCardListener listener;

    /**
     * 卡片点击监听器接口
     */
    public interface PlantCardListener {
        void onCardClick(String plantId);
    }

    /**
     * 构造函数
     * @param plantId 植物ID
     */
    public PlantCard(String plantId) {
        this.plantId = plantId;
        this.cardWidth = Constants.PLANT_CARD_WIDTH;
        this.cardHeight = Constants.PLANT_CARD_HEIGHT;

        // 获取植物配置
        this.plantConfig = GameConfig.getInstance().getPlantConfig(plantId);

        // 检查是否解锁
        PlayerProgress progress = DataManager.getInstance().getProvider().getProgress();
        this.unlocked = progress.isPlantUnlocked(plantId);

        // 初始化状态
        this.onCooldown = false;
        this.cooldownProgress = 1.0f;
        this.isSelected = false;
        this.isHovering = false;
        this.clickable = true;
        this.currentSun = Constants.START_SUN;

        // 设置面板属性
        setPreferredSize(new Dimension(cardWidth, cardHeight));
        setOpaque(false);

        // 加载图片
        loadImage();

        // 添加鼠标监听
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (clickable && isEnabled() && listener != null) {
                    listener.onCardClick(plantId);
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
     * 加载植物图片
     */
    private void loadImage() {
        // 从资源管理器加载
        // plantImage = ResourceManager.getInstance().getPlantCardImage(plantId);

        // 临时：如果没有图片，创建默认图标
        if (plantImage == null) {
            plantImage = createDefaultImage();
        }
    }

    /**
     * 创建默认图片
     */
    private Image createDefaultImage() {
        BufferedImage img = new BufferedImage(cardWidth - 20, cardWidth - 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // 绘制植物轮廓
        g.setColor(new Color(100, 150, 100));
        g.fillOval(10, 10, cardWidth - 40, cardWidth - 40);

        // 绘制叶子
        g.setColor(new Color(50, 100, 50));
        g.fillOval(5, 15, 15, 10);
        g.fillOval(cardWidth - 40, 15, 15, 10);

        // 绘制眼睛
        g.setColor(Color.WHITE);
        g.fillOval(20, 25, 10, 10);
        g.fillOval(cardWidth - 50, 25, 10, 10);
        g.setColor(Color.BLACK);
        g.fillOval(22, 27, 4, 4);
        g.fillOval(cardWidth - 48, 27, 4, 4);

        g.dispose();
        return img;
    }

    /**
     * 更新卡片状态
     * @param currentSun 当前阳光数量
     * @param onCooldown 是否在冷却中
     * @param cooldownProgress 冷却进度
     * @param isSelected 是否被选中
     */
    public void updateState(int currentSun, boolean onCooldown, float cooldownProgress, boolean isSelected) {
        this.currentSun = currentSun;
        this.onCooldown = onCooldown;
        this.cooldownProgress = cooldownProgress;
        this.isSelected = isSelected;
        repaint();
    }

    /**
     * 设置点击监听器
     */
    public void setListener(PlantCardListener listener) {
        this.listener = listener;
    }

    /**
     * 设置是否可点击
     */
    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }

    /**
     * 获取植物ID
     */
    public String getPlantId() {
        return plantId;
    }

    /**
     * 获取阳光消耗
     */
    public int getSunCost() {
        return plantConfig != null ? plantConfig.getSunCost() : 0;
    }

    /**
     * 是否已解锁
     */
    public boolean isUnlocked() {
        return unlocked;
    }

    /**
     * 刷新解锁状态
     */
    public void refreshUnlockStatus() {
        PlayerProgress progress = DataManager.getInstance().getProvider().getProgress();
        this.unlocked = progress.isPlantUnlocked(plantId);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 确定卡片颜色
        determineColors();

        // 绘制背景
        drawBackground(g2d);

        // 绘制冷却遮罩
        if (onCooldown) {
            drawCooldownOverlay(g2d);
        }

        // 绘制植物图标
        drawPlantIcon(g2d);

        // 绘制阳光消耗
        drawSunCost(g2d);

        // 绘制名称
        drawPlantName(g2d);

        // 绘制选中边框
        if (isSelected) {
            drawSelectedBorder(g2d);
        }

        // 绘制悬停边框
        if (isHovering && clickable && !onCooldown && unlocked) {
            drawHoverBorder(g2d);
        }

        // 绘制未解锁遮罩
        if (!unlocked) {
            drawLockedOverlay(g2d);
        }

        // 绘制阳光不足遮罩
        if (unlocked && !onCooldown && currentSun < getSunCost()) {
            drawInsufficientOverlay(g2d);
        }
    }

    /**
     * 确定卡片颜色
     */
    private void determineColors() {
        if (!unlocked) {
            bgColor = new Color(80, 80, 80);
            borderColor = new Color(50, 50, 50);
            textColor = new Color(120, 120, 120);
        } else if (onCooldown) {
            bgColor = Constants.CARD_COOLDOWN_COLOR;
            borderColor = new Color(60, 60, 60);
            textColor = new Color(150, 150, 150);
        } else if (currentSun < getSunCost()) {
            bgColor = Constants.CARD_INSUFFICIENT_COLOR;
            borderColor = new Color(100, 100, 100);
            textColor = new Color(150, 150, 150);
        } else {
            bgColor = Constants.CARD_NORMAL_COLOR;
            borderColor = new Color(180, 180, 100);
            textColor = Color.BLACK;
        }
    }

    /**
     * 绘制背景
     */
    private void drawBackground(Graphics2D g) {
        // 背景
        g.setColor(bgColor);
        g.fillRoundRect(0, 0, cardWidth, cardHeight, 10, 10);

        // 边框
        g.setColor(borderColor);
        g.drawRoundRect(0, 0, cardWidth, cardHeight, 10, 10);

        // 阴影效果
        g.setColor(new Color(0, 0, 0, 30));
        g.fillRoundRect(2, 2, cardWidth, cardHeight, 10, 10);
    }

    /**
     * 绘制植物图标
     */
    private void drawPlantIcon(Graphics2D g) {
        int iconSize = cardWidth - 30;
        int iconX = (cardWidth - iconSize) / 2;
        int iconY = 8;

        if (plantImage != null) {
            g.drawImage(plantImage, iconX, iconY, iconSize, iconSize, null);
        } else {
            // 占位符
            g.setColor(new Color(100, 150, 100));
            g.fillOval(iconX + 5, iconY + 5, iconSize - 10, iconSize - 10);
        }
    }

    /**
     * 绘制阳光消耗
     */
    private void drawSunCost(Graphics2D g) {
        int sunCost = getSunCost();
        if (sunCost <= 0) return;

        // 阳光背景
        g.setColor(new Color(255, 200, 50));
        g.fillOval(5, cardHeight - 25, 25, 25);
        g.setColor(new Color(200, 100, 0));
        g.drawOval(5, cardHeight - 25, 25, 25);

        // 阳光数字
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        String sunText = String.valueOf(sunCost);
        FontMetrics fm = g.getFontMetrics();
        int textX = 5 + (25 - fm.stringWidth(sunText)) / 2;
        int textY = cardHeight - 25 + (25 + fm.getAscent() - fm.getDescent()) / 2;
        g.drawString(sunText, textX, textY);

        // 阳光图标
        g.setColor(Color.YELLOW);
        g.fillOval(20, cardHeight - 28, 5, 5);
    }

    /**
     * 绘制植物名称
     */
    private void drawPlantName(Graphics2D g) {
        if (plantConfig == null) return;

        String name = plantConfig.getName();
        g.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(name);
        int textX = (cardWidth - textWidth) / 2;
        int textY = cardHeight - 8;

        g.setColor(textColor);
        g.drawString(name, textX, textY);
    }

    /**
     * 绘制冷却遮罩
     */
    private void drawCooldownOverlay(Graphics2D g) {
        // 半透明遮罩
        g.setColor(new Color(80, 80, 80, 180));
        g.fillRoundRect(0, 0, cardWidth, cardHeight, 10, 10);

        // 绘制冷却进度（扇形）
        int angle = (int)(360 * (1 - cooldownProgress));
        g.setColor(new Color(100, 100, 100, 200));
        g.fillArc(5, 5, cardWidth - 10, cardHeight - 10, 90, -angle);

        // 冷却文字
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        String cdText = String.format("%.0f", (1 - cooldownProgress) * 100);
        FontMetrics fm = g.getFontMetrics();
        int textX = (cardWidth - fm.stringWidth(cdText)) / 2;
        int textY = (cardHeight + fm.getAscent() - fm.getDescent()) / 2;
        g.drawString(cdText, textX, textY);
    }

    /**
     * 绘制选中边框
     */
    private void drawSelectedBorder(Graphics2D g) {
        g.setColor(new Color(255, 200, 0));
        g.setStroke(new BasicStroke(3));
        g.drawRoundRect(2, 2, cardWidth - 5, cardHeight - 5, 10, 10);

        // 四个角的光点
        g.setColor(new Color(255, 255, 100));
        g.fillOval(5, 5, 6, 6);
        g.fillOval(cardWidth - 11, 5, 6, 6);
        g.fillOval(5, cardHeight - 11, 6, 6);
        g.fillOval(cardWidth - 11, cardHeight - 11, 6, 6);
    }

    /**
     * 绘制悬停边框
     */
    private void drawHoverBorder(Graphics2D g) {
        g.setColor(new Color(255, 255, 150));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(1, 1, cardWidth - 3, cardHeight - 3, 10, 10);
    }

    /**
     * 绘制未解锁遮罩
     */
    private void drawLockedOverlay(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(0, 0, cardWidth, cardHeight, 10, 10);

        // 锁图标
        g.setColor(Color.WHITE);
        g.fillRect(cardWidth / 2 - 8, cardHeight / 2 - 5, 16, 12);
        g.fillArc(cardWidth / 2 - 10, cardHeight / 2 - 12, 20, 14, 0, 180);

        // 问号
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        String lockText = "?";
        FontMetrics fm = g.getFontMetrics();
        int textX = (cardWidth - fm.stringWidth(lockText)) / 2;
        int textY = (cardHeight + fm.getAscent() - fm.getDescent()) / 2;
        g.drawString(lockText, textX, textY);
    }

    /**
     * 绘制阳光不足遮罩
     */
    private void drawInsufficientOverlay(Graphics2D g) {
        g.setColor(new Color(100, 50, 50, 150));
        g.fillRoundRect(0, 0, cardWidth, cardHeight, 10, 10);

        // 红字提示
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 10));
        String text = "NOT ENOUGH SUN";
        FontMetrics fm = g.getFontMetrics();
        int textX = (cardWidth - fm.stringWidth(text)) / 2;
        int textY = cardHeight - 30;
        g.drawString(text, textX, textY);
    }

    /**
     * 获取悬停提示文本
     */
    public String getTooltipText() {
        if (plantConfig == null) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("<html><b>").append(plantConfig.getName()).append("</b><br>");
        sb.append("阳光: ").append(plantConfig.getSunCost()).append("<br>");

        if (plantConfig.isAttacker()) {
            sb.append("伤害: ").append(plantConfig.getDamage()).append("<br>");
            sb.append("攻击速度: ").append(plantConfig.getAttackCooldown() / 1000).append("秒<br>");
        }

        if (plantConfig.isProducer()) {
            sb.append("生产阳光间隔: ").append(Constants.SUNFLOWER_PRODUCE_INTERVAL / 1000).append("秒<br>");
        }

        if (plantConfig.getHealth() > Constants.PLANT_DEFAULT_HEALTH) {
            sb.append("生命值: ").append(plantConfig.getHealth()).append("<br>");
        }

        if (!unlocked) {
            sb.append("<font color='red'>未解锁</font><br>");
        } else if (onCooldown) {
            int remaining = PlantSelector.getInstance().getRemainingCooldown(plantId);
            sb.append("<font color='orange'>冷却中: ").append(remaining / 1000).append("秒</font><br>");
        } else if (currentSun < plantConfig.getSunCost()) {
            sb.append("<font color='red'>阳光不足! 需要").append(plantConfig.getSunCost()).append("阳光</font><br>");
        }

        sb.append("</html>");
        return sb.toString();
    }
}