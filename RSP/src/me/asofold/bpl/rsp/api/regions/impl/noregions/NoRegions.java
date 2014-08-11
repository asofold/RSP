package me.asofold.bpl.rsp.api.regions.impl.noregions;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;

import me.asofold.bpl.rsp.api.regions.IRegions;
import me.asofold.bpl.rsp.api.regions.RegionResult;

public class NoRegions implements IRegions {

	@Override
	public boolean setRegionResult(Location loc, String playerName, UUID uuid, RegionResult result) {
		// Does nothing (...).
		return false;
	}
	
	@Override
	public int getRegionCount(World world) {
		return 0;
	}

}
