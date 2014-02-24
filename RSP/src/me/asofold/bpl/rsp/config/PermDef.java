package me.asofold.bpl.rsp.config;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import me.asofold.bpl.rsp.api.IPermissionUser;
import me.asofold.bpl.rsp.api.IRegionEnter;
import me.asofold.bpl.rsp.api.IRegionExit;
import me.asofold.bpl.rsp.config.ConfigPermDef.GroupUse;



/**
 * definition for groups, permissions.
 * @author mc_dev
 *
 */
public class PermDef {
	
	/**
	 * Names of the permission groups that count on entering regions.
	 */
	public final Set<String> grpAddEnter = new LinkedHashSet<String>();
	
	/**
	 * Names of the groups that get removed on exit of regions.
	 */
	public final Set<String> grpRemExit = new LinkedHashSet<String>();
	
	/**
	 * Names of the permission groups that count on exiting regions.
	 */
	public final Set<String> grpAddExit = new LinkedHashSet<String>();
	
	/**
	 * Names of the groups that get removed on entering of regions.
	 */
	public final Set<String> grpRemEnter = new LinkedHashSet<String>();
	
	/**
	 * name of this Permdef.
	 */
	public final String defName;
	final int hash;
	
	/**
	 * Permission a player must have not to be checked with this def.
	 * (Will be checked BEFORE filterPermission)
	 */
	public String ignorePermName = null;
	
	/**
	 * Permission to ignore adding.
	 */
	public String ignoreAddPermName = null;
	
	/**
	 * Permission to ignore removal.
	 */
	public String ignoreRemovePermName = null;
	
	/**
	 * Permission the player must have to use this.
	 */
	public String filterPermission = null;
	//public String filterGroup = null;
	
	public int priority = 0;
	
	public int lazyDist = Integer.MAX_VALUE;
	
	/**
	 * Call on entering a region.
	 */
	public final List<IRegionEnter> callOnEnter = new LinkedList<IRegionEnter>();
	
	/**
	 * Call on exit of a region.
	 */
	public final List<IRegionExit> callOnExit = new LinkedList<IRegionExit>();
	
	/**
	 * 
	 * @param defName Must not be null, choose unique.
	 */
	public PermDef(String defName){
		if ( defName == null) throw new IllegalArgumentException("defName must not be null.");
		this.defName = defName;
		this.hash = defName.hashCode();
	}
	
	@Override
	public final int hashCode() {
		return hash;
	}
	@Override
	public final boolean equals(final Object obj) {
		if (obj instanceof PermDef ){
			final PermDef other = (PermDef) obj;
			if (hash != other.hash) return false;
			else return defName.equals(other.defName);
		} else return false;
	}
	
	public String getDefName(){
		return defName;
	}
	
	/**
	 * Factory to get PermDef from a ConfigPermDef, for runtime use ...
	 * @param cfgDef
	 * @return
	 */
	public static PermDef fromConfigPermDef(ConfigPermDef cfgDef){
		PermDef def = new PermDef(cfgDef.defName);
		def.ignorePermName = cfgDef.ignorePerm;
		def.ignoreAddPermName = cfgDef.ignoreAddPerm;
		def.ignoreRemovePermName = cfgDef.ignoreRemovePerm;
		def.filterPermission = cfgDef.filterPerm;
		def.priority = cfgDef.priority;
		def.lazyDist = cfgDef.lazyDist;
		def.callOnEnter.addAll(cfgDef.callOnEnter);
		def.callOnExit.addAll(cfgDef.callOnExit);
		for (GroupUse use : cfgDef.groups.keySet()){
			Set<String> groups = cfgDef.groups.get(use);
			for (String group : groups){
				def.addGroup(use, group);
			}
		}
		return def;
	}
	
	public void addGroup(GroupUse use, String group) {
		if ( use == null ) throw new IllegalArgumentException("use must be set.");
		switch(use){
			case HAVE_INSIDE:
				add(grpAddEnter, group);
				add(grpRemExit, group);
				break;
			case HAVE_OUTSIDE:
				add(grpAddExit, group);
				add(grpRemEnter, group);
				break;
			case ADD_ENTER:
				add(grpAddEnter, group);
				break;
			case ADD_EXIT:
				add( grpAddExit, group);
				break;
			case REMOVE_ENTER:
				add(grpRemEnter, group);
				break;
			case REMOVE_EXIT:
				add(grpRemExit, group);
			default:
				throw new IllegalArgumentException("use '"+use+"' not yet supported !");	
		}
	}
	
	public Set<String> add(Set<String> set, String entry){
		if (set == null) set = new HashSet<String>();
		set.add(entry);
		return set;
	}
	
	/**
	 * Convenience method to check both permissions if needed.
	 * @param user
	 * @return
	 */
	public boolean ignoreAdd(IPermissionUser user) {
		if (ignorePermName != null && user.has(ignorePermName)) {
			return true;
		}
		else if (ignoreAddPermName != null && user.has(ignoreAddPermName)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Convenience method to check both permissions if needed.
	 * @param user
	 * @return
	 */
	public boolean ignoreRemove(IPermissionUser user) {
		if (ignorePermName != null && user.has(ignorePermName)) {
			return true;
		}
		else if (ignoreRemovePermName != null && user.has(ignoreRemovePermName)) {
			return true;
		}
		else {
			return false;
		}
	}
	
}
