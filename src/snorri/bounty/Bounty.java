package snorri.bounty;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Bounty extends JavaPlugin {
	private static final Logger log = Logger.getLogger("Minecraft");
	private FileConfiguration config;
	private FileConfiguration languageConfig;
	private static final String NAME = "Bounty";
	private static Economy economy;
	private static Permission permission;

	// private static Scoreboard scoreboard;
	// private static Objective objective;
	public static Server server;

	public void onEnable() {
		println("Plugin enabled");
		this.config = getConfig();
		this.languageConfig = getCustomConfig("languageConfig.yml");
		System.out.print("Config: " + (config==null) + "Language: " + (languageConfig==null));
		loadConfig();
		server = getServer();
		getServer().getPluginManager().registerEvents(new BountyEventListener(), this);
		getServer().getPluginManager().registerEvents(new BountyMenu(), this);

		setupEconomy();
		setupPermission();
		// setupScoreboard();
		
		if (EssentialsHook.initialHook() == false) {
			Bukkit.getLogger().log(Level.SEVERE, "Essentials could not be hooked into! Bounty has been Disabled.");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		
		if (TownyHook.initialHook() == false) {
			Bukkit.getLogger().log(Level.SEVERE, "Towny could not be hooked into! Bounty has been Disabled.");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
	}

	public void onDisable() {
		try {
			closeConfig();
		} catch (IOException e) {
			warn("Failed to close config");
		}
		println("Plugin disabled");
	}

	private FileConfiguration getCustomConfig(String name) {
		File c = new File(getDataFolder(), name);
		return YamlConfiguration.loadConfiguration(c);
	}

	/*
	 * public static void updateScore(Player player) { if (!
	 * player.getScoreboard().equals(scoreboard))
	 * player.setScoreboard(scoreboard); Score score =
	 * objective.getScore(player); score.setScore((int)
	 * PlayerBounty.getSumOn(player)); }
	 */

	public boolean can(CommandSender sender, String p) {
		if (permission == null) {
			return true;
		}
		return permission.has(sender, p);
	}

	public String getFlags(String[] args) {
		String result = "";
		for (String arg : args) {
			if (arg.charAt(0) == '-') {
				for (int i = 1; i < arg.length(); i++) {
					result = result + arg.charAt(i);
				}
			}
		}
		return result;
	}

	public String[] getArgs(String[] args) {
		ArrayList<String> result = new ArrayList<String>();
		for (String arg : args) {
			if (arg.charAt(0) != '-') {
				result.add(arg);
			}
		}
		return (String[]) result.toArray(args);
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if ((cmd.getName().equalsIgnoreCase("bounty")) && (args.length > 1)) {
			if (((sender instanceof Player)) && (!can(sender, "bounty.set"))) {
				sender.sendMessage(
						Settings.getColor("failure") + LanguageSettings.getString("permissions.denied", "", "", ""));
				return true;
			}
			String flags = getFlags(args);
			args = getArgs(args);
			OfflinePlayer target = server.getOfflinePlayer(args[0]);
			if ((!target.hasPlayedBefore()) && (!flags.contains("o"))) {
				sender.sendMessage(Settings.getColor("failure")
						+ LanguageSettings.getString("chat.newPlayer1", target.getName(), sender.getName(), ""));
				sender.sendMessage(Settings.getColor("failure")
						+ LanguageSettings.getString("chat.newPlayer2", target.getName(), sender.getName(), ""));
				return true;
			}
			double amount = 0.0D + Integer.parseInt(args[1]);
			PlayerBounty existingBounty = PlayerBounty.getBounty(target, (Player) sender);
			if ((existingBounty == null) && (amount < Settings.getMin())) {
				sender.sendMessage(Settings.getColor("failure") + LanguageSettings.getString("chat.minimumBounty",
						target.getName(), sender.getName(), formatCurrency(Settings.getMin())));
				return true;
			}
			EconomyResponse result = getEconomy().withdrawPlayer((OfflinePlayer) sender, amount);
			if (!result.transactionSuccess()) {
				sender.sendMessage(Settings.getColor("failure") + LanguageSettings.getString("chat.outOfCash",
						target.getName(), sender.getName(), formatCurrency(amount)));
				return true;
			}
			if (existingBounty == null) {
				if ((!Settings.allowAnonymous()) && (flags.contains("a"))) {
					sender.sendMessage(Settings.getColor("failure")
							+ LanguageSettings.getString("permissions.anonymous", "", "", ""));
					return true;
				}
				PlayerBounty b = new PlayerBounty(target, (OfflinePlayer) sender, amount, flags.contains("a"));
				// if (target.getPlayer() != null)
				// updateScore(target.getPlayer());
				sender.sendMessage(Settings.getColor("success") + LanguageSettings.getString("chat.bountyPlaced",
						target.getName(), b.getSetByPlayerName(), formatCurrency(amount)));
				globalBroadcast(LanguageSettings.getString("broadcast.bountyPlaced", target.getName(),
						b.getSetByPlayerName(), formatCurrency(amount)));
				return true;
			}
			existingBounty.addReward(amount);
			sender.sendMessage(Settings.getColor("success") + LanguageSettings.getString("chat.bountyIncreased",
					target.getName(), sender.getName(), formatCurrency(existingBounty.getReward())));
			return true;
		}
		else if ((cmd.getName().equalsIgnoreCase("bounties")) && (args.length == 0)) {
			if (((sender instanceof Player)) && (!can(sender, "bounty.view"))) {
				sender.sendMessage(
						Settings.getColor("failure") + LanguageSettings.getString("permissions.denied", "", "", ""));
				return true;
			}
			sender.sendMessage(Settings.getColor("success") + LanguageSettings.getString("chat.activeHeader", "", "", ""));
			if (PlayerBounty.active.size() == 0) {
				sender.sendMessage(Settings.getColor("blank") + LanguageSettings.getString("chat.none", "", "", ""));
			}
			for (int i = 0; (i < PlayerBounty.active.size()) && (i < Settings.getListLength()); i++) {
				PlayerBounty bounty = (PlayerBounty) PlayerBounty.active.get(i);

				// TODO: add real UUID conversion
				// this is quick fix (could also add anonymous name thing)
				// fixed for now

				String targetName = bounty.getTarget().getName();
				if (targetName == null)
					targetName = bounty.getTarget().getUniqueId().toString();

				// make this universal if it's an issue

				sender.sendMessage(Settings.getColor("success") + LanguageSettings.getString("chat.bountyList",
						targetName, bounty.getSetByPlayerName(), formatCurrency(bounty.getReward())));

			}
			return true;
		}
		else if ((cmd.getName().equalsIgnoreCase("bountyon")) && (args.length == 1)) {
			if (((sender instanceof Player)) && (!can(sender, "bounty.view"))) {
				sender.sendMessage(
						Settings.getColor("failure") + LanguageSettings.getString("permissions.denied", "", "", ""));
				return true;
			}
			OfflinePlayer target = getServer().getOfflinePlayer(args[0]);
			double amount = PlayerBounty.getSumOn(target);
			sender.sendMessage(Settings.getColor("success")
					+ LanguageSettings.getString("chat.bountyOn", target.getName(), "", formatCurrency(amount)));
			return true;
		}
		else if ((cmd.getName().equalsIgnoreCase("unbounty")) && (args.length == 1)) {
			if (((sender instanceof Player)) && (!can(sender, "bounty.cancel"))) {
				sender.sendMessage(
						Settings.getColor("failure") + LanguageSettings.getString("permissions.denied", "", "", ""));
				return true;
			}
			OfflinePlayer target = getServer().getOfflinePlayer(args[0]);

			OfflinePlayer setBy = (OfflinePlayer) sender;
			double refund = PlayerBounty.getBounty(target, setBy).cancel();
			getEconomy().depositPlayer((OfflinePlayer) sender, refund);
			sender.sendMessage(Settings.getColor("success") + LanguageSettings.getString("chat.bountyCancelled",
					target.getName(), sender.getName(), formatCurrency(refund)));
			return true;
		} else if (cmd.getName().equalsIgnoreCase("bounties")) {
			
			if (((sender instanceof Player)) && (!can(sender, "bounty.view"))) {
				sender.sendMessage(
						Settings.getColor("failure") + LanguageSettings.getString("permissions.denied", "", "", ""));
				return true;
			}
			
			int page = 0;
			
			Player player = (Player) sender;
			
			if (args.length > 0) {
				try {
					page = Integer.parseInt(args[0]);
				} catch (NumberFormatException e) {
					player.sendMessage("§cWarning: " + args[0] + " isn't a Number!");
				}
			}
			
			BountyMenu.getBountyList(player, page);
			
			return true;
				
		}
		return false;
	}

	private void loadConfig() {
		File dir = new File("plugins/" + getName());
		if (!dir.exists()) {
			dir.mkdir();
			println("Created plugin data folder");
		}
		File configLoc = new File(dir, "config.yml");
		if (!configLoc.exists()) {
			try {
				configLoc.createNewFile();
				Settings.writeToConfig(this.config);
				PlayerBounty.writeCategories(this.config);
				this.config.save(configLoc);
				println("Created default config file");
			} catch (IOException localIOException) {
			}
		} else {
			Settings.readFromConfig(this.config);
			PlayerBounty.readFromConfig(this.config);
			println("Settings read from config file");
			System.out.print("Config.yml: " + (this.config==null));
		}
		configLoc = new File(dir, "languageConfig.yml");
		if (!configLoc.exists()) {
			try {
				configLoc.createNewFile();
				LanguageSettings.writeDefaultLanguageFile(this.languageConfig);
				this.languageConfig.save(configLoc);
				println("Created default language config file");
			} catch (IOException localIOException1) {
			}
		} else {
			LanguageSettings.setConfig(this.languageConfig);
			System.out.print("language.yml: " + (this.languageConfig==null));
			println("Language settings read from language config file");
		}
	}

	private void closeConfig() throws IOException {
		Settings.writeToConfig(this.config);
		PlayerBounty.writeToConfig(this.config);
		this.config.save("plugins/" + getName() + "/config.yml");
	}

	/*
	 * private void setupScoreboard() { scoreboard =
	 * Bukkit.getScoreboardManager().getNewScoreboard(); objective =
	 * scoreboard.registerNewObjective("showbounty", "dummy");
	 * objective.setDisplaySlot(DisplaySlot.BELOW_NAME); }
	 */

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> ecoProvider = getServer().getServicesManager()
				.getRegistration(Economy.class);
		if (ecoProvider != null) {
			setEconomy((Economy) ecoProvider.getProvider());
		}
		
		return getEconomy() != null;
	}

	private boolean setupPermission() {
		RegisteredServiceProvider<Permission> permProvider = getServer().getServicesManager()
				.getRegistration(Permission.class);
		if (permProvider != null) {
			setPermission((Permission) permProvider.getProvider());
		}
		return getPermission() != null;
	}

	public static String formatCurrency(double value) {
		return economy.format(value);
	}

	public static void globalBroadcast(String msg) {
		if (!Settings.shouldBroadcast()) {
			return;
		}
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.sendMessage(Settings.getColor("global") + "[" + "Bounty" + "] " + msg);
		}
	}

	public static void println(String msg) {
		log.info("[" + NAME + "] " + msg);
	}

	public static void warn(String msg) {
		log.warning("[" + NAME + "] " + msg);
	}

	public static Economy getEconomy() {
		return economy;
	}

	public static void setEconomy(Economy economy) {
		Bounty.economy = economy;
	}

	public static Permission getPermission() {
		return permission;
	}

	public static void setPermission(Permission permission) {
		Bounty.permission = permission;
	}
}

/*
 * Location: C:\Users\vikin_000\Desktop\Minecraft Plugin Development\Bounty.jar
 * 
 * Qualified Name: snorri.bounty.Bounty
 * 
 * JD-Core Version: 0.7.0.1
 * 
 */