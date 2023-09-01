package com.blank038.dailyshop.command;

import com.blank038.dailyshop.DailyShop;
import com.blank038.dailyshop.api.DailyShopApi;
import com.blank038.dailyshop.cacheframework.manager.CacheManager;
import com.blank038.dailyshop.view.DailyShopView;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MainCommand implements CommandExecutor {
    private final DailyShop main = DailyShop.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
        } else {
            switch (args[0].toLowerCase()) {
                case "open":
                    this.open(sender, args);
                    break;
                case "reload":
                    this.reload(sender);
                    break;
                case "reset":
                    this.reset(sender);
                    break;
                case "resetplayer":
                    this.resetPlayer(sender, args);
                    break;
                case "resetallplayer":
//                    this.resetAllPlayer(sender, args);
                    break;
                default:
                    sendHelp(sender);
                    break;
            }
        }
        return false;
    }

    private void sendHelp(CommandSender sender) {
        for (String text : main.getConfig().getStringList("message.help." + (sender.hasPermission("dailyshop.admin") ? "admin" : "default"))) {
            sender.sendMessage(text.replace("&", "§"));
        }
    }

    private void open(CommandSender sender, String[] args) {
        Player target = null;
        if (args.length == 1) {
            return;
        }
        if (args.length == 2) {
            target = (Player) sender;
        } else if (sender.hasPermission("dailyshop.open.control")) {
            target = Bukkit.getPlayerExact(args[2]);
        } else if (sender instanceof Player) {
            target = (Player) sender;
        }
        if (target != null) {
            DailyShopView.open(target, args[1]);
        }
    }

    private void reload(CommandSender sender) {
        if (sender.hasPermission("dailyshop.admin")) {
            this.main.loadConfig();
            sender.sendMessage(this.main.getString("message.reload", true));
        }
    }

    private void reset(CommandSender sender) {
        if (sender.hasPermission("dailyshop.admin")) {
            CacheManager.getDayData().reset();
            sender.sendMessage(this.main.getString("message.reset", true));
        }
    }

    private void resetPlayer(CommandSender sender, String[] args) {
        if (sender.hasPermission("dailyshop.admin")) {
            if (args.length == 1) {
                sender.sendMessage(this.main.getString("message.pls-enter-player-name", true));
                return;
            }
            Player player = Bukkit.getPlayerExact(args[1]);
            if (player == null || !player.isOnline()) {
                sender.sendMessage(this.main.getString("message.player-offline", true));
                return;
            }
            DailyShopApi.resetPlayerDailyCommodities(player);
            sender.sendMessage(this.main.getString("message.resetPlayer", true)
                    .replace("%player%", player.getName()));
        }
    }
}
