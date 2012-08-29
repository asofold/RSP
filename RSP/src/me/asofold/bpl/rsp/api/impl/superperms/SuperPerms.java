package me.asofold.bpl.rsp.api.impl.superperms;

import me.asofold.bpl.rsp.api.IPermissionUser;
import me.asofold.bpl.rsp.api.IPermissions;

public final class SuperPerms implements IPermissions {

	public SuperPerms(){
	}
	
	@Override
	public final boolean isAvailable() {
		return true;
	}

	@Override
	public final IPermissionUser getUser(final String player, final String world) {
		return new SuperPermsUser(player, world);
	}

	@Override
	public final void saveChanges() {
	}

	@Override
	public final String getInterfaceName() {
		return "rsp-superperms";
	}

}
