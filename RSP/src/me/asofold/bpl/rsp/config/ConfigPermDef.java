package me.asofold.bpl.rsp.config;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.asofold.bpl.rsp.api.IRegionEnter;
import me.asofold.bpl.rsp.api.IRegionExit;
import me.asofold.bpl.rsp.config.compatlayer.CompatConfig;



/**
 * PermDef for reading from / saving to config.
 * @author mc_dev
 *
 */
public class ConfigPermDef {
	public static enum GroupUse{
		ADD_ENTER,
	    REMOVE_ENTER,
		REMOVE_EXIT,
		ADD_EXIT,
		HAVE_INSIDE,
		HAVE_OUTSIDE,
	};
	
	final String defName;
	String ignorePerm = null;
	String filterPerm = null;
	final Map<GroupUse, Set<String>> groups = new LinkedHashMap<GroupUse, Set<String>>();
	final List<IRegionEnter> callOnEnter = new LinkedList<IRegionEnter>();
	final List<IRegionExit> callOnExit = new LinkedList<IRegionExit>();
	int priority = 0;
	
	int lazyDist = Integer.MAX_VALUE;
	
	/**
	 * Constructor with default priority (0).
	 * @param defName
	 * @param ignorePerm
	 * @param filterPerm
	 */
	public ConfigPermDef(String defName, String ignorePerm, String filterPerm){
		this(defName, ignorePerm, filterPerm, 0);
	}

	/**
	 * 
	 * @param defName
	 * @param ignorePerm
	 * @param filterPerm
	 * @param priority
	 */
	public ConfigPermDef(String defName, String ignorePerm, String filterPerm, int priority){
		this(defName, ignorePerm, filterPerm, priority, Integer.MAX_VALUE);
	}
	
	public ConfigPermDef(String defName, String ignorePerm, String filterPerm, int priority, int lazyDist){
		if ( defName == null) throw new IllegalArgumentException("defName must be set.");
		this.defName = defName;
		this.ignorePerm = ignorePerm;
		this.filterPerm = filterPerm;
		this.priority = priority;
		this.lazyDist = lazyDist;
	}
	
	
	
	/**
	 * 
	 * @param use
	 * @param group
	 * @throws IllegalArgumentException If use is null.
	 */
	public void addGroup( GroupUse use, String group){
		if ( use == null ) throw new IllegalArgumentException("use must be set.");
		if ( group == null ) return;
		Set<String> groups = this.groups.get(use);
		if ( groups == null){
			groups = new HashSet<String>();
			this.groups.put(use,  groups);
		}
		groups.add(group);
	}
	
	public String getDefName(){
		return defName;
	}
	
	public void addCallOnEnter(IRegionEnter call){
		callOnEnter.add(call);
	}
	
	public void addCallOnExit(IRegionExit call){
		callOnExit.add(call);
	}

	/**
	 * Read ConfigPermDef instances from a configuraton.
	 * @param cfg
	 * @param prefix
	 * @return
	 */
	public static List<ConfigPermDef> readPermDefs(CompatConfig cfg, String prefix) {
		List<ConfigPermDef> defs = new LinkedList<ConfigPermDef>();
		List<String> defNames = cfg.getStringKeys(prefix);
		if (defNames == null) return defs;
		for ( String defName : defNames){
			String defBase = prefix+"."+defName+".";
			String ignorePerm = cfg.getString(defBase+"ignore-perm", null);
			String filterPerm = cfg.getString(defBase+"filter-perm", null);
			int priority = cfg.getInt(defBase + "priority", 0);
			int lazyDist = Math.max(0, cfg.getInt(defBase + "lazy-dist", Integer.MAX_VALUE));
			ConfigPermDef def = new ConfigPermDef(defName, ignorePerm, filterPerm, priority, lazyDist);
			for ( GroupUse use : GroupUse.values()){		
				String grpKey = defBase+(use.name().toLowerCase().replaceAll("_", "-"))+".groups";
				List<String> grps = cfg.getStringList(grpKey, null);
				if ( grps != null ){
					for ( String group: grps){
						def.addGroup(use, group);
					}
				}
			}
			defs.add(def);
		}
		return defs;
	}
}
