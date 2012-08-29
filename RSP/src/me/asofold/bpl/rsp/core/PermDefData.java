package me.asofold.bpl.rsp.core;

import java.util.HashSet;
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
	 * PermDefs to add at entering a region.
	 */
	final Set<PermDef> defAddEnter = new HashSet<PermDef>();
	
	/**
	 * PermDefs to remove when leaving a region.
	 */
	final Set<PermDef> defRemExit = new HashSet<PermDef>();
	
	/**
	 * PermDefs to remove when entering a region.
	 */
	final Set<PermDef> defRemEnter = new HashSet<PermDef>();
	
	/**
	 * PermDefs to add when leaving a region.
	 */
	final Set<PermDef> defAddExit = new HashSet<PermDef>();
	
	final Set<PermDef> callOnEnter = new HashSet<PermDef>();
	final Set<PermDef> callOnExit = new HashSet<PermDef>();
	
	String worldName = null;
	String rid = null;
	
	public PermDefData(String worldName, String rid){
		this.worldName = worldName;
		this.rid = rid;
	}

	/**
	 * TODO: THIS DOES NOT OVERRIDE
	 * @param def
	 */
	public void addPermDef(PermDef def) {
		if (!def.callOnEnter.isEmpty()) callOnEnter.add(def); 
		if (!def.callOnExit.isEmpty()) callOnExit.add(def); 
		if (!def.grpAddEnter.isEmpty()) defAddEnter.add(def);
		if (!def.grpRemExit.isEmpty()) defRemExit.add(def);
		if (!def.grpRemEnter.isEmpty()) defRemEnter.add(def);
		if (!def.grpAddExit.isEmpty()) defAddExit.add(def);
	}
	
	public boolean removePermDef(PermDef def){
		// TODO: add new group uses.
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
