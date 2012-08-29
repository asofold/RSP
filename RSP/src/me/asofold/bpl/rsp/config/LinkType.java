package me.asofold.bpl.rsp.config;

/**
 * Type of a link for a permdef.
 * @author mc_dev
 *
 */
public enum LinkType {
	/**
	 * Linking to a region for a certain world.
	 */
	REGION,
	/**
	 * Concerning the online state. <br>
	 * Might only be considered at the first login and at checkout-parked.
	 */
	ONLINE,
	/**
	 * Valid while the player is in a region he is owner of.
	 */
	OWNERHIP,
}
