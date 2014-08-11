package me.asofold.bpl.rsp.api.regions;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;

public interface IRegions {
	
	// TODO: Consider: boolean isAvailable();
	
	/**
	 * Set result according to the player at the location.
	 * @param loc
	 * @param playerName
	 * @param uuid
	 * @param result Not to be stored, control of the object should be with the caller, once set. Calling result.reset() here is not intended.
	 * @return If anything was changed.
	 */
	public boolean setRegionResult(Location loc, String playerName, UUID uuid, RegionResult result);
	
	/**
	 * For statistics only.
	 * @param world
	 */
	public int getRegionCount(World world);
	
}
