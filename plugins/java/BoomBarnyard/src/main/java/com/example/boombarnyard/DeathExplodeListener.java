package com.example.boombarnyard;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;

public class DeathExplodeListener implements Listener {

    private final BoomBarnyardPlugin plugin;

    public DeathExplodeListener(BoomBarnyardPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        EntityType type = entity.getType();
        String name = type.name();
        if (!plugin.isEntityEnabled(name)) {
            return; // Not configured to explode
        }
        Location loc = entity.getLocation();
        World world = loc.getWorld();
        if (world == null) return;

        // Nuclear override path
        if (plugin.isNuclearEnabled() && plugin.isNuclear(entity)) {
            plugin.getLogger().info("Nuclear entity death detected (" + name + ") at " + loc.getBlockX()+","+loc.getBlockY()+","+loc.getBlockZ());
            plugin.cancelAura(entity);
            plugin.handleNuclearExplosion(loc, type);
            return;
        }

        double power = plugin.getPowerForEntity(name);
        boolean fire = plugin.isFire();
        boolean breakBlocks = plugin.isBreakBlocks();

        if (plugin.isPreBoomEnabled()) {
            int ticks = plugin.getTicksBeforeBoom();
            String soundName = plugin.getPreBoomSound();
            String particleName = plugin.getPreBoomParticle();
            String msg = plugin.getPreBoomMessage();
            plugin.getLogger().info("Scheduling explosion for " + name + " in " + ticks + " ticks (power=" + power + ") at " + loc.getBlockX()+","+loc.getBlockY()+","+loc.getBlockZ());
            // Play sound and particle immediately, then schedule explosion
            try {
                Sound s = Sound.valueOf(soundName);
                world.playSound(loc, s, 1.0f, 1.0f);
            } catch (Exception ignored) {}
            try {
                Particle p = Particle.valueOf(particleName);
                world.spawnParticle(p, loc, 20, 0.5, 0.5, 0.5, 0.1);
            } catch (Exception ignored) {}
            if (msg != null && !msg.isEmpty()) {
                // send warning message to nearby players only
                double radius = 10.0;
                world.getPlayers().stream()
                        .filter(p -> p.getLocation().distanceSquared(loc) <= radius * radius)
                        .forEach(p -> p.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', msg)));
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    World w = loc.getWorld();
                    if (w == null) return;
            plugin.getLogger().info("Executing scheduled explosion for " + name + " at " + loc.getBlockX()+","+loc.getBlockY()+","+loc.getBlockZ());
                    w.createExplosion(loc.getX(), loc.getY(), loc.getZ(), (float) power, fire, breakBlocks);
                }
            }.runTaskLater(plugin, ticks);
        } else {
        plugin.getLogger().info("Immediate explosion for " + name + " (power=" + power + ") at " + loc.getBlockX()+","+loc.getBlockY()+","+loc.getBlockZ());
            world.createExplosion(loc.getX(), loc.getY(), loc.getZ(), (float) power, fire, breakBlocks);
        }
    }
}
