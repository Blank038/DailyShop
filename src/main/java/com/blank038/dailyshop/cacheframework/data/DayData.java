package com.blank038.dailyshop.cacheframework.data;

import com.blank038.dailyshop.DailyShop;
import com.blank038.dailyshop.cacheframework.cache.CommodityCache;
import com.blank038.dailyshop.cacheframework.manager.CacheManager;
import com.blank038.dailyshop.cacheframework.storage.PlayerStorage;
import com.blank038.dailyshop.util.EcoUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DayData {
    private final File file;
    private final Map<String, Integer> quantityPurchasedTotal = new HashMap<>();

    public DayData(File file) {
        this.file = file;
        FileConfiguration data = YamlConfiguration.loadConfiguration(file);
        // 读取今日商品全服限购列表
        if (data.contains("commoditys")) {
            for (String key : data.getConfigurationSection("commoditys").getKeys(false)) {
                ConfigurationSection section = data.getConfigurationSection("commoditys." + key);
                quantityPurchasedTotal.put(key, section.getInt("buy-count"));
            }
        }
    }

    public Map<String, Integer> getDayCommoditys() {
        return new HashMap<>(quantityPurchasedTotal);
    }

    public void save() {
        FileConfiguration data = new YamlConfiguration();
        for (Map.Entry<String, Integer> entry : quantityPurchasedTotal.entrySet()) {
            data.set("commoditys." + entry.getKey() + ".buy-count", entry.getValue());
        }
        try {
            data.save(file);
        } catch (IOException e) {
            DailyShop.getInstance().getLogger().severe(e.toString());
        }
    }

    public boolean tryPurchase(Player player, String sourceGroup, String commodityId) {
        if (CacheManager.getCommodityDataMap().containsKey(commodityId)) {
            PlayerStorage playerData = CacheManager.getPlayerStorageMap().get(player.getUniqueId());
            if (!CacheManager.getPlayerStorageMap().containsKey(player.getUniqueId()) || !playerData.getGroupCache().containsKey(sourceGroup)
                    || !playerData.hasCommodity(sourceGroup, commodityId)) {
                player.sendMessage(DailyShop.getInstance().getString("message.denied", true));
                return false;
            }
            CommodityCache commodityData = CacheManager.getCommodityDataMap().get(commodityId);
            int amount = playerData.getQuantityPurchased(commodityId);
            if (commodityData.isEnableLimit() && amount >= commodityData.getOneself()) {
                player.sendMessage(DailyShop.getInstance().getString("message.limit.oneself", true));
                return false;
            }
            int total = quantityPurchasedTotal.getOrDefault(commodityId, 0);
            if (commodityData.isEnableLimit() && total >= commodityData.getAll()) {
                player.sendMessage(DailyShop.getInstance().getString("message.limit.all", true));
                return false;
            }
            PlayerStorage.CommodityEntry entry = playerData.getCommodityEntry(sourceGroup, commodityId);
            int price = (int) (commodityData.getPrice() * (0.1D * entry.discount));
            if (EcoUtil.balance(player, commodityData.getPayType(), price, commodityData.getVaultName())) {

                EcoUtil.take(player, commodityData.getPayType(), price, commodityData.getVaultName());

                quantityPurchasedTotal.replace(commodityId, total + 1);
                playerData.addQuantityPurchased(commodityId, 1);

                commodityData.give(player);
                player.sendMessage(DailyShop.getInstance().getString("message.buy", true)
                        .replace("%item%", commodityData.getDisplayName()));
                return true;
            } else {
                player.sendMessage(DailyShop.getInstance().getString("message.lack", true));
            }
        } else {
            player.sendMessage(DailyShop.getInstance().getString("message.not-exist", true));
        }
        return false;
    }

    public void reset() {
        quantityPurchasedTotal.clear();
    }
}