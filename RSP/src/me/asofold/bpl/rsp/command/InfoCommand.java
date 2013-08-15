package me.asofold.bpl.rsp.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.asofold.bpl.rsp.core.RSPCore;

public class InfoCommand extends AbstractCommand<RSPCore> {

	public InfoCommand(RSPCore access) {
		super(access, "info", "rsp.cmd.info");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args)
	{
		access.sendInfo(sender);
		return true;
	}

}
