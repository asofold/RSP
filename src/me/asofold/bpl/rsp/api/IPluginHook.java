package me.asofold.bpl.rsp.api;

import java.util.Set;

/**
 * Factory or component that needs certain plugins and will be called with enabling/disabling any of those.
 * @author dev1mc
 *
 */
public interface IPluginHook {
	
	/**
	 * Get the plugin names for which this factory is valid.
	 * The set may be empty or even null (then it will be considered if no factories are found for new plugins).
	 * @return
	 */
	public Set<String> getPluginHookNames();
	
}
