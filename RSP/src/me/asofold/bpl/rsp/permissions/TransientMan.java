package me.asofold.bpl.rsp.permissions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
	 * Map lower case player names to Set of lower case group names.
	 */
	private final Map<String, Set<String>> inGroup = new HashMap<String, Set<String>>();
	
	private RSPCore core;
	
	public TransientMan(RSPCore core){
		this.core = core;
	}
	
	
	public Set<String> clear(){
		Set<String> changed = new HashSet<String>();
		return clear(changed);
	}
	
	/**
	 * Clear all and remove from players.
	 * @param changed Players with changed permissions.
	 * @param update if to recalculate permissions right away.
	 * @return Set with players whose permissions were changed.
	 */
	public Set<String> clear(Set<String> changed){
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
	public Set<String> removeGroup(String groupName, Set<String> changed, boolean update){
		String lcName = groupName.toLowerCase();
		TransientGroup group = groups.remove(lcName);
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
	public void removeGroupFromPlayers(TransientGroup group, Set<String> changed, boolean update) {
		for (String name : changed){
			removeGroupFromPlayer(name, group, changed, update);
		}
	}
	
	public void removeGroupFromPlayer(String name, TransientGroup group, Set<String> changed, boolean update) {
		String lcName = name.toLowerCase();
		Set<String> present = inGroup.get(lcName);
		if (present == null) return;
		if (present.remove(group.lcName)){
			changed.add(lcName);
			if (update) updatePlayer(lcName);
		}
	}
	
	public void updatePlayers(Set<String> changed) {
		for (String name : changed){
			updatePlayer(name);
		}
	}
	
	public void updatePlayer(String name){
		updatePlayer(name, false);
	}
	
	/**
	 * Reset permissions for this player. does recalculate.
	 * @param lcName
	 * @param recalculate
	 */
	public void updatePlayer(String name, boolean newAttachment) {
		String lcName = name.toLowerCase();
		Set<String> present = inGroup.get(lcName);
		if (present == null || present.isEmpty()){
			removePlayer(lcName);
			return;
		}
		Map<String, Boolean> permissions = getPermissions(present);
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
			Player player = Bukkit.getPlayerExact(lcName);
			if (player == null) return;
			attachment = player.addAttachment(core.getPlugin());
			attachements.put(lcName, attachment);
		}
		PermissionUtil.setPermissions(attachment, permissions, true);
	}

	/**
	 * @param groupNames
	 * @return
	 */
	public Map<String, Boolean> getPermissions(Set<String> groupNames){
		 Map<String, Boolean> permissions = new HashMap<String, Boolean>(); // number ?
		 for (String groupName : groupNames){
			 TransientGroup group = groups.get(groupName.toLowerCase());
			 if (group == null) continue;
			 permissions.putAll(group.permissions);
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

	public void addGroup(TransientGroup group, Set<String> changed, boolean update) {
		groups.put(group.lcName, group);
		for (String lcName : inGroup.keySet()){
			Set<String> present = inGroup.get(lcName);
			if (present.contains(group.lcName)){
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
	public boolean addGroupToPlayer(String playerName, String groupName, boolean update){
		String lcName = playerName.toLowerCase();
		Set<String> present = inGroup.get(lcName);
		if (present == null){
			present = new HashSet<String>();
			inGroup.put(lcName, present);
		}
		if (!present.add(groupName.toLowerCase())) return false; // already contained the group.
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
	public boolean removeGroupFromPlayer(String playerName, String groupName, boolean update){
		String lcName = playerName.toLowerCase();
		Set<String> present = inGroup.get(lcName);
		if (present == null) return false;
		if (!present.remove(groupName.toLowerCase())) return false; // already contained the group.
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
