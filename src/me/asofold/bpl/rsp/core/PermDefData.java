package me.asofold.bpl.rsp.core;

import java.util.LinkedHashSet;
import java.util.Set;

import me.asofold.bpl.rsp.config.PermDef;



/**
 * Class to map ids to (world->region->id) -> Set<PermDefData> for entering and exiting regions.
 * @author mc_dev
 *
 */
final class PermDefData {
	/**
	 * Permdefs for this id.
	 */
	
	// TODO: re-think data type (fastest, simplest)
	
	/**
	 * All permdefs that were added.
	 */
	final Set<PermDef> allAdded = new LinkedHashSet<PermDef>();
	
	/**
	 * PermDefs to add at entering a region.
	 */
	final Set<PermDef> defAddEnter = new LinkedHashSet<PermDef>();
	
	/**
	 * PermDefs to remove when leaving a region.
	 */
	final Set<PermDef> defRemExit = new LinkedHashSet<PermDef>();
	
	/**
	 * PermDefs to remove when entering a region.
	 */
	final Set<PermDef> defRemEnter = new LinkedHashSet<PermDef>();
	
	/**
	 * PermDefs to add when leaving a region.
	 */
	final Set<PermDef> defAddExit = new LinkedHashSet<PermDef>();
	
	final Set<PermDef> callOnEnter = new LinkedHashSet<PermDef>();
	final Set<PermDef> callOnExit = new LinkedHashSet<PermDef>();
	
	String worldName = null;
	String rid = null;
	
	int minLazyDist = Integer.MAX_VALUE;
	
	public PermDefData(String worldName, String rid){
		this.worldName = worldName;
		this.rid = rid;
	}
	
	public void updateMinLazyDist() {
		
	}

	/**
	 * TODO: THIS DOES NOT OVERRIDE
	 * @param def
	 */
	public void addPermDef(PermDef def) {
		boolean added = false;
		// TODO: Should replace ? Or throw otherwise externally.
		if (!def.callOnEnter.isEmpty()) {
			callOnEnter.add(def);
			added = true;
		}
		if (!def.callOnExit.isEmpty()) {
			callOnExit.add(def);
			added = true;
		}
		if (!def.grpAddEnter.isEmpty()) {
			defAddEnter.add(def);
			added = true;
		}
		if (!def.grpRemExit.isEmpty()) {
			defRemExit.add(def);
			added = true;
		}
		if (!def.grpRemEnter.isEmpty()) {
			defRemEnter.add(def);
			added = true;
		}
		if (!def.grpAddExit.isEmpty()) {
			defAddExit.add(def);
			added = true;
		}
		// Adjust (allow empty permdefs for lazy-dist).
		if (allAdded.add(def) || added) {
			minLazyDist = Math.min(minLazyDist, def.lazyDist);
		}
	}
	
	public boolean removePermDef(PermDef def){
		boolean found = false;
		if ( defAddEnter.contains(def)){
			found = true;
			defAddEnter.remove(def);
		}
		if ( defRemExit.contains(def)){
			found = true;
			defRemExit.remove(def);
		}
		if ( defRemEnter.contains(def)){
			found = true;
			defRemEnter.remove(def);
		}
		if ( defAddExit.contains(def)){
			found = true;
			defAddExit.remove(def);
		}
		if ( callOnEnter.contains(def)){
			found = true;
			callOnEnter.remove(def);
		}
		if ( callOnExit.contains(def)){
			found = true;
			callOnExit.remove(def);
		}
		// Adjust (allow removal of empty).
		if (allAdded.remove(def)) {
			updateMinLazyDist();
		}
		
		return found;
	}

	/**
	 * Convenience method.
	 * @param defName
	 * @return
	 */
	public boolean removePermDef(String defName) {
		return removePermDef(new PermDef(defName));
	}
	
	public boolean isEmpty(){
		return defAddEnter.isEmpty() &&  defRemExit.isEmpty() && defRemEnter.isEmpty() && defAddExit.isEmpty() && callOnEnter.isEmpty() && callOnExit.isEmpty();
	}
	
	/**
	 * Very simple check (just name comparison).
	 * @param defName
	 * @return
	 */
	public boolean contains(String defName){
		return contains(new PermDef(defName));
	}
	
	/**
	 * Very simple check (just name comparison).
	 * @param def
	 * @return
	 */
	public boolean contains(PermDef def){
		return defAddEnter.contains(def) ||  defRemExit.contains(def) || defRemEnter.contains(def) || defAddExit.contains(def) || callOnEnter.contains(def) || callOnExit.contains(def);
	}
}
