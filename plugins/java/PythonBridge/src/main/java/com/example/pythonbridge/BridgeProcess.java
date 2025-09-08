package com.example.pythonbridge;

import org.bukkit.Bukkit;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BridgeProcess {
    private final PythonBridge plugin;
    private final String pythonCommand;
    private final String scriptPath;
    private Process process;
    private BufferedWriter writer;

    public BridgeProcess(PythonBridge plugin, String pythonCommand, String scriptPath) {
        this.plugin = plugin;
        this.pythonCommand = (pythonCommand == null || pythonCommand.isBlank()) ? "python" : pythonCommand;
        this.scriptPath = scriptPath;
    }

    public void start() throws IOException {
        if (process != null && process.isAlive()) return;
        List<String> cmd = new ArrayList<>();
        cmd.add(pythonCommand);
        cmd.add(scriptPath);
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        pb.directory(new File("."));
        process = pb.start();
        writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
        listen();
        plugin.getLogger().info("Started Python bridge process.");
    }

    private void listen() {
        CompletableFuture.runAsync(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    final String msg = line;
                    Bukkit.getScheduler().runTask(plugin, () -> plugin.getLogger().info("[PY] " + msg));
                }
            } catch (IOException e) {
                plugin.getLogger().warning("Python bridge reader stopped: " + e.getMessage());
            } finally {
                int code = -1;
                if (process != null) code = process.exitValue();
                plugin.getLogger().warning("Python process ended with code " + code);
            }
        });
    }

    public void sendJson(String json) {
        if (writer == null) return;
        try {
            writer.write(json);
            writer.write("\n");
            writer.flush();
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to send to Python: " + e.getMessage());
        }
    }

    public void stop() {
        if (process != null) {
            process.destroy();
            process = null;
        }
    }
}
