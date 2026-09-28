package com.vanishtp.plugin.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LocateCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("此指令只能由玩家執行。");
            return true;
        }
        if (!player.hasPermission("vanishtp.locate")) {
            player.sendMessage("§c你沒有權限使用此指令。");
            return true;
        }
        if (args.length != 1) {
            player.sendMessage("§c用法：/fkclocate <地形名稱>");
            player.sendMessage("§c地形名稱請用原版小寫英文 ID，例如：desert、jungle、plains、ocean、savanna...");
            return true;
        }

        String biome = args[0].toLowerCase();

        // 借用原版指令本身的地形搜尋邏輯（由 Mojang 實作，保證效能與 Folia 相容性），
        // 用 console 以 /execute as ... at ... 代替玩家執行，
        // 玩家本身不需要原版的 minecraft.command.locate 權限，改由 vanishtp.locate 控管。
        String cmd = "execute as " + player.getName() + " at @s run locate biome minecraft:" + biome;

        boolean dispatched;
        try {
            dispatched = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        } catch (Exception ex) {
            dispatched = false;
        }

        if (!dispatched) {
            player.sendMessage("§c搜尋失敗，請確認地形名稱是否正確（原版小寫英文 ID）。");
        }
        return true;
    }
}
