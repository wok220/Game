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
import java.awt.event.MouseMotionAdapter;

/**
 * 拼图系统界面
 * 显示和处理拼图游戏
 */
public class PuzzleScreen extends JPanel implements Refreshable {

    /** 主窗口引用 */
    private GameFrame gameFrame;

    /** 拼图块数量 */
    private static final int PUZZLE_PIECE_COUNT = 4;

    /** 拼图块区域 */
    private Rectangle[] puzzlePieceRects;

    /** 拼图背景区域 */
    private Rectangle puzzleBackgroundRect;

    /** 关闭按钮区域 */
    private Rectangle closeButtonRect;

    /** 选中的拼图块 */
    private int selectedPiece;

    /** 拼图块是否已解锁 */
    private boolean[] puzzlePiecesUnlocked;

    /**
     * 构造函数
     * @param gameFrame 主窗口引用
     */
    public PuzzleScreen(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
        this.selectedPiece = -1;
        this.puzzlePiecesUnlocked = new boolean[PUZZLE_PIECE_COUNT];

        setLayout(null);
        setBackground(new Color(30, 30, 40));

        loadPuzzleProgress();
        initUI();
        initListeners();
    }

    /**
     * 加载拼图进度
     */
    private void loadPuzzleProgress() {
        PlayerProgress progress = DataManager.getInstance().getProvider().getProgress();
        if (progress != null) {
            boolean[] pieces = progress.getPuzzlePieces();
            if (pieces != null && pieces.length == PUZZLE_PIECE_COUNT) {
                puzzlePiecesUnlocked = pieces;
            }
        }
    }

    /**
     * 初始化UI组件
     */
    private void initUI() {
        // 拼图背景区域
        int puzzleWidth = 400;
        int puzzleHeight = 300;
        int puzzleX = (Constants.WINDOW_WIDTH - puzzleWidth) / 2;
        int puzzleY = 150;
        puzzleBackgroundRect = new Rectangle(puzzleX, puzzleY, puzzleWidth, puzzleHeight);

        // 拼图块区域
        puzzlePieceRects = new Rectangle[PUZZLE_PIECE_COUNT];
        int pieceWidth = puzzleWidth / 2;
        int pieceHeight = puzzleHeight / 2;

        for (int i = 0; i < PUZZLE_PIECE_COUNT; i++) {
            int pieceX = puzzleX + (i % 2) * pieceWidth;
            int pieceY = puzzleY + (i / 2) * pieceHeight;
            puzzlePieceRects[i] = new Rectangle(pieceX, pieceY, pieceWidth, pieceHeight);
        }

        // 关闭按钮
        closeButtonRect = new Rectangle(Constants.WINDOW_WIDTH - 50, 20, 30, 30);
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
                handleDrag(e.getX(), e.getY());
            }
        });
    }

    /**
     * 处理点击事件
     * @param x 鼠标X坐标
     * @param y 鼠标Y坐标
     */
    private void handleClick(int x, int y) {
        // 检查关闭按钮
        if (closeButtonRect.contains(x, y)) {
            gameFrame.showMainScreen();
            return;
        }

        // 检查拼图块
        for (int i = 0; i < PUZZLE_PIECE_COUNT; i++) {
            if (puzzlePieceRects[i].contains(x, y)) {
                if (puzzlePiecesUnlocked[i]) {
                    // 拼图块已解锁，选中它
                    selectedPiece = i;
                    repaint();
                } else {
                    // 拼图块未解锁，提示用户
                    JOptionPane.showMessageDialog(this,
                            "该拼图块尚未解锁",
                            "提示",
                            JOptionPane.INFORMATION_MESSAGE);
                }
                return;
            }
        }
    }

    /**
     * 处理按下事件
     * @param x 鼠标X坐标
     * @param y 鼠标Y坐标
     */
    private void handlePress(int x, int y) {
        // 预留：可以添加拖拽开始的逻辑
    }

    /**
     * 处理释放事件
     */
    private void handleRelease() {
        // 预留：可以添加拖拽结束的逻辑
    }

    /**
     * 处理悬停事件
     * @param x 鼠标X坐标
     * @param y 鼠标Y坐标
     */
    private void handleHover(int x, int y) {
        // 预留：可以添加悬停效果
    }

    /**
     * 处理拖拽事件
     * @param x 鼠标X坐标
     * @param y 鼠标Y坐标
     */
    private void handleDrag(int x, int y) {
        // 预留：可以添加拖拽逻辑
    }

    @Override
    public void refresh() {
        loadPuzzleProgress();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 半透明背景
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // 标题
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 24));
        g2d.setColor(Color.WHITE);
        String title = "拼图系统";
        FontMetrics fm = g2d.getFontMetrics();
        int titleX = (getWidth() - fm.stringWidth(title)) / 2;
        g2d.drawString(title, titleX, 80);

        // 绘制拼图背景
        drawPuzzleBackground(g2d);

        // 绘制拼图块
        drawPuzzlePieces(g2d);

        // 绘制关闭按钮
        drawCloseButton(g2d);
    }

    /**
     * 绘制拼图背景
     */
    private void drawPuzzleBackground(Graphics2D g) {
        g.setColor(new Color(50, 50, 70));
        g.fillRoundRect(puzzleBackgroundRect.x, puzzleBackgroundRect.y,
                puzzleBackgroundRect.width, puzzleBackgroundRect.height, 15, 15);
        g.setColor(Color.WHITE);
        g.drawRoundRect(puzzleBackgroundRect.x, puzzleBackgroundRect.y,
                puzzleBackgroundRect.width, puzzleBackgroundRect.height, 15, 15);

        // 绘制完整拼图背景
        // TODO: 绘制完整拼图背景
    }

    /**
     * 绘制拼图块
     */
    private void drawPuzzlePieces(Graphics2D g) {
        for (int i = 0; i < PUZZLE_PIECE_COUNT; i++) {
            Rectangle rect = puzzlePieceRects[i];

            if (puzzlePiecesUnlocked[i]) {
                // 拼图块已解锁
                if (selectedPiece == i) {
                    g.setColor(new Color(100, 100, 120));
                } else {
                    g.setColor(new Color(80, 80, 100));
                }
            } else {
                // 拼图块未解锁
                g.setColor(new Color(50, 50, 60));
            }

            g.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);
            g.setColor(Color.WHITE);
            g.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);

            // 绘制拼图块内容
            // TODO: 绘制拼图块内容
            g.setFont(new Font("Arial", Font.PLAIN, 16));
            g.drawString("拼图 " + (i + 1), rect.x + 20, rect.y + 40);

            if (!puzzlePiecesUnlocked[i]) {
                g.drawString("未解锁", rect.x + 20, rect.y + 70);
            }
        }
    }

    /**
     * 绘制关闭按钮
     */
    private void drawCloseButton(Graphics2D g) {
        g.setColor(new Color(200, 100, 100));
        g.fillRoundRect(closeButtonRect.x, closeButtonRect.y, closeButtonRect.width, closeButtonRect.height, 8, 8);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("X", closeButtonRect.x + 10, closeButtonRect.y + 22);
    }
}