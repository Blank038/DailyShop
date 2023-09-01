package com.blank038.dailyshop.util;

import com.blank038.dailyshop.enums.PayType;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * @author Blank038
 */
@Deprecated
public class EcoUtil {
    private static Economy economy;
    private static PlayerPointsAPI ppa;


    public static void init() {
        economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
        ppa = ((PlayerPoints) Bukkit.getPluginManager().getPlugin("PlayerPoints")).getAPI();
    }

    public static boolean balance(Player player, PayType payType, int price, String nyEconomyType) {
        boolean allow = false;
        switch (payType) {
            case VAULT:
                allow = economy.getBalance(player) >= price;
                break;
            case PLAYER_POINTS:
                allow = ppa.look(player.getUniqueId()) >= price;
                break;
            default:
                if (nyEconomyType != null) {
                    allow = com.mc9y.nyeconomy.Main.getNyEconomyAPI().getBalance(nyEconomyType, player.getName()) >= price;
                }
                break;
        }
        return allow;
    }

    public static void take(Player player, PayType payType, int price, String nyEconomyType) {
        switch (payType) {
            case VAULT:
                economy.withdrawPlayer(player, price);
                break;
            case PLAYER_POINTS:
                ppa.take(player.getUniqueId(), price);
                break;
            default:
                if (nyEconomyType != null) {
                    com.mc9y.nyeconomy.Main.getNyEconomyAPI().withdraw(nyEconomyType, player.getName(), price);
                }
                break;
        }
    }
}
