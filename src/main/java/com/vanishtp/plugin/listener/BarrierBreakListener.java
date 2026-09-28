package com.vanishtp.plugin.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;

public class BarrierBreakListener implements Listener {

    /**
     * 屏障方塊在生存模式硬度為 -1（無法破壞）。
     * 玩家有 vanishtp.breakbarrier 權限、且主手拿著屏障方塊時，開始挖屏障就設為瞬間破壞。
     * 走的是正常破壞流程，之後仍會觸發 BlockBreakEvent，保護區插件一樣能攔截。
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBarrierDamage(BlockDamageEvent event) {
        if (event.getBlock().getType() != Material.BARRIER) return;

        Player player = event.getPlayer();
        if (!player.hasPermission("vanishtp.breakbarrier")) return;
        if (player.getInventory().getItemInMainHand().getType() != Material.BARRIER) return;

        event.setInstaBreak(true);
    }
}
