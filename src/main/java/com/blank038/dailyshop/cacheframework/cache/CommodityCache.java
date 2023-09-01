package com.blank038.dailyshop.cacheframework.cache;

import com.blank038.dailyshop.enums.PayType;
import com.blank038.dailyshop.util.TextUtil;
import de.tr7zw.nbtapi.NBTItem;
import de.tr7zw.nbtapi.utils.MinecraftVersion;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CommodityCache {
    private final boolean enableLimit;
    private final int oneself, all, price, min, max, amount, data, customData;
    private final String displayName, itemType;
    private final List<String> lore, commands;
    private final PayType payType;
    private String vaultName;

    public CommodityCache(ConfigurationSection section) {
        enableLimit = section.getBoolean("limit.enable");
        oneself = section.getInt("limit.oneself");
        all = section.getInt("limit.all");
        price = section.getInt("price");

        switch (section.getString("type").toLowerCase()) {
            case "vault":
                payType = PayType.VAULT;
                break;
            case "playerpoints":
                payType = PayType.PLAYER_POINTS;
                break;
            default:
                payType = PayType.NY_ECONOMY;
                vaultName = section.getString("type");
                break;
        }
        String[] split = section.getString("random").split("-");
        min = Integer.parseInt(split[0]);
        max = Integer.parseInt(split[1]);

        displayName = TextUtil.formatHexColor(section.getString("item.name"));
        itemType = section.getString("item.id");
        data = section.getInt("item.data");
        this.customData = section.getInt("item.customData", -1);
        amount = section.getInt("item.amount");
        lore = section.getStringList("item.lore");
        commands = section.getStringList("commands");
    }

    public ItemStack getDisplayItem(int discount, int amount) {
        ItemStack itemStack = new ItemStack(Material.valueOf(this.itemType), this.amount);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_13_R1)) {
            ((Damageable) itemMeta).setDamage(this.data);
            if (customData != -1) {
                itemMeta.setCustomModelData(this.customData);
            }
        } else {
            itemStack.setDurability((short) this.data);
        }
        itemMeta.setDisplayName(displayName);
        List<String> lore = new ArrayList<>();
        int now = (int) (price * (discount * 0.1));
        for (String l : this.lore) {
            lore.add(TextUtil.formatHexColor(l)
                    .replace("%discount%", String.valueOf(discount))
                    .replace("%now%", String.valueOf(now))
                    .replace("%original%", String.valueOf(price))
                    .replace("%amount%", String.valueOf(amount)));
        }
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public int randomDiscount() {
        return (int) (min + Math.random() * ((max + 1) - min));
    }

    public void give(Player player) {
        this.commands.forEach(s -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player%", player.getName())));
    }
}