package me.asofold.bpl.rsp.plshared.players;

import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

/**
 * Provide mappings by name for players. Lower-case usually means trim + lower-case (!).
 * @author mc_dev
 *
 */
public interface PlayerMap {
	
	/**
	 * Get a player by a UUID (exact of course).
	 * @param id
	 * @return
	 */
	public Player getPlayerExact(final UUID id);
	
	/**
	 * Lookup only by the exact name (will match lower case name or exact).
	 * @param name
	 * @return null if not in mapping.
	 */
	public Player getPlayerExact(final String name);
	
	/**
	 * Lookup by lower-case name (exact name, no matching).
	 * @param name
	 * @return null if not in mapping.
	 */
	public Player getPlayer(final String name);
	
	/**
	 * Lookup of the closest matching name (shortest name with that prefix).
	 * @param name
	 * @return null if no match, or if several matches exist.
	 */
	public String getNameByPrefix(final String name);
	
	/**
	 * Lookup by lower-case prefix.
	 * @param name
	 * @return null if no match, or if several matches exist.
	 */
	public Player getPlayerByPrefix(final String name);
	
	/**
	 * Find all players with the prefix (lower case).
	 * @param name
	 * @return Always return a set (unmodifiable, possibly).
	 */
	public Set<String> matchByPrefix(final String name);
}
