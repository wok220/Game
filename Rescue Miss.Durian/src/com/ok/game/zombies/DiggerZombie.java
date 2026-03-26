package com.ok.game.zombies;

import com.ok.game.entities.Plant;
import com.ok.game.entities.Zombie;
import com.ok.game.systems.SunSystem;
import com.ok.utils.Constants;

import java.awt.*;
import java.util.Random;

/**
 * 矿工僵尸
 * 从地底挖隧道，从后方出现，然后转身向前走
 */
public class DiggerZombie extends Zombie {

    /** 随机数生成器（用于掉落阳光） */
    private static Random random = new Random();

    /** 阳光掉落概率（百分比） */
    private static final int SUN_DROP_CHANCE = 30;

    /** 矿工状态 */
    public enum DiggerState {
        DIGGING,    // 挖隧道（不可见，快速移动）
        EMERGING,   // 正在钻出地面
        WALKING     // 正常行走（从后方出现后）
    }

    /** 当前矿工状态 */
    private DiggerState diggerState;

    /** 挖掘速度（像素/秒，比普通僵尸快） */
    private int digSpeed;

    /** 挖掘深度偏移（用于视觉效果） */
    private int digOffsetY;

    /** 挖掘动画计时器 */
    private int digAnimationTimer;

    /** 挖掘动画持续时间 */
    private static final int DIG_ANIMATION_DURATION = 500;

    /** 钻出地面动画计时器 */
    private int emergeTimer;

    /** 钻出地面动画持续时间 */
    private static final int EMERGE_DURATION = 800;

    /** 是否已经到达后方 */
    private boolean hasReachedBack;

    /** 目标X坐标（后方位置，通常是网格左侧） */
    private int targetBackX;

    /** 原始X坐标（起始位置） */
    private int startX;

    /** 是否正在转身 */
    private boolean isTurning;

    /** 转身动画计时器 */
    private int turnTimer;

    /** 转身动画持续时间 */
    private static final int TURN_DURATION = 300;

    /** 挖掘时留下的土堆效果位置 */
    private int dirtMoundX;

    /**
     * 构造函数
     * @param row 所在行
     * @param x 屏幕X坐标（起始位置，屏幕右侧）
     * @param y 屏幕Y坐标
     */
    public DiggerZombie(int row, int x, int y) {
        super("DiggerZombie", row, x, y);

        this.isDigger = true;
        this.diggerState = DiggerState.DIGGING;
        this.digSpeed = this.speed * 2;  // 挖掘速度是普通僵尸的2倍
        this.speed = digSpeed;
        this.hasReachedBack = false;
        this.startX = x;
        this.targetBackX = Constants.GRID_OFFSET_X - 50;  // 网格左侧后方
        this.dirtMoundX = x;

        this.digOffsetY = 20;
        this.digAnimationTimer = 0;
        this.emergeTimer = 0;
        this.turnTimer = 0;
        this.isTurning = false;
    }

    @Override
    protected void loadImage() {
        // 从资源管理器加载图片
        // this.image = ResourceManager.getInstance().getZombieImage("DiggerZombie");

        // 临时：如果没有图片资源，使用默认绘制（由父类处理）
    }

    @Override
    public void update(float deltaTime) {
        if (state == ZombieState.DYING) {
            updateDying(deltaTime);
            return;
        }

        // 更新动画计时器
        updateAnimations(deltaTime);

        // 根据状态更新
        switch (diggerState) {
            case DIGGING:
                updateDigging(deltaTime);
                break;
            case EMERGING:
                updateEmerging(deltaTime);
                break;
            case WALKING:
                updateWalking(deltaTime);
                break;
        }

        // 更新闪烁效果
        updateBlink(deltaTime);

        // 更新攻击状态
        if (state == ZombieState.ATTACKING || state == ZombieState.EATING) {
            updateAttacking(deltaTime);
        }
    }

    /**
     * 更新动画效果
     */
    private void updateAnimations(float deltaTime) {
        if (digAnimationTimer > 0) {
            digAnimationTimer -= deltaTime * 1000;
        }

        if (emergeTimer > 0) {
            emergeTimer -= deltaTime * 1000;
        }

        if (turnTimer > 0) {
            turnTimer -= deltaTime * 1000;
            if (turnTimer <= 0) {
                isTurning = false;
                turnTimer = 0;
            }
        }
    }

    /**
     * 更新挖掘状态（地下移动）
     */
    private void updateDigging(float deltaTime) {
        // 快速向左移动（地下）
        x -= digSpeed * deltaTime;
        dirtMoundX = x;

        // 挖掘动画
        if (digAnimationTimer <= 0) {
            digAnimationTimer = DIG_ANIMATION_DURATION;
        }

        // 检查是否到达后方
        if (x <= targetBackX && !hasReachedBack) {
            hasReachedBack = true;
            // 开始钻出地面
            diggerState = DiggerState.EMERGING;
            emergeTimer = EMERGE_DURATION;
            speed = this.speed / 2;  // 钻出时速度变慢
        }
    }

    /**
     * 更新钻出地面状态
     */
    private void updateEmerging(float deltaTime) {
        // 逐渐减少深度偏移
        float progress = 1.0f - (float) emergeTimer / EMERGE_DURATION;
        digOffsetY = (int)(20 * (1 - progress));

        // 钻出完成
        if (emergeTimer <= 0) {
            diggerState = DiggerState.WALKING;
            digOffsetY = 0;

            // 开始转身
            isTurning = true;
            turnTimer = TURN_DURATION;

            // 恢复移动速度
            speed = Constants.ZOMBIE_WALK_SPEED;
        }
    }

    @Override
    protected void updateWalking(float deltaTime) {
        // 转身动画期间不移动
        if (isTurning) {
            return;
        }

        // 从后方出现后，向右移动（向房子方向）
        x += speed * deltaTime;

        // 检查是否到达房子（现在是从左边向右移动，房子在左侧）
        if (x >= Constants.WINDOW_WIDTH) {
            onReachHouse();
        }
    }

    @Override
    public void setTargetPlant(Plant plant) {
        // 只有在行走状态下才能攻击植物
        if (diggerState == DiggerState.WALKING && !isTurning) {
            super.setTargetPlant(plant);
        }
    }

    @Override
    public void takeDamage(int damage) {
        // 挖掘状态和钻出状态下的僵尸不能被攻击（在地下）
        if (diggerState == DiggerState.DIGGING || diggerState == DiggerState.EMERGING) {
            return;
        }

        // 行走状态下正常受伤害
        super.takeDamage(damage);
    }

    @Override
    protected void maybeDropSun() {
        // 30%概率掉落阳光
        if (random.nextInt(100) < SUN_DROP_CHANCE) {
            SunSystem sunSystem = SunSystem.getInstance();
            int sunX = x + width / 2;
            int sunY = y + height / 2;
            sunSystem.createDropSun(sunX, sunY, Constants.SUN_PRODUCE_AMOUNT);
        }
    }

    @Override
    protected void onDeath() {
        super.onDeath();
        // AudioManager.getInstance().playSound("zombie_die");
    }

    // ==================== 渲染 ====================

    @Override
    public void render(Graphics2D g) {
        if (!visible) return;

        // 根据状态渲染
        switch (diggerState) {
            case DIGGING:
                renderDigging(g);
                break;
            case EMERGING:
                renderEmerging(g);
                break;
            case WALKING:
                renderWalking(g);
                break;
        }
    }

    /**
     * 渲染挖掘状态（地下）
     */
    private void renderDigging(Graphics2D g) {
        // 绘制土堆效果
        float progress = 1.0f - (float) digAnimationTimer / DIG_ANIMATION_DURATION;
        int moundSize = (int)(20 + 10 * progress);

        g.setColor(new Color(101, 67, 33));
        g.fillOval(dirtMoundX, y + height - 15, moundSize, 12);

        // 绘制挖掘痕迹
        g.setColor(new Color(139, 69, 19, 100));
        for (int i = 0; i < 5; i++) {
            int trailX = dirtMoundX + i * 15;
            g.fillOval(trailX, y + height - 10, 8, 8);
        }

        // 绘制铁锹头部（露出地面）
        g.setColor(new Color(100, 100, 120));
        g.fillRect(dirtMoundX + 15, y + height - 20, 8, 15);
        g.setColor(new Color(80, 60, 40));
        g.fillRect(dirtMoundX + 20, y + height - 25, 3, 12);

        // 绘制"挖"的粒子效果
        if (digAnimationTimer > 0) {
            g.setColor(new Color(139, 69, 19, 150));
            for (int i = 0; i < 3; i++) {
                int px = dirtMoundX + 10 + i * 8;
                int py = y + height - 10 + (int)(Math.random() * 10);
                g.fillOval(px, py, 3, 3);
            }
        }
    }

    /**
     * 渲染钻出地面状态
     */
    private void renderEmerging(Graphics2D g) {
        float progress = 1.0f - (float) emergeTimer / EMERGE_DURATION;
        int visibleHeight = (int)(height * progress);
        int drawY = y + height - visibleHeight;

        // 绘制僵尸身体（逐渐出现）
        if (image != null) {
            g.drawImage(image, x, drawY, width, visibleHeight, null);
        } else {
            drawZombieBody(g, drawY, visibleHeight);
        }

        // 绘制土堆效果
        g.setColor(new Color(101, 67, 33));
        g.fillOval(x - 10, y + height - 15, width + 20, 20);

        // 绘制灰尘粒子
        g.setColor(new Color(160, 100, 50, 100));
        for (int i = 0; i < 5; i++) {
            int px = x + (int)(Math.random() * width);
            int py = y + height - 10 + (int)(Math.random() * 15);
            g.fillOval(px, py, 4, 4);
        }
    }

    /**
     * 渲染行走状态
     */
    private void renderWalking(Graphics2D g) {
        if (image != null) {
            // 转身动画期间，镜像绘制
            if (isTurning) {
                float progress = 1.0f - (float) turnTimer / TURN_DURATION;
                int width = this.width;
                int height = this.height;

                // 绘制残影效果
                for (int i = 0; i < 3; i++) {
                    float alpha = 0.3f * (1 - i * 0.3f);
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                    g.drawImage(image, x + i * 5, y, width, height, null);
                }
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }

            g.drawImage(image, x, y, width, height, null);
        } else {
            drawZombieBody(g, y, height);
        }

        // 绘制血条
        drawHealthBar(g);
    }

    /**
     * 绘制僵尸身体（默认图形）
     */
    private void drawZombieBody(Graphics2D g, int drawY, int drawHeight) {
        // 身体
        g.setColor(new Color(70, 90, 50));
        g.fillRect(x + 15, drawY + 20, width - 30, drawHeight - 30);

        // 头部
        g.setColor(new Color(90, 110, 60));
        g.fillOval(x + 20, drawY + 5, width - 40, drawHeight - 40);

        // 眼睛
        g.setColor(Color.WHITE);
        g.fillOval(x + 25, drawY + 18, 8, 8);
        g.fillOval(x + 45, drawY + 18, 8, 8);
        g.setColor(Color.BLACK);
        g.fillOval(x + 27, drawY + 20, 4, 4);
        g.fillOval(x + 47, drawY + 20, 4, 4);

        // 嘴巴
        g.drawArc(x + 32, drawY + 30, 16, 8, 0, -180);

        // 矿工帽
        g.setColor(new Color(100, 80, 50));
        g.fillArc(x + 20, drawY - 5, width - 40, 15, 0, 180);
        g.setColor(new Color(255, 200, 100));
        g.fillOval(x + width / 2 - 3, drawY - 3, 6, 6);

        // 手臂（持锹姿势）
        g.setColor(new Color(60, 80, 40));
        g.fillRect(x + 5, drawY + 35, 15, 12);
        g.fillRect(x + width - 20, drawY + 35, 15, 12);

        // 铁锹
        g.setColor(new Color(100, 100, 120));
        g.fillRect(x + width - 25, drawY + 40, 8, 20);
        g.setColor(new Color(80, 60, 40));
        g.fillRect(x + width - 22, drawY + 35, 3, 12);
    }

    @Override
    protected void drawHealthBar(Graphics2D g) {
        // 只有行走状态才显示血条
        if (diggerState == DiggerState.WALKING) {
            super.drawHealthBar(g);
        }
    }

    // ==================== Getters ====================

    /**
     * 获取矿工状态
     */
    public DiggerState getDiggerState() {
        return diggerState;
    }

    /**
     * 是否正在挖掘
     */
    public boolean isDigging() {
        return diggerState == DiggerState.DIGGING;
    }

    /**
     * 是否正在钻出
     */
    public boolean isEmerging() {
        return diggerState == DiggerState.EMERGING;
    }

    /**
     * 是否正在行走
     */
    public boolean isWalking() {
        return diggerState == DiggerState.WALKING;
    }

    /**
     * 是否正在转身
     */
    public boolean isTurning() {
        return isTurning;
    }

    /**
     * 获取挖掘进度
     */
    public float getDigProgress() {
        if (startX <= targetBackX) return 1.0f;
        return 1.0f - (float) (x - targetBackX) / (startX - targetBackX);
    }

    /**
     * 获取钻出进度
     */
    public float getEmergeProgress() {
        return 1.0f - (float) emergeTimer / EMERGE_DURATION;
    }
}