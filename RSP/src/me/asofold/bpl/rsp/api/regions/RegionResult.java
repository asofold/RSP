package me.asofold.bpl.rsp.api.regions;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Result/summary for a player at a location. Currently does not ensure that ids are unique.
 * @author dev1mc
 *
 */
public class RegionResult {
	
	private boolean isOwner;
	private boolean isMember;
	private final Collection<String> ids = new ArrayList<String>();
	
	public void setOwner() {
		this.isOwner = true;
	}
	
	public void setMember() {
		this.isMember = true;
	}
	
	public void addId(String id) {
		this.ids.add(id);
	}
	
	public void addIds(Collection<String> ids) {
		this.ids.addAll(ids);
	}
	
	public boolean isMember() {
		return this.isMember;
	}
	
	public boolean isOwner() {
		return this.isOwner;
	}
	
	public boolean hasIds() {
		return !ids.isEmpty();
	}
	
	public Collection<String> getIds() {
		return this.ids;
	}
	
	/**
	 * 
	 */
	public void reset() {
		this.isMember = this.isOwner = false;
		this.ids.clear();
	}
	
}
