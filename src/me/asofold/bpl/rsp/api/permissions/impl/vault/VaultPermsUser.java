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
	private final UUID playerId;
	private final String playerName;
	private final String worldName;
	private final boolean useWorlds;
	
	private Set<String> groupCache = null;
	private final Player bp;
	
	public VaultPermsUser(final Permission perms, final UUID id, final String player, final String world, final IPermissionSettings settings) {
		this.perms = perms;
		this.playerId = id;
		if (settings.getLowerCasePlayers()) this.playerName = player.toLowerCase();
		else this.playerName = player;
		// world:
		if (world == null ) this.worldName = null;
		else if (settings.getLowerCaseWorlds()) this.worldName = world.toLowerCase();
		else this.worldName = world;
		// use worlds:
		useWorlds = settings.getUseWorlds();
		bp = Players.getPlayerExact(player);
	}

	@Override
	public final boolean has(final String perm) {
		if (bp != null && bp.hasPermission(perm)) return true;
		else return perms.has(worldName, playerName, perm); // this should work with Vault.
	}

	@Override
	public final boolean inGroup(final String group) {
		if (useWorlds) return perms.playerInGroup(worldName, playerName, group);
		else return perms.playerInGroup((String) null, playerName, group);
	}

	@Override
	public final void addGroup(final String group) {
		if (useWorlds)perms.playerAddGroup(worldName, playerName, group);
		else perms.playerAddGroup((String) null, playerName, group);
		if (groupCache != null) groupCache.add(group);
	}

	@Override
	public final void removeGroup(final String group) {
		if (useWorlds) perms.playerRemoveGroup(worldName, playerName, group);
		else perms.playerRemoveGroup((String) null, playerName, group);
		if (groupCache != null) groupCache.remove(group);
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
		return playerId;
	}

}
