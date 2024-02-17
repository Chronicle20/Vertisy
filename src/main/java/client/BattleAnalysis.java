package client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Sep 1, 2017
 */
public class BattleAnalysis{

	public boolean running;
	public long end, start;
	public double acquiredExp, acquiredMeso;
	public Map<Integer, List<Integer>> totalDamage = new HashMap<>();
	// min max skill damage?
	// total attacks? per skill?
	// average damage?
	// times used?
	//

	public void addDamage(int skillid, int damage){
		if(!running) return;
		List<Integer> dmg = totalDamage.get(skillid);
		if(dmg == null) dmg = new ArrayList<>();
		dmg.add(damage);
		totalDamage.put(skillid, dmg);
	}

	public void addExp(int exp){
		if(!running) return;
		acquiredExp += exp;
	}

	public void addMeso(int meso){
		if(!running) return;
		acquiredMeso += meso;
	}
}
