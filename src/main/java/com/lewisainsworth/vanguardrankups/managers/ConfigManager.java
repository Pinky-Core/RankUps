package com.lewisainsworth.vanguardrankups.managers;

import com.lewisainsworth.vanguardrankups.VanguardRankUps;
import com.lewisainsworth.vanguardrankups.models.Rank;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    
    private final VanguardRankUps plugin;
    private final Map<Integer, Rank> ranks;
    
    public ConfigManager(VanguardRankUps plugin) {
        this.plugin = plugin;
        this.ranks = new HashMap<>();
    }
    
    public void loadConfig() {
        // Cargar configuración desde rankups.yml como archivo principal
        loadRanksFromRankupsYml();
    }

    private void loadRanksFromRankupsYml() {
        ranks.clear();
        String fileName = "rankups.yml";
        java.io.File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        java.io.File rankupsFile = new java.io.File(dataFolder, fileName);
        if (!rankupsFile.exists()) {
            try {
                plugin.saveResource(fileName, false);
            } catch (IllegalArgumentException ignored) {
                // No bundled resource, continue with empty
            }
        }
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(rankupsFile);
        
        // Cargar configuración general desde rankups.yml
        loadGeneralConfig(yml);
        java.util.List<java.util.Map<String, String>> transitions = new java.util.ArrayList<>();
        java.util.Map<String, org.bukkit.configuration.ConfigurationSection> entryByNext = new java.util.HashMap<>();
        java.util.Map<String, String> prevOf = new java.util.HashMap<>();
        java.util.Set<String> nextSet = new java.util.HashSet<>();
        for (String key : yml.getKeys(false)) {
            ConfigurationSection sec = yml.getConfigurationSection(key);
            if (sec == null) continue;
            // Saltar secciones de configuración general
            if (key.equals("database") || key.equals("settings") || key.equals("messages") || 
                key.equals("sounds") || key.equals("gui") || key.equals("task_types")) {
                continue;
            }
            String current = sec.getString("rank");
            String next = sec.getString("next");
            if (current == null || next == null) continue;
            java.util.Map<String, String> pair = new java.util.HashMap<>();
            pair.put("current", current);
            pair.put("next", next);
            transitions.add(pair);
            entryByNext.put(next, sec);
            nextSet.add(next);
            prevOf.put(next, current);
        }
        // Determine chain starts (currents that are never a 'next')
        java.util.Set<String> starts = new java.util.HashSet<>();
        for (java.util.Map<String, String> t : transitions) {
            String curr = t.get("current");
            if (!nextSet.contains(curr)) {
                starts.add(curr);
            }
        }
        int level = 0;
        java.util.Set<String> visitedPairs = new java.util.HashSet<>();
        for (String start : starts) {
            String curr = start;
            while (true) {
                // find transition where current==curr
                String next = null;
                ConfigurationSection sec = null;
                for (java.util.Map<String, String> t : transitions) {
                    if (curr.equalsIgnoreCase(t.get("current"))) {
                        String id = t.get("current")+"->"+t.get("next");
                        if (visitedPairs.contains(id)) continue;
                        next = t.get("next");
                        sec = entryByNext.get(next);
                        visitedPairs.add(id);
                        break;
                    }
                }
                if (next == null || sec == null) break;
                level++;
                Rank rank = new Rank(level,
                    sec.getString("display-name", next),
                    sec.getString("display-name", next));
                // LP group is the target group (next)
                rank.setLuckPermsGroup(next);
                
                // También guardar el grupo actual para referencia
                plugin.getLogger().info("Loaded rank " + level + ": " + curr + " -> " + next + " (LP group: " + next + ")");
                // Parse requirements list
                java.util.List<String> reqs = sec.getStringList("requirements");
                java.util.Map<String, java.util.Map<String, Integer>> requirements = new java.util.HashMap<>();
                java.util.Map<String, Integer> mobKills = new java.util.HashMap<>();
                java.util.Map<String, Integer> blockBreaks = new java.util.HashMap<>();
                int playtime = 0;
                for (String r : reqs) {
                    if (r == null) continue;
                    String[] parts = r.trim().split("\\s+");
                    if (parts.length == 0) continue;
                    String type = parts[0].toLowerCase();
                    if (type.startsWith("playtime")) {
                        if (parts.length >= 2) {
                            try { playtime = Integer.parseInt(parts[1]); } catch (NumberFormatException ignored) {}
                        }
                    } else if (type.startsWith("block-break") && parts.length >= 3) {
                        String mat = parts[1].toUpperCase();
                        int amt = 0; try { amt = Integer.parseInt(parts[2]); } catch (NumberFormatException ignored) {}
                        if (amt > 0) blockBreaks.put(mat, amt);
                    } else if (type.startsWith("mob-kills") && parts.length >= 3) {
                        String mob = parts[1].toUpperCase();
                        int amt = 0; try { amt = Integer.parseInt(parts[2]); } catch (NumberFormatException ignored) {}
                        if (amt > 0) mobKills.put(mob, amt);
                    }
                }
                if (!mobKills.isEmpty()) requirements.put("mob_kills", mobKills);
                if (!blockBreaks.isEmpty()) requirements.put("block_breaks", blockBreaks);
                if (playtime > 0) {
                    java.util.Map<String, Integer> play = new java.util.HashMap<>();
                    play.put("total", playtime);
                    requirements.put("playtime_minutes", play);
                }
                rank.setRequirements(requirements);
                // Rewards
                java.util.Map<String, Object> rewards = new java.util.HashMap<>();
                if (sec.contains("commands")) rewards.put("commands", sec.getStringList("commands"));
                rewards.put("experience", 0);
                rewards.put("money", 0);
                rank.setRewards(rewards);
                rank.setCost(0);
                ranks.put(level, rank);
                curr = next;
            }
        }
        plugin.getLogger().info("Loaded " + ranks.size() + " ranks from rankups.yml");
    }
    
    private void loadGeneralConfig(YamlConfiguration yml) {
        // Esta configuración se mantiene en memoria para acceso rápido
        // pero se carga desde rankups.yml
    }
    
    private void loadRanks() {
        ranks.clear();
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection ranksSection = config.getConfigurationSection("ranks");
        
        if (ranksSection == null) {
            plugin.getLogger().warning("No ranks found in configuration!");
            return;
        }
        
        for (String rankKey : ranksSection.getKeys(false)) {
            try {
                int level = Integer.parseInt(rankKey);
                ConfigurationSection rankSection = ranksSection.getConfigurationSection(rankKey);
                
                if (rankSection != null) {
                    Rank rank = new Rank(
                        level,
                        rankSection.getString("name", "Rank " + level),
                        rankSection.getString("display_name", "&7[Rank " + level + "]")
                    );
                    
                    // LuckPerms group (optional)
                    String lpGroup = rankSection.getString("luckperms_group", null);
                    if (lpGroup != null && !lpGroup.isBlank()) {
                        rank.setLuckPermsGroup(lpGroup);
                    }

                    // Load requirements
                    ConfigurationSection requirementsSection = rankSection.getConfigurationSection("requirements");
                    if (requirementsSection != null) {
                        Map<String, Map<String, Integer>> requirements = new HashMap<>();
                        
                        // Mob kills
                        ConfigurationSection mobKillsSection = requirementsSection.getConfigurationSection("mob_kills");
                        if (mobKillsSection != null) {
                            Map<String, Integer> mobKills = new HashMap<>();
                            for (String mob : mobKillsSection.getKeys(false)) {
                                mobKills.put(mob, mobKillsSection.getInt(mob));
                            }
                            requirements.put("mob_kills", mobKills);
                        }
                        
                        // Block breaks
                        ConfigurationSection blockBreaksSection = requirementsSection.getConfigurationSection("block_breaks");
                        if (blockBreaksSection != null) {
                            Map<String, Integer> blockBreaks = new HashMap<>();
                            for (String block : blockBreaksSection.getKeys(false)) {
                                blockBreaks.put(block, blockBreaksSection.getInt(block));
                            }
                            requirements.put("block_breaks", blockBreaks);
                        }
                        
                        // Playtime
                        int playtime = requirementsSection.getInt("playtime_minutes", 0);
                        if (playtime > 0) {
                            Map<String, Integer> playtimeMap = new HashMap<>();
                            playtimeMap.put("total", playtime);
                            requirements.put("playtime_minutes", playtimeMap);
                        }
                        
                        rank.setRequirements(requirements);
                    }
                    
                    // Load rewards
                    ConfigurationSection rewardsSection = rankSection.getConfigurationSection("rewards");
                    if (rewardsSection != null) {
                        Map<String, Object> rewards = new HashMap<>();
                        
                        // Commands
                        if (rewardsSection.contains("commands")) {
                            rewards.put("commands", rewardsSection.getStringList("commands"));
                        }
                        
                        // Items
                        if (rewardsSection.contains("items")) {
                            rewards.put("items", rewardsSection.getStringList("items"));
                        }
                        
                        // Experience
                        rewards.put("experience", rewardsSection.getInt("experience", 0));
                        
                        // Money
                        rewards.put("money", rewardsSection.getInt("money", 0));
                        
                        rank.setRewards(rewards);
                    }
                    
                    // Cost
                    rank.setCost(rankSection.getInt("cost", 0));
                    
                    ranks.put(level, rank);
                }
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Invalid rank level: " + rankKey);
            }
        }
        
        plugin.getLogger().info("Loaded " + ranks.size() + " ranks from configuration.");
    }
    
    public Map<Integer, Rank> getRanks() {
        return ranks;
    }
    
    public Rank getRank(int level) {
        return ranks.get(level);
    }
    
    public int getMaxRank() {
        return getRankupsYmlConfig().getInt("settings.max-rank", 10);
    }
    
    public String getPrefix() {
        return getRankupsYmlConfig().getString("settings.prefix", "&8[&bVanguardRankUps&8] &r");
    }
    
    public boolean isDebug() {
        return getRankupsYmlConfig().getBoolean("settings.debug", false);
    }
    
    public int getAutoSaveInterval() {
        return getRankupsYmlConfig().getInt("settings.auto-save-interval", 300);
    }
    
    public String getMessage(String key) {
        return getRankupsYmlConfig().getString("messages." + key, "Message not found: " + key);
    }
    
    public String getSound(String key) {
        return getRankupsYmlConfig().getString("sounds." + key, "");
    }
    
    public org.bukkit.configuration.ConfigurationSection getGUIConfig() {
        return getRankupsYmlConfig().getConfigurationSection("gui");
    }
    
    public boolean isTaskTypeEnabled(String taskType) {
        return getRankupsYmlConfig().getBoolean("task_types." + taskType + ".enabled", true);
    }
    
    public String getTaskTypeDisplayName(String taskType) {
        return getRankupsYmlConfig().getString("task_types." + taskType + ".display_name", taskType);
    }
    
    public String getTaskTypeDescription(String taskType) {
        return getRankupsYmlConfig().getString("task_types." + taskType + ".description", "");
    }
    
    public int getRankLevelByGroup(String groupName) {
        for (Map.Entry<Integer, Rank> entry : ranks.entrySet()) {
            Rank rank = entry.getValue();
            if (groupName.equalsIgnoreCase(rank.getLuckPermsGroup())) {
                return entry.getKey();
            }
        }
        return -1; // No encontrado
    }
    
    private YamlConfiguration getRankupsYmlConfig() {
        java.io.File rankupsFile = new java.io.File(plugin.getDataFolder(), "rankups.yml");
        if (rankupsFile.exists()) {
            return YamlConfiguration.loadConfiguration(rankupsFile);
        }
        return new YamlConfiguration();
    }
} 