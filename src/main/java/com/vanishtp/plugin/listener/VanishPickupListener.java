package com.vanishtp.plugin.listener;

import com.vanishtp.plugin.manager.VanishManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class VanishPickupListener implements Listener {

    private final VanishManager vanishManager;

    public VanishPickupListener(VanishManager vanishManager) {
        this.vanishManager = vanishManager;
    }

    /** 隱身狀態下不能撿取物品，避免巡查時不小心撿走場上的東西 */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player && vanishManager.isVanished(player)) {
            event.setCancelled(true);
        }
    }
}
