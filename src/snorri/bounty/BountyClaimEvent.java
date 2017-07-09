package snorri.bounty;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BountyClaimEvent extends Event {
	
	private static final HandlerList handlers = new HandlerList();
	private Player killer;
	private double reward;
	
	public Player getKiller() {
		return killer;
	}
	public double getReward() {
		return reward;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
	    return handlers;
	}
	
	public BountyClaimEvent(Player killer, double reward) {
		this.killer = killer;
		this.reward = reward;
	}
}