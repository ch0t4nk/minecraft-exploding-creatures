package com.example.pythonbridge;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Bukkit;

import java.io.File;

public class PythonBridge extends JavaPlugin {
    private BridgeProcess bridge;

    @Override
    public void onEnable() {
        saveDefaultConfigIfMissing();
        FileConfiguration cfg = getConfig();
        if (!cfg.getBoolean("enabled", true)) {
            getLogger().info("PythonBridge disabled in config.yml");
            return;
        }
        String pythonPath = cfg.getString("python-path", "");
        String script = cfg.getString("script", "plugins/python/bridge/bridge_runner.py");
        bridge = new BridgeProcess(this, pythonPath, script);
        try {
            bridge.start();
        } catch (Exception e) {
            getLogger().severe("Failed to start Python process: " + e.getMessage());
        }
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this, bridge), this);
        getLogger().info("PythonBridge active.");
    }

    @Override
    public void onDisable() {
        if (bridge != null) {
            bridge.stop();
        }
        getLogger().info("PythonBridge plugin is stopping. Goodbye!");
    }

    private void saveDefaultConfigIfMissing() {
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            getDataFolder().mkdirs();
            saveResource("config.yml", false);
        }
    }
}
