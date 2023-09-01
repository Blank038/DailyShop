package com.blank038.dailyshop.cacheframework.cache;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Blank038
 */
@Getter
public class GroupCache {
    private final List<String> commodities = new ArrayList<>();
    private final int randomCount;

    public GroupCache(FileConfiguration data) {
        this.commodities.addAll(data.getStringList("commodities"));
        this.randomCount = data.getInt("randomCount");
    }
}
