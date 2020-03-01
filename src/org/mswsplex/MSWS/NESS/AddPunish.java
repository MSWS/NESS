package org.mswsplex.MSWS.NESS;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class AddPunish {
	@SuppressWarnings("deprecation")
	public static void addPunish(String punished, String punisher, String reason, String type, Integer severity) {
		OfflinePlayer target = Bukkit.getOfflinePlayer(punished);
		String uuid = target.getUniqueId() + "";
		ConfigurationSection user = NESS.main.data.getConfigurationSection("Users." + uuid + ".history");
		if (user == null) {
			NESS.main.data.createSection("Users." + uuid + ".history");
			user = NESS.main.data.getConfigurationSection("Users." + uuid + ".history");
		}
		int pos = 0;
		if (NESS.main.data.contains("Users." + uuid + ".history")) {
			while (NESS.main.data.contains("Users." + uuid + ".history." + pos))
				pos++;
		}
		Integer pastClient1 = 0, pastClient2 = 0, pastClient3 = 0;
		if (NESS.main.data.getConfigurationSection("Users." + uuid + ".history") != null) {
			ConfigurationSection history = NESS.main.data.getConfigurationSection("Users." + uuid + ".history");
			List<String> his = new ArrayList<String>();
			for (String res : history.getKeys(false))
				his.add(res);
			for (int i = his.size() - 1; i >= 0; i--) {
				String res = his.get(i);
				if (history.contains(res + ".unbanner"))
					continue;
				String tempType = history.getString(res + ".type");
				Integer sev = history.getInt(res + ".severity");
				switch (tempType) {
				case "hacking":
					switch (sev) {
					case 1:
						pastClient1++;
						break;
					case 2:
						pastClient2++;
						break;
					case 3:
						pastClient3++;
						break;
					}
					break;
				}
			}
		}
		user.set(pos + ".type", type);
		user.set(pos + ".severity", severity);
		user.set(pos + ".reason", reason);
		user.set(pos + ".user", punisher);
		user.set(pos + ".date", System.currentTimeMillis());
		int dur = 0;
		switch (type) {
		case "hacking":
			dur = (1 + pastClient3) * 24 * 30 * 60;
			if (target.isOnline()) {
				String msg = "";
				for (String res : NESS.main.config.getStringList("BanMessage")) {
					msg = msg + res + "\n";
				}
				Bukkit.getLogger().log(Level.SEVERE, dur + "");
				((Player) target)
						.kickPlayer(
								MSG.color(msg
										.replace("%hack%",
												NESS.main.banwave.getString(
														"Logs." + NESS.main.nessReason.get(target) + ".reason"))
										.replace("%duration%", TimeManagement.getTime((int) dur))
										.replace("%token%", reason)));
			}
			break;
		}
		user.set(pos + ".duration", dur);
		NESS.main.saveData();
	}
}
