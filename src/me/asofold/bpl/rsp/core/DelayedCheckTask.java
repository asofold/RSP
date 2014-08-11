package me.asofold.bpl.rsp.core;

import org.bukkit.Bukkit;

public class DelayedCheckTask implements Runnable {
	private final String playerName;
	public boolean isRegistered = false;
	private RSPCore core = null;
	private int taskId = -1;
	public DelayedCheckTask(String playerName) {
		this.playerName = playerName;
	}
	
	@Override
	public void run() {
		isRegistered = false;
		taskId = -1;
		core.checkDelayed(playerName);
	}
	
	/**
	 * Forces register.
	 * @param core
	 */
	public void register(final RSPCore core) {
		this.core = core;
		taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(core.getTriple().plugin, this);
		if (taskId != -1) {
			isRegistered = true;
		}
	}
	
	public void cancel() {
		if (isRegistered) {
			Bukkit.getScheduler().cancelTask(taskId);
		}
		taskId = -1;
		isRegistered = false;
		core = null;
	}

	public void registerIfIdle(RSPCore core) {
		if (!isRegistered) {
			register(core);
		}
	}

}
