package com.lewisainsworth.vanguardrankups;

import com.lewisainsworth.vanguardrankups.commands.RankupCommand;
import com.lewisainsworth.vanguardrankups.commands.RankupAdminCommand;
import com.lewisainsworth.vanguardrankups.listeners.PlayerListener;
import com.lewisainsworth.vanguardrankups.listeners.QuestListener;
import com.lewisainsworth.vanguardrankups.managers.ConfigManager;
import com.lewisainsworth.vanguardrankups.managers.DatabaseManager;
import com.lewisainsworth.vanguardrankups.managers.RankupManager;
import com.lewisainsworth.vanguardrankups.managers.TaskManager;
import com.lewisainsworth.vanguardrankups.utils.MessageUtils;
import org.bukkit.plugin.java.JavaPlugin;

public class VanguardRankUps extends JavaPlugin {
    
    private static VanguardRankUps instance;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private RankupManager rankupManager;
    private TaskManager taskManager;
    private MessageUtils messageUtils;
    
    @Override
    public void onEnable() {
        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.databaseManager = new DatabaseManager(this);
        this.rankupManager = new RankupManager(this);
        this.taskManager = new TaskManager(this);
        this.messageUtils = new MessageUtils(this);
        
        // Load configuration
        configManager.loadConfig();
        
        // Initialize database
        databaseManager.initialize();
        
        // Initialize managers
        // This block is now redundant as managers are initialized above
        // this.rankupManager = new RankupManager(this);
        // this.taskManager = new TaskManager(this);
        
        // Register commands
        getCommand("rankup").setExecutor(new RankupCommand(this));
        getCommand("rankup").setTabCompleter(new com.lewisainsworth.vanguardrankups.commands.RankupTabCompleter(this));
        getCommand("rankupadmin").setExecutor(new RankupAdminCommand(this));
        getCommand("rankupadmin").setTabCompleter(new com.lewisainsworth.vanguardrankups.commands.RankupAdminTabCompleter(this));
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new QuestListener(this), this);
        
        // Start task scheduler
        taskManager.startScheduler();
        
        getLogger().info("VanguardRankUps has been enabled successfully!");
        getLogger().info("Author: Lewis Ainsworth");
        getLogger().info("Version: " + getDescription().getVersion());
    }
    
    @Override
    public void onDisable() {
        if (taskManager != null) {
            taskManager.stopScheduler();
        }
        
        if (databaseManager != null) {
            databaseManager.close();
        }
        
        getLogger().info("VanguardRankUps has been disabled!");
    }
    
    public static VanguardRankUps getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public RankupManager getRankupManager() {
        return rankupManager;
    }
    
    public TaskManager getTaskManager() {
        return taskManager;
    }
    
    public MessageUtils getMessageUtils() {
        return messageUtils;
    }
} 