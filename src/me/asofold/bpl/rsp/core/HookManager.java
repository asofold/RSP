package me.asofold.bpl.rsp.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.asofold.bpl.rsp.api.permissions.IPermissionSettings;
import me.asofold.bpl.rsp.api.permissions.IPermissions;
import me.asofold.bpl.rsp.api.permissions.IPermissionsFactory;
import me.asofold.bpl.rsp.api.permissions.impl.bpermissions.BPermsFactory;
import me.asofold.bpl.rsp.api.permissions.impl.pex.PexPermsFactory;
import me.asofold.bpl.rsp.api.permissions.impl.superperms.SuperPerms;
import me.asofold.bpl.rsp.api.permissions.impl.vault.VaultPermsFactory;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;


/**
 * Manage plugin relations.
 * @author mc_dev
 *
 */
public class HookManager{
	/**
	 * Preset factories for permissions.
	 */
	private final List<IPermissionsFactory> presetFactories = new LinkedList<IPermissionsFactory>();
	/**
	 * Are associated with plugins and will be checked before presetFactories.
	 */
	private final List<IPermissionsFactory> extNonEmptyFactories = new LinkedList<IPermissionsFactory>();
	/**
	 * Are not associated with plugins and will be checked last.
	 */
	private final List<IPermissionsFactory> extEmptyFactories = new LinkedList<IPermissionsFactory>();
	
	private final Set<String> currentHooks = new HashSet<String>();
	private final Set<String> allHooks = new HashSet<String>();
	
	/**
	 * Current permissions hook in use.
	 */
	private IPermissions permissions = new SuperPerms();

	private IPermissionSettings settings = null;
	
	public HookManager(){
		try{
			presetFactories.add(new PexPermsFactory());
		} catch(Throwable t){
		}
		try{
			presetFactories.add(new BPermsFactory());
		} catch(Throwable t){
		}
//		try{
//			presetFactories.add(new PermBukkitPermsFactory());
//		} catch(Throwable t){
//		}
		try{
			presetFactories.add(new VaultPermsFactory());
		} catch(Throwable t){
		}
	}
	
	/**
	 * Find an enabled plugin that can be used.
	 */
	public IPermissions setPermissions(){
		this.permissions = null;
		currentHooks.clear();
		allHooks.clear();
		Map<String, Plugin> plugins = new HashMap<String, Plugin>();
		for ( Plugin plugin : Bukkit.getServer().getPluginManager().getPlugins()){
			if ( plugin.isEnabled() ) plugins.put(plugin.getDescription().getName(), plugin);
		}
		for ( IPermissionsFactory factory : extNonEmptyFactories){
			Set<String> hooks = factory.getPluginHookNames();
			allHooks.addAll(hooks);
			for ( String hook : hooks){
				if (plugins.containsKey(hook)){
					if ( trySetPermissions(hook, factory)) return permissions;
				}
			}
		}
		for ( IPermissionsFactory factory : presetFactories){
			Set<String> hooks = factory.getPluginHookNames();
			allHooks.addAll(hooks);
			for ( String hook : hooks){
				if (plugins.containsKey(hook)){
					if (trySetPermissions(hook, factory)) return permissions;
				}
			}
		}
		for ( IPermissionsFactory factory : extEmptyFactories){
			if ( trySetPermissions(null, factory)) return permissions;
		}
		
		permissions = new SuperPerms();
		System.out.println("[RSP] No compatible permissions plugin found, using superperms (no external groups).");
		return permissions;
	}
	
	/**
	 * Attempt to set internal interface.
	 * @param hook
	 * @param factory
	 * @return
	 */
	public boolean trySetPermissions( String hook, IPermissionsFactory factory){
		try{
			permissions = factory.getPermissions(settings);
			Set<String> hooks = factory.getPluginHookNames();
			if ( hooks != null){
				currentHooks.clear();
				currentHooks.addAll(hooks);
			}
			if (hook == null) hook = "";
			System.out.println("[RSP] Using for permissions: "+hook+" ("+permissions.getInterfaceName()+")");
			return true;
		} catch ( Throwable t){
			return false;
		}
	}


	public void addPermissionsFactory(IPermissionsFactory factory) {
		Set<String> hooks = factory.getPluginHookNames();
		if ( hooks == null ){
			extEmptyFactories.add(0, factory);
		} else if ( hooks.isEmpty()){
			extEmptyFactories.add(0, factory);
		} else extNonEmptyFactories.add( 0, factory);
		setPermissions();
	}
	
	public boolean hasPluginHook(String pluginName){
		return allHooks.contains(pluginName);
	}

	public IPermissions getPermissions() {
		return permissions;
	}

	public IPermissionSettings getPermissionSettings() {
		return settings;
	}

	public void setPermissionSettings(IPermissionSettings settings) {
		this.settings  = settings;
	}

	public IPermissions setPermissionsOnDisable(String pluginName) {
		return setPermissions(); // TODO: more specific ?
	}

	public IPermissions setPermissionsOnEnable(String pluginName) {
		return setPermissions(); // TODO: more specific ?
	}

}
