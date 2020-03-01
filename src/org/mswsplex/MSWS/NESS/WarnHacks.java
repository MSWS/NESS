package org.mswsplex.MSWS.NESS;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class WarnHacks {
	static HashMap<Player, String> notify = new HashMap<>();

	@SuppressWarnings("deprecation")
	public static void warnHacks(Player hacker, String hack, int level, double maxPing) {
		NESS.main.legit.put(hacker, false);
		int currentLine = Thread.currentThread().getStackTrace()[2].getLineNumber();
		if (hacker.getGameMode() == GameMode.SPECTATOR)
			return;
		if (NESS.main.config.getStringList("DisabledWorlds").contains(hacker.getWorld().getName()))
			return;
		if (!NESS.main.config.getBoolean("Settings.Global"))
			return;
		if (hacker.isInsideVehicle())
			return;
		if (hacker.isDead())
			return;
		if (hacker.hasPermission("ness.bypass." + hack.replace(" ", "")))
			return;
		if (PlayerManager.timeSince("lastJoin", hacker) <= 3000)
			return;
		if (NESS.main.config.getBoolean("Settings.Cancel") && NESS.main.safeLoc.containsKey(hacker)
				&& Math.random() < NESS.main.config.getDouble("Configuration.LagPossibility") / 100)
			hacker.teleport(NESS.main.safeLoc.get(hacker));
		for (String res : NESS.main.config.getStringList("DisabledChecks")) {
			if ((currentLine + "").equals(res) || hack.equals(res))
				return;
		}
		if (NESS.main.config.getIntegerList("DisabledChecks").contains(currentLine))
			return;
		String fromClass = new Exception().getStackTrace()[1].getClassName(), className = "";
		for (int i = 0; i < fromClass.length(); i++) {
			className = fromClass.split("\\.")[fromClass.split("\\.").length - 1];
//			if (fromClass.substring(fromClass.length() - i, fromClass.length()).startsWith(".")) {
//				className = fromClass.substring(fromClass.length() - i + 1, fromClass.length());
//				break;
//			}
		}
		if (PlayerManager.getPing(hacker) < maxPing || maxPing == -1) {
			String add = hack + " (" + className + ": " + currentLine + ") VL: " + PlayerManager.getVl(hacker, hack)
					+ " Time: %time% X: " + parseDecimal(hacker.getLocation().getX(), 2) + " Y: "
					+ parseDecimal(hacker.getLocation().getY(), 2) + " Z: "
					+ parseDecimal(hacker.getLocation().getZ(), 2) + " Ping: " + PlayerManager.getPing(hacker);
			String add2 = "";
			if (hacker.getItemInHand() != null && (hacker.getItemInHand().getType() != Material.AIR)) {
				add = add + " Hand: " + MSG.camelCase(hacker.getItemInHand().getType().toString());
			}
			add = add + " ";
			for (String res : new String[] { "tooManyTicks", "clicks", "timerTick", "regenTicks", "placeTicks",
					"moveTicks", "oldFire", "shiftTicks", "wasFlight", "isHit", "packets" }) {
				if (PlayerManager.getAction(res, hacker) != 0 && PlayerManager.getAction(res, hacker) != -20) {
					add2 = add2 + res + ": " + PlayerManager.getAction(res, hacker) + " ";
				} else if (PlayerManager.getInfo(res, hacker) != null) {
					if (PlayerManager.getInfo(res, hacker) instanceof Double) {
						if (((double) PlayerManager.getInfo(res, hacker)) == -20.0)
							continue;
					}
					add2 = add2 + res + ": " + (System.currentTimeMillis() - (double) PlayerManager.getInfo(res, hacker))
							+ " ";
				}
			}
			if (!add2.isEmpty()) {
				PlayerManager.addLogMessage(hacker, add2.trim());
				PlayerManager.addLogMessage(hacker, "");
			}
			PlayerManager.addLogMessage(hacker, add.trim());

			if (NESS.main.devMode) {
				for (Player player : Bukkit.getOnlinePlayers()) {
					if (player.hasPermission("ness.notify.developer")) {
						MSG.tell(player, "&9Dev> &7" + hacker.getDisplayName() + ": &e" + currentLine + " &7("
								+ MSG.vlCol(PlayerManager.getVl(hacker, hack)) + hack + "&7) [&e" + className + "&7]");
					}
				}
			}
			if (PlayerManager.getVl(hacker, hack) <= NESS.main.config.getInt("Configuration.VLForBanwave")
					&& PlayerManager.getVl(hacker, hack) >= NESS.main.config.getInt("Configuration.MinimumVL")) {
				String oldCol = MSG.vlCol(PlayerManager.getVl(hacker, hack)),
						newCol = MSG.vlCol(PlayerManager.getVl(hacker, hack) + level), oldHack = notify.get(hacker),
						newHack = hack;
				if (!notify.containsKey(hacker)
						|| (notify.containsKey(hacker) && ((!oldCol.equals(newCol) && (oldHack.equals(newHack))
								|| PlayerManager.timeSince("hacks", hacker) >= 5000)))) {
					for (Player target : Bukkit.getOnlinePlayers()) {
						if (target.hasPermission("ness.notify.hacks")) {
							String msg = NESS.main.config.getString("Messages.WarnHacks")
									.replace("%player%", hacker.getDisplayName())
									.replace("%vlCol%", MSG.vlCol(PlayerManager.getVl(hacker, hack) + level))
									.replace("%hack%", hack).replace("%prefix%", NESS.main.prefix)
									.replace("%world%", hacker.getWorld().getName())
									.replace("%vl%", (PlayerManager.getVl(hacker, hack) + level) + "");
							MSG.tell(target, msg);
							ByteArrayDataOutput out = ByteStreams.newDataOutput();
							out.writeUTF("Forward");
							out.writeUTF("ALL");
							out.writeUTF("NESS");
							ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
							DataOutputStream msgout = new DataOutputStream(msgbytes);
							try {
								msgout.writeUTF(msg);
								msgout.writeShort(123);
							} catch (IOException e) {
								e.printStackTrace();
							}
							out.writeShort(msgbytes.toByteArray().length);
							out.write(msgbytes.toByteArray());
						}
					}
					PlayerManager.setAction("hacks", hacker, (double) System.currentTimeMillis());
					notify.put(hacker, hack);
				}
			}
			PlayerManager.addVL(hacker, hack, level);
			if (PlayerManager.getVl(hacker, hack) >= NESS.main.config.getInt("Configuration.VLForInstaban")) {
				if (NESS.main.devMode) {
					for (Player target : Bukkit.getOnlinePlayers()) {
						if (target.hasPermission("ness.notify.title"))
							target.sendTitle(MSG.color("&4&lINSTANT"),
									MSG.color("&e" + hacker.getDisplayName() + " &7for " + hack));
					}
					// PlayerManager.resetVL(hacker, hack);
					notify.put(hacker, MSG.vlCol(PlayerManager.getVl(hacker, hack)));
				}
				NESS.main.banwave.set("queue." + hacker.getUniqueId() + "", null);
				BanManager.nessBan(hacker, hack);
			} else if (PlayerManager.getVl(hacker, hack) >= NESS.main.config.getInt("Configuration.VLForBanwave")
					&& !NESS.main.banwave.contains("queue." + hacker.getUniqueId())) {
				for (Player target : Bukkit.getOnlinePlayers()) {
					if (NESS.main.devMode && target.hasPermission("ness.notify.title")) {
						target.sendTitle(MSG.color("&c&lQUEUE"),
								MSG.color("&e" + hacker.getDisplayName() + " &7for " + hack));
					}
					if (target.hasPermission("ness.notify.queue")) {
						MSG.tell(target, NESS.main.config.getString("Messages.IsQueued")
								.replace("%player%", hacker.getDisplayName()).replace("%prefix%", NESS.main.prefix));
					}
					notify.put(hacker, MSG.vlCol(PlayerManager.getVl(hacker, hack)));
				}
				NESS.main.banwave.set("queue." + hacker.getUniqueId() + "", hack);
				NESS.main.saveBW();
			}
		}
	}

	public static String parseDecimal(double d, int length) {
		String name = d + "";
		if (name.contains(".")) {
			if (name.split("\\.")[1].length() > 2) {
				name = name.split("\\.")[0] + "."
						+ name.split("\\.")[1].substring(0, Math.min(name.split("\\.")[1].length(), length));
			}
		}
		return name;
	}
}
