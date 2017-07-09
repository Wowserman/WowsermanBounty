package snorri.bounty;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.PatternSyntaxException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.TownBlock;

public class BountyMenu implements Listener {
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerClickInventory(InventoryClickEvent event) {
		
		if (event.getInventory().getName().contains("Bounty List Page: ")==false)
			return;
		
		Player player = (Player) event.getWhoClicked();
		
		if (event.getCurrentItem() != null) {
			
			if (event.getCurrentItem().hasItemMeta()) {
				
				if (event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase("§eNext Page")) {
					BountyMenu.getBountyList(player, BountyMenu.getPageOfMenu(event.getInventory()) + 1);
				}
				
				else if (event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase("§eBack")) {
					BountyMenu.getBountyList(player, BountyMenu.getPageOfMenu(event.getInventory()) - 1);
				}
				
				else if (event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase("§eClose")) {
					player.closeInventory();
				}
			}
		}

		event.setCancelled(true);
	}

	private static final int[] slots = { 10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
	
	@SuppressWarnings("static-access")
	public static void getBountyList(Player player, int page) {
		Inventory tl = Bukkit.createInventory(player, 54, "Bounty List Page: " + page);

		List<PlayerBounty> bounties = PlayerBounty.getTopBounties(); // Gets Organized list of Player Bounties, organized based on reward.
		
		if (bounties.size() > 28 * page) {
			// Show Next Button
			ItemStack next = new ItemStack(Material.FEATHER, 1);
			ItemMeta m = next.getItemMeta();
			m.setDisplayName("§eNext Page");
			m.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			next.setItemMeta(m);
			tl.setItem(53, next);
		}
		
		if (page > 1) {
			// Show Back Button
			ItemStack back = new ItemStack(Material.FEATHER, 1);
			ItemMeta m = back.getItemMeta();
			m.setDisplayName("§eBack");
			m.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			back.setItemMeta(m);
			tl.setItem(45, back);
		}
		
		ItemStack close = new ItemStack(Material.BARRIER, 1);
		ItemMeta m = close.getItemMeta();
		m.setDisplayName("§eClose");
		m.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		close.setItemMeta(m);
		tl.setItem(49, close);
		
		int startIndex = bounties.size() - 28 > 0 ? bounties.size() - (page * 28) : 0;

		for (int i = 0; i < 28; i++) {
			if (bounties.size() > startIndex + i) {
				PlayerBounty bounty = bounties.get(startIndex + i);

				/*
				 * Wowserman: $1,000,000
				 * 
				 * Target is online, and was
				 * Last Seen in Berlin, Germany.
				 * Last Seen in the Wilderness.
				 * 
				 * Target is Offline.
				 * 
				 * Target has Never Played Before.
				 * 
				 */
				
				ItemStack skull = BountyEventListener.getSkullOf(bounty.getTarget().getName());
				ItemMeta meta = skull.getItemMeta();
				meta.setDisplayName("§a" + bounty.getTarget().getName() + " $" + NumberFormat.getNumberInstance(Locale.US).format(bounty.getReward()));
				List<String> lores = new ArrayList<String>();
				lores.add("");
				
				if (Bukkit.getPlayer(bounty.getTarget().getName())!=null && bounty.getTarget().hasPlayedBefore()) {
					Location location = bounty.getTarget().getPlayer().getLocation();
					TownBlock tb = TownyHook.getTownyUniverse().getTownBlock(location);
					lores.add("§aTarget is Online, and was");
					if (tb != null && tb.hasTown()) {
						try {
							lores.add("§aLast Seen in " + (tb.getTown().hasNation() ? tb.getTown().getName() + ", " + tb.getTown().getNation().getName():tb.getTown().getName() + ""));
						} catch (NotRegisteredException e) {
							lores.add("§aLast Seen in the Wilderness.");
						}
					} else {
						lores.add("§aLast Seen in the Wilderness.");
					}
				} else if (bounty.getTarget().hasPlayedBefore()==false) {
					lores.add("§cTarget has Never Played Before.");
				} else {
					lores.add("§cTarget is Offline.");
				}
				
				meta.setLore(lores);
				skull.setItemMeta(meta);
				tl.setItem(slots[i], skull);
			}
		}
		player.openInventory(tl);
	}
	
	public static int getPageOfMenu(Inventory menu) {
		try {
			return Integer.parseInt(menu.getName().split(" ")[3]);
		} catch (NumberFormatException e) {
			return 1;
		} catch (PatternSyntaxException e) {
			return 1;
		} catch (IndexOutOfBoundsException e) {
			return 1;
		}
	}
}
