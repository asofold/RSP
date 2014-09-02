package me.asofold.bpl.rsp.core;

import java.util.UUID;

import me.asofold.bpl.rsp.config.compatlayer.CompatConfig;
import me.asofold.bpl.rsp.permissions.PrioMap;

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
	
	public static OfflinePlayerData fromConfig(CompatConfig cfg, String prefix, String key) {
		OfflinePlayerData opd = new OfflinePlayerData();
		// TODO: Check if key is uuid.
		// TODO: Check for name setting (use that to override, if in doubt).
		// TODO: Parse group names with priorities (!).
	}
}
