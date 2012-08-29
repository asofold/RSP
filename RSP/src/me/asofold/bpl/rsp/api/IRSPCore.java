package me.asofold.bpl.rsp.api;

import java.util.Collection;

import me.asofold.bpl.rsp.config.ConfigPermDef;

import org.bukkit.Location;


/**
 * Public interface for accessing public methods.
 * TODO: a) Make minimal ! b) adjust comments to new config (simple methods=enter+add, leave+remove)
 * TODO: cleanup methods to most simple design.
 * TODO: add additional methods with lifetime enum (like type: till reload, forever(saved´) )
 * @author mc_dev
 *
 */
public interface IRSPCore {
	// --------------------------------------------
	// NEW API
	
	/**
	 * Remove PermDef with that name.
	 * This should be the standard remove method.
	 * @param defName
	 * @return If a PermDef with that name existed at all.
	 */
	public boolean removePermDef(String defName);
	
	/**
	 * Add a permdef to refer to.
	 * @param defName
	 * @return True if a PermDef with the same name has been replaced.
	 */
	public boolean  addPermDef(ConfigPermDef permDef);
	
	/**
	 * Assign an already registered PermDef to a region.
	 * NOTE: Does not insist on the world to exist (!).
	 * @param defName
	 * @param use
	 * @param worldName
	 * @param rid
	 */
	public void linkPermDef(String defName, String worldName, String rid);
	
	/**
	 * Simple check if a PermDef with a certain name has been registered.
	 * @param defName
	 * @return
	 */
	public boolean hasPermDef( String defName);
	
	/**
	 * Remove PermDef from context (world, region), the permDef will not be deleted.
	 * @param defName
	 * @param worldName
	 * @param rid
	 * @return
	 */
	public boolean unlinkPermDef(String defName, String worldName, String rid);
	
	/**
	 * Add a factory for IPermissions.
	 * This will be used amongst other possible ones - last registered comes first,
	 * though factories returning null or empty sets for pluginHookNames will be checked after non-empty sets.
	 * @param factory
	 */
	public void addPermissionsFactory( IPermissionsFactory factory);
	
	/**
	 * Convenience method to get the names of all players that are in a certain region.<br>
	 * NOTE 1: Only covers players that have permission for a linked permdef for that region (not all players / all regions).<br>
	 * NOTE 2: Due to lazy-dist and other settings, this may not be 100% precise.<br>
	 * NOTE 3: This might get adjusted to be "exact" at some point.<br>
	 * @param worldName
	 * @param rid
	 * @return
	 */
	public Collection<String> getPlayersInRegion(String worldName, String rid);
	
	/**
	 * Check if a location is within bounds for a world.
	 * @param loc
	 * @return Will return true, if there is no confinement for a world.
	 */
	public boolean isWithinBounds(Location loc);

}
