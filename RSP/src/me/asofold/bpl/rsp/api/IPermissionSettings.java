package me.asofold.bpl.rsp.api;

public interface IPermissionSettings {
	public boolean getUseWorlds();
	public boolean getLowerCasePlayers();
	public boolean getLowerCaseWorlds();
	// TODO: Might have to add getUseUUID, depending on what the plugins are doing.
	public boolean getSaveAtAll();
}
