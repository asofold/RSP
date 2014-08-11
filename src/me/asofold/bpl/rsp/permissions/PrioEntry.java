package me.asofold.bpl.rsp.permissions;

/**
 * A Group entry for a group in use.
 * @author mc_dev
 *
 */
public class PrioEntry {
	
	/** Priority with which the entry is referenced for adding.*/
	public int prioAdd;
	
	/** Priority with which the entry is referenced for removing.*/
	public int prioRem ;
	
	/**
	 * Constructor, for not set state.
	 */
	public PrioEntry(){
		reset();
	}
	
	public PrioEntry(final int prioAdd, final int prioRem) {
		set(prioAdd, prioRem);
	}

	/**
	 * Set both entries explicitly.
	 * @param prioAdd
	 * @param prioRem
	 */
	public void set(final int prioAdd, final int prioRem){
		this.prioAdd = prioAdd;
		this.prioRem = prioRem;
	}

	/**
	 * Reset to not set.
	 */
	public final void reset() {
		prioAdd = prioRem = Integer.MIN_VALUE;
	}
	
	/**
	 * If the entry is set at all.
	 * @return
	 */
	public final boolean isEmpty(){
		return prioAdd == prioRem && prioAdd== Integer.MIN_VALUE;
	}
	
	/**
	 * Check if priorities favor adding.
	 * @return
	 */
	public final boolean isAdd(){
		if (prioAdd > prioRem) return true;
		else if (prioAdd == prioRem && prioAdd != Integer.MIN_VALUE) return true;
		else return false;
	}
}
