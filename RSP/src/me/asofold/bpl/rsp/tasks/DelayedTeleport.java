package me.asofold.bpl.rsp.tasks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

/**
 * Task for setting velocity to 0 and teleporting the player to the desired destination.
 * @author mc_dev
 *
 */
public final class DelayedTeleport implements Runnable {
	private final Location loc;
	private final String playerName;

	public DelayedTeleport(final String playerName, final Location loc){
		this.playerName = playerName;
		this.loc = loc;
	}

	@Override
	public void run() {
		final Player player = Bukkit.getServer().getPlayer(playerName);
		if ( player != null ){
			player.setVelocity(new org.bukkit.util.Vector(0,0,0));
			player.teleport(loc, TeleportCause.PLUGIN);
		}
	}
}
