package com.ok.resource;

import com.ok.utils.Constants;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 资源管理器
 * 单例模式，统一加载和管理游戏中的所有图片资源
 */
public class ResourceManager {

    /** 单例实例 */
    private static ResourceManager instance;

    /** 图片缓存 */
    private Map<String, BufferedImage> imageCache;

    /**
     * 私有构造函数
     */
    private ResourceManager() {
        imageCache = new HashMap<>();
    }

    /**
     * 获取单例实例
     */
    public static ResourceManager getInstance() {
        if (instance == null) {
            instance = new ResourceManager();
        }
        return instance;
    }

    // ==================== 图片加载 ====================

    /**
     * 加载图片
     * @param path 图片路径（相对于 resources 目录）
     * @return 图片对象，加载失败返回 null
     */
    public BufferedImage loadImage(String path) {
        // 先检查缓存
        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }

        // 从资源路径加载
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                System.err.println("图片资源不存在: " + path);
                return null;
            }
            BufferedImage image = ImageIO.read(is);
            if (image != null) {
                imageCache.put(path, image);
            }
            return image;
        } catch (IOException e) {
            System.err.println("加载图片失败: " + path);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 加载图片并缩放到指定尺寸
     * @param path 图片路径
     * @param width 目标宽度
     * @param height 目标高度
     * @return 缩放后的图片
     */
    public BufferedImage loadImage(String path, int width, int height) {
        BufferedImage original = loadImage(path);
        if (original == null) {
            return null;
        }
        return scaleImage(original, width, height);
    }

    /**
     * 缩放图片
     * @param original 原图
     * @param width 目标宽度
     * @param height 目标高度
     * @return 缩放后的图片
     */
    public BufferedImage scaleImage(BufferedImage original, int width, int height) {
        if (original == null) {
            return null;
        }
        Image scaled = original.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = result.createGraphics();
        g2d.drawImage(scaled, 0, 0, null);
        g2d.dispose();
        return result;
    }

    // ==================== 获取图片（自动加载） ====================

    /**
     * 获取植物图片
     * @param plantId 植物ID
     * @return 植物图片
     */
    public BufferedImage getPlantImage(String plantId) {
        String path = Constants.IMAGE_PATH + "plants/" + plantId + ".png";
        return loadImage(path, Constants.PLANT_WIDTH, Constants.PLANT_HEIGHT);
    }

    /**
     * 获取僵尸图片
     * @param zombieId 僵尸ID
     * @return 僵尸图片
     */
    public BufferedImage getZombieImage(String zombieId) {
        String path = Constants.IMAGE_PATH + "zombies/" + zombieId + ".png";
        return loadImage(path, Constants.ZOMBIE_WIDTH, Constants.ZOMBIE_HEIGHT);
    }

    /**
     * 获取子弹图片
     * @param bulletType 子弹类型
     * @return 子弹图片
     */
    public BufferedImage getBulletImage(String bulletType) {
        String path = Constants.IMAGE_PATH + "bullets/" + bulletType + ".png";
        return loadImage(path, Constants.BULLET_WIDTH, Constants.BULLET_HEIGHT);
    }

    /**
     * 获取阳光图片
     * @return 阳光图片
     */
    public BufferedImage getSunImage() {
        String path = Constants.IMAGE_PATH + "ui/sun.png";
        return loadImage(path, 40, 40);
    }

    /**
     * 获取植物卡片图片
     * @param plantId 植物ID
     * @return 卡片图片
     */
    public BufferedImage getPlantCardImage(String plantId) {
        String path = Constants.IMAGE_PATH + "cards/" + plantId + ".png";
        return loadImage(path, Constants.PLANT_CARD_WIDTH, Constants.PLANT_CARD_HEIGHT);
    }

    /**
     * 获取铲子图标
     * @return 铲子图标
     */
    public BufferedImage getShovelImage() {
        String path = Constants.IMAGE_PATH + "ui/shovel.png";
        return loadImage(path, 40, 40);
    }

    /**
     * 获取菜单按钮图标
     * @return 菜单图标
     */
    public BufferedImage getMenuButtonImage() {
        String path = Constants.IMAGE_PATH + "ui/menu.png";
        return loadImage(path, 30, 30);
    }

    /**
     * 获取小车图片
     * @return 小车图片
     */
    public BufferedImage getCartImage() {
        String path = Constants.IMAGE_PATH + "ui/cart.png";
        return loadImage(path, Constants.CART_WIDTH, Constants.CART_HEIGHT);
    }

    /**
     * 获取头像图片（默认）
     * @return 头像图片
     */
    public BufferedImage getAvatarImage() {
        String path = Constants.IMAGE_PATH + "ui/avatar.png";
        return loadImage(path, Constants.AVATAR_SIZE, Constants.AVATAR_SIZE);
    }

    /**
     * 获取鸽子图标
     * @return 鸽子图标
     */
    public BufferedImage getPigeonIcon() {
        String path = Constants.IMAGE_PATH + "ui/pigeon.png";
        return loadImage(path, 30, 30);
    }

    /**
     * 获取拼图块图片
     * @param pieceIndex 拼图块索引（0-3）
     * @return 拼图块图片
     */
    public BufferedImage getPuzzlePieceImage(int pieceIndex) {
        String path = Constants.IMAGE_PATH + "puzzle/piece_" + pieceIndex + ".png";
        return loadImage(path, Constants.PUZZLE_PIECE_WIDTH, Constants.PUZZLE_PIECE_HEIGHT);
    }

    /**
     * 获取完整拼图背景（淡色）
     * @return 完整拼图背景
     */
    public BufferedImage getPuzzleBackgroundImage() {
        String path = Constants.IMAGE_PATH + "puzzle/background.png";
        return loadImage(path, Constants.PUZZLE_PIECE_WIDTH * Constants.PUZZLE_COLS,
                Constants.PUZZLE_PIECE_HEIGHT * Constants.PUZZLE_ROWS);
    }

    /**
     * 获取物品栏物品放大图
     * @param itemId 物品ID
     * @param type 类型（plant/zombie/tool）
     * @return 放大图
     */
    public BufferedImage getLargeItemImage(String itemId, String type) {
        String path = Constants.IMAGE_PATH + type + "/large/" + itemId + ".png";
        return loadImage(path, 200, 200);
    }

    /**
     * 获取背景图片
     * @param backgroundName 背景名称
     * @return 背景图片
     */
    public BufferedImage getBackgroundImage(String backgroundName) {
        String path = Constants.IMAGE_PATH + "backgrounds/" + backgroundName + ".png";
        return loadImage(path, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
    }

    /**
     * 获取草地格子背景
     * @return 草地格子背景
     */
    public BufferedImage getGrassTileImage() {
        String path = Constants.IMAGE_PATH + "ui/grass_tile.png";
        return loadImage(path, Constants.GRID_WIDTH, Constants.GRID_HEIGHT);
    }

    /**
     * 获取街道图片（右侧）
     * @return 街道图片
     */
    public BufferedImage getStreetImage() {
        String path = Constants.IMAGE_PATH + "backgrounds/street.png";
        return loadImage(path, 200, Constants.WINDOW_HEIGHT);
    }

    /**
     * 获取房子图片（左侧）
     * @return 房子图片
     */
    public BufferedImage getHouseImage() {
        String path = Constants.IMAGE_PATH + "backgrounds/house.png";
        return loadImage(path, 200, Constants.WINDOW_HEIGHT);
    }

    // ==================== 图片工具方法 ====================

    /**
     * 获取图片的多个帧（用于动画）
     * @param basePath 基础路径（如 "plants/PeaShooter_"）
     * @param frameCount 帧数
     * @param width 每帧宽度
     * @param height 每帧高度
     * @return 帧数组
     */
    public BufferedImage[] getAnimationFrames(String basePath, int frameCount, int width, int height) {
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            String path = Constants.IMAGE_PATH + basePath + i + ".png";
            frames[i] = loadImage(path, width, height);
        }
        return frames;
    }

    /**
     * 预加载一组图片
     * @param paths 图片路径列表
     */
    public void preloadImages(String[] paths) {
        for (String path : paths) {
            loadImage(path);
        }
    }

    /**
     * 清除图片缓存（用于释放内存）
     */
    public void clearCache() {
        imageCache.clear();
    }

    /**
     * 检查图片是否已缓存
     */
    public boolean isCached(String path) {
        return imageCache.containsKey(path);
    }

    /**
     * 获取缓存大小
     */
    public int getCacheSize() {
        return imageCache.size();
    }
}