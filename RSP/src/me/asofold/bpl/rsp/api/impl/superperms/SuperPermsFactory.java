package me.asofold.bpl.rsp.api.impl.superperms;

import java.util.Set;

import me.asofold.bpl.rsp.api.IPermissionSettings;
import me.asofold.bpl.rsp.api.IPermissions;
import me.asofold.bpl.rsp.api.IPermissionsFactory;

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
