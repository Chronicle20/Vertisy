/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client.autoban;

import java.util.HashMap;
import java.util.Map;

import client.MapleCharacter;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @author kevintjuh93
 */
public class AutobanManager{

	private MapleCharacter chr;
	private Map<AutobanFactory, Integer> points = new HashMap<>();
	private Map<AutobanFactory, Long> lastTime = new HashMap<>();
	private int misses = 0, noDamage;
	private int lastmisses = 0;
	private int samemisscount = 0;
	private int staticDamage, staticAmount;
	private long spam[] = new long[31];
	private int lastSkill = -1;
	private long timestamp[] = new long[20];
	private byte timestampcounter[] = new byte[20];
	private int attacksInOneSpot = 0;
	private long[] updateTime = new long[UpdateType.values().length];
	private int[] illegalUpdateType = new int[UpdateType.values().length];

	public AutobanManager(MapleCharacter nchr){
		this.chr = nchr;
	}

	public void addPoint(AutobanFactory fac, String reason){
		if(chr.isGM() || chr.isBanned()) return;
		if(lastTime.containsKey(fac)){
			if(lastTime.get(fac) < (System.currentTimeMillis() - fac.getExpire())){
				points.put(fac, points.get(fac) / 2); // So the points are not completely gone.
			}
		}
		if(fac.getExpire() != -1) lastTime.put(fac, System.currentTimeMillis());
		if(points.containsKey(fac)){
			points.put(fac, points.get(fac) + 1);
		}else points.put(fac, 1);
		if(points.get(fac) >= fac.getMaximum()){
			chr.autoban(reason, fac);
			// chr.autoban("Autobanned for " + fac.name() + " ;" + reason, 1);
			// chr.sendPolice("You have been blocked by #bMooplePolice for the HACK reason#k.");
		}
		// Lets log every single point too.
		Logger.log(LogType.INFO, LogFile.ANTICHEAT, MapleCharacter.makeMapleReadable(chr.getName()) + " caused " + fac.name() + " " + reason);
	}

	public void addTickWithNoDamage(){
		if(++noDamage >= 5){
			AutobanFactory.GOD_MODE.alert(chr, "Potential Blink GodMode.");
			noDamage = 0;
		}
	}

	public void resetTickWithNoDamage(){
		noDamage = 0;
	}

	public void addMiss(){
		this.misses++;
	}

	public void addStaticDamage(int damage){
		if(damage == staticDamage){
			if(++this.staticAmount > 4){
				AutobanFactory.GOD_MODE.alert(chr, "Potential static damage - Damage: " + damage);
				this.staticAmount = 0;
			}
		}else{
			this.staticDamage = damage;
			this.staticAmount = 0;
		}
	}

	public void resetMisses(){
		if(lastmisses == misses && misses > 6){
			samemisscount++;
		}
		if(samemisscount > 4){
			AutobanFactory.GOD_MODE.alert(chr, "Potential Miss GodMode.");
			chr.sendPolice("You will be disconnected for miss godmode.");
		}
		// chr.autoban("Autobanned for : " + misses + " Miss godmode", 1);
		else if(samemisscount > 0) this.lastmisses = misses;
		this.misses = 0;
	}

	/**
	 * <code>type</code>:<br>
	 * 0: HP Regen<br>
	 * 1: MP Regen<br>
	 * 15: Side Mob Vac<br>
	 * 16: Mob Vac<br>
	 * 17: Lemmings<br>
	 * 18: Mob Vac<br>
	 * 30: Summon Attack<br>
	 */
	public void spam(int type){
		this.spam[type] = System.currentTimeMillis();
	}

	public void spam(int type, int timestamp){
		this.spam[type] = timestamp;
	}

	public long getLastSpam(int type){
		return spam[type];
	}

	/**
	 * Timestamp checker
	 * <code>type</code>:<br>
	 * 0: HealOverTime<br>
	 * 1: Pet Food<br>
	 * 2: ItemSort<br>
	 * 3: ItemIdSort<br>
	 * 4: SpecialMove<br>
	 * 5: UseCatchItem<br>
	 * 6: Item Drop<br>
	 * 7: Chat<br>
	 * 8: Attack<br>
	 * 
	 * @param type type
	 * @return Timestamp checker
	 */
	public void setTimestamp(int type, long time, int times){
		if(time <= 0){
			// Logger.log(LogType.INFO, LogFile.GENERAL_INFO, "AutoBanmanager(setTimestamp) type " + type + " has a time of " + time + " which is bad.");
			return;
		}
		if(this.timestamp[type] >= time){ // Newer time should always be higher.
			this.timestampcounter[type]++;
			if(this.timestampcounter[type] >= times){
				chr.getClient().disconnect(false, false);
				// System.out.println("Same timestamp for type: " + type + "; Character: " + chr);
			}
			return;
		}
		this.timestamp[type] = time;
	}

	public long getTimestamp(int type){
		return this.timestamp[type];
	}

	public void setLastUpdate(UpdateType type, long time){
		updateTime[type.ordinal()] = time;
	}

	public long getLastUpdate(UpdateType type){
		return updateTime[type.ordinal()];
	}

	public void increaseIllegalUpdateType(UpdateType type){
		int val = illegalUpdateType[type.ordinal()];
		if(++val > type.amount){
			AutobanFactory.PACKET_EDIT.alert(chr, "Illegal update type " + type.name());
			// System.out.println("Illegal " + type.name());
			val = 0;
		}
		illegalUpdateType[type.ordinal()] = val;
	}

	public void setLastSkill(int skill){
		this.lastSkill = skill;
	}

	public int getLastSkill(){
		return lastSkill;
	}

	public int getAttacksInOneSpot(){
		return attacksInOneSpot;
	}

	public void setAttacksInOneSpot(int attacksInOneSpot){
		this.attacksInOneSpot = attacksInOneSpot;
	}

	public void incrementAttacksInOneSpot(){
		this.attacksInOneSpot++;
	}

	public enum UpdateType{
		CHARACTER_HP_INC(9),
		CHARACTER_MP_INC(9),
		HP_INC_SIZE(9),
		MP_INC_SIZE(9);

		public int amount;

		UpdateType(int amount){
			this.amount = amount;
		}
	}
}
