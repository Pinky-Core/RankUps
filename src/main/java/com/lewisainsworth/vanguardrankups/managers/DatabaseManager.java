package com.lewisainsworth.vanguardrankups.managers;

import com.lewisainsworth.vanguardrankups.VanguardRankUps;
import com.lewisainsworth.vanguardrankups.models.PlayerData;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DatabaseManager {
    
    private final VanguardRankUps plugin;
    private Connection connection;
    private final String dbType;
    
    public DatabaseManager(VanguardRankUps plugin) {
        this.plugin = plugin;
        this.dbType = getRankupsYmlConfig().getString("database.type", "sqlite");
    }
    
    public void initialize() {
        try {
            if ("sqlite".equalsIgnoreCase(dbType)) {
                initializeSQLite();
            } else if ("mysql".equalsIgnoreCase(dbType)) {
                initializeMySQL();
            } else {
                plugin.getLogger().severe("Unsupported database type: " + dbType);
                return;
            }
            
            createTables();
                    plugin.getLogger().info("Database initialized successfully using " + dbType.toUpperCase());
    } catch (SQLException e) {
        plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
        e.printStackTrace();
    }
}

private org.bukkit.configuration.file.YamlConfiguration getRankupsYmlConfig() {
    java.io.File rankupsFile = new java.io.File(plugin.getDataFolder(), "rankups.yml");
    if (rankupsFile.exists()) {
        return org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(rankupsFile);
    }
    return new org.bukkit.configuration.file.YamlConfiguration();
}
    
    private void initializeSQLite() throws SQLException {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        String dbFile = getRankupsYmlConfig().getString("database.sqlite.file", "rankups.db");
        String dbPath = new File(dataFolder, dbFile).getAbsolutePath();
        
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }
    
    private void initializeMySQL() throws SQLException {
        ConfigurationSection mysqlConfig = getRankupsYmlConfig().getConfigurationSection("database.mysql");
        if (mysqlConfig == null) {
            throw new SQLException("MySQL configuration not found");
        }
        
        String host = mysqlConfig.getString("host", "localhost");
        int port = mysqlConfig.getInt("port", 3306);
        String database = mysqlConfig.getString("database", "rankups");
        String username = mysqlConfig.getString("username", "root");
        String password = mysqlConfig.getString("password", "password");
        
        String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC", host, port, database);
        connection = DriverManager.getConnection(url, username, password);
    }
    
    private void createTables() throws SQLException {
        String createPlayersTable = """
            CREATE TABLE IF NOT EXISTS players (
                uuid VARCHAR(36) PRIMARY KEY,
                name VARCHAR(16) NOT NULL,
                current_rank INT DEFAULT 0,
                join_time BIGINT DEFAULT 0,
                total_playtime BIGINT DEFAULT 0
            )
        """;
        
        String createMobKillsTable = """
            CREATE TABLE IF NOT EXISTS mob_kills (
                uuid VARCHAR(36),
                mob_type VARCHAR(32),
                count INT DEFAULT 0,
                PRIMARY KEY (uuid, mob_type),
                FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE
            )
        """;
        
        String createBlockBreaksTable = """
            CREATE TABLE IF NOT EXISTS block_breaks (
                uuid VARCHAR(36),
                block_type VARCHAR(32),
                count INT DEFAULT 0,
                PRIMARY KEY (uuid, block_type),
                FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE
            )
        """;
        
        String createFishingTable = """
            CREATE TABLE IF NOT EXISTS fishing_catches (
                uuid VARCHAR(36),
                fish_type VARCHAR(32),
                count INT DEFAULT 0,
                PRIMARY KEY (uuid, fish_type),
                FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE
            )
        """;
        
        String createFarmingTable = """
            CREATE TABLE IF NOT EXISTS farming_harvests (
                uuid VARCHAR(36),
                crop_type VARCHAR(32),
                count INT DEFAULT 0,
                PRIMARY KEY (uuid, crop_type),
                FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE
            )
        """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createPlayersTable);
            stmt.execute(createMobKillsTable);
            stmt.execute(createBlockBreaksTable);
            stmt.execute(createFishingTable);
            stmt.execute(createFarmingTable);
        }
    }
    
    public PlayerData loadPlayerData(UUID playerUUID, String playerName) {
        PlayerData playerData = new PlayerData(playerUUID, playerName);
        
        try {
            // Load basic player data
            String selectPlayer = "SELECT current_rank, join_time, total_playtime FROM players WHERE uuid = ?";
            try (PreparedStatement stmt = connection.prepareStatement(selectPlayer)) {
                stmt.setString(1, playerUUID.toString());
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    playerData.setCurrentRank(rs.getInt("current_rank"));
                    playerData.setJoinTime(rs.getLong("join_time"));
                    playerData.setTotalPlaytime(rs.getLong("total_playtime"));
                } else {
                    // Player doesn't exist, create new record
                    insertNewPlayer(playerUUID, playerName);
                }
            }
            
            // Load mob kills
            loadMobKills(playerUUID, playerData);
            
            // Load block breaks
            loadBlockBreaks(playerUUID, playerData);
            
            // Load fishing catches
            loadFishingCatches(playerUUID, playerData);
            
            // Load farming harvests
            loadFarmingHarvests(playerUUID, playerData);
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load player data: " + e.getMessage());
            e.printStackTrace();
        }
        
        return playerData;
    }
    
    private void insertNewPlayer(UUID playerUUID, String playerName) throws SQLException {
        String insertPlayer = "INSERT INTO players (uuid, name, current_rank, join_time, total_playtime) VALUES (?, ?, 0, ?, 0)";
        try (PreparedStatement stmt = connection.prepareStatement(insertPlayer)) {
            stmt.setString(1, playerUUID.toString());
            stmt.setString(2, playerName);
            stmt.setLong(3, System.currentTimeMillis());
            stmt.executeUpdate();
        }
    }
    
    private void loadMobKills(UUID playerUUID, PlayerData playerData) throws SQLException {
        String selectMobKills = "SELECT mob_type, count FROM mob_kills WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(selectMobKills)) {
            stmt.setString(1, playerUUID.toString());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String mobType = rs.getString("mob_type");
                int count = rs.getInt("count");
                playerData.getMobKills().put(mobType, count);
            }
        }
    }
    
    private void loadBlockBreaks(UUID playerUUID, PlayerData playerData) throws SQLException {
        String selectBlockBreaks = "SELECT block_type, count FROM block_breaks WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(selectBlockBreaks)) {
            stmt.setString(1, playerUUID.toString());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String blockType = rs.getString("block_type");
                int count = rs.getInt("count");
                playerData.getBlockBreaks().put(blockType, count);
            }
        }
    }
    
    private void loadFishingCatches(UUID playerUUID, PlayerData playerData) throws SQLException {
        String selectFishing = "SELECT fish_type, count FROM fishing_catches WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(selectFishing)) {
            stmt.setString(1, playerUUID.toString());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String fishType = rs.getString("fish_type");
                int count = rs.getInt("count");
                playerData.getFishingCatches().put(fishType, count);
            }
        }
    }
    
    private void loadFarmingHarvests(UUID playerUUID, PlayerData playerData) throws SQLException {
        String selectFarming = "SELECT crop_type, count FROM farming_harvests WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(selectFarming)) {
            stmt.setString(1, playerUUID.toString());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String cropType = rs.getString("crop_type");
                int count = rs.getInt("count");
                playerData.getFarmingHarvests().put(cropType, count);
            }
        }
    }
    
    public void savePlayerData(PlayerData playerData) {
        try {
            // Update basic player data
            String updatePlayer = "UPDATE players SET current_rank = ?, total_playtime = ? WHERE uuid = ?";
            try (PreparedStatement stmt = connection.prepareStatement(updatePlayer)) {
                stmt.setInt(1, playerData.getCurrentRank());
                stmt.setLong(2, playerData.getTotalPlaytime());
                stmt.setString(3, playerData.getPlayerUUID().toString());
                stmt.executeUpdate();
            }
            
            // Save mob kills
            saveMobKills(playerData);
            
            // Save block breaks
            saveBlockBreaks(playerData);
            
            // Save fishing catches
            saveFishingCatches(playerData);
            
            // Save farming harvests
            saveFarmingHarvests(playerData);
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save player data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void saveMobKills(PlayerData playerData) throws SQLException {
        String upsertMobKills = "INSERT OR REPLACE INTO mob_kills (uuid, mob_type, count) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(upsertMobKills)) {
            for (Map.Entry<String, Integer> entry : playerData.getMobKills().entrySet()) {
                stmt.setString(1, playerData.getPlayerUUID().toString());
                stmt.setString(2, entry.getKey());
                stmt.setInt(3, entry.getValue());
                stmt.executeUpdate();
            }
        }
    }
    
    private void saveBlockBreaks(PlayerData playerData) throws SQLException {
        String upsertBlockBreaks = "INSERT OR REPLACE INTO block_breaks (uuid, block_type, count) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(upsertBlockBreaks)) {
            for (Map.Entry<String, Integer> entry : playerData.getBlockBreaks().entrySet()) {
                stmt.setString(1, playerData.getPlayerUUID().toString());
                stmt.setString(2, entry.getKey());
                stmt.setInt(3, entry.getValue());
                stmt.executeUpdate();
            }
        }
    }
    
    private void saveFishingCatches(PlayerData playerData) throws SQLException {
        String upsertFishing = "INSERT OR REPLACE INTO fishing_catches (uuid, fish_type, count) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(upsertFishing)) {
            for (Map.Entry<String, Integer> entry : playerData.getFishingCatches().entrySet()) {
                stmt.setString(1, playerData.getPlayerUUID().toString());
                stmt.setString(2, entry.getKey());
                stmt.setInt(3, entry.getValue());
                stmt.executeUpdate();
            }
        }
    }
    
    private void saveFarmingHarvests(PlayerData playerData) throws SQLException {
        String upsertFarming = "INSERT OR REPLACE INTO farming_harvests (uuid, crop_type, count) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(upsertFarming)) {
            for (Map.Entry<String, Integer> entry : playerData.getFarmingHarvests().entrySet()) {
                stmt.setString(1, playerData.getPlayerUUID().toString());
                stmt.setString(2, entry.getKey());
                stmt.setInt(3, entry.getValue());
                stmt.executeUpdate();
            }
        }
    }
    
    public void resetPlayerData(UUID playerUUID) {
        try {
            String deletePlayer = "DELETE FROM players WHERE uuid = ?";
            try (PreparedStatement stmt = connection.prepareStatement(deletePlayer)) {
                stmt.setString(1, playerUUID.toString());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to reset player data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to close database connection: " + e.getMessage());
            }
        }
    }
} 