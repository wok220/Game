package com.ok.ui;

/**
 * 支持参数传递的刷新接口
 * 用于需要在切换界面时传递参数的场景
 *
 * 使用场景：
 * - 切换到战斗场景时，需要传递游戏模式（闯关/无限）和关卡索引
 * - 切换到物品栏时，需要传递初始分类（植物/僵尸/工具）
 *
 * 实现此接口的界面，在切换时可通过 refreshWithParams() 接收参数
 */
public interface ParamRefreshable extends Refreshable {

    /**
     * 带参数的刷新方法
     * 切换界面时调用，用于传递启动参数
     *
     * @param params 参数数组，由调用方定义，实现类自行解析
     *               - 战斗场景: params[0] = mode (String), params[1] = levelIndex (Integer)
     *               - 物品栏: params[0] = category (String)
     */
    void refreshWithParams(Object... params);
}