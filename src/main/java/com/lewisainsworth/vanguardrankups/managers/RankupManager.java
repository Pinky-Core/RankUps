package com.lewisainsworth.vanguardrankups.managers;

import com.lewisainsworth.vanguardrankups.VanguardRankUps;
import com.lewisainsworth.vanguardrankups.models.PlayerData;
import com.lewisainsworth.vanguardrankups.models.Rank;
import com.lewisainsworth.vanguardrankups.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RankupManager {
    
    private final VanguardRankUps plugin;
    private final Map<UUID, PlayerData> playerDataMap;
    
    public RankupManager(VanguardRankUps plugin) {
        this.plugin = plugin;
        this.playerDataMap = new HashMap<>();
    }
    
    public PlayerData getPlayerData(UUID playerUUID) {
        return playerDataMap.get(playerUUID);
    }
    
    private void applyLuckPermsGroup(PlayerData playerData, int previousRankLevel, Rank nextRankData) {
        Player player = Bukkit.getPlayer(playerData.getPlayerUUID());
        if (player == null) {
            return;
        }
        
        // Config gate
        boolean lpEnabled = getRankupsYmlConfig().getBoolean("settings.luckperms.enabled", true);
        if (!lpEnabled) {
            return;
        }
        
        String targetGroup = nextRankData.getLuckPermsGroup();
        if (targetGroup == null || targetGroup.isBlank()) {
            return;
        }
        
        // Try API first
        try {
            // Load LuckPerms classes dynamically
            Class<?> luckPermsClass = Class.forName("net.luckperms.api.LuckPerms");
            Class<?> luckPermsProviderClass = Class.forName("net.luckperms.api.LuckPermsProvider");
            Class<?> userClass = Class.forName("net.luckperms.api.model.user.User");
            
            Object lp = luckPermsProviderClass.getMethod("get").invoke(null);
            Object userManager = lp.getClass().getMethod("getUserManager").invoke(lp);
            Object user = userManager.getClass().getMethod("getUser", java.util.UUID.class).invoke(userManager, player.getUniqueId());
            
            if (user == null) {
                // Async load fallback - simplified for now
                plugin.getLogger().info("User not found in LuckPerms, using command fallback");
                throw new Exception("User not found");
            }
            
            applyLpChanges(user, previousRankLevel, targetGroup, nextRankData);
            userManager.getClass().getMethod("saveUser", userClass).invoke(userManager, user);
            
        } catch (Throwable t) {
            // Fallback to commands if API missing
            plugin.getLogger().info("Using command fallback for LuckPerms integration");
            
            // Remove previous group if configured and known
            boolean removePrevious = getRankupsYmlConfig().getBoolean("settings.luckperms.remove_previous_group", true);
            if (removePrevious && previousRankLevel > 0) {
                Rank prevRank = plugin.getConfigManager().getRank(previousRankLevel);
                if (prevRank != null && prevRank.getLuckPermsGroup() != null && !prevRank.getLuckPermsGroup().isBlank()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " parent remove " + prevRank.getLuckPermsGroup());
                }
            }
            
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " parent add " + targetGroup);
            
            // Apply prefix via commands if not respecting LP prefix
            boolean respectLpPrefix = getRankupsYmlConfig().getBoolean("settings.luckperms.respect_luckperms_prefix", true);
            if (!respectLpPrefix) {
                String displayName = nextRankData.getDisplayName();
                if (displayName != null && !displayName.isBlank()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " meta setprefix " + displayName);
                }
            }
        }
    }

    private void applyLpChanges(Object user, int previousRankLevel, String targetGroup, Rank nextRankData) {
        try {
            // Load LuckPerms classes dynamically
            Class<?> inheritanceNodeClass = Class.forName("net.luckperms.api.node.types.InheritanceNode");
            Class<?> metaNodeClass = Class.forName("net.luckperms.api.node.types.MetaNode");
            Class<?> nodeClass = Class.forName("net.luckperms.api.node.Node");
            
            boolean removePrevious = getRankupsYmlConfig().getBoolean("settings.luckperms.remove_previous_group", true);
            if (removePrevious && previousRankLevel > 0) {
                Rank prevRank = plugin.getConfigManager().getRank(previousRankLevel);
                if (prevRank != null && prevRank.getLuckPermsGroup() != null && !prevRank.getLuckPermsGroup().isBlank()) {
                    Object prevNode = inheritanceNodeClass.getMethod("builder", String.class).invoke(null, prevRank.getLuckPermsGroup());
                    prevNode = prevNode.getClass().getMethod("build").invoke(prevNode);
                    Object userData = user.getClass().getMethod("data").invoke(user);
                    userData.getClass().getMethod("remove", nodeClass).invoke(userData, prevNode);
                }
            }
            
            Object addNode = inheritanceNodeClass.getMethod("builder", String.class).invoke(null, targetGroup);
            addNode = addNode.getClass().getMethod("build").invoke(addNode);
            Object userData = user.getClass().getMethod("data").invoke(user);
            userData.getClass().getMethod("add", nodeClass).invoke(userData, addNode);
            
            // Check if we should respect LuckPerms prefix or set our own
            boolean respectLpPrefix = getRankupsYmlConfig().getBoolean("settings.luckperms.respect_luckperms_prefix", true);
            if (!respectLpPrefix) {
                // Check if the group already has a prefix configured
                boolean groupHasPrefix = checkGroupHasPrefix(targetGroup);
                if (!groupHasPrefix) {
                    // Only set prefix if group doesn't have one
                    String displayName = nextRankData.getDisplayName();
                    if (displayName != null && !displayName.isBlank()) {
                        // Remove existing prefix first
                        Object prefixNode = nodeClass.getMethod("builder", String.class).invoke(null, "prefix.100");
                        prefixNode = prefixNode.getClass().getMethod("build").invoke(prefixNode);
                        userData.getClass().getMethod("remove", nodeClass).invoke(userData, prefixNode);
                        
                        // Add new prefix using MetaNode
                        Object metaNode = metaNodeClass.getMethod("builder", String.class, String.class).invoke(null, "prefix", displayName);
                        metaNode = metaNode.getClass().getMethod("build").invoke(metaNode);
                        userData.getClass().getMethod("add", nodeClass).invoke(userData, metaNode);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to apply LuckPerms changes via API: " + e.getMessage());
        }
    }
    
    private boolean checkGroupHasPrefix(String groupName) {
        try {
            // Load LuckPerms classes dynamically
            Class<?> luckPermsProviderClass = Class.forName("net.luckperms.api.LuckPermsProvider");
            Class<?> groupClass = Class.forName("net.luckperms.api.model.group.Group");
            
            Object lp = luckPermsProviderClass.getMethod("get").invoke(null);
            Object groupManager = lp.getClass().getMethod("getGroupManager").invoke(lp);
            Object group = groupManager.getClass().getMethod("getGroup", String.class).invoke(groupManager, groupName);
            
            if (group == null) {
                return false;
            }
            
            // Check if group has prefix meta
            Object cachedData = group.getClass().getMethod("getCachedData").invoke(group);
            Object metaData = cachedData.getClass().getMethod("getMetaData").invoke(cachedData);
            Object prefix = metaData.getClass().getMethod("getPrefix").invoke(metaData);
            
            return prefix != null;
        } catch (Exception e) {
            plugin.getLogger().warning("Could not check prefix for group " + groupName + ": " + e.getMessage());
            return false;
        }
    }

    public PlayerData getOrCreatePlayerData(UUID playerUUID, String playerName) {
        return playerDataMap.computeIfAbsent(playerUUID, uuid -> {
            PlayerData data = plugin.getDatabaseManager().loadPlayerData(uuid, playerName);
            return data;
        });
    }
    
    public boolean canRankup(PlayerData playerData) {
        int currentRank = playerData.getCurrentRank();
        int nextRank = currentRank + 1;
        
        Rank nextRankData = plugin.getConfigManager().getRank(nextRank);
        if (nextRankData == null) {
            return false; // No next rank available
        }
        
        return checkRequirements(playerData, nextRankData);
    }
    
    public boolean checkRequirements(PlayerData playerData, Rank rank) {
        // Check mob kills
        Map<String, Integer> requiredMobKills = rank.getMobKills();
        for (Map.Entry<String, Integer> entry : requiredMobKills.entrySet()) {
            String mobType = entry.getKey();
            int required = entry.getValue();
            int current = playerData.getMobKills(mobType);
            
            if (current < required) {
                return false;
            }
        }
        
        // Check block breaks
        Map<String, Integer> requiredBlockBreaks = rank.getBlockBreaks();
        for (Map.Entry<String, Integer> entry : requiredBlockBreaks.entrySet()) {
            String blockType = entry.getKey();
            int required = entry.getValue();
            int current = playerData.getBlockBreaks(blockType);
            
            if (current < required) {
                return false;
            }
        }
        
        // Check playtime
        int requiredPlaytime = rank.getPlaytimeMinutes();
        if (requiredPlaytime > 0) {
            long currentPlaytime = playerData.getPlaytimeMinutes();
            if (currentPlaytime < requiredPlaytime) {
                return false;
            }
        }
        
        // Check fishing
        Map<String, Integer> requiredFishing = rank.getFishing();
        for (Map.Entry<String, Integer> entry : requiredFishing.entrySet()) {
            String fishType = entry.getKey();
            int required = entry.getValue();
            int current = playerData.getFishingCatches(fishType);
            
            if (current < required) {
                return false;
            }
        }
        
        // Check farming
        Map<String, Integer> requiredFarming = rank.getFarming();
        for (Map.Entry<String, Integer> entry : requiredFarming.entrySet()) {
            String cropType = entry.getKey();
            int required = entry.getValue();
            int current = playerData.getFarmingHarvests(cropType);
            
            if (current < required) {
                return false;
            }
        }
        
        return true;
    }
    
    public boolean performRankup(PlayerData playerData) {
        int currentRank = playerData.getCurrentRank();
        int nextRank = currentRank + 1;
        
        Rank nextRankData = plugin.getConfigManager().getRank(nextRank);
        if (nextRankData == null) {
            return false;
        }
        
        if (!checkRequirements(playerData, nextRankData)) {
            return false;
        }
        
        // Perform rankup
        playerData.setCurrentRank(nextRank);
        
        // Reset rankup notification flag since player has ranked up
        playerData.setRankupNotificationSent(false);
        
        // Update permissions/rank in LuckPerms if configured
        applyLuckPermsGroup(playerData, currentRank, nextRankData);

        // Give rewards
        giveRewards(playerData, nextRankData);
        
        // Save data
        plugin.getDatabaseManager().savePlayerData(playerData);
        
        // Play sound
        Player player = Bukkit.getPlayer(playerData.getPlayerUUID());
        if (player != null) {
            String soundName = plugin.getConfigManager().getSound("rankup_success");
            if (!soundName.isEmpty()) {
                try {
                    Sound sound = Sound.valueOf(soundName);
                    player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid sound: " + soundName);
                }
            }
        }
        
        return true;
    }
    
    public void forceRankup(UUID playerUUID, int rank) {
        PlayerData playerData = getPlayerData(playerUUID);
        if (playerData == null) {
            return;
        }
        
        int previousRank = playerData.getCurrentRank();
        playerData.setCurrentRank(rank);
        
        // Reset rankup notification flag since player has ranked up
        playerData.setRankupNotificationSent(false);
        
        // Aplicar grupo de LuckPerms si está configurado
        Rank rankData = plugin.getConfigManager().getRank(rank);
        if (rankData != null && rankData.getLuckPermsGroup() != null) {
            applyLuckPermsGroup(playerData, previousRank, rankData);
        }
        
        plugin.getDatabaseManager().savePlayerData(playerData);
    }
    
    public void resetPlayer(UUID playerUUID) {
        PlayerData playerData = getPlayerData(playerUUID);
        if (playerData != null) {
            playerData.reset();
            plugin.getDatabaseManager().savePlayerData(playerData);
        }
        plugin.getDatabaseManager().resetPlayerData(playerUUID);
    }
    
    private void giveRewards(PlayerData playerData, Rank rank) {
        Player player = Bukkit.getPlayer(playerData.getPlayerUUID());
        if (player == null) {
            return;
        }
        
        // Execute commands
        List<String> commands = rank.getCommands();
        for (String command : commands) {
            String processedCommand = command
                .replace("%player%", player.getName())
                .replace("%player_name%", player.getName())
                .replace("%rankup_current_rank%", getLuckPermsGroupOfRank(playerData.getCurrentRank() - 1))
                .replace("%rankup_next_rank%", getLuckPermsGroupOfRank(playerData.getCurrentRank()));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
        }
        
        // Give items
        List<String> items = rank.getItems();
        for (String itemString : items) {
            ItemStack item = parseItemString(itemString);
            if (item != null) {
                player.getInventory().addItem(item);
            }
        }
        
        // Give experience
        int experience = rank.getExperience();
        if (experience > 0) {
            player.giveExp(experience);
        }
        
        // Give money (requires Vault)
        int money = rank.getMoney();
        if (money > 0) {
            // This would require Vault integration
            // For now, we'll just log it
            plugin.getLogger().info("Money reward of " + money + " for " + player.getName() + " (Vault integration needed)");
        }
        
        // Send message
        String message = plugin.getConfigManager().getMessage("rankup_success")
            .replace("%rank%", rank.getName());
        plugin.getMessageUtils().sendMessage(player, message);
    }

    private String getLuckPermsGroupOfRank(int rankLevel) {
        if (rankLevel <= 0) return "";
        Rank r = plugin.getConfigManager().getRank(rankLevel);
        if (r == null) return "";
        String g = r.getLuckPermsGroup();
        return g == null ? "" : g;
    }
    
    private org.bukkit.configuration.file.YamlConfiguration getRankupsYmlConfig() {
        java.io.File rankupsFile = new java.io.File(plugin.getDataFolder(), "rankups.yml");
        if (rankupsFile.exists()) {
            return org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(rankupsFile);
        }
        return new org.bukkit.configuration.file.YamlConfiguration();
    }
    
    private ItemStack parseItemString(String itemString) {
        try {
            String[] parts = itemString.split(":");
            Material material = Material.valueOf(parts[0]);
            int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
            
            ItemStack item = new ItemStack(material, amount);
            
            // Parse enchantments if present
            if (parts.length > 2) {
                String[] enchantments = parts[2].split(",");
                for (String enchant : enchantments) {
                    String[] enchantParts = enchant.split(":");
                    if (enchantParts.length == 2) {
                        // This would require proper enchantment parsing
                        // For now, we'll just create the basic item
                    }
                }
            }
            
            return item;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to parse item: " + itemString);
            return null;
        }
    }
    
    public Map<String, Object> getProgress(PlayerData playerData) {
        Map<String, Object> progress = new HashMap<>();
        
        int currentRank = playerData.getCurrentRank();
        int nextRank = currentRank + 1;
        Rank nextRankData = plugin.getConfigManager().getRank(nextRank);
        
        if (nextRankData == null) {
            progress.put("max_rank", true);
            return progress;
        }
        
        // Mob kills progress
        Map<String, Object> mobKillsProgress = new HashMap<>();
        Map<String, Integer> requiredMobKills = nextRankData.getMobKills();
        for (Map.Entry<String, Integer> entry : requiredMobKills.entrySet()) {
            String mobType = entry.getKey();
            int required = entry.getValue();
            int current = playerData.getMobKills(mobType);
            
            Map<String, Object> mobProgress = new HashMap<>();
            mobProgress.put("current", current);
            mobProgress.put("required", required);
            mobProgress.put("percentage", Math.min(100.0, (double) current / required * 100));
            
            mobKillsProgress.put(mobType, mobProgress);
        }
        progress.put("mob_kills", mobKillsProgress);
        
        // Block breaks progress
        Map<String, Object> blockBreaksProgress = new HashMap<>();
        Map<String, Integer> requiredBlockBreaks = nextRankData.getBlockBreaks();
        for (Map.Entry<String, Integer> entry : requiredBlockBreaks.entrySet()) {
            String blockType = entry.getKey();
            int required = entry.getValue();
            int current = playerData.getBlockBreaks(blockType);
            
            Map<String, Object> blockProgress = new HashMap<>();
            blockProgress.put("current", current);
            blockProgress.put("required", required);
            blockProgress.put("percentage", Math.min(100.0, (double) current / required * 100));
            
            blockBreaksProgress.put(blockType, blockProgress);
        }
        progress.put("block_breaks", blockBreaksProgress);
        
        // Playtime progress
        int requiredPlaytime = nextRankData.getPlaytimeMinutes();
        if (requiredPlaytime > 0) {
            long currentPlaytime = playerData.getPlaytimeMinutes();
            
            Map<String, Object> playtimeProgress = new HashMap<>();
            playtimeProgress.put("current", currentPlaytime);
            playtimeProgress.put("required", requiredPlaytime);
            playtimeProgress.put("percentage", Math.min(100.0, (double) currentPlaytime / requiredPlaytime * 100));
            
            progress.put("playtime", playtimeProgress);
        }
        
        return progress;
    }
    
    public void saveAllData() {
        for (PlayerData playerData : playerDataMap.values()) {
            plugin.getDatabaseManager().savePlayerData(playerData);
        }
    }
    
    public void removePlayerData(UUID playerUUID) {
        PlayerData playerData = playerDataMap.remove(playerUUID);
        if (playerData != null) {
            plugin.getDatabaseManager().savePlayerData(playerData);
        }
    }
} 