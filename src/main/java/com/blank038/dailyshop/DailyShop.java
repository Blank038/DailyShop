package com.blank038.dailyshop;

import com.aystudio.core.bukkit.plugin.AyPlugin;
import com.blank038.dailyshop.cacheframework.manager.CacheManager;
import com.blank038.dailyshop.command.MainCommand;
import com.blank038.dailyshop.listen.PlayerListener;
import com.blank038.dailyshop.util.EcoUtil;
import com.blank038.dailyshop.util.TextUtil;
import org.bukkit.Bukkit;

/**
 * 每日折扣, 每日每人限购, 每日全区限购
 */
public class DailyShop extends AyPlugin {
    private static DailyShop instance;

    @Override
    public void onEnable() {
        instance = this;
        loadConfig();

        EcoUtil.init();

        this.getCommand("dailyshop").setExecutor(new MainCommand());
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);

        CacheManager.checkTime(true);
        // 启动重置线程
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> CacheManager.checkTime(false), 20L, 20L);
        // 启动定时存储线程
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, CacheManager::saveCommonData, 1200L * 5, 1200L * 5);
    }

    @Override
    public void onDisable() {
        CacheManager.saveCommonData();
    }

    public void loadConfig() {
        CacheManager.initialize();
    }

    public String getString(String key, boolean prefix) {
        return TextUtil.formatHexColor((prefix ? getConfig().getString("message.prefix") : "") + getConfig().getString(key));
    }

    public static DailyShop getInstance() {
        return instance;
    }
}