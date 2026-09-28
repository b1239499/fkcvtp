package com.vanishtp.plugin.listener;

import com.vanishtp.plugin.manager.VanishManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class TeleportUnvanishListener implements Listener {

    private final VanishManager vanishManager;

    public TeleportUnvanishListener(VanishManager vanishManager) {
        this.vanishManager = vanishManager;
    }

    /**
     * 通用版的「傳送後解除隱身」：不管伺服器上實際是哪一套 /tpa 或 /spawn 插件
     * 在處理傳送，只要隱身玩家透過「指令」成功被傳送（TeleportCause.COMMAND），
     * 就自動解除隱身。
     *
     * 我們自己的 /fkcvtp 傳送用的是預設 cause（PLUGIN），不會被這裡誤判而提早解除隱身。
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCommandTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (!vanishManager.isVanished(player)) return;
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.COMMAND) return;

        vanishManager.unvanish(player);
        player.sendMessage("§e隱身狀態已解除。");
    }
}
