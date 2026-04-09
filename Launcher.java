package com.ok;

import com.ok.data.DataManager;
import com.ok.data.LocalDataProvider;
import com.ok.ui.GameFrame;
import javax.swing.SwingUtilities;

public class Launcher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 初始化数据管理器
            DataManager.getInstance().setProvider(new LocalDataProvider());

            // 创建并显示主窗口
            GameFrame frame = new GameFrame();
            frame.setVisible(true);
        });
    }
}