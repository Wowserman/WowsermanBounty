package snorri.bounty;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class BountyEventListener implements Listener {
	
	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		Player victim = event.getEntity();
		if (!Settings.enabledInWorld(victim.getWorld())) {
			return;
		}
		Player killer = victim.getKiller();
		if ((killer == null) || (!(killer instanceof Player)) || killer.equals(victim)) {
			return;
		}
		double reward = PlayerBounty.completeBountiesOn(victim, killer);
		Bounty.println("" + reward);
		if (reward > 0.0D) {
			Bounty.getEconomy().depositPlayer(killer, reward);
			killer.sendMessage(Settings.getColor("success") + LanguageSettings.getString("chat.compensated",
					victim.getName(), killer.getName(), Bounty.formatCurrency(reward)));
			Bounty.globalBroadcast(LanguageSettings.getString("broadcast.compensated", victim.getName(),
					killer.getName(), Bounty.formatCurrency(reward)));
			checkToRemoveBountiesDoneBy(killer);
			Bukkit.getPluginManager().callEvent(new BountyClaimEvent(killer, reward));
		}
	}

	@EventHandler
	public void onLogin(PlayerLoginEvent event) {
		Player player = event.getPlayer();
		// Bounty.updateScore(player);
		checkToRemoveBountiesBy(player);
	}

	private void checkToRemoveBountiesBy(Player player) {
		for (OfflinePlayer target : PlayerBounty.removeBountiesBy(player)) {
			player.sendMessage(Settings.getColor("success")
					+ LanguageSettings.getString("chat.completed", target.getName(), player.getName(), ""));
			awardHead(player, target);
		}
	}

	private void checkToRemoveBountiesDoneBy(Player player) {
		for (PlayerBounty bounty : PlayerBounty.removeBountiesDoneBy(player)) {
			awardHead(bounty.getSetBy().getPlayer(), bounty.getTarget());
			bounty.getSetBy().getPlayer().sendMessage(Settings.getColor("success") + LanguageSettings
					.getString("chat.completed", bounty.getTarget().getName(), bounty.getSetBy().getName(), ""));
		}
	}

	// TODO make sure this is legit code

	private void awardHead(Player player, OfflinePlayer victim) {
		if (Settings.shouldAwardHead())
			player.getInventory().addItem(getSkullOf(victim.getName()));
	}

	public static ItemStack getSkullOf(String victim) {
		ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
		SkullMeta meta = (SkullMeta) skull.getItemMeta();
		meta.setOwner(victim);
		if (Settings.shouldMarkSkulls()) {
			meta.setDisplayName(victim + " (BOUNTIED)");
		}
		skull.setItemMeta(meta);
		return skull;
	}
}

/*
 * Location: C:\Users\vikin_000\Desktop\Minecraft Plugin Development\Bounty.jar
 * 
 * Qualified Name: snorri.bounty.BountyEventListener
 * 
 * JD-Core Version: 0.7.0.1
 * 
 */