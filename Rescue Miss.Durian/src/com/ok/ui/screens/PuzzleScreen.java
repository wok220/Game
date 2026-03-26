package com.ok.ui.screens;

import com.ok.data.DataManager;
import com.ok.data.PlayerProgress;
import com.ok.ui.GameFrame;
import com.ok.ui.Refreshable;
import com.ok.utils.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 拼图系统界面
 * 2x2拼图，使用鸽子兑换拼图碎片
 */
public class PuzzleScreen extends JPanel implements Refreshable {

    /** 主窗口引用 */
    private GameFrame gameFrame;

    /** 拼图碎片解锁状态 */
    private boolean[] puzzlePieces;

    /** 鸽子数量 */
    private int pigeonCount;

    /** 拼图块兑换所需鸽子数量 */
    private int[] pieceCosts;

    /** 拼图块区域 */
    private Rectangle[] pieceRects;

    /** 鸽子图标区域 */
    private Rectangle pigeonIconRect;

    /** 鸽子数量显示区域 */
    private Rectangle pigeonCountRect;

    /** 兑换按钮区域（每个拼图一个） */
    private Rectangle[] exchangeButtons;

    /** 返回按钮区域 */
    private Rectangle backButtonRect;

    /** 提示消息 */
    private String message;

    /** 消息计时器 */
    private Timer messageTimer;

    /** 拼图图片 */
    private Image puzzleCompleteImage;

    /** 拼图背景（淡色） */
    private Image puzzleBackgroundImage;

    /** 拼图块图片 */
    private Image[] puzzlePieceImages;

    /**
     * 构造函数
     * @param gameFrame 主窗口引用
     */
    public PuzzleScreen(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
        this.puzzlePieces = new boolean[Constants.PUZZLE_ROWS * Constants.PUZZLE_COLS];
        this.pieceCosts = new int[]{5, 8, 10, 15};  // 不同拼图所需鸽子不同
        this.pieceRects = new Rectangle[4];
        this.exchangeButtons = new Rectangle[4];
        this.pieceCosts = new int[]{5, 8, 10, 15};
        this.message = null;

        setLayout(null);
        setBackground(new Color(20, 20, 40));

        initRects();
        initListeners();
        loadData();
        loadImages();
    }

    /**
     * 初始化区域矩形
     */
    private void initRects() {
        int centerX = Constants.WINDOW_WIDTH / 2;
        int startY = 150;
        int pieceWidth = Constants.PUZZLE_PIECE_WIDTH;
        int pieceHeight = Constants.PUZZLE_PIECE_HEIGHT;

        // 返回按钮
        backButtonRect = new Rectangle(20, 20, 80, 35);

        // 拼图区域 (2x2)
        int puzzleStartX = centerX - pieceWidth;
        int puzzleStartY = startY;

        pieceRects[0] = new Rectangle(puzzleStartX, puzzleStartY, pieceWidth, pieceHeight);
        pieceRects[1] = new Rectangle(puzzleStartX + pieceWidth, puzzleStartY, pieceWidth, pieceHeight);
        pieceRects[2] = new Rectangle(puzzleStartX, puzzleStartY + pieceHeight, pieceWidth, pieceHeight);
        pieceRects[3] = new Rectangle(puzzleStartX + pieceWidth, puzzleStartY + pieceHeight, pieceWidth, pieceHeight);

        // 兑换按钮（每个拼图下方）
        int buttonWidth = 80;
        int buttonHeight = 30;
        for (int i = 0; i < 4; i++) {
            int buttonX = pieceRects[i].x + (pieceWidth - buttonWidth) / 2;
            int buttonY = pieceRects[i].y + pieceHeight + 10;
            exchangeButtons[i] = new Rectangle(buttonX, buttonY, buttonWidth, buttonHeight);
        }

        // 鸽子图标和数量区域
        pigeonIconRect = new Rectangle(centerX - 100, Constants.WINDOW_HEIGHT - 100, 40, 40);
        pigeonCountRect = new Rectangle(centerX - 50, Constants.WINDOW_HEIGHT - 90, 150, 30);
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
        });
    }

    /**
     * 加载玩家数据
     */
    private void loadData() {
        PlayerProgress progress = DataManager.getInstance().getProvider().getProgress();
        if (progress != null) {
            boolean[] pieces = progress.getPuzzlePieces();
            for (int i = 0; i < 4 && i < pieces.length; i++) {
                puzzlePieces[i] = pieces[i];
            }
            pigeonCount = progress.getPigeonCount();
        }
    }

    /**
     * 加载拼图图片
     */
    private void loadImages() {
        // 从资源管理器加载图片
        // puzzleCompleteImage = ResourceManager.getInstance().getPuzzleBackgroundImage();
        // puzzleBackgroundImage = ResourceManager.getInstance().getPuzzleBackgroundImage();
        // for (int i = 0; i < 4; i++) {
        //     puzzlePieceImages[i] = ResourceManager.getInstance().getPuzzlePieceImage(i);
        // }

        // 临时：创建默认图片
        if (puzzleCompleteImage == null) {
            puzzleCompleteImage = createDefaultPuzzleImage();
        }
        if (puzzleBackgroundImage == null) {
            puzzleBackgroundImage = createDefaultPuzzleBackground();
        }
        if (puzzlePieceImages == null) {
            puzzlePieceImages = new Image[4];
            for (int i = 0; i < 4; i++) {
                puzzlePieceImages[i] = createDefaultPieceImage(i);
            }
        }
    }

    /**
     * 创建默认完整拼图图片
     */
    private Image createDefaultPuzzleImage() {
        int width = Constants.PUZZLE_PIECE_WIDTH * 2;
        int height = Constants.PUZZLE_PIECE_HEIGHT * 2;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // 绘制开朗榴莲头轮廓
        g.setColor(new Color(255, 220, 150));
        g.fillRoundRect(20, 20, width - 40, height - 40, 30, 30);

        // 眼睛
        g.setColor(Color.WHITE);
        g.fillOval(width / 4, height / 3, 30, 30);
        g.fillOval(width * 3 / 4 - 30, height / 3, 30, 30);
        g.setColor(Color.BLACK);
        g.fillOval(width / 4 + 8, height / 3 + 8, 12, 12);
        g.fillOval(width * 3 / 4 - 22, height / 3 + 8, 12, 12);

        // 微笑
        g.setColor(new Color(100, 50, 0));
        g.setStroke(new BasicStroke(5));
        g.drawArc(width / 4, height / 2, width / 2, height / 3, 0, -180);

        // 榴莲刺
        g.setColor(new Color(80, 60, 30));
        for (int i = 0; i < 12; i++) {
            int x = 20 + (int)(Math.random() * (width - 40));
            int y = 20 + (int)(Math.random() * (height - 40));
            g.fillOval(x, y, 8, 8);
        }

        g.dispose();
        return img;
    }

    /**
     * 创建默认拼图背景（淡色）
     */
    private Image createDefaultPuzzleBackground() {
        int width = Constants.PUZZLE_PIECE_WIDTH * 2;
        int height = Constants.PUZZLE_PIECE_HEIGHT * 2;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        g.setColor(new Color(255, 240, 200, 80));
        g.fillRoundRect(20, 20, width - 40, height - 40, 30, 30);

        // 淡色轮廓
        g.setColor(new Color(200, 180, 120, 100));
        g.setStroke(new BasicStroke(3));
        g.drawRoundRect(20, 20, width - 40, height - 40, 30, 30);

        g.dispose();
        return img;
    }

    /**
     * 创建默认拼图块图片
     */
    private Image createDefaultPieceImage(int index) {
        int width = Constants.PUZZLE_PIECE_WIDTH;
        int height = Constants.PUZZLE_PIECE_HEIGHT;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // 根据索引绘制不同部分
        int offsetX = (index % 2) * width;
        int offsetY = (index / 2) * height;

        g.setColor(new Color(255, 220, 150));
        g.fillRoundRect(5, 5, width - 10, height - 10, 15, 15);

        // 绘制拼图形状边缘（凸起/凹陷效果）
        g.setColor(new Color(180, 140, 80));
        if (index == 0) {
            // 左上角
            g.fillOval(width - 15, height - 15, 15, 15);
        } else if (index == 1) {
            // 右上角
            g.fillOval(0, height - 15, 15, 15);
        } else if (index == 2) {
            // 左下角
            g.fillOval(width - 15, 0, 15, 15);
        } else {
            // 右下角
            g.fillOval(0, 0, 15, 15);
        }

        g.dispose();
        return img;
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

        // 检查兑换按钮
        for (int i = 0; i < 4; i++) {
            if (exchangeButtons[i].contains(x, y) && !puzzlePieces[i]) {
                attemptExchange(i);
                return;
            }
        }
    }

    /**
     * 尝试兑换拼图
     * @param index 拼图索引
     */
    private void attemptExchange(int index) {
        int cost = pieceCosts[index];

        if (pigeonCount >= cost) {
            // 兑换成功
            PlayerProgress progress = DataManager.getInstance().getProvider().getProgress();
            if (progress.spendPigeons(cost)) {
                progress.unlockPuzzlePiece(index);
                DataManager.getInstance().getProvider().saveProgress(progress);

                // 更新本地数据
                puzzlePieces[index] = true;
                pigeonCount = progress.getPigeonCount();

                showMessage("兑换成功！", new Color(100, 200, 100));
            } else {
                showMessage("兑换失败，请重试", new Color(200, 100, 100));
            }
        } else {
            // 鸽子不足
            showMessage("请抓获更多鸽子 🕊️", new Color(200, 100, 100));
        }

        repaint();
    }

    /**
     * 显示提示消息
     */
    private void showMessage(String msg, Color color) {
        this.message = msg;
        repaint();

        if (messageTimer != null) {
            messageTimer.cancel();
        }
        messageTimer = new Timer();
        messageTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                message = null;
                repaint();
            }
        }, 2000);
    }

    @Override
    public void refresh() {
        loadData();
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

        // 绘制拼图区域
        drawPuzzleArea(g2d);

        // 绘制鸽子信息
        drawPigeonInfo(g2d);

        // 绘制提示消息
        if (message != null) {
            drawMessage(g2d);
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
        String title = "开朗榴莲头";
        String subtitle = "拼图收集";

        // 主标题
        g.setFont(new Font("微软雅黑", Font.BOLD, 36));
        FontMetrics fm = g.getFontMetrics();
        int titleX = (getWidth() - fm.stringWidth(title)) / 2;

        g.setColor(new Color(0, 0, 0, 100));
        g.drawString(title, titleX + 2, 72);
        g.setColor(new Color(255, 215, 0));
        g.drawString(title, titleX, 70);

        // 副标题
        g.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        fm = g.getFontMetrics();
        int subX = (getWidth() - fm.stringWidth(subtitle)) / 2;
        g.setColor(new Color(200, 200, 150));
        g.drawString(subtitle, subX, 105);
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
     * 绘制拼图区域
     */
    private void drawPuzzleArea(Graphics2D g) {
        for (int i = 0; i < 4; i++) {
            Rectangle rect = pieceRects[i];

            // 绘制拼图背景（淡色）
            if (puzzleBackgroundImage != null) {
                // 绘制淡色背景
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                g.drawImage(puzzleBackgroundImage, rect.x, rect.y, rect.width, rect.height,
                        rect.x - rect.width * (i % 2), rect.y - rect.height * (i / 2),
                        rect.width, rect.height, null);
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }

            // 绘制拼图块（如果已解锁）
            if (puzzlePieces[i]) {
                if (puzzlePieceImages != null && puzzlePieceImages[i] != null) {
                    g.drawImage(puzzlePieceImages[i], rect.x, rect.y, rect.width, rect.height, null);
                } else {
                    // 默认绘制
                    drawDefaultPiece(g, rect, i);
                }
            } else {
                // 未解锁：绘制锁定效果
                g.setColor(new Color(0, 0, 0, 150));
                g.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 15, 15);

                // 问号
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 48));
                FontMetrics fm = g.getFontMetrics();
                String question = "?";
                int textX = rect.x + (rect.width - fm.stringWidth(question)) / 2;
                int textY = rect.y + (rect.height + fm.getAscent() - fm.getDescent()) / 2;
                g.drawString(question, textX, textY);
            }

            // 绘制边框
            g.setColor(new Color(120, 100, 80));
            g.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 15, 15);

            // 绘制兑换按钮
            drawExchangeButton(g, i);
        }
    }

    /**
     * 绘制默认拼图块
     */
    private void drawDefaultPiece(Graphics2D g, Rectangle rect, int index) {
        g.setColor(new Color(255, 220, 150));
        g.fillRoundRect(rect.x + 5, rect.y + 5, rect.width - 10, rect.height - 10, 15, 15);

        // 根据索引绘制简单图案
        g.setColor(new Color(100, 70, 40));
        g.setFont(new Font("Arial", Font.BOLD, 24));
        String text = String.valueOf(index + 1);
        FontMetrics fm = g.getFontMetrics();
        int textX = rect.x + (rect.width - fm.stringWidth(text)) / 2;
        int textY = rect.y + (rect.height + fm.getAscent() - fm.getDescent()) / 2;
        g.drawString(text, textX, textY);
    }

    /**
     * 绘制兑换按钮
     */
    private void drawExchangeButton(Graphics2D g, int index) {
        Rectangle rect = exchangeButtons[index];

        if (puzzlePieces[index]) {
            // 已解锁：显示"已解锁"
            g.setColor(new Color(80, 100, 80));
            g.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 8, 8);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 10));
            g.drawString("已解锁", rect.x + 20, rect.y + 20);
        } else {
            // 未解锁：显示兑换按钮
            boolean canAfford = pigeonCount >= pieceCosts[index];
            if (canAfford) {
                g.setColor(new Color(100, 150, 100));
            } else {
                g.setColor(new Color(100, 80, 80));
            }
            g.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 8, 8);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            String text = "兑换 🕊️" + pieceCosts[index];
            FontMetrics fm = g.getFontMetrics();
            int textX = rect.x + (rect.width - fm.stringWidth(text)) / 2;
            int textY = rect.y + (rect.height + fm.getAscent() - fm.getDescent()) / 2;
            g.drawString(text, textX, textY);
        }

        // 边框
        g.setColor(new Color(60, 50, 40));
        g.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 8, 8);
    }

    /**
     * 绘制鸽子信息
     */
    private void drawPigeonInfo(Graphics2D g) {
        // 鸽子图标背景
        g.setColor(new Color(80, 70, 100));
        g.fillRoundRect(pigeonIconRect.x, pigeonIconRect.y, pigeonIconRect.width, pigeonIconRect.height, 10, 10);

        // 绘制鸽子图标
        g.setColor(new Color(200, 200, 220));
        g.fillOval(pigeonIconRect.x + 10, pigeonIconRect.y + 10, 20, 20);
        g.fillOval(pigeonIconRect.x + 15, pigeonIconRect.y + 5, 10, 10);

        // 鸽子数量
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.setColor(new Color(255, 215, 0));
        String countText = "x " + pigeonCount;
        FontMetrics fm = g.getFontMetrics();
        int textX = pigeonCountRect.x;
        int textY = pigeonCountRect.y + (pigeonCountRect.height + fm.getAscent() - fm.getDescent()) / 2;
        g.drawString(countText, textX, textY);

        // 边框
        g.setColor(new Color(120, 100, 80));
        g.drawRoundRect(pigeonIconRect.x, pigeonIconRect.y, pigeonIconRect.width, pigeonIconRect.height, 10, 10);
        g.drawRoundRect(pigeonCountRect.x, pigeonCountRect.y, pigeonCountRect.width, pigeonCountRect.height, 10, 10);
    }

    /**
     * 绘制提示消息
     */
    private void drawMessage(Graphics2D g) {
        // 半透明背景
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(Constants.WINDOW_WIDTH / 2 - 150, Constants.WINDOW_HEIGHT / 2 - 40, 300, 60, 20, 20);

        // 消息文字
        g.setFont(new Font("微软雅黑", Font.BOLD, 16));
        FontMetrics fm = g.getFontMetrics();
        int textX = (Constants.WINDOW_WIDTH - fm.stringWidth(message)) / 2;
        int textY = Constants.WINDOW_HEIGHT / 2 - 10;

        g.setColor(Color.WHITE);
        g.drawString(message, textX, textY);
    }
}