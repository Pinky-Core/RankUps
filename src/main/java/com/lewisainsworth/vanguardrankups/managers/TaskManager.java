package com.lewisainsworth.vanguardrankups.managers;

import com.lewisainsworth.vanguardrankups.VanguardRankUps;
import com.lewisainsworth.vanguardrankups.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public class TaskManager {
    
    private final VanguardRankUps plugin;
    private BukkitTask autoSaveTask;
    private BukkitTask playtimeTask;
    
    public TaskManager(VanguardRankUps plugin) {
        this.plugin = plugin;
    }
    
    public void startScheduler() {
        // Auto-save task
        int autoSaveInterval = plugin.getConfigManager().getAutoSaveInterval();
        if (autoSaveInterval > 0) {
            autoSaveTask = new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.getRankupManager().saveAllData();
                    if (plugin.getConfigManager().isDebug()) {
                        plugin.getLogger().info("Auto-saved all player data");
                    }
                }
            }.runTaskTimerAsynchronously(plugin, autoSaveInterval * 20L, autoSaveInterval * 20L);
        }
        
        // Playtime tracking task
        playtimeTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID playerUUID = player.getUniqueId();
                    PlayerData playerData = plugin.getRankupManager().getPlayerData(playerUUID);
                    
                    if (playerData != null) {
                        playerData.updatePlaytime();
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Update every second
    }
    
    public void stopScheduler() {
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
            autoSaveTask = null;
        }
        
        if (playtimeTask != null) {
            playtimeTask.cancel();
            playtimeTask = null;
        }
    }
    
    public void saveAllData() {
        plugin.getRankupManager().saveAllData();
    }
} 