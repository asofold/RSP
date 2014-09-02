package me.asofold.bpl.rsp.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import me.asofold.bpl.rsp.RSP;
import me.asofold.bpl.rsp.api.IRSPCore;
import me.asofold.bpl.rsp.api.RSPReloadEvent;
import me.asofold.bpl.rsp.api.permissions.IPermissionSettings;
import me.asofold.bpl.rsp.api.permissions.IPermissionUser;
import me.asofold.bpl.rsp.api.permissions.IPermissions;
import me.asofold.bpl.rsp.api.permissions.IPermissionsFactory;
import me.asofold.bpl.rsp.api.permissions.impl.superperms.SuperPerms;
import me.asofold.bpl.rsp.api.regions.IRegionResultHook;
import me.asofold.bpl.rsp.api.regions.IRegions;
import me.asofold.bpl.rsp.api.regions.RegionResult;
import me.asofold.bpl.rsp.api.regions.impl.noregions.NoRegions;
import me.asofold.bpl.rsp.api.regions.impl.worldguard.WGRegions;
import me.asofold.bpl.rsp.config.ConfigPermDef;
import me.asofold.bpl.rsp.config.PermDef;
import me.asofold.bpl.rsp.config.Settings;
import me.asofold.bpl.rsp.config.WorldSettings;
import me.asofold.bpl.rsp.config.compatlayer.CompatConfig;
import me.asofold.bpl.rsp.config.compatlayer.CompatConfigFactory;
import me.asofold.bpl.rsp.permissions.PermissionUtil;
import me.asofold.bpl.rsp.permissions.TransientMan;
import me.asofold.bpl.rsp.plshared.Players;
import me.asofold.bpl.rsp.stats.Stats;
import me.asofold.bpl.rsp.utils.BlockPos;
import me.asofold.bpl.rsp.utils.Utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;


/**
 * Core functionality (detached from the plugin).
 * TODO: isReady method
 * @author mc_dev
 *
 */
public class RSPCore implements IRSPCore{
	
	//////////////////// ---
	// STATIC MEMBERS
	//////////////////// ---
	
	private static String grpDetails(final String userName, final String worldName, final String grp) {
		return userName+"@"+worldName+":"+grp;
	}
	
	private static final int defaultMaxCheckedOut = 300;
	
	private static final Set<String> reservedRids = new LinkedHashSet<String>(Arrays.asList(new String[]{
			"__global__", "__owner__", "__member__", "__region__"
	}));
	
	// Stats
	public static final Stats stats = new Stats("[RSP][STATS]");
	public static final Integer CHECKOUT_PARK = stats.getNewId("CheckoutParked");
	public static final Integer CHECKOUT_ALL = stats.getNewId("CheckoutAll");
	public static final Integer SAVE_CHANGES = stats.getNewId("SaveChanges");
	public static final Integer PLAYER_CHANGED_WORLD = stats.getNewId("CheckWorldChange");
	public static final Integer PLAYER_LOGIN = stats.getNewId("CheckLogin");
	public static final Integer PLAYER_JOIN = stats.getNewId("CheckJoin");
	public static final Integer DELAYED_CHECK = stats.getNewId("CheckDelayed");
	public static final Integer PLAYER_MOVE = stats.getNewId("CheckMove");
	public static final Integer VEHICLE_ENTER = stats.getNewId("CheckVehicleEnter");
	public static final Integer VEHICLE_EXIT = stats.getNewId("CheckVehicleExit");
	public static final Integer PLAYER_PORTAL = stats.getNewId("CheckPortal");
	public static final Integer PLAYER_RESPAWN = stats.getNewId("CheckRespawn");
	public static final Integer PLAYER_TELEPORT = stats.getNewId("CheckTeleport");
	
	
	////////////////////// ---
	// INSTANCE MEMBERS
	////////////////////// ---
	
	private RSPTriple triple = null; 

	/**
	 * Preset to something.
	 */
	private IPermissions permissions = new SuperPerms();
	
	private IRegions regions = new NoRegions();
	private final RegionResult regionResult = new RegionResult();
	
	//////////////
	// Settings
	//////////////
			
	/** Global settings instance. */
	protected Settings settings = new Settings();
	
	/**
	 * General world-specific settings.
	 */
	private final  Map <String, WorldSettings> worlds = new LinkedHashMap<String, WorldSettings>();
	
	
	///////////////////////////////
	// Managers and Hooks.
	///////////////////////////////
	
	private final HookManager hookManager;
	
	private final PermDefManager pdMan;
	
	private final TransientMan transientMan;
	
	/** Registered for checking the ApplicableRegionSet.*/
	private final List<IRegionResultHook> regionResultHooks = new ArrayList<IRegionResultHook>();
	
	/////////////////
	// Player Data
	/////////////////
	
	protected int taskIdUpdateAll = -1;
	
	/**
	 * OfflinePlayerData by lower-case (!) player name. Data is kept only until the UUID is known.
	 */
	final Map<String, OfflinePlayerData> offlinePlayerDataByName = new HashMap<String, OfflinePlayerData>();
	
	/**
	 * OfflinePlayerData by UUID.
	 */
	final Map<UUID, OfflinePlayerData> offlinePlayerDataById = new HashMap<UUID, OfflinePlayerData>();
	
	/**
	 * Active players data. Exact name.
	 */
	final Map<String, PlayerData> playerData = new LinkedHashMap<String, PlayerData>();
	
	/**
	 * Parked playerData. Exact name.
	 */
	final Map<String, PlayerData> parked = new LinkedHashMap<String, PlayerData>();
	
	/**
	 * Maximum size of checked-out storage.
	 */
	int maxCheckedOut = defaultMaxCheckedOut;
	
	/**
	 * To remember checked out players and save some time on rejoin.
	 */
	final Set<String> checkedOut = new LinkedHashSet<String>();
	
	///////////////////////
	// Cache / Temporary
	///////////////////////
	
	/**
	 * Last error timestamp of the kind of (frequent) error.
	 */
	private final Map<RSPError, Long> errorTs = new HashMap<RSPError, Long>();
	
	/** For temporary use only, always call setWorld(null) after use. */
	protected final Location useLoc = new Location(null, 0, 0, 0);
	
	
	////////////////////// ---
	// METHODS
	////////////////////// ---
	
	public RSPCore(RSPTriple triple) {
		this.setTriple(triple);
		
		hookManager = new HookManager(); // might fail.
		pdMan = new PermDefManager(this);
		transientMan = new TransientMan(this);
	}
	
	/**
	 * TODO: how about runtime settings ? -> policy = add settings from config, remove only those from config (!)
	 * @return
	 */
	public boolean reloadSettings() {
		// checkout players
		checkoutAllPlayers(false);
		// unregister all permdefs read from config
		pdMan.unregisterAllPermdefs();
		boolean res = false;
		Throwable ref = null;
		// do reload
		try{
			res = uncheckedReloadSettings();
			res &= uncheckedReloadPlayerSettings();
			setPermissions();
			transientMan.updateChildrenPermissions();
			recheckAllPlayers(); // TODO: Consider reCheck ?
			scheduleTasks();
		} catch (Throwable t) {
			Bukkit.getLogger().severe("[RSP] Failed to load configuration: " + t.getMessage());
			Bukkit.getLogger().log(Level.SEVERE, "[RSP] Exception: ", t);
			res = false;
			ref = t;
		}
		Bukkit.getPluginManager().callEvent(new RSPReloadEvent(triple.plugin, res, ref));
		return res;
	}
	
	public boolean uncheckedReloadPlayerSettings() {
		// Clear present data.
		offlinePlayerDataById.clear();
		offlinePlayerDataByName.clear();
		// Load data.
		final File file = new File(triple.plugin.getDataFolder(), "players.yml");
		final CompatConfig cfg = CompatConfigFactory.getConfig(file);
		if (!file.exists()) {
			// Create an empty file.
			cfg.save();
			return true;
		}
		cfg.load();
		for (final String key : cfg.getStringKeys()) {
			OfflinePlayerData opd = OfflinePlayerData.fromConfig(cfg, "");
			if (opd != null) {
				if (opd.uuid != null) {
					offlinePlayerDataById.put(opd.uuid, opd);
				} else if (opd.playerName != null) {
					offlinePlayerDataByName.put(opd.playerName, opd);
				}
				// (No else.)
			} else {
				triple.plugin.getLogger().warning("Bad or no entry in players.yml for key: " + key);
			}
		}
		return true;
	}

	boolean uncheckedReloadSettings() {
		File file = new File(triple.plugin.getDataFolder(), "rsp.yml");
		CompatConfig cfg = CompatConfigFactory.getConfig(file);
		boolean changed = false;
		if (!file.exists()) {
			changed = true;
		} else {
			cfg.load();
		}
		changed |= Settings.forceDefaults(Settings.getDefaultConfiguration(), cfg);
		if (changed) cfg.save();
		return applySettings(cfg);
	}
	
	public boolean applySettings(CompatConfig cfg) {
		final Settings settings = Settings.fromConfig(cfg);
		if (settings == null) return false;
		this.settings = settings; // TODO: Order.
		setUseStats(settings.useStats);
		stats.setLogStats(settings.logStats);
		stats.setShowRange(settings.statsShowRange);
		hookManager.setPermissionSettings(new IPermissionSettings() {
			@Override
			public boolean getUseWorlds() {
				return settings.useWorlds;
			}
			@Override
			public boolean getLowerCaseWorlds() {
				return settings.lowerCaseWorlds;
			}
			
			@Override
			public boolean getLowerCasePlayers() {
				return settings.lowerCasePlayers;
			}
			@Override
			public boolean getSaveAtAll() {
				return settings.saveAtAll; // TODO
			}
		});
		// TODO: check consistency
		
		worlds.clear();
		worlds.putAll(settings.worlds);
		
		if (!pdMan.applySettings(settings)) { // TODO: put these to settings too ?
			// TODO ??
		}
		
		transientMan.clear();
		transientMan.fromConfig(cfg);
		
		PluginManager pm = Bukkit.getServer().getPluginManager();
		
		for (String n : settings.loadPlugins) {
			Plugin plg = pm.getPlugin(n);
			if (plg == null) continue;
			if (!pm.isPluginEnabled(n)) {
				try{
					pm.enablePlugin(plg);
				} catch (Throwable t) {
					Bukkit.getServer().getLogger().severe("[RSP] Failed to enable plugin '"+n+"': "+t.getMessage());
					t.printStackTrace();
				}
			}
		}
		
		
		
		return true;
	}
	
//	public void checkAllPlayers() {
//		//if (!permissions.isAvailable()) return;
//		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
//			try{
//				check(player.getUniqueId(), player.getName(), player.getLocation(useLoc));
//				useLoc.setWorld(null);
//			} catch (Throwable t) {
//				System.out.println("[RSP] Failed to check player: "+player.getName());
//			}
//		}
//		
//	}

	public void onScheduledSave() {
		forceSaveChanges();
	}
	
	public void setWG() {
		regions = new NoRegions();
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
		if (plugin == null || !plugin.isEnabled()) {
			// TODO: Delegate such check to the factories as well.
			return;
		}
		try {
			regions = new WGRegions();
		}
		catch (RuntimeException e1) {}
		catch (Throwable e2) {} // TODO
	}

	public RSP getPlugin() {
		return triple.plugin;
	}
	
	public RSPTriple getTriple() {
		return triple;
	}

	public void setTriple(RSPTriple triple) {
		boolean sched = false;
		if ((triple != null) && (triple.plugin != null)) {
			if (this.triple==null || triple.plugin != this.triple.plugin) sched = true;
		}
		this.triple = triple;
		maxCheckedOut = Math.max(defaultMaxCheckedOut, Bukkit.getServer().getMaxPlayers()*3);
		if (sched) scheduleTasks();
	}
	
	/**
	 * Ensures that data exists, use case sensitive name.
	 * @param playerName
	 * @return
	 */
	final PlayerData getData(final UUID id, final String playerName) {
		
		
		// TODO: Consistency checking.
		
		// Data from active players:
		PlayerData data = playerData.get(playerName);
		if (data != null) {
			return data;
		}
		data = parked.remove(playerName); // Data from parked players:
		if (data == null) {
			data = new PlayerData(id, playerName);	// Newly created player data
			checkedOut.remove(playerName); // if that should be the case.
		}
		playerData.put(playerName, data);
		updateByOfflineData(data);
		return data;
	}
	
	private void updateByOfflineData(final PlayerData data) {
		OfflinePlayerData opd = offlinePlayerDataById.get(data.id);
		if (opd == null) {
			opd = offlinePlayerDataByName.remove(data.playerName.toLowerCase());
			if (opd != null) {
				opd.uuid = data.id;
				offlinePlayerDataById.put(data.id, opd);
				saveOfflinPlayerData(); // TODO: async save / set dirty.
			}
		}
		if (opd != null) {
			// Add groups.
		}
	}
	
	private void saveOfflinPlayerData() {
		// TODO Auto-generated method stub
	}

	/**
	 * 
	 * @param error
	 * @param details
	 * @return if logged.
	 */
	public boolean logSevere(RSPError error, String details) {
		if ((settings.minDelayFrequent != 0) && (error != null)) {
			// check if to display this error (again, potentially):
			long ts = System.currentTimeMillis();
			Long ots = errorTs.get(error);
			if (ots == null) {
				errorTs.put(error, ts);
			}
			else if (ts-ots<=settings.minDelayFrequent) return false; // ignore
			else errorTs.put(error, ts);
		}
		String message = "[RSP] Serious problem: ";
		switch(error) {
		case NULL_LOCATION:
			message += "Location is null.";
			break;
		case NULL_WORLD:
			message += "World is null.";
			break;
		case NOT_PRESENT_REGIONS:
			message += "Region checking is not present (WorldGuard).";
			break;
		case NOT_PRESENT_PERMISSIONS:
			message += "Permissions are not present.";
			break;
		case NOT_AVAILABLE_PERMISSIONS:
			message += "Permissions are not available.";
			break;
		case FAILED_SCHEDULING_PARKED:
			message += "Failed to schedule checking task for parked PlayerData.";
			break;
		case SAVE_PERMISSIONS:
			message += "Failed to save permissions.";
			break;
		case ERROR_PERMISSIONS:
			message += "Permissions are inoperable.";
			break;
		case ADD_GROUP:
			message += "Failed to add a group.";
			break;
		case REMOVE_GROUP:
			message += "Failed to remove a group.";
			break;
		}
		if (details != null) message += " (details: "+details+")";
		triple.plugin.getServer().getLogger().severe(message);
		return true;
	}
	
	public void checkDelayed(final String playerName) {
		final long ts = settings.useStats ? System.nanoTime() : 0L;
		final Player player = Players.getPlayerExact(playerName);
		if (player != null) {
			// TODO: More against inconsistent states (clear groups + full re-check)?
			if (settings.heavyDelayedCheck) {
				recheck(player, true);
			} else {
				final PlayerData data = getData(player.getUniqueId(), playerName);
				data.forceCacheExpiration();
				check(data.id, playerName, player.getLocation(useLoc));
			}
			useLoc.setWorld(null);
			if (settings.useStats) {
				RSPCore.stats.addStats(RSPCore.DELAYED_CHECK, System.nanoTime() - ts);
			}
		}
	}
	
	public void checkAndCheckDelayed(final UUID playerId, final String playerName, final Location loc) {
		// TODO: Mechanics with check: check might remove task for normal moving?
		// TODO: While registered: other events like interact should also trigger check + cancel.
		check(playerId, playerName, loc);
		getData(playerId, playerName).checkTask.registerIfIdle(this);
	}
	
	/**
	 * POLICY: If wg is not set, assume all perms to be forfeit as with logout(check all regions, but use cache).
	 * @param player
	 */
	public void check(final UUID playerId, final String playerName, final Location loc) {// Player player, Location loc) {
//		String playerName = player.getName();
		// TODO: cleanup for speed !
		if (loc == null) { // TODO: SUBJECT TO REMOVAL -> RATHER NOT :)
			checkout(playerId, playerName, false);
			logSevere(RSPError.NULL_LOCATION, "check: " + playerName);
			return;
		}
		final World world = loc.getWorld();
		if (world == null) { // TODO: SUBJECT TO REMOVAL
			checkout(playerId, playerName, false);
			logSevere(RSPError.NULL_WORLD, "check: " + playerName);
			return;
		}
		final PlayerData data = getData(playerId, playerName);
		final String worldName = world.getName();
		WorldSettings settings = worlds.get(worldName);
		if (settings == null) settings = this.settings.defaults;
		boolean withinLazyDist = false;
		int lazyDist = Math.min(settings.lazyDist, data.minLazyDist);
		if (data.checkPos != null) {
			withinLazyDist = !data.checkPos.setOnDist(loc, lazyDist);
		}
		if (settings.confine && !withinLazyDist) {
			// TODO: This might keep permissions, which are meant to be removed. [Might add some abuse protection, checkout on abuse.]
			if (!Confinement.checkConfinement(settings, data, loc)) return;
		}
		if (!permissions.isAvailable()) { // TODO: SUBJECT TO REMOVAL (?)
			logSevere(RSPError.NOT_AVAILABLE_PERMISSIONS, "check");
			return;
		}
		final Map<String, Integer>  ridIdMap = pdMan.regionIdMap.get(worldName);
		if (ridIdMap == null) {
			// no defs in that world
			// TODO: set checked so faster return must be possible!
			// TODO: maybe set checkPos to have lazydist applied !
			if (!data.idCache.isEmpty()) {
				// TODO: stats ?
				checkout(playerId, playerName, false);
			}
			return;
		}
		
		final IPermissionUser user; 
		boolean groupsChanged = false; // PlayerData has groups set to be applied to the permission user.
		boolean userChanged = false; // Permission user is changed.
		boolean prepared = false;
		lazyDist = settings.lazyDist;
		// Check cache expiration:
		final boolean checkExpire = data.checkCache(this.settings.lifetimeCache);
		if (checkExpire) {
			user = permissions.getUser(playerId, playerName, worldName);
			if (!data.idCache.isEmpty()) {
				user.prepare();
				prepared = true;
				for (Integer id : data.idCache) {
					final PermDefData pd = pdMan.idDefMap.get(id);
					if (pd == null) continue; // TODO: maybe remove (contract	).
					if (data.checkExpire(user, pd, id)) {
						groupsChanged = true;
					}
				}
			}
			withinLazyDist = false;
		}
		else if (withinLazyDist) {
			// User can't have been changed.
			return; // (location heuristic)
		}
		else user = permissions.getUser(playerId, playerName, worldName);
		data.checkPos = new BlockPos(loc);
		
		final Set<Integer> active = data.idCache;
		final int nActive = active.size();
		
		// TODO: ? REFACTOR (account for not used because not registered ids)
		
		final boolean regChanged = regions.setRegionResult(loc, playerName, data.id, regionResult);
		int nMatched = 0;
		final List<Integer> newIds = new LinkedList<Integer>();
		final Set<Integer> matched = new LinkedHashSet<Integer>();
		for (final String rid : regionResult.getIds()) {
			if (reservedRids.contains(rid)) {
				continue;
			}
			final Integer id = ridIdMap.get(rid);
			if (id != null) {
				if (active.contains(id)) {
					nMatched++;
					matched.add(id);
				} else {
					newIds.add(id);
				}
			} // else: ignore this region.
		}
		
		// Generic ids ---
		// __global__: Everywhere.
		final Integer wGlobalId = ridIdMap.get("__global__");
		if (wGlobalId != null) {
			if (active.contains(wGlobalId)) {
				nMatched ++;
				matched.add(wGlobalId);
			}
			else {
				newIds.add(wGlobalId);
			}
		}
		// __owner__: Region owners.
		if (regionResult.isOwner()) {
			final Integer wOwnerId = ridIdMap.get("__owner__");
			if (wOwnerId != null) {
				if (active.contains(wOwnerId)) {
					nMatched ++;
					matched.add(wOwnerId);
				}
				else {
					newIds.add(wOwnerId);
				}
			}
		}
		// __member__: Region members.
		if (regionResult.isMember()) {
			final Integer wMemberId = ridIdMap.get("__member__");
			if (wMemberId != null) {
				if (active.contains(wMemberId)) {
					nMatched ++;
					matched.add(wMemberId);
				}
				else {
					newIds.add(wMemberId);
				}
			}
		}
		// __region__: Players on regions.
		if (regChanged && regionResult.hasIds()) { // TODO: !isEmpty
			final Integer wRegionId = ridIdMap.get("__region__");
			if (wRegionId != null) {
				if (active.contains(wRegionId)) {
					nMatched ++;
					matched.add(wRegionId);
				}
				else {
					newIds.add(wRegionId);
				}
			}
		}
		
		if (regChanged) {
			regionResult.reset();
		}
		
		// Check for ids to remove ---
		
		if (nMatched < nActive) { // TODO: consistency of this with PermDefdata guaranteed?
			// REGION EXIT
			// needs adjustment ("complicated") !
			// check which have to be removed. => might need a permdef-name cache as well (reference count)?
			// remove perms for not present groups:
			final List<Integer> rem = new LinkedList<Integer>();
			for (final Integer id : active) {
				if (!matched.contains(id)) {
					// TODO: check if not inside of add + police ?
					rem.add(id);
				}
			}
			if (!rem.isEmpty()) {
				if (!prepared) {
					user.prepare();
					prepared = true;
				}
				for (final Integer id : rem) {
					final PermDefData defs = pdMan.idDefMap.get(id);
					if (defs == null) continue; // TODO: internal error
					if (data.checkExit(user, defs, id)) {
						groupsChanged = true;
					}
				}
			}
		} 
		// else assert nMatched == active.size();		
		
		// Check through active for lazy dist.
		for (final Integer id : active) {
			// Group kept.
			final PermDefData defs = pdMan.idDefMap.get(id);
			if (defs != null) {
				lazyDist = Math.min(lazyDist, defs.minLazyDist);
				if (checkExpire || groupsChanged) {
					if (data.checkEnter(user, defs, id, false)) {
						groupsChanged = true;
					}
				}
			}
		}
				
		// add perms for new ids:
		// REGION ENTER [def eneter, actually]
		if (!newIds.isEmpty()) {
			if (!prepared) {
				user.prepare();
				prepared = true;
			}
			for (Integer id : newIds) {
				final PermDefData defs = pdMan.idDefMap.get(id);
				if (defs == null) {
					// TODO: internal error.	
					continue; 
				}
				lazyDist = Math.min(lazyDist, defs.minLazyDist);
				if (data.checkEnter(user, defs, id, true)) {
					groupsChanged = true;
				}
				// TODO: also add others ?
			}
		}
		
		data.minLazyDist = lazyDist;
		data.isChecked = true;
		if (groupsChanged) {
			if (PermissionUtil.changeGroups(playerName, transientMan, user, data.groups, true, false)) {
				userChanged = true;
			}
		}
		if (prepared) {
			if (userChanged) {
				user.applyChanges();
			}
			else {
				user.discardChanges();
			}
		}
		if (this.settings.saveOnCheck && userChanged) {
			forceSaveChanges(); // TODO: maybe deprecate this anyway.
		}
		
		// Further general calls.
		if (!regionResultHooks.isEmpty()) {
			for (int i = 0; i < regionResultHooks.size(); i++) {
				try{
					regionResultHooks.get(i).onRegionResult(playerName, data.id, world, regionResult);
				}
				catch (Throwable t) {
					// TODO: log.
				}
			}
		}
	}
	
	@Override
	public boolean isWithinBounds(final Location loc) {
		final World w = loc.getWorld();
		final String wn = w.getName();
		WorldSettings s = worlds.get(wn);
		if (s == null) {
			s = this.settings.defaults;
			this.worlds.put(wn, s);
		}
		return Confinement.isWithinBounds(s, loc);
	}
	
	/**
	 * TODO: also remove all other perms from groups that are configured to be removed.
	 * @param playerName
	 * @param heavy If to check for all permdefs.
	 */
	public void checkout(final UUID id, final String playerName, boolean heavy) { //Player player) {
		// check all given groups or ALL if not checked.
		if (checkedOut.size() > maxCheckedOut) releaseCheckedOut();
		final PlayerData data = getData(id, playerName);
		
		// TODO: Split to checkout(data), because later name changes might be happening. 
		
		data.forceCacheExpiration();
		data.checkTask.cancel();
		data.lastValidLoc.setWorld(null);
		if (!permissions.isAvailable()) return; // TODO: maybe not
		Set<Integer> ids = new LinkedHashSet<Integer>();
		if (!data.isChecked) {
			if (heavy) ids.addAll(pdMan.idDefMap.keySet());
		} else {
			ids.addAll(data.idCache);
		}
		if (removePermsById(id, playerName, ids)) {
			if (this.settings.saveOnCheckOut) forceSaveChanges();
		}
		data.clearCache();
		playerData.remove(playerName);
		checkedOut.add(playerName);
	}
	
	private void releaseCheckedOut() {
		checkedOut.clear(); // TODO: maybe something more sophisticatd (release half, randomly ?)
	}


	
	/**
	 * Park PlayerData.
	 * @param playerName
	 */
	public void park(final UUID id, final String playerName) {
		if (this.settings.noParking) {
			checkout(id, playerName, false);
			return;
		}
		PlayerData data = playerData.remove(playerName);
		if (data==null) return;
		data.tsCache = System.currentTimeMillis(); // TODO: maybe use extra ts.
		parked.put(playerName, data);
	}
	
	/**
	 * Check parked PlayerData for expiration.<br>
	 * This does a certain number then schedules a sync delayed task.
	 * @param n 
	 */
	public void checkParked() {
		long ts = System.currentTimeMillis() - this.settings.durExpireParked;
		List<PlayerData> rem = new LinkedList<PlayerData>();
		int n = 0;
		for (PlayerData data : parked.values()) {
			if (data.tsCache >= ts) {
				continue;
			}
			rem.add(data);
			n++;
			if (n >= this.settings.nExpireParked && n<parked.size()) {
				if (Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(triple.plugin, new Runnable() {
					@Override
					public void run() {
						checkParked();
					}
				}, this.settings.ticksCheckParked) != -1) {
					// Scheduled.
					break;
				}
				else {
					logSevere(RSPError.FAILED_SCHEDULING_PARKED, null);
					n = 0;
				}
			}	
		}
		for (final PlayerData data : rem) {
			if (this.settings.useStats) {
				long ns = System.nanoTime();
				checkout(data.id, data.playerName, false); 
				stats.addStats(CHECKOUT_PARK, System.nanoTime()-ns);
			} 
			else {
				checkout(data.id, data.playerName, false); 
			}
		}
	}
	
	public void onPlayerJoin(final Player player) {
		checkJoin(player.getUniqueId(), player.getName(), player.getLocation(useLoc), true);
		useLoc.setWorld(null);
	}
	
	public void onPlayerLeave(final Player player) {
		park(player.getUniqueId(), player.getName());
	}
	
	public void checkJoin(final UUID id, final String playerName, final Location loc, final boolean forceShallow) {
		if (forceShallow || checkedOut.contains(playerName) || parked.containsKey(playerName)) {
			// TODO: maybe remove from transientMan
			transientMan.updatePlayer(playerName, true); // TODO: maybe something more efficient (flag in player data).
			check(id, playerName, loc);
		} else {
			recheck(id, playerName, loc, true);
		}
	}
	
	/**
	 * Convenience: checkout and check.
	 * Currently a heavy method.
	 * @param player
	 */
	public void recheck(final Player player, final boolean heavy) {
		recheck(player.getUniqueId(), player.getName(), player.getLocation(useLoc), heavy);
		useLoc.setWorld(null);
	}
	
	public void recheck(final UUID id, final String playerName, final Location loc, final boolean heavy) {
		checkout(id, playerName, heavy);
		check(id, playerName, loc);
	}


	public void recheckAllPlayers() {
//		if (!permissions.isAvailable()) return;
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			try{
				recheck(player, true);
			} catch (Throwable t) {
				System.out.println("[RSP] Failed to recheck player: "+player.getName());
			}
		}
	}

	public void checkoutAllPlayers() {
		checkoutAllPlayers(true);
	}

	public void checkoutAllPlayers(boolean heavy) {
		// if (!permissions.isAvailable()) return;
		Collection<PlayerData> check = new LinkedList<PlayerData>();
		if (heavy) {
			for (Player player : Bukkit.getServer().getOnlinePlayers()) {
				check.add(getData(player.getUniqueId(), player.getName()));
			}
		} else {
			check.addAll(playerData.values());
		}
		check.addAll(parked.values());
		for (PlayerData data : check) {
			try{
				if (this.settings.useStats) {
					long ns = System.nanoTime();
					checkout(data.id, data.playerName, heavy);
					stats.addStats(CHECKOUT_ALL, System.nanoTime()-ns);
				}
				else checkout(data.id, data.playerName, heavy);
			} catch (Throwable t) {
				System.out.println("[RSP] Failed to checkout player: " + data.playerName + " / " + data.id.toString());
			}
//			if (saveOnCheckOutAll) {
////				permissions.getUser(playerName).save();
//			}
		}
		if (checkedOut.size() > maxCheckedOut) releaseCheckedOut(); // TODO: 
	}
	
	/**
	 * Remove all groups unless user has ignore perm.
	 * This checks the permission user explicitely (no cache).
	 * NO other checks. Does NOT use or fetch PlayerData.
	 * @param user
	 * @param rids
	 */
	public boolean removePermsById(final UUID playerId, final String playerName, final Set<Integer> ids) {
		boolean changed = false;
		boolean trChanged = false;
		final Map<String, IPermissionUser> uMap = new LinkedHashMap<String, IPermissionUser>();
		for (Integer id: ids) {
			final PermDefData defs = pdMan.idDefMap.get(id);
			if (defs == null) continue;
			final IPermissionUser user;
			if (uMap.containsKey(defs.worldName)) { // TODO: this is crap there should only be one world involved.
				user = uMap.get(defs.worldName);
			}
			else {
				user = permissions.getUser(playerId, playerName, defs.worldName);
				user.prepare();
				uMap.put(defs.worldName, user);
			}
			for(PermDef def : defs.defRemExit) {
				if (def == null) continue; // safety
				if (def.ignoreRemove(user)) {
					// Other policy ?
					continue;
				}
				// Do mind that the filter-permission is not checked here (!).
				// remove perm groups
				for (String gr : def.grpRemExit) {
					if (transientMan.isTransient(gr)) {
						if (transientMan.removeGroupFromPlayer(playerName, gr, false)) trChanged = true;
					}
					else if (user.inGroup(gr)) {
						user.removeGroup(gr);
						changed = true;
					}
				}
			}
		}
		if (trChanged) {
			transientMan.updatePlayer(playerName);
		}
		if (changed) {
			for (String wn : uMap.keySet()) {
				IPermissionUser user = uMap.get(wn);
				if (!user.applyChanges()) {
					onGroupChangeFailure(user.getUniqueId(), playerName, wn);
				}
			}
		}
		else {
			for (IPermissionUser user : uMap.values()) {
				user.discardChanges();
			}
		}
		return changed;
	}
	
	public void clearAllPermDefs() {
		checkoutAllPlayers(false);
		try {
			forceSaveChanges();
		} catch (Throwable t) {
			Bukkit.getServer().getLogger().severe("[RSP] clearAllPermDefs: Failed to save changes.");
		}
		pdMan.removeAllPermDef();
	}
	
	/**
	 * For commands and slow checks.
	 * @param sender
	 * @param perm
	 * @return
	 */
	public boolean hasPermission(CommandSender sender, String perm) {
		if (sender.hasPermission(perm)) {
			return true;
		}
		if (!(sender instanceof Player)) {
			// Not sure if to keep this.
			return sender.isOp();
		} else {
			return false;
		}
	}


	public boolean getUseStats() {
		return triple.playerListener.getUseStats();
	}


	public String getStatsStr() {
		return stats.getStatsStr();
	}
	
	public String getStatsStr(boolean colors) {
		return stats.getStatsStr(colors);
	}



	public void onPluginDisabled(String pluginName) {
		setPermissions(hookManager.setPermissionsOnDisable(pluginName)); 
	}

	public void onPluginEnabled(String pluginName) {
		setPermissions(hookManager.setPermissionsOnEnable(pluginName)); // TODO: dumb.
	}

	public boolean scheduleTasks() {
		if (triple == null ||triple.plugin==null) {
			Utils.warn("scheduleTasks: no plugin present.");
		}
		BukkitScheduler sched = Bukkit.getServer().getScheduler();
		sched.cancelTasks(triple.plugin);
		boolean res = true;
		// Saving task
		if (this.settings.savingPeriod>0) { 
			if (sched.scheduleSyncRepeatingTask(triple.plugin, new Runnable() {
				@Override
				public void run() {
					onScheduledSave();
				}
			}, this.settings.savingPeriod, this.settings.savingPeriod) == -1) {
				Bukkit.getServer().getLogger().severe("[RSP] Failed to schedule saving task.");
				res = false; 
			}
		}
		// checkParked task / TODO: maybe not a scheduled task ?
		if (sched.scheduleSyncRepeatingTask(triple.plugin, new Runnable() {
			@Override
			public void run() {
				checkParked();
			}
		}, this.settings.checkParkedPeriod , this.settings.checkParkedPeriod) == -1) {
			Bukkit.getServer().getLogger().severe("[RSP] Failed to schedule checkParked task.");
			res = false;
		}
		return res;
	}
	
	/**
	 * If scheduled (might include "already scheduled"). 
	 * @return
	 */
	public boolean scheduleUpdateAll() {
	    if (taskIdUpdateAll != -1) return true;
	    taskIdUpdateAll = Bukkit.getScheduler().scheduleSyncDelayedTask(triple.plugin, new Runnable() {
            @Override
            public void run() {
                // Security check.
                if (triple.plugin == null || !Bukkit.getPluginManager().isPluginEnabled(triple.plugin)) return;
                // Update permissions for all players.
                transientMan.updateChildrenPermissions();
                recheckAllPlayers();
                // Finally reset the taskId.
                taskIdUpdateAll = -1;
                Bukkit.getLogger().info("[RSP] Updated permissions and all players.");
            }
        });
	    if (taskIdUpdateAll == -1) {
	        Bukkit.getServer().getLogger().severe("[RSP] Failed to schedule updateAll task.");
	        return false;
	    }
	    else return true;
	}

	/**
	 * Force saving of permission changes.
	 */
	public void forceSaveChanges() {
		if (!this.settings.saveAtAll) return;
		try{
			if (this.settings.useStats) {
				long ns = System.nanoTime();
				permissions.saveChanges();
				stats.addStats(SAVE_CHANGES, System.nanoTime()-ns);
			}
			else permissions.saveChanges();
		} catch (Throwable t) {
			if (logSevere(RSPError.SAVE_PERMISSIONS, t.getMessage())) t.printStackTrace();
		}
	}

	@Override
	public Collection<String> getPlayersInRegion(String worldName, String rid) {
		List<String> out = new LinkedList<String>();
		Integer id = pdMan.getId(worldName, rid);
		if (id == null) return out;
		// TODO: keep them in memory right away !
		for (PlayerData data : playerData.values()) {
			if (data.idCache.contains(id)) out.add(data.playerName);
		}
		return out;
	}
	
	public boolean getCreatePortals() {
		return this.settings.createPortals;
	}

	/**
	 * Save permission changes if appropriate.
	 * TODO: Depending on settings return directly or do other checks to decide.
	 */
	public void mightSaveChanges() {
		if (permissions == null) return; // TODO: CHECK IF POSSIBLE
		boolean available = false;
		try{
			available = permissions.isAvailable();
		} catch (Throwable t) {
			if (logSevere(RSPError.ERROR_PERMISSIONS,t.getMessage())) t.printStackTrace();
		}
		if (available) forceSaveChanges(); 
	}
	
	public Stats getStats() {
		return stats;
	}

	
	public void setUseStats(boolean use) {
		this.settings.useStats = use;
		triple.playerListener.setUseStats(use);
		if (!use) {
			stats.clear();
		}
	}
	
	public void sendGeneralInfo(CommandSender sender) {
		String msg = "[RSP] Info: ";
		int linksize =0;
		int worlds = 0;
		int regions = 0;
		for (String wn : pdMan.regionIdMap.keySet()) {
			World world = Bukkit.getServer().getWorld(wn);
			if (world == null) continue; // only consider valid worlds !
			Map<?,?> m = pdMan.regionIdMap.get(wn);
			if (m==null) continue;
			int s = m.size();
			if (s==0) continue;
			linksize += s;
			worlds++;
			regions += this.regions.getRegionCount(world);
		}
		msg += "playerData="+playerData.size()+" | parked="+parked.size()+" | checkedOut="+checkedOut.size()+" | permdefs="+pdMan.permDefSetups.size()+" | linked="+linksize+"/"+regions+" in "+worlds+" worlds | permissions=" + permissions.getInterfaceName()+" |";
		sender.sendMessage(msg);
		String[] sn = new String[]{"playerData", "parked", "checkedOut"};
		Set<?>[] sets = new Set<?>[]{playerData.keySet(), parked.keySet(), checkedOut};
		for (int i=0; i<sets.length; i++) {
			for (int j=i+1; j<sets.length; j++) {
				@SuppressWarnings("unchecked")
				Set<Object> other = (Set<Object>) sets[j];
				List<String> found = new LinkedList<String>();
				for (Object obj : sets[i]) {
					if (other.contains(obj)) {
						found.add((String) obj);
					}
				}
				if (!found.isEmpty()) {
					sender.sendMessage("[RSP] Inconsistency (players both in "+sn[i]+"->"+sn[j]+"): "+Utils.join(found, ", "));
				}
			}
		}
	}
	
	public void sendPlayerInfo(final CommandSender sender, final Player player) {
		final WorldSettings settings = getSettings(player.getWorld().getName());
		final PlayerData data = getData(player.getUniqueId(), player.getName());
		final String c0 = sender instanceof Player ? ChatColor.GREEN.toString() : "";
		final String c1 = sender instanceof Player ? ChatColor.WHITE.toString() : "";
		final String c2 = sender instanceof Player ? ChatColor.GRAY.toString() : "";
		// TODO: groups are temporary anyway.
//		String groups = "";
//		if (!data.groups.isEmpty()) {
//			LinkedList<String> all = new LinkedList<String>();
//			for (final Entry<String, PrioEntry> entry : data.groups.entrySet()) {
//				final PrioEntry pe = entry.getValue();
//				all.add(entry.getKey() + "(" + pe.prioAdd + "/" + pe.prioRem + ")");
//			}
//			groups = Utils.join(all,  " ");
//		}
		StringBuilder builder = new StringBuilder(128 + 32 * data.idCache.size());
		builder.append(c0 + player.getName() + c1 + ":" + (data.minLazyDist != Integer.MAX_VALUE ? (c1 + " lazydist=" + c2 + data.minLazyDist) : "") + (data.minLazyDist != settings.lazyDist ? "(" + settings.lazyDist + ")" : ""));
		if (!data.idCache.isEmpty()) {
			builder.append(c1 + " links:" + c2);
			for (final Integer id : data.idCache) {
				final PermDefData pd = this.pdMan.getPermDefData(id);
				if (pd == null) {
					builder.append(" (missing id: " + id + ")");
				} else {
					builder.append(" " + pd.rid + "(" + pd.worldName + ")");
				}
			}
		}
		final Map<String, Integer> groups = transientMan.getGroupPriorityMap(player.getName());
		if (groups != null && !groups.isEmpty()) {
			builder.append(c1 + " transient-groups:" + c2);
			for (final Entry<String, Integer> entry : groups.entrySet()) {
				builder.append(" " + entry.getKey() + "@" + entry.getValue());
			}
		}
		sender.sendMessage(builder.toString());
	}

	public void resetStats() {
		stats.clear();
	}	
	
	public WorldSettings getSettings(String world) {
		WorldSettings s = worlds.get(world);
		if (s == null) {
			return settings.defaults;
		}
		else return s;
	}

	public void onRemoveFailure(final String userName, final String worldName, final String grp) {
		logSevere(RSPError.REMOVE_GROUP, grpDetails(userName, worldName, grp));
	}

	public void onAddFailure(final String userName, final String worldName, final String grp) {
		logSevere(RSPError.ADD_GROUP, grpDetails(userName, worldName, grp));		
	}
	


	@Override
	public void addPermissionsFactory(IPermissionsFactory factory) {
		hookManager.addPermissionsFactory(factory);
	}

	
	public boolean hasPluginHook(String pluginName) {
		return hookManager.hasPluginHook(pluginName);
	}

	/**
	 * Re-check for all registered hooks.
	 */
	public void setPermissions() {
		setPermissions(hookManager.setPermissions());
	}

	/**
	 * Set permissions, does not alter state of hookManager.
	 * @param permissions
	 */
	public void setPermissions(IPermissions permissions) {
		this.permissions = permissions;
		pdMan.setPermissions(permissions);
	}

	@Override
	public void linkPermDef(String defName, String worldName, String rid) {
		pdMan.linkPermDef(defName, worldName, rid);
	}
	
	@Override
	public boolean hasPermDef(String defName) {
		return pdMan.hasPermDef(defName);
	}

	@Override
	public boolean removePermDef(String defName) {
		return pdMan.removePermDef(defName);
	}

	@Override
	public boolean addPermDef(ConfigPermDef permDef) {
		return pdMan.addPermDef(permDef);
	}

	@Override
	public boolean unlinkPermDef(String defName, String worldName, String rid) {
		return pdMan.unlinkPermDef(defName, worldName, rid);
	}

    public void onAnyPluginDisabled() {
        scheduleUpdateAll();
    }

    public void onAnyPluginEnabled() {
        scheduleUpdateAll();
    }
	
	public void onGroupChangeFailure(UUID id, String userName, String worldName) {
		// TODO Auto-generated method stub
	}

	@Override
	public void addRegionResultHook(IRegionResultHook regionResultHook) {
		if (!regionResultHooks.contains(regionResultHook)) {
			regionResultHooks.add(regionResultHook);
		}
	}

	@Override
	public void removeRegionResultHook(IRegionResultHook regionResultHook) {
		regionResultHooks.remove(regionResultHook);
	}

	public void onDisable() {
		clearAllPermDefs(); // does checkout and save
		regionResultHooks.clear();
		permissions = new SuperPerms();
		regions = new NoRegions();
	}
	
}
