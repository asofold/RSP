package me.asofold.bpl.rsp.listeners;

import me.asofold.bpl.rsp.config.WorldSettings;
import me.asofold.bpl.rsp.core.Confinement;
import me.asofold.bpl.rsp.core.RSPCore;
import me.asofold.bpl.rsp.core.RSPError;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.TravelAgent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;


/**
 * TODO: on VECTOR ? 
 * TODO: get rid of unnecessary checks already here ?
 * TODO: split into several listeners, to be able to register only the needed ones.
 * @author mc_dev
 *
 */
public class RSPPlayerListener implements Listener{
	
	private final RSPCore core;
	private boolean useStats = true;
	
	public RSPPlayerListener(RSPCore core){
		this.core = core;
	}
	
	


	@EventHandler(priority=EventPriority.MONITOR)
	final void onPlayerChangedWorld(final PlayerChangedWorldEvent event) {
		// TODO: maybe remove !
		if ( useStats){
			final long ts = System.nanoTime();
			Player player = event.getPlayer();
			core.check(player.getName(), player.getLocation());
			RSPCore.stats.addStats(RSPCore.PLAYER_CHANGED_WORLD, System.nanoTime()-ts);
		} else{
			Player player = event.getPlayer();
			core.check(player.getName(), player.getLocation());	
		}
	}

	@EventHandler(priority=EventPriority.MONITOR)
	final void onPlayerJoin(final PlayerJoinEvent event) {
		// TODO: maybe switch to another way (check if needs deep re-check).
		// TODO: How about checking the bounds here?
		final Player player = event.getPlayer();
		if (useStats){
			final long ts = System.nanoTime();
			core.checkJoin(player.getName(), player.getLocation());
			RSPCore.stats.addStats(RSPCore.PLAYER_JOIN, System.nanoTime()-ts);
		} else{
			core.checkJoin(player.getName(), player.getLocation());
		}
	}

	@EventHandler(priority=EventPriority.MONITOR)
	final void onPlayerKick(final PlayerKickEvent event) {
		if ( event.isCancelled()) return;
		core.park(event.getPlayer().getName());
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	final void onPlayerQuit(final PlayerQuitEvent event) {
		core.park(event.getPlayer().getName());
	}

	@EventHandler(priority=EventPriority.MONITOR)
	final void onPlayerMove(final PlayerMoveEvent event) {
		if ( event.isCancelled()) return;
		final Location ref = event.isCancelled() ? event.getFrom() : event.getTo();
		if ( useStats){
			final long ts = System.nanoTime();
			core.check(event.getPlayer().getName(), ref);
			RSPCore.stats.addStats(RSPCore.PLAYER_MOVE, System.nanoTime()-ts);
		} else{
			core.check(event.getPlayer().getName(), ref);
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	final void onVehicleMove(final VehicleMoveEvent event){
		final Vehicle vehicle = event.getVehicle();
		final Entity entity = vehicle.getPassenger();
		if ( entity == null) return;
		if ( !(entity instanceof Player) ) return;
		if ( !core.isWithinBounds(entity.getLocation())){
			((Player) entity).leaveVehicle();
			vehicle.setPassenger(null);
			final Vector v = new Vector(0,0,0);
			vehicle.setVelocity(v);
			entity.setVelocity(v.clone());
		}
	}
	
	@EventHandler(priority=EventPriority.LOW)
	final void onVehicleEnterLow(final VehicleEnterEvent event){
		if ( event.isCancelled() ) return;
		if ( event.getEntered() instanceof Player){
			if ( !core.isWithinBounds(event.getVehicle().getLocation())){
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	final void onVehicleEnter(final VehicleEnterEvent event){
		if ( event.isCancelled() ) return;
		final Entity entity = event.getEntered();
		if ( entity instanceof Player){
			if ( useStats){
				final long ts = System.nanoTime();
				core.check(((Player) entity).getName(), event.getVehicle().getLocation());
				RSPCore.stats.addStats(RSPCore.VEHICLE_ENTER, System.nanoTime()-ts);
			} else{
				core.check(((Player) entity).getName(), event.getVehicle().getLocation());
			}
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	final void onVehicleExit(VehicleExitEvent event){
		if ( event.isCancelled() ) return;
		final Entity entity = event.getExited();
		if ( entity instanceof Player){
			if ( useStats){
				final long ts = System.nanoTime();
				core.check(((Player) entity).getName(), event.getVehicle().getLocation());
				RSPCore.stats.addStats(RSPCore.VEHICLE_EXIT, System.nanoTime()-ts);
			} else{
				core.check(((Player) entity).getName(), event.getVehicle().getLocation());
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOW)
	final void onPlayerPortalLow(final PlayerPortalEvent event) {
		if (event.isCancelled()) return;
		final Location to = event.getTo();
		if ( to == null ) return;
		WorldSettings sTo = core.getSettings(to.getWorld().getName());
		if (!sTo.confine) return;
		if (event.useTravelAgent()){
			try{
				// TODO: use getCreationRadius, getSearchRadius
				final double d = Confinement.distance(sTo, to);
				final TravelAgent ta = event.getPortalTravelAgent();
				final int sr = ta.getSearchRadius();
				if (d<sTo.cR-sr) return; // simply allow.
				else if (d>sTo.cR+sr){ // just deny.
					event.setCancelled(true);
					return;
				}
				Location target = ta.findPortal(to);
				if ( (target==null) && ta.getCanCreatePortal() && core.getCreatePortals()) target = ta.findOrCreate(to);
				if ( target != null){
					if ( !core.isWithinBounds(target) ) event.setCancelled(true);
				} else if (!Confinement.isWithinBounds(sTo,  to)) event.setCancelled(true);
			} catch (Throwable t){
				event.setCancelled(true);
			}
		}
		else if (!Confinement.isWithinBounds(sTo,  to)) event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.MONITOR)
	final void onPlayerPortal(final PlayerPortalEvent event) {
		// TODO: maybe this should be removed !
		if ( event.isCancelled()) return;
		if ( event.getTo() == null ) return;
		if ( useStats){
			long ts = System.nanoTime();
			core.check(event.getPlayer().getName(), event.getTo());
			RSPCore.stats.addStats(RSPCore.PLAYER_PORTAL, System.nanoTime()-ts);
		} else{
			core.check(event.getPlayer().getName(), event.getTo());
		}
	}

	@EventHandler(priority=EventPriority.MONITOR)
	final void onPlayerRespawn(final PlayerRespawnEvent event) {
		final Location loc = event.getRespawnLocation();
		if ( !core.isWithinBounds(loc)){
			Bukkit.getLogger().warning("[RSP] Player "+event.getPlayer().getName()+" respawns outside of boundaries or world "+loc.getWorld().getName()+"!");
		}
		if ( useStats){
			long ts = System.nanoTime();
			core.check(event.getPlayer().getName(), loc);
			RSPCore.stats.addStats(RSPCore.PLAYER_RESPAWN, System.nanoTime()-ts);
		} else{
			core.check(event.getPlayer().getName(), loc);
		}
	}
	
	@EventHandler(priority=EventPriority.LOW)
	final void onPlayerTeleportLow(final PlayerTeleportEvent event) {
		if ( event.isCancelled()) return;
		final Location to = event.getTo();
		if (to != null){
			if (!core.isWithinBounds(to)) event.setCancelled(true);
		}
//		else{
//			final Location from = event.getFrom();
//			if (from != null && !core.isWithinBounds(from)) event.setCancelled(true);
//		}
	}

	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
	final void onPlayerTeleport(final PlayerTeleportEvent event) {
		if ( event.isCancelled()) return;
		final Location to = event.getTo();
		if (to == null) return;
		if (useStats){
			final long ts = System.nanoTime();
			core.check(event.getPlayer().getName(), to);
			RSPCore.stats.addStats(RSPCore.PLAYER_TELEPORT, System.nanoTime()-ts);
		} else{
			core.check(event.getPlayer().getName(), to);
		}
	}
	
	public final void setUseStats(final boolean use){
		useStats = use;
	}
	
	public final boolean getUseStats(){
		return useStats;
	}
}
