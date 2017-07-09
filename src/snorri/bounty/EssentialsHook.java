package snorri.bounty;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import com.earth2me.essentials.Essentials;

public class EssentialsHook {

	private static Essentials essentails;

	public static Essentials getEssentails() {
		return essentails;
	}

	public static boolean initialHook() {
		Plugin plugin = Bukkit.getPluginManager().getPlugin("Essentials");
		if (plugin == null || plugin instanceof Essentials == false)
			return false;
		essentails = (Essentials) plugin;
		return true;
	}
}
