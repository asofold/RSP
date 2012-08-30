package me.asofold.bpl.rsp.permissions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import me.asofold.bpl.rsp.config.compatlayer.CompatConfig;
import me.asofold.bpl.rsp.core.RSPCore;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;


/**
 * TODO: change interfaces all to Set<String> changed as return value, but provide method without it.
 * TODO: decide if Set<String> makes sense at all ?
 * TODO: consider LinkedHashMaps for permissions ! (to preserve order).
 * Manager for transient permissions.<br>
 * <hr>
 * All changed players sets must be lower case names !
 * @author mc_dev
 *
 */
public class TransientMan {
	
	/**
	 * Map lower case group names to groups.
	 */
	private final Map<String, TransientGroup> groups = new HashMap<String, TransientGroup>(100);
	
	/**
	 * Map lower case player names to PermissionAttachments.
	 */
	private final Map<String, PermissionAttachment> attachements = new HashMap<String, PermissionAttachment>();
	
	/**
	 * Map lower case player names to Map of lower case group names mapping to priorities (Integer).
	 */
	private final Map<String, Map<String, Integer>> inGroup = new HashMap<String, Map<String, Integer>>();
	
	private final RSPCore core;
	
	public TransientMan(RSPCore core){
		this.core = core;
	}
	
	
	public final Set<String> clear(){
		final Set<String> changed = new HashSet<String>();
		return clear(changed);
	}
	
	/**
	 * Clear all and remove from players.
	 * @param changed Players with changed permissions.
	 * @param update if to recalculate permissions right away.
	 * @return Set with players whose permissions were changed.
	 */
	public final Set<String> clear(final Set<String> changed){
		for (PermissionAttachment attachment : attachements.values()){
			attachment.remove();
		}
		attachements.clear();
		groups.clear();
		inGroup.clear();
		return changed;
	}
	
	/**
	 * Remove from players and internals.
	 * @param groupName
	 * @param update if to recalculate permissions right away.
	 * @return
	 */
	public final Set<String> removeGroup(final String groupName, final Set<String> changed, final boolean update){
		final String lcName = groupName.toLowerCase();
		final TransientGroup group = groups.remove(lcName);
		if (group == null) return changed;
		removeGroupFromPlayers(group, changed, update);
		return changed;
	}
	
	/**
	 * 
	 * @param group
	 * @param changed
	 * @param update
	 */
	public final void removeGroupFromPlayers(final TransientGroup group, final Set<String> changed, final boolean update) {
		for (final String name : changed){
			removeGroupFromPlayer(name, group, changed, update);
		}
	}
	
	public final void removeGroupFromPlayer(final String name, final TransientGroup group, final Set<String> changed, final boolean update) {
		final String lcName = name.toLowerCase();
		final Map<String, Integer> present = inGroup.get(lcName);
		if (present == null) return;
		if (present.remove(group.lcName) != null){
			changed.add(lcName);
			if (update) updatePlayer(lcName);
		}
	}
	
	public final void updatePlayers(final Set<String> changed) {
		for (String name : changed){
			updatePlayer(name);
		}
	}
	
	public final void updatePlayer(final String name){
		updatePlayer(name, false);
	}
	
	/**
	 * Reset permissions for this player. does recalculate.
	 * @param lcName
	 * @param recalculate
	 */
	public final void updatePlayer(final String name, final boolean newAttachment) {
		final String lcName = name.toLowerCase();
		final Map<String, Integer> present = inGroup.get(lcName);
		if (present == null || present.isEmpty()){
			removePlayer(lcName);
			return;
		}
		final Map<String, Boolean> permissions = getPermissions(present);
		if (permissions.isEmpty()){
			removePlayer(lcName);
			return;
		}
		PermissionAttachment attachment = attachements.get(lcName);
		if (newAttachment && attachment != null){
			attachment.remove();
			attachment = null;
		}
		if (attachment == null){
			final Player player = Bukkit.getPlayerExact(lcName);
			if (player == null) return;
			attachment = player.addAttachment(core.getPlugin());
			attachements.put(lcName, attachment);
		}
		PermissionUtil.setPermissions(attachment, permissions, true);
	}

	/**
	 * @param groupNames Group name to priority.
	 * @return
	 */
	public final Map<String, Boolean> getPermissions(final Map<String, Integer> groupNames){
		final Map<String, Boolean> permissions = new HashMap<String, Boolean>(40); // number ?
		final PrioMap<String> prioPerms = new PrioMap<String>(40, 0.65f);
		// TODO: calculate effective permissons, including conflicts.
		for (final Entry<String, Integer> entry : groupNames.entrySet()){
			final String groupName = entry.getKey();
			final Integer prio = entry.getValue();
			
			final TransientGroup group = groups.get(groupName.toLowerCase());
			if (group == null) continue;
			
			for (final Entry<String, Boolean> permEntry : group.permissions.entrySet()){
				final String perm = permEntry.getKey();
				final Boolean has = permEntry.getValue();
				if (has) prioPerms.updateAdd(perm, prio);
				else prioPerms.updateRem(perm, prio);
			}
		}
		for (final Entry<String, PrioEntry> prioEntry : prioPerms.entrySet()){
			if (prioEntry.getValue().isAdd()) permissions.put(prioEntry.getKey(), true);
			else permissions.put(prioEntry.getKey(), false);
		}
		return permissions;
	}

	public void removePlayer(String playerName){
		String lcName = playerName.toLowerCase();
		inGroup.remove(lcName);
		PermissionAttachment attachment = attachements.remove(lcName);
		if (attachment == null) return;
		attachment.remove();
	}

	/**
	 * Add groups from config. 
	 * @param cfg
	 */
	public void fromConfig(CompatConfig cfg){
		String path = "transient-groups";
		List<String> keys = cfg.getStringKeys(path);
		Set<String> changed = new HashSet<String>();
		for (String key : keys){
			TransientGroup group = TransientGroup.fromConfig(cfg,  path + "." + key, key);
			if (group == null) continue;
			addGroup(group, changed, false);
		}
		updatePlayers(changed);
	}

	public final void addGroup(final TransientGroup group, final Set<String> changed, final boolean update) {
		groups.put(group.lcName, group);
		for (final String lcName : inGroup.keySet()){
			final Map<String, Integer> present = inGroup.get(lcName);
			if (present.containsKey(group.lcName)){
				changed.add(lcName);
				if (update) updatePlayer(lcName);
			}
		}
	}
	
	/**
	 * 
	 * @param playerName
	 * @param groupName
	 * @param update
	 * @param recalculate
	 * @return If changed, i.e. group was not present.
	 */
	public final boolean addGroupToPlayer(final String playerName, final String groupName, final int priority, final boolean update){
		final String lcName = playerName.toLowerCase();
		Map<String, Integer> present = inGroup.get(lcName);
		if (present == null){
			present = new HashMap<String, Integer>();
			inGroup.put(lcName, present);
		}
		
		final String lcGroup = groupName.toLowerCase();
		final Integer oldPrio = present.get(lcGroup);
		
		if (oldPrio == null) present.put(lcGroup, priority);
		else if (priority <= oldPrio.intValue()) return false;
		else present.put(lcGroup, priority);
		
		if (update) updatePlayer(lcName);
		return true;
	}
	
	/**
	 * 
	 * @param playerName
	 * @param groupName
	 * @param update
	 * @param recalculate
	 * @return If changed, i.e. the group was present.
	 */
	public final boolean removeGroupFromPlayer(final String playerName, final String groupName, final boolean update){
		final String lcName = playerName.toLowerCase();
		final Map<String, Integer> present = inGroup.get(lcName);
		if (present == null) return false;
		if (present.remove(groupName.toLowerCase()) == null) return false; // already contained the group.
		if (present.isEmpty()){
			removePlayer(lcName);
			return true;
		}
		if (update) updatePlayer(lcName);
		return true;
	}
	
	public final boolean isTransient(String groupName){
		return groups.containsKey(groupName.toLowerCase());
	}
}
