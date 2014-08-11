package me.asofold.bpl.rsp.api;

import me.asofold.bpl.rsp.RSP;

import org.bukkit.event.HandlerList;
import org.bukkit.event.server.PluginEvent;


/**
 * This event is thrown after the RSP configuration has been reloaded.
 * @author mc_dev
 *
 */
public class RSPReloadEvent extends PluginEvent {

	private static final HandlerList handlers = new HandlerList();
	
	private final boolean error;

	private final Throwable failureReason;
	
	/**
	 * Confine to use with RSP.
	 * @param rsp
	 */
	public RSPReloadEvent(RSP rsp, boolean error, Throwable failureReason) {
		super(rsp);
		this.error = error;
		this.failureReason = failureReason;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	/**
	 * Must have :_) ...
	 * @return
	 */
	public static HandlerList getHandlerList() {
		return handlers;
	}

	/**
	 * If the configuration was reloaded with errors.<br>
	 * Usually errors would mean a total failure, check getFailureReason() to check what failed.
	 * @return
	 */
	public boolean isError() {
		return error;
	}
	
	/**
	 * Returns the Throwable that made reload fail. 
	 * If it is null and isError() is set, that means that the configuration syntax was somehow wrong.
	 * @return
	 */
	public Throwable getFailureReason() {
		return failureReason;
	}
	
	/**
	 * Convenience method.
	 * @return
	 */
	public IRSPCore getCore(){
		return RSP.getRSPCore();
	}

	

}
