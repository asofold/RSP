package me.asofold.bpl.rsp.command;

import me.asofold.bpl.rsp.command.stats.StatsCommand;
import me.asofold.bpl.rsp.core.RSPCore;


public class RSPCommand extends AbstractCommand<RSPCore> {

	public RSPCommand(RSPCore access) {
		super(access, "rsp", "rsp.filter.cmd.rsp");
		addSubCommands(
			new InfoCommand(access),
			new PingCommand(access),
			new ReCheckCommand(access),
			new ReloadCommand(access),
			new StatsCommand(access)
			);
	}
}
