package me.asofold.bpl.rsp.api.permissions.impl.pex;

import java.util.UUID;

import me.asofold.bpl.rsp.api.permissions.GroupCache;
import me.asofold.bpl.rsp.api.permissions.IPermissionSettings;
import me.asofold.bpl.rsp.api.permissions.IPermissionUser;
import me.asofold.bpl.rsp.api.permissions.impl.SimpleGroupCache;
import me.asofold.bpl.rsp.plshared.Players;

import org.bukkit.entity.Player;

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
	private final GroupCache groupCache = new SimpleGroupCache();
	private final Player bp;
	
	public PexPermUser(final PexPerms perms, final UUID id, final String player, final String world, final IPermissionSettings settings) {
		// (Other settings are ignored, pex does it.)
		this.perms = perms;
		this.playerId = id;
		this.playerName = player;
		this.worldName = world;
		
		// TODO: Just use UUID?
		bp = Players.getPlayerExact(player);
		if (bp != null) {
			// Safest way.
			user = PermissionsEx.getUser(player);
		} else {
			// Fail hard on legacy versions.
			user = PermissionsEx.getPermissionManager().getUser(id);
		}
	}

	@Override
	public final boolean has(final String perm) {
		if (bp != null) {
			return bp.hasPermission(perm);
		}
		else {
			return user.has(perm);
		}
	}

	@Override
	public final boolean inGroup(final String group) {
		if (groupCache.isPrepared()) {
			return groupCache.isGroupPresent(group);
		}
		else {
			return user.inGroup(group);
		}
	}

	@Override
	public final void addGroup(final String group) {
		if (groupCache.isPrepared()) {
			groupCache.addGroup(group);
		}
		else {
			user.addGroup(group);
			if (user.isVirtual()) {
				perms.changed.add(playerName);
			}
		}
	}

	@Override
	public final void removeGroup(final String group) {
		if (groupCache.isPrepared()) {
			groupCache.removeGroup(group);
		}
		else {
			user.removeGroup(group);
			if (user.isVirtual()) {
				perms.changed.add(playerName);
			}
		}
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
		if (groupCache.isPrepared()) {
			groupCache.clear();
		}
		groupCache.addPresentGroups(user.getOwnParentIdentifiers()); // TODO: VERIFY SANITY.
		return true;
	}

	@Override
	public final boolean applyChanges() {
		if (groupCache.hasChangesPending()) {
			// TODO: Do JITs contain the necessary kind of magic by now?
			for (final String groupName : groupCache.getGroupsToRemove()) {
				user.removeGroup(groupName);
			}
			for (final String groupName : groupCache.getGroupsToAdd()) {
				user.addGroup(groupName);
			}
		}
		groupCache.clear();
		if (user.isVirtual()) {
			perms.changed.add(playerName);
		}
		return true;
	}

	@Override
	public final void discardChanges() {
		groupCache.clear();
	}

	@Override
	public UUID getUniqueId() {
		return playerId;
	}
}
