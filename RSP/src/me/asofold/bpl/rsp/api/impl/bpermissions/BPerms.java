package me.asofold.bpl.rsp.api.impl.bpermissions;

import java.util.HashSet;
import java.util.Set;

import me.asofold.bpl.rsp.api.IPermissionSettings;
import me.asofold.bpl.rsp.api.IPermissionUser;
import me.asofold.bpl.rsp.api.IPermissions;

import de.bananaco.bpermissions.api.World;
import de.bananaco.bpermissions.api.WorldManager;

public final class BPerms implements IPermissions {
	
	/**
	 * Changed worlds.<br>
	 * Changed by user objects!
	 */
	final Set<String> changed = new HashSet<String>();
	private final IPermissionSettings settings;
	
	/**
	 * 
	 * @param settings Is getting ignored for bPermissions: always use worlds.
	 */
	public BPerms(final IPermissionSettings settings){
		this.settings = settings;
	}
	
	@Override
	public final boolean isAvailable() {
		return WorldManager.getInstance() != null;
	}

	@Override
	public final IPermissionUser getUser(final String player, final String world) {
		return new BPermUser(this, player, world, settings);
	}

	@Override
	public final void saveChanges() {
		if (!settings.getSaveAtAll()){
			changed.clear();
			return;
		}
		final WorldManager wm = WorldManager.getInstance();
		for ( final String wn : changed){
			final World world = wm.getWorld(wn);
			if (world != null) world.save(); // TODO: notify if false.
		}
		changed.clear();
	}

	@Override
	public final String getInterfaceName() {
		return "rsp-bperms";
	}
}
