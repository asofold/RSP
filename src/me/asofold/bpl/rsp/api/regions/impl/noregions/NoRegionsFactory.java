package me.asofold.bpl.rsp.api.regions.impl.noregions;

import java.util.HashSet;
import java.util.Set;

import me.asofold.bpl.rsp.api.regions.IRegions;
import me.asofold.bpl.rsp.api.regions.IRegionsFactory;

public class NoRegionsFactory implements IRegionsFactory {

	@Override
	public Set<String> getPluginHookNames() {
		return new HashSet<String>();
	}

	@Override
	public IRegions getRegions() {
		return new NoRegions();
	}

}
