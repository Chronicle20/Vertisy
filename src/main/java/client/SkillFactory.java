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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import constants.ServerConstants;
import constants.skills.*;
import provider.*;
import server.MapleStatEffect;
import server.life.Element;
import tools.ObjectParser;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericLittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

public class SkillFactory{

	private static Map<Integer, Skill> skills = new HashMap<>();
	private static MapleDataProvider datasource = null;
	private static MapleDataProvider delaySource = null;

	public static Skill getSkill(int id){
		if(!skills.isEmpty()) return skills.get(Integer.valueOf(id));
		return null;
	}

	public static Collection<Skill> getSkills(){
		return skills.values();
	}

	private static final File baseBin = new File(System.getProperty("wzpath") + "/bin/Skills/");

	public static void loadAllSkills(){
		baseBin.mkdirs();
		if(ServerConstants.WZ_LOADING){
			skills.clear();
			datasource = MapleDataProviderFactory.getDataProvider(MapleDataProviderFactory.fileInWZPath("Skill.wz"));
			delaySource = MapleDataProviderFactory.getDataProvider(MapleDataProviderFactory.fileInWZPath("Character.wz"));
			final MapleDataDirectoryEntry root = datasource.getRoot();
			final MapleData delayData = delaySource.getData("00002000.img");
			int skillid;
			for(MapleDataFileEntry topDir : root.getFiles()){ // Loop thru jobs
				if(topDir.getName().length() <= 8){
					for(MapleData data : datasource.getData(topDir.getName())){ // Loop thru each jobs
						if(data.getName().equals("skill")){
							for(MapleData data2 : data){ // Loop thru each jobs
								if(data2 != null){
									skillid = Integer.parseInt(data2.getName());
									Skill skill = loadFromData(skillid, data2, delayData);
									skills.put(skillid, skill);
									saveData(skill);
								}
							}
						}
					}
				}
			}
		}else{
			try{
				skills.clear();
				for(File f : baseBin.listFiles()){
					int skill = ObjectParser.isInt(f.getName().replace(".bin", "")).intValue();
					skills.put(skill, loadFromData(skill));
				}
			}catch(IOException ex){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
			}
		}
	}

	private static Skill loadFromData(int id, MapleData data, MapleData delayData){
		Skill ret = new Skill(id);
		boolean isBuff = false;
		ret.skillType = MapleDataTool.getInt("skillType", data, -1);
		String elem = MapleDataTool.getString("elemAttr", data, null);
		if(elem != null){
			ret.element = Element.getFromChar(elem.charAt(0));
		}else{
			ret.element = Element.NEUTRAL;
		}
		/*MapleData special = data.getChildByPath("special");
		if(special != null){
			if(id % 10000 < 1000) System.out.println(id + " has a special child");
		}*/
		MapleData effect = data.getChildByPath("effect");
		if(ret.skillType != -1){
			if(ret.skillType == 2){
				isBuff = true;
			}
		}else{
			MapleData action_ = data.getChildByPath("action");
			boolean action = false;
			if(action_ == null){
				if(data.getChildByPath("prepare/action") != null){
					action = true;
				}else{
					switch (id){
						case Gunslinger.INVISIBLE_SHOT:
						case Corsair.HYPNOTIZE:
							action = true;
							break;
					}
				}
			}else{
				action = true;
			}
			ret.action = action;
			MapleData hit = data.getChildByPath("hit");
			MapleData ball = data.getChildByPath("ball");
			isBuff = effect != null && hit == null && ball == null;
			if(action_ != null){
				String actStr = null;
				// if(action)
				// actStr = MapleDataTool.getString(action_, null);
				// else
				actStr = MapleDataTool.getString("0", action_, "");
				if(actStr != null){
					isBuff |= actStr.equals("alert2");
					/*MapleData act = delayData.getChildByPath(actStr);
					if(act != null) {
						for(MapleData d : act) {
							ret.delay += Math.abs(MapleDataTool.getInt("delay", d, 0));
						}
						if (ret.delay > 30) {
							ret.delay = (int) Math.round(ret.delay * 11.0 / 16.0);
							ret.delay -= (ret.delay % 30);
						}
					}*/
				}
			}
			if(hit != null){
				for(MapleData d : hit.getChildren()){
					for(MapleData i : d.getChildren()){
						ret.delay += MapleDataTool.getInt("delay", i, 0);
					}
				}
				if(ret.delay > 30){
					ret.delay = (int) Math.round(ret.delay * 11.0 / 16.0);
					ret.delay -= (ret.delay % 30);
				}
			}
			MapleData level = data.getChildByPath("level");
			if(level != null){
				for(MapleData lvl : level.getChildren()){
					MapleData lt = lvl.getChildByPath("lt");
					MapleData rb = lvl.getChildByPath("rb");
					MapleData range = lvl.getChildByPath("range");
					if(lt != null && rb != null){
						ret.lt.put(Integer.parseInt(lvl.getName()), MapleDataTool.getPoint(lt));
						ret.rb.put(Integer.parseInt(lvl.getName()), MapleDataTool.getPoint(rb));
					}
					if(range != null){
						ret.range.put(Integer.parseInt(lvl.getName()), MapleDataTool.getInt(range));
					}
				}
			}
			switch (id){
				case Hero.RUSH:
				case Paladin.RUSH:
				case DarkKnight.RUSH:
				case DragonKnight.SACRIFICE:
				case FPMage.EXPLOSION:
				case FPMage.POISON_MIST:
				case Cleric.HEAL:
				case Ranger.MORTAL_BLOW:
				case Sniper.MORTAL_BLOW:
				case Assassin.DRAIN:
				case Hermit.SHADOW_WEB:
				case Bandit.STEAL:
				case Shadower.SMOKE_SCREEN:
				case SuperGM.HEAL_PLUS_DISPEL:
				case Hero.MONSTER_MAGNET:
				case Paladin.MONSTER_MAGNET:
				case DarkKnight.MONSTER_MAGNET:
				case Evan.ICE_BREATH:
				case Evan.FIRE_BREATH:
				case Gunslinger.RECOIL_SHOT:
				case Marauder.ENERGY_DRAIN:
				case BlazeWizard.FLAME_GEAR:
				case NightWalker.SHADOW_WEB:
				case NightWalker.POISON_BOMB:
				case NightWalker.VAMPIRE:
				case ChiefBandit.CHAKRA:
				case Evan.RECOVERY_AURA:
					isBuff = false;
					break;
				case Beginner.RECOVERY:
				case Beginner.NIMBLE_FEET:
				case Beginner.MONSTER_RIDER:
				case Beginner.ECHO_OF_HERO:
				case Swordsman.IRON_BODY:
				case Fighter.AXE_BOOSTER:
				case Fighter.POWER_GUARD:
				case Fighter.RAGE:
				case Fighter.SWORD_BOOSTER:
				case Crusader.ARMOR_CRASH:
				case Crusader.COMBO:
				case Hero.ENRAGE:
				case Hero.HEROS_WILL:
				case Hero.MAPLE_WARRIOR:
				case Hero.STANCE:
				case Page.BW_BOOSTER:
				case Page.POWER_GUARD:
				case Page.SWORD_BOOSTER:
				case Page.THREATEN:
				case WhiteKnight.BW_FIRE_CHARGE:
				case WhiteKnight.BW_ICE_CHARGE:
				case WhiteKnight.BW_LIT_CHARGE:
				case WhiteKnight.MAGIC_CRASH:
				case WhiteKnight.SWORD_FIRE_CHARGE:
				case WhiteKnight.SWORD_ICE_CHARGE:
				case WhiteKnight.SWORD_LIT_CHARGE:
				case Paladin.BW_HOLY_CHARGE:
				case Paladin.HEROS_WILL:
				case Paladin.MAPLE_WARRIOR:
				case Paladin.STANCE:
				case Paladin.SWORD_HOLY_CHARGE:
				case Spearman.HYPER_BODY:
				case Spearman.IRON_WILL:
				case Spearman.POLEARM_BOOSTER:
				case Spearman.SPEAR_BOOSTER:
				case DragonKnight.DRAGON_BLOOD:
				case DragonKnight.POWER_CRASH:
				case DarkKnight.AURA_OF_BEHOLDER:
				case DarkKnight.BEHOLDER:
				case DarkKnight.HEROS_WILL:
				case DarkKnight.HEX_OF_BEHOLDER:
				case DarkKnight.MAPLE_WARRIOR:
				case DarkKnight.STANCE:
				case Magician.MAGIC_GUARD:
				case Magician.MAGIC_ARMOR:
				case FPWizard.MEDITATION:
				case FPWizard.SLOW:
				case FPMage.SEAL:
				case FPMage.SPELL_BOOSTER:
				case FPArchMage.HEROS_WILL:
				case FPArchMage.INFINITY:
				case FPArchMage.MANA_REFLECTION:
				case FPArchMage.MAPLE_WARRIOR:
				case ILWizard.MEDITATION:
				case ILMage.SEAL:
				case ILWizard.SLOW:
				case ILMage.SPELL_BOOSTER:
				case ILArchMage.HEROS_WILL:
				case ILArchMage.INFINITY:
				case ILArchMage.MANA_REFLECTION:
				case ILArchMage.MAPLE_WARRIOR:
				case Cleric.INVINCIBLE:
				case Cleric.BLESS:
				case Priest.DISPEL:
				case Priest.DOOM:
				case Priest.HOLY_SYMBOL:
				case Priest.MYSTIC_DOOR:
				case Bishop.HEROS_WILL:
				case Bishop.HOLY_SHIELD:
				case Bishop.INFINITY:
				case Bishop.MANA_REFLECTION:
				case Bishop.MAPLE_WARRIOR:
				case Archer.FOCUS:
				case Hunter.BOW_BOOSTER:
				case Hunter.SOUL_ARROW:
				case Ranger.PUPPET:
				case Bowmaster.CONCENTRATE:
				case Bowmaster.HEROS_WILL:
				case Bowmaster.MAPLE_WARRIOR:
				case Bowmaster.SHARP_EYES:
				case Crossbowman.CROSSBOW_BOOSTER:
				case Crossbowman.SOUL_ARROW:
				case Sniper.PUPPET:
				case Marksman.BLIND:
				case Marksman.HEROS_WILL:
				case Marksman.MAPLE_WARRIOR:
				case Marksman.SHARP_EYES:
				case Rogue.DARK_SIGHT:
				case Assassin.CLAW_BOOSTER:
				case Assassin.HASTE:
				case Hermit.MESO_UP:
				case Hermit.SHADOW_PARTNER:
				case NightLord.HEROS_WILL:
				case NightLord.MAPLE_WARRIOR:
				case NightLord.NINJA_AMBUSH:
				case NightLord.SHADOW_STARS:
				case Bandit.DAGGER_BOOSTER:
				case Bandit.HASTE:
				case ChiefBandit.MESO_GUARD:
				case ChiefBandit.PICKPOCKET:
				case Shadower.HEROS_WILL:
				case Shadower.MAPLE_WARRIOR:
				case Shadower.NINJA_AMBUSH:
				case Pirate.DASH:
				case Marauder.TRANSFORMATION:
				case Buccaneer.SUPER_TRANSFORMATION:
				case Corsair.BATTLE_SHIP:
				case GM.HIDE:
				case SuperGM.HASTE:
				case SuperGM.HOLY_SYMBOL:
				case SuperGM.BLESS:
				case SuperGM.HIDE:
				case SuperGM.HYPER_BODY:
				case Noblesse.BLESSING_OF_THE_FAIRY:
				case Noblesse.ECHO_OF_HERO:
				case Noblesse.MONSTER_RIDER:
				case Noblesse.NIMBLE_FEET:
				case Noblesse.RECOVERY:
				case DawnWarrior.COMBO:
				case DawnWarrior.FINAL_ATTACK:
				case DawnWarrior.IRON_BODY:
				case DawnWarrior.RAGE:
				case DawnWarrior.SOUL:
				case DawnWarrior.SOUL_CHARGE:
				case DawnWarrior.SWORD_BOOSTER:
				case BlazeWizard.ELEMENTAL_RESET:
				case BlazeWizard.FLAME:
				case BlazeWizard.IFRIT:
				case BlazeWizard.MAGIC_ARMOR:
				case BlazeWizard.MAGIC_GUARD:
				case BlazeWizard.MEDITATION:
				case BlazeWizard.SEAL:
				case BlazeWizard.SLOW:
				case BlazeWizard.SPELL_BOOSTER:
				case WindArcher.BOW_BOOSTER:
				case WindArcher.EAGLE_EYE:
				case WindArcher.FINAL_ATTACK:
				case WindArcher.FOCUS:
				case WindArcher.PUPPET:
				case WindArcher.SOUL_ARROW:
				case WindArcher.STORM:
				case WindArcher.WIND_WALK:
				case NightWalker.CLAW_BOOSTER:
				case NightWalker.DARKNESS:
				case NightWalker.DARK_SIGHT:
				case NightWalker.HASTE:
				case NightWalker.SHADOW_PARTNER:
				case ThunderBreaker.DASH:
				case ThunderBreaker.ENERGY_CHARGE:
				case ThunderBreaker.ENERGY_DRAIN:
				case ThunderBreaker.KNUCKLER_BOOSTER:
				case ThunderBreaker.LIGHTNING:
				case ThunderBreaker.SPARK:
				case ThunderBreaker.LIGHTNING_CHARGE:
				case ThunderBreaker.SPEED_INFUSION:
				case ThunderBreaker.TRANSFORMATION:
				case Legend.BLESSING_OF_THE_FAIRY:
				case Legend.AGILE_BODY:
				case Legend.ECHO_OF_HERO:
				case Legend.RECOVERY:
				case Legend.MONSTER_RIDER:
				case Aran.MAPLE_WARRIOR:
				case Aran.HEROS_WILL:
				case Aran.POLEARM_BOOSTER:
				case Aran.COMBO_DRAIN:
				case Aran.SNOW_CHARGE:
				case Aran.BODY_PRESSURE:
				case Aran.SMART_KNOCKBACK:
				case Aran.COMBO_BARRIER:
				case Aran.COMBO_ABILITY:
				case Evan.BLESSING_OF_THE_FAIRY:
				case Evan.RECOVERY:
				case Evan.NIMBLE_FEET:
				case Evan.HEROS_WILL:
				case Evan.ECHO_OF_HERO:
				case Evan.MAGIC_BOOSTER:
				case Evan.MAGIC_GUARD:
				case Evan.ELEMENTAL_RESET:
				case Evan.MAPLE_WARRIOR:
				case Evan.MAGIC_RESISTANCE:
				case Evan.MAGIC_SHIELD:
				case Evan.SLOW:
					isBuff = true;
					break;
			}
		}
		MapleData levelData = data.getChildByPath("level");
		if(levelData != null){
			for(MapleData level : levelData){
				ret.effects.add(MapleStatEffect.loadSkillEffectFromData(level, id, isBuff));
			}
		}
		ret.masterLevel = MapleDataTool.getInt("masterLevel", data, 0);
		ret.animationTime = 0;
		if(effect != null){
			for(MapleData effectEntry : effect){
				ret.animationTime += MapleDataTool.getIntConvert("delay", effectEntry, 0);
			}
		}
		return ret;
	}

	private static void saveData(Skill skill){
		if(ServerConstants.BIN_DUMPING){
			File file = new File(baseBin, "/" + skill.id + ".bin");
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			skill.save(mplew);
			try{
				file.createNewFile();
				mplew.saveToFile(file);
			}catch(IOException e){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
			}
		}
	}

	private static Skill loadFromData(int id) throws IOException{
		File file = new File(baseBin, "/" + id + ".bin");
		Skill skill = new Skill(id);
		byte[] in = Files.readAllBytes(file.toPath());
		ByteArrayByteStream babs = new ByteArrayByteStream(in);
		GenericLittleEndianAccessor glea = new GenericLittleEndianAccessor(babs);
		skill.load(glea);
		return skill;
	}

	public static String getSkillName(int skillid){
		MapleData data = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/" + "String.wz")).getData("Skill.img");
		StringBuilder skill = new StringBuilder();
		skill.append(String.valueOf(skillid));
		if(skill.length() == 4){
			skill.delete(0, 4);
			skill.append("000").append(String.valueOf(skillid));
		}
		if(data.getChildByPath(skill.toString()) != null){
			for(MapleData skilldata : data.getChildByPath(skill.toString()).getChildren()){
				if(skilldata.getName().equals("name")) return MapleDataTool.getString(skilldata, null);
			}
		}
		return null;
	}
}
