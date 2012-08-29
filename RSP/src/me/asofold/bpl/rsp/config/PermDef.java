package me.asofold.bpl.rsp.config;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
	public Set<String> grpAddEnter = new HashSet<String>(); // new HashSet<String>();
	
	/**
	 * Names of the groups that get removed on exit of regions.
	 */
	public Set<String> grpRemExit = new HashSet<String>();
	
	/**
	 * Names of the permission groups that count on exiting regions.
	 */
	public Set<String> grpAddExit = new HashSet<String>(); // new HashSet<String>();
	
	/**
	 * Names of the groups that get removed on entering of regions.
	 */
	public Set<String> grpRemEnter = new HashSet<String>();
	
	/**
	 * name of this Permdef.
	 */
	public final String defName;
	final int hash;
	/**
	 * Permission a player must have not to be checked with this def.
	 * (Will be checked BEFORE filterPermission)
	 */
	public String ignorePermName = null; // "rsp.ignore";
	/**
	 * Permission the player must have to use this.
	 */
	public String filterPermission = null;
	//public String filterGroup = null;
	
	public int priority;
	
	/**
	 * Call on entering a region.
	 */
	public List<IRegionEnter> callOnEnter = new LinkedList<IRegionEnter>();
	
	/**
	 * Call on exit of a region.
	 */
	public List<IRegionExit> callOnExit = new LinkedList<IRegionExit>();
	
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
	public final boolean equals(Object obj) {
		if (obj instanceof PermDef ){
			PermDef other = (PermDef) obj;
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
		def.filterPermission = cfgDef.filterPerm;
		def.priority = cfgDef.priority;
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
				grpAddEnter = add(grpAddEnter, group);
				grpRemExit = add(grpRemExit, group);
				break;
			case HAVE_OUTSIDE:
				grpAddExit = add(grpAddExit, group);
				grpRemEnter = add(grpRemEnter, group);
				break;
			case ADD_ENTER:
				grpAddEnter = add(grpAddEnter, group);
				break;
			case ADD_EXIT:
				grpAddExit = add( grpAddExit, group);
				break;
			case REMOVE_ENTER:
				grpRemEnter = add(grpRemEnter, group);
				break;
			case REMOVE_EXIT:
				grpRemExit = add(grpRemExit, group);
			default:
				throw new IllegalArgumentException("use '"+use+"' not yet supported !");	
		}
	}
	
	public Set<String> add(Set<String> set, String entry){
		if (set == null) set = new HashSet<String>();
		set.add(entry);
		return set;
	}
	
}
