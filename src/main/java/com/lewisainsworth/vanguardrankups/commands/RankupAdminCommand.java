package com.lewisainsworth.vanguardrankups.commands;

import com.lewisainsworth.vanguardrankups.VanguardRankUps;
import com.lewisainsworth.vanguardrankups.models.PlayerData;
import com.lewisainsworth.vanguardrankups.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RankupAdminCommand implements CommandExecutor {
    
    private final VanguardRankUps plugin;
    
    public RankupAdminCommand(VanguardRankUps plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("vanguardrankups.admin")) {
            plugin.getMessageUtils().sendMessage(sender, plugin.getConfigManager().getMessage("no_permission"));
            return true;
        }
        
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "reload":
                handleReload(sender);
                break;
                
            case "force":
                handleForceRankup(sender, args);
                break;
                
            case "reset":
                handleResetPlayer(sender, args);
                break;
                
            case "setrank":
                handleSetRank(sender, args);
                break;
                
            case "info":
                handlePlayerInfo(sender, args);
                break;
                
            case "help":
                showHelp(sender);
                break;
                
            default:
                plugin.getMessageUtils().sendMessage(sender, "§cComando desconocido. Usa /rankupadmin help para ver los comandos disponibles.");
                break;
        }
        
        return true;
    }
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage("§8§m" + "─".repeat(20));
        sender.sendMessage("§b§lVanguardRankUps §8- §fComandos de Administración");
        sender.sendMessage("§8§m" + "─".repeat(20));
        sender.sendMessage("§7/rankupadmin reload §8- §fRecargar configuración");
        sender.sendMessage("§7/rankupadmin force <jugador> §8- §fForzar rankup de un jugador");
        sender.sendMessage("§7/rankupadmin reset <jugador> §8- §fResetear progreso de un jugador");
        sender.sendMessage("§7/rankupadmin setrank <jugador> <rango> §8- §fEstablecer rango de un jugador");
        sender.sendMessage("§7/rankupadmin info <jugador> §8- §fVer información de un jugador");
        sender.sendMessage("§7/rankupadmin help §8- §fMostrar esta ayuda");
        sender.sendMessage("§8§m" + "─".repeat(20));
    }
    
    private void handleReload(CommandSender sender) {
        plugin.getConfigManager().loadConfig();
        String message = plugin.getConfigManager().getMessage("config_reloaded");
        plugin.getMessageUtils().sendMessage(sender, message);
    }
    
    private void handleForceRankup(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.getMessageUtils().sendMessage(sender, "§cUso: /rankupadmin force <jugador>");
            return;
        }
        
        String targetName = args[1];
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null) {
            plugin.getMessageUtils().sendMessage(sender, plugin.getConfigManager().getMessage("player_not_found"));
            return;
        }
        
        PlayerData targetData = plugin.getRankupManager().getOrCreatePlayerData(target.getUniqueId(), target.getName());
        
        if (plugin.getRankupManager().performRankup(targetData)) {
            String message = plugin.getConfigManager().getMessage("rank_forced")
                .replace("%player%", target.getName());
            plugin.getMessageUtils().sendMessage(sender, message);
        } else {
            plugin.getMessageUtils().sendMessage(sender, "§cNo se pudo forzar el rankup para " + target.getName());
        }
    }
    
    private void handleResetPlayer(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.getMessageUtils().sendMessage(sender, "§cUso: /rankupadmin reset <jugador>");
            return;
        }
        
        String targetName = args[1];
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null) {
            plugin.getMessageUtils().sendMessage(sender, plugin.getConfigManager().getMessage("player_not_found"));
            return;
        }
        
        plugin.getRankupManager().resetPlayer(target.getUniqueId());
        
        String message = plugin.getConfigManager().getMessage("rank_reset")
            .replace("%player%", target.getName());
        plugin.getMessageUtils().sendMessage(sender, message);
    }
    
    private void handleSetRank(CommandSender sender, String[] args) {
        if (args.length < 3) {
            plugin.getMessageUtils().sendMessage(sender, "§cUso: /rankupadmin setrank <jugador> <rango>");
            plugin.getMessageUtils().sendMessage(sender, "§7Ejemplos:");
            plugin.getMessageUtils().sendMessage(sender, "§7  /rankupadmin setrank <jugador> 5");
            plugin.getMessageUtils().sendMessage(sender, "§7  /rankupadmin setrank <jugador> plebeyo-iii");
            return;
        }
        
        String targetName = args[1];
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null) {
            plugin.getMessageUtils().sendMessage(sender, plugin.getConfigManager().getMessage("player_not_found"));
            return;
        }
        
        String rankArg = args[2];
        int rankLevel = -1;
        
        // Primero intentar como número
        try {
            rankLevel = Integer.parseInt(rankArg);
            
            if (rankLevel < 0 || rankLevel > plugin.getConfigManager().getMaxRank()) {
                plugin.getMessageUtils().sendMessage(sender, "§cEl rango debe estar entre 0 y " + plugin.getConfigManager().getMaxRank());
                return;
            }
        } catch (NumberFormatException e) {
            // Si no es número, buscar por nombre de grupo de LuckPerms
            rankLevel = plugin.getConfigManager().getRankLevelByGroup(rankArg);
            
            if (rankLevel == -1) {
                plugin.getMessageUtils().sendMessage(sender, "§cRango no encontrado: " + rankArg);
                plugin.getMessageUtils().sendMessage(sender, "§7Usa un número (0-" + plugin.getConfigManager().getMaxRank() + ") o un nombre de grupo válido");
                return;
            }
        }
        
        plugin.getRankupManager().forceRankup(target.getUniqueId(), rankLevel);
        
        String message = plugin.getConfigManager().getMessage("rank_set")
            .replace("%player%", target.getName())
            .replace("%rank%", String.valueOf(rankLevel));
        plugin.getMessageUtils().sendMessage(sender, message);
    }
    
    private void handlePlayerInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.getMessageUtils().sendMessage(sender, "§cUso: /rankupadmin info <jugador>");
            return;
        }
        
        String targetName = args[1];
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null) {
            plugin.getMessageUtils().sendMessage(sender, plugin.getConfigManager().getMessage("player_not_found"));
            return;
        }
        
        PlayerData targetData = plugin.getRankupManager().getOrCreatePlayerData(target.getUniqueId(), target.getName());
        
        sender.sendMessage("§8§m" + "─".repeat(20));
        sender.sendMessage("§b§lInformación de " + target.getName());
        sender.sendMessage("§8§m" + "─".repeat(20));
        sender.sendMessage("§7Rango actual: §f" + targetData.getCurrentRank());
        sender.sendMessage("§7Tiempo total jugado: §f" + plugin.getMessageUtils().formatTime(targetData.getPlaytimeMinutes()));
        
        // Mob kills
        if (!targetData.getMobKills().isEmpty()) {
            sender.sendMessage("§c§lMobs eliminados:");
            for (var entry : targetData.getMobKills().entrySet()) {
                sender.sendMessage("§7  " + entry.getKey() + ": §f" + entry.getValue());
            }
        }
        
        // Block breaks
        if (!targetData.getBlockBreaks().isEmpty()) {
            sender.sendMessage("§6§lBloques minados:");
            for (var entry : targetData.getBlockBreaks().entrySet()) {
                sender.sendMessage("§7  " + entry.getKey() + ": §f" + entry.getValue());
            }
        }
        
        // Fishing
        if (!targetData.getFishingCatches().isEmpty()) {
            sender.sendMessage("§b§lPescas:");
            for (var entry : targetData.getFishingCatches().entrySet()) {
                sender.sendMessage("§7  " + entry.getKey() + ": §f" + entry.getValue());
            }
        }
        
        // Farming
        if (!targetData.getFarmingHarvests().isEmpty()) {
            sender.sendMessage("§a§lCultivos:");
            for (var entry : targetData.getFarmingHarvests().entrySet()) {
                sender.sendMessage("§7  " + entry.getKey() + ": §f" + entry.getValue());
            }
        }
        
        sender.sendMessage("§8§m" + "─".repeat(40));
    }
} 