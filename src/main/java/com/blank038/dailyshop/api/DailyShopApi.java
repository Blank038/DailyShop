package com.blank038.dailyshop.api;

import com.blank038.dailyshop.cacheframework.manager.CacheManager;
import com.blank038.dailyshop.cacheframework.storage.PlayerStorage;
import org.bukkit.entity.Player;

/**
 * @author Blank038
 */
public class DailyShopApi {

    public static void resetPlayerDailyCommodities(Player player) {
        if (player == null) {
            return;
        }
        PlayerStorage storage = CacheManager.loadPlayerStorage(player.getUniqueId(), player.getName(), true);
        storage.resetCommodities();
    }
}
