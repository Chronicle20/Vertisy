/*
 * This file is part of the OdinMS Maple Story Server
 * Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 * Matthias Butz <matze@odinms.de>
 * Jan Christian Meyer <vimes@odinms.de>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation version 3 as published by
 * the Free Software Foundation. You may not use, modify or distribute
 * this program under any other version of the GNU Affero General Public
 * License.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package client;

import constants.JobConstants;

public enum MapleJob{
	BEGINNER(0),
	WARRIOR(100),
	FIGHTER(110),
	CRUSADER(111),
	HERO(112),
	PAGE(120),
	WHITEKNIGHT(121),
	PALADIN(122),
	SPEARMAN(130),
	DRAGONKNIGHT(131),
	DARKKNIGHT(132),
	MAGICIAN(200),
	FP_WIZARD(210),
	FP_MAGE(211),
	FP_ARCHMAGE(212),
	IL_WIZARD(220),
	IL_MAGE(221),
	IL_ARCHMAGE(222),
	CLERIC(230),
	PRIEST(231),
	BISHOP(232),
	BOWMAN(300),
	HUNTER(310),
	RANGER(311),
	BOWMASTER(312),
	CROSSBOWMAN(320),
	SNIPER(321),
	MARKSMAN(322),
	THIEF(400),
	ASSASSIN(410),
	HERMIT(411),
	NIGHTLORD(412),
	BANDIT(420),
	CHIEFBANDIT(421),
	SHADOWER(422),
	BLADE_RECRUIT(430),
	BLADE_ACOLYTE(431),
	BLADE_SPECIALIST(432),
	BLADE_LORD(433),
	BLADE_MASTER(434),
	PIRATE(500),
	BRAWLER(510),
	MARAUDER(511),
	BUCCANEER(512),
	GUNSLINGER(520),
	OUTLAW(521),
	CORSAIR(522),
	MAPLELEAF_BRIGADIER(800),
	GM(900),
	SUPERGM(910),
	MWLB(920),
	NOBLESSE(1000),
	DAWNWARRIOR1(1100),
	DAWNWARRIOR2(1110),
	DAWNWARRIOR3(1111),
	DAWNWARRIOR4(1112),
	BLAZEWIZARD1(1200),
	BLAZEWIZARD2(1210),
	BLAZEWIZARD3(1211),
	BLAZEWIZARD4(1212),
	WINDARCHER1(1300),
	WINDARCHER2(1310),
	WINDARCHER3(1311),
	WINDARCHER4(1312),
	NIGHTWALKER1(1400),
	NIGHTWALKER2(1410),
	NIGHTWALKER3(1411),
	NIGHTWALKER4(1412),
	THUNDERBREAKER1(1500),
	THUNDERBREAKER2(1510),
	THUNDERBREAKER3(1511),
	THUNDERBREAKER4(1512),
	LEGEND(2000),
	EVAN(2001),
	ARAN1(2100),
	ARAN2(2110),
	ARAN3(2111),
	ARAN4(2112),
	EVAN1(2200),
	EVAN2(2210),
	EVAN3(2211),
	EVAN4(2212),
	EVAN5(2213),
	EVAN6(2214),
	EVAN7(2215),
	EVAN8(2216),
	EVAN9(2217),
	EVAN10(2218),
	CITIZEN(3000),
    //
	DEMON_AVENGER1(3101),
	DEMON_AVENGER2(3120),
	DEMON_AVENGER3(3121),
	DEMON_AVENGER4(3122),
    //
	BATTLE_MAGE1(3200),
	BATTLE_MAGE2(3210),
	BATTLE_MAGE3(3211),
	BATTLE_MAGE4(3212),
    //
	WILD_HUNTER1(3300),
	WILD_HUNTER2(3310),
	WILD_HUNTER3(3311),
	WILD_HUNTER4(3312),
    //
	MECHANIC1(3500),
	MECHANIC2(3510),
	MECHANIC3(3511),
	MECHANIC4(3512),;

	final int jobid;

	private MapleJob(int id){
		jobid = id;
	}

	public int getId(){
		return jobid;
	}

	public static String getName(MapleJob mjob){
		return mjob.name();
	}

	public static MapleJob getById(int id){
		for(MapleJob l : MapleJob.values()){
			if(l.getId() == id) return l;
		}
		return null;
	}

	public static boolean checkJobMask(int mask, MapleJob toCheck){
		long maskToCheck = getBy5ByteEncoding(toCheck);
		return (mask & maskToCheck) == maskToCheck;
	}

	public static long getBy5ByteEncoding(MapleJob job){
		return 1L << (job.getId() / 100);
	}

	public boolean isBeginner(MapleJob beginners){
		return MAGICIAN == beginners || WARRIOR == beginners || THIEF == beginners || PIRATE == beginners || BOWMAN == beginners || ARAN1 == beginners || THUNDERBREAKER1 == beginners || DAWNWARRIOR1 == beginners || NIGHTWALKER1 == beginners || BLAZEWIZARD1 == beginners;
	}

	public boolean isA(MapleJob basejob){
		return getId() >= basejob.getId() && getId() / 100 == basejob.getId() / 100;
	}

	public byte getJobAdvancementCategory(){// This is JobConstants.get_job_level
		switch (this){
			case BEGINNER:
			case NOBLESSE:
			case LEGEND:
			case EVAN:
			case CITIZEN:
				return 0;
			case WARRIOR:
			case MAGICIAN:
			case BOWMAN:
			case THIEF:
			case PIRATE:
			case ARAN1:
			case BLAZEWIZARD1:
			case DAWNWARRIOR1:
			case WINDARCHER1:
			case THUNDERBREAKER1:
			case NIGHTWALKER1:
			case BATTLE_MAGE1:
			case DEMON_AVENGER1:
			case MECHANIC1:
			case WILD_HUNTER1:
			case BLADE_ACOLYTE:
				return 1;
			case SPEARMAN:
			case FIGHTER:
			case PAGE:
			case CLERIC:
			case FP_MAGE:
			case IL_MAGE:
			case ASSASSIN:
			case BANDIT:
			case CROSSBOWMAN:
			case HUNTER:
			case GUNSLINGER:
			case BRAWLER:
			case ARAN2:
			case BLAZEWIZARD2:
			case DAWNWARRIOR2:
			case WINDARCHER2:
			case THUNDERBREAKER2:
			case NIGHTWALKER2:
			case EVAN1:
			case EVAN2:
			case EVAN3:
			case BATTLE_MAGE2:
			case DEMON_AVENGER2:
			case MECHANIC2:
			case WILD_HUNTER2:
			case BLADE_LORD:
				return 2;
			case CRUSADER:
			case DRAGONKNIGHT:
			case WHITEKNIGHT:
			case PRIEST:
			case FP_WIZARD:
			case IL_WIZARD:
			case RANGER:
			case SNIPER:
			case HERMIT:
			case CHIEFBANDIT:
			case MARAUDER:
			case OUTLAW:
			case ARAN3:
			case BLAZEWIZARD3:
			case DAWNWARRIOR3:
			case WINDARCHER3:
			case THUNDERBREAKER3:
			case NIGHTWALKER3:
			case EVAN4:
			case EVAN5:
			case EVAN6:
			case EVAN7:
			case BATTLE_MAGE3:
			case DEMON_AVENGER3:
			case MECHANIC3:
			case WILD_HUNTER3:
			case BLADE_MASTER:
				return 3;
			case PALADIN:
			case DARKKNIGHT:
			case HERO:
			case BISHOP:
			case FP_ARCHMAGE:
			case IL_ARCHMAGE:
			case BOWMASTER:
			case MARKSMAN:
			case NIGHTLORD:
			case SHADOWER:
			case BUCCANEER:
			case CORSAIR:
			case ARAN4:
			case BLAZEWIZARD4:
			case DAWNWARRIOR4:
			case WINDARCHER4:
			case THUNDERBREAKER4:
			case NIGHTWALKER4:
			case EVAN8:
			case EVAN9:
			case EVAN10:
			case BATTLE_MAGE4:
			case DEMON_AVENGER4:
			case MECHANIC4:
			case WILD_HUNTER4:
			case BLADE_RECRUIT:
			case BLADE_SPECIALIST:// i guess
				return 4;
			case GM:
			case MAPLELEAF_BRIGADIER:
			case SUPERGM:
			case MWLB:
				return 5;
			default:
				break;
		}
		return -1;
	}

	// update below
	public MapleJob get2ndJob(){
		switch (this){
			case FIGHTER:
			case CRUSADER:
			case HERO:
				return FIGHTER;
			case PAGE:
			case WHITEKNIGHT:
			case PALADIN:
				return PAGE;
			case SPEARMAN:
			case DRAGONKNIGHT:
			case DARKKNIGHT:
				return SPEARMAN;
			case FP_WIZARD:
			case FP_MAGE:
			case FP_ARCHMAGE:
				return FP_WIZARD;
			case IL_WIZARD:
			case IL_MAGE:
			case IL_ARCHMAGE:
				return IL_WIZARD;
			case CLERIC:
			case PRIEST:
			case BISHOP:
				return CLERIC;
			case HUNTER:
			case RANGER:
			case BOWMASTER:
				return HUNTER;
			case CROSSBOWMAN:
			case SNIPER:
			case MARKSMAN:
				return CROSSBOWMAN;
			case ASSASSIN:
			case HERMIT:
			case NIGHTLORD:
				return ASSASSIN;
			case BANDIT:
			case CHIEFBANDIT:
			case SHADOWER:
				return BANDIT;
			case BRAWLER:
			case MARAUDER:
			case BUCCANEER:
				return BRAWLER;
			case GUNSLINGER:
			case OUTLAW:
			case CORSAIR:
				return CORSAIR;
			case DAWNWARRIOR1:
			case DAWNWARRIOR2:
			case DAWNWARRIOR3:
			case DAWNWARRIOR4:
				return DAWNWARRIOR2;
			case BLAZEWIZARD1:
			case BLAZEWIZARD2:
			case BLAZEWIZARD3:
			case BLAZEWIZARD4:
				return BLAZEWIZARD2;
			case WINDARCHER1:
			case WINDARCHER2:
			case WINDARCHER3:
			case WINDARCHER4:
				return WINDARCHER2;
			case NIGHTWALKER1:
			case NIGHTWALKER2:
			case NIGHTWALKER3:
			case NIGHTWALKER4:
				return NIGHTWALKER2;
			case THUNDERBREAKER1:
			case THUNDERBREAKER2:
			case THUNDERBREAKER3:
			case THUNDERBREAKER4:
				return THUNDERBREAKER2;
			case ARAN2:
			case ARAN3:
			case ARAN4:
				return ARAN2;
			case EVAN3:
			case EVAN4:
			case EVAN5:
			case EVAN6:
			case EVAN7:
			case EVAN8:
			case EVAN9:
			case EVAN10:
				return EVAN2;
			default:
				return null;
		}
	}

	public MapleJob[] getJobTree(){
		switch (this){
			case FIGHTER:
			case CRUSADER:
			case HERO:
				return new MapleJob[]{WARRIOR, FIGHTER, CRUSADER, HERO};
			case PAGE:
			case WHITEKNIGHT:
			case PALADIN:
				return new MapleJob[]{WARRIOR, PAGE, WHITEKNIGHT, PALADIN};
			case SPEARMAN:
			case DRAGONKNIGHT:
			case DARKKNIGHT:
				return new MapleJob[]{WARRIOR, SPEARMAN, DRAGONKNIGHT, DARKKNIGHT};
			case FP_WIZARD:
			case FP_MAGE:
			case FP_ARCHMAGE:
				return new MapleJob[]{MAGICIAN, FP_WIZARD, FP_MAGE, FP_ARCHMAGE};
			case IL_WIZARD:
			case IL_MAGE:
			case IL_ARCHMAGE:
				return new MapleJob[]{MAGICIAN, IL_WIZARD, IL_MAGE, IL_ARCHMAGE};
			case CLERIC:
			case PRIEST:
			case BISHOP:
				return new MapleJob[]{MAGICIAN, CLERIC, PRIEST, BISHOP};
			case HUNTER:
			case RANGER:
			case BOWMASTER:
				return new MapleJob[]{BOWMAN, HUNTER, RANGER, BOWMASTER};
			case CROSSBOWMAN:
			case SNIPER:
			case MARKSMAN:
				return new MapleJob[]{BOWMAN, CROSSBOWMAN, SNIPER, MARKSMAN};
			case ASSASSIN:
			case HERMIT:
			case NIGHTLORD:
				return new MapleJob[]{THIEF, ASSASSIN, HERMIT, NIGHTLORD};
			case BANDIT:
			case CHIEFBANDIT:
			case SHADOWER:
				return new MapleJob[]{THIEF, BANDIT, CHIEFBANDIT, SHADOWER};
			case BRAWLER:
			case MARAUDER:
			case BUCCANEER:
				return new MapleJob[]{PIRATE, BRAWLER, MARAUDER, BUCCANEER};
			case GUNSLINGER:
			case OUTLAW:
			case CORSAIR:
				return new MapleJob[]{PIRATE, GUNSLINGER, OUTLAW, CORSAIR};
			case DAWNWARRIOR1:
			case DAWNWARRIOR2:
			case DAWNWARRIOR3:
			case DAWNWARRIOR4:
				return new MapleJob[]{DAWNWARRIOR1, DAWNWARRIOR2, DAWNWARRIOR3, DAWNWARRIOR4};
			case BLAZEWIZARD1:
			case BLAZEWIZARD2:
			case BLAZEWIZARD3:
			case BLAZEWIZARD4:
				return new MapleJob[]{BLAZEWIZARD1, BLAZEWIZARD2, BLAZEWIZARD3, BLAZEWIZARD4};
			case WINDARCHER1:
			case WINDARCHER2:
			case WINDARCHER3:
			case WINDARCHER4:
				return new MapleJob[]{WINDARCHER1, WINDARCHER2, WINDARCHER3, WINDARCHER4};
			case NIGHTWALKER1:
			case NIGHTWALKER2:
			case NIGHTWALKER3:
			case NIGHTWALKER4:
				return new MapleJob[]{NIGHTWALKER1, NIGHTWALKER2, NIGHTWALKER3, NIGHTWALKER4};
			case THUNDERBREAKER1:
			case THUNDERBREAKER2:
			case THUNDERBREAKER3:
			case THUNDERBREAKER4:
				return new MapleJob[]{THUNDERBREAKER1, THUNDERBREAKER2, THUNDERBREAKER3, THUNDERBREAKER4};
			case LEGEND:
			case ARAN1:
			case ARAN2:
			case ARAN3:
			case ARAN4:
				return new MapleJob[]{ARAN1, ARAN2, ARAN3, ARAN4};
			case EVAN:
			case EVAN1:
			case EVAN2:
			case EVAN3:
			case EVAN4:
			case EVAN5:
			case EVAN6:
			case EVAN7:
			case EVAN8:
			case EVAN9:
			case EVAN10:
				return new MapleJob[]{EVAN1, EVAN2, EVAN3, EVAN4, EVAN5, EVAN6, EVAN7, EVAN8, EVAN9, EVAN10};
			case BLADE_RECRUIT:
			case BLADE_ACOLYTE:
			case BLADE_SPECIALIST:
			case BLADE_LORD:
			case BLADE_MASTER:
				return new MapleJob[]{THIEF, BLADE_RECRUIT, BLADE_ACOLYTE, BLADE_SPECIALIST, BLADE_LORD, BLADE_MASTER};
			default:
				break;
		}
		return null;
	}

	public boolean isAran(){
		return jobid / 100 == 21 || jobid == 2000;
	}

	public boolean isBeginner(){
		if(jobid == 0) return true;
		if(jobid == 1000) return true;
		if(jobid == 2000) return true;
		if(jobid == 2001) return true;
		if(jobid == 3000) return true;
		return false;
	}

	public boolean isWarrior(){
		if(jobid >= 100 && jobid <= 132) return true;
		if(jobid >= 1100 && jobid <= 1112) return true;
		if(isAran()) return true;
		return false;
	}

	public boolean isMagician(){
		if(jobid >= 200 && jobid <= 232) return true;
		if(jobid >= 1200 && jobid <= 1212) return true;
		if(JobConstants.is_evan_job(jobid)) return true;
		return false;
	}

	public boolean isArcher(){
		if(jobid >= 300 && jobid <= 322) return true;
		if(jobid >= 1300 && jobid <= 1312) return true;
		return false;
	}

	public boolean isThief(){
		if(jobid >= 400 && jobid <= 422) return true;
		if(jobid >= 1400 && jobid <= 1412) return true;
		if(JobConstants.is_dualblade_job(jobid)) return true;
		return false;
	}

	public boolean isPirate(){
		if(jobid >= 500 && jobid <= 522) return true;
		if(jobid >= 1500 && jobid <= 1512) return true;
		return false;
	}
}
