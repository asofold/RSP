package me.asofold.bpl.rsp.api.regions.impl.worldguard;

import java.util.UUID;

import me.asofold.bpl.rsp.api.regions.IRegions;
import me.asofold.bpl.rsp.api.regions.RegionResult;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WGRegions implements IRegions {
	
	private final WorldGuardPlugin wg;
	
	public WGRegions() {
		wg = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
	}
	
	@Override
	public boolean setRegionResult(final Location loc, final String playerName, final UUID uuid, final RegionResult result) {
		boolean any = false; // Any "changes", not counting overriding differently.
		// (Needs WorldEdit in the class-path, to use getApplicableRegions.)
		final ApplicableRegionSet set = wg.getRegionManager(loc.getWorld()).getApplicableRegions(loc);
		boolean owner = false;
		boolean member = false;
		for (final ProtectedRegion region : set){
			result.addId(region.getId());
			// TODO: Currently both are checked, to allow distinction by filter-perm.
			if (!owner && region.isOwner(playerName)) {
				owner = true;
			}
			if (!member && region.isMember(playerName)) {
				member = true;
			}
			any = true; // Fastest ? :p
		}
		if (owner) {
			result.setOwner();
		}
		if (member) {
			result.setMember();
		}
		return any;
	}

	@Override
	public int getRegionCount(World world) {
		return wg.getRegionManager(world).size();
	}
	
}
