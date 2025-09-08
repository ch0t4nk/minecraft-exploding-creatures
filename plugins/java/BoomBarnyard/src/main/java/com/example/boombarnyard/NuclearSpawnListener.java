package com.example.boombarnyard;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import java.util.concurrent.ThreadLocalRandom;

public class NuclearSpawnListener implements Listener {
    private final BoomBarnyardPlugin plugin;
    public NuclearSpawnListener(BoomBarnyardPlugin plugin){ this.plugin = plugin; }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent event) {
        if (!plugin.isNuclearEnabled()) return;
    LivingEntity c = event.getEntity();
    EntityType type = c.getType();
        String name = type.name();
        if (!plugin.isEntityEnabled(name)) return;
        double chance = plugin.getNuclearChance();
        if (chance <= 0) return;
        if (ThreadLocalRandom.current().nextDouble() < chance) {
            plugin.markNuclear(c);
            plugin.getLogger().info("Marked NUCLEAR entity: " + name + " at " + c.getLocation().getBlockX()+","+c.getLocation().getBlockY()+","+c.getLocation().getBlockZ());
            try {
                Sound s = Sound.valueOf(plugin.getNuclearTagSound());
                c.getWorld().playSound(c.getLocation(), s, 1.5f, 0.6f);
            } catch (Exception ignored) {}
        }
    }
}
