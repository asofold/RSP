package me.asofold.bpl.rsp.command;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.asofold.bpl.rsp.core.RSPCore;
import me.asofold.bpl.rsp.plshared.Players;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReCheckCommand extends AbstractCommand<RSPCore> {

	public ReCheckCommand(RSPCore access) {
		super(access, "recheck", "rsp.command.recheck", new String[]{"update", "check"});
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
	{
		// Tab-complete players.
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args)
	{
		boolean reCheckAll = false;
		final Set<String> names = new HashSet<String>();
		for (int i = 1; i < args.length; i++) {
			final String name = args[i].trim();
			if (name.isEmpty()) {
				continue;
			}
			if (name.equals("*")) {
				reCheckAll = true;
				break;
			}
			names.add(name);
		}
		if (reCheckAll) {
			access.recheckAllPlayers();
			sender.sendMessage("[RSP] Re-checked all players.");
		} else {
			if (names.isEmpty()) {
				sender.sendMessage("[RSP] No names to check.");
			}
			for (String name : names) {
				final Player player = Players.getPlayerExact(name);
				if (player == null) {
					sender.sendMessage("[RSP] Not online: " + name);
				} else {
					access.recheck(player, true);
					sender.sendMessage("[RSP] Re-checked: " + name);
				}
			}
		}
		return true;
	}
	
	

}
