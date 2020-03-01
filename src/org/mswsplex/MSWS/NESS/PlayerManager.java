package org.mswsplex.MSWS.NESS;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerManager {
	public static boolean groundAround(Location loc) {
		int radius = 2;
		for (int x = -radius; x < radius; x++) {
			for (int y = -radius; y < radius; y++) {
				for (int z = -radius; z < radius; z++) {
					Material mat = loc.getWorld().getBlockAt(loc.add(x, y, z)).getType();
					if (mat.isSolid() || mat == Material.WATER || mat == Material.STATIONARY_WATER
							|| mat == Material.LAVA || mat == Material.STATIONARY_LAVA) {
						loc.subtract(x, y, z);
						return true;
					}
					loc.subtract(x, y, z);
				}
			}
		}
		return false;
	}

	public static boolean redstoneAround(Location loc) {
		int radius = 2;
		for (int x = -radius; x < radius; x++) {
			for (int y = -radius; y < radius; y++) {
				for (int z = -radius; z < radius; z++) {
					Material mat = loc.getWorld().getBlockAt(loc.add(x, y, z)).getType();
					if (mat.toString().toLowerCase().contains("piston")) {
						loc.subtract(x, y, z);
						return true;
					}
					loc.subtract(x, y, z);
				}
			}
		}
		return false;
	}

	public static void resetVL(OfflinePlayer player, String hack) {
		if (hack.equalsIgnoreCase("all")) {
			NESS.main.vl.set(player.getUniqueId() + "", null);
		} else {
			NESS.main.vl.set(player.getUniqueId() + "." + hack, null);
		}
	}

	public static void addVL(Player player, String check, int vl) {
		String uuid = player.getUniqueId() + "";
		NESS.main.vl.set(uuid + "." + check, NESS.main.vl.getInt(uuid + "." + check) + vl);
		if (NESS.main.vl.getInt(uuid + "." + check) <= 0)
			NESS.main.vl.set(uuid + "." + check, null);
		if (NESS.main.vl.getConfigurationSection(uuid).getKeys(false).size() == 0)
			NESS.main.vl.set(uuid, null);
		NESS.main.saveVl();
	}

	public static Integer getVl(OfflinePlayer player, String check) {
		return NESS.main.vl.getInt(player.getUniqueId() + "." + check);
	}

	public static int getPing(Player player) {
		int ping = 999;
		try {
			Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
			ping = (int) entityPlayer.getClass().getField("ping").get(entityPlayer);
		} catch (Exception e) {
		}
		return ping;
	}

	public static Integer distToBlock(Location loc) {
		Location res = loc;
		int yDif = -1;
		while (!res.subtract(0, yDif, 0).getBlock().getType().isSolid() && res.subtract(0, yDif, 0).getY() > 0) {
			res.add(0, yDif, 0);
			yDif++;
		}
		return yDif;
	}

	public static void addAction(String category, Player player) {
		NESS.main.vl.set(player.getUniqueId() + ".category." + category, getAction(category, player) + 1);
	}

	public static void setAction(String category, Player player, Double amo) {
		NESS.main.vl.set(player.getUniqueId() + ".category." + category, amo);
	}

	public static void setInfo(String category, OfflinePlayer player, Object obj) {
		NESS.main.vl.set(player.getUniqueId() + ".category." + category, obj);
	}

	public static Object getInfo(String category, OfflinePlayer player) {
		return NESS.main.vl.get(player.getUniqueId() + ".category." + category);
	}

	public static Double getAction(String category, Player player) {
		if (NESS.main.vl.contains(player.getUniqueId() + ".category." + category)) {
			return NESS.main.vl.getDouble(player.getUniqueId() + ".category." + category);
		} else {
			return 0.0;
		}
	}

	public static void removeAction(String category, Player player) {
		NESS.main.vl.set(player.getUniqueId() + ".category." + category, null);
	}

	public static Double timeSince(String category, Player player) {
		return (System.currentTimeMillis() - getAction(category, player));
	}

	public static Player getPlayer(CommandSender sender, String name) {
		List<Player> list = new ArrayList<Player>();
		for (Player target : Bukkit.getOnlinePlayers()) {
			if (target.getName().toLowerCase().contains(name.toLowerCase())) {
				list.add(target);
			}
		}
		if (list.size() != 1) {
			MSG.tell(sender,
					NESS.main.prefix + " &e" + list.size() + " &7players with \"&e" + name + "&7\" in their username.");
			String msg = "";
			for (int i = 0; i < list.size(); i++) {
				msg = msg + "&e" + list.get(i) + "&7, ";
			}
			if (list.size() > 0)
				MSG.tell(sender, NESS.main.prefix + "Results: [" + msg + "&7].");
			return null;
		}
		return list.get(0);
	}

	@SuppressWarnings("unchecked")
	public static void addLogMessage(OfflinePlayer player, String msg) {
		List<String> log;
		if (PlayerManager.getInfo("log", player) == null) {
			log = new ArrayList<String>();
			log.add("BEGIN LOG User: " + player.getName() + " TIME:" + System.currentTimeMillis());
			PlayerManager.setInfo("logStart", player, System.currentTimeMillis());
		} else {
			log = (ArrayList<String>) PlayerManager.getInfo("log", player);
		}
		double time = System.currentTimeMillis() - ((long) PlayerManager.getInfo("logStart", player));
		log.add(msg.replace("%time%", time + ""));
		PlayerManager.setInfo("log", player, log);
	}
}
