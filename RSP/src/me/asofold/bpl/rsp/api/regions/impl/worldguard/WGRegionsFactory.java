package me.asofold.bpl.rsp.api.regions.impl.worldguard;

import java.util.HashSet;
import java.util.Set;

import me.asofold.bpl.rsp.api.regions.IRegions;
import me.asofold.bpl.rsp.api.regions.IRegionsFactory;

public class WGRegionsFactory implements IRegionsFactory{

	@Override
	public IRegions getRegions() {
		return new WGRegions();
	}

	@Override
	public Set<String> getPluginHookNames() {
		Set<String> names = new HashSet<String> ();
		names.add("WorldGuard");
		return names;
	}

}
