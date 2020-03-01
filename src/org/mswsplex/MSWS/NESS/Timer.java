package org.mswsplex.MSWS.NESS;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Timer {
	static HashMap<Player, Location> timerLoc = new HashMap<>();

	public void register() {
		new BukkitRunnable() {
			public void run() {
				for (Player player : Bukkit.getOnlinePlayers()) {
					if (NESS.main.vl.contains(player.getUniqueId() + "")) {
						for (String res : NESS.main.vl.getConfigurationSection(player.getUniqueId() + "")
								.getKeys(false)) {
							if (!res.matches("(category|accuracy)"))
								PlayerManager.addVL(player, res, -5);
						}
					}
					if (PlayerManager.getAction("bowShots", player) > 25) {
						if (NESS.main.devMode)
							MSG.tell(player,
									"&9Dev> &7Fast Bow Amount: " + PlayerManager.getAction("bowShots", player));
						WarnHacks.warnHacks(player, "Fast Bow",
								(int) (50 * (PlayerManager.getAction("bowShots", player) - 25)), -1);
					}
					PlayerManager.removeAction("bowShots", player);
					PlayerManager.removeAction("blocks", player);
				}
			}
		}.runTaskTimer(NESS.main, 0, 100);
		new BukkitRunnable() {
			public void run() {
				for (Player player : Bukkit.getOnlinePlayers()) {
					NESS.main.vl.set(player.getUniqueId() + ".accuracy", null);
				}
			}
		}.runTaskTimer(NESS.main, 0, 200);
		if (NESS.main.config.getBoolean("AutoAnnouncer.Enabled")) {
			new BukkitRunnable() {
				public void run() {
					for (Player player : Bukkit.getOnlinePlayers()) {
						MSG.tell(player, NESS.main.config.getString("AutoAnnouncer.Message"));
					}
				}
			}.runTaskTimer(NESS.main, 0, NESS.main.config.getInt("AutoAnnouncer.Delay") * 20);
		}
		new BukkitRunnable() {
			@Override
			public void run() {
				boolean groundAround = false;
				for (Player player : Bukkit.getOnlinePlayers()) {
					groundAround = PlayerManager.groundAround(player.getLocation());
					if (PlayerManager.getAction("shiftTicks", player) > 15)
						WarnHacks.warnHacks(player, "AutoSneak", 100, -1);
					if (PlayerManager.getAction("packets", player) > 10
							|| PlayerManager.getAction("timerTick", player) > 400) {
						player.kickPlayer((NESS.main.config.contains("PacketMessage"))
								? NESS.main.config.getString("PacketMessage")
								: "You are sending too many packets! [NESS]");
						PlayerManager.removeAction("packets", player);
						PlayerManager.removeAction("timerTick", player);
					}
					if (NESS.main.oldLoc.containsKey(player)) {
						if (!groundAround) {
							Material bottom = null;
							Integer dTG = 0;
							boolean web = false, cactus = false;
							for (int x = -2; x <= 2; x++) {
								for (int y = -2; y <= 3; y++) {
									for (int z = -2; z <= 2; z++) {
										Material belowSel = player.getWorld()
												.getBlockAt(player.getLocation().add(x, y, z)).getType();
										if (belowSel == Material.WEB) {
											web = true;
										}
										if (belowSel == Material.CACTUS)
											cactus = true;
									}
								}
							}
							while (!player.getLocation().getWorld().getBlockAt(player.getLocation().subtract(0, dTG, 0))
									.getType().isSolid() && dTG < 20) {
								dTG++;
							}
							bottom = player.getLocation().getWorld()
									.getBlockAt(player.getLocation().subtract(0, dTG, 0)).getType();
							if (NESS.main.oldLoc.get(player).equals(player.getLocation()) && !player.isFlying()
									&& PlayerManager.timeSince("wasFlight", player) >= 3000) {
								if (!NESS.main.oldLoc.get(player).equals(player.getLocation()))
									WarnHacks.warnHacks(player, "Flight", 5, 50);
							}
							if ((Math.abs(NESS.main.oldLoc.get(player).getX()) - Math.abs(player.getLocation().getX()))
									+ (Math.abs(NESS.main.oldLoc.get(player).getZ())
											- Math.abs(player.getLocation().getZ())) == 0) {
								double vert = Math.abs(NESS.main.oldLoc.get(player).getY())
										- Math.abs(player.getLocation().getY());
								if (NESS.main.oldLoc.get(player).getY() < player.getLocation().getY()) {
									if (!web && vert < 1 && !player.hasPotionEffect(PotionEffectType.JUMP)
											&& !player.isFlying()
											&& PlayerManager.timeSince("wasFlight", player) >= 3000
											&& PlayerManager.getAction("placeTicks", player) == 0
											&& bottom != Material.SLIME_BLOCK && !cactus
											&& PlayerManager.timeSince("isHit", player) >= 500)
										WarnHacks.warnHacks(player, "Flight", 5, -1);
								}
							}
							if (!web && !PlayerManager.groundAround(NESS.main.oldLoc.get(player))) {
								if (!player.isFlying()
										&& player.getLocation().getY() - NESS.main.oldLoc.get(player).getY() > -2
										&& PlayerManager.timeSince("wasFlight", player) >= 3000
										&& PlayerManager.timeSince("isHit", player) >= 2000
										&& PlayerManager.getAction("placeTicks", player) == 0
										&& !player.hasPotionEffect(PotionEffectType.JUMP)) {
									if (!NESS.main.oldLoc.get(player).equals(player.getLocation())
											&& bottom != Material.SLIME_BLOCK)
										WarnHacks.warnHacks(player, "Flight", 5, -1);
								}
							}
						}
					}
					NESS.main.oldLoc.put(player, player.getLocation());
					NESS.main.lastHitLoc.remove(player);
					NESS.main.lastLookLoc.remove(player);
					PlayerManager.removeAction("shiftTicks", player);
					boolean flag = true;
					if (PlayerManager.getAction("oldFire", player) > 100 && player.getFireTicks() == -20
							&& PlayerManager.timeSince("teleported", player) >= 1000) {
						if (player.getGameMode() != GameMode.CREATIVE) {
							for (int x = -1; x <= 1; x++) {
								for (int z = -1; z <= 1; z++) {
									if (player.getWorld().getBlockAt(player.getLocation().add(x, 0, z)).isLiquid()) {
										flag = false;
									}
								}
							}
							if (flag)
								WarnHacks.warnHacks(player, "AntiFire", 50, -1);
						}
					}
					PlayerManager.setAction("oldFire", player, (double) player.getFireTicks());
				}
			}
		}.runTaskTimer(NESS.main, 0, 10);
		new BukkitRunnable() {
			@Override
			public void run() {
				for (Player player : Bukkit.getOnlinePlayers()) {
					if (PlayerManager.getAction("clicks", player) >= 18) {
						if (NESS.main.devMode)
							MSG.tell(player, "&9Dev> &7" + PlayerManager.getAction("clicks", player) + "");
						WarnHacks.warnHacks(player, "High CPS",
								(int) ((PlayerManager.getAction("clicks", player) - 15) * 5), -1);
					}
					if ((PlayerManager.getAction("regenTicks", player) >= 2
							&& !player.hasPotionEffect(PotionEffectType.SATURATION)
							&& !player.hasPotionEffect(PotionEffectType.HEAL)
							&& !player.hasPotionEffect(PotionEffectType.HEALTH_BOOST))
							|| PlayerManager.getAction("regenTicks", player) >= 5) {
						WarnHacks.warnHacks(player, "Regen", 50, -1);
					}
					if (PlayerManager.getAction("chatMessage", player) >= 10) {
						WarnHacks.warnHacks(player, "Spambot",
								(int) (PlayerManager.getAction("chatMessage", player) * 5), -1);
					}
					if (/* PlayerManager.getAction("moveTicks", player) > 30 && */ timerLoc.containsKey(player)
							&& PlayerManager.timeSince("wasFlight", player) >= 5000
							&& PlayerManager.timeSince("wasIce", player) >= 5000
							&& player.getLocation().getWorld().equals(timerLoc.get(player).getWorld())) {
						Double dist = timerLoc.get(player).distance(player.getLocation());
						Double hozDist = dist - Math.abs(player.getLocation().getY() - timerLoc.get(player).getY());
						if (hozDist > 8.5 && !player.isFlying() && !player.isInsideVehicle()) {
							WarnHacks.warnHacks(player, "Timer", Math.min(100, (int) (hozDist - 8)), 400);
							if (NESS.main.devMode)
								MSG.tell(player, "&9Dev> &7amo: " + PlayerManager.getAction("moveTicks", player)
										+ " dist: " + hozDist);
						}
					}
					if (PlayerManager.getAction("timerTick", player) >= 30) {
						PlayerManager.addAction("tooManyTicks", player);
						if (NESS.main.devMode)
							MSG.tell(player,
									"&9Dev> &7Too many ticks: " + PlayerManager.getAction("timerTick", player));
					}
					if (PlayerManager.getAction("tooManyTicks", player) >= 3) {
						WarnHacks.warnHacks(player, "Timer", 50, 500);
						PlayerManager.removeAction("tooManyTicks", player);
					}
					if (PlayerManager.getAction("skinPackets", player) >= 5
							&& PlayerManager.timeSince("lastMove", player) <= 500) {
						WarnHacks.warnHacks(player, "SkinBlinker",
								(int) (5 * (PlayerManager.getAction("skinPackets", player) + 10)), -1);
					}
					timerLoc.put(player, player.getLocation());
					PlayerManager.removeAction("clicks", player);
					PlayerManager.removeAction("timerTick", player);
					PlayerManager.removeAction("regenTicks", player);
					PlayerManager.removeAction("foodTicks", player);
					PlayerManager.removeAction("chatMessage", player);
					PlayerManager.removeAction("placeTicks", player);
					PlayerManager.removeAction("vPlaceTicks", player);
					PlayerManager.removeAction("moveTicks", player);
					PlayerManager.removeAction("skinPackets", player);
				}
				if (NESS.main.seconds % NESS.main.config.getInt("Configuration.BanWaves") == 0) {
					BanManager.banwave();
				}
				NESS.main.seconds++;
			}
		}.runTaskTimer(NESS.main, 0, 20);

		new BukkitRunnable() {
			public void run() {
				for (Player player : Bukkit.getOnlinePlayers()) {
					if ((PlayerManager.getAction("foodTicks", player) >= 2
							&& !player.hasPotionEffect(PotionEffectType.SATURATION)
							&& !player.hasPotionEffect(PotionEffectType.HUNGER)
							|| PlayerManager.getAction("foodTicks", player) >= 5)) {
						WarnHacks.warnHacks(player, "FastEat", 50, -1);
					}
				}
			}
		}.runTaskTimer(NESS.main, 0, 40);
	}
}
