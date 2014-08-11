package me.asofold.bpl.rsp.api.regions;

/**
 * TODO: maybe add a reason for enter (move, other)
 * @author mc_dev
 *
 */
public interface IRegionEnter {
	public void onRegionEnter(String playerName, String worldName, String rid, String permDefName);
}
