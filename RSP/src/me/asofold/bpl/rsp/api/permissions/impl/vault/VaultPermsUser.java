package me.asofold.bpl.rsp.api.permissions.impl.vault;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import me.asofold.bpl.rsp.api.permissions.IPermissionSettings;
import me.asofold.bpl.rsp.api.permissions.IPermissionUser;
import me.asofold.bpl.rsp.plshared.Players;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.entity.Player;

public final class VaultPermsUser implements IPermissionUser {
	
	private final Permission perms;
	private final UUID id;
	private final String player;
	private final String world;
	private final boolean useWorlds;
	
	private Set<String> groupCache = null;
	private final Player bp;
	
	public VaultPermsUser(final Permission perms, final UUID id, final String player, final String world, final IPermissionSettings settings) {
		this.perms = perms;
		this.id = id;
		if (settings.getLowerCasePlayers()) this.player = player.toLowerCase();
		else this.player = player;
		// world:
		if (world == null ) this.world = null;
		else if (settings.getLowerCaseWorlds()) this.world = world.toLowerCase();
		else this.world = world;
		// use worlds:
		useWorlds = settings.getUseWorlds();
		bp = Players.getPlayerExact(player);
	}

	@Override
	public final boolean has(final String perm) {
		if (bp != null && bp.hasPermission(perm)) return true;
		else return perms.has(world, player, perm); // this should work with Vault.
	}

	@Override
	public final boolean inGroup(final String group) {
		if (useWorlds) return perms.playerInGroup(world, player, group);
		else return perms.playerInGroup((String) null, player, group);
	}

	@Override
	public final void addGroup(final String group) {
		if (useWorlds)perms.playerAddGroup(world, player, group);
		else perms.playerAddGroup((String) null, player, group);
		if (groupCache != null) groupCache.add(group);
	}

	@Override
	public final void removeGroup(final String group) {
		if (useWorlds) perms.playerRemoveGroup(world, player, group);
		else perms.playerRemoveGroup((String) null, player, group);
		if (groupCache != null) groupCache.remove(group);
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
	public boolean prepare() {
		if (groupCache == null) groupCache = new LinkedHashSet<String>();
		else groupCache.clear();
		final String[] groups = perms.getGroups();
		for (int i = 0; i < groups.length; i++){
			groupCache.add(groups[i]);
		}
		return true;
	}

	@Override
	public boolean applyChanges() {
		// TODO: later...
		return true;
	}

	@Override
	public void discardChanges() {
		groupCache = null;
	}

	@Override
	public UUID getUniqueId() {
		return id;
	}

}
