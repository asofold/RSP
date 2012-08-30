package me.asofold.bpl.rsp;

import me.asofold.bpl.rsp.api.IRSPCore;
import me.asofold.bpl.rsp.command.RSPCommand;
import me.asofold.bpl.rsp.core.RSPCore;
import me.asofold.bpl.rsp.core.RSPTriple;
import me.asofold.bpl.rsp.listeners.RSPPlayerListener;
import me.asofold.bpl.rsp.listeners.RSPServerListener;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;



/**
 * Concept evaluation for region specific permissions (rsp),
 * using WorldGuard and PermissionsEx at first.
 * 
 * 
 * @author mc_dev
 *
 */
public class RSP extends JavaPlugin{
	private static final RSPCore core = new RSPCore(null);
	private final RSPPlayerListener playerListener = new RSPPlayerListener(core);
	private final RSPServerListener serverListener = new RSPServerListener(core);
	
	String pluginName ="RSP";
	String pluginVersion = "?";

	@Override
	public void onDisable() {
		core.clearAllPermDefs(); // does checkout and save
		core.setWG(); // TODO: set to null ?
		System.out.println(getPluginVersionString()+" is disabled.");
	}

	@Override
	public void onEnable() {
		// TODO: intercept unwanted onEnable calls
		PluginDescriptionFile pdf = getDescription();
		pluginName = pdf.getName();
		pluginVersion = pdf.getVersion();
		core.setTriple(new RSPTriple(this, playerListener));
		core.setWG();
		
		// Events and config:
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(serverListener, this);
		if (!core.reloadSettings()){
			getServer().getLogger().severe("[RSP] Failed to load config.");
			core.setPermissions();
		}
		pm.registerEvents(playerListener, this);
		
		// commands:
		getCommand("rsp").setExecutor(new RSPCommand(RSP.core));
		// TODO: check if core is ready (no errors on con fig loading) ?
		System.out.println(getPluginVersionString()+" is enabled.");
		core.recheckAllPlayers(); // TODO: maybe only do this if reloading config failed ! [actually then it should be disabled completely?]
	}
	
	public String getPluginVersionString(){
		return pluginName+"("+pluginVersion+")";
	}

	/**
	 * Api method
	 * @return
	 */
	public static IRSPCore getRSPCore(){
		return core;
	}

	/**
	 * Get the RSP Plugin instance.
	 * @return
	 */
	public static final Plugin getPluginInstance() {
		RSPTriple triple = core.getTriple();
		if ( triple == null) return null;
		return triple.plugin;
	}
}
