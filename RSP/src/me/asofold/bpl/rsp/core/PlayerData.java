package me.asofold.bpl.rsp.core;

import java.util.HashSet;
import java.util.Set;

import me.asofold.bpl.rsp.api.IPermissionUser;
import me.asofold.bpl.rsp.api.IRegionEnter;
import me.asofold.bpl.rsp.api.IRegionExit;
import me.asofold.bpl.rsp.config.PermDef;
import me.asofold.bpl.rsp.permissions.PrioMap;
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
	public final DelayedCheckTask checkTask;
	/**
	 * For future use (deny enter?).
	 */
	public Location lastValidLoc = null;
	public boolean isChecked = false;
	public long tsCache = 0;
	public BlockPos checkPos = null;
	
	/** Minimum lazy dist for all permdefs, updated externally (!). */
	public int minLazyDist = Integer.MAX_VALUE;
	
	/**
	 * Active rids for the user. Might contain regions for which the player has the ignore-perm.
	 * Used for quick diff check. (only consider changes, till reset)
	 */
	public final Set<Integer> idCache = new HashSet<Integer>();
	
	/**
	 * For group mnanipulations.
	 */
	public final PrioMap<String> groups = new PrioMap<String>(8, 0.3f );
	
//	/**
//	 * Groups to add. Used while check ONLY.
//	 */
//	public final Set<String> grpAdd = new HashSet<String>();
//	/**
//	 * Groups to remove. Used during check ONLY.
//	 */
//	public final Set<String> grpRem= new HashSet<String>();
	
	public PlayerData(final String playerName){
		this.playerName = playerName;
		this.checkTask = new DelayedCheckTask(playerName);
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
	public final void onCacheExpire(final long ts){
		tsCache = ts;
		checkPos = null;
		minLazyDist = Integer.MAX_VALUE;
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
	public final boolean checkEnter(final IPermissionUser user, final PermDefData data, final Integer id ){
		idCache.add(id);
		boolean changed = false;
		// check remEnter
		for (final PermDef def : data.defRemEnter){
			if (def.ignorePermName != null){
				if (user.has( def.ignorePermName)) continue; // TODO: subject to policy
			}
			if (def.filterPermission != null){
				if ( !user.has( def.filterPermission)) continue;
			}
//			grpRem.addAll(def.grpRemEnter);
			for (final String grp : def.grpRemEnter){
				groups.updateRem(grp, def.priority);
			}
			changed = true;
		}
		// check addEnter
		for (final PermDef def : data.defAddEnter){
			if ( def.ignorePermName != null ){
				if (user.has(def.ignorePermName )) continue;
			}
			if (def.filterPermission != null){
				if ( !user.has( def.filterPermission)) continue;
			}
//			grpAdd.addAll(def.grpAddEnter);
			for (final String grp : def.grpAddEnter){
				groups.updateAdd(grp, def.priority);
			}
			changed = true;
		}
		// check calls:
		for (final PermDef def : data.callOnEnter){
			for (final IRegionEnter call : def.callOnEnter){
				try{
					call.onRegionEnter(playerName, data.worldName, data.rid, def.defName);
				} catch(final Throwable t){
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
	public final boolean checkExit(final IPermissionUser user, final PermDefData data, final Integer id) {
		idCache.remove(id);
		boolean changed = false;
		// Check remExit:
		for (final PermDef def : data.defRemExit){
			if (def.ignorePermName != null){
				if (user.has( def.ignorePermName)) continue; // TODO: subject to policy
			}
			if (def.filterPermission != null){
				if ( !user.has( def.filterPermission)) continue;
			}
//			grpRem.addAll(def.grpRemExit);
			for (final String grp : def.grpRemExit){
				groups.updateRem(grp, def.priority);
			}
			changed = true;
		}
		// Check addExit:
		for (final PermDef def : data.defAddExit){
			if ( def.ignorePermName != null ){
				if (user.has(def.ignorePermName )) continue;
			}
			if (def.filterPermission != null){
				if ( !user.has( def.filterPermission)) continue;
			}
//			grpAdd.addAll(def.grpAddExit);
			for (final String grp : def.grpAddExit){
				groups.updateAdd(grp, def.priority);
			}
			changed = true;
		}		
		// check calls:
		for (final PermDef def : data.callOnExit){
			for ( IRegionExit call : def.callOnExit){
				try{
					call.onRegionExit(playerName, data.worldName, data.rid, def.defName);
				} catch(final Throwable t){
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
	public final boolean checkExpire(final IPermissionUser user, final PermDefData data, final Integer id ){
		boolean changed = false;
		for (final PermDef def : data.defAddEnter){
			if (def.ignorePermName != null){
				if (user.has( def.ignorePermName)){
					// no removing here
					continue; 
				}
			}
			if (def.filterPermission != null){
				if (!user.has(def.filterPermission)){
					// The permission might have been removed.
//					grpRem.addAll(def.grpAddEnter);
					for (final String grp : def.grpAddEnter){
						groups.updateRem(grp, def.priority);
					}
					changed = true;
					continue;
				}
			}
//			grpAdd.addAll(def.grpAddEnter);
			for (final String grp : def.grpAddEnter){
				groups.updateAdd(grp, def.priority);
			}
			changed = true;
		}
		return changed;
	}
	
	/**
	 * Set such that next check will re check all ids.
	 */
	public void forceCacheExpiration() {
		tsCache = 0;
	}
	
}
