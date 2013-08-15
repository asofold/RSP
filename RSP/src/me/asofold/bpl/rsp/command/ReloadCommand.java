package me.asofold.bpl.rsp.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.asofold.bpl.rsp.core.RSPCore;

public class ReloadCommand extends AbstractCommand<RSPCore>{

	public ReloadCommand(RSPCore access) {
		super(access, "reload", "rsp.cmd.reload");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args)
	{
		if (access.reloadSettings() ){
			sender.sendMessage("[RSP] Settings reloaded.");
		} 
		else{
			sender.sendMessage("[RSP] Reload failed !");
		}
		return true;
	}
	
	

}
