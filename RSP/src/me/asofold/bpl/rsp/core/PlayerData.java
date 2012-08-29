package me.asofold.bpl.rsp.core;

import java.util.HashSet;
import java.util.Set;

import me.asofold.bpl.rsp.api.IPermissionUser;
import me.asofold.bpl.rsp.api.IRegionEnter;
import me.asofold.bpl.rsp.api.IRegionExit;
import me.asofold.bpl.rsp.config.PermDef;
import me.asofold.bpl.rsp.utils.BlockPos;

import org.bukkit.Bukkit;
import org.bukkit.Location;


/**
 * Runtime data for a player
 * @author mc_dev
 *
 */
public class PlayerData {
	public final String playerName;
	/**
	 * For future use (deny enter?).
	 */
	public Location lastValidLoc = null;
	public boolean isChecked = false;
	public long tsCache = 0;
	public BlockPos checkPos = null;
	
	/**
	 * Active rids for the user. Might contain regions for which the player has the ignore-perm.
	 * Used for quick diff check. (only consider changes, till reset)
	 */
	public final Set<Integer> idCache = new HashSet<Integer>();
	/**
	 * Groups to add. Used while check ONLY.
	 */
	public final Set<String> grpAdd = new HashSet<String>();
	/**
	 * Groups to remove. Used during check ONLY.
	 */
	public final Set<String> grpRem= new HashSet<String>();
	
//	private final Set<String> hasCache = new HashSet<String>();
//	private final Set<String> hasNotCache = new HashSet<String>();
	
	public PlayerData(String playerName){
		this.playerName = playerName;
	}

	/**
	 * Check if cache is expired and adjust internals to it.<br>
	 * This will delete permissions cache, but not check or remove cached ids.<br>
	 * @param dur
	 * @return if expired
	 */
	public final boolean checkCache(final long dur){
		final long ts =  System.currentTimeMillis();
		if (ts - tsCache > dur){
			onCacheExpire(ts);
			return true;
		}
		else return false;
	}
	
	/**
	 * 
	 * @param ts
	 */
	public final void onCacheExpire(long ts){
		tsCache = ts;
		checkPos = null; 
	}
	
	public final void clearCache(){
		clearCache(System.currentTimeMillis());
	}
	
	/**
	 * Really clear cache (also removes cached ids).
	 * @param ts
	 */
	public final void clearCache(final long ts) {
		onCacheExpire(ts);
		idCache.clear(); // TODO maybe not necessary
		isChecked = false;
	}
	
	/**
	 * Check def and perms and adjust permissions (only adding),
	 * @param user
	 * @param def
	 * @param id
	 * @return If any groups have been added (might or might not have effect on permissions later).
	 */
	public final boolean checkEnter(  IPermissionUser user, PermDefData data, Integer id ){
		idCache.add(id);
		boolean changed = false;
		// check remEnter
		for ( PermDef def : data.defRemEnter){
			if (def.ignorePermName != null){
				if (user.has( def.ignorePermName)) continue; // TODO: subject to policy
			}
			if (def.filterPermission != null){
				if ( !user.has( def.filterPermission)) continue;
			}
			grpRem.addAll(def.grpRemEnter);
			changed = true;
		}
		// check addEnter
		for ( PermDef def : data.defAddEnter){
			if ( def.ignorePermName != null ){
				if (user.has(def.ignorePermName )) continue;
			}
			if (def.filterPermission != null){
				if ( !user.has( def.filterPermission)) continue;
			}
			grpAdd.addAll(def.grpAddEnter);
			changed = true;
		}
		// check calls:
		for ( PermDef def : data.callOnEnter){
			for ( IRegionEnter call : def.callOnEnter){
				try{
					call.onRegionEnter(playerName, data.worldName, data.rid, def.defName);
				} catch(Throwable t){
					// TODO: add other info
					Bukkit.getServer().getLogger().severe("rsp - could not call on enter for permdef '"+def.defName+"': "+t.getMessage());
					t.printStackTrace();
					// TODO: maybe remove permdef
				}
			}
		}
		return changed;
	}

	/**
	 * Check perms for removing the def/id,
	 * does respect ignore perm.
	 * NOTE: DOES NOT CHECK IF ID IS PRESENT AT ALL.
	 * @param user
	 * @param def
	 * @param id
	 * @return If any groups have been added (might or might not have effect on permissions later).
	 */
	public final boolean checkExit(IPermissionUser user, PermDefData data, Integer id) {
		idCache.remove(id);
		boolean changed = false;
		// Check remExit:
		for ( PermDef def : data.defRemExit){
			if (def.ignorePermName != null){
				if (user.has( def.ignorePermName)) continue; // TODO: subject to policy
			}
			if (def.filterPermission != null){
				if ( !user.has( def.filterPermission)) continue;
			}
			grpRem.addAll(def.grpRemExit);
			changed = true;
		}
		// Check addExit:
		for ( PermDef def : data.defAddExit){
			if ( def.ignorePermName != null ){
				if (user.has(def.ignorePermName )) continue;
			}
			if (def.filterPermission != null){
				if ( !user.has( def.filterPermission)) continue;
			}
			grpAdd.addAll(def.grpAddExit);
			changed = true;
		}		
		// check calls:
		for ( PermDef def : data.callOnExit){
			for ( IRegionExit call : def.callOnExit){
				try{
					call.onRegionExit(playerName, data.worldName, data.rid, def.defName);
				} catch(Throwable t){
					// TODO: add other info
					Bukkit.getServer().getLogger().severe("rsp - could not call on exit for permdef '"+def.defName+"': "+t.getMessage());
					t.printStackTrace();
					// TODO: maybe remove permdef
				}
			}
		}
		
		return changed;
	}

	/**
	 * This is a light check for permission changes only (!), in order to keep the ids after cache expiration, provided they are still valid.
	 * @param user
	 * @param data
	 * @param id
	 * @return If any groups have been added (might or might not have effect on permissions later).
	 */
	public final boolean checkExpire(  IPermissionUser user, PermDefData data, Integer id ){
		boolean changed = false;
		for ( PermDef def : data.defAddEnter){
			if (def.ignorePermName != null){
				if (user.has( def.ignorePermName)){
					// no removing here
					continue; 
				}
			}
			if (def.filterPermission != null){
				if ( !user.has( def.filterPermission)){
					// The permission might have been removed.
					grpRem.addAll(def.grpAddEnter);
					changed = true;
					continue;
				}
			}
			grpAdd.addAll(def.grpAddEnter);
			changed = true;
		}
		return changed;
	}
	
}
