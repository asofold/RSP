package me.asofold.bpl.rsp.listeners;

import me.asofold.bpl.rsp.core.RSPCore;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;


public class RSPServerListener implements Listener {
	private RSPCore core;

	public RSPServerListener(RSPCore core){
		this.core = core;
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPluginDisable(PluginDisableEvent event) {
		String pluginName = event.getPlugin().getDescription().getName();
		if ( pluginName.equals("WorldGuard")){
			// TODO: move this to hooks as well ?
			core.checkoutAllPlayers();
			core.setWG();
		} 
		
		// extra if for perms:
		if (core.hasPluginHook(pluginName)){
			core.onPluginDisabled(pluginName);
		}
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void onPluginEnable(PluginEnableEvent event) {
		String pluginName = event.getPlugin().getDescription().getName();
		if ( pluginName.equals("WorldGuard")) core.setWG(); // TODO: move this to hooks as well ?
		// extra if for perms:
		if (core.hasPluginHook(pluginName)){
			core.onPluginEnabled(pluginName);
		}
	}

}
