package me.asofold.bpl.rsp.api.permissions.impl;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import me.asofold.bpl.rsp.api.permissions.GroupCache;

/**
 * Simple overriding implementation. Adding will override removing and vice
 * versa.
 * 
 * @author dev7u1
 *
 */
public class SimpleGroupCache implements GroupCache {

	private boolean isPrepared = false;

	private final Set<String> present = new LinkedHashSet<String>();

	private final Set<String> add = new LinkedHashSet<String>();

	private final Set<String> remove = new LinkedHashSet<String>();

	@Override
	public boolean isPrepared() {
		return isPrepared;
	}

	@Override
	public void addPresentGroup(String groupName) {
		isPrepared = true;
		present.add(groupName);
	}

	@Override
	public void addPresentGroups(Collection<String> groupNames) {
		isPrepared = true;
		present.addAll(groupNames);
	}

	@Override
	public boolean isGroupPresent(String groupName) {
		return present.contains(groupName);
	}

	@Override
	public void addGroup(String groupName) {
		isPrepared = true;
		if (!present.contains(groupName)) {
			add.add(groupName);
		}
		remove.remove(groupName);
	}

	@Override
	public void removeGroup(String groupName) {
		isPrepared = true;
		if (present.contains(groupName)) {
			remove.add(groupName);
		}
		add.remove(groupName);
	}

	@Override
	public Set<String> getGroupsToAdd() {
		return add;
	}

	@Override
	public Set<String> getGroupsToRemove() {
		return remove;
	}
	
	@Override
	public boolean hasChangesPending() {
		return !add.isEmpty() || !remove.isEmpty();
	}

	@Override
	public void clear() {
		isPrepared = false;
		present.clear();
		add.clear();
		remove.clear();
	}

}
