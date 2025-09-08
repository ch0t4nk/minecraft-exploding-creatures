package com.example.boombarnyard;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.entity.Player;

public class FallDamageListener implements Listener {
    private final BoomBarnyardPlugin plugin;
    public FallDamageListener(BoomBarnyardPlugin plugin){ this.plugin = plugin; }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        Player p = (Player) event.getEntity();
        if (plugin.isFallImmune(p.getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
