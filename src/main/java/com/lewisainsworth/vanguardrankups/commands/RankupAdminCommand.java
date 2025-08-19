package com.lewisainsworth.vanguardrankups.commands;

import com.lewisainsworth.vanguardrankups.VanguardRankUps;
import com.lewisainsworth.vanguardrankups.models.PlayerData;
import com.lewisainsworth.vanguardrankups.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.Map;

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
                
            case "requirements":
            case "req":
                handleRequirements(sender, args);
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
        sender.sendMessage("§7/rankupadmin requirements <rango> §8- §fVer requisitos de un rango");
        sender.sendMessage("§7/rankupadmin requirements <rango> <tipo> <objetivo> <valor> §8- §fCambiar requisito");
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
    
    private void handleRequirements(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.getMessageUtils().sendMessage(sender, "§cUso: /rankupadmin requirements <rango>");
            plugin.getMessageUtils().sendMessage(sender, "§cO para cambiar: /rankupadmin requirements <rango> <tipo> <objetivo> <valor>");
            return;
        }
        
        // Parse rank level
        int rankLevel;
        try {
            rankLevel = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            plugin.getMessageUtils().sendMessage(sender, "§cEl rango debe ser un número válido");
            return;
        }
        
        if (rankLevel < 1 || rankLevel > plugin.getConfigManager().getMaxRank()) {
            plugin.getMessageUtils().sendMessage(sender, "§cEl rango debe estar entre 1 y " + plugin.getConfigManager().getMaxRank());
            return;
        }
        
        // If only rank specified, show current requirements
        if (args.length == 2) {
            showRankRequirements(sender, rankLevel);
            return;
        }
        
        // If 5 args, update requirement
        if (args.length == 5) {
            updateRequirement(sender, rankLevel, args);
            return;
        }
        
        plugin.getMessageUtils().sendMessage(sender, "§cUso: /rankupadmin requirements <rango>");
        plugin.getMessageUtils().sendMessage(sender, "§cO para cambiar: /rankupadmin requirements <rango> <tipo> <objetivo> <valor>");
    }
    
    private void showRankRequirements(CommandSender sender, int rankLevel) {
        var requirements = plugin.getRankupManager().getRankRequirements(rankLevel);
        if (requirements == null) {
            plugin.getMessageUtils().sendMessage(sender, "§cNo se encontraron requisitos para el rango " + rankLevel);
            return;
        }
        
        sender.sendMessage("§8§m" + "─".repeat(30));
        sender.sendMessage("§b§lRequisitos del Rango " + rankLevel);
        sender.sendMessage("§8§m" + "─".repeat(30));
        
        // Mob kills
        if (requirements.containsKey("mob_kills")) {
            @SuppressWarnings("unchecked")
            var mobKills = (Map<String, Integer>) requirements.get("mob_kills");
            sender.sendMessage("§c§lMobs Eliminados:");
            for (var entry : mobKills.entrySet()) {
                sender.sendMessage("§7  " + entry.getKey() + ": §f" + entry.getValue());
            }
        }
        
        // Block breaks
        if (requirements.containsKey("block_breaks")) {
            @SuppressWarnings("unchecked")
            var blockBreaks = (Map<String, Integer>) requirements.get("block_breaks");
            sender.sendMessage("§6§lBloques Minados:");
            for (var entry : blockBreaks.entrySet()) {
                sender.sendMessage("§7  " + entry.getKey() + ": §f" + entry.getValue());
            }
        }
        
        // Playtime
        if (requirements.containsKey("playtime_minutes")) {
            @SuppressWarnings("unchecked")
            var playtime = (Map<String, Integer>) requirements.get("playtime_minutes");
            int total = playtime.getOrDefault("total", 0);
            sender.sendMessage("§e§lTiempo Jugado: §f" + total + " minutos");
        }
        
        // Fishing
        if (requirements.containsKey("fishing")) {
            @SuppressWarnings("unchecked")
            var fishing = (Map<String, Integer>) requirements.get("fishing");
            sender.sendMessage("§b§lPesca:");
            for (var entry : fishing.entrySet()) {
                sender.sendMessage("§7  " + entry.getKey() + ": §f" + entry.getValue());
            }
        }
        
        // Farming
        if (requirements.containsKey("farming")) {
            @SuppressWarnings("unchecked")
            var farming = (Map<String, Integer>) requirements.get("farming");
            sender.sendMessage("§a§lAgricultura:");
            for (var entry : farming.entrySet()) {
                sender.sendMessage("§7  " + entry.getKey() + ": §f" + entry.getValue());
            }
        }
        
        sender.sendMessage("§8§m" + "─".repeat(30));
    }
    
    private void updateRequirement(CommandSender sender, int rankLevel, String[] args) {
        String requirementType = args[2].toLowerCase();
        String target = args[3];
        int newValue;
        
        try {
            newValue = Integer.parseInt(args[4]);
            if (newValue < 0) {
                plugin.getMessageUtils().sendMessage(sender, "§cEl valor debe ser mayor o igual a 0");
                return;
            }
        } catch (NumberFormatException e) {
            plugin.getMessageUtils().sendMessage(sender, "§cEl valor debe ser un número válido");
            return;
        }
        
        boolean success = plugin.getRankupManager().updateRankRequirements(rankLevel, requirementType, target, newValue);
        
        if (success) {
            plugin.getMessageUtils().sendMessage(sender, "§a✅ Requisito actualizado exitosamente!");
            plugin.getMessageUtils().sendMessage(sender, "§7Rango: §f" + rankLevel);
            plugin.getMessageUtils().sendMessage(sender, "§7Tipo: §f" + requirementType);
            plugin.getMessageUtils().sendMessage(sender, "§7Objetivo: §f" + target);
            plugin.getMessageUtils().sendMessage(sender, "§7Nuevo valor: §f" + newValue);
            
            // Show updated requirements
            showRankRequirements(sender, rankLevel);
        } else {
            plugin.getMessageUtils().sendMessage(sender, "§c❌ Error al actualizar el requisito");
            plugin.getMessageUtils().sendMessage(sender, "§7Tipos válidos: mob_kills, block_breaks, playtime_minutes, fishing, farming");
        }
    }
} 