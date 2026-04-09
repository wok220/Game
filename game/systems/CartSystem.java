package com.ok.game.systems;

import com.ok.game.entities.Zombie;
import com.ok.game.grid.GridManager;
import com.ok.utils.Constants;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 小车系统
 * 管理每行的小车，僵尸到达最左侧时触发小车，消灭该行所有僵尸
 */
public class CartSystem {

    /** 单例实例 */
    private static CartSystem instance;

    /** 网格管理器引用 */
    private GridManager gridManager;

    /** 小车状态数组（每行一辆车，true表示未使用，false表示已使用） */
    private boolean[] carts;

    /** 小车移动动画状态（每行） */
    private CartAnimation[] animations;

    /** 小车宽度（像素） */
    private int cartWidth;

    /** 小车高度（像素） */
    private int cartHeight;

    /** 小车移动速度（像素/秒） */
    private int cartSpeed;

    /** 小车触发位置X坐标（网格左边界） */
    private int triggerX;

    /** 小车起始X坐标 */
    private int cartStartX;

    /** 小车Y坐标（每行） */
    private int[] cartY;

    /**
     * 小车动画内部类
     */
    private static class CartAnimation {
        boolean isActive;      // 是否正在动画中
        int row;               // 所在行
        float x;               // 当前X坐标
        float targetX;         // 目标X坐标（窗口右侧）
        float speed;           // 移动速度
        List<Zombie> zombiesToKill;  // 要消灭的僵尸列表

        CartAnimation(int row, float startX, float targetX, float speed) {
            this.isActive = true;
            this.row = row;
            this.x = startX;
            this.targetX = targetX;
            this.speed = speed;
            this.zombiesToKill = new ArrayList<>();
        }

        boolean update(float deltaTime) {
            if (!isActive) return false;

            x += speed * deltaTime;
            if (x >= targetX) {
                isActive = false;
                return true; // 动画完成
            }
            return false; // 动画进行中
        }

        Rectangle getBounds() {
            return new Rectangle((int)x, getCartY(row), Constants.CART_WIDTH, Constants.CART_HEIGHT);
        }
    }

    /**
     * 私有构造函数
     */
    public CartSystem() {
        int rows = Constants.GRID_ROWS;
        this.carts = new boolean[rows];
        this.animations = new CartAnimation[rows];
        this.cartY = new int[rows];

        this.cartWidth = Constants.CART_WIDTH;
        this.cartHeight = Constants.CART_HEIGHT;
        this.cartSpeed = Constants.CART_SPEED;
        this.triggerX = Constants.CART_TRIGGER_X;
        this.cartStartX = Constants.GRID_OFFSET_X - cartWidth;

        // 初始化所有小车为未使用状态
        for (int i = 0; i < rows; i++) {
            carts[i] = true;
            animations[i] = null;
            // 计算每行小车的Y坐标（网格中央）
            cartY[i] = Constants.GRID_OFFSET_Y + i * Constants.GRID_HEIGHT +
                    (Constants.GRID_HEIGHT - cartHeight) / 2;
        }
    }

    /**
     * 获取单例实例
     */
    public static CartSystem getInstance() {
        if (instance == null) {
            instance = new CartSystem();
        }
        return instance;
    }

    /**
     * 初始化
     * @param gridManager 网格管理器
     */
    public void init(GridManager gridManager) {
        this.gridManager = gridManager;
    }

    // ==================== 小车触发 ====================

    /**
     * 触发指定行的小车
     * @param row 网格行
     * @param zombies 所有僵尸列表（用于获取该行僵尸）
     * @return 是否触发成功（小车未使用且未被触发）
     */
    public boolean triggerCart(int row, List<Zombie> zombies) {
        // 检查行是否有效
        if (row < 0 || row >= carts.length) {
            return false;
        }

        // 检查小车是否可用
        if (!carts[row]) {
            return false;
        }

        // 检查是否已有动画在进行
        if (animations[row] != null && animations[row].isActive) {
            return false;
        }

        // 标记小车已使用
        carts[row] = false;

        // 收集该行所有存活的僵尸
        List<Zombie> zombiesInRow = new ArrayList<>();
        for (Zombie zombie : zombies) {
            if (zombie.isAlive() && !zombie.isDying() && zombie.getRow() == row) {
                zombiesInRow.add(zombie);
            }
        }

        // 如果没有僵尸，直接返回（小车仍然被消耗）
        if (zombiesInRow.isEmpty()) {
            return true;
        }

        // 创建动画
        CartAnimation animation = new CartAnimation(
                row,
                cartStartX,
                Constants.WINDOW_WIDTH + cartWidth,
                cartSpeed
        );
        animation.zombiesToKill.addAll(zombiesInRow);
        animations[row] = animation;

        return true;
    }

    /**
     * 更新所有小车动画
     * @param deltaTime 帧间隔时间（秒）
     * @param zombies 所有僵尸列表（用于动画中消灭僵尸）
     */
    public void update(float deltaTime, List<Zombie> zombies) {
        for (int row = 0; row < animations.length; row++) {
            CartAnimation anim = animations[row];
            if (anim == null || !anim.isActive) {
                continue;
            }

            // 更新动画位置
            boolean completed = anim.update(deltaTime);

            // 检查碰撞，消灭僵尸
            Rectangle cartBounds = anim.getBounds();
            for (Zombie zombie : anim.zombiesToKill) {
                if (zombie.isAlive() && !zombie.isDying()) {
                    Rectangle zombieBounds = zombie.getBounds();
                    if (cartBounds.intersects(zombieBounds)) {
                        // 小车撞到僵尸，僵尸死亡
                        zombie.takeDamage(zombie.getHealth()); // 直接秒杀
                    }
                }
            }

            // 动画完成，清理
            if (completed) {
                animations[row] = null;
            }
        }
    }

    // ==================== 小车状态查询 ====================

    /**
     * 检查指定行的小车是否可用
     * @param row 网格行
     * @return 是否可用
     */
    public boolean isCartAvailable(int row) {
        if (row < 0 || row >= carts.length) {
            return false;
        }
        return carts[row];
    }

    /**
     * 获取剩余小车数量
     * @return 剩余小车数量
     */
    public int getRemainingCartCount() {
        int count = 0;
        for (boolean cart : carts) {
            if (cart) {
                count++;
            }
        }
        return count;
    }

    /**
     * 获取所有小车的可用状态
     * @return 小车可用状态数组
     */
    public boolean[] getAllCartStatus() {
        return carts.clone();
    }

    /**
     * 检查指定行的小车是否正在动画中
     * @param row 网格行
     * @return 是否在动画中
     */
    public boolean isCartAnimating(int row) {
        if (row < 0 || row >= animations.length) {
            return false;
        }
        return animations[row] != null && animations[row].isActive;
    }

    /**
     * 获取小车动画的X坐标（用于绘制）
     * @param row 网格行
     * @return X坐标，如果没有动画返回-1
     */
    public float getCartAnimationX(int row) {
        if (row < 0 || row >= animations.length) {
            return -1;
        }
        CartAnimation anim = animations[row];
        if (anim != null && anim.isActive) {
            return anim.x;
        }
        return -1;
    }

    // ==================== 绘制辅助 ====================

    /**
     * 获取小车绘制位置（静态小车）
     * @param row 网格行
     * @return 小车位置（左上角），如果小车已使用返回null
     */
    public Point getCartPosition(int row) {
        if (!isCartAvailable(row)) {
            return null;
        }
        return new Point(cartStartX, cartY[row]);
    }

    /**
     * 获取小车矩形（用于绘制）
     * @param row 网格行
     * @return 小车矩形，如果小车已使用返回null
     */
    public Rectangle getCartBounds(int row) {
        if (!isCartAvailable(row)) {
            return null;
        }
        return new Rectangle(cartStartX, cartY[row], cartWidth, cartHeight);
    }

    /**
     * 获取动画小车的矩形（用于绘制）
     * @param row 网格行
     * @return 小车矩形，如果没有动画返回null
     */
    public Rectangle getAnimationCartBounds(int row) {
        if (row < 0 || row >= animations.length) {
            return null;
        }
        CartAnimation anim = animations[row];
        if (anim != null && anim.isActive) {
            return anim.getBounds();
        }
        return null;
    }

    // ==================== 重置 ====================

    /**
     * 重置小车系统（新关卡时调用）
     */
    public void reset() {
        int rows = Constants.GRID_ROWS;
        for (int i = 0; i < rows; i++) {
            carts[i] = true;
            animations[i] = null;
        }
    }

    /**
     * 获取小车Y坐标（静态方法，供内部使用）
     */
    private static int getCartY(int row) {
        return Constants.GRID_OFFSET_Y + row * Constants.GRID_HEIGHT +
                (Constants.GRID_HEIGHT - Constants.CART_HEIGHT) / 2;
    }
}