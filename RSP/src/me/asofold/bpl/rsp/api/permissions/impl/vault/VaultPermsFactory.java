package me.asofold.bpl.rsp.api.permissions.impl.vault;

import java.util.HashSet;
import java.util.Set;

import me.asofold.bpl.rsp.api.permissions.IPermissionSettings;
import me.asofold.bpl.rsp.api.permissions.IPermissions;
import me.asofold.bpl.rsp.api.permissions.IPermissionsFactory;


public class VaultPermsFactory implements IPermissionsFactory {

	@Override
	public IPermissions getPermissions(IPermissionSettings settings) {
		return new VaultPerms(settings);
	}

	@Override
	public Set<String> getPluginHookNames() {
		Set<String> out = new HashSet<String>();
		out.add("Vault"); // TODO: add maybe more plugins that vault supports.
		return out;
	}

}
