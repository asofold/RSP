package me.asofold.bpl.rsp.api;

import org.bukkit.World;

import com.sk89q.worldguard.protection.ApplicableRegionSet;

/**
 * 
 * @author mc_dev
 *
 */
public interface ISetCheck {
	/**
	 * Gets called on check(), only if RegionManager.getApplicableRegions is called.<br>
	 * The signature is not set in stone, might get changed for LocalPlayer etc.
	 * @param name Player name
	 * @param set Region set the player gets checked with.
	 */
	public void onSetCheck(String name, World world, ApplicableRegionSet set);
}
