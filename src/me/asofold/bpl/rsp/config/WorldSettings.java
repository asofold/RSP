package me.asofold.bpl.rsp.config;

import me.asofold.bpl.rsp.config.compatlayer.CompatConfig;

import org.bukkit.ChatColor;


/**
 * World specific settings.
 * @author mc_dev
 *
 */
public class WorldSettings {
	public boolean confine = false;
	public double cX = 0;
	public double cZ = 0;
	public double cR = 2500.0;
	public String cMessage = ChatColor.RED+"World border reached!";

	public boolean useLastLocation = true;
	public boolean circular = true;
	
	public int lazyDist = 5;
	
	public boolean fromConfig( CompatConfig cfg, String prefix){
		confine = cfg.getBoolean(prefix+"confine.enabled", false);
		cX = cfg.getDouble(prefix + "confine.center.x", 0.0);
		cZ = cfg.getDouble(prefix + "confine.center.z", 0.0);
		cR = cfg.getDouble(prefix+"confine.radius",2500.0);
		lazyDist = cfg.getInt(prefix+"heuristic.lazy-dist", 5);
		cMessage = cfg.getString(prefix+"confine.message", ChatColor.RED+"World border reached!");
		useLastLocation = cfg.getBoolean(prefix + "confine.resetto.recent", true);
		circular = cfg.getBoolean(prefix+"confine.circular", true);
		return true;
	}
}
