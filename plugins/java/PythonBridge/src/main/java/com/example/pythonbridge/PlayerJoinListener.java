package com.example.pythonbridge;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.json.JSONObject;

public class PlayerJoinListener implements Listener {
    private final PythonBridge plugin;
    private final BridgeProcess bridge;

    public PlayerJoinListener(PythonBridge plugin, BridgeProcess bridge) {
        this.plugin = plugin;
        this.bridge = bridge;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        JSONObject obj = new JSONObject();
        obj.put("type", "player_join");
        obj.put("player", event.getPlayer().getName());
        bridge.sendJson(obj.toString());
    }
}
