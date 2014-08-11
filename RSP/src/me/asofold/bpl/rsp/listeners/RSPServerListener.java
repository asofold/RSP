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
			// TODO: Transform to generic handling.
			core.checkoutAllPlayers();
			core.setWG();
		}
		
		// extra if for perms:
		if (core.hasPluginHook(pluginName)){
			core.onPluginDisabled(pluginName);
		}
		core.onAnyPluginDisabled();
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void onPluginEnable(PluginEnableEvent event) {
		String pluginName = event.getPlugin().getDescription().getName();
		if ( pluginName.equals("WorldGuard")) {
			// TODO: Transform to generic handling.
			core.setWG();
		}
		// extra if for perms:
		if (core.hasPluginHook(pluginName)){
			core.onPluginEnabled(pluginName);
		}
		core.onAnyPluginEnabled();
	}

}
