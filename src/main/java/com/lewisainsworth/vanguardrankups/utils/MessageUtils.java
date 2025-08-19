package com.lewisainsworth.vanguardrankups.utils;

import com.lewisainsworth.vanguardrankups.VanguardRankUps;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageUtils {
    
    private final VanguardRankUps plugin;
    
    public MessageUtils(VanguardRankUps plugin) {
        this.plugin = plugin;
    }
    
    public void sendMessage(CommandSender sender, String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        
        String prefix = plugin.getConfigManager().getPrefix();
        String formattedMessage = ChatColor.translateAlternateColorCodes('&', prefix + message);
        sender.sendMessage(formattedMessage);
    }
    
    public void sendMessage(Player player, String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        
        String prefix = plugin.getConfigManager().getPrefix();
        String formattedMessage = ChatColor.translateAlternateColorCodes('&', prefix + message);
        player.sendMessage(formattedMessage);
    }
    
    // Método estático para casos especiales donde no se quiere prefijo
    public static void sendMessageNoPrefix(CommandSender sender, String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        
        String formattedMessage = ChatColor.translateAlternateColorCodes('&', message);
        sender.sendMessage(formattedMessage);
    }
    
    public static void sendMessageNoPrefix(Player player, String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        
        String formattedMessage = ChatColor.translateAlternateColorCodes('&', message);
        player.sendMessage(formattedMessage);
    }
    
    // Métodos legacy para mantener compatibilidad
    public void sendPrefixedMessage(CommandSender sender, String message) {
        sendMessage(sender, message);
    }
    
    public void sendPrefixedMessage(Player player, String message) {
        sendMessage(player, message);
    }
    
    public static String formatProgressBar(int current, int required, int length) {
        if (required <= 0) {
            return "";
        }
        
        double percentage = Math.min(1.0, (double) current / required);
        int filledLength = (int) (percentage * length);
        int emptyLength = length - filledLength;
        
        StringBuilder bar = new StringBuilder();
        bar.append(ChatColor.GREEN);
        for (int i = 0; i < filledLength; i++) {
            bar.append("|");
        }
        bar.append(ChatColor.GRAY);
        for (int i = 0; i < emptyLength; i++) {
            bar.append("|");
        }
        
        return bar.toString();
    }
    
    public static String formatTime(long minutes) {
        if (minutes < 60) {
            return minutes + "m";
        } else if (minutes < 1440) { // 24 hours
            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;
            return hours + "h " + remainingMinutes + "m";
        } else {
            long days = minutes / 1440;
            long remainingHours = (minutes % 1440) / 60;
            return days + "d " + remainingHours + "h";
        }
    }
    
    public static String formatNumber(int number) {
        if (number < 1000) {
            return String.valueOf(number);
        } else if (number < 1000000) {
            return String.format("%.1fk", number / 1000.0);
        } else {
            return String.format("%.1fM", number / 1000000.0);
        }
    }
} 