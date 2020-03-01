package org.mswsplex.MSWS.NESS;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class NESSCommand implements TabCompleter, CommandExecutor {
	String[] accepted = { "Flight", "Kill_Aura", "Scaffold", "Speed", "Timer", "Jesus" };

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> result = new ArrayList<String>();
		if (args.length == 1) {
			for (String tempRes : new String[] { "toggle", "ban", "vl", "banwave", "time", "clear", "unban", "version",
					"reload", "report" }) {
				if (sender.hasPermission("ness.command." + tempRes) && tempRes.startsWith(args[0].toLowerCase()))
					result.add(tempRes);
			}
		} else {
			if (args.length == 3) {
				if (args[0].equalsIgnoreCase("clear") && sender.hasPermission("ness.command.clear")) {
					for (String tempRes : new String[] { "All", "AntiFire", "AutoSneak", "Blink", "BunnyHop",
							"Fast Bow", "FastLadder", "Fast Sneak", "Flight", "Glide", "High CPS",
							"Illegal Interaction", "Illegal Movement", "Jesus", "Kill Aura", "NoClip", "NoFall",
							"NoWeb", "Phase", "Reach", "Regen", "Scaffold", "Spambot", "Speed", "Spider", "Step",
							"Timer" }) {
						if (tempRes.toLowerCase().startsWith(args[2].toLowerCase()))
							result.add(tempRes);
					}
				}
				if (args[0].equalsIgnoreCase("report") && sender.hasPermission("ness.command.report")) {
					for (String res : accepted) {
						if (res.toLowerCase().startsWith(args[2].toLowerCase()))
							result.add(res.replace("_", ""));
					}
				}
			}
			if (args.length == 2) {
				if (args[0].equalsIgnoreCase("toggle") && sender.hasPermission("ness.command.toggle")) {
					for (String tempRes : new String[] { "cancel", "dev", "global", "manual", "debug" }) {
						if (tempRes.toLowerCase().startsWith(args[1].toLowerCase()))
							result.add(tempRes);
					}
				}
				if (args[0].equalsIgnoreCase("unban") && sender.hasPermission("ness.command.unban")) {
					ConfigurationSection section = NESS.main.data.getConfigurationSection("Users");
					if (section != null) {
						for (String tempRes : section.getKeys(false)) {
							OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(tempRes));
							boolean add = false;
							for (String entry : section.getConfigurationSection(tempRes + ".history").getKeys(false)) {
								if (BanManager.isActive(player, entry)) {
									add = true;
								}
							}
							if (Bukkit.getOfflinePlayer(UUID.fromString(tempRes)).getName().toLowerCase()
									.startsWith(args[1].toLowerCase()) && add)
								result.add(Bukkit.getOfflinePlayer(UUID.fromString(tempRes)).getName());
						}
					}
				}
				if (args[0].equalsIgnoreCase("clear") && sender.hasPermission("ness.command.clear")) {
					for (String tempRes : new String[] { "All" }) {
						if (tempRes.toLowerCase().startsWith(args[1].toLowerCase()))
							result.add(tempRes);
					}
				}
			}
			if (result.isEmpty()) {
				for (Player target : Bukkit.getOnlinePlayers()) {
					if (target.getName().toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
						result.add(target.getName());
					}
				}
			}
		}
		return result;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		String prefix = NESS.main.prefix + " &7";
		OfflinePlayer target;
		Player player;
		switch (command.getName().toLowerCase()) {
		case "ness":
			if (args.length > 0) {
				switch (args[0].toLowerCase()) {
				case "vl":
					if (!(sender.hasPermission("ness.command.vl"))) {
						MSG.noPerm(sender);
						return true;
					}
					if (args.length == 1) {
						boolean did = false;
						for (String res : NESS.main.vl.getKeys(false)) {
							if (Bukkit.getPlayer(UUID.fromString(res)) != null) {
								String msg = "";
								for (String user : NESS.main.vl.getConfigurationSection(res).getKeys(false)) {
									if (user.matches("(category|accuracy)"))
										continue;
									msg = msg + ("&6" + user + " " + MSG.vlCol(NESS.main.vl.getInt(res + "." + user))
											+ NESS.main.vl.getInt(res + "." + user)) + " ";
								}
								if (msg != "") {
									sender.sendMessage(MSG.color(prefix + "&e"
											+ Bukkit.getOfflinePlayer(UUID.fromString(res)).getName() + " &7" + msg));
									did = true;
								}
							}
						}
						if (!did) {
							MSG.tell(sender, NESS.main.prefix + " There are no violations.");
						}
					} else if (args.length == 2) {
						target = PlayerManager.getPlayer(sender, args[1]);
						if (target != null) {
							if (NESS.main.vl.contains(target.getUniqueId() + "")) {
								String msg = "";
								String res = target.getUniqueId() + "";
								for (String user : NESS.main.vl.getConfigurationSection(res).getKeys(false)) {
									if (user.matches("(category|accuracy)"))
										continue;
									msg = msg + ("&6" + user + " " + MSG.vlCol(NESS.main.vl.getInt(res + "." + user))
											+ NESS.main.vl.getInt(res + "." + user)) + " ";
								}
								sender.sendMessage(MSG.color(prefix + "&e"
										+ Bukkit.getOfflinePlayer(UUID.fromString(res)).getName() + " &7" + msg));
							} else {
								sender.sendMessage(MSG.color(prefix + "That player has no violations!"));
							}
						}
					} else {
						sender.sendMessage(MSG.color(prefix + "/ness vl <player>"));
					}
					break;
				case "ping":
					if (sender instanceof Player)
						MSG.tell(sender, PlayerManager.getPing(((Player) sender)) + "");
					break;
				case "report":
					if (!(sender.hasPermission("ness.command.report"))) {
						MSG.noPerm(sender);
						return true;
					}
					if (!(sender instanceof Player)) {
						return true;
					}
					player = (Player) sender;
					if (args.length < 3) {
						MSG.tell(sender, MSG.color(prefix + "/ness report [Player] [Hack]"));
						return true;
					}

					target = Bukkit.getPlayer(args[1]);
					if (target == null) {
						MSG.tell(sender, prefix + "Unknown Player.");
						return true;
					}
					boolean accept = false;
					String acceptMessage = "", hack = "";
					for (String res : accepted) {
						if (res.replace("_", "").equalsIgnoreCase(args[2])) {
							hack = res.replace("_", " ");
							accept = true;
						}
						acceptMessage = acceptMessage + "&c" + res.replace("_", "") + "&7, ";
					}

					acceptMessage = acceptMessage.substring(0, Math.max(acceptMessage.length() - 4, 0));

					if (!accept) {
						MSG.tell(sender, prefix + "I don't know that one, sorry! Accepted hacks are: " + acceptMessage);
						return true;
					}

					if (PlayerManager.getInfo("lastReport", player) != null
							&& PlayerManager.timeSince("lastReport", player) <= 3.6e+6
							&& !sender.hasPermission("ness.bypass.reportcooldown")) {
						MSG.tell(sender, prefix + "Your last report was too recent!");
						return true;
					}

					if (PlayerManager.getInfo("lastReported", target) != null
							&& PlayerManager.timeSince("lastReported", player) <= 300000
							&& !sender.hasPermission("ness.bypass.reportcooldown")) {
						MSG.tell(sender, prefix + "That player has already been reported recently.");
						return true;
					}
					MSG.tell(sender,
							prefix + "Successfully submitted a report, thanks for helping keep the server clean!");
					for (Player p : Bukkit.getOnlinePlayers()) {
						if (!p.hasPermission("ness.notify.report"))
							continue;
						MSG.tell(p, prefix + "&a" + sender.getName() + " &7reported &e" + target.getName() + "&7 for &c"
								+ hack + "&7.");
					}
					int amo = PlayerManager.getVl(target, hack);
					if (amo < 20) {
						WarnHacks.warnHacks((Player) target, hack, 20, -1);
					} else if (amo < 40) {
						WarnHacks.warnHacks((Player) target, hack, 50, -1);
					} else if (amo < 100) {
						WarnHacks.warnHacks((Player) target, hack, 100, -1);
					} else {
						WarnHacks.warnHacks((Player) target, hack, 200, -1);
					}
					PlayerManager.setInfo("lastReport", player, System.currentTimeMillis());
					PlayerManager.setInfo("lastReported", target, System.currentTimeMillis());
					PlayerManager.addLogMessage(target, "REPORTED BY " + sender.getName() + " for " + hack);
					break;
				case "version":
				case "ver":
					if (!(sender.hasPermission("ness.command.version"))) {
						MSG.noPerm(sender);
						return true;
					}
					MSG.tell(sender, NESS.main.prefix + " Current version is &e" + NESS.main.ver);
					String ver = new Updater().getSpigotVersion();
					if (ver != null) {
						MSG.tell(sender, NESS.main.prefix + " Latest version is &e" + ver);
					} else {
						MSG.tell(sender, NESS.main.prefix + " Unable to get lastest version.");
					}
					break;
				case "banwave":
					if (!(sender.hasPermission("ness.command.banwave"))) {
						MSG.noPerm(sender);
						return true;
					}
					BanManager.banwave();
					sender.sendMessage(MSG.color(prefix + "Succesfully initiated a banwave."));
					break;
				case "unban":
					if (!(sender.hasPermission("ness.command.unban"))) {
						MSG.noPerm(sender);
						return true;
					}
					if (args.length > 1) {
						UUID uuid = Bukkit.getOfflinePlayer(args[1]).getUniqueId();
						if (NESS.main.data.contains("Users." + uuid)) {
							ConfigurationSection section = NESS.main.data
									.getConfigurationSection("Users." + uuid + ".history");
							if (section != null) {
								for (String res : section.getKeys(false)) {
									section.set(res + ".duration", 0);
								}
								NESS.main.saveData();
								MSG.tell(sender, NESS.main.prefix + " &e" + Bukkit.getOfflinePlayer(args[1]).getName()
										+ " &7is now unbanned.");
							} else {
								MSG.tell(sender, NESS.main.prefix + " &7Unknown error.");
							}
						} else {
							MSG.tell(sender, NESS.main.prefix + " &7That user has no history.");
						}
					} else {
						MSG.tell(sender, NESS.main.prefix + " &7/ness unban [user]");
					}
					break;
				case "time":
					if (!(sender.hasPermission("ness.command.time"))) {
						MSG.noPerm(sender);
						return true;
					}
					long time = (long) (NESS.main.config.getInt("Configuration.BanWaves")
							- (NESS.main.seconds % NESS.main.config.getInt("Configuration.BanWaves")));
					sender.sendMessage(
							MSG.color(prefix + "Banwave will initiate in &e" + TimeManagement.getTime(time) + "&7!"));
					break;
				case "reload":
					if (!(sender.hasPermission("ness.command.reload"))) {
						MSG.noPerm(sender);
						return true;
					}
					NESS.main.refresh();
					MSG.tell(sender, NESS.main.prefix + " NESS succesfully reloaded.");
					break;
				case "reset":
					if (!(sender.hasPermission("ness.command.reset"))) {
						MSG.noPerm(sender);
						return true;
					}
					NESS.main.saveResource("config.yml", true);
					NESS.main.refresh();
					MSG.tell(sender, NESS.main.prefix + " NESS succesfully reset.");
					break;
				case "warn":
					if (!(sender.hasPermission("ness.command.warn"))) {
						MSG.noPerm(sender);
						return true;
					}
					if (args.length >= 4) {
						hack = "";
						String vl = "", active = "h";
						for (String res : args) {
							if (!res.equals(args[0]) && !res.equals(args[1])) {
								if (res.startsWith("v:")) {
									active = "v";
								} else if (res.startsWith("h:")) {
									active = "h";
								}
								if (active.equals("v")) {
									if (res.startsWith("v:")) {
										vl = vl + res.substring(2);
									} else {
										vl = vl + res;
									}
								} else if (active.equals("h")) {
									if (res.startsWith("h:")) {
										hack = hack + res.substring(2) + " ";
									} else {
										hack = hack + res + " ";
									}
								}
							}
						}
						player = PlayerManager.getPlayer(sender, args[1]);
						if (player != null)
							WarnHacks.warnHacks(player, hack.trim(), Integer.valueOf(vl.trim()), -1);
					} else {
						MSG.tell(sender, "/ness warn [player] h:[hacks] v:[vl]");
					}
					break;
				case "loadprotocol":
					if (!(sender.hasPermission("ness.command.loadprotocol"))) {
						MSG.noPerm(sender);
						return true;
					}
					try {
						new Protocols();
						MSG.tell(sender, NESS.main.prefix + " Succesfully enabled ProtocolLib checks.");
					} catch (Exception e) {
						MSG.tell(sender, NESS.main.prefix + " Error enabling ProtocolLib checks.");
					}
					break;
				case "ban":
					if (!(sender.hasPermission("ness.command.ban"))) {
						MSG.noPerm(sender);
						return true;
					}
					if (args.length > 2) {
						if (NESS.main.config.getBoolean("Settings.ManualBan")) {
							player = PlayerManager.getPlayer(sender, args[1]);
							if (player.hasPermission("ness.ban.bypass")) {
								MSG.noPerm(sender);
								return true;
							}
							if (player != null) {
								String reason = "";
								for (String res : args)
									if (res != args[0] && res != args[1])
										reason = reason + res + " ";
								BanManager.nessBan(player, reason.trim());
								sender.sendMessage(MSG.color(prefix + "Banning " + player.getName() + "."));
							}
						} else {
							sender.sendMessage(MSG.color(prefix + "Manual bans are not enabled."));
						}
					} else {
						sender.sendMessage(MSG.color(prefix + "/ness ban [player] <reason>"));
					}
					break;
				case "stats":
					MSG.tell(sender, "&c&lBuild Version:");
					MSG.tell(sender, "  &6Date &r2018-04-11");
					MSG.tell(sender, "  &6Git &rMSWS");
					MSG.tell(sender,
							"  &6Protocol " + MSG.TorF(Bukkit.getPluginManager().getPlugin("ProtocolLib") != null));
					MSG.tell(sender, "  &6Version &r" + NESS.main.ver);
					break;
				case "clear":
					if (!(sender.hasPermission("ness.command.clear"))) {
						MSG.noPerm(sender);
						return true;
					}
					if (args.length >= 3) {
						String name = "";
						for (String res : args)
							if (!res.matches("(?i)(clear|" + args[1] + ")"))
								name = name + res + " ";
						name = name.trim();
						if (args[2].equalsIgnoreCase(args[1]))
							name = args[1];
						if (args[1].equalsIgnoreCase("all")) {
							for (String res : NESS.main.vl.getKeys(false)) {
								OfflinePlayer offtarget = Bukkit.getOfflinePlayer(UUID.fromString(res));
								if (offtarget != null)
									PlayerManager.resetVL(offtarget, MSG.camelCase(name));
							}
							sender.sendMessage(MSG.color(prefix + "Succesfully cleared &eeveryone&7 of &6"
									+ MSG.camelCase(name) + " &7violations."));
							return true;
						}
						target = PlayerManager.getPlayer(sender, args[1]);
						if (target != null) {
							PlayerManager.resetVL(target, MSG.camelCase(name));
							sender.sendMessage(MSG.color(prefix + "Succesfully cleared &e" + target.getName()
									+ "&7 of &6" + MSG.camelCase(name) + " &7violations."));
						}
					} else {
						sender.sendMessage(MSG.color(prefix + "/ness clear [player] [violation|all]"));
					}
					break;
				case "toggle":
					if (!(sender.hasPermission("ness.command.toggle"))) {
						MSG.noPerm(sender);
						return true;
					}
					if (args.length == 2) {
						String name = "", id = "";
						switch (args[1].toLowerCase()) {
						case "cancel":
							name = "Lag Back";
							id = "Cancel";
							break;
						case "dev":
							name = "Developer Mode";
							id = "DeveloperMode";
							break;
						case "everywhere":
						case "global":
							name = "Global";
							id = "Global";
							break;
						case "manual":
						case "ban":
							name = "Manual Bans";
							id = "ManualBan";
							break;
						case "debug":
							name = "Debug Mode";
							id = "DebugMode";
							break;
						default:
							return true;
						}
						NESS.main.config.set("Settings." + id, !NESS.main.config.getBoolean("Settings." + id));
						MSG.tell(sender,
								prefix + name + ": " + MSG.TorF(NESS.main.config.getBoolean("Settings." + id)));
					} else {
						sender.sendMessage(MSG.color(prefix + "/ness toggle [cancel|dev|lobby|debug]"));
					}
					NESS.main.devMode = NESS.main.config.getBoolean("Settings.DeveloperMode");
					NESS.main.debugMode = NESS.main.config.getBoolean("Settings.DebugMode");
					NESS.main.saveConfig();
					break;
				case "lookup":
					if (!(sender.hasPermission("ness.command.lookup"))) {
						MSG.noPerm(sender);
						return true;
					}
					if (args.length <= 1) {
						MSG.tell(sender, "/ness lookup [ID]");
						return true;
					}
					if (!NESS.main.banwave.contains("Logs." + args[1])) {
						MSG.tell(sender, "Unknown ID");
						return true;
					}
					MSG.tell(sender, "&7User: &e" + NESS.main.banwave.getString("Logs." + args[1] + ".user"));
					MSG.tell(sender, "&7World: &e" + NESS.main.banwave.getString("Logs." + args[1] + ".world"));
					MSG.tell(sender, "&7VL: &e" + NESS.main.banwave.getString("Logs." + args[1] + ".vl"));
					MSG.tell(sender, "&7Hack: &e" + NESS.main.banwave.getString("Logs." + args[1] + ".reason"));
					break;
				}
			} else {
				sendHelp(sender);
			}
			break;
		default:
			return false;
		}
		return true;
	}

	public void sendHelp(CommandSender sender) {
		if (sender.hasPermission("ness.command.version"))
			MSG.tell(sender, "/ness version");
		if (sender.hasPermission("ness.command.report"))
			MSG.tell(sender, "/ness report [Player] [Hack]");
		if (sender.hasPermission("ness.command.vl"))
			MSG.tell(sender, "/ness vl <player>");
		if (sender.hasPermission("ness.command.banwave"))
			MSG.tell(sender, "/ness banwave");
		if (sender.hasPermission("ness.command.time"))
			if (sender.hasPermission("ness.command.ban"))
				MSG.tell(sender, "/ness ban [player]");
		if (sender.hasPermission("ness.command.clear"))
			MSG.tell(sender, "/ness clear [player] [violation|all]");
		if (sender.hasPermission("ness.command.unban"))
			MSG.tell(sender, "/ness unban [player]");
		if (sender.hasPermission("ness.command.reload"))
			MSG.tell(sender, "/ness reload");
		if (sender.hasPermission("ness.command.toggle"))
			MSG.tell(sender, "/ness toggle <feature>");
		MSG.tell(sender, NESS.main.prefix + " &7NESS &e" + NESS.main.ver + "&7 coded by &4&lMSWS&7.");
	}
}
