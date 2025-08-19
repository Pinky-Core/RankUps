package com.lewisainsworth.vanguardrankups.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Rank {
    private int level;
    private String name;
    private String displayName;
    private String luckPermsGroup;
    private Map<String, Map<String, Integer>> requirements;
    private Map<String, Object> rewards;
    private int cost;
    
    public Rank(int level, String name, String displayName) {
        this.level = level;
        this.name = name;
        this.displayName = displayName;
        this.luckPermsGroup = null;
        this.requirements = new HashMap<>();
        this.rewards = new HashMap<>();
        this.cost = 0;
    }
    
    public int getLevel() {
        return level;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getLuckPermsGroup() {
        return luckPermsGroup;
    }

    public void setLuckPermsGroup(String luckPermsGroup) {
        this.luckPermsGroup = luckPermsGroup;
    }
    
    public Map<String, Map<String, Integer>> getRequirements() {
        return requirements;
    }
    
    public void setRequirements(Map<String, Map<String, Integer>> requirements) {
        this.requirements = requirements;
    }
    
    public Map<String, Object> getRewards() {
        return rewards;
    }
    
    public void setRewards(Map<String, Object> rewards) {
        this.rewards = rewards;
    }
    
    public int getCost() {
        return cost;
    }
    
    public void setCost(int cost) {
        this.cost = cost;
    }
    
    @SuppressWarnings("unchecked")
    public List<String> getCommands() {
        return (List<String>) rewards.getOrDefault("commands", List.of());
    }
    
    @SuppressWarnings("unchecked")
    public List<String> getItems() {
        return (List<String>) rewards.getOrDefault("items", List.of());
    }
    
    public int getExperience() {
        return (int) rewards.getOrDefault("experience", 0);
    }
    
    public int getMoney() {
        return (int) rewards.getOrDefault("money", 0);
    }
    
    public Map<String, Integer> getMobKills() {
        return requirements.getOrDefault("mob_kills", new HashMap<>());
    }
    
    public Map<String, Integer> getBlockBreaks() {
        return requirements.getOrDefault("block_breaks", new HashMap<>());
    }
    
    public int getPlaytimeMinutes() {
        return requirements.getOrDefault("playtime_minutes", new HashMap<>()).getOrDefault("total", 0);
    }
    
    public Map<String, Integer> getFishing() {
        return requirements.getOrDefault("fishing", new HashMap<>());
    }
    
    public Map<String, Integer> getFarming() {
        return requirements.getOrDefault("farming", new HashMap<>());
    }
} 