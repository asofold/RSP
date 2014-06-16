package me.asofold.bpl.rsp.api.impl.vault;

import java.util.UUID;

import me.asofold.bpl.rsp.api.IPermissionSettings;
import me.asofold.bpl.rsp.api.IPermissionUser;
import me.asofold.bpl.rsp.api.IPermissions;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;


public final class VaultPerms implements IPermissions {
	private final Permission perms;
	private final IPermissionSettings settings;
	
	public VaultPerms(final IPermissionSettings settings) {
		final RegisteredServiceProvider<Permission> provider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (provider == null) throw new RuntimeException("Vault Permissions are not ready.");
        perms = provider.getProvider();
        if (perms == null) throw new RuntimeException("Vault Permissions are not ready.");
        final String name = perms.getName();
        if (name.equals("PermissionsBukkit")){
        	this.settings = new IPermissionSettings() {
				@Override
				public final boolean getUseWorlds() {
					return false;
				}
				
				@Override
				public final boolean getLowerCaseWorlds() {
					return settings.getLowerCaseWorlds();
				}
				
				@Override
				public final boolean getLowerCasePlayers() {
					return true;
				}

				@Override
				public boolean getSaveAtAll() {
					return false; // TODO
				}
			};
        }
        else this.settings = settings;
	}

	@Override
	public final boolean isAvailable() {
		return perms.isEnabled();
	}

	@Override
	public final IPermissionUser getUser(final UUID id, final String player, final String world) {
		return new VaultPermsUser(perms, id, player, world, settings);
	}

	@Override
	public final void saveChanges() {
		// TODO Check if Vault added it, finally.
	}

	@Override
	public final String getInterfaceName() {
		return "rsp-vault("+perms.getName()+")";
	}

}
