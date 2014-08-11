package me.asofold.bpl.rsp.command.stats;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.asofold.bpl.rsp.command.AbstractCommand;
import me.asofold.bpl.rsp.core.RSPCore;

public class StatsEnableCommand extends AbstractCommand<RSPCore> {

	public StatsEnableCommand(RSPCore access) {
		super(access, "enable", "rsp.cmd.stats.enable", new String[]{"on", "1"});
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args)
	{
		access.setUseStats(true);
		sender.sendMessage("[RSP] Stats are enabled.");
		return true;
	}
	
	

}
