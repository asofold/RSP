package me.asofold.bpl.rsp.plshared.players;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

/**
 * Allow lookup of players by exact and lower-case-name and lower-case prefixes.<br>
 * @author mc_dev
 *
 */
public class SimplePlayerMap implements PlayerMap{
	/** Empty, unmodifiable.*/
	protected static final Set<String> emptySet = Collections.unmodifiableSet(new HashSet<String>(0));
	
	/**
	 * Keep track of byName players. (exact name, lower case name).<br>
	 */
	protected final Map<String, Player> byName = new HashMap<String, Player>(100);
	
	/**
	 * Map all lower-case prefixes of players to a set of exact names.<br>
	 * TODO: Add a more efficient data structure (prefix tree).
	 */
	protected final Map<String, Set<String>> byNamePrefix = new HashMap<String, Set<String>>(1000);
	
	/**
	 * Map UUID object to player instances.
	 */
	protected final Map<UUID, Player> byUUID= new HashMap<UUID, Player>(100);
	
	/**
	 * 
	 * @param player
	 */
	public void addPlayer(final Player player){
		final String name = player.getName();
		final String lcName = name.toLowerCase();
		// Add direct mappings.
		byName.put(name, player);
		byName.put(lcName, player);
		byUUID.put(player.getUniqueId(), player); // TODO: Inconsistency checking.
		// Add prefix mappings.
		for (int i = 1; i < name.length() + 1; i++){
			final String prefix = lcName.substring(0, i);
			Set<String> set = byNamePrefix.get(prefix);
			if (set == null){
				set = new LinkedHashSet<String>();
				byNamePrefix.put(prefix, set);
			}
			set.add(name);
		}
	}
	
	/**
	 * 
	 * @param player
	 */
	public void removePlayer(final Player player){
		final String name = player.getName();
		final String lcName = name.toLowerCase();
		// Remove direct mappings.
		byName.remove(name);
		byName.remove(lcName);
		byUUID.remove(player.getUniqueId()); // TODO: Inconsistency checking.
		// Remove prefix mappings.
		for (int i = 1; i < name.length() + 1; i++){
			final String prefix = lcName.substring(0, i);
			final Set<String> set = byNamePrefix.get(prefix);
			if (set != null){
				set.remove(name);
				if (set.isEmpty()){
					byNamePrefix.remove(prefix);
					// TODO: From this point on one could remove all following prefixes directly.
				}
			}
		}
	}
	
	/**
	 * Remove all mappings.
	 */
	public void clear(){
		byName.clear();
		byNamePrefix.clear();
		byUUID.clear();
	}
	
	@Override
	public Player getPlayerExact(final String name){
		return byName.get(name);
	}
	
	@Override
	public Player getPlayer(final String name){
		return byName.get(name.trim().toLowerCase());
	}

	@Override
	public String getNameByPrefix(String name) {
		final String lcName = name.trim().toLowerCase();
		final Player player = byName.get(lcName);
		if (player != null) return player.getName();
		final Set<String> names =  byNamePrefix.get(lcName);
		if (names == null || names.isEmpty()) return null;
		int diff = Integer.MAX_VALUE;
		final int len = name.length();
		String match = null;
		for (final String ref : names){
			final int refLen = ref.length();
			final int cDiff = refLen - len;
			if (match == null || cDiff < diff){
				diff = cDiff;
				match = ref;
				if (refLen == len) break;
			}
			else if (cDiff == diff){
				// Ambigue.
				return null;
			}
		}
		return match;
	}

	@Override
	public Player getPlayerByPrefix(final String name) {
		final String match = getNameByPrefix(name);
		return match == null ? null : byName.get(match);
	}

	@Override
	public Set<String> matchByPrefix(final String name) {
		final String lcName = name.trim().toLowerCase();
		final Set<String> names = byNamePrefix.get(lcName);
		if (names == null) return emptySet;
		else return Collections.unmodifiableSet(names);
	}

	@Override
	public Player getPlayerExact(final UUID id) {
		return byUUID.get(id);
	}
	
	
}
