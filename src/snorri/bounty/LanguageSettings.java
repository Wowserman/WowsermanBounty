package snorri.bounty;

import org.bukkit.configuration.file.FileConfiguration;

public class LanguageSettings {
	private static FileConfiguration config;

	public static void setConfig(FileConfiguration c) {
		config = c;
	}

	public static void writeDefaultLanguageFile(FileConfiguration config) {
		config.set("chat.newPlayer1", "[WARNING] That player has not played on this server!");
		config.set("chat.newPlayer2", "If you really want to bounty them, use /bounty -o <player> <$$$>");
		config.set("chat.minimumBounty", "The minimum bounty on this server is $a");
		config.set("chat.outOfCash", "You do not have enough cash");
		config.set("chat.bountyPlaced", "A bounty has been placed on $t");
		config.set("broadcast.bountyPlaced", "$a bounty placed on $t by $s");
		config.set("chat.bountyIncreased", "Your bounty on $t has been increased");
		config.set("chat.activeHeader", "Active bounties:");
		config.set("chat.none", "NONE! Make one");
		config.set("chat.bountyList", "$a on $t by $s");
		config.set("chat.bountyOn", "The bounty on $t is $a");
		config.set("chat.bountyCancelled", "You have received $a for cancelling your bounty on $t");
		config.set("chat.compensated", "You have been compensated with $a for your work, assassin");
		config.set("broadcast.compensated", "$a reward has been claimed by $s for kiling $t");
		config.set("chat.completed", "Your bounty on $t has been completed");
		config.set("permissions.denied", "You do not have permission");
		config.set("permissions.anonymous", "Anonymous bounties are disabled on this server");
		setConfig(config);
	}

	public static String getString(String path, String targetName, String setByName, String formattedAmount) {
		System.out.print(config==null);
		String raw = config.getString(path);
		raw = raw.replace("$t", targetName);
		raw = raw.replace("$s", setByName);
		raw = raw.replace("$a", formattedAmount);
		return raw;
	}
}

/*
 * Location: C:\Users\vikin_000\Desktop\Minecraft Plugin Development\Bounty.jar
 * 
 * Qualified Name: snorri.bounty.LanguageSettings
 * 
 * JD-Core Version: 0.7.0.1
 * 
 */