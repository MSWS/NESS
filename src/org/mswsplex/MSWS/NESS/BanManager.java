package org.mswsplex.MSWS.NESS;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.mswsplex.punish.managers.TimeManager;
import org.mswsplex.punish.msws.Main;

public class BanManager {

	public static void banwave() {
		if (NESS.main.banwave.getConfigurationSection("queue") == null)
			NESS.main.banwave.createSection("queue");
		for (String res : NESS.main.banwave.getConfigurationSection("queue").getKeys(false)) {
			OfflinePlayer target = Bukkit.getOfflinePlayer(UUID.fromString(res));
			saveLogs(target, NESS.main.banwave.getString("queue." + res));
			for (String resres : NESS.main.config.getStringList("CommandsOnBanwave")) {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
						MSG.color(resres.replace("%player%", target.getName()).replace("%hack%",
								NESS.main.banwave.getString("Logs." + NESS.main.nessReason.get(target) + ".reason"))));
			}
			PlayerManager.resetVL(target, "all");
			NESS.main.banwave.set("queue." + target.getUniqueId(), null);
			ban(target);
		}

		NESS.main.saveBW();
	}

	public static void nessBan(Player player, String reason) {
		saveLogs(player, reason);
		NESS.main.saveBW();
		for (String res : NESS.main.config.getStringList("CommandsOnBan")) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
					MSG.color(res.replace("%player%", player.getName()).replace("%hack%",
							NESS.main.banwave.getString("Logs." + NESS.main.nessReason.get(player) + ".reason"))));
		}
		ban(player);
	}

	public static void ban(OfflinePlayer player) {
		if (NESS.main.nessReason.containsKey(player)) {
			if (player.isOnline())
				for (Player target : Bukkit.getOnlinePlayers())
					MSG.tell(target,
							NESS.main.config.getString("Messages.WasBanned").replace("%prefix%", NESS.main.prefix)
									.replace("%player%", player.getName()).replace("%hack%", NESS.main.banwave
											.getString("Logs." + NESS.main.nessReason.get(player) + ".reason")));
			if (!NESS.main.devMode) {
				if (NESS.main.config.getBoolean("Settings.HandleBans")) {
					AddPunish.addPunish(player.getName(), "NESS", NESS.main.nessReason.get(player), "hacking", 3);
				}
				Main punish = (Main) Bukkit.getServer().getPluginManager().getPlugin("Punish");
				if (punish != null)
					org.mswsplex.punish.managers.BanManager.addPunishment(player, "NESS",
							"Hacking (Token: " + NESS.main.nessReason.get(player) + ")", "sev3hackingban", 1.314e+10,
							2);
			}
			NESS.main.banwave.set("queue." + player.getUniqueId(), null);
		}
	}

	@SuppressWarnings("unchecked")
	public static void saveLogs(OfflinePlayer player, String reason) {
		String token = MSG.genUUID(10);

		NESS.main.nessReason.put(player, token);
		if (player.isOnline())
			NESS.main.banwave.set("Logs." + token + ".world", ((Player) player).getWorld().getName());
		NESS.main.banwave.set("Logs." + token + ".reason", reason);
		NESS.main.banwave.set("Logs." + token + ".vl", NESS.main.vl.getInt(player.getUniqueId() + "." + reason));
		NESS.main.banwave.set("Logs." + token + ".user", player.getName());
		NESS.main.banwave.set("Logs." + token + ".banner", "NESS");
		double time = System.currentTimeMillis() - ((long) PlayerManager.getInfo("logStart", player));
		PlayerManager.addLogMessage(player, "");
		PlayerManager.addLogMessage(player, "--- END OF LOGS---");
		PlayerManager.addLogMessage(player, "Time Elapsed: " + TimeManager.getTime(time));
		PlayerManager.addLogMessage(player, "Banning " + player.getName() + " for: " + reason + " (VL: "
				+ PlayerManager.getVl(player, reason) + ")");

		List<String> log = (List<String>) PlayerManager.getInfo("log", player);
		if (log != null && NESS.main.config.getBoolean("StoreLogs")) {
			File file = new File(NESS.main.getDataFolder(), "Logs/");
			try {
				file.mkdirs();
				Files.write(Paths.get(NESS.main.getDataFolder() + "/Logs/" + token + " (" + player.getName() + ").log"),
						log, StandardCharsets.UTF_8);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		NESS.main.banwave.set("queue." + player.getUniqueId(), null);
		PlayerManager.resetVL(player, "all");
		NESS.main.saveBW();
	}

	public static boolean isActive(OfflinePlayer player, String res) {
		String uuid = player.getUniqueId() + "";
		return (System
				.currentTimeMillis() < (long) (NESS.main.data.getLong("Users." + uuid + ".history." + res + ".date")
						+ NESS.main.data.getLong("Users." + uuid + ".history." + res + ".duration") * 60 * 1000));
	}
}
