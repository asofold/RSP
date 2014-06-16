package me.asofold.bpl.rsp.tasks;

import me.asofold.bpl.rsp.plshared.Players;

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
		// TODO: Switch to uuid ?
		final Player player = Players.getPlayerExact(playerName);
		if ( player != null ){
			player.setVelocity(new org.bukkit.util.Vector(0,0,0));
			player.teleport(loc, TeleportCause.PLUGIN);
		}
	}
}
