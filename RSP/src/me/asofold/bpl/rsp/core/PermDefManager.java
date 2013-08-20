package me.asofold.bpl.rsp.core;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import me.asofold.bpl.rsp.api.IPermissionUser;
import me.asofold.bpl.rsp.api.IPermissions;
import me.asofold.bpl.rsp.api.impl.superperms.SuperPerms;
import me.asofold.bpl.rsp.config.ConfigPermDef;
import me.asofold.bpl.rsp.config.PermDef;
import me.asofold.bpl.rsp.config.PermDefType;
import me.asofold.bpl.rsp.config.Settings;
import me.asofold.bpl.rsp.config.Settings.Link;
import me.asofold.bpl.rsp.utils.StringPair;

import org.bukkit.Bukkit;


public class PermDefManager {
	
	/**
	 * Permdefs by id.
	 */
	final Map<Integer, PermDefData> idDefMap = new HashMap<Integer, PermDefData>();
	
	/**
	 * Map Worldname -> rid -> id
	 */
	final Map<String, Map<String, Integer>> regionIdMap = new HashMap<String, Map<String,Integer>>();
	
	
	/**
	 * 
	 */
	final Map<String, PermDefSetup> permDefSetups = new HashMap<String, PermDefSetup>();
	
	/**
	 * Id for generic permdefs: online
	 */
	final Integer idGenericOnline;
	
	/**
	 * Id for generic permdefs: ownership
	 */
	final Integer idGenericOwnership;
	
	public PermDefManager(final RSPCore core){
		this.core = core;
		idGenericOnline = getNewId();
		idGenericOwnership = getNewId();
	}
	
	/**
	 * Id for mappings world->rid-> int
	 */
	int maxId = 0;

	private IPermissions permissions = new SuperPerms();
	
	private final RSPCore core;
	
	public Integer getNewId(){
		maxId++;
		return maxId;
	}
	
	/**
	 * Shortcut for getting the id for a region.
	 * @param worldName
	 * @param rid
	 * @return
	 */
	public final Integer getId(final String worldName, final String rid){
		final Map<String, Integer> idMap = regionIdMap.get(worldName);
		if ( idMap == null ) return null;
		return idMap.get(rid);
	}
	
	/**
	 * Shortcut for getting the PermDefData for a region.
	 * @param worldName
	 * @param rid
	 * @return
	 */
	public final PermDefData getPermDefData(final String worldName, final String rid){
		final Integer id = getId(worldName, rid);
		if ( id == null ) return null;
		return idDefMap.get(id);
	}
	
	/**
	 * Get the PermDefData for the given id, as is stored in PlayerData.idCache.
	 * @param id
	 * @return
	 */
	public PermDefData getPermDefData(final Integer id) {
		return idDefMap.get(id);
	}
	
	public boolean applySettings(Settings settings){
		for (ConfigPermDef def : settings.configPermDefs){
			addPermDef(def, PermDefType.CONFIG);
		}
		for (Link link : settings.links){
			for ( String defName : link.defNames){
				if ( !permDefSetups.containsKey(defName)){
					Bukkit.getServer().getLogger().warning("[RSP] Ignore link ("+link.world+", "+link.rid+") to non existent PermDef "+defName);
					continue;
				}
				try{
					linkPermDef(defName, link.world, link.rid);
				} catch (Throwable t){
					Bukkit.getServer().getLogger().warning("[RSP] Ignore link ("+link.world+", "+link.rid+") to PermDef "+defName+" due to an unexpected error.");
					t.printStackTrace();
				}
			}
		}
		return true; // maybe todo
	}
	
	public void linkPermDef(String defName, String worldName, String rid) {
		if ( !this.permDefSetups.containsKey(defName)) throw new RuntimeException("PermDef not present: "+defName);
		PermDefSetup setup = permDefSetups.get(defName);
		PermDef def = setup.permDef;
		Map<String, Integer> idMap = regionIdMap.get(worldName);
		if ( idMap == null){
			idMap = new HashMap<String, Integer>();
			regionIdMap.put(worldName, idMap);
		}
		Integer id = idMap.get(rid);
		PermDefData pdData;
		if ( id == null ){
			id = getNewId();
			idMap.put(rid,  id);
			pdData = new PermDefData(worldName, rid);
			idDefMap.put(id, pdData);
		} else pdData = idDefMap.get(id); // must exist now.
		pdData.addPermDef(def);
	}
	


	public void setPermissions(IPermissions permissions) {
		this.permissions = permissions;
	}
	
	public void unregisterAllPermdefs() {
		List<ConfigPermDef> rem = new LinkedList<ConfigPermDef>();
		for ( PermDefSetup setup : permDefSetups.values()){
			if ( setup.type == PermDefType.CONFIG){
				rem.add(setup.setup);
			}
		}
		for (ConfigPermDef def : rem){
			 removePermDef(def.getDefName());
		}
	}

	public void removeAllPermDef() {
		idDefMap.clear();
		regionIdMap.clear();
		permDefSetups.clear();
		// generic ids stay (!)
	}

	public final boolean hasPermDef(final String defName) {
		return permDefSetups.containsKey(defName);
	}
	
	public boolean addPermDef(ConfigPermDef permDef) {
		return addPermDef(permDef,  PermDefType.RUNTIME) ;
	}
	
	public boolean addPermDef(ConfigPermDef permDef, PermDefType type) {
		boolean res = removePermDef(permDef.getDefName());
		PermDefSetup setup = new PermDefSetup();
		setup.setup = permDef;
		setup.permDef = PermDef.fromConfigPermDef(permDef);
		setup.type = type; 
		permDefSetups.put(permDef.getDefName(), setup);
		return res;
	}
	
	public boolean removePermDef(String defName) {
		// just collect where to remove it from:
		PermDefSetup setup = permDefSetups.get(defName);
		if ( setup == null) return false;
		// no referencing yet, must iterate over all.
		List<StringPair> rem = new LinkedList<StringPair>();
		boolean res = false;
		for ( String wn : regionIdMap.keySet()){
			Map<String, Integer> idMap = regionIdMap.get(wn);
			for ( String rid : idMap.keySet()){
				PermDefData data = idDefMap.get(idMap.get(rid));
				// TODO: more checks ?
				if ( data.contains(setup.permDef)){
					res = true;
					rem.add(new StringPair(wn, rid));
				}
				 
			}
		 }
		// Do the removing;
		for (StringPair pair : rem){
			unlinkPermDef(defName, pair.first, pair.second);
		}
		permDefSetups.remove(defName);
		return res;
	}

	public boolean unlinkPermDef(String defName, String worldName, String rid) {
		// TODO: split / refactor ?
		PermDefSetup setup = permDefSetups.get(defName);
		if ( setup == null ) return false;
		Integer id = getId(worldName, rid);
		if (!unlinkPermDef(setup.permDef, worldName, rid)) return false;
		// remove perms groups from players
//		if ( id == null) return true; // no need by contract.
		boolean removeId = !idDefMap.containsKey(id); // indicate if the id should be removed from players.
		if (!permissions.isAvailable()) return true;
		// (TODO: mind code cloning possibly, for the following !)
//		List<IPermissionUser> changedUsers = new LinkedList<IPermissionUser>();
		boolean changed = false;
		List<String> keys = new LinkedList<String>();
		keys.addAll(core.playerData.keySet());
		keys.addAll(core.parked.keySet());
		for (String playerName : keys){
			PlayerData data = core.getData(playerName);
			if ( !data.isChecked ) continue;
			if ( !data.idCache.contains(id) ) continue;
			if ( removeId ) data.idCache.remove(id);
			// adjust perms:
			// TODO: POLICY ! [currently: remove grpRemExit]
			// TODO: CONFLICTS / REFERENCE COUNTS !
			IPermissionUser user = permissions.getUser(playerName, worldName);
			user.prepare();
			for ( String grp : setup.permDef.grpRemExit){
				if ( user.inGroup(grp) ){
					user.removeGroup(grp);
					changed = true;
//					changedUsers.add(user);
				}
			}
			if (changed){
				if (!user.applyChanges()) core.onGroupChangeFailure(playerName, worldName);
			}
			else user.discardChanges();
		}
//		if ( changed ){
////			for ( IPermissionUser user : changedUsers){
////				user.save();
////			}
//		}
		return changed;
	}
	
	/**
	 * PermDef is just used for comparison of names.
	 * This will not remove permissions of players.
	 * @param permDef
	 * @param worldName
	 * @param rid
	 * @return
	 */
	public boolean unlinkPermDef(PermDef permDef, String worldName, String rid) {
		Map<String, Integer> idMap = regionIdMap.get(worldName);
		if (idMap==null ) return false;
		Integer id = idMap.get(rid);
		if ( id == null) return false;
		PermDefData data = idDefMap.get(id);
		// TODO: maybe adjust reference counting later...
		boolean res = data.removePermDef(permDef);
		if ( data.isEmpty()){
			idDefMap.remove(id);
			idMap.remove(rid);
			if ( idMap.isEmpty()) regionIdMap.remove(worldName);
		}
		return res;
	}

}
