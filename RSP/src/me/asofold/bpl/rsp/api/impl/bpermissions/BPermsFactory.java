package me.asofold.bpl.rsp.api.impl.bpermissions;

import java.util.HashSet;
import java.util.Set;

import me.asofold.bpl.rsp.api.IPermissionSettings;
import me.asofold.bpl.rsp.api.IPermissions;
import me.asofold.bpl.rsp.api.IPermissionsFactory;


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
