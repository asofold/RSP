package me.asofold.bpl.rsp.command;

import me.asofold.bpl.rsp.core.RSPCore;
import me.asofold.bpl.rsp.tasks.PingTask;
import me.asofold.bpl.rsp.utils.Utils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class RSPCommand implements CommandExecutor {
	private RSPCore core;
	public RSPCommand(RSPCore core){
		this.core = core;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label,
			String[] args) {
		if (!label.equalsIgnoreCase("rsp")) return false;
		int length = args.length;
		if ( length == 1){
			if ( args[0].equalsIgnoreCase("reload")){
				if ( !core.hasPermission(sender, "rsp.cmd.reload")){
					sender.sendMessage("[RSP] You don't have permission for reload.");
					return false;
				}
				if ( core.reloadSettings() ){
					sender.sendMessage("[RSP] Settings reloaded.");
				} 
				else{
					sender.sendMessage("[RSP] Reload failed !");
				}
				return true;
			} 
			else if (args[0].equalsIgnoreCase("stats")){
				if ( !core.hasPermission(sender, "rsp.cmd.stats.see")){
					sender.sendMessage("[RSP] You don't have permission for stats.");
					return false;
				}
				if (core.getUseStats()){
					Utils.send(sender, core.getStatsStr(true), false);
				} 
				else{
					sender.sendMessage("[RSP] Stats are disabled.");
				}
				return true;
			} 
			else if (args[0].equalsIgnoreCase("info")){
				if ( !core.hasPermission(sender, "rsp.cmd.info")){
					sender.sendMessage("[RSP] You don't have permission for info.");
					return false;
				}
				core.sendInfo(sender);
				return true;
			}
		} else if ( length==2 && args[0].equals("stats") && args[1].equals("reset") ){
			if ( !core.hasPermission(sender, "rsp.cmd.stats.reset")){
				sender.sendMessage("[RSP] You don't have permission to reset the stats.");
				return false;
			}
			core.resetStats();
			sender.sendMessage("[RSP] Stats reset.");
			return true;
		} 
		else if ( length==2 && args[0].equals("stats") && (args[1].equals("on") || args[1].equals("1") )){
			if ( !core.hasPermission(sender, "rsp.cmd.stats.enable")){
				sender.sendMessage("[RSP] You don't have permission to enable stats.");
				return false;
			}
			core.setUseStats(true);
			sender.sendMessage("[RSP] Stats are enabled.");
			return true;
		} 
		else if ( length==2 && args[0].equals("stats") && (args[1].equals("off") || args[1].equals("0") )){
			if ( !core.hasPermission(sender, "rsp.cmd.stats.disable")){
				sender.sendMessage("[RSP] You don't have permission to disable stats.");
				return false;
			}
			core.setUseStats(false);
			sender.sendMessage("[RSP] Stats are disabled.");
			return true;
		} 
		else if (length > 1 && args[0].equals("ping")){
			if ( !core.hasPermission(sender, "rsp.cmd.ping")){
				sender.sendMessage("[RSP] You don't have permission for ping.");
				return false;
			}
			if (!(sender instanceof Player)){
				sender.sendMessage("[RSP] Need a player for testing permissions.");
				return true;
			}
			new PingTask(((Player)sender).getName(), args, 1).register(core.getTriple().plugin);
			return true;
		}
		return false;
	}

}
