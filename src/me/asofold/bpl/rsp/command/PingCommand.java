package me.asofold.bpl.rsp.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.asofold.bpl.rsp.core.RSPCore;
import me.asofold.bpl.rsp.tasks.PingTask;

public class PingCommand extends AbstractCommand<RSPCore> {

	public PingCommand(RSPCore access) {
		super(access, "ping", "rsp.cmd.ping");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args)
	{
		if (!(sender instanceof Player)){
			sender.sendMessage("[RSP] Need a player for testing permissions.");
			return true;
		}
		new PingTask(((Player)sender).getName(), args, 1).register(access.getTriple().plugin);
		return true;
	}

}
