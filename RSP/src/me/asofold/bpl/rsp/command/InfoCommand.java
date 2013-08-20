package me.asofold.bpl.rsp.command;

import me.asofold.bpl.rsp.core.RSPCore;
import me.asofold.bpl.rsp.plshared.Players;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InfoCommand extends AbstractCommand<RSPCore> {

	public InfoCommand(RSPCore access) {
		super(access, "info", "rsp.cmd.info");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args)
	{
		access.sendGeneralInfo(sender);
		if (args.length == 1 && sender instanceof Player) {
			args = new String[]{args[0], sender.getName()};
		}
		for (int i = 1; i < args.length; i++) {
			final String name = args[i].trim();
			if (name.isEmpty()) continue;
			final Player player = Players.getPlayerExact(name.toLowerCase());
			if (player != null && player.isOnline()) {
				access.sendPlayerInfo(sender, player);
			} else {
				sender.sendMessage("(Not online: " + name + ")");
			}
		}
		return true;
	}

}
