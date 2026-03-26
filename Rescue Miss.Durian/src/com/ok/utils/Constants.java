package com.ok.utils;

import java.awt.*;

/**
 * 游戏常量配置类
 * 只存放全局通用的配置参数
 * 植物/僵尸的特有属性（伤害、生命值、阳光消耗等）不放在这里，放在各自的类中
 */
public class Constants {

    // ==================== 窗口配置 ====================
    /** 窗口宽度 */
    public static final int WINDOW_WIDTH = 1200;

    /** 窗口高度 */
    public static final int WINDOW_HEIGHT = 800;

    /** 窗口标题 */
    public static final String WINDOW_TITLE = "解救开朗榴莲头";

    // ==================== 网格配置 ====================
    /** 网格行数（5行草坪） */
    public static final int GRID_ROWS = 5;

    /** 网格列数 */
    public static final int GRID_COLS = 9;

    /** 格子宽度（像素） */
    public static final int GRID_WIDTH = 100;

    /** 格子高度（像素） */
    public static final int GRID_HEIGHT = 100;

    /** 网格起始X坐标（相对于窗口左侧） */
    public static final int GRID_OFFSET_X = 200;

    /** 网格起始Y坐标（相对于窗口顶部） */
    public static final int GRID_OFFSET_Y = 150;

    // ==================== 阳光系统全局参数 ====================
    /** 初始阳光数量 */
    public static final int START_SUN = 150;

    /** 阳光每次增加数量（收集时增加） */
    public static final int SUN_GAIN_AMOUNT = 25;

    /** 阳光产生数量（向日葵生产和天上掉落） */
    public static final int SUN_PRODUCE_AMOUNT = 25;

    /** 向日葵产生阳光间隔（毫秒） */
    public static final int SUNFLOWER_PRODUCE_INTERVAL = 24000;

    /** 天上掉落阳光最小间隔（毫秒） */
    public static final int SKY_SUN_MIN_INTERVAL = 6000;

    /** 天上掉落阳光最大间隔（毫秒） */
    public static final int SKY_SUN_MAX_INTERVAL = 12000;

    /** 阳光存在时间（毫秒），超时后消失 */
    public static final int SUN_LIFESPAN = 8000;

    /** 阳光掉落速度（像素/秒） */
    public static final int SUN_FALL_SPEED = 50;

    /** 铲除植物返还阳光比例（返还阳光消耗的百分比，50表示返还50%） */
    public static final int SHOVEL_REFUND_RATIO = 50;

    // ==================== 帧率配置 ====================
    /** 目标帧率 */
    public static final int TARGET_FPS = 60;

    /** 每帧时间（毫秒） */
    public static final int FRAME_TIME = 1000 / TARGET_FPS;

    // ==================== 游戏流程配置 ====================
    /** 开场倒计时（秒） */
    public static final int COUNTDOWN_SECONDS = 3;

    /** 开场预览动画持续时间（毫秒） */
    public static final int PREVIEW_DURATION = 3000;

    /** 波次内僵尸生成间隔（毫秒） */
    public static final int ZOMBIE_SPAWN_INTERVAL = 2000;

    /** 波次间隔时间（毫秒） */
    public static final int WAVE_INTERVAL = 5000;

    // ==================== 子弹系统全局参数 ====================
    /** 子弹速度（像素/秒） */
    public static final int BULLET_SPEED = 300;

    /** 子弹宽度（像素） */
    public static final int BULLET_WIDTH = 20;

    /** 子弹高度（像素） */
    public static final int BULLET_HEIGHT = 20;

    // ==================== 僵尸系统全局参数 ====================
    /** 僵尸移动速度（像素/秒） */
    public static final int ZOMBIE_WALK_SPEED = 40;

    /** 僵尸攻击间隔（毫秒） */
    public static final int ZOMBIE_ATTACK_INTERVAL = 1000;

    /** 僵尸宽度（像素） */
    public static final int ZOMBIE_WIDTH = 80;

    /** 僵尸高度（像素） */
    public static final int ZOMBIE_HEIGHT = 100;

    // ==================== 植物系统全局参数 ====================
    /** 植物默认生命值（如果植物类没有指定，使用此值） */
    public static final int PLANT_DEFAULT_HEALTH = 100;

    /** 植物默认攻击冷却时间（毫秒） */
    public static final int PLANT_ATTACK_COOLDOWN = 1500;

    /** 植物攻击范围（像素），Integer.MAX_VALUE表示全行 */
    public static final int PLANT_ATTACK_RANGE = Integer.MAX_VALUE;

    /** 植物宽度（像素） */
    public static final int PLANT_WIDTH = 80;

    /** 植物高度（像素） */
    public static final int PLANT_HEIGHT = 80;

    // ==================== 小车系统配置 ====================
    /** 小车触发位置X坐标（像素） */
    public static final int CART_TRIGGER_X = GRID_OFFSET_X;

    /** 小车移动速度（像素/秒） */
    public static final int CART_SPEED = 500;

    /** 小车宽度（像素） */
    public static final int CART_WIDTH = 80;

    /** 小车高度（像素） */
    public static final int CART_HEIGHT = 60;

    // ==================== 账号系统配置 ====================
    /** 最大账号数量 */
    public static final int MAX_ACCOUNT_COUNT = 5;

    /** 默认账号名称前缀 */
    public static final String DEFAULT_ACCOUNT_NAME_PREFIX = "玩家";

    // ==================== 拼图系统配置 ====================
    /** 拼图行数 */
    public static final int PUZZLE_ROWS = 2;

    /** 拼图列数 */
    public static final int PUZZLE_COLS = 2;

    /** 拼图块宽度（像素） */
    public static final int PUZZLE_PIECE_WIDTH = 150;

    /** 拼图块高度（像素） */
    public static final int PUZZLE_PIECE_HEIGHT = 150;

    // ==================== 颜色配置 ====================
    /** 网格线颜色 */
    public static final Color GRID_COLOR = new Color(100, 100, 100, 80);

    /** 网格悬浮高亮颜色 */
    public static final Color GRID_HOVER_COLOR = new Color(255, 255, 255, 100);

    /** 阳光数量正常颜色 */
    public static final Color SUN_NORMAL_COLOR = Color.YELLOW;

    /** 阳光数量不足警告颜色 */
    public static final Color SUN_WARNING_COLOR = Color.RED;

    /** 植物卡片正常颜色 */
    public static final Color CARD_NORMAL_COLOR = new Color(255, 255, 200);

    /** 植物卡片冷却中颜色（深灰色） */
    public static final Color CARD_COOLDOWN_COLOR = new Color(80, 80, 80);

    /** 植物卡片阳光不足颜色（浅灰色） */
    public static final Color CARD_INSUFFICIENT_COLOR = new Color(180, 180, 180);

    /** 僵尸血条颜色 */
    public static final Color ZOMBIE_HEALTH_BAR_COLOR = Color.RED;

    /** 僵尸血条背景颜色 */
    public static final Color ZOMBIE_HEALTH_BAR_BG_COLOR = new Color(60, 60, 60);

    // ==================== UI布局配置 ====================
    /** 植物栏起始X坐标 */
    public static final int PLANT_BAR_X = 50;

    /** 植物栏起始Y坐标 */
    public static final int PLANT_BAR_Y = 50;

    /** 植物卡片宽度 */
    public static final int PLANT_CARD_WIDTH = 80;

    /** 植物卡片高度 */
    public static final int PLANT_CARD_HEIGHT = 90;

    /** 植物卡片间距 */
    public static final int PLANT_CARD_SPACING = 10;

    /** 阳光数量显示位置X */
    public static final int SUN_COUNTER_X = 50;

    /** 阳光数量显示位置Y */
    public static final int SUN_COUNTER_Y = 20;

    /** 铲子按钮位置X */
    public static final int SHOVEL_BUTTON_X = WINDOW_WIDTH - 100;

    /** 铲子按钮位置Y */
    public static final int SHOVEL_BUTTON_Y = 20;

    /** 菜单按钮位置X */
    public static final int MENU_BUTTON_X = WINDOW_WIDTH - 50;

    /** 菜单按钮位置Y */
    public static final int MENU_BUTTON_Y = 20;

    /** 关卡进度条位置X */
    public static final int PROGRESS_BAR_X = WINDOW_WIDTH - 250;

    /** 关卡进度条位置Y */
    public static final int PROGRESS_BAR_Y = WINDOW_HEIGHT - 50;

    /** 关卡进度条宽度 */
    public static final int PROGRESS_BAR_WIDTH = 200;

    /** 关卡进度条高度 */
    public static final int PROGRESS_BAR_HEIGHT = 20;

    /** 左下角头像位置X */
    public static final int AVATAR_X = 20;

    /** 左下角头像位置Y */
    public static final int AVATAR_Y = WINDOW_HEIGHT - 80;

    /** 头像大小 */
    public static final int AVATAR_SIZE = 60;

    // ==================== 字体配置 ====================
    /** 标题字体大小 */
    public static final int TITLE_FONT_SIZE = 48;

    /** 正文字体大小 */
    public static final int NORMAL_FONT_SIZE = 20;

    /** 小字体大小 */
    public static final int SMALL_FONT_SIZE = 14;

    // ==================== 音效配置 ====================
    /** 音效开关默认状态 */
    public static final boolean DEFAULT_SOUND_ENABLED = true;

    /** 音乐开关默认状态 */
    public static final boolean DEFAULT_MUSIC_ENABLED = true;

    /** 默认音效音量（0-100） */
    public static final int DEFAULT_SOUND_VOLUME = 80;

    /** 默认音乐音量（0-100） */
    public static final int DEFAULT_MUSIC_VOLUME = 70;

    // ==================== 动画配置 ====================
    /** 僵尸死亡动画持续时间（毫秒） */
    public static final int ZOMBIE_DEATH_ANIMATION = 500;

    /** 植物被攻击闪烁间隔（毫秒） */
    public static final int PLANT_HURT_BLINK_INTERVAL = 100;

    /** 阳光收集飞向计数器的动画时间（毫秒） */
    public static final int SUN_COLLECT_ANIMATION = 200;

    // ==================== 路径配置 ====================
    /** 图片资源根路径 */
    public static final String IMAGE_PATH = "/images/";

    /** 音效资源根路径 */
    public static final String SOUND_PATH = "/sounds/";

    /** 配置文件根路径 */
    public static final String CONFIG_PATH = "/config/";

    /** 存档文件路径（相对于程序运行目录） */
    public static final String SAVE_FILE_PATH = "saves/save_data.json";

    // ==================== 无限模式配置 ====================
    /** 无限模式初始难度系数 */
    public static final int ENDLESS_START_DIFFICULTY = 1;

    /** 每波次难度增加系数 */
    public static final double ENDLESS_DIFFICULTY_INCREMENT = 0.1;

    /** 无限模式僵尸最大数量限制（防止过多僵尸卡顿） */
    public static final int ENDLESS_MAX_ZOMBIES = 50;

    // ==================== 私有构造函数 ====================
    /** 私有构造函数，防止实例化 */
    private Constants() {
        // 工具类不应被实例化
    }
}