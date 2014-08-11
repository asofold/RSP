package me.asofold.bpl.rsp.utils;

import org.bukkit.Location;

public final class BlockPos {
	public String world;
	public int x;
	public int y;
	public int z;
	
	public BlockPos(final String world, final int x, final int y, final int z){
		set(world, x, y, z);
	}
	
	public BlockPos(final Location loc) {
		set(loc);
	}

	public final void set(final Location loc) {
		world = loc.getWorld().getName();
		x = loc.getBlockX();
		y = loc.getBlockY();
		z = loc.getBlockZ();
	}

	public final void set(final String world, final int x, final int y, final int z){
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/**
	 * Return if set, will set if the distance to one of the coordinates is greater than d.
	 * @param loc
	 * @param d
	 * @return
	 */
	public final boolean setOnDist(final Location loc , final int d){
		final String bw = loc.getWorld().getName();
		int bx = loc.getBlockX();
		int by = loc.getBlockY();
		int bz = loc.getBlockZ();
		if ( (Math.abs(bx-x)>d)||(Math.abs(by-y)>d)||(Math.abs(bz-z)>d) ){
			set(bw,bx,by,bz);
			return true;
		} else if ( !bw.equals(world) ){
			// This is assumed to be the slower check!
			// (still consistent)
			set(bw,bx,by,bz);
			return true;
		} 
		return false;
	}
}
