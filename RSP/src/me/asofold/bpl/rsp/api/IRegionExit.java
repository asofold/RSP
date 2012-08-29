package me.asofold.bpl.rsp.api;

/**
 * MAybe add a reason for exit, move etc.
 * @author mc_dev
 *
 */
public interface IRegionExit {
	public void onRegionExit(String playerName, String worldName, String rid, String permDefName);
}
