package org.mswsplex.MSWS.NESS;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;

public class OnMove implements Listener {

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		Material below = player.getWorld().getBlockAt(player.getLocation().subtract(0, 1, 0)).getType();
		Material bottom = null;
		Location from = event.getFrom(), to = event.getTo();
		Double dist = from.distance(to);
		Double hozDist = dist - (to.getY() - from.getY());
		Double fallDist = (double) player.getFallDistance();
		if (to.getY() < from.getY())
			hozDist = dist - (from.getY() - to.getY());
		Double vertDist = Math.abs(dist - hozDist);
		double dTG = 0; // Distance to ground
		NESS.main.legit.put(player, true);
		boolean groundAround = PlayerManager.groundAround(player.getLocation()), waterAround = false;
		int radius = 2;
		boolean ice = false, surrounded = true, lilypad = false, web = false, cactus = false;
		if (hozDist > .1 && vertDist == 0 && !player.isFlying() && player.isSprinting()) {
			PlayerManager.setAction("lastMove", player, (double) System.currentTimeMillis());
			if (PlayerManager.timeSince("lastChat", player) <= 50) {
				if (NESS.main.devMode)
					MSG.tell(player, "&9Dev> &7hozDist: " + hozDist);
				WarnHacks.warnHacks(player, "AutoWalk", 20, -1);
			}
		}

		double lastYaw = PlayerManager.getAction("lastYaw", player);
		double yawDiff = Math.abs(lastYaw - player.getLocation().getYaw());
		if (yawDiff > 0) {
			if (yawDiff % 1.0 == 0) {
				// if (NESS.main.devMode)
				// MSG.tell(player, "&9Dev> &7yawDiff: "+yawDiff);
				// WarnHacks.warnHacks(player, "Spinbot", 10, -1);
			}
		}

		PlayerManager.setAction("lastYaw", player, (double) player.getLocation().getYaw());
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				int y = 0;
				while (!player.getLocation().subtract(x, y, z).getBlock().getType().isSolid() && y < 20) {
					y++;
				}
				if (y < dTG || dTG == 0)
					dTG = y;
			}
		}
		dTG += player.getLocation().getY() % 1;
		bottom = player.getLocation().getWorld().getBlockAt(player.getLocation().subtract(0, dTG, 0)).getType();
		boolean carpet = false;
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				Material belowSel = player.getWorld().getBlockAt(player.getLocation().add(x, -1, z)).getType();
				for (Material mat : new Material[] { Material.ICE, Material.PACKED_ICE, Material.PISTON_BASE,
						Material.PISTON_STICKY_BASE }) {
					if (belowSel == mat)
						ice = true;
				}
				belowSel = player.getWorld().getBlockAt(player.getLocation().add(x, -.01, z)).getType();
				if (belowSel == Material.WATER_LILY)
					lilypad = true;
				if (belowSel == Material.CARPET || belowSel.toString().toLowerCase().contains("diode")
						|| belowSel.toString().toLowerCase().contains("comparator") || belowSel == Material.SNOW)
					carpet = true;
				if (belowSel.isSolid()) {
					PlayerManager.setAction("wasGround", player, (double) System.currentTimeMillis());
				}
			}
		}
		for (int x = -2; x <= 2; x++) {
			for (int y = -2; y <= 3; y++) {
				for (int z = -2; z <= 2; z++) {
					Material belowSel = player.getWorld().getBlockAt(player.getLocation().add(x, y, z)).getType();
					if (!belowSel.isSolid())
						surrounded = false;
					if (belowSel == Material.WEB) {
						web = true;
					}
					if (belowSel == Material.CACTUS)
						cactus = true;
				}
			}
		}
		if (ice) {
			PlayerManager.setAction("wasIce", player, (double) System.currentTimeMillis());
		}
		for (int x = -radius; x < radius; x++) {
			for (int y = -1; y < radius; y++) {
				for (int z = -radius; z < radius; z++) {
					Material mat = to.getWorld().getBlockAt(player.getLocation().add(x, y, z)).getType();
					if (mat.isSolid())
						waterAround = true;
				}
			}
		}
		if ((below == Material.WATER || below == Material.STATIONARY_WATER || below == Material.LAVA
				|| below == Material.STATIONARY_LAVA) && !player.isFlying()) {
			if (!waterAround && !lilypad
					&& !player.getWorld().getBlockAt(player.getLocation().add(0, 1, 0)).isLiquid()) {
				if ((Math.abs(from.getY() - to.getY()) + "").contains("00000000") || to.getY() == from.getY()) {
					WarnHacks.warnHacks(player, "Jesus", 10, -1);
				}
			}
		}

		if (NESS.main.debugMode) {
			MSG.tell(player, "&7dist: &e" + dist);
			MSG.tell(player, "&7X: &e" + player.getLocation().getX() + " &7V: &e" + player.getVelocity().getX());
			MSG.tell(player, "&7Y: &e" + player.getLocation().getY() + " &7V: &e" + player.getVelocity().getY());
			MSG.tell(player, "&7Z: &e" + player.getLocation().getZ() + " &7V: &e" + player.getVelocity().getZ());
			MSG.tell(player, "&7hozDist: &e" + hozDist + " &7vertDist: &e" + vertDist + " &7fallDist: &e" + fallDist);
			MSG.tell(player,
					"&7below: &e" + MSG.camelCase(below.toString()) + " bottom: " + MSG.camelCase(bottom.toString()));
			MSG.tell(player, "&7dTG: " + dTG);
			MSG.tell(player,
					"&7groundAround: &e" + MSG.TorF(groundAround) + " &7onGround: " + MSG.TorF(player.isOnGround()));
			MSG.tell(player, "&7ice: " + MSG.TorF(ice) + " &7surrounded: " + MSG.TorF(surrounded) + " &7lilypad: "
					+ MSG.TorF(lilypad) + " &7web: " + MSG.TorF(web));
			MSG.tell(player, " &7waterAround: " + MSG.TorF(waterAround));
		}

		if (surrounded && (hozDist > .2 || to.getBlockY() < from.getBlockY())) {
			WarnHacks.warnHacks(player, "NoClip", 10, -1);
		}
		if (player.isInsideVehicle()) {
			if (!groundAround && from.getY() <= to.getY()) {
				if (!player.isInsideVehicle()
						|| player.isInsideVehicle() && player.getVehicle().getType() != EntityType.HORSE)
					WarnHacks.warnHacks(player, "Flight", 10, -1);
			}
		}
		// SPEED/FLIGHT CHECK
		Double maxSpd = 0.4209;
		Material mat = null;
		if (player.isBlocking())
			maxSpd = .1729;
		if (PlayerManager.getInfo("blocking", player) != null) {
			if (player.getLocation().getY() % .5 == 0.0) {
				maxSpd = .2;
			} else {
				maxSpd = .3;
			}
		}
		for (int x = -1; x < 1; x++) {
			for (int z = -1; z < 1; z++) {
				mat = from.getWorld()
						.getBlockAt(from.getBlockX() + x, player.getEyeLocation().getBlockY() + 1, from.getBlockZ() + z)
						.getType();
				if (mat.isSolid()) {
					maxSpd = 0.50602;
					break;
				}
			}
		}
		if (player.isInsideVehicle() && player.getVehicle().getType() == EntityType.BOAT)
			maxSpd = 2.787;
		if (hozDist > maxSpd && !player.isFlying() && !player.hasPotionEffect(PotionEffectType.SPEED)
				&& PlayerManager.timeSince("wasFlight", player) >= 2000
				&& PlayerManager.timeSince("isHit", player) >= 2000
				&& PlayerManager.timeSince("teleported", player) >= 100) {
			if (groundAround) {
				if (PlayerManager.timeSince("wasIce", player) >= 1000) {
					if (!player.isInsideVehicle()
							|| player.isInsideVehicle() && player.getVehicle().getType() != EntityType.HORSE) {
						Material small = player.getWorld().getBlockAt(player.getLocation().subtract(0, .1, 0))
								.getType();
						if (!player.getWorld().getBlockAt(from).getType().isSolid()
								&& !player.getWorld().getBlockAt(to).getType().isSolid()) {
							if (small != Material.TRAP_DOOR && small != Material.IRON_TRAPDOOR) {
								if (NESS.main.devMode)
									MSG.tell(player, "&9Dev> &7Speed amo: " + hozDist);
								if (PlayerManager.getAction("blocks", player) < 2)
									if (player.isBlocking() || PlayerManager.getInfo("blocking", player) != null) {
										WarnHacks.warnHacks(player, "NoSlowDown", 10, 500);
									} else {
										WarnHacks.warnHacks(player, "Speed", 20, 500);
									}
							}
						}
					}
				}
			} else if (PlayerManager.timeSince("wasIce", player) >= 1000
					&& PlayerManager.timeSince("teleported", player) >= 500) {
				WarnHacks.warnHacks(player, "Flight", 20, 500);
			}
		}
		if (player.isSneaking() && !player.isFlying() && !player.hasPotionEffect(PotionEffectType.SPEED)) {
			if (hozDist > .2 && NESS.main.oldLoc.containsKey(player)
					&& NESS.main.oldLoc.get(player).getY() == player.getLocation().getY()
					&& PlayerManager.timeSince("wasFlight", player) >= 2000
					&& PlayerManager.timeSince("wasIce", player) >= 1000
					&& PlayerManager.timeSince("isHit", player) >= 1000
					&& PlayerManager.timeSince("teleported", player) >= 500) {
				if (!player.getWorld().getBlockAt(from).getType().isSolid()
						&& !player.getWorld().getBlockAt(to).getType().isSolid()) {
					WarnHacks.warnHacks(player, "Fast Sneak", 20, -1);
				}
			}
		}
		if (to.getY() == from.getY()) {
			if (!groundAround) {
				if (hozDist > .35 && PlayerManager.timeSince("wasIce", player) >= 1000) {
					if (!player.isFlying()) {
						WarnHacks.warnHacks(player, "Flight", 5, -1);
					}
				}
			} else {
				if (!player.isOnGround()) {
					if (NESS.main.oldLoc.containsKey(player)) {
						if (NESS.main.oldLoc.get(player).getY() == player.getLocation().getY()) {
							if (hozDist > .35 && !player.hasPotionEffect(PotionEffectType.SPEED) && !player.isFlying())
								if (PlayerManager.timeSince("teleported", player) >= 2000
										&& PlayerManager.timeSince("isHit", player) >= 1000
										&& PlayerManager.timeSince("wasFlight", player) >= 2000) {
									if (!player.isInsideVehicle() || player.isInsideVehicle()
											&& player.getVehicle().getType() != EntityType.HORSE) {
										if (!player.getWorld().getBlockAt(from).getType().isSolid()
												&& !player.getWorld().getBlockAt(to).getType().isSolid()) {
											WarnHacks.warnHacks(player, "Flight", 20, -1);
										}
									}
								}
						}
					}
				}
			}
		} else {
			if (groundAround && !player.isFlying() && below == Material.LADDER
					&& player.getWorld().getBlockAt(player.getLocation()).getType() == Material.LADDER) {
				if (from.getY() < to.getY() && PlayerManager.timeSince("isHit", player) >= 1000
						&& PlayerManager.distToBlock(player.getLocation()) >= 3
						&& PlayerManager.timeSince("wasGround", player) >= 2000) {
					if (vertDist > .118 && !player.isSneaking()) {
						WarnHacks.warnHacks(player, "FastLadder", 20, -1);
					}
				}
			}
		}
		if (from.getY() % .5 != 0 && to.getY() % .5 != 0 && !player.isFlying()) {
			String amo = "";
			Double diff = 1.0;
			if (NESS.main.oldLoc.containsKey(player)) {
				amo = to.getY() - NESS.main.oldLoc.get(player).getY() + "";
				diff = Math.abs(to.getY() - NESS.main.oldLoc.get(player).getY());
				if (amo.contains("999999") || amo.contains("0000000")
						|| ((diff < 0.05 && diff >= 0)) && !groundAround) {
					boolean fly = true;
					for (Material antMat : new Material[] { Material.STATIONARY_WATER, Material.WATER, Material.LAVA,
							Material.STATIONARY_LAVA, Material.CAULDRON, Material.CACTUS, Material.CARPET,
							Material.SNOW, Material.LADDER, Material.CHEST, Material.ENDER_CHEST,
							Material.TRAPPED_CHEST, Material.VINE }) {
						if (player.getWorld().getBlockAt(player.getLocation().add(0, 1, 0)).isLiquid()
								|| player.getWorld().getBlockAt(player.getLocation()).getType() == antMat
								|| below == antMat
								|| player.getWorld().getBlockAt(player.getLocation()).getType().isSolid()) {
							fly = false;
						}
					}
					if (fly && !web && PlayerManager.timeSince("sincePlace", player) >= 1000
							&& PlayerManager.timeSince("wasIce", player) >= 1000
							&& PlayerManager.timeSince("isHit", player) >= 1000 && bottom != Material.SLIME_BLOCK
							&& PlayerManager.timeSince("wasFlight", player) >= 500
							&& PlayerManager.timeSince("wasGround", player) >= 1500) {
						if (!player.isInsideVehicle()
								|| player.isInsideVehicle() && player.getVehicle().getType() != EntityType.HORSE)
							WarnHacks.warnHacks(player, "Flight", 5, 150);
					}
				}
				PlayerManager.setAction("oldFlight", player, to.getY() - NESS.main.oldLoc.get(player).getY());
			}
			if (player.isSneaking() && !player.hasPotionEffect(PotionEffectType.SPEED)) {
				if (hozDist > .2 && NESS.main.oldLoc.containsKey(player)
						&& NESS.main.oldLoc.get(player).getY() == player.getLocation().getY()
						&& PlayerManager.timeSince("wasFlight", player) >= 2000
						&& PlayerManager.timeSince("wasIce", player) >= 1000
						&& PlayerManager.timeSince("isHit", player) >= 1000) {
					if (!player.getWorld().getBlockAt(from).getType().isSolid()
							&& !player.getWorld().getBlockAt(to).getType().isSolid()) {
						WarnHacks.warnHacks(player, "Fast Sneak", 20, -1);
					}
				}
			}
		}
		if (player.getWorld().getHighestBlockAt(player.getLocation()).getLocation().distance(player.getLocation()) <= .5
				|| player.isOnGround()) {
			if (hozDist > .6 && !player.hasPotionEffect(PotionEffectType.SPEED) && !player.isFlying()
					&& PlayerManager.timeSince("wasFlight", player) >= 3000) {
				if (NESS.main.oldLoc.containsKey(player)) {
					if (NESS.main.oldLoc.get(player).getY() < to.getY() + 2
							&& PlayerManager.timeSince("isHit", player) >= 2000) {
						if (PlayerManager.timeSince("wasIce", player) >= 1000) {
							if (NESS.main.devMode)
								MSG.tell(player, "&9Dev> &7Speed amo: " + hozDist);
							WarnHacks.warnHacks(player, "Speed", 10, 400);
						}
					}
				}
			}
		} else {
			if (from.getY() == to.getY() && groundAround && player.isOnGround()) {
				if (hozDist > .6 && !player.hasPotionEffect(PotionEffectType.SPEED) && !player.isFlying()) {
					WarnHacks.warnHacks(player, "Speed", 30, -1);
				}
			}
		}
		if (player.getLocation().getYaw() > 360 || player.getLocation().getYaw() < -360
				|| player.getLocation().getPitch() > 90 || player.getLocation().getPitch() < -90) {
			WarnHacks.warnHacks(player, "Illegal Movement", 500, -1);
		}
		if (dist == 0) {
			if (!groundAround && !web && !player.isFlying() && PlayerManager.getAction("placeTicks", player) == 0
					&& bottom != Material.SLIME_BLOCK && bottom != Material.VINE && !cactus
					&& PlayerManager.timeSince("isHit", player) >= 500) {
				WarnHacks.warnHacks(player, "Flight", 10, 300);
			}
		}
		yawDiff = (double) Math.abs(from.getPitch() - to.getPitch());
		if (yawDiff > 30) {
			PlayerManager.setAction("extremeYaw", player, (double) System.currentTimeMillis());
		}
		if (!(player.isSneaking() && below == Material.LADDER) && !player.isFlying() && !player.isOnGround()
				&& player.getLocation().getY() % 1.0 == 0 && PlayerManager.timeSince("lastJoin", player) >= 1000
				&& PlayerManager.timeSince("teleported", player) >= 5000
				&& !below.toString().toLowerCase().contains("stairs") && below != Material.SLIME_BLOCK) {
			WarnHacks.warnHacks(player, "NoGround", 10, 300);
		}
		if (to.getY() != from.getY()) {
			if (from.getY() < to.getY()) {
				maxSpd = 1.52;
			} else {
				maxSpd = 10.0;
			}
			if (!groundAround && !player.isFlying()) {
				if (dist > maxSpd && !player.hasPotionEffect(PotionEffectType.JUMP) && !player.isFlying()
						&& PlayerManager.timeSince("isHit", player) >= 2000 && bottom != Material.SLIME_BLOCK) {
					WarnHacks.warnHacks(player, "Flight", 5, -1);
				}
				if (from.getY() >= to.getY()) {
					double vel = from.getY() - to.getY();
					if (!web && ((vel > 0.0799 && vel < 0.08) || (vel > .01 && vel < .02) || (vel > .549 && vel < .55))
							&& !player.isFlying() && PlayerManager.timeSince("wasFlight", player) >= 3000
							&& PlayerManager.timeSince("isHit", player) >= 1000) {
						WarnHacks.warnHacks(player, "Flight", 5, -1);
					}
					if ((vel > 0.0999 && vel < 0.1) && to.getY() > 0) {
						WarnHacks.warnHacks(player, "Glide", 5, -1);
					}
					if (vel == .125) {
						WarnHacks.warnHacks(player, "Glide", 5, -1);
					}
				} else {
					if (hozDist == 0 && !player.hasPotionEffect(PotionEffectType.JUMP)
							&& PlayerManager.timeSince("wasFlight", player) >= 3000
							&& PlayerManager.timeSince("sincePlace", player) >= 1000 && bottom != Material.SLIME_BLOCK
							&& !cactus && PlayerManager.timeSince("isHit", player) >= 500) {
						WarnHacks.warnHacks(player, "Flight", 10, -1);
					}
				}
			} else {
				step: if (to.getY() - from.getY() > .6 && !player.isFlying() && groundAround
						&& !player.hasPotionEffect(PotionEffectType.JUMP)
						&& PlayerManager.timeSince("wasFlight", player) >= 100 && bottom != Material.SLIME_BLOCK) {
					for (Entity ent : player.getNearbyEntities(2, 2, 2)) {
						if (ent instanceof Boat)
							break step;
					}
					WarnHacks.warnHacks(player, "Step", 10 * (int) (to.getY() - from.getY()), -1);
				}
				if (from.getY() - to.getY() > 1 && fallDist == 0) {
					if (from.getY() - to.getY() > 2) {
						WarnHacks.warnHacks(player, "Phase", 50, -1);
					}
				}
			}
			if (from.getY() - to.getY() > .3 && fallDist <= .4 && below != Material.STATIONARY_WATER
					&& !player.getLocation().getBlock().isLiquid()) {
				if (hozDist < .1 || !groundAround) {
					if (groundAround && hozDist > .05 && PlayerManager.timeSince("isHit", player) >= 1000) {
						if (!player.isInsideVehicle()
								|| player.isInsideVehicle() && player.getVehicle().getType() != EntityType.HORSE)
							WarnHacks.warnHacks(player, "Speed", 10, -1);
					} else if (PlayerManager.timeSince("breakTime", player) >= 2000
							&& PlayerManager.timeSince("teleported", player) >= 500 && below != Material.PISTON_BASE
							&& below != Material.PISTON_STICKY_BASE) {
						if ((!player.isInsideVehicle()
								|| (player.isInsideVehicle() && player.getVehicle().getType() != EntityType.HORSE))
								&& !player.isFlying() && to.getY() > 0) {
							if (bottom != Material.SLIME_BLOCK)
								WarnHacks.warnHacks(player, "NoFall", 20, -1);
						}
					}
				} else if (bottom != Material.SLIME_BLOCK) {
					if (!player.isInsideVehicle()
							|| player.isInsideVehicle() && player.getVehicle().getType() != EntityType.HORSE
									&& PlayerManager.timeSince("isHit", player) >= 1000)
						WarnHacks.warnHacks(player, "BunnyHop", 25, -1);
				}
			}
			if (from.getY() - to.getY() > 0.3 && below != Material.STATIONARY_WATER
					&& !player.getLocation().getBlock().isLiquid()) {
				for (Double amo : new Double[] { .3959395, .8152412, .4751395, .5317675 }) {
					if (Math.abs(fallDist - amo) < .01 && !web) {
						if (groundAround && below.isSolid() && PlayerManager.timeSince("sincePlace", player) >= 1000
								&& PlayerManager.timeSince("isHit", player) >= 1000)
							WarnHacks.warnHacks(player, "BunnyHop", 25, -1);
					}
				}
				boolean flag = true;
				if (fallDist > 1 || PlayerManager.timeSince("wasFlight", player) <= 500) {
					flag = false;
				} else {
					for (Double amo : new Double[] { .7684762, .46415937 }) {
						if ((fallDist - amo) < .01) {
							flag = false;
						}
					}
				}
				if (flag && PlayerManager.timeSince("isHit", player) >= 1000 && !player.isFlying()
						&& PlayerManager.timeSince("sincePlace", player) >= 1000 && below != Material.LADDER
						&& PlayerManager.timeSince("isHit", player) >= 1000 && !web)
					WarnHacks.warnHacks(player, "BunnyHop", 5, -1);
			}

			if (to.getY() > from.getY()) {
				double lastDTG = PlayerManager.getAction("lastDTG", player);
				String diff = Math.abs(dTG - lastDTG) + "";
				if (player.getLocation().getY() % .5 != 0 && !player.isFlying() && !below.isSolid()
						&& (((dTG + "").contains("99999999") || (dTG + "").contains("00000000"))
								|| diff.contains("000000") || diff.startsWith("0.286"))
						&& PlayerManager.timeSince("isHit", player) >= 500
						&& !below.toString().toLowerCase().contains("water")
						&& !below.toString().toLowerCase().contains("lava")) {
					WarnHacks.warnHacks(player, "Spider", 20, -1);
					if (NESS.main.devMode)
						MSG.tell(player, "&9Dev> &7dTG: " + dTG + " diff: " + diff);
				}
				PlayerManager.setInfo("lastDTG", player, dTG);
			}
		} else {
			if (!groundAround && hozDist > .32 && vertDist == 0 && !player.isFlying()
					&& PlayerManager.timeSince("sincePlace", player) >= 1000
					&& PlayerManager.timeSince("wasIce", player) >= 1000)
				WarnHacks.warnHacks(player, "Flight", 5, -1);
			// Block rightBelow = player.getLocation().subtract(0, .1, 0).getBlock();
			if (player.getLocation().getY() % .5 != 0.0 && !player.isFlying()
					&& PlayerManager.timeSince("wasGround", player) > 1000
					&& PlayerManager.timeSince("sincePlace", player) >= 1500
					&& !bottom.toString().toLowerCase().contains("fence")
					&& !bottom.toString().toLowerCase().contains("wall") && !web && !carpet
					&& !below.toString().toLowerCase().contains("diode")
					&& !below.toString().toLowerCase().contains("comparator") && below != Material.SNOW && !lilypad
					&& !waterAround) {
				WarnHacks.warnHacks(player, "Flight", 5, -1);
			}
		}
		if (player.getWorld().getBlockAt(player.getLocation()).getType() == Material.WEB) {
			if (dist > .2 && !player.isFlying() && !player.hasPotionEffect(PotionEffectType.SPEED))
				WarnHacks.warnHacks(player, "NoWeb", (int) Math.round(dist * 20), -1);
		}
		PlayerManager.addAction("moveTicks", player);
		if (NESS.main.legit.get(player) && below.isSolid()) {
			NESS.main.safeLoc.put(player, player.getLocation());
		}
		PlayerManager.setAction("oldYaw", player, (double) player.getLocation().getYaw());
	}
}
