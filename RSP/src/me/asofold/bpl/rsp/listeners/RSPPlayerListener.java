package me.asofold.bpl.rsp.listeners;

import me.asofold.bpl.rsp.config.WorldSettings;
import me.asofold.bpl.rsp.core.Confinement;
import me.asofold.bpl.rsp.core.RSPCore;

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
import org.bukkit.event.player.PlayerLoginEvent;
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
		// (Must have).
		final long ts = useStats ? System.nanoTime() : 0L; 
		Player player = event.getPlayer();
		core.check(player.getName(), player.getLocation());
		if ( useStats){
			RSPCore.stats.addStats(RSPCore.PLAYER_CHANGED_WORLD, System.nanoTime()-ts);
		}
	}
	
	@EventHandler(priority=EventPriority.LOW)
	final void onPlayerLogin(final PlayerLoginEvent event) {
		// TODO: maybe switch to another way (check if needs deep re-check).
		// TODO: How about checking the bounds here?
		final long ts = useStats ? System.nanoTime() : 0L;
		final Player player = event.getPlayer();
		core.checkJoin(player.getName(), player.getLocation(), false);
		if (useStats){
			RSPCore.stats.addStats(RSPCore.PLAYER_LOGIN, System.nanoTime()-ts);
		}
	}

	@EventHandler(priority=EventPriority.MONITOR)
	final void onPlayerJoin(final PlayerJoinEvent event) {
		// TODO: maybe switch to another way (check if needs deep re-check).
		// TODO: How about checking the bounds here?
		final long ts = useStats ? System.nanoTime() : 0L;
		final Player player = event.getPlayer();
		core.checkJoin(player.getName(), player.getLocation(), true);
		if (useStats){
			RSPCore.stats.addStats(RSPCore.PLAYER_JOIN, System.nanoTime()-ts);
		}
	}

	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
	final void onPlayerKick(final PlayerKickEvent event) {
		core.park(event.getPlayer().getName());
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	final void onPlayerQuit(final PlayerQuitEvent event) {
		core.park(event.getPlayer().getName());
	}

	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = false)
	final void onPlayerMove(final PlayerMoveEvent event) {
		final long ts = useStats ? System.nanoTime() : 0L;
		final Location ref = event.isCancelled() ? event.getFrom() : event.getTo();
		core.check(event.getPlayer().getName(), ref);
		if ( useStats){
			RSPCore.stats.addStats(RSPCore.PLAYER_MOVE, System.nanoTime()-ts);
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST, ignoreCancelled = true)
	final void onVehicleMove(final VehicleMoveEvent event){
		final Vehicle vehicle = event.getVehicle();
		final Entity entity = vehicle.getPassenger();
		if (entity == null) return;
		if (!(entity instanceof Player)) return;
		if (!core.isWithinBounds(entity.getLocation())){
			((Player) entity).leaveVehicle();
			vehicle.setPassenger(null);
			final Vector v = new Vector(0,0,0);
			vehicle.setVelocity(v);
			entity.setVelocity(v.clone());
		}
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled = true)
	final void onVehicleEnterLow(final VehicleEnterEvent event){
		if (event.getEntered() instanceof Player){
			if ( !core.isWithinBounds(event.getVehicle().getLocation())){
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
	final void onVehicleEnter(final VehicleEnterEvent event){
		final Entity entity = event.getEntered();
		if ( entity instanceof Player){
			final long ts = useStats ? System.nanoTime() : 0L;
			core.check(((Player) entity).getName(), event.getVehicle().getLocation());
			if ( useStats){
				RSPCore.stats.addStats(RSPCore.VEHICLE_ENTER, System.nanoTime()-ts);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
	final void onVehicleExit(VehicleExitEvent event){
		final Entity entity = event.getExited();
		if ( entity instanceof Player){
			final long ts = useStats ? System.nanoTime() : 0L;
			core.check(((Player) entity).getName(), event.getVehicle().getLocation());
			if ( useStats){
				RSPCore.stats.addStats(RSPCore.VEHICLE_EXIT, System.nanoTime()-ts);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled = true)
	final void onPlayerPortalLow(final PlayerPortalEvent event) {
		final Location to = event.getTo();
		if (to == null ) return;
		final WorldSettings sTo = core.getSettings(to.getWorld().getName());
		if (!sTo.confine) return;
		if (event.useTravelAgent()){
			try{
				// TODO: use getCreationRadius, getSearchRadius
				final double d = Confinement.distance(sTo, to);
				final TravelAgent ta = event.getPortalTravelAgent();
				final int sr = ta.getSearchRadius();
				if (d < sTo.cR - sr) return; // simply allow.
				else if (d > sTo.cR + sr){ // just deny.
					event.setCancelled(true);
					return;
				}
				Location target = ta.findPortal(to);
				if ((target == null) && ta.getCanCreatePortal() && core.getCreatePortals()) target = ta.findOrCreate(to);
				if (target != null){
					if (!core.isWithinBounds(target)) event.setCancelled(true);
				} else if (!Confinement.isWithinBounds(sTo, to)) event.setCancelled(true);
			} catch (Throwable t){
				event.setCancelled(true);
			}
		}
		else if (!Confinement.isWithinBounds(sTo,  to)) event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
	final void onPlayerPortal(final PlayerPortalEvent event) {
		// TODO: maybe this should be removed ! <- which "this" ?
		final long ts = useStats ? System.nanoTime() : 0L;
		final Location to = event.getTo();
		if (to == null) return;
		core.checkAndCheckDelayed(event.getPlayer().getName(), to);
		if (useStats){
			RSPCore.stats.addStats(RSPCore.PLAYER_PORTAL, System.nanoTime()-ts);
		}
	}

	@EventHandler(priority=EventPriority.MONITOR)
	final void onPlayerRespawn(final PlayerRespawnEvent event) {
		final long ts = useStats ? System.nanoTime() : 0L;
		final Location loc = event.getRespawnLocation();
		if ( !core.isWithinBounds(loc)){
			Bukkit.getLogger().warning("[RSP] Player "+event.getPlayer().getName()+" respawns outside of boundaries or world "+loc.getWorld().getName()+"!");
		}
		core.checkAndCheckDelayed(event.getPlayer().getName(), loc);
		if ( useStats){
			RSPCore.stats.addStats(RSPCore.PLAYER_RESPAWN, System.nanoTime()-ts);
		}
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled = true)
	final void onPlayerTeleportLow(final PlayerTeleportEvent event) {
		final Location to = event.getTo();
		if (to != null){
			if (!core.isWithinBounds(to)) event.setCancelled(true);
		}
		// (If checking from, also teleport soon.)
//		else{
//			final Location from = event.getFrom();
//			if (from != null && !core.isWithinBounds(from)) event.setCancelled(true);
//		}
	}

	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
	final void onPlayerTeleport(final PlayerTeleportEvent event) {
		final long ts = useStats ? System.nanoTime() : 0L;
		final Location to = event.getTo();
		if (to == null) return;
		core.checkAndCheckDelayed(event.getPlayer().getName(), to);
		if (useStats) {
			RSPCore.stats.addStats(RSPCore.PLAYER_TELEPORT, System.nanoTime() - ts);
		}
	}
	
	public void setUseStats(final boolean use){
		useStats = use;
	}
	
	public boolean getUseStats(){
		return useStats;
	}
}
