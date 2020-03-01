package org.mswsplex.MSWS.NESS;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class OnAttack implements Listener {
	private HashMap<Player, Entity> lastEntityHit = new HashMap<>();
	static HashMap<Player, Entity> lastHitBy = new HashMap<>();

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onAttack(EntityDamageByEntityEvent event) {
		if (event.getDamager() == null)
			return;
		if (event.getDamager().getType() == EntityType.PLAYER) {
			Player player = (Player) event.getDamager();
			double maxDist = 5.3;
			if (player.getGameMode() == GameMode.CREATIVE)
				maxDist = 5.563;
			if (event.getEntity().getLocation().distance(player.getLocation()) > maxDist) {
				WarnHacks.warnHacks(player, "Reach", 10, 150);
				event.setCancelled(true);
				if (NESS.main.devMode) {
					MSG.tell(player, "&9Dev> &7Reach Distance: "
							+ event.getEntity().getLocation().distance(player.getLocation()));
				}
			}
			if (NESS.main.lastHitLoc.containsKey(player)) {
				if (NESS.main.lastHitLoc.get(player).distance(event.getEntity().getLocation()) >= 5) {
					WarnHacks.warnHacks(player, "Kill Aura", 10, -1);
				}
			}
			if ((!player.isOnGround()) && (player.getLocation().getY() % 1.0 == 0) && !player.isFlying()) {
				WarnHacks.warnHacks(player, "Criticals", 40, -1);
			}
			if (lastEntityHit.containsKey(player)
					&& lastEntityHit.get(player).getWorld().equals(event.getEntity().getWorld())) {
				if (NESS.main.lastHitLoc.containsKey(player)) {
					if (lastEntityHit.get(player).equals(event.getEntity())) {
						Double dist = event.getEntity().getLocation().distance(NESS.main.lastHitLoc.get(player));
						if (PlayerManager.timeSince("lastHit", player) <= 100 && dist > .23) {
							WarnHacks.warnHacks(player, "Kill Aura", 10, -1);
							if (NESS.main.devMode) {
								MSG.tell(player, "&9Dev> &7Quick hit: " + PlayerManager.timeSince("lastHit", player)
										+ " Velocity: " + dist);
							}
						}
					}
				}
			}
			NESS.main.vl.set(player.getUniqueId() + ".accuracy.hits",
					NESS.main.vl.getInt(player.getUniqueId() + ".accuracy.hits") + 1);
			int hits = NESS.main.vl.getInt(player.getUniqueId() + ".accuracy.hits");
			int miss = NESS.main.vl.getInt(player.getUniqueId() + ".accuracy.misses");
			double acc = (double) hits / (double) Math.max(miss + hits, 1);
			if (hits + miss >= 10) {
				if (acc > .80) {
					if (NESS.main.lastHitLoc.containsKey(player)) {
						if (lastEntityHit.get(player).equals(event.getEntity())) {
							Double dist = event.getEntity().getLocation().distance(NESS.main.lastHitLoc.get(player));
							if (dist > .129) {
								WarnHacks.warnHacks(player, "Kill Aura", 5, -1);
							}
						}
					}
				}
				NESS.main.vl.set(player.getUniqueId() + ".accuracy", null);
			}
			NESS.main.lastHitLoc.put(player, event.getEntity().getLocation());
			lastEntityHit.put(player, event.getEntity());
			if (player.getTargetBlock((HashSet<Byte>) null, 4).getType().isSolid())
				PlayerManager.addAction("clicks", player);

			if (Bukkit.getPluginManager().isPluginEnabled("ProtocolLib"))
				if (Protocols.angles.containsKey(player.getUniqueId())) {
					final Location real = Protocols.angles.get(player.getUniqueId());
					final double difference = real.getYaw() - player.getLocation().getYaw();
					if (difference > 1 && player.getFallDistance() < 1f) {
						if (NESS.main.devMode)
							MSG.tell(player, "&9Dev> &7Diff: " + difference);
						WarnHacks.warnHacks(player, "Kill Aura", 50, -1);
					}
					PlayerManager.setAction("oldDist", player, difference);
				}
			PlayerManager.setAction("lastHit", player, (double) System.currentTimeMillis());
		}
		if (event.getEntityType() == EntityType.PLAYER && !event.isCancelled()) {
			Player player = (Player) event.getEntity();
			// PlayerManager.setAction("isHit", player, (double)
			// System.currentTimeMillis());
			lastHitBy.put(player, event.getDamager());
			Location hitAt = event.getEntity().getLocation();
			Bukkit.getScheduler().scheduleSyncDelayedTask(NESS.main, () -> {
				boolean web = false;
				for (int x = -2; x <= 2; x++) {
					for (int y = -2; y <= 3; y++) {
						for (int z = -2; z <= 2; z++) {
							Material belowSel = player.getWorld().getBlockAt(player.getLocation().add(x, y, z))
									.getType();
							if (belowSel == Material.WEB) {
								web = true;
							}
						}
					}
				}
				Location hitTo = event.getEntity().getLocation();
				double dist = hitAt.distanceSquared(hitTo);
				if (dist < .58 && !player.getLocation().add(0, 2, 0).getBlock().getType().isSolid() && !web
						&& !player.getLocation().getBlock().getType().isSolid()) {
					WarnHacks.warnHacks(player, "AntiKB", (int) Math.round(10 * (.50 - dist) + 5), 500);
					if (NESS.main.devMode)
						MSG.tell(event.getEntity(), "&9Dev> &7KB Dist: " + dist);
				}
			}, 5);
		}
	}
}
