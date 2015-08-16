package me.asofold.bpl.rsp.api.permissions.impl.bpermissions;

import java.util.UUID;

import me.asofold.bpl.rsp.api.permissions.GroupCache;
import me.asofold.bpl.rsp.api.permissions.IPermissionSettings;
import me.asofold.bpl.rsp.api.permissions.IPermissionUser;
import me.asofold.bpl.rsp.api.permissions.impl.SimpleGroupCache;
import me.asofold.bpl.rsp.plshared.Players;

import org.bukkit.entity.Player;

import de.bananaco.bpermissions.api.User;
import de.bananaco.bpermissions.api.World;
import de.bananaco.bpermissions.api.WorldManager;

public final class BPermUser implements IPermissionUser {
	
	private final User user;
	private final UUID id;
	private final String player;
	private final String world;
	private final BPerms perms;
	
	private final GroupCache groupCache = new SimpleGroupCache(); 
	private final Player bp;

	public BPermUser(final BPerms perms, final UUID id, final String player, final String world, final IPermissionSettings settings) {
		// set world
		if (world == null) {
			this.world = null;
		}
		else if (settings.getLowerCaseWorlds()) {
			this.world = world.toLowerCase();
		}
		else {
			this.world = world;
		}
		// set player
		this.id = id; // TODO: Check their API (!).
		if (settings.getLowerCasePlayers()) {
			this.player = player.toLowerCase();
		}
		else {
			this.player = player;
		}
		
		// TODO: USE-WORLDS
		
		// get objects from bPermsisions:
		final WorldManager man = WorldManager.getInstance();
		final World pW = man.getWorld(this.world);
		this.perms = perms;
		if ( pW == null ) {
			throw new RuntimeException("rsp - Invalid world: "+world);
		}
		user = pW.getUser(player);
		if (user == null) {
			throw new RuntimeException("rsp - Failed to get user '"+player+"' for world '"+world+"'.");
		}
		bp = Players.getPlayerExact(player);
	}

	@Override
	public final boolean has(final String perm) {
		if (bp != null) {
			return bp.hasPermission(perm);
		}
		else {
			return user.hasPermission(perm);
		}
	}

	@Override
	public final boolean inGroup(final String group) {
		if (groupCache.isPrepared()) {
			return groupCache.isGroupPresent(group);
		}
		else {
			return user.getGroupsAsString().contains(group);
		}
	}

	@Override
	public final void addGroup(final String group) {
		if (groupCache.isPrepared()) {
			groupCache.addGroup(group);
		}
		else {
			user.addGroup(group);
			if (user.isDirty()) {
				perms.changed.add(world);
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
			if (user.isDirty()) {
				perms.changed.add(world);
			}
		}
	}

	@Override
	public final String getUserName() {
		return player;
	}

	@Override
	public final String getWorldName() {
		return world;
	}

	@Override
	public final boolean prepare() {
		if (groupCache.isPrepared()) {
			groupCache.clear();
		}
		groupCache.addPresentGroups(user.getGroupsAsString());
		return true;
	}

	@Override
	public final boolean applyChanges() {
		if (groupCache.hasChangesPending()) {
			for (final String groupName : groupCache.getGroupsToRemove()) {
				user.removeGroup(groupName);
			}
			for (final String groupName : groupCache.getGroupsToAdd()) {
				user.addGroup(groupName);
			}
		}
		groupCache.clear();
		if (user.isDirty()) {
			perms.changed.add(world);
		}
		return true;
	}

	@Override
	public final void discardChanges() {
		groupCache.clear();
	}

	@Override
	public UUID getUniqueId() {
		return id;
	}

}
