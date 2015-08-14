package me.asofold.bpl.rsp.api.permissions.impl.pex;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import me.asofold.bpl.rsp.api.permissions.IPermissionSettings;
import me.asofold.bpl.rsp.api.permissions.IPermissionUser;
import me.asofold.bpl.rsp.plshared.Players;

import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public final class PexPermUser implements IPermissionUser {
	private final PermissionUser user;
	private final PexPerms perms; 
	private final UUID playerId;
	private final String playerName;
	private final String worldName;
	
	/**
	 * null = not fetched / unprepared !
	 */
	private Set<String> groupCache = null;
	private final Player bp;
	
	public PexPermUser(final PexPerms perms, final UUID id, final String player, final String world, final IPermissionSettings settings) {
		// TODO: use-worlds is ignored, currently
		// (Other settings are ignored, pex does it.)
		this.perms = perms;
		this.playerId = id;
		this.playerName = player;
		this.worldName = world;
		
		bp = Players.getPlayerExact(player);
		if (bp != null) {
			// Safest way.
			user = PermissionsEx.getUser(player);
		} else {
			PermissionUser temp = null; // Stupid IDE.
			try {
				temp = PermissionsEx.getPermissionManager().getUser(id);
			} catch (Throwable t) {}
			if (temp == null) {
				// Legacy or migration (!).
				// This is more safe than one might assume (name has been correct a moment ago).
				temp = PermissionsEx.getUser(player);
			}
			user = temp;
		}
	}

	@Override
	public final boolean has(final String perm) {
		if (bp != null) return bp.hasPermission(perm);
		else  return user.has(perm);
	}

	@Override
	public final boolean inGroup(final String group) {
		if (groupCache == null) return user.inGroup(group);
		else return groupCache.contains(group);
	}

	@Override
	public final void addGroup(final String group) {
		if (groupCache == null){
			user.addGroup(group);
			if (user.isVirtual()) perms.changed.add(playerName);
		}
		else groupCache.add(group);
	}

	@Override
	public final void removeGroup(final String group) {
		if (groupCache == null){
			user.removeGroup(group);
			if (user.isVirtual()) perms.changed.add(playerName);
		}
		else groupCache.remove(group);
	}

	@Override
	public final String getUserName() {
		return playerName;
	}

	@Override
	public final String getWorldName() {
		return worldName;
	}

	@Override
	public final boolean prepare() {
		fetchGroups();
		return true;
	}

	private final void fetchGroups() {
		if (groupCache == null) groupCache = new LinkedHashSet<String>();
		else groupCache.clear();
		final PermissionGroup[] groups = user.getGroups(); // TODO: use getGroups(worldName) ?
		for (int i = 0; i< groups.length; i++){
			groupCache.add(groups[i].getName());
		}
	}

	@Override
	public final boolean applyChanges() {
		if (groupCache == null) return true;
		final String[] groups = new String[groupCache.size()];
		groupCache.toArray(groups);
		user.setGroups(groups);
		if (user.isVirtual()) perms.changed.add(playerName);
		groupCache = null;
		return true;
	}

	@Override
	public final void discardChanges() {
		groupCache = null;
	}

	@Override
	public UUID getUniqueId() {
		return playerId;
	}
}
