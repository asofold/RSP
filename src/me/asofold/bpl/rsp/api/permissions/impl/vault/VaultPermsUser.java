package me.asofold.bpl.rsp.api.permissions.impl.vault;

import java.util.UUID;

import me.asofold.bpl.rsp.api.permissions.GroupCache;
import me.asofold.bpl.rsp.api.permissions.IPermissionSettings;
import me.asofold.bpl.rsp.api.permissions.IPermissionUser;
import me.asofold.bpl.rsp.api.permissions.impl.SimpleGroupCache;
import me.asofold.bpl.rsp.plshared.Players;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public final class VaultPermsUser implements IPermissionUser {
	
	private final Permission perms;
	private final UUID playerId;
	private final String playerName;
	private final String worldName;
	private final boolean useWorlds;
	
	private final GroupCache groupCache = new SimpleGroupCache();
	private final Player player;
	private final OfflinePlayer offlinePlayer;
	
	public VaultPermsUser(final Permission perms, final UUID id, final String player, final String world, final IPermissionSettings settings) {
		this.perms = perms;
		this.playerId = id;
		if (settings.getLowerCasePlayers()) {
			this.playerName = player.toLowerCase();
		}
		else {
			this.playerName = player;
		}
		// world:
		if (world == null ) {
			this.worldName = null;
		}
		else if (settings.getLowerCaseWorlds()) {
			this.worldName = world.toLowerCase();
		}
		else {
			this.worldName = world;
		}
		// use worlds:
		useWorlds = settings.getUseWorlds();
		this.player = Players.getPlayerExact(player);
		this.offlinePlayer = this.player == null ? Bukkit.getOfflinePlayer(id) : this.player;
	}
	
	@Override
	public final boolean has(final String perm) {
		if (player != null) {
			return player.hasPermission(perm);
		}
		else {
			return perms.playerHas(useWorlds ? worldName : null, offlinePlayer, perm);
		}
	}

	@Override
	public final boolean inGroup(final String group) {
		if (groupCache.isPrepared()) {
			return groupCache.isGroupPresent(group);
		}
		else {
			return perms.playerInGroup(useWorlds ? this.worldName : null, offlinePlayer, group);
		}
	}

	@Override
	public final void addGroup(final String group) {
		if (groupCache.isPrepared()) {
			groupCache.addGroup(group);
		}
		else {
			doAddGroup(group);
		}
	}
	
	private final void doAddGroup(final String group) {
		perms.playerAddGroup(useWorlds ? this.worldName : null, offlinePlayer, group);
	}

	@Override
	public final void removeGroup(final String group) {
		if (groupCache.isPrepared()) {
			groupCache.removeGroup(group);
		}
		else {
			doRemoveGroup(group);
		}
	}
	
	private final void doRemoveGroup(final String group) {
		perms.playerRemoveGroup(useWorlds ? this.worldName : null, offlinePlayer, group);
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
		if (groupCache.isPrepared()) {
			groupCache.clear();
		}
		final String[] groupNames = perms.getPlayerGroups(useWorlds ? this.worldName : null, offlinePlayer);
		for (int i = 0; i < groupNames.length; i++) {
			groupCache.addPresentGroup(groupNames[i]);
		}
		return true;
	}

	@Override
	public boolean applyChanges() {
		if (groupCache.hasChangesPending()) {
			for (final String groupName : groupCache.getGroupsToRemove()) {
				doRemoveGroup(groupName);
			}
			for (final String groupName : groupCache.getGroupsToAdd()) {
				doAddGroup(groupName);
			}
		}
		groupCache.clear();
		return true;
	}

	@Override
	public void discardChanges() {
		groupCache.clear();
	}

	@Override
	public UUID getUniqueId() {
		return playerId;
	}

}
