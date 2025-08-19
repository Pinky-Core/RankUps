package com.lewisainsworth.vanguardrankups.commands;

import com.lewisainsworth.vanguardrankups.VanguardRankUps;
import com.lewisainsworth.vanguardrankups.models.Rank;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RankupAdminTabCompleter implements TabCompleter {

	private final VanguardRankUps plugin;

	public RankupAdminTabCompleter(VanguardRankUps plugin) {
		this.plugin = plugin;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> suggestions = new ArrayList<>();

		if (!sender.hasPermission("vanguardrankups.admin")) {
			return suggestions;
		}

		if (args.length == 1) {
			List<String> base = new ArrayList<>();
			base.add("reload");
			base.add("force");
			base.add("reset");
			base.add("setrank");
			base.add("info");
			StringUtil.copyPartialMatches(args[0], base, suggestions);
			Collections.sort(suggestions);
			return suggestions;
		}

		if (args.length == 2) {
			String sub = args[0].toLowerCase();
			if (sub.equals("force") || sub.equals("reset") || sub.equals("setrank") || sub.equals("info")) {
				List<String> players = new ArrayList<>();
				for (Player p : Bukkit.getOnlinePlayers()) {
					players.add(p.getName());
				}
				StringUtil.copyPartialMatches(args[1], players, suggestions);
				Collections.sort(suggestions);
				return suggestions;
			}
		}

		if (args.length == 3) {
			String sub = args[0].toLowerCase();
			if (sub.equals("setrank")) {
				List<String> rankOptions = new ArrayList<>();
				// Sugerir números 0..maxRank y grupos LP
				int max = plugin.getConfigManager().getMaxRank();
				for (int i = 0; i <= max; i++) {
					rankOptions.add(String.valueOf(i));
				}
				for (Map.Entry<Integer, Rank> e : plugin.getConfigManager().getRanks().entrySet()) {
					Rank r = e.getValue();
					if (r.getLuckPermsGroup() != null) {
						rankOptions.add(r.getLuckPermsGroup());
					}
				}
				StringUtil.copyPartialMatches(args[2], rankOptions, suggestions);
				Collections.sort(suggestions);
				return suggestions;
			}
		}

		return suggestions;
	}
}

