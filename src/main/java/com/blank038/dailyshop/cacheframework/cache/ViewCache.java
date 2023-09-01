package com.blank038.dailyshop.cacheframework.cache;

import com.aystudio.core.bukkit.util.common.CommonUtil;
import com.blank038.dailyshop.util.TextUtil;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;


/**
 * @author Blank038
 */
@Getter
public class ViewCache {
    private final String groupId, viewTitle;
    private final int[] commoditySlots;
    private final int viewSize;
    private final ConfigurationSection itemSection;

    public ViewCache(FileConfiguration data) {
        this.groupId = data.getString("group");
        this.viewTitle = TextUtil.formatHexColor(data.getString("title"));
        this.viewSize = data.getInt("size");
        this.itemSection = data.getConfigurationSection("items");
        this.commoditySlots = Arrays.stream(CommonUtil.formatSlots(data.getString("commodity-slots")))
                .mapToInt(s -> s)
                .toArray();
    }
}
