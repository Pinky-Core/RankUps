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
			base.add("requirements");
			base.add("req");        // Alias para requirements
			base.add("addrequirement");
			base.add("addreq");     // Alias para addrequirement
			base.add("fixplaytime");
			base.add("help");
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
			
			if (sub.equals("requirements") || sub.equals("req")) {
				List<String> rankOptions = new ArrayList<>();
				// Sugerir números 1..maxRank (los requisitos empiezan en rango 1)
				int max = plugin.getConfigManager().getMaxRank();
				for (int i = 1; i <= max; i++) {
					rankOptions.add(String.valueOf(i));
				}
				StringUtil.copyPartialMatches(args[1], rankOptions, suggestions);
				Collections.sort(suggestions);
				return suggestions;
			}
			
			if (sub.equals("addrequirement") || sub.equals("addreq")) {
				List<String> players = new ArrayList<>();
				for (Player p : Bukkit.getOnlinePlayers()) {
					players.add(p.getName());
				}
				StringUtil.copyPartialMatches(args[1], players, suggestions);
				Collections.sort(suggestions);
				return suggestions;
			}
			
			if (sub.equals("fixplaytime")) {
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
			
			if (sub.equals("requirements") || sub.equals("req")) {
				List<String> requirementTypes = new ArrayList<>();
				requirementTypes.add("mob_kills");
				requirementTypes.add("block_breaks");
				requirementTypes.add("playtime_minutes");
				requirementTypes.add("fishing");
				requirementTypes.add("farming");
				requirementTypes.add("quests");
				StringUtil.copyPartialMatches(args[2], requirementTypes, suggestions);
				Collections.sort(suggestions);
				return suggestions;
			}
			
			if (sub.equals("addrequirement") || sub.equals("addreq")) {
				List<String> requirementTypes = new ArrayList<>();
				requirementTypes.add("mob_kills");
				requirementTypes.add("block_breaks");
				requirementTypes.add("playtime_minutes");
				requirementTypes.add("fishing");
				requirementTypes.add("farming");
				requirementTypes.add("quests");
				StringUtil.copyPartialMatches(args[2], requirementTypes, suggestions);
				Collections.sort(suggestions);
				return suggestions;
			}
			
			if (sub.equals("fixplaytime")) {
				List<String> timeOptions = new ArrayList<>();
				timeOptions.add("30");      // 30 minutos
				timeOptions.add("60");      // 1 hora
				timeOptions.add("120");     // 2 horas
				timeOptions.add("240");     // 4 horas
				timeOptions.add("480");     // 8 horas
				timeOptions.add("720");     // 12 horas
				timeOptions.add("1440");    // 1 día
				StringUtil.copyPartialMatches(args[2], timeOptions, suggestions);
				Collections.sort(suggestions);
				return suggestions;
			}
		}
		
		if (args.length == 4) {
			String sub = args[0].toLowerCase();
			if (sub.equals("requirements") || sub.equals("req")) {
				String requirementType = args[2].toLowerCase();
				List<String> targets = new ArrayList<>();
				
				switch (requirementType) {
					case "mob_kills":
						targets.add("ZOMBIE");
						targets.add("SKELETON");
						targets.add("SPIDER");
						targets.add("CREEPER");
						targets.add("ENDERMAN");
						targets.add("BLAZE");
						targets.add("WITHER");
						break;
					case "block_breaks":
						targets.add("STONE");
						targets.add("IRON_ORE");
						targets.add("COAL_ORE");
						targets.add("DIAMOND_ORE");
						targets.add("EMERALD_ORE");
						targets.add("OAK_LOG");
						targets.add("DEEPSLATE_DIAMOND_ORE");
						break;
					case "fishing":
						targets.add("COD");
						targets.add("SALMON");
						targets.add("TROPICAL_FISH");
						targets.add("PUFFERFISH");
						break;
					case "farming":
						targets.add("WHEAT");
						targets.add("CARROT");
						targets.add("POTATO");
						targets.add("BEETROOT");
						break;
					case "quests":
						targets.add("total");
						break;
					case "playtime_minutes":
						targets.add("total");
						break;
				}
				
				StringUtil.copyPartialMatches(args[3], targets, suggestions);
				Collections.sort(suggestions);
				return suggestions;
			}
			
			if (sub.equals("addrequirement") || sub.equals("addreq")) {
				String requirementType = args[2].toLowerCase();
				List<String> targets = new ArrayList<>();
				
				switch (requirementType) {
					case "mob_kills":
						targets.add("ZOMBIE");
						targets.add("SKELETON");
						targets.add("SPIDER");
						targets.add("CREEPER");
						targets.add("ENDERMAN");
						targets.add("BLAZE");
						targets.add("WITHER");
						break;
					case "block_breaks":
						targets.add("STONE");
						targets.add("IRON_ORE");
						targets.add("COAL_ORE");
						targets.add("DIAMOND_ORE");
						targets.add("EMERALD_ORE");
						targets.add("OAK_LOG");
						targets.add("DEEPSLATE_DIAMOND_ORE");
						break;
					case "fishing":
						targets.add("COD");
						targets.add("SALMON");
						targets.add("TROPICAL_FISH");
						targets.add("PUFFERFISH");
						break;
					case "farming":
						targets.add("WHEAT");
						targets.add("CARROT");
						targets.add("POTATO");
						targets.add("BEETROOT");
						break;
					case "quests":
						targets.add("total");
						break;
					case "playtime_minutes":
						targets.add("total");
						break;
				}
				
				StringUtil.copyPartialMatches(args[3], targets, suggestions);
				Collections.sort(suggestions);
				return suggestions;
			}
		}

		if (args.length == 5) {
			String sub = args[0].toLowerCase();
			if (sub.equals("addrequirement") || sub.equals("addreq")) {
				List<String> amounts = new ArrayList<>();
				amounts.add("10");
				amounts.add("25");
				amounts.add("50");
				amounts.add("100");
				amounts.add("500");
				amounts.add("1000");
				StringUtil.copyPartialMatches(args[4], amounts, suggestions);
				Collections.sort(suggestions);
				return suggestions;
			}
		}

		return suggestions;
	}
}

