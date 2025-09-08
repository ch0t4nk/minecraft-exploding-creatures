package com.example.boombarnyard;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class BoomBarnyardPlugin extends JavaPlugin {

    private double explosionPower;
    private double maxExplosionPower; // new clamp value
    private boolean breakBlocks;
    private boolean fire;
    private java.util.Set<String> enabledEntities;
    private java.util.Map<String, Double> perEntityPowers;
    private boolean preBoomEnabled;
    private int ticksBeforeBoom;
    private String preBoomSound;
    private String preBoomParticle;
    private String preBoomMessage;

    // Nuclear settings
    private boolean nuclearEnabled;
    private double nuclearChance;
    private double nuclearPower;
    private boolean nuclearReduceToOneHP;
    private double nuclearEffectRadius;
    private double nuclearLaunchVertical;
    private double nuclearLaunchHorizontal;
    private double nuclearKnockbackMultiplier; // new multiplier to scale horizontal & vertical launch
    private int nuclearFallImmunityTicks;
    private boolean nuclearGlow;
    private String nuclearMessage;
    private int nuclearParticles;
    private String nuclearTagSound;
    private String nuclearDetonateSound;

    private NamespacedKey nuclearKey;
    private java.util.Set<java.util.UUID> fallImmunePlayers;
    // Countdown settings
    private boolean nuclearCountdownEnabled;
    private int nuclearCountdownSeconds;
    private String nuclearCountdownTitle;
    private String nuclearCountdownSoundEach;
    private String nuclearCountdownFinalSound;
    private org.bukkit.boss.BarColor nuclearCountdownColor;
    private org.bukkit.boss.BarStyle nuclearCountdownStyle;

    // Glow / aura settings
    private String nuclearGlowTeamColor;
    private boolean nuclearAuraEnabled;
    private String nuclearAuraParticle;
    private int nuclearAuraInterval;
    private int nuclearAuraCount;
    private double nuclearAuraRadius;
    private double nuclearAuraExtra;
    private java.util.Map<java.util.UUID, Integer> auraTasks = new java.util.concurrent.ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();
    this.nuclearKey = new NamespacedKey(this, "nuclear");
    this.fallImmunePlayers = java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());
    Bukkit.getPluginManager().registerEvents(new DeathExplodeListener(this), this);
    Bukkit.getPluginManager().registerEvents(new NuclearSpawnListener(this), this);
    Bukkit.getPluginManager().registerEvents(new FallDamageListener(this), this);
        getLogger().info("BoomBarnyard enabled. Pigs and chickens will go BOOM!");

        runSelfTestIfRequested();
    }

    @Override
    public void onDisable() {
        getLogger().info("BoomBarnyard disabled.");
    }

    public void reloadBoomConfig() {
        reloadConfig();
        loadSettings();
    }

    private void loadSettings() {
        FileConfiguration cfg = getConfig();
        this.explosionPower = cfg.getDouble("explosion-power", 3.0);
        this.maxExplosionPower = Math.max(0.1, cfg.getDouble("max-explosion-power", 6.0));
        this.breakBlocks = cfg.getBoolean("break-blocks", false);
        this.fire = cfg.getBoolean("fire", false);
        this.enabledEntities = new java.util.HashSet<>(cfg.getStringList("enabled-entities"));
        this.perEntityPowers = new java.util.HashMap<>();
        java.util.Map<String, Object> map = cfg.getConfigurationSection("per-entity-powers") != null
                ? cfg.getConfigurationSection("per-entity-powers").getValues(false)
                : java.util.Collections.emptyMap();
        for (java.util.Map.Entry<String, Object> e : map.entrySet()) {
            try {
                double val = Double.parseDouble(e.getValue().toString());
                this.perEntityPowers.put(e.getKey(), clampPower(val));
            } catch (Exception ex) {
                getLogger().warning("Invalid power for entity " + e.getKey() + ", skipping");
            }
        }
        this.preBoomEnabled = cfg.getBoolean("pre-boom.enabled", true);
        this.ticksBeforeBoom = cfg.getInt("pre-boom.ticks-before-boom", 20);
        this.preBoomSound = cfg.getString("pre-boom.sound", "ENTITY_CREEPER_PRIMED");
        this.preBoomParticle = cfg.getString("pre-boom.particle", "EXPLOSION_NORMAL");
        this.preBoomMessage = cfg.getString("pre-boom.warning-message", "&cBoom incoming!");

    // Nuclear load
    this.nuclearEnabled = cfg.getBoolean("nuclear.enabled", true);
    this.nuclearChance = Math.max(0.0, Math.min(1.0, cfg.getDouble("nuclear.chance", 0.02)));
    this.nuclearPower = Math.max(0.1, cfg.getDouble("nuclear.power", 20.0));
    this.nuclearReduceToOneHP = cfg.getBoolean("nuclear.reduce-to-one-hp", true);
    this.nuclearEffectRadius = Math.max(1.0, cfg.getDouble("nuclear.effect-radius", 25.0));
    this.nuclearLaunchVertical = cfg.getDouble("nuclear.launch-vertical", 2.5);
    this.nuclearLaunchHorizontal = cfg.getDouble("nuclear.launch-horizontal", 1.2);
    this.nuclearKnockbackMultiplier = Math.max(0.1, cfg.getDouble("nuclear.knockback-multiplier", 1.0));
    this.nuclearFallImmunityTicks = Math.max(0, cfg.getInt("nuclear.fall-immunity-ticks", 200));
    this.nuclearGlow = cfg.getBoolean("nuclear.glow", true);
    this.nuclearMessage = cfg.getString("nuclear.message", "&6NUCLEAR BARNYARD DETONATION! You barely survived!");
    this.nuclearParticles = Math.max(0, cfg.getInt("nuclear.particles", 400));
    this.nuclearTagSound = cfg.getString("nuclear.tag-sound", "ENTITY_WITHER_SPAWN");
    this.nuclearDetonateSound = cfg.getString("nuclear.detonate-sound", "ENTITY_ENDER_DRAGON_GROWL");
    // Countdown load
    this.nuclearCountdownEnabled = cfg.getBoolean("nuclear.countdown.enabled", true);
    this.nuclearCountdownSeconds = Math.max(1, cfg.getInt("nuclear.countdown.seconds", 5));
    this.nuclearCountdownTitle = cfg.getString("nuclear.countdown.title", "&cNUCLEAR DETONATION IN {s}...");
    this.nuclearCountdownSoundEach = cfg.getString("nuclear.countdown.sound-each-tick", "BLOCK_NOTE_BLOCK_HAT");
    this.nuclearCountdownFinalSound = cfg.getString("nuclear.countdown.final-sound", "ENTITY_ENDER_DRAGON_GROWL");
    try { this.nuclearCountdownColor = org.bukkit.boss.BarColor.valueOf(cfg.getString("nuclear.countdown.color", "RED").toUpperCase()); }
    catch (Exception ex) { this.nuclearCountdownColor = org.bukkit.boss.BarColor.RED; }
    try { this.nuclearCountdownStyle = org.bukkit.boss.BarStyle.valueOf(cfg.getString("nuclear.countdown.style", "SEGMENTED_10").toUpperCase()); }
    catch (Exception ex) { this.nuclearCountdownStyle = org.bukkit.boss.BarStyle.SEGMENTED_10; }

    // Glow / aura load
    this.nuclearGlowTeamColor = cfg.getString("nuclear.glow-team-color", "YELLOW").toUpperCase();
    this.nuclearAuraEnabled = cfg.getBoolean("nuclear.aura.enabled", true);
    this.nuclearAuraParticle = cfg.getString("nuclear.aura.particle", "GLOW");
    this.nuclearAuraInterval = Math.max(1, cfg.getInt("nuclear.aura.interval-ticks", 10));
    this.nuclearAuraCount = Math.max(1, cfg.getInt("nuclear.aura.count", 12));
    this.nuclearAuraRadius = Math.max(0.1, cfg.getDouble("nuclear.aura.radius", 0.6));
    this.nuclearAuraExtra = Math.max(0.0, cfg.getDouble("nuclear.aura.extra", 0.01));
    }

    private double clampPower(double val) {
        if (val < 0.1) return 0.1; // minimal meaningful explosion
        if (val > maxExplosionPower) return maxExplosionPower;
        return val;
    }

    public double getExplosionPower() { return clampPower(explosionPower); }
    public boolean isBreakBlocks() { return breakBlocks; }
    public boolean isFire() { return fire; }
    public boolean isEntityEnabled(String typeName) { return enabledEntities.contains(typeName); }
    public double getPowerForEntity(String typeName) { return clampPower(perEntityPowers.getOrDefault(typeName, explosionPower)); }
    public double getMaxExplosionPower() { return maxExplosionPower; }

    public boolean isPreBoomEnabled() { return preBoomEnabled; }
    public int getTicksBeforeBoom() { return ticksBeforeBoom; }
    public String getPreBoomSound() { return preBoomSound; }
    public String getPreBoomParticle() { return preBoomParticle; }
    public String getPreBoomMessage() { return preBoomMessage; }

    // Nuclear getters
    public boolean isNuclearEnabled() { return nuclearEnabled; }
    public double getNuclearChance() { return nuclearChance; }
    public double getNuclearPower() { return nuclearPower; }
    public boolean isNuclearReduceToOneHP() { return nuclearReduceToOneHP; }
    public double getNuclearEffectRadius() { return nuclearEffectRadius; }
    public double getNuclearLaunchVertical() { return nuclearLaunchVertical; }
    public double getNuclearLaunchHorizontal() { return nuclearLaunchHorizontal; }
    public double getNuclearKnockbackMultiplier() { return nuclearKnockbackMultiplier; }
    public int getNuclearFallImmunityTicks() { return nuclearFallImmunityTicks; }
    public boolean isNuclearGlow() { return nuclearGlow; }
    public String getNuclearMessage() { return nuclearMessage; }
    public int getNuclearParticles() { return nuclearParticles; }
    public String getNuclearTagSound() { return nuclearTagSound; }
    public String getNuclearDetonateSound() { return nuclearDetonateSound; }
    public boolean isNuclearCountdownEnabled() { return nuclearCountdownEnabled; }
    public int getNuclearCountdownSeconds() { return nuclearCountdownSeconds; }
    public String getNuclearCountdownTitle() { return nuclearCountdownTitle; }
    public String getNuclearCountdownSoundEach() { return nuclearCountdownSoundEach; }
    public String getNuclearCountdownFinalSound() { return nuclearCountdownFinalSound; }
    public org.bukkit.boss.BarColor getNuclearCountdownColor() { return nuclearCountdownColor; }
    public org.bukkit.boss.BarStyle getNuclearCountdownStyle() { return nuclearCountdownStyle; }
    public boolean isNuclearAuraEnabled() { return nuclearAuraEnabled; }
    public String getNuclearAuraParticle() { return nuclearAuraParticle; }

    public void markNuclear(LivingEntity entity) {
        try {
            entity.getPersistentDataContainer().set(nuclearKey, PersistentDataType.BYTE, (byte)1);
            if (nuclearGlow) entity.setGlowing(true);
            if (nuclearAuraEnabled) scheduleAura(entity);
            // Attempt scoreboard team coloring for outline (best-effort)
            try {
                org.bukkit.scoreboard.Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
                final String teamName = "BBNUKE"; // single team color for all
                org.bukkit.scoreboard.Team team = sb.getTeam(teamName);
                if (team == null) {
                    team = sb.registerNewTeam(teamName);
                    try { team.setColor(org.bukkit.ChatColor.valueOf(nuclearGlowTeamColor)); } catch (Exception ignored) {}
                    team.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
                }
                try { team.setColor(org.bukkit.ChatColor.valueOf(nuclearGlowTeamColor)); } catch (Exception ignored) {}
                team.addEntry(entity.getUniqueId().toString());
            } catch (Exception ignored) {}
        } catch (Exception ignored) {}
    }

    private void scheduleAura(LivingEntity entity) {
        java.util.UUID id = entity.getUniqueId();
        cancelAura(id); // ensure none duplicated
        Particle particle;
        try { particle = Particle.valueOf(nuclearAuraParticle); } catch (Exception ex) { particle = Particle.GLOW; }
        final LivingEntity target = entity;
        final Particle pRef = particle;
        final double r = nuclearAuraRadius;
        final double extra = nuclearAuraExtra;
        final int count = nuclearAuraCount;
        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
            @Override public void run() {
                if (target.isDead() || !target.isValid()) { cancelAura(id); return; }
                org.bukkit.Location base = target.getLocation().add(0, target.getHeight() * 0.5, 0);
                try {
                    target.getWorld().spawnParticle(pRef, base, count, r, r/2.0, r, extra);
                } catch (Exception ignored) {}
            }
        }, 0L, nuclearAuraInterval);
        auraTasks.put(id, taskId);
    }

    public void cancelAura(java.util.UUID id) {
        Integer task = auraTasks.remove(id);
        if (task != null) Bukkit.getScheduler().cancelTask(task);
    }

    public void cancelAura(org.bukkit.entity.Entity e) { if (e != null) cancelAura(e.getUniqueId()); }

    public boolean isNuclear(org.bukkit.entity.Entity e) {
        try {
            if (!(e instanceof LivingEntity)) return false;
            Byte b = e.getPersistentDataContainer().get(nuclearKey, PersistentDataType.BYTE);
            return b != null && b == 1;
        } catch (Exception ex) {
            return false;
        }
    }

    public void addFallImmunity(Player p, int ticks) {
        fallImmunePlayers.add(p.getUniqueId());
        new BukkitRunnable(){
            @Override public void run(){ fallImmunePlayers.remove(p.getUniqueId()); }
        }.runTaskLater(this, ticks);
    }

    public boolean isFallImmune(java.util.UUID id) { return fallImmunePlayers.contains(id); }

    public void handleNuclearExplosion(org.bukkit.Location loc, EntityType sourceType) {
        // Visual pre effects: particles & sound
        try {
            Sound det = Sound.valueOf(nuclearDetonateSound);
            loc.getWorld().playSound(loc, det, 4.0f, 1.0f);
        } catch (Exception ignored) {}
        if (nuclearParticles > 0) {
            try { loc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc, 1); } catch (Exception ignored) {}
            try { loc.getWorld().spawnParticle(Particle.CLOUD, loc, nuclearParticles, 4, 2, 4, 0.05); } catch (Exception ignored) {}
        }

        // Record players in radius before explosion for post processing
        java.util.List<Player> affected = new java.util.ArrayList<>();
        for (Player p : loc.getWorld().getPlayers()) {
            if (p.getLocation().distanceSquared(loc) <= nuclearEffectRadius * nuclearEffectRadius) {
                affected.add(p);
                // Temporary resistance to avoid death
                p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 4, false, false, false));
            }
        }

        // Create large explosion (respect break-blocks & fire config)
        loc.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), (float)nuclearPower, fire, breakBlocks);
        getLogger().info("NUCLEAR detonation at " + loc.getBlockX()+","+loc.getBlockY()+","+loc.getBlockZ()+" power="+nuclearPower+" radius="+nuclearEffectRadius);

        // Post tick adjustments
        new BukkitRunnable(){
            @Override public void run(){
                for (Player p : affected) {
                    if (!p.isOnline()) continue;
                    // Launch vector
                    org.bukkit.util.Vector dir = p.getLocation().toVector().subtract(loc.toVector());
                    dir.setY(0);
                    if (dir.lengthSquared() < 0.01) dir = new org.bukkit.util.Vector(0,0,0.01);
                    double mult = nuclearKnockbackMultiplier;
                    dir.normalize().multiply(nuclearLaunchHorizontal * mult).setY(nuclearLaunchVertical * mult);
                    p.setVelocity(dir);
                    addFallImmunity(p, nuclearFallImmunityTicks);
                    if (nuclearReduceToOneHP && p.getHealth() > 1.0) {
                        try { p.setHealth(1.0); } catch (Exception ignored) {}
                    }
                    if (nuclearMessage != null && !nuclearMessage.isEmpty()) {
                        p.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', nuclearMessage));
                    }
                }
            }
        }.runTaskLater(this, 2);

        // Spawn gore-like item particles (visual only, no pickup)
        Material mat = (sourceType == EntityType.CHICKEN) ? Material.CHICKEN : Material.PORKCHOP;
        for (int i=0;i<12;i++) {
            ItemStack stack = new ItemStack(mat, 1);
            org.bukkit.entity.Item item = loc.getWorld().dropItem(loc, stack);
            item.setPickupDelay(Integer.MAX_VALUE);
            double angle = (Math.PI * 2.0) * (i / 12.0);
            double speed = 0.6 + (i % 3) * 0.1;
            item.setVelocity(new org.bukkit.util.Vector(Math.cos(angle)*speed, 0.7 + (i%4)*0.05, Math.sin(angle)*speed));
            new BukkitRunnable(){ @Override public void run(){ item.remove(); } }.runTaskLater(this, 100);
        }
    }

    public void scheduleNuclearCountdown(org.bukkit.Location loc, EntityType sourceType) {
        if (!nuclearCountdownEnabled) { handleNuclearExplosion(loc, sourceType); return; }
        final int total = nuclearCountdownSeconds;
        org.bukkit.boss.BossBar bar = Bukkit.createBossBar("", nuclearCountdownColor, nuclearCountdownStyle);
        java.util.List<Player> viewers = new java.util.ArrayList<>();
        for (Player p : loc.getWorld().getPlayers()) {
            if (p.getLocation().distanceSquared(loc) <= (nuclearEffectRadius * nuclearEffectRadius) * 4) { // show a bit farther
                bar.addPlayer(p);
                viewers.add(p);
            }
        }
        new BukkitRunnable(){
            int remaining = total;
            @Override public void run(){
                if (remaining <= 0) {
                    try { Sound fs = Sound.valueOf(nuclearCountdownFinalSound); loc.getWorld().playSound(loc, fs, 3f, 1f);} catch (Exception ignored) {}
                    bar.removeAll();
                    handleNuclearExplosion(loc, sourceType);
                    cancel();
                    return;
                }
                double progress = (double)remaining / (double)total;
                bar.setProgress(Math.min(1.0, Math.max(0.0, progress)));
                String title = nuclearCountdownTitle.replace("{s}", Integer.toString(remaining));
                bar.setTitle(org.bukkit.ChatColor.translateAlternateColorCodes('&', title));
                try { Sound tickS = Sound.valueOf(nuclearCountdownSoundEach); loc.getWorld().playSound(loc, tickS, 1.2f, 1.0f);} catch (Exception ignored) {}
                remaining--;
            }
        }.runTaskTimer(this, 0L, 20L);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Legacy single-purpose reload command
        if (command.getName().equalsIgnoreCase("boombarnyardreload")) {
            if (!sender.hasPermission("boombarnyard.reload")) {
                sender.sendMessage("§cYou don't have permission.");
                return true;
            }
            long start = System.currentTimeMillis();
            reloadBoomConfig();
            long took = System.currentTimeMillis() - start;
            sender.sendMessage("§aBoomBarnyard config reloaded in " + took + "ms. (max=" + getMaxExplosionPower() + ")");
            // Allow re-run after reload if trigger file placed again before restart
            runSelfTestIfRequested();
            return true;
        }
        // New unified command
        if (command.getName().equalsIgnoreCase("boombarnyard")) {
            if (!sender.hasPermission("boombarnyard.use")) {
                sender.sendMessage("§cMissing permission: boombarnyard.use");
                return true;
            }
            if (args.length == 0) {
                sender.sendMessage("§eUsage: /boombarnyard reload | /boombarnyard test <ENTITY> | /boombarnyard nukeall [ENTITY|ALL]");
                return true;
            }
            String sub = args[0].toLowerCase();
            getLogger().info("/boombarnyard invoked by " + sender.getName() + " sub='" + sub + "' args=" + java.util.Arrays.toString(args));
            switch (sub) {
                case "reload":
                    if (!sender.hasPermission("boombarnyard.reload")) {
                        sender.sendMessage("§cMissing permission: boombarnyard.reload");
                        return true;
                    }
                    long start = System.currentTimeMillis();
                    reloadBoomConfig();
                    long took = System.currentTimeMillis() - start;
                    sender.sendMessage("§aConfig reloaded in " + took + "ms. (max=" + getMaxExplosionPower() + ")");
                    runSelfTestIfRequested();
                    return true;
                case "test":
                    if (!sender.hasPermission("boombarnyard.test")) {
                        sender.sendMessage("§cMissing permission: boombarnyard.test");
                        return true;
                    }
                    String entName = (args.length >= 2) ? args[1].toUpperCase() : "PIG";
                    org.bukkit.entity.EntityType et;
                    try {
                        et = org.bukkit.entity.EntityType.valueOf(entName);
                    } catch (IllegalArgumentException ex) {
                        sender.sendMessage("§cUnknown entity type: " + entName);
                        return true;
                    }
                    org.bukkit.World w = Bukkit.getWorlds().get(0);
                    org.bukkit.Location loc = w.getSpawnLocation().clone().add(1, 0, 0);
                    org.bukkit.entity.Entity spawned = w.spawnEntity(loc, et);
                    sender.sendMessage("§aSpawned test entity " + entName + ", killing in 1 tick... (max=" + getMaxExplosionPower() + ")");
                    getLogger().info("Manual test spawn: " + entName + " at " + loc.getBlockX()+","+loc.getBlockY()+","+loc.getBlockZ());
                    new org.bukkit.scheduler.BukkitRunnable(){
                        @Override
                        public void run(){
                            if (spawned instanceof org.bukkit.entity.LivingEntity) {
                                ((org.bukkit.entity.LivingEntity) spawned).damage(1000.0);
                                sender.sendMessage("§aApplied lethal damage to test entity.");
                            } else {
                                spawned.remove();
                                sender.sendMessage("§eEntity was not living; removed.");
                            }
                        }
                    }.runTaskLater(this,1);
                    return true;
                case "nukeall":
                    if (!sender.hasPermission("boombarnyard.nukeall")) {
                        sender.sendMessage("§cMissing permission: boombarnyard.nukeall");
                        return true;
                    }
                    String filter = (args.length >= 2) ? args[1].toUpperCase() : "ALL";
                    int converted = 0;
                    int skipped = 0;
                    org.bukkit.World world = Bukkit.getWorlds().get(0);
                    for (org.bukkit.entity.Entity e : world.getEntities()) {
                        if (!(e instanceof org.bukkit.entity.LivingEntity)) continue;
                        if (e instanceof org.bukkit.entity.Player) continue;
                        org.bukkit.entity.LivingEntity le = (org.bukkit.entity.LivingEntity) e;
                        String typeName = le.getType().name();
                        if (!isEntityEnabled(typeName)) continue; // only configured ones
                        if (!filter.equals("ALL") && !typeName.equals(filter)) continue;
                        if (isNuclear(le)) { skipped++; continue; }
                        markNuclear(le);
                        converted++;
                    }
                    sender.sendMessage("§eNukeAll: converted=" + converted + " already-nuclear=" + skipped + " filter=" + filter);
                    getLogger().info("/boombarnyard nukeall by " + sender.getName() + " converted=" + converted + " skipped=" + skipped + " filter=" + filter);
                    return true;
                default:
                    sender.sendMessage("§cUnknown subcommand. Use reload, test, or nukeall.");
                    return true;
            }
        }
        return false;
    }

    @Override
    public java.util.List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!command.getName().equalsIgnoreCase("boombarnyard")) return null;
        java.util.List<String> list = new java.util.ArrayList<>();
        if (args.length == 1) {
            java.util.List<String> base = java.util.Arrays.asList("reload", "test", "nukeall");
            for (String s : base) if (s.startsWith(args[0].toLowerCase())) list.add(s);
            return list;
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if ("test".equals(sub)) {
                for (EntityType et : EntityType.values()) {
                    if (et.isAlive() && et.name().startsWith(args[1].toUpperCase())) list.add(et.name());
                }
                return list;
            }
            if ("nukeall".equals(sub)) {
                list.add("ALL");
                for (String e : enabledEntities) if (e.startsWith(args[1].toUpperCase())) list.add(e);
                return list;
            }
        }
        return java.util.Collections.emptyList();
    }

    private void runSelfTestIfRequested() {
        try {
            FileConfiguration cfg = getConfig();
            java.io.File data = getDataFolder();
            if (!data.exists()) data.mkdirs();
            java.io.File trigger = new java.io.File(data, "run-self-test");
            boolean enabled = cfg.getBoolean("self-test.enabled", false);
            boolean always = cfg.getBoolean("self-test.always-run", false);
            getLogger().info("Self-test check: dataFolder=" + data.getAbsolutePath() + ", triggerExists=" + trigger.exists() + ", enabled=" + enabled + ", always-run=" + always);
            if (!(trigger.exists() || (enabled && always) || (enabled && !new java.io.File(data, ".self-test-done").exists()))) {
                return; // nothing to do
            }
            String entName = cfg.getString("self-test.entity", "PIG");
            int delay = cfg.getInt("self-test.delay-after-enable-ticks", 40);
            getLogger().info("Scheduling self-test for entity " + entName + " in " + delay + " ticks.");
            new org.bukkit.scheduler.BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        org.bukkit.World w = Bukkit.getWorlds().get(0);
                        org.bukkit.Location loc = w.getSpawnLocation().clone();
                        loc.add(2, 0, 0);
                        org.bukkit.entity.EntityType et = org.bukkit.entity.EntityType.valueOf(entName);
                        org.bukkit.entity.Entity spawned = w.spawnEntity(loc, et);
                        getLogger().info("Self-test spawned entity: " + entName + " at " + loc.getBlockX()+","+loc.getBlockY()+","+loc.getBlockZ());
                        // Delay one more tick so death listener clearly sees a death event post-spawn
                        new org.bukkit.scheduler.BukkitRunnable() {
                            @Override
                            public void run() {
                                if (spawned instanceof org.bukkit.entity.LivingEntity) {
                                    ((org.bukkit.entity.LivingEntity) spawned).damage(1000.0);
                                    getLogger().info("Self-test applied lethal damage to " + entName);
                                } else {
                                    spawned.remove();
                                }
                            }
                        }.runTaskLater(BoomBarnyardPlugin.this, 1);
                        new java.io.File(data, ".self-test-done").createNewFile();
                    } catch (Exception ex) {
                        getLogger().warning("Self-test failed: " + ex.getMessage());
                    }
                }
            }.runTaskLater(this, delay);
            if (trigger.exists()) trigger.delete();
        } catch (Exception ex) {
            getLogger().warning("Error in self-test logic: " + ex.getMessage());
        }
    }
}
