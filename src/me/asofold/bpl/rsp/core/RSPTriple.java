package me.asofold.bpl.rsp.core;

import me.asofold.bpl.rsp.RSP;
import me.asofold.bpl.rsp.listeners.RSPPlayerListener;

public class RSPTriple {
	public final RSP plugin;
	public final RSPPlayerListener playerListener;
	public RSPTriple(RSP plugin, RSPPlayerListener playerListener){
		this.plugin = plugin;
		this.playerListener = playerListener;
	}
}
