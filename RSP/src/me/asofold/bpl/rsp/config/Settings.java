package me.asofold.bpl.rsp.config;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import me.asofold.bpl.rsp.config.compatlayer.CompatConfig;

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.MemoryConfiguration;



public class Settings {
	
	public static class Link{
		public final String world;
		public final String rid;
		public final List<String> defNames;
		public Link(String world, String rid, List<String> defNames){
			this.world = world;
			this.rid = rid;
			this.defNames = defNames;
		}
	}
	
	public boolean useStats = true;
	
	/**
	 * Default world specific settings.
	 * Include lazy dist .
	 */
	public WorldSettings defaults = new WorldSettings();
	
	/**
	 * General world-specific settings.
	 */
	public  Map<String, WorldSettings> worlds = new HashMap<String, WorldSettings>();
	
	// heuristic:
	public final int lazyDist = 5;
	
	public boolean useWorlds = true;
	public boolean lowerCasePlayers = true;
	public boolean lowerCaseWorlds = false;
	
	// stats:
	public boolean logStats = false;
	public boolean statsShowRange = false;
	
	// logging of errors:
	/**
	 * Minimum delay for frequent logging of errors.
	 */
	public final long minDelayLogFrequent = 10000;
	
	public long lifetimeCache = 12345;
	public long savingPeriod = 0;
	public boolean saveOnCheck = false;
	public boolean saveOnCheckOut = false;
	
	
	public boolean createPortals = false;
	
	/**
	 * Period (ticks) for check parked PlayerData for expiration.
	 */
	public long checkParkedPeriod = 1800; // TODO: -> DefaultSettings
	
	public long minDelayFrequent = 10000;
	

	/**
	 * Duration after which parked PlayerData is released.
	 */
	public long durExpireParked = 300000;
	
	/**
	 * Ticks to delay till checking further parked PlayerData entries.
	 */
	public long ticksCheckParked = 2;
	/**
	 * Number of parked PlayerData entries to check out.
	 */
	public int nExpireParked = 1;
	
	public boolean noParking = false;
	
	public boolean saveAtAll = false;
	
	public  final int defaultMaxCheckedOut = 300;
	public int maxCheckedOut = defaultMaxCheckedOut;
	
	public List<String> loadPlugins = new LinkedList<String>();
	
	/**
	 * Just as read from configuration.
	 */
	public List<ConfigPermDef> configPermDefs = new LinkedList<ConfigPermDef>();
	public List<Link> links = new LinkedList<Link>();
	public Map<LinkType, List<String>> genericLinks = new HashMap<LinkType, List<String>>();
	
	
	public static MemoryConfiguration getDefaultConfiguration(){
		MemoryConfiguration cfg = new MemoryConfiguration();
		Settings ref = new Settings();
		cfg.set("heuristic.lazy-dist", ref.lazyDist);
		cfg.set("confine.enabled", false);
		cfg.set("player-cache.lifetime", ref.lifetimeCache);
		cfg.set("stats.use", ref.useStats);
		cfg.set("stats.log", ref.logStats);
		cfg.set("stats.show.range", ref.statsShowRange);
		cfg.set("no-parking", ref.noParking);
		cfg.set("permissions.save-at-all", ref.saveAtAll);
		cfg.set("permissions.saving-period", ref.savingPeriod);
		cfg.set("permissions.save.on-check", ref.saveOnCheck);
		cfg.set("permissions.save.on-checkout", ref.saveOnCheck);
		cfg.set("permissions.use-worlds", ref.useWorlds);
		cfg.set("permissions.lower-case.players", ref.lowerCasePlayers);
		cfg.set("permissions.lower-case.worlds", ref.lowerCaseWorlds);
		cfg.set("create-portals", ref.createPortals);
		cfg.set("errors.log.min-delay", ref.minDelayLogFrequent);
		cfg.set("load-plugins", new LinkedList<String>());
		return cfg;
	}

	public static boolean forceDefaults(Configuration defaults, CompatConfig config){
		Map<String ,Object> all = defaults.getValues(true);
		boolean changed = false;
		for ( String path : all.keySet()){
			if ( !config.hasEntry(path)){
				config.setProperty(path, defaults.get(path));
				changed = true;
			}
		}
		return changed;
	}
	
	/**
	 * 
	 * @param cfg
	 * @return null on failure.
	 */
	public static Settings fromConfig(CompatConfig cfg){
		forceDefaults(getDefaultConfiguration(), cfg);
		Settings settings = new Settings();
		settings.defaults = new WorldSettings();
		if (!settings.defaults.fromConfig(cfg, "")) return null;
		settings.lifetimeCache = cfg.getLong("player-cache.lifetime", settings.lifetimeCache);
		settings.useStats = cfg.getBoolean("stats.use", settings.useStats);
		settings.logStats = cfg.getBoolean("stats.log", settings.logStats);
		settings.statsShowRange = cfg.getBoolean("stats.show.range", settings.statsShowRange);
		settings.noParking = cfg.getBoolean("no-parking", settings.noParking);
		settings.saveAtAll = cfg.getBoolean("permissions.save-at-all", settings.saveAtAll);
		settings.savingPeriod = cfg.getLong("permissions.saving-period", settings.savingPeriod/20)*20;
		settings.saveOnCheck = cfg.getBoolean("permissions.save.on-check", settings.saveOnCheck);
		settings.saveOnCheckOut = cfg.getBoolean("permissions.save.on-checkout", settings.saveOnCheckOut);
		settings.createPortals = cfg.getBoolean("create-portals", settings.createPortals);
		settings.minDelayFrequent = cfg.getLong("errors.log.min-delay", settings.minDelayLogFrequent);
		settings.useWorlds = cfg.getBoolean("permissions.use-worlds", settings.useWorlds);
		settings.lowerCaseWorlds = cfg.getBoolean("permissions.lower-case.worlds", settings.lowerCaseWorlds);
		settings.lowerCasePlayers = cfg.getBoolean("permissions.lower-case.players", settings.lowerCasePlayers);
		List<String> wKeys = cfg.getStringKeys("worlds");
		if ( wKeys != null){
			for ( String world : wKeys){
				WorldSettings ws = new WorldSettings();
				if ( !ws.fromConfig(cfg, "worlds."+world+".")){
					Bukkit.getServer().getLogger().warning("[RSP] Ignore bad world settings for: "+world);
					continue;
				}
				settings.worlds.put(world, ws);
			}
		}
		settings.loadPlugins = cfg.getStringList("load-plugins", new LinkedList<String>());
		settings.configPermDefs = ConfigPermDef.readPermDefs(cfg, "permdefs");
		wKeys = cfg.getStringKeys("links");
		if (wKeys != null){
			for (String wn : wKeys){
				List<String> rids = cfg.getStringKeys("links."+wn);
				if  (rids==null) continue;
				for ( String rid : rids){
					List<String> defNames = cfg.getStringList("links."+wn+"."+rid, null);
					if ( defNames == null) continue;
					settings.links.add(new Link(wn, rid, defNames));
				}
			}
		}
		
		// Generic permdef links
		for (LinkType linkType: new  LinkType[]{LinkType.ONLINE, LinkType.OWNERHIP}){
			List<String> gen = cfg.getStringList("generic-links."+linkType.toString().toLowerCase(), null); 
			if (gen != null){
				settings.genericLinks.put(linkType, gen);
			}
		}
		return settings;
	}

}
