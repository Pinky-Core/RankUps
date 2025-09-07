package com.lewisainsworth.vanguardrankups.listeners;

import com.lewisainsworth.vanguardrankups.VanguardRankUps;
import com.lewisainsworth.vanguardrankups.models.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Listener for quest completion events from the Quests plugin
 * This allows tracking general quest completion for rank-up requirements
 */
public class QuestListener implements Listener {
    
    private final VanguardRankUps plugin;
    
    public QuestListener(VanguardRankUps plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Handle quest completion events from the Quests plugin
     */
    @EventHandler
    public void onQuestComplete(com.leonardobishop.quests.bukkit.api.event.PlayerFinishQuestEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = plugin.getRankupManager().getOrCreatePlayerData(player.getUniqueId(), player.getName());
        
        // Increment completed quests count
        playerData.addCompletedQuest();
        
        // Check if player can now rankup
        if (plugin.getRankupManager().canRankup(playerData)) {
            plugin.getMessageUtils().sendMessage(player, "¡Puedes hacer rankup! Usa /rankup confirm");
        }
        
        // Save player data
        plugin.getDatabaseManager().savePlayerData(playerData);
    }
    
    /**
     * Helper method to manually add completed quests
     * This can be used by other plugins or commands
     */
    public void addCompletedQuest(Player player) {
        PlayerData playerData = plugin.getRankupManager().getOrCreatePlayerData(player.getUniqueId(), player.getName());
        playerData.addCompletedQuest();
        
        // Check if player can now rankup
        if (plugin.getRankupManager().canRankup(playerData)) {
            plugin.getMessageUtils().sendMessage(player, "¡Puedes hacer rankup! Usa /rankup confirm");
        }
        
        // Save player data
        plugin.getDatabaseManager().savePlayerData(playerData);
    }
} 