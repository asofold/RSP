package me.asofold.bpl.rsp.command.stats;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.asofold.bpl.rsp.command.AbstractCommand;
import me.asofold.bpl.rsp.core.RSPCore;

public class StatsDisableCommand extends AbstractCommand<RSPCore> {

	public StatsDisableCommand(RSPCore access) {
		super(access, "disable", "rsp.cmd.stats.disable", new String[]{"off", "0"});
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args)
	{
		access.setUseStats(false);
		sender.sendMessage("[RSP] Stats are disabled.");
		return true;
	}
	
}
