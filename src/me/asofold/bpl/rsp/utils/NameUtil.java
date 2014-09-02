package me.asofold.bpl.rsp.utils;

import java.util.Collection;
import java.util.LinkedHashSet;

public class NameUtil {
	
	/**
	 * Valid user name check (Minecraft). Stolen from TrustCore.
	 * @param name Allows null input.
	 * @return
	 */
	public static boolean isValidMinecraftUserName(final String name) {
		return name != null && name.matches("[\\w]{2,16}");
	}
	
	/**
	 * Get a new collection only containing valid user names (Minecraft). 
	 * @param names Allows null input.
	 * @return A Collection fit for iterating (Iterator), holding unique elements. Likely a LinkedHashSet instance. Always returns a Collection.
	 */
	public static Collection<String> filterValidMinecraftUserNames(final Collection<String> names) {
		final Collection<String> validNames = new LinkedHashSet<String>();
		if (names == null) {
			return validNames;
		}
		for (final String name : names) {
			if (isValidMinecraftUserName(name)) {
				validNames.add(name);
			}
		}
		return validNames;
	}
	
}
