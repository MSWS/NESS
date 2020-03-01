package org.mswsplex.MSWS.NESS;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class MiscEvents implements Listener {
	@EventHandler
	public void commandProcess(PlayerCommandPreprocessEvent event) {
		PlayerManager.addAction("chatMessage", event.getPlayer());
	}

	@SuppressWarnings("unchecked")
	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		PlayerManager.addAction("chatMessage", event.getPlayer());
		PlayerManager.setInfo("lastChat", player, System.currentTimeMillis());
		List<Double> chats = new ArrayList<Double>();
		if (PlayerManager.getInfo("chatTimes", player) != null) {
			chats = (List<Double>) PlayerManager.getInfo("chatTimes", player);
		}
		chats.add((double) System.currentTimeMillis());
		PlayerManager.setInfo("chatTimes", player, chats);
		double lastClick = 0;
		double lastTime = 0;
		int times = 0;
		for (int i = 0; i < chats.size(); i++) {
			double d = chats.get(i);
			if (Math.abs(lastTime - (d - lastClick)) <= 5) {
				times++;
			}
			if (System.currentTimeMillis() - d > 5000) {
				chats.remove(i);
			}
			lastTime = d - lastClick;
			lastClick = d;
		}
		if (chats.size() > 20) {
			WarnHacks.warnHacks(player, "Spambot", 5 * (chats.size() - 8), -1);
			PlayerManager.addLogMessage(player, "Spammed: " + event.getMessage() + " TIME: %time%");
		}
		if (times > 4) {
			WarnHacks.warnHacks(player, "Spambot", 5 * (times - 2), -1);
			PlayerManager.addLogMessage(player, "Spammed: " + event.getMessage() + " TIME: %time%");
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDamaged(EntityDamageEvent event) {
		if (event.getEntity() == null || !(event.getEntity() instanceof Player) || event.isCancelled())
			return;
		Player player = (Player) event.getEntity();
		PlayerManager.setAction("isHit", player, (double) System.currentTimeMillis());
	}

	@EventHandler
	public void onClickEvent(InventoryClickEvent event) {
		if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR)
			return;
		ItemStack item = event.getCurrentItem();
		if (!item.hasItemMeta())
			return;
		ItemMeta meta = item.getItemMeta();
		Map<Enchantment, Integer> enchants = meta.getEnchants();
		if (!NESS.main.config.getBoolean("Settings.AllowEnchants")) {
			for (Enchantment enchant : enchants.keySet()) {
				if (meta.getEnchantLevel(enchant) >= 100) {
					event.setResult(org.bukkit.event.Event.Result.DENY);
					event.setCancelled(true);
					event.setCurrentItem(new ItemStack(Material.AIR));
				}
			}
		}
	}

	@EventHandler
	public void onToggleFlight(PlayerToggleFlightEvent event) {
		PlayerManager.setAction("wasFlight", event.getPlayer(), (double) System.currentTimeMillis());
	}

	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if (event.getBlock().isLiquid()) {
			WarnHacks.warnHacks(player, "Illegal Interaction", 100, 200);
		}
		if (event.getBlock().getType() == Material.LONG_GRASS || event.getBlock().getType() == Material.SNOW)
			PlayerManager.setAction("longBroken", player, (double) System.currentTimeMillis());
//		Block target = player.getTargetBlock((Set<Material>) null, 3);
//		boolean bypass = false;
//		if(Bukkit.getServer().getPluginManager().isPluginEnabled("mcMMO")) {
//			if(AbilityAPI.treeFellerEnabled(player)) {
//				bypass = true;
//			}
//		}
//		if (!event.getBlock().getLocation().equals(target.getLocation()) && target.getType().isSolid()
//				&& !target.getType().name().toLowerCase().contains("sign")
//				&& !target.getType().name().toLowerCase().contains("step")
//				&& !target.getType().name().toLowerCase().contains("chest")
//				&& target.getType() != Material.SNOW
//				&& target.getType() != Material.TORCH
//				&& target.getType() != Material.TNT
//				&& !target.getType().name().toLowerCase().contains("leaves")
//				&& player.getGameMode() != GameMode.CREATIVE
//				&& PlayerManager.timeSince("longBroken", player)>1000
//				&& !bypass) {
//			if (NESS.main.devMode)
//				MSG.tell(player,
//						"&9Dev> &7type: " + target.getType() + " Solid: " + MSG.TorF(target.getType().isSolid()));
//			WarnHacks.warnHacks(player, "Illegal Interaction", 40, 200);
//		}
		PlayerManager.setAction("breakTime", player, (double) System.currentTimeMillis());
	}

	@EventHandler
	public void onRegen(EntityRegainHealthEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if (!player.hasPotionEffect(PotionEffectType.REGENERATION)
					&& !player.hasPotionEffect(PotionEffectType.SATURATION)
					&& !player.hasPotionEffect(PotionEffectType.HEAL)
					&& !player.hasPotionEffect(PotionEffectType.HEALTH_BOOST)) {
				PlayerManager.addAction("regenTicks", player);
			}
		}
	}

	@EventHandler
	public void onFoodChange(FoodLevelChangeEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			ItemStack hand = player.getItemInHand();
			if (hand == null || hand.getType() == Material.AIR)
				return;
			if (!isFood(hand.getType()))
				return;
			if (player.getFoodLevel() >= event.getFoodLevel())
				return;
			if (player.hasPotionEffect(PotionEffectType.SATURATION))
				return;
			if (PlayerManager.timeSince("lastAte", player) < 1650) {
				WarnHacks.warnHacks(player, "FastEat",
						(int) Math.min((1650 - PlayerManager.timeSince("lastAte", player)), 100), -1);
				if (NESS.main.devMode)
					MSG.tell(player, "&9Dev> &7Food delay: " + PlayerManager.timeSince("lastAte", player));
			}
			PlayerManager.addAction("foodTicks", player);
			PlayerManager.setInfo("lastAte", player, System.currentTimeMillis());
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlaceBlock(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		Block target = player.getTargetBlock((Set<Material>) null, 5);

		if (event.getBlock().getWorld().getBlockAt(event.getBlock().getLocation().subtract(0, 1, 0))
				.getType() == Material.AIR) {
			if (!event.getBlock().getLocation().equals(target.getLocation())) {
				if ((!event.isCancelled()) && target.getType().isSolid()
						&& !target.getType().name().toLowerCase().contains("sign")
						&& !target.getType().toString().toLowerCase().contains("fence")
						&& player.getLocation().getY() > event.getBlock().getLocation().getY())
					WarnHacks.warnHacks(player, "Scaffold", 20, -1);
			}
			if (NESS.main.oldLoc.containsKey(player)
					&& NESS.main.oldLoc.get(player).getY() == player.getLocation().getY()) {
				if (!player.isSneaking() && !player.isFlying() && PlayerManager.groundAround(player.getLocation())) {
					if (PlayerManager.getAction("placeTicks", player) > 2)
						if (player.getWorld().getBlockAt(player.getLocation().subtract(0, 1, 0))
								.equals(event.getBlock()))
							WarnHacks.warnHacks(player, "Scaffold", 50, -1);
				}
			}
		} else if (event.getBlock().getType() == event.getBlock().getWorld()
				.getBlockAt(event.getBlock().getLocation().subtract(0, 1, 0)).getType()) {
			if (NESS.main.oldLoc.get(player).getX() == player.getLocation().getX()
					&& NESS.main.oldLoc.get(player).getZ() == player.getLocation().getZ()
					&& NESS.main.oldLoc.get(player).getY() < player.getLocation().getY()) {
				if (PlayerManager.getAction("vPlaceTicks", player) > 2 && !player.isFlying()) {
					if (player.getWorld().getBlockAt(player.getLocation().subtract(0, 1, 0)).equals(event.getBlock()))
						WarnHacks.warnHacks(player, "Scaffold", 50, -1);
				}
			}
		}

		if (!player.isSneaking() && !player.isFlying() && PlayerManager.groundAround(player.getLocation())) {
			if (event.getBlock().getWorld().getBlockAt(event.getBlock().getLocation().subtract(0, 1, 0))
					.getType() == Material.AIR) {
				if (player.getWorld().getBlockAt(player.getLocation().subtract(0, 1, 0)).equals(event.getBlock())) {
					if (PlayerManager.timeSince("extremeYaw", player) <= 250) {
						WarnHacks.warnHacks(player, "Scaffold", 20, -1);
					}
					if (PlayerManager.getAction("placeTicks", player) > 2) {
						WarnHacks.warnHacks(player, "Scaffold", 20, -1);
					}
				}
			}
		}

		if (event.getBlockAgainst().isLiquid() && event.getBlock().getType() != Material.WATER_LILY
				&& !event.isCancelled()) {
			WarnHacks.warnHacks(player, "Illegal Interaction", 100, 200);
		}
		if (player.getWorld().getBlockAt(player.getLocation().subtract(0, 1, 0)).equals(event.getBlock()))
			PlayerManager.addAction("vPlaceTicks", player);
		PlayerManager.addAction("placeTicks", player);
		PlayerManager.setAction("sincePlace", player, (double) System.currentTimeMillis());
	}

	@EventHandler
	public void onHotbarSwap(PlayerItemHeldEvent event) {
		Player player = event.getPlayer();
		ItemStack hand = player.getItemInHand();
		if (hand != null) {
			if ((hand.getType() == Material.BOW
					&& player.getInventory().containsAtLeast(new ItemStack(Material.ARROW), 1))) {
				PlayerManager.setInfo("blocking", player, true);
			}
		}
		PlayerManager.setInfo("blocking", player, null);
	}

	@SuppressWarnings("unchecked")
	@EventHandler(priority = EventPriority.LOWEST)
	public void onInteract(PlayerInteractEvent event) {
		Player player = (Player) event.getPlayer();
		Block target = player.getTargetBlock((Set<Material>) null, 5);
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.isBlockInHand() && !player.isFlying()) {
			if (player.getLocation().getY() > target.getLocation().getY() && !target.isLiquid()
					&& target.getY() % .5 == 0) {
				List<Block> blocks = player.getLastTwoTargetBlocks((Set<Material>) null, 10);
				BlockFace face = null;
				if (blocks.size() > 1) {
					face = blocks.get(1).getFace(blocks.get(0));
				}
				if (event.getBlockFace() != face && target.getType() != Material.LEVER) {
					WarnHacks.warnHacks(player, "Scaffold", 5, -1);
				}
			}
		}
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
			if (!event.getClickedBlock().getLocation().equals(target.getLocation()) && target.getType().isSolid()
					&& !target.getType().name().toLowerCase().contains("sign")
					&& !target.getType().name().toLowerCase().contains("step") && target.getType() != Material.CACTUS
					&& PlayerManager.timeSince("longBroken", player) > 1000) {
				if (NESS.main.devMode)
					MSG.tell(player,
							"&9Dev> &7type: " + target.getType() + " Solid: " + MSG.TorF(target.getType().isSolid()));
				// WarnHacks.warnHacks(player, "Illegal Interaction", 40, 200);
			}
		}
		if (!event.isCancelled()) {
			if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				ItemStack hand = player.getItemInHand();
				if (hand != null) {
					if (hand.getType().name().toLowerCase().contains("sword")) {
						PlayerManager.addAction("blocks", player);
					}
					if ((hand.getType() == Material.BOW
							&& player.getInventory().containsAtLeast(new ItemStack(Material.ARROW), 1))) {
						PlayerManager.setInfo("blocking", player, true);
					}
				}
			}
		}

//		if (NESS.main.lastLookLoc.containsKey(player)) {
//			if (player.getWorld().equals(NESS.main.lastLookLoc.get(player).getWorld()))
//				if (NESS.main.lastLookLoc.get(player).distance(target.getLocation()) >= 10) {
//					WarnHacks.warnHacks(player, "Kill Aura", 5, -1);
//				}
//		}
		NESS.main.vl.set(player.getUniqueId() + ".accuracy.misses",
				NESS.main.vl.getInt(player.getUniqueId() + ".accuracy.misses") + 1);
		NESS.main.lastLookLoc.put(player, target.getLocation());
		autoclicker: if ((target.getType() == Material.AIR || target.getType().isSolid())
				&& target.getType() != Material.SLIME_BLOCK) {
			if (player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR) {
				if (player.getItemInHand().containsEnchantment(Enchantment.DIG_SPEED)) {
					int lvl = player.getItemInHand().getEnchantmentLevel(Enchantment.DIG_SPEED);
					if (lvl >= 5)
						break autoclicker;
				}
			}
			for (PotionEffect eff : player.getActivePotionEffects()) {
				if (eff.getType() == PotionEffectType.FAST_DIGGING) {
					if (eff.getAmplifier() >= 2)
						break autoclicker;
				}
			}
			PlayerManager.addAction("clicks", player);
			List<Double> clicks = new ArrayList<Double>();
			if (PlayerManager.getInfo("clickTimes", player) != null) {
				clicks = (List<Double>) PlayerManager.getInfo("clickTimes", player);
			}
			clicks.add((double) System.currentTimeMillis());

			PlayerManager.setInfo("clickTimes", player, clicks);
			double lastClick = 0;
			double lastTime = 0;
			int times = 0;
			for (int i = 0; i < clicks.size(); i++) {
				double d = clicks.get(i);
				if (d - lastClick == lastTime && lastTime < 200) {
					times++;
				}
				if (System.currentTimeMillis() - d > 20000) {
					clicks.remove(i);
				}
				lastTime = d - lastClick;
				lastClick = d;
			}
			if (times > 50)
				WarnHacks.warnHacks(player, "AutoClicker", 5 * (times - 20) + 10, -1);
		}
	}

	@EventHandler
	public void onShootBow(ProjectileLaunchEvent event) {
		if (event.getEntity().getShooter() instanceof Player) {
			Player player = (Player) event.getEntity().getShooter();
			if (event.getEntity().getType() == EntityType.ARROW) {
				Vector vel = event.getEntity().getVelocity();
				double totVel = Math.abs(vel.getX()) + Math.abs(vel.getY()) + Math.abs(vel.getZ());
				for (int i = 0; i < totVel; i++) {
					PlayerManager.addAction("bowShots", player);
				}
				PlayerManager.setInfo("blocking", player, null);
			}
		}
	}

	@EventHandler
	public void onShift(PlayerToggleSneakEvent event) {
		Player player = event.getPlayer();
		PlayerManager.addAction("shiftTicks", player);
	}

	@EventHandler
	public void onTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		PlayerManager.setAction("teleported", player, (double) System.currentTimeMillis());
		Timer.timerLoc.remove(player);
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		PlayerManager.setAction("teleported", player, (double) System.currentTimeMillis());
		Timer.timerLoc.remove(player);
	}

	@EventHandler
	public void onRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		PlayerManager.setAction("teleported", player, (double) System.currentTimeMillis());
		Timer.timerLoc.remove(player);
	}

	@EventHandler
	public void onLogin(PlayerLoginEvent event) {
		Player player = (Player) event.getPlayer();
		String uuid = player.getUniqueId() + "";
		if (NESS.main.data.contains("Users." + uuid + ".history")
				&& NESS.main.config.getBoolean("Settings.HandleBans")) {
			for (String res : NESS.main.data.getConfigurationSection("Users." + uuid + ".history").getKeys(false)) {
				if (BanManager.isActive(player, res)) {
					if (NESS.main.data.getString("Users." + uuid + ".history." + res + ".type")
							.matches("(permban|exploiting|hacking|other|ipban)")) {
						Double dur = (double) ((NESS.main.data.getLong("Users." + uuid + ".history." + res + ".date")
								+ NESS.main.data.getLong("Users." + uuid + ".history." + res + ".duration") * 60 * 1000)
								- System.currentTimeMillis()) / 60000;
						String msg = "";
						for (String resres : NESS.main.config.getStringList("BanMessage")) {
							msg = msg + resres + "\n";
						}
						event.disallow(
								Result.KICK_BANNED, MSG
										.color(msg
												.replace("%token%",
														NESS.main.data.getString(
																"Users." + uuid + ".history." + res + ".reason"))
												.replace("%duration%", TimeManagement.getTime((int) Math.round(dur)))
												.replace("%hack%",
														NESS.main.banwave.getString("Logs."
																+ NESS.main.data.getString(
																		"Users." + uuid + ".history." + res + ".reason")
																+ ".reason"))));
					}
				}
			}
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (player.hasPermission("NESS.notify.updater") && NESS.main.config.getBoolean("CheckForUpdates")) {
			Updater updater = new Updater();
			try {
				String onlineVer = updater.getSpigotVersion();
				if (MSG.outdated(NESS.main.ver + "", onlineVer)) {
					MSG.tell(player, "&7----------&a[NESS Update Checker]&7----------");
					MSG.tell(player, "&aA new update is available! &7Current Version: &c" + NESS.main.ver
							+ " &7New Version: &b" + onlineVer);
					MSG.tell(player, "Download it here: &bhttps://www.spigotmc.org/resources/53281/");
				}
			} catch (Exception e) {
				MSG.tell(player, "&7----------&a[NESS Update Checker]&7----------");
				MSG.tell(player,
						"&cCould not connect to spigotmc.org, the site may be down or the connection could be blocked.");
			}
		}
		PlayerManager.setAction("lastJoin", player, (double) System.currentTimeMillis());
	}

	@EventHandler
	public void onKick(PlayerKickEvent event) {
		if (event.getReason().equals("disconnect.spam"))
			event.setCancelled(true);
	}

	private boolean isFood(Material mat) {
		List<Material> food = new ArrayList<Material>();
		food.add(Material.APPLE);
		food.add(Material.MUSHROOM_SOUP);
		food.add(Material.BREAD);
		food.add(Material.RAW_BEEF);
		food.add(Material.COOKED_BEEF);
		food.add(Material.GOLDEN_APPLE);
		food.add(Material.RAW_FISH);
		food.add(Material.COOKED_FISH);
		food.add(Material.COOKIE);
		food.add(Material.RAW_BEEF);
		food.add(Material.COOKED_BEEF);
		food.add(Material.RAW_CHICKEN);
		food.add(Material.COOKED_CHICKEN);
		food.add(Material.ROTTEN_FLESH);
		food.add(Material.SPIDER_EYE);
		food.add(Material.CARROT_ITEM);
		food.add(Material.POTATO_ITEM);
		food.add(Material.BAKED_POTATO);
		food.add(Material.POISONOUS_POTATO);
		food.add(Material.PUMPKIN_PIE);
		food.add(Material.RABBIT);
		food.add(Material.COOKED_RABBIT);
		food.add(Material.RABBIT_STEW);
		food.add(Material.MUTTON);
		food.add(Material.COOKED_MUTTON);
		food.add(Material.MELON);
		return food.contains(mat);
	}
}
