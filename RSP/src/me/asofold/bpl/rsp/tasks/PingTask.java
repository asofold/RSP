package me.asofold.bpl.rsp.tasks;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Ping the player with permission state.<br>
 * It is canceled by either disconnecting (not being online on run), or by sneaking for 4 periods = econds).
 * TODO: maybe add minimal delay if state does not change.
 * @author mc_dev
 *
 */
public class PingTask implements Runnable{
	private final String playerName;
	public int taskId = -1;
	List<String> perms = new LinkedList<String>();
	public long period = 20;
	public PingTask(String playerName, String[] args, int index){
		this.playerName = playerName;
		for (int i = index; i<args.length; i++){
			String p = args[i].trim();
			if (p.isEmpty()) continue;
			perms.add(p);
		}
	}
	int nSneak = 0;
	@Override
	public void run() {
		if (taskId == -1) return;
		try{
			Player player = Bukkit.getServer().getPlayerExact(playerName);
			if (player == null) {
				cancel();
				return;
			}
			if (player.isSneaking()){
				nSneak++;
				if (nSneak >= 4) cancel();
			}
			else nSneak = 0;
			String out = ChatColor.GRAY+"[RSP]"+ChatColor.YELLOW+"[PERMISSIONS]";
			if (perms.isEmpty()) {
				out += ChatColor.RED+" No Permissions specified.";
			}
			else{
				for (String p : perms){
					out += " " + (player.hasPermission(p)?ChatColor.GREEN.toString():ChatColor.RED.toString())+p;
				}
			}
			player.sendMessage(out);
		} catch ( Throwable t){
			cancel();
		}
		
	}
	
	public void cancel() {
		if( taskId == -1) return;
		Bukkit.getServer().getScheduler().cancelTask(taskId);
		taskId = -1;
	}
	
	public int register(Plugin plugin){
		if (taskId!=-1) cancel();
		taskId = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, period, period);
		return taskId;
	}
	
}
