package me.asofold.bpl.rsp.permissions;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.asofold.bpl.rsp.RSP;
import me.asofold.bpl.rsp.api.IPermissionUser;
import me.asofold.bpl.rsp.config.compatlayer.CompatConfig;
import me.asofold.bpl.rsp.core.RSPCore;
import me.asofold.bpl.rsp.utils.Utils;

import org.bukkit.permissions.PermissionAttachment;


public class PermissionUtil {
	/**
	 * Groups to be added will not be removed (!). does removing first.
	 * @param user
	 * @param add
	 * @param remove
	 * @param clear Clear sets after processing.
	 * @return  If the user was changed.
	 */
	public static final boolean changeGroups(final String playerName, final TransientMan trMan, final IPermissionUser user, 
			final PrioMap<String> groups, final boolean clear, final boolean prepare){
		// final Set<String> add, final Set<String> remove
		boolean changed = false;
		boolean trChanged = false;
		if (prepare && !user.prepare()){
			// TODO: log ?
		}
		
		for (final Entry<String, PrioEntry> entry : groups.entrySet()){
			final PrioEntry prios = entry.getValue();
			if (prios.isEmpty()){
				// Technically this might be impossible.
				// TODO: policy
				continue; 
			}
			final boolean isAdd = prios.isAdd();
			final String grp = entry.getKey();
			if (trMan.isTransient(grp)){
				if (isAdd){
					if (trMan.addGroupToPlayer(playerName, grp, prios.prioAdd, false)) trChanged = true;
				}
				else if (trMan.removeGroupFromPlayer(playerName, grp, false)) trChanged = true;
			}
			else{
				if (isAdd){
					if (!user.inGroup(grp)){
						user.addGroup(grp);
						changed = true;
					}
				}
				else if (user.inGroup(grp)){
					user.removeGroup(grp);
					changed = true;
				}
			}
		}
		
//		for ( final String grp : remove){
//			if (add.contains(grp)) continue;
//			if (trMan.isTransient(grp)){
//				if (trMan.removeGroupFromPlayer(playerName, grp, false)) trChanged = true;
//			}
//			else if (user.inGroup(grp)){
//				user.removeGroup(grp);
//						// ((RSPCore) RSP.getRSPCore()).onRemoveFailure(user.getUserName(), user.getWorldName(), grp);
//				changed = true;
//			}
//		}
//		for (final String grp : add){
//			if (trMan.isTransient(grp)){
//				if (trMan.addGroupToPlayer(playerName, grp, false)) trChanged = true;
//			}
//			else 
//			if (!user.inGroup(grp)){
//				user.addGroup(grp);
//				changed = true;
//			}
//		}
		if (trChanged) trMan.updatePlayer(playerName);
		if (!prepare){
			// No calls to user.
		}
		else if (changed){
			if (!user.applyChanges()){
				((RSPCore) RSP.getRSPCore()).onGroupChangeFailure(user.getUserName(), user.getWorldName());
			}
		}
		else{
			user.discardChanges();
		}
		if (clear){
//			add.clear();
//			remove.clear();
			groups.clear();
		}
		return changed;
	}
	
	/**
	 * Read permissions into map (PEX-style).
	 * @param cfg
	 * @param path
	 * @param map
	 */
	public static void readPermissions(CompatConfig cfg, String path, Map<String, Boolean> map){
		List<String> keys = cfg.getStringList(path, null);
		if (keys == null) return;
		for (String key : keys){
			key = key.trim();
			boolean state = true;
			if (key.startsWith("-")){
				state = false;
				key = key.substring(1);
			}
			if (key.isEmpty()) continue;
			if (isValidPermissionName(key)){
				Utils.warn("[RSP] Ignore permission with bad name: " + key);
				continue;
			}
			map.put(key, state);
		}
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public static boolean isValidPermissionName(String perm) {
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	* HACK to get permissions set at once.
	* <hr>
	* (Adapted from PermissionZones by EvenPrime, see: GitHub.)
	*
	* @param attachment
	* @param permissions
	*/
    public static final void setPermissions(final PermissionAttachment attachment, final Map<String, Boolean> permissions, final boolean recalculate) {
        try {
        	final Field field = PermissionAttachment.class.getDeclaredField("permissions");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            final Map<String, Boolean> presentPerms = (Map<String, Boolean>) field.get(attachment);
            presentPerms.clear();
            presentPerms.putAll(permissions);
            if (recalculate) attachment.getPermissible().recalculatePermissions();
        } catch(Throwable t) {
            Utils.warn("[RSP] Fall back to single permission adding - adding transient permissions failed - caused by:");
            t.printStackTrace();
            for (final String perm : attachment.getPermissions().keySet()) {
                attachment.unsetPermission(perm);
            }
            for (final Entry<String, Boolean> entry : permissions.entrySet()) {
                attachment.setPermission(entry.getKey(), entry.getValue());
            }
        }
    }
	
}
