package me.asofold.bpl.rsp.permissions;

import java.util.LinkedHashMap;

/**
 * 
 * @author mc_dev
 *
 * @param <K> Type of the key.
 */
public class PrioMap<K> extends LinkedHashMap<K, PrioEntry>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8176435323358139844L;

	public PrioMap(int initialCapacity, float loadFactor){
		// TODO: order subject to change.
		super(initialCapacity, loadFactor, true);
	}

	/**
	 * Update or put.
	 * @param key
	 * @param value
	 */
	public void update(final K key, final int prioAdd, final int prioRem){
		final PrioEntry oldValue = get(key);
		if (oldValue == null) super.put(key,  new PrioEntry(prioAdd, prioRem));
		else{
			oldValue.prioAdd = Math.max(oldValue.prioAdd, prioAdd);
			oldValue.prioRem = Math.max(oldValue.prioRem, prioRem);
		}
	}
	
	public void updateAdd(final K key, final int prioAdd){
		update(key, prioAdd, Integer.MIN_VALUE);
	}
	
	public void updateRem(final K key, final int prioRem){
		update(key, Integer.MIN_VALUE, prioRem);
	}

}
