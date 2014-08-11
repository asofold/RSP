package me.asofold.bpl.rsp.api.permissions.impl.superperms;

import java.util.UUID;

import me.asofold.bpl.rsp.api.permissions.IPermissionUser;
import me.asofold.bpl.rsp.plshared.Players;

import org.bukkit.entity.Player;

public final class SuperPermsUser implements IPermissionUser {
	
	private Player player;
	private final UUID id;
	private final String playerName;
	private final String worldName;

	public SuperPermsUser(final UUID id, final String playerName, final String world) {
		this.id = id;
		this.playerName = playerName;
		this.worldName = world;
		setPlayer();
	}
	
	/**
	 * 
	 * @return If player present.
	 */
	private final boolean setPlayer(){
		player = Players.getPlayerExact(playerName);
		return player != null;
	}

	@Override
	public final boolean has(final String perm) {
		if (player != null || setPlayer()) return player.hasPermission(perm);
		else return false;
	}

	@Override
	public final boolean prepare() {
		return false;
	}

	@Override
	public final boolean applyChanges() {
		return false;
	}

	@Override
	public final void discardChanges() {
	}

	@Override
	public final boolean inGroup(String group) {
		return false;
	}

	@Override
	public final void addGroup(String group) {
	}

	@Override
	public final void removeGroup(String group) {
	}

	@Override
	public String getUserName() {
		return playerName;
	}

	@Override
	public final String getWorldName() {
		return worldName;
	}

	@Override
	public UUID getUniqueId() {
		return id;
	}

}
