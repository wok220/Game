package com.ok.data;

/**
 * 数据管理器
 * 单例模式，持有当前的 IDataProvider 实例
 * 游戏逻辑通过此类访问数据，不直接依赖具体的 IDataProvider 实现
 *
 * 前期：setProvider(new LocalDataProvider())
 * 后期账号切换：setProvider(new MultiAccountDataProvider(account))
 */
public class DataManager {

    /** 单例实例 */
    private static DataManager instance;

    /** 当前数据提供者 */
    private IDataProvider dataProvider;

    /**
     * 私有构造函数
     */
    private DataManager() {
        // 默认不初始化provider，由外部调用setProvider设置
    }

    /**
     * 获取单例实例
     */
    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    /**
     * 设置数据提供者
     * @param provider 数据提供者实例
     */
    public void setProvider(IDataProvider provider) {
        this.dataProvider = provider;
    }

    /**
     * 获取当前数据提供者
     * @return 数据提供者
     */
    public IDataProvider getProvider() {
        if (dataProvider == null) {
            throw new IllegalStateException("DataManager未初始化，请先调用setProvider()");
        }
        return dataProvider;
    }

    /**
     * 检查数据提供者是否已初始化
     */
    public boolean isInitialized() {
        return dataProvider != null;
    }

    /**
     * 重置数据管理器（用于测试或重新初始化）
     */
    public void reset() {
        dataProvider = null;
    }
}