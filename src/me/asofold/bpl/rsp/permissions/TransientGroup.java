package me.asofold.bpl.rsp.permissions;

import java.util.LinkedHashMap;
import java.util.Map;

import me.asofold.bpl.rsp.config.compatlayer.CompatConfig;


/**
 * Permission group.
 * @author mc_dev
 *
 */
public class TransientGroup {
	public final String name;
	public final String lcName;
	public final Map<String, Boolean> permissions = new LinkedHashMap<String, Boolean>();
	
	
	public TransientGroup(String groupName){
		name = groupName;
		lcName = name.toLowerCase();
	}
	
	public static TransientGroup fromConfig(CompatConfig cfg, String path, String groupName){
		groupName = groupName.trim();
		if (groupName.isEmpty()) return null;
		TransientGroup group = new TransientGroup(groupName);
		PermissionUtil.readPermissions(cfg, path, group.permissions);
		return group;
	}
	
}
