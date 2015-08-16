package me.asofold.bpl.rsp.api.permissions;

import java.util.Collection;
import java.util.Set;

/**
 * Simple interface for keeping track of group changes, for simple and efficient
 * adding and removing of groups.
 * 
 * @author dev7u1
 *
 */
public interface GroupCache {

	/**
	 * Indicate if any data has been added and if calls can be made. Set on
	 * adding/removing anything.
	 * 
	 * @return
	 */
	public boolean isPrepared();

	/**
	 * Store a single group that the player is member of.
	 * 
	 * @param groupName
	 */
	public void addPresentGroup(String groupName);

	/**
	 * Store groups that the player currently is member of. Already contained
	 * data should not be cleared.
	 * 
	 * @param groupNames
	 */
	public void addPresentGroups(Collection<String> groupNames);

	/**
	 * Test if the group name is present, not taking any pending changes into
	 * account.
	 * 
	 * @param groupName
	 */
	public boolean isGroupPresent(String groupName);

	/**
	 * Set this group to be added (unless already contained).
	 * 
	 * @param groupName
	 */
	public void addGroup(String groupName);

	/**
	 * Set this group to be removed (if already contained or previously added).
	 * 
	 * @param groupName
	 */
	public void removeGroup(String groupName);

	/**
	 * Get the groups to add. This may be an internally stored set and should
	 * only be altered, if the resulting state is acceptable, e.g. before
	 * calling clear anyway.
	 * 
	 * @return
	 */
	public Set<String> getGroupsToAdd();

	/**
	 * Get the groups to remove. This may be an internally stored set and should
	 * only be altered, if the resulting state is acceptable, e.g. before
	 * calling clear anyway.
	 * 
	 * @return
	 */
	public Set<String> getGroupsToRemove();
	
	/**
	 * Quick test if any groups are set to be added or to be removed.
	 * 
	 * @return
	 */
	public boolean hasChangesPending();

	/**
	 * Call after using (and before, in case of runtime errors).
	 */
	public void clear();

}
