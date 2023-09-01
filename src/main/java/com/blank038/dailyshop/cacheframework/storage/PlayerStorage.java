package com.blank038.dailyshop.cacheframework.storage;

import com.blank038.dailyshop.cacheframework.manager.CacheManager;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Blank038
 */
@Getter
public class PlayerStorage {
    private final String playerName;
    private final UUID playerUniqueId;
    private final Map<String, List<CommodityEntry>> groupCache = new HashMap<>();
    private final Map<String, Integer> quantityPurchased = new HashMap<>();
    private LocalDate resetTime;


    public PlayerStorage(UUID uniqueId, String playerName, FileConfiguration data) {
        this.playerName = playerName;
        this.playerUniqueId = uniqueId;
        if (data.contains("groups")) {
            data.getConfigurationSection("groups").getKeys(false).forEach(s -> {
                List<CommodityEntry> group = data.getStringList("groups." + s).stream()
                        .map(v -> {
                            String[] split = v.split(",");
                            CommodityEntry commodityEntry = new CommodityEntry();
                            commodityEntry.commodityId = split[0];
                            commodityEntry.discount = Integer.parseInt(split[1]);
                            return commodityEntry;
                        })
                        .collect(Collectors.toList());
                this.groupCache.put(s, group);
            });
        }
        this.resetTime = LocalDate.from(Instant.ofEpochMilli(data.getLong("resetTime")).atZone(ZoneId.systemDefault()));
        this.checkResetDate();
    }

    public void checkResetDate() {
        LocalDate localTime = LocalDate.now();
        if (localTime.isAfter(this.resetTime)) {
            this.resetTime = LocalDate.from(localTime);
            this.resetCommodities();
        }
    }

    public void resetCommodities() {
        this.groupCache.clear();
        this.quantityPurchased.clear();
        CacheManager.getGroupCacheMap().forEach((key, value) -> {
            List<String> clone = new ArrayList<>(value.getCommodities());
            List<CommodityEntry> commodityEntries = new ArrayList<>();

            for (int count = 0; count < value.getRandomCount() && !clone.isEmpty(); count++) {
                String random = clone.get((int) (Math.random() * clone.size()));
                clone.remove(random);

                CommodityEntry commodityEntry = new CommodityEntry();
                commodityEntry.commodityId = random;
                commodityEntry.discount = CacheManager.getCommodityDataMap().get(random).randomDiscount();
                commodityEntries.add(commodityEntry);
            }
            this.groupCache.put(key, commodityEntries);
        });
    }

    public void addQuantityPurchased(String commodityId, int amount) {
        int count = this.quantityPurchased.getOrDefault(commodityId, 0);
        this.quantityPurchased.put(commodityId, count + amount);
    }

    public int getQuantityPurchased(String commodityId) {
        return this.quantityPurchased.getOrDefault(commodityId, 0);
    }

    public boolean hasGroup(String groupId) {
        return this.groupCache.containsKey(groupId);
    }

    public boolean hasCommodity(String groupId, String commodityId) {
        if (this.groupCache.containsKey(groupId)) {
            return this.groupCache.get(groupId).stream().anyMatch(s -> s.commodityId.equals(commodityId));
        }
        return false;
    }

    public CommodityEntry getCommodityEntry(String groupId, String commodityId) {
        if (this.groupCache.containsKey(groupId)) {
            return this.groupCache.get(groupId).stream()
                    .filter(s -> s.commodityId.equals(commodityId))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public FileConfiguration toConfiguration() {
        FileConfiguration data = new YamlConfiguration();
        for (Map.Entry<String, List<CommodityEntry>> entry : this.groupCache.entrySet()) {
            data.set("groups." + entry.getKey(), entry.getValue().stream()
                    .map(s -> s.commodityId + "," + s.discount)
                    .collect(Collectors.toList()));
        }
        data.set("resetTime", this.resetTime.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli());
        return data;
    }

    public static class CommodityEntry {
        public String commodityId;
        public int discount;
    }
}
