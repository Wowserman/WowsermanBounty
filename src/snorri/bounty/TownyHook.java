package snorri.bounty;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.object.TownyUniverse;

public class TownyHook {

	private static Towny towny;

	public static Towny getTowny() {
		return towny;
	}
	
	public static TownyUniverse getTownyUniverse() {
		return towny.getTownyUniverse();
	}

	public static boolean initialHook() {
		Plugin plugin = Bukkit.getPluginManager().getPlugin("Essentials");
		if (plugin == null || plugin instanceof Towny == false)
			return false;
		towny = (Towny) plugin;
		return true;
	}
}
