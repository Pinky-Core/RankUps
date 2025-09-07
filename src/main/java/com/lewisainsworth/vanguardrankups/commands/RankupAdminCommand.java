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
                
            case "addrequirement":
            case "addreq":
                handleAddRequirement(sender, args);
                break;
                
            case "fixplaytime":
                handleFixPlaytime(sender, args);
                break;
                
            case "syncplaytime":
                handleSyncPlaytime(sender, args);
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
        sender.sendMessage("§7/rankupadmin addrequirement <jugador> <tipo> <objetivo> <cantidad> §8- §fSumar requisito a jugador");
        sender.sendMessage("§7/rankupadmin fixplaytime <jugador> <minutos> §8- §fCorregir tiempo jugado de un jugador");
        sender.sendMessage("§7/rankupadmin syncplaytime <jugador|all> §8- §fSincronizar tiempo con estadísticas de Minecraft");
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
        sender.sendMessage("§7Tiempo total jugado: §f" + targetData.getFormattedPlaytime());
        sender.sendMessage("§7Tiempo en minutos: §f" + targetData.getPlaytimeMinutes() + " min");
        
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
    
    /**
     * Handle adding requirements to a player
     */
    private void handleAddRequirement(CommandSender sender, String[] args) {
        if (args.length < 5) {
            plugin.getMessageUtils().sendMessage(sender, "§cUso: /rankupadmin addrequirement <jugador> <tipo> <objetivo> <cantidad>");
            plugin.getMessageUtils().sendMessage(sender, "§7Tipos válidos: mob_kills, block_breaks, playtime_minutes, fishing, farming, quests");
            plugin.getMessageUtils().sendMessage(sender, "§7Ejemplo: /rankupadmin addrequirement Player123 quests total 30");
            return;
        }
        
        String targetName = args[1];
        String requirementType = args[2].toLowerCase();
        String target = args[3];
        int amount;
        
        try {
            amount = Integer.parseInt(args[4]);
            if (amount < 0) {
                plugin.getMessageUtils().sendMessage(sender, "§cLa cantidad debe ser mayor o igual a 0");
                return;
            }
        } catch (NumberFormatException e) {
            plugin.getMessageUtils().sendMessage(sender, "§cLa cantidad debe ser un número válido");
            return;
        }
        
        // Get player data
        PlayerData playerData = plugin.getRankupManager().getOrCreatePlayerData(
            Bukkit.getOfflinePlayer(targetName).getUniqueId(), 
            targetName
        );
        
        boolean success = false;
        String message = "";
        
        switch (requirementType) {
            case "mob_kills":
                int currentMobKills = playerData.getMobKills(target);
                // Agregar la cantidad especificada
                for (int i = 0; i < amount; i++) {
                    playerData.addMobKill(target);
                }
                success = true;
                message = "§a✅ Mobs eliminados de " + target + " sumados exitosamente!";
                break;
                
            case "block_breaks":
                int currentBlockBreaks = playerData.getBlockBreaks(target);
                // Agregar la cantidad especificada
                for (int i = 0; i < amount; i++) {
                    playerData.addBlockBreak(target);
                }
                success = true;
                message = "§a✅ Bloques minados de " + target + " sumados exitosamente!";
                break;
                
            case "playtime_minutes":
                long currentPlaytime = playerData.getPlaytimeMinutes();
                // Agregar tiempo jugado directamente
                playerData.addPlaytimeMinutes(amount);
                success = true;
                message = "§a✅ Tiempo jugado sumado exitosamente!";
                break;
                
            case "fishing":
                int currentFishing = playerData.getFishingCatches(target);
                // Agregar la cantidad especificada
                for (int i = 0; i < amount; i++) {
                    playerData.addFishingCatch(target);
                }
                success = true;
                message = "§a✅ Pesca de " + target + " sumada exitosamente!";
                break;
                
            case "farming":
                int currentFarming = playerData.getFarmingHarvests(target);
                // Agregar la cantidad especificada
                for (int i = 0; i < amount; i++) {
                    playerData.addFarmingHarvest(target);
                }
                success = true;
                message = "§a✅ Agricultura de " + target + " sumada exitosamente!";
                break;
                
            case "quests":
                int currentQuests = playerData.getTotalCompletedQuests();
                // Agregar la cantidad especificada
                for (int i = 0; i < amount; i++) {
                    playerData.addCompletedQuest();
                }
                success = true;
                message = "§a✅ Misiones completadas sumadas exitosamente!";
                break;
                
            default:
                plugin.getMessageUtils().sendMessage(sender, "§c❌ Tipo de requisito inválido");
                plugin.getMessageUtils().sendMessage(sender, "§7Tipos válidos: mob_kills, block_breaks, playtime_minutes, fishing, farming, quests");
                return;
        }
        
        if (success) {
            // Save player data
            plugin.getDatabaseManager().savePlayerData(playerData);
            
            // Send success message
            plugin.getMessageUtils().sendMessage(sender, message);
            plugin.getMessageUtils().sendMessage(sender, "§7Jugador: §f" + targetName);
            plugin.getMessageUtils().sendMessage(sender, "§7Tipo: §f" + requirementType);
            plugin.getMessageUtils().sendMessage(sender, "§7Objetivo: §f" + target);
            plugin.getMessageUtils().sendMessage(sender, "§7Cantidad sumada: §f" + amount);
            
            // Show updated player info
            if (sender.hasPermission("vanguardrankups.admin")) {
                handlePlayerInfo(sender, new String[]{"info", targetName});
            }
        }
    }
    
    /**
     * Handle fixing playtime for a player
     */
    private void handleFixPlaytime(CommandSender sender, String[] args) {
        if (args.length < 3) {
            plugin.getMessageUtils().sendMessage(sender, "§cUso: /rankupadmin fixplaytime <jugador> <minutos>");
            plugin.getMessageUtils().sendMessage(sender, "§7Ejemplo: /rankupadmin fixplaytime Player123 120");
            return;
        }
        
        String targetName = args[1];
        int newMinutes;
        
        try {
            newMinutes = Integer.parseInt(args[2]);
            if (newMinutes < 0) {
                plugin.getMessageUtils().sendMessage(sender, "§cLos minutos deben ser mayor o igual a 0");
                return;
            }
        } catch (NumberFormatException e) {
            plugin.getMessageUtils().sendMessage(sender, "§cLos minutos deben ser un número válido");
            return;
        }
        
        // Get player data
        PlayerData playerData = plugin.getRankupManager().getOrCreatePlayerData(
            Bukkit.getOfflinePlayer(targetName).getUniqueId(), 
            targetName
        );
        
        // Get current playtime for comparison
        long currentMinutes = playerData.getPlaytimeMinutes();
        
        // Reset playtime and set new value
        playerData.resetPlaytime();
        playerData.addPlaytimeMinutes(newMinutes);
        
        // Save player data
        plugin.getDatabaseManager().savePlayerData(playerData);
        
        // Send success message
        plugin.getMessageUtils().sendMessage(sender, "§a✅ Tiempo jugado corregido exitosamente!");
        plugin.getMessageUtils().sendMessage(sender, "§7Jugador: §f" + targetName);
        plugin.getMessageUtils().sendMessage(sender, "§7Tiempo anterior: §f" + currentMinutes + " minutos");
        plugin.getMessageUtils().sendMessage(sender, "§7Nuevo tiempo: §f" + newMinutes + " minutos");
        
        // Show updated player info
        if (sender.hasPermission("vanguardrankups.admin")) {
            handlePlayerInfo(sender, new String[]{"info", targetName});
        }
    }
    
    /**
     * Handle syncing playtime for players using PlaceholderAPI
     */
    private void handleSyncPlaytime(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.getMessageUtils().sendMessage(sender, "§cUso: /rankupadmin syncplaytime <jugador|all>");
            plugin.getMessageUtils().sendMessage(sender, "§7Ejemplos:");
            plugin.getMessageUtils().sendMessage(sender, "§7  /rankupadmin syncplaytime Player123");
            plugin.getMessageUtils().sendMessage(sender, "§7  /rankupadmin syncplaytime all");
            return;
        }
        
        String target = args[1].toLowerCase();
        
        if (target.equals("all")) {
            // Sync all online players
            syncAllPlayersPlaytime(sender);
        } else {
            // Sync specific player
            syncPlayerPlaytime(sender, target);
        }
    }
    
    private void syncAllPlayersPlaytime(CommandSender sender) {
        plugin.getMessageUtils().sendMessage(sender, "§a🔄 Sincronizando tiempo de todos los jugadores online...");
        
        int syncedCount = 0;
        int totalPlayers = Bukkit.getOnlinePlayers().size();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (syncPlayerPlaytimeInternal(player.getName())) {
                syncedCount++;
            }
        }
        
        plugin.getMessageUtils().sendMessage(sender, "§a✅ Sincronización completada!");
        plugin.getMessageUtils().sendMessage(sender, "§7Jugadores sincronizados: §f" + syncedCount + "/" + totalPlayers);
    }
    
    private void syncPlayerPlaytime(CommandSender sender, String playerName) {
        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer == null) {
            plugin.getMessageUtils().sendMessage(sender, "§c❌ El jugador " + playerName + " no está online");
            return;
        }
        
        if (syncPlayerPlaytimeInternal(playerName)) {
            plugin.getMessageUtils().sendMessage(sender, "§a✅ Tiempo sincronizado para " + playerName);
        } else {
            plugin.getMessageUtils().sendMessage(sender, "§c❌ Error al sincronizar tiempo para " + playerName);
        }
    }
    
    private boolean syncPlayerPlaytimeInternal(String playerName) {
        try {
            Player targetPlayer = Bukkit.getPlayer(playerName);
            if (targetPlayer == null) {
                return false;
            }
            
            // Get player data
            PlayerData playerData = plugin.getRankupManager().getOrCreatePlayerData(
                targetPlayer.getUniqueId(), 
                playerName
            );
            
            // Get real playtime from Minecraft statistics (more reliable than PlaceholderAPI)
            int playTicks = targetPlayer.getStatistic(org.bukkit.Statistic.PLAY_ONE_MINUTE);
            
            // Convert ticks to minutes (20 ticks = 1 second, 1200 ticks = 1 minute)
            int realMinutes = playTicks / 1200;
            
            // Get current playtime for comparison
            long currentMinutes = playerData.getPlaytimeMinutes();
            
            // Set the real playtime
            playerData.setPlaytimeMinutes(realMinutes);
            
            // Save player data
            plugin.getDatabaseManager().savePlayerData(playerData);
            
            if (plugin.getConfigManager().isDebug()) {
                plugin.getLogger().info("Synced playtime for " + playerName + ": " + currentMinutes + " -> " + realMinutes + " minutes (from " + playTicks + " ticks)");
            }
            
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().warning("Error syncing playtime for " + playerName + ": " + e.getMessage());
            return false;
        }
    }
} 