package org.mswsplex.MSWS.NESS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

public class Protocols {
	static Map<UUID, Location> angles = new HashMap<>();

	public Protocols() {
		if (!Bukkit.getPluginManager().isPluginEnabled("ProtocolLib"))
			return;
		ProtocolLibrary.getProtocolManager()
				.addPacketListener(new PacketAdapter(NESS.main, PacketType.Play.Client.POSITION) {
					public void onPacketReceiving(final PacketEvent event) {
						Player player = event.getPlayer();
						PlayerManager.addAction("timerTick", player);
					}
				});
		ProtocolLibrary.getProtocolManager()
				.addPacketListener(new PacketAdapter(NESS.main, PacketType.Play.Client.USE_ENTITY) {
					@Override
					public void onPacketReceiving(final PacketEvent event) {
						UUID u = event.getPlayer().getUniqueId();
						if (event.getPlayer() == null)
							return;
						final Location l = event.getPlayer().getLocation().clone();
						angles.put(u, l);
					}
				});
		ProtocolLibrary.getProtocolManager()
				.addPacketListener(new PacketAdapter(NESS.main, PacketType.Play.Client.CUSTOM_PAYLOAD) {
					@Override
					public void onPacketReceiving(final PacketEvent event) {
						Block target = event.getPlayer().getTargetBlock((Set<Material>) null, 5);
						if (target.getType() == Material.ANVIL)
							return;
						PlayerManager.addAction("packets", event.getPlayer());
						if (PlayerManager.getAction("packets", event.getPlayer()) > 10) {
							event.setCancelled(true);
						}
					}
				});
		ProtocolLibrary.getProtocolManager()
				.addPacketListener(new PacketAdapter(NESS.main, PacketType.Play.Client.SETTINGS) {
					@SuppressWarnings("unchecked")
					@Override
					public void onPacketReceiving(final PacketEvent event) {
						double lastClick = 0;
						double lastTime = 0;
						int times = 0;
						List<Double> packets = new ArrayList<Double>();
						if (PlayerManager.getInfo("packetTimes", event.getPlayer()) != null) {
							packets = (List<Double>) PlayerManager.getInfo("packetTimes", event.getPlayer());
						}
						packets.add((double) System.currentTimeMillis());
						PlayerManager.setInfo("packetTimes", event.getPlayer(), packets);

						for (int i = 0; i < packets.size(); i++) {
							double d = packets.get(i);
							if (Math.abs(lastTime - (d - lastClick)) <= 5) {
								times++;
							}
							if (System.currentTimeMillis() - d > 5000) {
								packets.remove(i);
							}
							lastTime = d - lastClick;
							lastClick = d;
						}

						if (times > 5) {
							WarnHacks.warnHacks(event.getPlayer(), "SkinBlinker", 20 * (times - 4), -1);
						}

						if (PlayerManager.getAction("skinPackets", event.getPlayer()) > 10) {
							event.setCancelled(true);
						}
						PlayerManager.addAction("skinPackets", event.getPlayer());
					}
				});
	}
}
