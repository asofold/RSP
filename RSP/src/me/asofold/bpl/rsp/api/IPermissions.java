package me.asofold.bpl.rsp.api;

/**
 * These methods are meant to be fast.
 * Initial checks will be done using isAvailable.
 * NOTE: IPermissionUser instances are expected to notify the instance of IPermissions about changes, for forceSave and scheduleSave to take effect !
 * (more methods to be added, for cumulative or scheduled saving, for instance)
 * @author mc_dev
 *
 */
public interface IPermissions {
	
	/**
	 * Check if this can be used at all.
	 * @return
	 */
	public boolean isAvailable();
	
	/**
	 * Get a user for a specific world, to manipulate world specific permissions.
	 * NOTE: With some permission plugins such as PEX, the worldName will be ignored, because groups already carry the world information.
	 * @param playerName
	 * @param worldName
	 * @return
	 */
	public IPermissionUser getUser(String player, String world);
	
	
	/**
	 * Is more or less expected to be a forced save.
	 * Might be called for scheduled saves (now and then) as well as during plugin shutdown.
	 * This should only save if changes have been made, though.
	 * For PEX users will be saved, for bPermissions worlds.
	 */
	public void saveChanges();
	
	/**
	 * More like a unique identifier, used in logs, should probably include the version.
	 * @return
	 */
	public String getInterfaceName();

}
