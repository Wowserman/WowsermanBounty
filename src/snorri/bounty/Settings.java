package snorri.bounty;

import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class Settings {

	// TODO: add a bounty "price" or "tax" similar to cancel penalty

	private static double minBounty = 50.0D;
	private static int listLength = 5;
	private static double cancelPenalty = 0.25D;
	private static double awardHeadChance = 1.0D;
	private static boolean markSkulls = true;
	private static boolean globalBroadcast = true;
	private static boolean allowAnonymous = true;
	private static String anonymousName = "anonymous";
	private static boolean enableInAllWorlds = true;
	private static List<String> enabledWorlds = new ArrayList<String>();
	private static HashMap<String, String> colors = new HashMap<String, String>();

	static {
		enabledWorlds.add("list worlds here if 'enableInAllWorlds' is false");
		colors.put("success", ChatColor.GREEN.toString());
		colors.put("failure", ChatColor.RED.toString());
		colors.put("global", ChatColor.GOLD.toString());
		colors.put("blank", ChatColor.GRAY.toString());
	}

	public static boolean shouldAwardHead() {
		return Math.random() < awardHeadChance;
	}

	public static double getMin() {
		return minBounty;
	}

	public static int getListLength() {
		return listLength;
	}

	public static boolean allowAnonymous() {
		return allowAnonymous;
	}

	public static boolean enabledInWorld(World world) {
		return (enableInAllWorlds) || (enabledWorlds.contains(world.getName()));
	}

	public static boolean shouldMarkSkulls() {
		return markSkulls;
	}

	public static boolean shouldBroadcast() {
		return globalBroadcast;
	}

	public static double getReturnAmount(double bountyCost) {
		return (1.0D - cancelPenalty) * bountyCost;
	}

	public static String getColor(String purpose) {
		return (String) colors.get(purpose);
	}

	public static String getAnonymousName() {
		return anonymousName;
	}

	public static void readFromConfig(FileConfiguration config) {
		
		minBounty = config.getDouble("minBounty");
		listLength = config.getInt("listLength");
		cancelPenalty = config.getDouble("cancelPenalty");
		awardHeadChance = config.getDouble("awardHeadChance");
		markSkulls = config.getBoolean("markSkulls");
		globalBroadcast = config.getBoolean("globalBroadcast");
		allowAnonymous = config.getBoolean("allowAnonymous");
		anonymousName = config.getString("anonymousName");
		enableInAllWorlds = config.getBoolean("enableInAllWorlds");
		enabledWorlds = config.getStringList("enabledWorlds");
		if (!enableInAllWorlds) {
			Bounty.println("Enabled in worlds: " + Joiner.on(", ").join(enabledWorlds));
		}
		ConfigurationSection colors = config.createSection("colors");
		for (String key : colors.getKeys(false)) {
			((HashMap) colors).put(key, colors.getString(key));
		}
	}

	public static void writeToConfig(FileConfiguration config) {
		config.set("minBounty", Double.valueOf(minBounty));
		config.set("listLength", listLength);
		config.set("cancelPenalty", Double.valueOf(cancelPenalty));
		config.set("awardHeadChance", Double.valueOf(awardHeadChance));
		config.set("markSkulls", Boolean.valueOf(markSkulls));
		config.set("globalBroadcast", Boolean.valueOf(globalBroadcast));
		config.set("allowAnonymous", Boolean.valueOf(allowAnonymous));
		config.set("anonymousName", anonymousName);
		config.set("enableInAllWorlds", Boolean.valueOf(enableInAllWorlds));
		config.set("enabledWorlds", enabledWorlds);
		for (String key : colors.keySet()) {
			config.set("colors." + key, colors.get(key));
		}
	}
}

/*
 * Location: C:\Users\vikin_000\Desktop\Minecraft Plugin Development\Bounty.jar
 * 
 * Qualified Name: snorri.bounty.Settings
 * 
 * JD-Core Version: 0.7.0.1
 * 
 */