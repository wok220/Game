package com.ok.game.zombies;

import com.ok.game.entities.Plant;
import com.ok.game.entities.Zombie;
import com.ok.game.core.GameManager;
import com.ok.game.systems.SunSystem;
import com.ok.utils.Constants;

import java.awt.*;
import java.util.Random;

/**
 * 巨人僵尸
 * Boss级僵尸，拥有极高的生命值和攻击力，会投掷小鬼僵尸
 */
public class Gargantuar extends Zombie {

    /** 随机数生成器 */
    private static Random random = new Random();

    /** 阳光掉落概率（百分比） */
    private static final int SUN_DROP_CHANCE = 100;

    /** 是否已经投掷过小鬼 */
    private boolean hasThrownImp;

    /** 投掷计时器 */
    private int throwTimer;

    /** 投掷冷却时间（毫秒） */
    private static final int THROW_COOLDOWN = 5000;

    /** 是否正在投掷动画 */
    private boolean isThrowing;

    /** 投掷动画计时器 */
    private int throwAnimationTimer;

    /** 投掷动画持续时间 */
    private static final int THROW_ANIMATION_DURATION = 800;

    /** 砸地攻击动画计时器 */
    private int smashTimer;

    /** 是否正在砸地攻击 */
    private boolean isSmashing;

    /** 砸地攻击持续时间 */
    private static final int SMASH_DURATION = 600;

    /** 砸地攻击范围（像素） */
    private int smashRange;

    /** 砸地攻击伤害 */
    private int smashDamage;

    /** 武器图片（电线杆/大铁棒） */
    private boolean hasWeapon;

    /** 愤怒状态（生命值低于50%时触发） */
    private boolean isEnraged;

    /** 愤怒状态下的移动速度 */
    private int enragedSpeed;

    /** 原始移动速度 */
    private int originalSpeed;

    /** 脚步声特效计时器 */
    private int footstepTimer;

    /**
     * 构造函数
     * @param row 所在行
     * @param x 屏幕X坐标
     * @param y 屏幕Y坐标
     */
    public Gargantuar(int row, int x, int y) {
        super("Gargantuar", row, x, y);

        this.hasThrownImp = false;
        this.hasWeapon = true;
        this.isEnraged = false;
        this.isThrowing = false;
        this.isSmashing = false;

        this.throwTimer = 0;
        this.throwAnimationTimer = 0;
        this.smashTimer = 0;
        this.footstepTimer = 0;

        this.smashRange = Constants.GRID_WIDTH * 2;  // 2格范围
        this.smashDamage = 300;  // 秒杀植物

        this.originalSpeed = this.speed;
        this.enragedSpeed = (int)(originalSpeed * 1.5f);  // 愤怒时速度提升50%
    }

    @Override
    protected void loadImage() {
        // 从资源管理器加载图片
        // this.image = ResourceManager.getInstance().getZombieImage("Gargantuar");

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

        // 检查愤怒状态
        checkEnrage();

        // 更新投掷计时器
        if (!hasThrownImp && !isThrowing) {
            throwTimer += deltaTime * 1000;
            if (throwTimer >= THROW_COOLDOWN) {
                throwTimer = 0;
                startThrow();
            }
        }

        // 更新攻击状态
        if (state == ZombieState.ATTACKING || state == ZombieState.EATING) {
            updateAttacking(deltaTime);
            return;
        }

        // 行走状态
        if (state == ZombieState.WALKING) {
            updateWalking(deltaTime);
        }

        // 更新脚步声特效
        updateFootstep(deltaTime);
    }

    /**
     * 更新脚步声特效
     */
    private void updateFootstep(float deltaTime) {
        if (footstepTimer > 0) {
            footstepTimer -= deltaTime * 1000;
        }
    }
    /**
     * 更新动画计时器
     */
    private void updateAnimations(float deltaTime) {
        if (throwAnimationTimer > 0) {
            throwAnimationTimer -= deltaTime * 1000;
            if (throwAnimationTimer <= 0) {
                isThrowing = false;
                throwAnimationTimer = 0;
                onThrowComplete();
            }
        }

        if (smashTimer > 0) {
            smashTimer -= deltaTime * 1000;
            if (smashTimer <= 0) {
                isSmashing = false;
                smashTimer = 0;
            }
        }

        if (footstepTimer > 0) {
            footstepTimer -= deltaTime * 1000;
        }
    }

    /**
     * 检查愤怒状态
     */
    private void checkEnrage() {
        float healthPercent = (float) health / maxHealth;
        if (!isEnraged && healthPercent <= 0.5f) {
            isEnraged = true;
            speed = enragedSpeed;
            onEnrage();
        }
    }

    /**
     * 愤怒状态触发
     */
    private void onEnrage() {
        // 播放愤怒音效
        // AudioManager.getInstance().playSound("gargantuar_enrage");

        // 可以添加视觉特效（红色光环）
    }

    @Override
    protected void updateWalking(float deltaTime) {
        // 投掷动画或砸地动画期间不移动
        if (isThrowing || isSmashing) {
            return;
        }

        x -= speed * deltaTime;

        // 更新脚步声
        if (footstepTimer <= 0 && state == ZombieState.WALKING) {
            footstepTimer = 500;
            // AudioManager.getInstance().playSound("footstep_heavy");
        }

        if (x <= 0) {
            onReachHouse();
        }
    }

    @Override
    public void setTargetPlant(Plant plant) {
        // 如果正在投掷或砸地，不攻击
        if (isThrowing || isSmashing) {
            return;
        }

        // 检查是否在砸地范围内
        if (plant != null && isInSmashRange(plant)) {
            startSmash(plant);
            return;
        }

        super.setTargetPlant(plant);
    }

    /**
     * 检查植物是否在砸地范围内
     */
    private boolean isInSmashRange(Plant plant) {
        int plantCenterX = plant.getX() + plant.getWidth() / 2;
        int zombieCenterX = x + width / 2;
        int distance = Math.abs(plantCenterX - zombieCenterX);
        return distance <= smashRange;
    }

    /**
     * 开始砸地攻击
     */
    private void startSmash(Plant plant) {
        isSmashing = true;
        smashTimer = SMASH_DURATION;

        // 对范围内的植物造成伤害
        GameManager gm = GameManager.getInstance();
        int zombieCenterX = x + width / 2;

        for (int col = 0; col < Constants.GRID_COLS; col++) {
            Plant targetPlant = gm.getGridManager().getPlant(row, col);
            if (targetPlant != null && targetPlant.isAlive()) {
                int plantCenterX = targetPlant.getX() + targetPlant.getWidth() / 2;
                if (Math.abs(plantCenterX - zombieCenterX) <= smashRange) {
                    targetPlant.takeDamage(smashDamage);
                }
            }
        }

        // 播放砸地音效
        // AudioManager.getInstance().playSound("gargantuar_smash");

        // 清除攻击目标
        targetPlant = null;
        state = ZombieState.WALKING;
    }

    /**
     * 开始投掷小鬼
     */
    private void startThrow() {
        isThrowing = true;
        throwAnimationTimer = THROW_ANIMATION_DURATION;

        // 播放投掷音效
        // AudioManager.getInstance().playSound("gargantuar_throw");
    }

    /**
     * 投掷完成，生成小鬼僵尸
     */
    private void onThrowComplete() {
        hasThrownImp = true;

        // 生成小鬼僵尸
        GameManager gm = GameManager.getInstance();

        // 小鬼僵尸出现在前方
        int impX = x - 50;
        int impY = y;

        // 创建小鬼僵尸（需要实现ImpZombie类）
        // ImpZombie imp = new ImpZombie(row, impX, impY);
        // gm.addZombie(imp);
    }

    @Override
    public void takeDamage(int damage) {
        if (state == ZombieState.DYING) return;

        // 巨人僵尸有护甲，减少部分伤害
        int reducedDamage = (int)(damage * 0.8f);
        super.takeDamage(reducedDamage);

        // 播放受伤音效
        // AudioManager.getInstance().playSound("gargantuar_hurt");
    }

    @Override
    protected void maybeDropSun() {
        // 100%概率掉落大量阳光
        SunSystem sunSystem = SunSystem.getInstance();
        int sunX = x + width / 2;
        int sunY = y + height / 2;

        // 掉落3-5个阳光
        int sunCount = 3 + random.nextInt(3);
        for (int i = 0; i < sunCount; i++) {
            sunSystem.createDropSun(sunX + i * 10, sunY, Constants.SUN_PRODUCE_AMOUNT);
        }
    }

    @Override
    protected void onDeath() {
        super.onDeath();
        // AudioManager.getInstance().playSound("gargantuar_die");
    }

    // ==================== 渲染 ====================

    @Override
    public void render(Graphics2D g) {
        if (!visible) return;

        // 绘制巨人身体
        if (image != null) {
            g.drawImage(image, x, y, width, height, null);
        } else {
            drawGargantuarBody(g);
        }

        // 绘制武器
        if (hasWeapon && !isThrowing) {
            drawWeapon(g);
        }

        // 绘制投掷动画
        if (isThrowing) {
            drawThrowAnimation(g);
        }

        // 绘制砸地动画
        if (isSmashing) {
            drawSmashEffect(g);
        }

        // 绘制愤怒特效
        if (isEnraged) {
            drawEnrageEffect(g);
        }

        // 绘制血条
        drawHealthBar(g);
    }

    /**
     * 绘制巨人身体
     */
    private void drawGargantuarBody(Graphics2D g) {
        // 巨大身体
        g.setColor(new Color(80, 100, 70));
        g.fillRect(x + 10, y + 20, width - 20, height - 30);

        // 头部
        g.setColor(new Color(100, 120, 80));
        g.fillOval(x + 15, y + 5, width - 30, height - 25);

        // 眼睛
        g.setColor(Color.WHITE);
        g.fillOval(x + 25, y + 20, 12, 12);
        g.fillOval(x + 45, y + 20, 12, 12);
        g.setColor(Color.BLACK);
        g.fillOval(x + 28, y + 23, 6, 6);
        g.fillOval(x + 48, y + 23, 6, 6);

        // 眉毛（愤怒时）
        if (isEnraged) {
            g.setColor(Color.RED);
            g.fillRect(x + 22, y + 15, 18, 5);
            g.fillRect(x + 42, y + 15, 18, 5);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(x + 22, y + 18, 18, 3);
            g.fillRect(x + 42, y + 18, 18, 3);
        }

        // 嘴巴
        g.setColor(Color.BLACK);
        g.fillOval(x + 35, y + 35, 12, 8);
        g.fillRect(x + 35, y + 38, 12, 5);

        // 牙齿
        g.setColor(Color.WHITE);
        for (int i = 0; i < 4; i++) {
            g.fillRect(x + 37 + i * 2, y + 38, 2, 6);
        }

        // 身体伤痕
        g.setColor(new Color(60, 80, 50));
        g.fillOval(x + 35, y + 50, 15, 8);
    }

    /**
     * 绘制武器（电线杆）
     */
    private void drawWeapon(Graphics2D g) {
        int weaponX = x + width - 15;
        int weaponY = y + 30;

        // 杆身
        g.setColor(new Color(100, 80, 60));
        g.fillRect(weaponX, weaponY, 12, 50);

        // 杆头
        g.setColor(new Color(80, 60, 40));
        g.fillRect(weaponX - 5, weaponY - 5, 22, 10);

        // 电线
        g.setColor(new Color(50, 50, 80));
        g.setStroke(new BasicStroke(2));
        g.drawLine(weaponX + 6, weaponY + 10, weaponX + 6, weaponY + 40);
        g.drawLine(weaponX + 2, weaponY + 20, weaponX + 10, weaponY + 20);
    }

    /**
     * 绘制投掷动画
     */
    private void drawThrowAnimation(Graphics2D g) {
        float progress = 1.0f - (float) throwAnimationTimer / THROW_ANIMATION_DURATION;

        // 武器旋转
        int angle = (int)(360 * progress);

        // 投掷轨迹
        int startX = x + width - 20;
        int startY = y + 30;
        int endX = x - 80;
        int endY = y + 20;

        g.setColor(new Color(255, 200, 100, 150));
        for (int i = 0; i <= 10; i++) {
            float t = (float) i / 10;
            int px = (int)(startX + (endX - startX) * t * progress);
            int py = (int)(startY + (endY - startY) * t * progress - 30 * Math.sin(Math.PI * t));
            g.fillOval(px - 4, py - 4, 8, 8);
        }

        // 小鬼影子
        g.setColor(new Color(80, 80, 80, 100));
        g.fillOval(endX - 10, endY + 10, 20, 8);
    }

    /**
     * 绘制砸地效果
     */
    private void drawSmashEffect(Graphics2D g) {
        float progress = 1.0f - (float) smashTimer / SMASH_DURATION;
        int shockwaveSize = (int)(smashRange * progress * 2);

        // 冲击波
        g.setColor(new Color(255, 200, 100, (int)(150 * (1 - progress))));
        g.fillOval(x + width / 2 - shockwaveSize / 2,
                y + height - 20,
                shockwaveSize, shockwaveSize / 2);

        // 灰尘粒子
        for (int i = 0; i < 10; i++) {
            int px = x + width / 2 + (int)((random.nextFloat() - 0.5f) * smashRange);
            int py = y + height - 10 + (int)(random.nextFloat() * 30);
            g.setColor(new Color(139, 69, 19, (int)(200 * (1 - progress))));
            g.fillOval(px, py, 4, 4);
        }

        // 裂纹效果
        g.setColor(new Color(60, 60, 60, 100));
        for (int i = 0; i < 8; i++) {
            int px = x + width / 2 + (int)((i - 4) * 10);
            int py = y + height - 5;
            g.drawLine(px, py, px + 5, py - 8);
            g.drawLine(px, py, px - 5, py - 8);
        }
    }

    /**
     * 绘制愤怒特效
     */
    private void drawEnrageEffect(Graphics2D g) {
        // 红色光环
        float pulse = 0.5f + 0.5f * (float)Math.sin(System.currentTimeMillis() * 0.01);
        int alpha = (int)(100 + 50 * pulse);

        g.setColor(new Color(255, 0, 0, alpha));
        g.setStroke(new BasicStroke(3));
        g.drawOval(x - 5, y - 5, width + 10, height + 10);

        // 蒸汽效果
        for (int i = 0; i < 3; i++) {
            int steamX = x + 10 + i * 20;
            int steamY = y - 5;
            g.setColor(new Color(200, 100, 100, 100));
            g.fillOval(steamX, steamY, 8, 12);
        }
    }

    @Override
    protected void drawHealthBar(Graphics2D g) {
        int barWidth = width - 10;
        int barHeight = 10;
        int barX = x + 5;
        int barY = y - 15;

        float percent = (float) health / maxHealth;

        // 背景
        g.setColor(new Color(60, 60, 60));
        g.fillRect(barX, barY, barWidth, barHeight);

        // 当前血量
        int healthWidth = (int) (barWidth * percent);
        if (healthWidth > 0) {
            if (percent > 0.6f) {
                g.setColor(Color.GREEN);
            } else if (percent > 0.3f) {
                g.setColor(Color.YELLOW);
            } else {
                g.setColor(Color.RED);
            }
            g.fillRect(barX, barY, healthWidth, barHeight);
        }

        // 边框
        g.setColor(Color.BLACK);
        g.drawRect(barX, barY, barWidth, barHeight);

        // 愤怒标记
        if (isEnraged) {
            g.setColor(Color.RED);
            g.drawString("!!!", barX + barWidth - 25, barY + 9);
        }
    }

    // ==================== Getters ====================

    /**
     * 是否愤怒
     */
    public boolean isEnraged() {
        return isEnraged;
    }

    /**
     * 是否正在投掷
     */
    public boolean isThrowing() {
        return isThrowing;
    }

    /**
     * 是否正在砸地
     */
    public boolean isSmashing() {
        return isSmashing;
    }

    /**
     * 是否已经投掷过小鬼
     */
    public boolean hasThrownImp() {
        return hasThrownImp;
    }

    /**
     * 获取投掷动画进度
     */
    public float getThrowProgress() {
        return 1.0f - (float) throwAnimationTimer / THROW_ANIMATION_DURATION;
    }

    /**
     * 获取砸地动画进度
     */
    public float getSmashProgress() {
        return 1.0f - (float) smashTimer / SMASH_DURATION;
    }
}