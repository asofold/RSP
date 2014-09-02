package me.asofold.bpl.rsp.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;

import me.asofold.bpl.rsp.config.compatlayer.CompatConfig;
import me.asofold.bpl.rsp.permissions.PrioEntry;
import me.asofold.bpl.rsp.permissions.PrioMap;
import me.asofold.bpl.rsp.utils.IdUtil;

/**
 * Represents offline player-data.
 * @author dev1mc
 *
 */
public class OfflinePlayerData {
	
	/**
	 * Left non-final, as this may be updated to the correct name.
	 */
	public String playerName = null;
	
	/**
	 * Updated lazily if only the name is read from edited files.
	 */
	public UUID uuid = null;
	
	/**
	 * Preset groups with priorities (for add).
	 */
	public final PrioMap<String> groups = new PrioMap<String>(8, 0.3f);
	
	/**
	 * The player name or the uuid is added here as key (!).
	 * @param cfg
	 * @param prefix
	 */
	public void toConfig(final CompatConfig cfg, String prefix) {
		String key = uuid == null ? playerName : uuid.toString();
		prefix += key + ".";
		if (playerName != null) {
			cfg.set(prefix + "name", playerName);
		}
		List<String> groupStrings = new ArrayList<String>(groups.size());
		for (Entry<String, PrioEntry> entry : groups.entrySet()) {
			final String groupName = entry.getKey();
			final PrioEntry prio = entry.getValue();
			String groupSpec;
			if (prio.isAdd()) {
				groupSpec = groupName + (prio.prioAdd == 0 ? "" : ("@" + prio.prioAdd));
			} else {
				groupSpec = "-" + groupName + (prio.prioRem == 0 ? "" : ("@" + prio.prioRem));
			}
			groupStrings.add(groupSpec);
		}
		cfg.set(prefix + "groups", groupStrings);
	}
	
	public static OfflinePlayerData fromConfig(final CompatConfig cfg, String prefix, final String key) {
		final OfflinePlayerData opd = new OfflinePlayerData();
		prefix += key + ".";
		try {
			opd.uuid = IdUtil.UUIDFromString(key.trim());
		} catch (IllegalArgumentException e) {
			opd.playerName = key.trim();
		}
		final String playerName = cfg.getString(prefix + "name", null);
		if (playerName != null) {
			opd.playerName = playerName.trim();
		}
		if (opd.uuid == null && (opd.playerName == null || opd.playerName.isEmpty())) {
			return null;
		}
		// Groups.
		final List<String> groupSpecs = cfg.getStringList(prefix + "groups");
		if (groupSpecs != null) {
			for (String groupSpec : groupSpecs) {
				groupSpec = groupSpec.trim();
				boolean add = true; // Default: add group.
				int priority = 0; // Default: priority 0.
				if (groupSpec.startsWith("-")) {
					add = false;
					groupSpec = groupSpec.substring(1);
				}
				int index = groupSpec.indexOf('@');
				if (index == 0) {
					Bukkit.getLogger().warning("[RSP] Ignore empty group name in players.yml at '" + prefix + "'!");
					continue;
				}
				else if (index > 0) {
					String prioSpec = groupSpec.substring(index + 1).trim();
					try {
						priority = Integer.parseInt(prioSpec);
						groupSpec = groupSpec.substring(0, index).trim();
						if (groupSpec.isEmpty()) {
							Bukkit.getLogger().warning("[RSP] Ignore empty group name in players.yml at '" + prefix + "'!");
							continue;
						}
					} catch (NumberFormatException e) {
						Bukkit.getLogger().warning("[RSP] Can not interpret '" + groupSpec + "' as group with '" + prefix + "', interpret as group name.");
					}
				}
				if (add) {
					opd.groups.updateAdd(groupSpec, priority);
				} else {
					opd.groups.updateRem(groupSpec, priority);
				}
			}
		}
		return opd;
	}
}
