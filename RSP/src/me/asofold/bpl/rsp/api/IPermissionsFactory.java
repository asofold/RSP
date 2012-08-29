package me.asofold.bpl.rsp.api;

import java.util.Set;

public interface IPermissionsFactory {
	/**
	 * Simply return an instance, for runtime use.
	 * This will by checked for exceptions, thus is safe to use.
	 * @param useWorlds If to try to use world specific groups, or to use global groups (might be ignored), permission checks should always be world specific.
	 * @return
	 */
	public IPermissions getPermissions(IPermissionSettings settings);
	
	/**
	 * Get the plugin names for which this factory is valid.
	 * The set may be empty or even null (then it will be considered if no factories are found for new plugins).
	 * @return
	 */
	public Set<String> getPluginHookNames();
}
