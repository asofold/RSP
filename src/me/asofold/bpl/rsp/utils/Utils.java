package me.asofold.bpl.rsp.utils;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Utils {
	/**
	 * 
	 * @param parts
	 * @param link can be null
	 * @return
	 */
	public static final String join(Collection<String> parts, String link){
		StringBuilder builder = new StringBuilder();
		int i = 0;
		int max = parts.size();
		for ( String part : parts){
			builder.append(part);
			i++;
			if ( i<max && link!=null ) builder.append(link);
		}
		return builder.toString();
	}

	public static final void warn(String msg) {
		Bukkit.getServer().getLogger().warning("[RSP] "+msg);
	}
	
	public static void send( CommandSender sender, String message){
		send(sender, message, true);
	}
	public static void send( CommandSender sender, String message, boolean prefix){
		if (prefix){
			if ( message.startsWith("[")) message = "[RSP]" + message;
			else message = "[RSP] "+message;
		}
		if ( sender instanceof Player) sender.sendMessage(message);
		else sender.sendMessage(ChatColor.stripColor(message));
	}
	
	/**
	 * Block coordinate for double, especially important for negative numbers.
	 * (Adapted From Bukkit/NumberConversions.)
	 * @param x
	 * @return
	 */
	public static final int floor(final double x) {
        final int floor = (int) x;
        return (floor == x)? floor : floor - (int) (Double.doubleToRawLongBits(x) >>> 63);
    }
}
