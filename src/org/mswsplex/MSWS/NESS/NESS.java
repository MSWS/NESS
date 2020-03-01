package org.mswsplex.MSWS.NESS;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

public class NESS extends JavaPlugin implements PluginMessageListener {
	FileConfiguration config, vl, banwave, data;

	File vlYml = new File(getDataFolder() + "/vls.yml"), bwYml = new File(getDataFolder() + "/banwaves.yml");
	File dataYml = new File(getDataFolder() + "/data.yml"), configYml;

	HashMap<Player, Location> oldLoc = new HashMap<>();
	HashMap<Player, Location> safeLoc = new HashMap<>();
	HashMap<Player, Location> lastHitLoc = new HashMap<>();
	HashMap<Player, Location> lastLookLoc = new HashMap<>();
	public HashMap<Player, Boolean> legit = new HashMap<>();
	HashMap<OfflinePlayer, String> nessReason = new HashMap<>();

	String prefix, ver;
	double seconds;
	boolean devMode, debugMode;
	Updater updater = new Updater();
	public static NESS main;

	//Perm: ness.bypass.reportcooldown
	
	public void onEnable() {
		main = this;

		ver = getDescription().getVersion();
		configYml = new File(getDataFolder(), "config.yml");
		if (!configYml.exists())
			saveResource("config.yml", true);
		config = YamlConfiguration.loadConfiguration(configYml);
		data = YamlConfiguration.loadConfiguration(dataYml);
		devMode = config.getBoolean("Settings.DeveloperMode");
		debugMode = config.getBoolean("Settings.DebugMode");
		prefix = config.getString("Prefix");
		seconds = 0;
		vl = YamlConfiguration.loadConfiguration(vlYml);
		banwave = YamlConfiguration.loadConfiguration(bwYml);

		getServer().getPluginManager().registerEvents(new OnMove(), this);
		getServer().getPluginManager().registerEvents(new MiscEvents(), this);
		getServer().getPluginManager().registerEvents(new OnAttack(), this);
		getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);

		getCommand("ness").setExecutor(new NESSCommand());
		getCommand("ness").setTabCompleter(new NESSCommand());

		if (getServer().getPluginManager().getPlugin("ProtocolLib") != null) {
			Double milsSinceLoad = System.currentTimeMillis() - data.getDouble("LastShutdown");
			MSG.log("&aProtocolLib found!");
			if (Bukkit.getOnlinePlayers().size() == 0 && milsSinceLoad > 10000) {
				new Protocols();
			}
		} else {
			MSG.log("&cProtocolLib not found! Certain checks will be disabled.");
		}

		for (String res : vl.getKeys(false))
			vl.set(res, null);
		saveVl();

		new Metrics(this);

		if (config.getBoolean("CheckForUpdates")) {
			try {
				String onlineVer = updater.getSpigotVersion();
				if (MSG.outdated(ver + "", onlineVer)) {
					MSG.log("----------&a[NESS Update Checker]&7----------");
					MSG.log("&aA new update is available! &7Current Version: &c" + ver + " &7New Version: &b"
							+ onlineVer);
					MSG.log("Download it here: &bhttps://www.spigotmc.org/resources/53281/");
				}
			} catch (Exception e) {
				MSG.log("----------&a[NESS Update Checker]&7----------");
				MSG.log("&cCould not connect to spigotmc.org, the site may be down or the connection could be blocked.");
			}
		}
		new Timer().register();
		MSG.log("&aSuccesfully Enabled!");
	}

	public void onDisable() {
		// Logging the time of shutdown
		data.set("LastShutdown", (double) System.currentTimeMillis());
		saveData();
		main = null;
	}

	public void refresh() {
		if (!configYml.exists()) {
			saveResource("config.yml", true);
			MSG.log("&cWARNING! &7Config was not found, recreating it!");
		}
		data = YamlConfiguration.loadConfiguration(dataYml);
		config = YamlConfiguration.loadConfiguration(configYml);
		prefix = config.getString("Prefix");
		devMode = config.getBoolean("Settings.DeveloperMode");
	}

	public void saveVl() {
		try {
			vl.save(vlYml);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveBW() {
		try {
			banwave.save(bwYml);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveConfig() {
		try {
			config.save(configYml);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveData() {
		try {
			data.save(dataYml);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		if (!channel.equals("BungeeCord")) {
			return;
		}
		ByteArrayDataInput in = ByteStreams.newDataInput(message);
		String subchannel = in.readUTF();
		if (subchannel.equals("NESS") && !config.getBoolean("ServerOnly")) {
			short len = in.readShort();
			byte[] msgbytes = new byte[len];
			in.readFully(msgbytes);
			DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
			try {
				String msg = msgin.readUTF();
				for (Player target : Bukkit.getOnlinePlayers()) {
					if (target.hasPermission("ness.notify.hacks")) {
						MSG.tell(target, msg);
					}
				}
			} catch (IOException e) {
			}
		}
	}
}
