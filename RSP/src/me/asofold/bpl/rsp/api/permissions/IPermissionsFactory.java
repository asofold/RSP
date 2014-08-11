package me.asofold.bpl.rsp.api.permissions;

import me.asofold.bpl.rsp.api.IPluginHook;



public interface IPermissionsFactory extends IPluginHook {
	/**
	 * Simply return an instance, for runtime use.
	 * This will by checked for exceptions, thus can and should check availability, could throw a RuntimeException, if not available.
	 * @param useWorlds If to try to use world specific groups, or to use global groups (might be ignored), permission checks should always be world specific.
	 * @return
	 */
	public IPermissions getPermissions(IPermissionSettings settings);
	
}
