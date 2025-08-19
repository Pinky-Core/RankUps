package com.lewisainsworth.vanguardrankups.commands;

import com.lewisainsworth.vanguardrankups.VanguardRankUps;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RankupTabCompleter implements TabCompleter {

	private final VanguardRankUps plugin;

	public RankupTabCompleter(VanguardRankUps plugin) {
		this.plugin = plugin;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> suggestions = new ArrayList<>();

		if (args.length == 1) {
			List<String> base = new ArrayList<>();
			base.add("info");
			base.add("progress");
			base.add("p");           // Alias para progress
			base.add("help");
			base.add("h");           // Alias para help
			base.add("confirm");
			
			// Comandos de admin que también están en /rankup (con verificación de permisos)
			if (sender.hasPermission("vanguardrankups.force")) {
				base.add("force");
			}
			if (sender.hasPermission("vanguardrankups.reload")) {
				base.add("reload");
			}
			
			StringUtil.copyPartialMatches(args[0], base, suggestions);
			Collections.sort(suggestions);
			return suggestions;
		}

		if (args.length == 2) {
			if ("force".equalsIgnoreCase(args[0]) && sender.hasPermission("vanguardrankups.force")) {
				List<String> players = new ArrayList<>();
				for (Player p : Bukkit.getOnlinePlayers()) {
					players.add(p.getName());
				}
				StringUtil.copyPartialMatches(args[1], players, suggestions);
				Collections.sort(suggestions);
				return suggestions;
			}
		}

		return suggestions;
	}
}

