package me.asofold.bpl.rsp.api;

import java.util.UUID;

/**
 * All group changes are to be made with applyChanges and not with addGroup or removeGroup, unless it is impossible, then applyChanges should do nothing.<br>
 * So the order of calls usually should be prepare() - removeGroup(...), addGroup(...) - applyChanges().<br>
 * If removeGroup or addGroup are called after applyChanges or without prepare, TODO: throw exception, do nothing, apply directly.
 * @author mc_dev
 *
 */
public interface IPermissionUser {
	/**
	 * Is expected to create the user if not existent. This method should check superperms for RSP adds transient permissions  extra.
	 * Only called for players that are online or have been online recently.
	 * NOTE: Methods are expected to notify IPermissions instance about changes, so saving can be done from there.
	 * @param playerName
	 * @param perm
	 * @return
	 */
	public boolean has(String perm);
	
	/**
	 * Prepare to do group manipulations.<br>
	 * This should always re-fetch group information and cache it in a set or similar.
	 * @return If successful
	 */
	public boolean prepare();
	
	/**
	 * Apply group manipulations.
	 * @return If successful
	 */
	public boolean applyChanges();
	
	/**
	 * Discard group manipulations and internal cache - this is rather used to tell that after prepare nothing was done, useful to clear a group cache.
	 */
	public void discardChanges();
	
	/**
	 * Expected to only account for "top-level" groups, i.e. not inherited ones.
	 * @param group
	 * @return
	 */
	public boolean inGroup(String group);
	
	/**
	 * Expected to only account for "top-level" groups, i.e. not inherited ones.
	 * @param group
	 * @return If successful.
	 */
	public void addGroup(String group);
	
	/**
	 * Expected to only account for "top-level" groups, i.e. not inherited ones.
	 * @param group
	 * @return If successful.
	 */
	public void removeGroup(String group);
	

	public String getUserName();
	
	public UUID getUniqueId();

	public String getWorldName();
	
}
