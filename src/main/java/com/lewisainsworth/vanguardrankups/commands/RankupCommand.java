package com.lewisainsworth.vanguardrankups.commands;

import com.lewisainsworth.vanguardrankups.VanguardRankUps;
import com.lewisainsworth.vanguardrankups.models.PlayerData;
import com.lewisainsworth.vanguardrankups.models.Rank;
import com.lewisainsworth.vanguardrankups.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class RankupCommand implements CommandExecutor {
    
    private final VanguardRankUps plugin;
    
    public RankupCommand(VanguardRankUps plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageUtils().sendMessage(sender, "&cEste comando solo puede ser usado por jugadores.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("vanguardrankups.rankup")) {
            plugin.getMessageUtils().sendMessage(player, plugin.getConfigManager().getMessage("no_permission"));
            return true;
        }
        
        if (args.length == 0) {
            // Por defecto mostrar solo progreso
            showProgress(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "info":
            case "i":
                showRankupInfo(player);
                break;
                
            case "progress":
            case "p":
                showProgress(player);
                break;
                
            case "help":
            case "h":
                showHelp(player);
                break;
                
            case "confirm":
            case "c":
                performRankup(player);
                break;
                
            case "force":
                if (!player.hasPermission("vanguardrankups.force")) {
                    plugin.getMessageUtils().sendMessage(player, plugin.getConfigManager().getMessage("no_permission"));
                    return true;
                }
                handleForceRankup(player, args);
                break;
                
            case "reload":
                if (!player.hasPermission("vanguardrankups.reload")) {
                    plugin.getMessageUtils().sendMessage(player, plugin.getConfigManager().getMessage("no_permission"));
                    return true;
                }
                handleReload(player);
                break;
                
            default:
                // Mostrar progreso por defecto
                showProgress(player);
                break;
        }
        
        return true;
    }
    
    private void showRankupInfo(Player player) {
        PlayerData playerData = plugin.getRankupManager().getOrCreatePlayerData(player.getUniqueId(), player.getName());
        int currentRank = playerData.getCurrentRank();
        int nextRank = currentRank + 1;
        
        Rank currentRankData = plugin.getConfigManager().getRank(currentRank);
        Rank nextRankData = plugin.getConfigManager().getRank(nextRank);
        
        player.sendMessage("§8§m" + "─".repeat(10));
        player.sendMessage("§b§lVanguardRankUps §8- §fInformación del Rango");
        player.sendMessage("§8§m" + "─".repeat(10));
        
        if (currentRankData != null) {
            player.sendMessage("§7Rango actual: §f" + currentRankData.getDisplayName());
        } else {
            player.sendMessage("§7Rango actual: §cSin rango");
        }
        
        if (nextRankData != null) {
            player.sendMessage("§7Siguiente rango: §f" + nextRankData.getDisplayName());
            
            boolean canRankup = plugin.getRankupManager().canRankup(playerData);
            if (canRankup) {
                player.sendMessage("§a§l✅ ¡Puedes hacer rankup! Usa /rankup confirm");
            } else {
                player.sendMessage("§c§l❌ No cumples los requisitos para el siguiente rango");
                player.sendMessage("§7Usa §f/rankup §7para ver tu progreso detallado");
            }
        } else {
            player.sendMessage("§7Siguiente rango: §c§l🎉 ¡Rango máximo alcanzado!");
        }
        
        player.sendMessage("§8§m" + "─".repeat(10));
    }
    
    private void showProgress(Player player) {
        PlayerData playerData = plugin.getRankupManager().getOrCreatePlayerData(player.getUniqueId(), player.getName());
        int currentRank = playerData.getCurrentRank();
        int nextRank = currentRank + 1;
        
        Rank currentRankData = plugin.getConfigManager().getRank(currentRank);
        Rank nextRankData = plugin.getConfigManager().getRank(nextRank);
        
        if (nextRankData == null) {
            plugin.getMessageUtils().sendMessage(player, plugin.getConfigManager().getMessage("already_max_rank"));
            return;
        }
        
        // Configuración de GUI personalizable
        String separatorChar = plugin.getConfigManager().getGUIConfig() != null ? 
            plugin.getConfigManager().getGUIConfig().getString("colors.separator", "&8&m") : "&8&m";
        String titleColor = plugin.getConfigManager().getGUIConfig() != null ? 
            plugin.getConfigManager().getGUIConfig().getString("colors.title", "&b&l") : "&b&l";
        String sectionColor = plugin.getConfigManager().getGUIConfig() != null ? 
            plugin.getConfigManager().getGUIConfig().getString("colors.section", "&e&l") : "&e&l";
        
        String separator = separatorChar + "─".repeat(20);
        
        // Enviar título personalizable
        MessageUtils.sendMessageNoPrefix(player, separator);
        MessageUtils.sendMessageNoPrefix(player, titleColor + plugin.getConfigManager().getMessage("gui_progress_title"));
        MessageUtils.sendMessageNoPrefix(player, separator);
        
        // Información del rango actual y siguiente
        if (currentRankData != null) {
            MessageUtils.sendMessageNoPrefix(player, "§7Rango actual: §f" + currentRankData.getDisplayName());
        }
        MessageUtils.sendMessageNoPrefix(player, "§7Próximo rango: §f" + nextRankData.getDisplayName());
        MessageUtils.sendMessageNoPrefix(player, "");
        
        // Mostrar progreso de mob kills
        if (!nextRankData.getMobKills().isEmpty()) {
            MessageUtils.sendMessageNoPrefix(player, sectionColor + "🗡 Mobs Eliminados:");
            for (Map.Entry<String, Integer> entry : nextRankData.getMobKills().entrySet()) {
                String mobType = entry.getKey();
                int required = entry.getValue();
                int current = playerData.getMobKills(mobType);
                
                String progressMessage = plugin.getConfigManager().getMessage("progress_mob_kills")
                    .replace("%current%", String.valueOf(current))
                    .replace("%required%", String.valueOf(required));
                
                String progressBar = createCustomProgressBar(current, required);
                
                MessageUtils.sendMessageNoPrefix(player, "§7  " + mobType + ": " + progressMessage);
                MessageUtils.sendMessageNoPrefix(player, "§7  " + progressBar);
            }
            MessageUtils.sendMessageNoPrefix(player, "");
        }
        
        // Mostrar progreso de block breaks
        if (!nextRankData.getBlockBreaks().isEmpty()) {
            MessageUtils.sendMessageNoPrefix(player, sectionColor + "⛏ Bloques Minados:");
            for (Map.Entry<String, Integer> entry : nextRankData.getBlockBreaks().entrySet()) {
                String blockType = entry.getKey();
                int required = entry.getValue();
                int current = playerData.getBlockBreaks(blockType);
                
                String progressMessage = plugin.getConfigManager().getMessage("progress_block_breaks")
                    .replace("%current%", String.valueOf(current))
                    .replace("%required%", String.valueOf(required));
                
                String progressBar = createCustomProgressBar(current, required);
                
                MessageUtils.sendMessageNoPrefix(player, "§7  " + blockType + ": " + progressMessage);
                MessageUtils.sendMessageNoPrefix(player, "§7  " + progressBar);
            }
            MessageUtils.sendMessageNoPrefix(player, "");
        }
        
        // Mostrar progreso de playtime
        int requiredPlaytime = nextRankData.getPlaytimeMinutes();
        if (requiredPlaytime > 0) {
            long currentPlaytime = playerData.getPlaytimeMinutes();
            
            String progressMessage = plugin.getConfigManager().getMessage("progress_playtime")
                .replace("%current%", String.valueOf(currentPlaytime))
                .replace("%required%", String.valueOf(requiredPlaytime));
            
            String progressBar = createCustomProgressBar((int)currentPlaytime, requiredPlaytime);
            
            MessageUtils.sendMessageNoPrefix(player, sectionColor + "⏰ Tiempo Jugado:");
            MessageUtils.sendMessageNoPrefix(player, "§7  " + progressMessage);
            MessageUtils.sendMessageNoPrefix(player, "§7  " + progressBar);
            MessageUtils.sendMessageNoPrefix(player, "");
        }
        
        // Mostrar progreso de fishing
        if (!nextRankData.getFishing().isEmpty()) {
            MessageUtils.sendMessageNoPrefix(player, sectionColor + "🎣 Pesca:");
            for (Map.Entry<String, Integer> entry : nextRankData.getFishing().entrySet()) {
                String fishType = entry.getKey();
                int required = entry.getValue();
                int current = playerData.getFishingCatches(fishType);
                
                String progressMessage = plugin.getConfigManager().getMessage("progress_fishing")
                    .replace("%current%", String.valueOf(current))
                    .replace("%required%", String.valueOf(required));
                
                String progressBar = createCustomProgressBar(current, required);
                
                MessageUtils.sendMessageNoPrefix(player, "§7  " + fishType + ": " + progressMessage);
                MessageUtils.sendMessageNoPrefix(player, "§7  " + progressBar);
            }
            MessageUtils.sendMessageNoPrefix(player, "");
        }
        
        // Mostrar progreso de farming
        if (!nextRankData.getFarming().isEmpty()) {
            MessageUtils.sendMessageNoPrefix(player, sectionColor + "🌾 Agricultura:");
            for (Map.Entry<String, Integer> entry : nextRankData.getFarming().entrySet()) {
                String cropType = entry.getKey();
                int required = entry.getValue();
                int current = playerData.getFarmingHarvests(cropType);
                
                String progressMessage = plugin.getConfigManager().getMessage("progress_farming")
                    .replace("%current%", String.valueOf(current))
                    .replace("%required%", String.valueOf(required));
                
                String progressBar = createCustomProgressBar(current, required);
                
                MessageUtils.sendMessageNoPrefix(player, "§7  " + cropType + ": " + progressMessage);
                MessageUtils.sendMessageNoPrefix(player, "§7  " + progressBar);
            }
            MessageUtils.sendMessageNoPrefix(player, "");
        }
        
        // Mostrar progreso de misiones completadas
        int requiredQuests = nextRankData.getRequiredQuests();
        if (requiredQuests > 0) {
            int currentQuests = playerData.getTotalCompletedQuests();
            
            String progressMessage = plugin.getConfigManager().getMessage("progress_quests")
                .replace("%current%", String.valueOf(currentQuests))
                .replace("%required%", String.valueOf(requiredQuests));
            
            String progressBar = createCustomProgressBar(currentQuests, requiredQuests);
            
            MessageUtils.sendMessageNoPrefix(player, sectionColor + "📋 Misiones Completadas:");
            MessageUtils.sendMessageNoPrefix(player, "§7  " + progressMessage);
            MessageUtils.sendMessageNoPrefix(player, "§7  " + progressBar);
            MessageUtils.sendMessageNoPrefix(player, "");
        }
        
        // Mostrar estado de rankup
        if (plugin.getRankupManager().canRankup(playerData)) {
            MessageUtils.sendMessageNoPrefix(player, "§a§l✅ ¡Puedes hacer rankup! Usa /rankup confirm");
        } else {
            MessageUtils.sendMessageNoPrefix(player, "§c§l❌ Completa todos los requisitos para hacer rankup");
        }
        
        MessageUtils.sendMessageNoPrefix(player, separator);
    }
    
    private String createCustomProgressBar(int current, int required) {
        if (required <= 0) return "";
        
        // Obtener configuración personalizable de las barras de progreso
        int barLength = 20;
        String filledChar = "█";
        String emptyChar = "░";
        boolean showPercentage = true;
        boolean showNumbers = true;
        
        if (plugin.getConfigManager().getGUIConfig() != null) {
            barLength = plugin.getConfigManager().getGUIConfig().getInt("progress_bar.length", 20);
            filledChar = plugin.getConfigManager().getGUIConfig().getString("progress_bar.filled", "&a█");
            emptyChar = plugin.getConfigManager().getGUIConfig().getString("progress_bar.empty", "&7░");
            showPercentage = plugin.getConfigManager().getGUIConfig().getBoolean("progress_bar.show_percentage", true);
            showNumbers = plugin.getConfigManager().getGUIConfig().getBoolean("progress_bar.show_numbers", true);
        }
        
        // Calcular progreso
        double progress = Math.min(1.0, (double) current / required);
        int filledLength = (int) Math.round(progress * barLength);
        
        // Crear la barra
        StringBuilder bar = new StringBuilder();
        
        // Caracteres llenos
        for (int i = 0; i < filledLength; i++) {
            bar.append(filledChar);
        }
        
        // Caracteres vacíos
        for (int i = filledLength; i < barLength; i++) {
            bar.append(emptyChar);
        }
        
        // Agregar información adicional
        if (showNumbers || showPercentage) {
            bar.append(" §7");
            if (showNumbers) {
                bar.append(current).append("/").append(required);
            }
            if (showNumbers && showPercentage) {
                bar.append(" ");
            }
            if (showPercentage) {
                bar.append("(").append((int)(progress * 100)).append("%)");
            }
        }
        
        return bar.toString();
    }
    
    private void showHelp(Player player) {
        player.sendMessage("§8§m" + "─".repeat(15));
        player.sendMessage("§b§lVanguardRankUps §8- §fComandos");
        player.sendMessage("§8§m" + "─".repeat(15));
        player.sendMessage("§7/rankup §8- §fVer tu progreso detallado");
        player.sendMessage("§7/rankup info §8- §fVer información básica del rango");
        player.sendMessage("§7/rankup confirm §8- §fConfirmar rankup");
        player.sendMessage("§7/rankup help §8- §fMostrar esta ayuda");
        player.sendMessage("§8§m" + "─".repeat(15));
    }
    
    private void performRankup(Player player) {
        PlayerData playerData = plugin.getRankupManager().getOrCreatePlayerData(player.getUniqueId(), player.getName());
        
        if (!plugin.getRankupManager().canRankup(playerData)) {
            plugin.getMessageUtils().sendMessage(player, plugin.getConfigManager().getMessage("rankup_failed"));
            return;
        }
        
        if (plugin.getRankupManager().performRankup(playerData)) {
            // Success message is sent in performRankup method
        } else {
            plugin.getMessageUtils().sendMessage(player, plugin.getConfigManager().getMessage("rankup_failed"));
        }
    }
    
    private void handleForceRankup(Player player, String[] args) {
        if (args.length < 2) {
            plugin.getMessageUtils().sendMessage(player, "§cUso: /rankup force <jugador>");
            return;
        }
        
        String targetName = args[1];
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null) {
            plugin.getMessageUtils().sendMessage(player, plugin.getConfigManager().getMessage("player_not_found"));
            return;
        }
        
        PlayerData targetData = plugin.getRankupManager().getOrCreatePlayerData(target.getUniqueId(), target.getName());
        int currentRank = targetData.getCurrentRank();
        int nextRank = currentRank + 1;
        
        Rank nextRankData = plugin.getConfigManager().getRank(nextRank);
        if (nextRankData == null) {
            plugin.getMessageUtils().sendMessage(player, "§cEl jugador ya tiene el rango máximo.");
            return;
        }
        
        if (plugin.getRankupManager().performRankup(targetData)) {
            String message = plugin.getConfigManager().getMessage("rank_forced")
                .replace("%player%", target.getName());
            plugin.getMessageUtils().sendMessage(player, message);
        } else {
            plugin.getMessageUtils().sendMessage(player, "§cNo se pudo forzar el rankup.");
        }
    }
    
    private void handleReload(Player player) {
        plugin.getConfigManager().loadConfig();
        String message = plugin.getConfigManager().getMessage("config_reloaded");
        plugin.getMessageUtils().sendMessage(player, message);
    }
} 