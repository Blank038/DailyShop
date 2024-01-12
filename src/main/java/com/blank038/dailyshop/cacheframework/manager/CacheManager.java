package com.blank038.dailyshop.cacheframework.manager;

import com.blank038.dailyshop.DailyShop;
import com.blank038.dailyshop.cacheframework.cache.GroupCache;
import com.blank038.dailyshop.cacheframework.cache.ViewCache;
import com.blank038.dailyshop.cacheframework.cache.CommodityCache;
import com.blank038.dailyshop.cacheframework.data.DayData;
import com.blank038.dailyshop.cacheframework.storage.PlayerStorage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Blank038
 */
public class CacheManager {
    private final static Map<String, CommodityCache> COMMODITY_DATA_MAP = new HashMap<>();
    private final static Map<String, ViewCache> VIEW_CACHE_MAP = new HashMap<>();
    private final static Map<String, GroupCache> GROUP_CACHE_MAP = new HashMap<>();
    private final static Map<UUID, PlayerStorage> PLAYER_STORAGE_MAP = new HashMap<>();

    private static DayData dayData;
    private static FileConfiguration timeData;

    public static FileConfiguration getTimeData() {
        return timeData;
    }

    public static DayData getDayData() {
        return dayData;
    }

    public static void initialize() {
        DailyShop.getInstance().saveDefaultConfig();
        DailyShop.getInstance().reloadConfig();

        if (dayData != null) {
            dayData.save();
        }

        COMMODITY_DATA_MAP.clear();
        File commodityFolder = new File(DailyShop.getInstance().getDataFolder(), "item");
        if (!commodityFolder.exists()) {
            DailyShop.getInstance().saveResource("item/example.yml", "item/example.yml");
        }
        for (File file : commodityFolder.listFiles()) {
            FileConfiguration data = YamlConfiguration.loadConfiguration(file);
            for (String key : data.getKeys(false)) {
                COMMODITY_DATA_MAP.put(key, new CommodityCache(data.getConfigurationSection(key)));
            }
        }

        DailyShop.getInstance().saveResource("day.yml", "day.yml", false, (file) -> {
            CacheManager.dayData = new DayData(file);
        });

        VIEW_CACHE_MAP.clear();
        File viewFolder = new File(DailyShop.getInstance().getDataFolder(), "view");
        if (!viewFolder.exists()) {
            DailyShop.getInstance().saveResource("view/example.yml", "view/example.yml");
        }
        for (File file : viewFolder.listFiles()) {
            String viewId = file.getName().substring(0, file.getName().lastIndexOf(".yml"));
            CacheManager.VIEW_CACHE_MAP.put(viewId, new ViewCache(YamlConfiguration.loadConfiguration(file)));
        }

        GROUP_CACHE_MAP.clear();
        File groupFolder = new File(DailyShop.getInstance().getDataFolder(), "group");
        if (!groupFolder.exists()) {
            DailyShop.getInstance().saveResource("group/example.yml", "group/example.yml");
        }
        for (File file : groupFolder.listFiles()) {
            String groupId = file.getName().substring(0, file.getName().lastIndexOf(".yml"));
            CacheManager.GROUP_CACHE_MAP.put(groupId, new GroupCache(YamlConfiguration.loadConfiguration(file)));
        }

        DailyShop.getInstance().saveResource("time.yml", "time.yml", false,
                (file) -> CacheManager.timeData = YamlConfiguration.loadConfiguration(file));
    }

    public static void load(UUID uuid, String playerName) {
        PLAYER_STORAGE_MAP.put(uuid, CacheManager.loadPlayerStorage(uuid, playerName, false));
    }

    public static PlayerStorage loadPlayerStorage(UUID uuid, String playerName, boolean checkOnline) {
        if (!checkOnline) {
            CacheManager.unload(uuid);
        } else if (CacheManager.PLAYER_STORAGE_MAP.containsKey(uuid)) {
            return CacheManager.PLAYER_STORAGE_MAP.get(uuid);
        }
        File folder = new File(DailyShop.getInstance().getDataFolder(), "playerData");
        folder.mkdir();

        File file = new File(folder, uuid.toString() + ".yml");
        return new PlayerStorage(uuid, playerName, YamlConfiguration.loadConfiguration(file));
    }

    public static void unload(UUID uuid) {
        CacheManager.save(PLAYER_STORAGE_MAP.remove(uuid));
    }

    public static void save(PlayerStorage playerCache) {
        if (playerCache != null) {
            File folder = new File(DailyShop.getInstance().getDataFolder(), "playerData");
            folder.mkdir();

            File file = new File(folder, playerCache.getPlayerUniqueId() + ".yml");
            FileConfiguration data = playerCache.toConfiguration();
            try {
                data.save(file);
            } catch (IOException e) {
                DailyShop.getInstance().getLogger().severe(e.toString());
            }
        }
    }

    public static void checkTime(boolean start) {
        CacheManager.PLAYER_STORAGE_MAP.forEach((k, v) -> v.checkResetDate());

        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf_1 = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat sdf_2 = new SimpleDateFormat("HH:mm");
        if ((start || sdf_2.format(date).equals("00:00")) && !sdf_1.format(date).equals(CacheManager.getTimeData().getString("time"))) {
            CacheManager.getTimeData().set("time", sdf_1.format(date));
            CacheManager.saveCommonData();
            CacheManager.getDayData().reset();
        }
    }

    public static void saveCommonData() {
        CacheManager.dayData.save();
        try {
            CacheManager.timeData.save(new File(DailyShop.getInstance().getDataFolder(), "time.yml"));
        } catch (IOException e) {
            DailyShop.getInstance().getLogger().severe(e.toString());
        }
    }

    public static Map<String, CommodityCache> getCommodityDataMap() {
        return COMMODITY_DATA_MAP;
    }

    public static Map<String, ViewCache> getViewCacheMap() {
        return VIEW_CACHE_MAP;
    }

    public static Map<String, GroupCache> getGroupCacheMap() {
        return GROUP_CACHE_MAP;
    }

    public static Map<UUID, PlayerStorage> getPlayerStorageMap() {
        return PLAYER_STORAGE_MAP;
    }
}
