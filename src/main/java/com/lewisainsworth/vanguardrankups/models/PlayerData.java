package com.lewisainsworth.vanguardrankups.models;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData {
    private UUID playerUUID;
    private String playerName;
    private int currentRank;
    private long joinTime;
    private long totalPlaytime;
    private Map<String, Integer> mobKills;
    private Map<String, Integer> blockBreaks;
    private Map<String, Integer> fishingCatches;
    private Map<String, Integer> farmingHarvests;
    private boolean rankupNotificationSent;
    
    public PlayerData(UUID playerUUID, String playerName) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.currentRank = 0;
        this.joinTime = System.currentTimeMillis();
        this.totalPlaytime = 0;
        this.mobKills = new HashMap<>();
        this.blockBreaks = new HashMap<>();
        this.fishingCatches = new HashMap<>();
        this.farmingHarvests = new HashMap<>();
        this.rankupNotificationSent = false;
    }
    
    public UUID getPlayerUUID() {
        return playerUUID;
    }
    
    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    
    public int getCurrentRank() {
        return currentRank;
    }
    
    public void setCurrentRank(int currentRank) {
        this.currentRank = currentRank;
    }
    
    public long getJoinTime() {
        return joinTime;
    }
    
    public void setJoinTime(long joinTime) {
        this.joinTime = joinTime;
    }
    
    public long getTotalPlaytime() {
        return totalPlaytime;
    }
    
    public void setTotalPlaytime(long totalPlaytime) {
        this.totalPlaytime = totalPlaytime;
    }
    
    public Map<String, Integer> getMobKills() {
        return mobKills;
    }
    
    public void setMobKills(Map<String, Integer> mobKills) {
        this.mobKills = mobKills;
    }
    
    public Map<String, Integer> getBlockBreaks() {
        return blockBreaks;
    }
    
    public void setBlockBreaks(Map<String, Integer> blockBreaks) {
        this.blockBreaks = blockBreaks;
    }
    
    public Map<String, Integer> getFishingCatches() {
        return fishingCatches;
    }
    
    public void setFishingCatches(Map<String, Integer> fishingCatches) {
        this.fishingCatches = fishingCatches;
    }
    
    public Map<String, Integer> getFarmingHarvests() {
        return farmingHarvests;
    }
    
    public void setFarmingHarvests(Map<String, Integer> farmingHarvests) {
        this.farmingHarvests = farmingHarvests;
    }
    
    public void addMobKill(String mobType) {
        mobKills.put(mobType, mobKills.getOrDefault(mobType, 0) + 1);
    }
    
    public void addBlockBreak(String blockType) {
        blockBreaks.put(blockType, blockBreaks.getOrDefault(blockType, 0) + 1);
    }
    
    public void addFishingCatch(String fishType) {
        fishingCatches.put(fishType, fishingCatches.getOrDefault(fishType, 0) + 1);
    }
    
    public void addFarmingHarvest(String cropType) {
        farmingHarvests.put(cropType, farmingHarvests.getOrDefault(cropType, 0) + 1);
    }
    
    public int getMobKills(String mobType) {
        return mobKills.getOrDefault(mobType, 0);
    }
    
    public int getBlockBreaks(String blockType) {
        return blockBreaks.getOrDefault(blockType, 0);
    }
    
    public int getFishingCatches(String fishType) {
        return fishingCatches.getOrDefault(fishType, 0);
    }
    
    public int getFarmingHarvests(String cropType) {
        return farmingHarvests.getOrDefault(cropType, 0);
    }
    
    public void updatePlaytime() {
        long currentTime = System.currentTimeMillis();
        totalPlaytime += (currentTime - joinTime);
        joinTime = currentTime;
    }
    
    public long getPlaytimeMinutes() {
        return totalPlaytime / (1000 * 60);
    }
    
    public boolean isRankupNotificationSent() {
        return rankupNotificationSent;
    }
    
    public void setRankupNotificationSent(boolean rankupNotificationSent) {
        this.rankupNotificationSent = rankupNotificationSent;
    }
    
    public void reset() {
        this.currentRank = 0;
        this.totalPlaytime = 0;
        this.mobKills.clear();
        this.blockBreaks.clear();
        this.fishingCatches.clear();
        this.farmingHarvests.clear();
        this.rankupNotificationSent = false;
    }
} 