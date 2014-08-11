package me.asofold.bpl.rsp.api.permissions.impl.pex;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import me.asofold.bpl.rsp.api.permissions.IPermissionSettings;
import me.asofold.bpl.rsp.api.permissions.IPermissionUser;
import me.asofold.bpl.rsp.api.permissions.IPermissions;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public final class PexPerms implements IPermissions {
	/**
	 * Changes users.<br>
	 * Accessed by user objects!
	 */
	final Set<String> changed = new HashSet<String>();
	private final IPermissionSettings settings;
	
	/**
	 * 
	 * @param settings Groups are global anyway, this is ignored, permission checks are done with respect to world, though.
	 */
	public PexPerms(final IPermissionSettings settings) {
		this.settings = settings;
	}

	@Override
	public final boolean isAvailable() {
		return PermissionsEx.isAvailable();
	}

	@Override
	public final IPermissionUser getUser(final UUID id, final String player, final String world) {
		if (!settings.getSaveAtAll() && !changed.isEmpty()) changed.clear();
		return new PexPermUser(this, id, player, world, settings);
	}

	@Override
	public final void saveChanges() {
		if (!settings.getSaveAtAll()){
			changed.clear();
			return;
		}
		for ( final String n : changed){
			final PermissionUser user = PermissionsEx.getUser(n);
			if (user == null) continue; // should not happen, but for safety.
			user.save();
		}
		changed.clear();
	}

	@Override
	public final String getInterfaceName() {
		return "rsp-pex";
	}
}
