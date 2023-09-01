package com.blank038.dailyshop.view;

import com.aystudio.core.bukkit.util.common.CommonUtil;
import com.aystudio.core.bukkit.util.inventory.GuiModel;
import com.blank038.dailyshop.DailyShop;
import com.blank038.dailyshop.cacheframework.cache.CommodityCache;
import com.blank038.dailyshop.cacheframework.cache.ViewCache;
import com.blank038.dailyshop.cacheframework.manager.CacheManager;
import com.blank038.dailyshop.cacheframework.storage.PlayerStorage;
import de.tr7zw.nbtapi.NBTItem;
import de.tr7zw.nbtapi.utils.MinecraftVersion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Blank038
 */
public class DailyShopView {

    public static void open(Player player, String viewKey) {
        if (!CacheManager.getViewCacheMap().containsKey(viewKey) || !CacheManager.getPlayerStorageMap().containsKey(player.getUniqueId())) {
            return;
        }
        ViewCache viewCache = CacheManager.getViewCacheMap().get(viewKey);
        GuiModel model = new GuiModel(viewCache.getViewTitle(), viewCache.getViewSize());
        model.registerListener(DailyShop.getInstance());
        model.setCloseRemove(true);

        DailyShopView.initializeCommodities(model, player, viewCache);
        DailyShopView.initializeDisplayItem(model, viewCache);

        model.execute((e) -> {
            e.setCancelled(true);
            if (e.getClickedInventory() == e.getInventory()) {
                ItemStack itemStack = e.getCurrentItem();
                if (itemStack == null || itemStack.getType() == Material.AIR) {
                    return;
                }
                NBTItem nbtItem = new NBTItem(itemStack);
                Player clicker = (Player) e.getWhoClicked();
                if (nbtItem.hasTag("CommodityGroup") && nbtItem.hasTag("CommodityId")) {
                    if (CacheManager.getDayData().tryPurchase(clicker, nbtItem.getString("CommodityGroup"), nbtItem.getString("CommodityId"))) {
                        DailyShopView.open(clicker, viewKey);
                    }
                } else if (nbtItem.hasTag("DailyShopCommandKey")) {
                    String key = nbtItem.getString("DailyShopCommandKey");
                    for (String command : viewCache.getItemSection().getStringList(key + ".commands")) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", clicker.getName()));
                    }
                }
            }
        });
        model.openInventory(player);
    }

    private static void initializeCommodities(GuiModel model, Player player, ViewCache viewCache) {
        PlayerStorage playerStorage = CacheManager.getPlayerStorageMap().get(player.getUniqueId());
        if (playerStorage.hasGroup(viewCache.getGroupId())) {
            int[] commoditiySlots = viewCache.getCommoditySlots();
            List<PlayerStorage.CommodityEntry> commodityEntries = playerStorage.getGroupCache().get(viewCache.getGroupId());
            for (int i = 0; i < commodityEntries.size() && i < commoditiySlots.length; i++) {
                PlayerStorage.CommodityEntry commodityEntry = commodityEntries.get(i);
                CommodityCache commodityCache = CacheManager.getCommodityDataMap().get(commodityEntry.commodityId);
                int amount = commodityCache.getAll() - CacheManager.getDayData().getDayCommoditys().getOrDefault(commodityEntry.commodityId, 0);

                NBTItem nbtItem = new NBTItem(commodityCache.getDisplayItem(commodityEntry.discount, amount));
                nbtItem.setString("CommodityGroup", viewCache.getGroupId());
                nbtItem.setString("CommodityId", commodityEntry.commodityId);

                model.setItem(commoditiySlots[i], nbtItem.getItem());
            }
        }
    }

    private static void initializeDisplayItem(GuiModel model, ViewCache viewCache) {
        ConfigurationSection itemSection = viewCache.getItemSection();
        if (itemSection != null) {
            itemSection.getKeys(false).forEach(s -> {
                ConfigurationSection section = itemSection.getConfigurationSection(s);

                ItemStack itemStack = new ItemStack(Material.valueOf(section.getString("type")), section.getInt("amount"));
                ItemMeta itemMeta = itemStack.getItemMeta();

                if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_13_R1)) {
                    ((Damageable) itemMeta).setDamage((short) section.getInt("data"));
                    if (section.contains("custom-data")) {
                        itemMeta.setCustomModelData(section.getInt("custom-data"));
                    }
                } else {
                    itemStack.setDurability((short) section.getInt("data"));
                }

                itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', section.getString("name")));
                List<String> lore = new ArrayList<>();
                for (String text : section.getStringList("lore")) {
                    lore.add(ChatColor.translateAlternateColorCodes('&', text));
                }
                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);

                if (section.contains("commands")) {
                    NBTItem nbtItem = new NBTItem(itemStack);
                    nbtItem.setString("DailyShopCommandKey", s);
                    itemStack = nbtItem.getItem();
                }

                for (int slot : CommonUtil.formatSlots(section.getString("slot"))) {
                    model.setItem(slot, itemStack);
                }
            });
        }
    }
}
