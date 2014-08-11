package me.asofold.bpl.rsp.plshared.players;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

/**
 * Keep references of online players by exact and lower-case-name and lower-case prefixes.<br>
 * @author mc_dev
 *
 */
public class OnlinePlayerMap extends SimplePlayerMap{
	
	private final Listener listener = new Listener() {
		@EventHandler(priority = EventPriority.LOWEST)
		public void onJoin(final PlayerJoinEvent event){
			addPlayer(event.getPlayer());
		}
		@EventHandler(priority = EventPriority.MONITOR)
		public void onQuit(final PlayerQuitEvent event){
			removePlayer(event.getPlayer());
		}
		@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
		public void onKick(final PlayerKickEvent event){
			removePlayer(event.getPlayer());
		}
	};
	
	/**
	 * This will re-initialize with all byName players, should be called in onEnable.
	 * @param plugin
	 */
	public void initWithOnlinePlayers(){
		clear();
		final Player[] players = Bukkit.getOnlinePlayers();
		for (int i = 0; i < players.length; i ++){
			addPlayer(players[i]);
		}
	}
	
	/**
	 * Register listener with the given plugin.
	 * @param plugin
	 */
	public void registerOnlinePlayerListener(final Plugin plugin){
		Bukkit.getPluginManager().registerEvents(listener, plugin);
	}
	
}
