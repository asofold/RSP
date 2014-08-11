package me.asofold.bpl.rsp.api.regions;

import java.util.UUID;

import org.bukkit.World;

/**
 * Whenever RSPCore.check() has run and regions have been checked, these hooks get called.
 * @author dev1mc
 *
 */
public interface IRegionResultHook {
	
	/**
	 * 
	 * @param playerName
	 * @param id
	 * @param world
	 * @param regionResult Not for storage, as control should be with the caller.
	 */
	public void onRegionResult(String playerName, UUID id, World world, RegionResult regionResult);
	
}
