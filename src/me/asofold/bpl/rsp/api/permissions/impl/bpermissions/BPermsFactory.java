package me.asofold.bpl.rsp.api.permissions.impl.bpermissions;

import java.util.HashSet;
import java.util.Set;

import me.asofold.bpl.rsp.api.permissions.IPermissionSettings;
import me.asofold.bpl.rsp.api.permissions.IPermissions;
import me.asofold.bpl.rsp.api.permissions.IPermissionsFactory;


public class BPermsFactory implements IPermissionsFactory {

	@Override
	public IPermissions getPermissions(IPermissionSettings settings) {
		return new BPerms(settings);
	}

	@Override
	public Set<String> getPluginHookNames() {
		Set<String> out = new HashSet<String>();
		out.add("bPermissions");
		return out;
	}

}
