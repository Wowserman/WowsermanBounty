package snorri.bounty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class PlayerBounty {
	public static ArrayList<PlayerBounty> active = new ArrayList<PlayerBounty>();
	public static ArrayList<PlayerBounty> completed = new ArrayList<PlayerBounty>();
	private OfflinePlayer target;
	private OfflinePlayer setBy;
	private OfflinePlayer doneBy;
	private double reward;
	private boolean anonymous;

	public static void writeCategories(FileConfiguration config) {
		config.createSection("active");
		config.createSection("completed");
	}

	private static String getPlayerSignature(OfflinePlayer player) {
		return player.getUniqueId().toString();
	}

	@SuppressWarnings("deprecation")
	private static OfflinePlayer readPlayerSignature(String signature) {
		try {
			UUID id = UUID.fromString(signature);
			return Bounty.server.getOfflinePlayer(id);
		} catch (IllegalArgumentException e) {
		}
		return Bounty.server.getOfflinePlayer(signature);
	}

	public static void writeToConfig(FileConfiguration config) {

		config.set("active", null);
		config.set("completed", null);

		for (PlayerBounty bounty : active) {
			config.set("active." + getPlayerSignature(bounty.getSetBy()) + "." + getPlayerSignature(bounty.getTarget())
					+ ".reward", Double.valueOf(bounty.getReward()));
			config.set("active." + getPlayerSignature(bounty.getSetBy()) + "." + getPlayerSignature(bounty.getTarget())
					+ ".anonymous", Boolean.valueOf(bounty.isAnonymous()));
		}
		for (PlayerBounty bounty : completed) {
			config.set("completed." + getPlayerSignature(bounty.getSetBy()) + "."
					+ getPlayerSignature(bounty.getTarget()) + ".doneBy", bounty.getDoneBy());
			config.set("completed." + getPlayerSignature(bounty.getSetBy()) + "."
					+ getPlayerSignature(bounty.getTarget()) + ".reward", Double.valueOf(bounty.getReward()));
		}
	}
	
	public static List<PlayerBounty> getTopBounties() {
		List<PlayerBounty> b = new ArrayList<PlayerBounty>(active);
		Collections.sort(b, new Comparator<PlayerBounty>() {
			@Override
		    public int compare(PlayerBounty z1, PlayerBounty z2) {
		        if (z1.getReward() > z2.getReward())
		            return 1;
		        if (z1.getReward() < z2.getReward())
		            return -1;
		        return 0;
		    }
		});
		return b;
	}

	public static void readFromConfig(FileConfiguration config) {
		System.out.print("Config.yml 2:" + (config==null));
		ConfigurationSection activeList = config.getConfigurationSection("active");
		ConfigurationSection completedList = config.getConfigurationSection("completed");
		for (String setBy : activeList.getKeys(false)) {
			ConfigurationSection subSect = activeList.getConfigurationSection(setBy);
			for (String target : subSect.getKeys(false)) {
				OfflinePlayer setter = readPlayerSignature(setBy);
				OfflinePlayer victim = readPlayerSignature(target);
				double amount = subSect.getDouble(target + ".reward");
				boolean anonymous = subSect.getBoolean(target + ".anonymous");
				new PlayerBounty(victim, setter, amount, anonymous);
			}
		}
		for (String setBy : completedList.getKeys(false)) {
			ConfigurationSection subSect = completedList.getConfigurationSection(setBy);
			for (String target : subSect.getKeys(false)) {
				OfflinePlayer setter = readPlayerSignature(setBy);
				OfflinePlayer victim = readPlayerSignature(target);
				double amount = subSect.getDouble(target + ".reward");
				OfflinePlayer doneBy = readPlayerSignature(subSect.getString(target + ".doneBy"));
				new PlayerBounty(victim, setter, doneBy, amount);
			}
		}
	}

	public static double completeBountiesOn(OfflinePlayer target, OfflinePlayer doneBy) {
		double totalReward = 0.0D;
		for (int i = 0; i < active.size(); i++) {
			PlayerBounty bounty = (PlayerBounty) active.get(i);
			if (bounty.target.getUniqueId().equals(target.getUniqueId())) {
				totalReward += bounty.complete(doneBy);
				i--;
			}
		}
		return totalReward;
	}

	public static PlayerBounty getBounty(OfflinePlayer target, OfflinePlayer setBy) {
		for (PlayerBounty bounty : active) {
			if ((bounty.target.getUniqueId().equals(target.getUniqueId()))
					&& (bounty.setBy.getUniqueId().equals(setBy.getUniqueId()))) {
				return bounty;
			}
		}
		return null;
	}

	public static double getSumOn(OfflinePlayer target) {
		double totalReward = 0.0D;
		for (PlayerBounty bounty : active) {
			if (bounty.target.getUniqueId().equals(target.getUniqueId())) {
				totalReward += bounty.reward;
			}
		}
		return totalReward;
	}

	public static ArrayList<OfflinePlayer> removeBountiesBy(OfflinePlayer setBy) {
		ArrayList<OfflinePlayer> names = new ArrayList<OfflinePlayer>();
		for (int i = 0; i < completed.size(); i++) {
			PlayerBounty bounty = (PlayerBounty) completed.get(i);
			if (bounty.setBy.getUniqueId().equals(setBy.getUniqueId())) {
				names.add(bounty.remove());
				i--;
			}
		}
		return names;
	}

	public static ArrayList<PlayerBounty> removeBountiesDoneBy(OfflinePlayer doneBy) {
		ArrayList<PlayerBounty> names = new ArrayList<PlayerBounty>();
		for (int i = 0; i < completed.size(); i++) {
			PlayerBounty bounty = (PlayerBounty) completed.get(i);
			if (bounty.doneBy.getUniqueId().equals(doneBy.getUniqueId())) {
				names.add(bounty);
				bounty.remove();
				i--;
			}
		}
		return names;
	}

	public PlayerBounty(OfflinePlayer target, OfflinePlayer setBy, double reward, boolean anonymous) {
		this.target = target;
		this.setBy = setBy;
		this.reward = reward;
		active.add(this);
		this.anonymous = anonymous;
	}

	public PlayerBounty(Player target, Player setBy, double reward, boolean anonymous) {
		this((OfflinePlayer) target, (OfflinePlayer) setBy, reward, anonymous);
	}

	public PlayerBounty(OfflinePlayer target, OfflinePlayer setBy, OfflinePlayer doneBy, double reward) {
		this(target, setBy, reward, true);
		complete(doneBy);
	}

	private double complete(OfflinePlayer doneBy2) {
		this.doneBy = doneBy2;
		active.remove(this);
		completed.add(this);
		this.anonymous = true;
		return this.reward;
	}

	public double cancel() {
		active.remove(this);
		return Settings.getReturnAmount(this.reward);
	}

	private OfflinePlayer remove() {
		completed.remove(this);
		return this.target;
	}

	public double getReward() {
		return this.reward;
	}

	public boolean isAnonymous() {
		return this.anonymous;
	}

	public void addReward(double increment) {
		this.reward += increment;
	}

	public OfflinePlayer getSetBy() {
		return this.setBy;
	}

	public String getSetByPlayerName() {
		if (isAnonymous()) {
			return Settings.getColor("blank") + Settings.getAnonymousName();
		}
		return this.setBy.getName();
	}

	public OfflinePlayer getDoneBy() {
		return this.doneBy;
	}

	public OfflinePlayer getTarget() {
		return this.target;
	}
}

/*
 * Location: C:\Users\vikin_000\Desktop\Minecraft Plugin Development\Bounty.jar
 * 
 * Qualified Name: snorri.bounty.PlayerBounty
 * 
 * JD-Core Version: 0.7.0.1
 * 
 */