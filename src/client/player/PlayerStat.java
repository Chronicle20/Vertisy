package client.player;

import java.util.Map.Entry;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleCharacter.SkillEntry;
import client.MapleJob;
import client.Skill;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.EquipSlot;
import constants.skills.*;
import server.ItemInformationProvider;
import server.MapleStatEffect;
import server.item.Potential;
import server.item.PotentialLevelData;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Nov 18, 2017
 */
public class PlayerStat{

	// The final value you do calculations off of.
	public double critDamage;
	public double incCr;// , incCritDamage
	public double accuracy, accuracyR;
	public double watk, watkR;

	public void reset(){
		critDamage = 0D;
		accuracy = 0;
		accuracyR = 0;
		watk = 0;
		watkR = 0;
		// incCritDamage = 0;
		// critRate = 0;
		// incCr = 0;
	}

	public void setFrom(MapleCharacter chr){
		if(chr.getJob().getId() >= 500 && chr.getJob().getId() <= 522){
			if(chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) EquipSlot.WEAPON.getSlots()[0]) != null) accuracy += 10;
		}else if(chr.getJob().getId() >= 1500 && chr.getJob().getId() <= 1512){
			if(chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) EquipSlot.WEAPON.getSlots()[0]) != null) accuracy += 5;
		}
		for(Item item : chr.getInventory(MapleInventoryType.EQUIPPED)){
			Equip equip = (Equip) item;
			applyEquipStats(equip);
			applyItemOption(equip);
		}
		for(Entry<Skill, SkillEntry> skillData : chr.getSkills().entrySet()){
			if(skillData.getValue().skillevel > 0){
				if(skillData.getKey().getId() % 10000 < 1000){
					applyPassive(skillData.getKey(), skillData.getValue().skillevel);
				}
			}
		}
		applyBuffStats(chr);
		//
		if(chr.getJob().isWarrior() || chr.getJob().isBeginner()) accuracy += ((double) chr.getTotalDex() * 0.8D);
		else if(chr.getJob().isThief() || chr.getJob().isArcher()) accuracy += ((double) chr.getTotalDex() * 0.6D);
		else if(chr.getJob().isPirate()) accuracy += ((double) chr.getTotalDex() * 0.5D);
		//
		if(chr.getJob().isWarrior() || chr.getJob().isThief() || chr.getJob().isArcher() || chr.getJob().isBeginner()) accuracy += ((double) chr.getTotalLuk() * 0.5D);
		else if(chr.getJob().isMagician()) accuracy += ((double) chr.getTotalLuk() * 0.3D);
		else if(chr.getJob().isPirate()) accuracy += ((double) chr.getTotalLuk() * 0.2D);// I guess?
		//
		if(accuracyR > 0) accuracy += accuracy * (accuracyR / 100);
		if(chr.getJob().isA(MapleJob.MAGICIAN) || chr.getJob().isA(MapleJob.BLAZEWIZARD1) || chr.getJob().isA(MapleJob.EVAN1)){
			accuracy = -1;
		}
		// Apply caps
		accuracy = Math.min(999, accuracy);
	}

	private void applyBuffStats(MapleCharacter chr){
		Integer acc = chr.getBuffedValue(MapleBuffStat.ACC);
		if(acc != null){
			accuracy += acc;
		}
		MapleStatEffect buff = chr.getStatForBuff(MapleBuffStat.SHARP_EYES);
		if(buff != null) critDamage += buff.getY() / 100D;
	}

	private void applyPassive(Skill skill, int level){
		MapleStatEffect effect = skill.getEffect(level);
		accuracy += effect.getAcc();
		switch (skill.getId()){
			// Warrior
			// Magician
			// Archer
			case Archer.THE_BLESSING_OF_AMAZON:
				accuracy += effect.getX();
				break;
			case Archer.CRITICAL_SHOT:
				critDamage += effect.getDamage() / 100D;
				break;
			case Hunter.BOW_MASTERY:
				accuracy += effect.getX();
				break;
			case Bowmaster.BOW_EXPERT:
				watk += effect.getX();
				break;
			// Thief
			// Pirate
			case Pirate.BULLET_TIME:
				accuracy += effect.getX();// assuming x, they are same for the levels I checked
				break;
			case Brawler.KNUCKLER_MASTERY:
				accuracy += effect.getX();
				break;
			// Cygnus
			case ThunderBreaker.CRITICAL_PUNCH:
				critDamage += effect.getDamage() / 100D;
				break;
			case ThunderBreaker.QUICK_MOTION:
				accuracy += effect.getX();
				break;
			case ThunderBreaker.KNUCKLER_MASTERY:
				accuracy += effect.getX();
				break;
			// Evan
			case Evan.CRITICAL_MAGIC:
				critDamage += effect.getDamage() / 100D;
				break;
		}
	}

	private void applyEquipStats(Equip equip){
		accuracy += equip.getAcc();
	}

	/**
	 * Apply potential stats from the provided Equip
	 * Does both basic and % stats.
	 */
	private void applyItemOption(Equip equip){
		if(equip.getDurability() == 0 && equip.getDurability() != -1) return;
		ItemInformationProvider ii = ItemInformationProvider.getInstance();
		for(short option : equip.getOptionArray()){
			if(option != 0){
				Potential pot = ii.potentials.get(option);
				if(pot == null) continue;
				PotentialLevelData data = pot.getLevelData(equip);
				if(data == null) continue;
				accuracy += data.incACC;
				accuracyR += data.incACCr;
				// incCr += data.incCr;
			}
		}
	}
}
