package me.asofold.bpl.rsp.command.stats;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.asofold.bpl.rsp.command.AbstractCommand;
import me.asofold.bpl.rsp.core.RSPCore;
import me.asofold.bpl.rsp.utils.Utils;

public class StatsCommand extends AbstractCommand<RSPCore>{

	public StatsCommand(RSPCore access) {
		super(access, "stats", "rsp.cmd.stats.see");
		addSubCommands(
			new StatsEnableCommand(access),
			new StatsDisableCommand(access),
			new StatsResetCommand(access)
			);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args)
	{
		if (args.length == 1) {
			if (access.getUseStats()){
				Utils.send(sender, access.getStatsStr(true), false);
			} 
			else{
				sender.sendMessage("[RSP] Stats are disabled.");
			}
			return true;
		} else {
			return super.onCommand(sender, command, alias, args);
		}
	}
	
	

}
