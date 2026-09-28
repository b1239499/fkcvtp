package com.vanishtp.plugin.command;

import com.vanishtp.plugin.manager.VanishManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpCommand implements CommandExecutor {

    private final VanishManager vanishManager;

    public TpCommand(VanishManager vanishManager) {
        this.vanishManager = vanishManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("此指令只能由玩家執行。");
            return true;
        }
        if (!player.hasPermission("vanishtp.use")) {
            player.sendMessage("§c你沒有權限使用此指令。");
            return true;
        }
        if (!vanishManager.isVanished(player)) {
            player.sendMessage("§c你必須先使用 /fkcvanish 進入隱身狀態，才能直接傳送。");
            return true;
        }

        if (args.length == 1) {
            teleportToPlayer(player, args[0]);
            return true;
        }

        if (args.length == 3 || args.length == 4) {
            if (!player.hasPermission("vanishtp.tpcoords")) {
                player.sendMessage("§c你沒有權限傳送到指定座標。");
                return true;
            }
            teleportToCoordinates(player, args);
            return true;
        }

        player.sendMessage("§c用法：/fkcvtp <玩家>");
        player.sendMessage("§c或　：/fkcvtp <x> <y> <z> [世界名稱]");
        return true;
    }

    private void teleportToPlayer(Player player, String targetName) {
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            player.sendMessage("§c找不到玩家 " + targetName);
            return;
        }
        if (target.equals(player)) {
            player.sendMessage("§c不能傳送到自己身上。");
            return;
        }

        // Folia 安全的跨 region 非同步傳送
        player.teleportAsync(target.getLocation()).thenAccept(success -> {
            if (Boolean.TRUE.equals(success)) {
                player.sendMessage("§a已隱身傳送到 " + target.getName() + " 身邊。");
            } else {
                player.sendMessage("§c傳送失敗。");
            }
        });
    }

    private void teleportToCoordinates(Player player, String[] args) {
        double x, y, z;
        try {
            x = Double.parseDouble(args[0]);
            y = Double.parseDouble(args[1]);
            z = Double.parseDouble(args[2]);
        } catch (NumberFormatException ex) {
            player.sendMessage("§cx、y、z 必須是數字。");
            return;
        }

        World resolvedWorld = player.getWorld();
        if (args.length == 4) {
            World specified = Bukkit.getWorld(args[3]);
            if (specified == null) {
                player.sendMessage("§c找不到世界 " + args[3]);
                return;
            }
            resolvedWorld = specified;
        }
        final World world = resolvedWorld;

        Location location = new Location(world, x, y, z, player.getLocation().getYaw(), player.getLocation().getPitch());

        player.teleportAsync(location).thenAccept(success -> {
            if (Boolean.TRUE.equals(success)) {
                player.sendMessage(String.format("§a已隱身傳送到座標 (%.1f, %.1f, %.1f)，世界：%s",
                        x, y, z, world.getName()));
            } else {
                player.sendMessage("§c傳送失敗。");
            }
        });
    }
}
