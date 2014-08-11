package me.asofold.bpl.rsp.command.stats;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.asofold.bpl.rsp.command.AbstractCommand;
import me.asofold.bpl.rsp.core.RSPCore;

public class StatsResetCommand extends AbstractCommand<RSPCore>{

	public StatsResetCommand(RSPCore access) {
		super(access, "reset", "rsp.cmd.stats.reset");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args)
	{
		access.resetStats();
		sender.sendMessage("[RSP] Stats reset.");
		return true;
	}
	
	

}
