package me.asofold.bpl.rsp.api.permissions.impl.superperms;

import java.util.Set;

import me.asofold.bpl.rsp.api.permissions.IPermissionSettings;
import me.asofold.bpl.rsp.api.permissions.IPermissions;
import me.asofold.bpl.rsp.api.permissions.IPermissionsFactory;

public class SuperPermsFactory implements IPermissionsFactory {

	@Override
	public IPermissions getPermissions(IPermissionSettings settings) {
		return new SuperPerms();
	}

	@Override
	public Set<String> getPluginHookNames() {
		return null;
	}

}
