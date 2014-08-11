package me.asofold.bpl.rsp.api.regions;

import me.asofold.bpl.rsp.api.IPluginHook;

public interface IRegionsFactory extends IPluginHook {
	
	/**
	 * Factory for runtime use. Should throw a RuntimeException or similar if not available.
	 * @return
	 */
	public IRegions getRegions();
	
}
