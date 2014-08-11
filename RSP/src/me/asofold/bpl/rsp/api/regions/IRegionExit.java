package me.asofold.bpl.rsp.api.regions;

/**
 * MAybe add a reason for exit, move etc.
 * @author mc_dev
 *
 */
public interface IRegionExit {
	public void onRegionExit(String playerName, String worldName, String rid, String permDefName);
}
