package constants;

import java.util.Calendar;

import client.MapleCharacter;
import client.MapleJob;
import client.Skill;
import constants.skills.*;

/*
 * @author kevintjuh93
 */
public class GameConstants{

	// Maple Trade System's Constants (ITC)
	public static int nRegisterFeeMeso = 5000, // mesos to sell
	        nCommissionRate = 10, // % added to everything
	        nCommissionBase = 100, // amount to add to the current sell amount
	        nAuctionDurationMin = 1, // minimum hours to auction an item
	        nAuctionDurationMax = 168; // maximum hours to auction an item
	public static int MAIN_NX_TYPE = 4;
	public static String MAIN_NX_NAME = "NX";
	public static final int MAX_DAMAGE = 199999;
	// 5A 00 8E DC BE 39 00 09 3D 00

	public static boolean isWeekend(){
		Calendar cal = Calendar.getInstance();
		if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) return true;
		return false;
	}

	public static int getHiddenSkill(final int skill){
		switch (skill){
			case Aran.HIDDEN_FULL_DOUBLE:
			case Aran.HIDDEN_FULL_TRIPLE:
				return Aran.FULL_SWING;
			case Aran.HIDDEN_OVER_DOUBLE:
			case Aran.HIDDEN_OVER_TRIPLE:
				return Aran.OVER_SWING;
		}
		return skill;
	}

	public static boolean isAranSkills(final int skill){
		return Aran.FULL_SWING == skill || Aran.OVER_SWING == skill || Aran.COMBO_TEMPEST == skill || Aran.COMBO_PENRIL == skill || Aran.COMBO_DRAIN == skill || Aran.HIDDEN_FULL_DOUBLE == skill || Aran.HIDDEN_FULL_TRIPLE == skill || Aran.HIDDEN_OVER_DOUBLE == skill || Aran.HIDDEN_OVER_TRIPLE == skill || Aran.COMBO_SMASH == skill || Aran.DOUBLE_SWING == skill || Aran.TRIPLE_SWING == skill;
	}

	public static boolean isHiddenSkills(final int skill){
		return BladeSpecialist.TORNADO_SPIN_TWIRL == skill || Aran.HIDDEN_FULL_DOUBLE == skill || Aran.HIDDEN_FULL_TRIPLE == skill || Aran.HIDDEN_OVER_DOUBLE == skill || Aran.HIDDEN_OVER_TRIPLE == skill;
	}

	public static boolean isAran(final int job){
		return job == 2000 || (job >= 2100 && job <= 2112);
	}

	public static boolean isInJobTree(int skillId, int jobId){
		int skill = skillId / 10000;
		return is_correct_job_for_skill_root(jobId, skill);
	}

	/*v95
	int __cdecl is_correct_job_for_skill_root(int nJob, int nSkillRoot)
	{
	int result; // eax@4
	
	if ( nSkillRoot % 100 )
	result = nSkillRoot / 10 == nJob / 10 && nJob % 10 >= nSkillRoot % 10;
	else
	result = nSkillRoot / 100 == nJob / 100;
	return result;
	}
	
	 */
	public static boolean is_correct_job_for_skill_root(int nJob, int nSkillRoot){
		if((nSkillRoot % 100) > 0) return nSkillRoot / 10 == nJob / 10 && nJob % 10 >= nSkillRoot % 10;
		else return nSkillRoot / 100 == nJob / 100;
	}

	public static int get_novice_skill_point(MapleCharacter chr){
		int nValue = 0;
		nValue += chr.getSkillLevel(1000);
		nValue += chr.getSkillLevel(1001);
		nValue += chr.getSkillLevel(1002);
		nValue += chr.getSkillLevel(10001000);
		nValue += chr.getSkillLevel(10001001);
		nValue += chr.getSkillLevel(10001002);
		nValue += chr.getSkillLevel(20001000);
		nValue += chr.getSkillLevel(20001001);
		nValue += chr.getSkillLevel(20001002);
		nValue += chr.getSkillLevel(20011000);
		nValue += chr.getSkillLevel(20011001);
		nValue += chr.getSkillLevel(20011002);
		int nLevel = chr.getLevel();
		if(nLevel >= 7) nLevel = 7;
		return nLevel + nValue - 1;
	}

	public static boolean isBeginnerSkill(int skillid){
		// Skills 'taught' to you somehow, not by distribute sp.
		if(skillid >= 1003 && skillid <= 9002) return true;// Explorer
		if(skillid >= 10001003 && skillid <= 10009002) return true;// Knight of Cygnus
		if(skillid >= 20001003 && skillid <= 20009002) return true;// Legend
		if(skillid >= 20011003 && skillid <= 20019002) return true;// Evan
		switch (skillid){// skills you can put SP in, handle in a separate method?
			case 1000:
			case 1001:
			case 1002:
			case 10001000:
			case 10001001:
			case 10001002:
			case 20001000:
			case 20001001:
			case 20001002:
			case 20011000:
			case 20011001:
			case 20011002:
				return true;
			default:
				return false;
		}
	}

	public static boolean isPqSkill(final int skill){
		return skill >= 20001013 && skill <= 20000018 || skill % 10000000 == 1020 || skill == 10000013 || skill % 10000000 >= 1009 && skill % 10000000 <= 1011;
	}

	public static boolean bannedBindSkills(final int skill){
		return isAranSkills(skill) || isPqSkill(skill);
	}

	public static boolean isGMSkills(final int skill){
		return skill >= 9001000 && skill <= 9101008 || skill >= 8001000 && skill <= 8001001;
	}

	public static boolean isDojo(int mapid){
		return mapid >= 925020100 && mapid <= 925023814;
	}

	public static boolean isPyramid(int mapid){
		return mapid >= 926010010 & mapid <= 930010000;
	}

	public static boolean isPQSkillMap(int mapid){
		return isDojo(mapid) || isPyramid(mapid);
	}

	public static boolean isFinisherSkill(int skillId){
		return skillId > 1111002 && skillId < 1111007 || skillId == 11111002 || skillId == 11111003;
	}

	public static boolean hasExtendedSPTable(MapleJob job){
		return job.getId() / 1000 == 3 || job.getId() / 100 == 22 || job.getId() == 2001;
	}

	public static int getAttackDelay(final int id, final Skill skill){
		switch (id){ // Assume it's faster(2)
			case Bowmaster.HURRICANE:
			case WindArcher.HURRICANE:
			case Corsair.RAPID_FIRE:
			case Gunslinger.RECOIL_SHOT:
				return 40;
			case NightWalker.TRIPLE_THROW:
			case NightLord.TRIPLE_THROW:
			case 5221007:
				return 570;
			case Aran.DOUBLE_SWING:
			case Aran.TRIPLE_SWING:
				return 300;
			case Aran.BODY_PRESSURE:
				return 0;
			case ChiefBandit.BAND_OF_THIEVES:
				return 660;
			case BlazeWizard.FIRE_PILLAR:
				return 1200;
			case 0:
			case ILWizard.THUNDER_BOLT:
				return 810;
		}
		if(skill != null && skill.getSkillType() == 3) return 0; // final attack
		if(skill != null && skill.getDelay() > 0 && !isNoDelaySkill(id)) return skill.getDelay();
		// TODO delay for final attack, weapon type, swing,stab etc
		return 330; // Default usually
	}

	public static boolean isNoDelaySkill(int skillId){
		switch (skillId){
			case Marauder.ENERGY_CHARGE:
			case ThunderBreaker.ENERGY_CHARGE:
				return true;
		}
		return false;
	}

	public static boolean isAffectedByAttackSpeed(int skillId){
		switch (skillId){
			case Cleric.HEAL:
			case ChiefBandit.BAND_OF_THIEVES:
				return false;
		}
		return true;
	}

	public static boolean isBadMap(int mapid){
		switch (mapid){
			case 180000000:
			case 280030000:
			case 240060000:
			case 240060100:
			case 240060200:
			case 551030200:
				return true;
		}
		return false;
	}
}
