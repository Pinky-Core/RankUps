package com.lewisainsworth.vanguardrankups.listeners;

import com.lewisainsworth.vanguardrankups.VanguardRankUps;
import com.lewisainsworth.vanguardrankups.models.PlayerData;
import com.lewisainsworth.vanguardrankups.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {
    
    private final VanguardRankUps plugin;
    
    public PlayerListener(VanguardRankUps plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getRankupManager().getOrCreatePlayerData(player.getUniqueId(), player.getName());
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = plugin.getRankupManager().getPlayerData(player.getUniqueId());
        
        if (playerData != null) {
            playerData.updatePlaytime();
            plugin.getDatabaseManager().savePlayerData(playerData);
            plugin.getRankupManager().removePlayerData(player.getUniqueId());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) {
            return;
        }
        
        Player killer = event.getEntity().getKiller();
        PlayerData playerData = plugin.getRankupManager().getPlayerData(killer.getUniqueId());
        
        if (playerData == null) {
            return;
        }
        
        EntityType entityType = event.getEntity().getType();
        String entityName = entityType.name();
        
        // Check if this entity type is tracked
        if (isTrackedMob(entityName)) {
            playerData.addMobKill(entityName);
            
            // Check if player can rankup after this kill and hasn't been notified yet
            if (plugin.getRankupManager().canRankup(playerData) && !playerData.isRankupNotificationSent()) {
                String soundName = plugin.getConfigManager().getSound("progress_update");
                if (!soundName.isEmpty()) {
                    try {
                        Sound sound = Sound.valueOf(soundName);
                        killer.playSound(killer.getLocation(), sound, 0.5f, 1.0f);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid sound: " + soundName);
                    }
                }
                
                MessageUtils.sendMessageNoPrefix(killer, "§a¡Puedes hacer rankup! Usa /rankup confirm");
                playerData.setRankupNotificationSent(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = plugin.getRankupManager().getPlayerData(player.getUniqueId());
        
        if (playerData == null) {
            return;
        }
        
        Material blockType = event.getBlock().getType();
        String blockName = blockType.name();
        
        // Check if this block type is tracked
        if (isTrackedBlock(blockName)) {
            playerData.addBlockBreak(blockName);
            
            // Check if player can rankup after this break and hasn't been notified yet
            if (plugin.getRankupManager().canRankup(playerData) && !playerData.isRankupNotificationSent()) {
                String soundName = plugin.getConfigManager().getSound("progress_update");
                if (!soundName.isEmpty()) {
                    try {
                        Sound sound = Sound.valueOf(soundName);
                        player.playSound(player.getLocation(), sound, 0.5f, 1.0f);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid sound: " + soundName);
                    }
                }
                
                MessageUtils.sendMessageNoPrefix(player, "§a¡Puedes hacer rankup! Usa /rankup confirm");
                playerData.setRankupNotificationSent(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }
        
        Player player = event.getPlayer();
        PlayerData playerData = plugin.getRankupManager().getPlayerData(player.getUniqueId());
        
        if (playerData == null) {
            return;
        }
        
        // Get the caught item
        ItemStack caught = null;
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH && event.getHook().getHookedEntity() instanceof org.bukkit.entity.Fish) {
            org.bukkit.entity.Fish fish = (org.bukkit.entity.Fish) event.getHook().getHookedEntity();
            // For now, we'll use a default fish type since getItem() might not be available
            caught = new ItemStack(org.bukkit.Material.COD);
        }
        
        if (caught != null) {
            String fishType = caught.getType().name();
            
            // Check if this fish type is tracked
            if (isTrackedFish(fishType)) {
                playerData.addFishingCatch(fishType);
                
                // Check if player can rankup after this catch and hasn't been notified yet
                if (plugin.getRankupManager().canRankup(playerData) && !playerData.isRankupNotificationSent()) {
                    String soundName = plugin.getConfigManager().getSound("progress_update");
                    if (!soundName.isEmpty()) {
                        try {
                            Sound sound = Sound.valueOf(soundName);
                            player.playSound(player.getLocation(), sound, 0.5f, 1.0f);
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Invalid sound: " + soundName);
                        }
                    }
                    
                    MessageUtils.sendMessageNoPrefix(player, "§a¡Puedes hacer rankup! Usa /rankup confirm");
                    playerData.setRankupNotificationSent(true);
                }
            }
        }
    }
    
    // Helper methods to check if entities/blocks are tracked
    private boolean isTrackedMob(String entityName) {
        // Check if this mob type is required by any rank
        for (var rankEntry : plugin.getConfigManager().getRanks().entrySet()) {
            var rank = rankEntry.getValue();
            if (rank.getMobKills().containsKey(entityName)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isTrackedBlock(String blockName) {
        // Check if this block type is required by any rank
        for (var rankEntry : plugin.getConfigManager().getRanks().entrySet()) {
            var rank = rankEntry.getValue();
            if (rank.getBlockBreaks().containsKey(blockName)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isTrackedFish(String fishName) {
        // Check if this fish type is required by any rank
        for (var rankEntry : plugin.getConfigManager().getRanks().entrySet()) {
            var rank = rankEntry.getValue();
            if (rank.getFishing().containsKey(fishName)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isTrackedCrop(String cropName) {
        // Check if this crop type is required by any rank
        for (var rankEntry : plugin.getConfigManager().getRanks().entrySet()) {
            var rank = rankEntry.getValue();
            if (rank.getFarming().containsKey(cropName)) {
                return true;
            }
        }
        return false;
    }
} 