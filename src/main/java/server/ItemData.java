package server;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

import client.MapleClient;
import client.MapleJob;
import client.SkillFactory;
import client.inventory.MapleInventoryType;
import client.inventory.PetDataFactory.PetData;
import constants.ItemConstants;
import constants.ServerConstants;
import constants.skills.Assassin;
import constants.skills.NightWalker;
import provider.MapleData;
import provider.MapleDataTool;
import provider.wz.MapleDataType;
import server.ItemInformationProvider.RewardItem;
import server.ItemInformationProvider.scriptedItem;
import tools.ObjectParser;
import tools.Pair;
import tools.Randomizer;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericLittleEndianAccessor;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.LittleEndianWriter;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Mar 16, 2017
 */
public class ItemData{

	public String wzPath = "";
	public int itemid;
	public boolean exists = true;
	public String name = "", msg = "";
	public int price = -1;
	public double unitPrice = 0;
	public int meso, wholePrice;
	public int recoveryHP, recoveryMP;
	public double recovery;
	public int tamingMob;
	public boolean isCash;
	public String islot = "", vslot = "";
	public int reqJob, reqLevel, reqStr, reqDex, reqInt, reqLuk, reqPop;
	public int tuc, cursed, success, fs, rcount;
	public short incLEV, incReqLevel, incStr, incInt, incDex, incLuk, incAcc, incPAD, incPDD, incMAD, incMDD, incHP, incMHP, incMaxHP, incMHPr, incMMP, incMaxMP, incMMPr, incEVA, incSpeed, incJump, incPVPDamage, incCraft;
	public boolean tradeBlock, accountSharable, equipTradeBlock;
	public int quest;
	public boolean noCancelMouse, tradeAvailable, pickupRestricted, onlyOnePickup/*onlyPickup*/, consumeOnPickup;
	public int stateChangeItem, exp, maxLevel;
	public int attackSpeed;
	//
	public boolean notSale, expireOnLogout;// TODO: Handle
	public scriptedItem scriptedItem = new scriptedItem(0, "", false);
	// maker
	public short lv;
	public int randOption, randStat;
	//
	public int slotMax = -1;
	//
	public Pair<Integer, List<RewardItem>> rewardItems;
	//
	public MapleStatEffect itemEffect;
	//
	public List<Integer> petCanConsume = new ArrayList<>();
	// mastery book
	public int masterLevel, reqSkillLevel;
	public List<Integer> skills = new ArrayList<>();
	// summoning bags
	public List<Pair<Integer, Integer>> mobs = new ArrayList<>();
	// What items the scroll is allowed on
	public List<Integer> allowedItems = new ArrayList<>();
	//
	public Map<String, LevelData> levelData = new HashMap<>();
	public Map<Integer, SkillData> skillData = new HashMap<>();// The level of the item you get the skills at.
	public int statIncreaseProb, probForLevelSkill;
	//
	public boolean evol, autoReact;
	public int evol1, evol2, evol3, evol4, evol5, evolNo, evolProb1, evolProb2, evolProb3, evolProb4, evolProb5, evolReqItemID, evolReqPetLvl;
	// Pet
	public int chatBalloon, nameTag, hungry, life;
	public Map<String, PetData> petData = new HashMap<>();
	public boolean noRevive;
	public int limitedLife;
	//
	public boolean permanent;// use
	public boolean sweepForDrop, interactByUserAction;// idk
	//
	public boolean noMoveToLocker;
	public List<String> absAction = new ArrayList<>();
	//
	List<String> lAction = Arrays.asList(// Pet actions that we shouldn't load
	        "slang", "monolog", "interact", "food", "move", "stand0", "stand1", "jump", "hungry", "rest0", "rest1", "hang", "info");
	//
	public List<Integer> weaponTypes = new ArrayList<>();
	// Shit below this actually has to be used someday.(or most of it)
	public int maplepoint;// used for some maplepoint item
	// used for exp coupons
	public int rate;
	// public String time;
	// used for item locks
	public int protectTime;
	// scissors of karma, 1 or 2
	public int karma;
	// used for remote pot shop
	public int npc;
	// used for safety charm
	public int recoveryRate;
	// monster books
	public boolean monsterBook;
	public int mob;// can turn card itemid to mob
	// Like one of a kind, but you can specify how much?
	public int max;
	// some mob shit on potion consume
	public boolean mobPotion;
	public int mobHp, mobID;
	//
	public boolean timeLimited;
	// scroll booleans
	// rocover = white scroll, randstat = dark scroll
	public boolean preventslip, warmsupport, recover, randstat;
	// weapon shit
	public int durability = -1;

	public void loadFromXML(String wzPath, int itemid){
		this.wzPath = wzPath;
		this.itemid = itemid;
		MapleData itemData = ItemInformationProvider.getInstance().getItemMapleData(itemid);
		if(itemData == null){
			exists = false;
			// System.out.println("itemid: " + itemid + " has null itemdata");
			return;
		}
		MapleData rewardData = itemData.getChildByPath("reward");
		if(rewardData != null){
			int totalprob = 0;
			List<RewardItem> rewards = new ArrayList<RewardItem>();
			for(MapleData child : rewardData.getChildren()){
				RewardItem reward = new RewardItem();
				reward.itemid = MapleDataTool.getInt("item", child, 0);
				reward.prob = (byte) MapleDataTool.getInt("prob", child, 0);
				reward.quantity = (short) MapleDataTool.getInt("count", child, 0);
				reward.effect = MapleDataTool.getString("Effect", child, "");
				reward.worldmsg = MapleDataTool.getString("worldMsg", child, "");
				reward.period = MapleDataTool.getInt("period", child, -1);
				totalprob += reward.prob;
				rewards.add(reward);
			}
			rewardItems = new Pair<Integer, List<RewardItem>>(totalprob, rewards);
		}
		MapleData strings = ItemInformationProvider.getInstance().getStringData(itemid);
		if(strings != null){
			name = MapleDataTool.getString("name", strings, "");
			msg = MapleDataTool.getString("msg", strings, "");
		}
		for(MapleData data : itemData.getChildren()){
			if(!lAction.contains(data.getName())){
				absAction.add(data.getName());
			}
			Integer weapon_type = ObjectParser.isInt(data.getName());
			if(weapon_type != null) weaponTypes.add(weapon_type);
		}
		MapleData spec = itemData.getChildByPath("spec");
		if(spec != null){
			itemEffect = MapleStatEffect.loadItemEffectFromData(spec, itemid);
			itemEffect.itemEffect = true;
			for(MapleData specSub : spec.getChildren()){
				switch (specSub.getName()){
					case "exp":
						exp = MapleDataTool.getInt(specSub);
						break;
					case "consumeOnPickup":
						consumeOnPickup = MapleDataTool.getInt(specSub, 0) == 1;
						break;
					case "npc":
						scriptedItem.npc = MapleDataTool.getInt(specSub);
						break;
					case "script":
						scriptedItem.script = MapleDataTool.getString(specSub);
						break;
					case "runOnPickup":
						scriptedItem.runOnPickup = MapleDataTool.getInt(specSub) == 1;
						break;
					case "onlyPickup":
						onlyOnePickup = MapleDataTool.getInt(specSub) == 1;
						break;
					case "time":// MapleStatEffect
					case "hp":
					case "hpR":
					case "mp":
					case "mpR":
					case "mpCon":
					case "hpCon":
					case "prop":
					case "mobCount":
					case "cooltime":
					case "ghost":
					case "incFatigue":
					case "pad":
					case "pdd":
					case "mad":
					case "mdd":
					case "acc":
					case "eva":
					case "speed":
					case "jump":
					case "berserk":
					case "booster":
					case "mesoupbyitem":
					case "itemupbyitem":
					case "lt":
					case "rb":
					case "x":
					case "y":
					case "damage":
					case "fixdamage":
					case "attackCount":
					case "bulletCount":
					case "bulletConsume":
					case "moneyCon":
					case "itemCon":
					case "itemConNo":
					case "moveTo":
					case "prob":
						break;
					default:
						if(specSub.getType().equals(MapleDataType.INT)){
							Integer nameInt = ObjectParser.isInt(specSub.getName());
							if(nameInt != null){
								int id = MapleDataTool.getInt(specSub, 0);
								if(id != 0){
									petCanConsume.add(id);
									break;
								}
							}
						}
						// System.out.println("Unhandled specSub: " + specSub.getName() + " item: " + itemid);
						break;
				}
			}
		}
		MapleData specEx = itemData.getChildByPath("specEx");
		if(specEx != null){
			for(MapleData specSub : specEx.getChildren()){
				switch (specSub.getName()){
					// nexon has ints for names. They have mobSkill, level, target properties. Just inflicts those on pickup most likely.
					// no clue the point of 'target' property, but its set to 1.
					case "consumeOnPickup":
						consumeOnPickup = MapleDataTool.getInt(specSub, 0) == 1;
						break;
					default:
						System.out.println("Unhandled specExSub: " + specSub.getName() + " item: " + itemid);
						break;
				}
			}
		}
		MapleData mobData = itemData.getChildByPath("mob");
		if(mobData != null){
			for(MapleData mobDataa : mobData.getChildren()){
				mobs.add(new Pair<Integer, Integer>(MapleDataTool.getInt("id", mobDataa, 0), MapleDataTool.getInt("prob", mobDataa, 0)));
			}
		}
		MapleData req = itemData.getChildByPath("req");
		if(req != null){
			for(MapleData reqData : req.getChildren()){
				int item = MapleDataTool.getInt(reqData, 0);
				if(item != 0) allowedItems.add(item);
			}
		}
		MapleData interact = itemData.getChildByPath("interact");
		if(interact != null){
			for(MapleData reqData : interact.getChildren()){
				Integer skillid = ObjectParser.isInt(reqData.getName());
				if(skillid != null){
					PetData data = new PetData();
					data.command = MapleDataTool.getString("command", reqData, "");
					data.inc = MapleDataTool.getInt("inc", reqData, 1);
					data.l0 = MapleDataTool.getInt("l0", reqData, 1);
					data.l1 = MapleDataTool.getInt("l1", reqData, 1);
					data.prob = MapleDataTool.getInt("prob", reqData, 1);
					MapleData fail = reqData.getChildByPath("fail").getChildByPath("0");
					if(fail != null){
						data.fail.act = MapleDataTool.getString("act", fail, "");
						for(MapleData failData : fail.getChildren()){
							Integer id = ObjectParser.isInt(failData.getName());
							if(id != null){
								data.fail.response.add(MapleDataTool.getString(failData));
							}
						}
					}
					MapleData success = reqData.getChildByPath("success").getChildByPath("0");
					if(success != null){
						data.success.act = MapleDataTool.getString("act", success, "");
						for(MapleData successData : success.getChildren()){
							Integer id = ObjectParser.isInt(successData.getName());
							if(id != null){
								data.success.response.add(MapleDataTool.getString(successData));
							}
						}
					}
					petData.put("" + skillid.intValue(), data);
				}
			}
		}
		MapleData info = itemData.getChildByPath("info");
		if(info != null){
			for(MapleData infoSub : info.getChildren()){
				switch (infoSub.getName()){
					case "incLEV":
						incLEV = MapleDataTool.getShort(infoSub);
						break;
					case "incReqLevel":
						incReqLevel = MapleDataTool.getShort(infoSub);
						break;
					case "incSTR":
						incStr = MapleDataTool.getShort(infoSub);
						break;
					case "incDEX":
						incDex = MapleDataTool.getShort(infoSub);
						break;
					case "incINT":
						incInt = MapleDataTool.getShort(infoSub);
						break;
					case "incLuk":
					case "incLUk":
					case "incLUK":
						incLuk = MapleDataTool.getShort(infoSub);
						break;
					case "incACC":
						incAcc = MapleDataTool.getShort(infoSub);
						break;
					case "incPAD":
						incPAD = MapleDataTool.getShort(infoSub);
						break;
					case "incPDD":
						incPDD = MapleDataTool.getShort(infoSub);
						break;
					case "incMAD":
						incMAD = MapleDataTool.getShort(infoSub);
						break;
					case "incMDD":
						incMDD = MapleDataTool.getShort(infoSub);
						break;
					case "incHP":
						incHP = MapleDataTool.getShort(infoSub);
						break;
					case "incMHP":
						incMHP = MapleDataTool.getShort(infoSub);
						break;
					case "incMaxHP":
						incMaxHP = MapleDataTool.getShort(infoSub);
						break;
					case "incMHPr":
						incMHPr = MapleDataTool.getShort(infoSub);
						break;
					case "incMMP":
						incMMP = MapleDataTool.getShort(infoSub);
						break;
					case "incMaxMP":
						incMaxMP = MapleDataTool.getShort(infoSub);
						break;
					case "incMMPr":
						incMMPr = MapleDataTool.getShort(infoSub);
						break;
					case "incEVA":
						incEVA = MapleDataTool.getShort(infoSub);
						break;
					case "incSpeed":
						incSpeed = MapleDataTool.getShort(infoSub);
						break;
					case "incJump":
						incJump = MapleDataTool.getShort(infoSub);
						break;
					case "incPVPDamage":
						incPVPDamage = MapleDataTool.getShort(infoSub);
						break;
					case "incCraft":
						incCraft = MapleDataTool.getShort(infoSub);
						break;
					case "equipTradeBlock":
						equipTradeBlock = MapleDataTool.getIntConvert(infoSub) == 1;
						break;
					case "tradeBlock":
						tradeBlock = MapleDataTool.getIntConvert(infoSub) == 1;
						break;
					case "accountSharable":
						accountSharable = MapleDataTool.getIntConvert(infoSub) == 1;
						break;
					case "quest":
						quest = MapleDataTool.getIntConvert(infoSub);
						break;
					case "reqJob":
						reqJob = MapleDataTool.getInt(infoSub, 0);
						break;
					case "reqLevel":
						reqLevel = MapleDataTool.getInt(infoSub, 0);
						break;
					case "reqSTR":
						reqStr = MapleDataTool.getInt(infoSub, 0);
						break;
					case "reqDEX":
						reqDex = MapleDataTool.getInt(infoSub, 0);
						break;
					case "reqINT":
						reqInt = MapleDataTool.getInt(infoSub, 0);
						break;
					case "reqLUK":
						reqLuk = MapleDataTool.getInt(infoSub, 0);
						break;
					case "reqPOP":
						reqPop = MapleDataTool.getInt(infoSub, 0);
						break;
					case "tuc":
						tuc = MapleDataTool.getInt(infoSub, 0);
						if(tuc > 0 && ItemConstants.MAX_SCROLLS != -1) tuc = ItemConstants.MAX_SCROLLS;
						break;
					case "cursed":
						cursed = MapleDataTool.getInt(infoSub, 0);
						break;
					case "success":
						success = MapleDataTool.getInt(infoSub, 0);
						break;
					case "fs":
						fs = MapleDataTool.getInt(infoSub, 0);
						break;
					case "rcount":
						rcount = MapleDataTool.getInt(infoSub, 0);
						break;
					case "recoveryHP":
						recoveryHP = MapleDataTool.getInt(infoSub, 0);
						break;
					case "recoveryMP":
						recoveryMP = MapleDataTool.getInt(infoSub, 0);
						break;
					case "recovery":
						recovery = MapleDataTool.getFloat(infoSub, 0);
						break;
					case "tamingMob":
						tamingMob = MapleDataTool.getInt(infoSub, 0);
						break;
					case "level":
						MapleData sData = infoSub.getChildByPath("case");
						if(sData != null){
							MapleData maxProbData = sData.getChildByPath("0");
							this.statIncreaseProb = MapleDataTool.getInt(maxProbData.getChildByPath("prob"), 0);
							MapleData probData = sData.getChildByPath("1");
							if(probData != null){
								this.probForLevelSkill = MapleDataTool.getInt(probData.getChildByPath("prob"), 0);
								for(MapleData level : sData.getChildByPath("1").getChildren()){
									Integer itemLevel = ObjectParser.isInt(level.getName());
									if(itemLevel != null){
										if(level.getChildByPath("Skill") != null){// High versions have "ItemSkill" which is 'potential skills'
											SkillData data = new SkillData();
											data.load(level);
											skillData.put(itemLevel, data);
										}
									}
								}
							}
						}
						MapleData lData = infoSub.getChildByPath("info");
						if(lData != null){
							for(MapleData level : lData.getChildren()){
								LevelData data = new LevelData();
								data.load(level, statIncreaseProb);
								levelData.put(level.getName(), data);
							}
						}
						break;
					case "cash":
						isCash = MapleDataTool.getInt(infoSub, 0) == 1;
						break;
					case "meso":
						meso = MapleDataTool.getInt(infoSub, -1);
						break;
					case "price":
						wholePrice = MapleDataTool.getInt(infoSub, 0);
						if(price == -1) price = wholePrice;
						break;
					case "islot":
						islot = MapleDataTool.getString(infoSub, "");
						break;
					case "vslot":
						vslot = MapleDataTool.getString(infoSub, "");
						break;
					case "unitPrice":
						unitPrice = MapleDataTool.getDouble(infoSub);
						break;
					case "noCancelMouse":
						noCancelMouse = MapleDataTool.getInt(infoSub) == 1;
						break;
					case "tradeAvailable":
						tradeAvailable = MapleDataTool.getInt(infoSub) == 1;
						break;
					case "only":
						pickupRestricted = MapleDataTool.getIntConvert(infoSub) == 1;
						break;
					case "onlyPickup":
						onlyOnePickup = MapleDataTool.getInt(infoSub) == 1;
						break;
					case "stateChangeItem":
						stateChangeItem = MapleDataTool.getInt(infoSub);
						break;
					case "maxLevel":
						maxLevel = MapleDataTool.getInt(infoSub, 256);
						break;
					case "attackSpeed":
						attackSpeed = MapleDataTool.getInt(infoSub, 6);
						break;
					case "lv":
						lv = MapleDataTool.getShort(infoSub);
						break;
					case "randOption":
						randOption = MapleDataTool.getInt(infoSub);
						break;
					case "randStat":
						randStat = MapleDataTool.getInt(infoSub);
						break;
					case "slotMax":
						slotMax = MapleDataTool.getInt(infoSub, -1);
						break;
					case "masterLevel":
						masterLevel = MapleDataTool.getInt(infoSub, 0);
						break;
					case "reqSkillLevel":
						reqSkillLevel = MapleDataTool.getInt(infoSub, 0);
						break;
					case "skill":
						for(MapleData data : infoSub.getChildren()){
							int skill = MapleDataTool.getInt(data, 0);
							if(skill == 0) continue;
							skills.add(skill);
						}
						break;
					case "chatBalloon":
						chatBalloon = MapleDataTool.getInt(infoSub, 0);
						break;
					case "nameTag":
						nameTag = MapleDataTool.getInt(infoSub, 0);
						break;
					case "hungry":
						hungry = MapleDataTool.getInt(infoSub, 1);
						break;
					case "life":
						life = MapleDataTool.getInt(infoSub, 0);
						break;
					case "limitedLife":
						limitedLife = MapleDataTool.getInt(infoSub, 0);
						break;
					case "noRevive":
						noRevive = MapleDataTool.getInt(infoSub, 0) == 1;
						break;
					case "noMoveToLocker":
						noMoveToLocker = MapleDataTool.getInt(infoSub, 0) == 1;
						break;
					case "notSale":
						notSale = MapleDataTool.getInt(infoSub, 0) == 1;
						break;
					case "expireOnLogout":
						expireOnLogout = MapleDataTool.getInt(infoSub, 0) == 1;
						break;
					case "maplepoint":
						maplepoint = MapleDataTool.getInt(infoSub, 0);
						break;
					case "rate":
						// rate = MapleDataTool.getInt(infoSub, 0);
						break;
					case "time":
						// time = MapleDataTool.getString(infoSub);
						break;
					case "protectTime":
						protectTime = MapleDataTool.getInt(infoSub, 0);
						break;
					case "karma":
						karma = MapleDataTool.getInt(infoSub, 0);
						break;
					case "npc":
						npc = MapleDataTool.getInt(infoSub, 0);
						break;
					case "recoveryRate":
						recoveryRate = MapleDataTool.getInt(infoSub, 0);
						break;
					case "monsterBook":
						monsterBook = MapleDataTool.getInt(infoSub, 0) == 1;
						break;
					case "mob":
						mob = MapleDataTool.getInt(infoSub, 0);
						break;
					case "max":
						max = MapleDataTool.getInt(infoSub, 0);
						break;
					case "mobPotion":
						mobPotion = MapleDataTool.getInt(infoSub, 0) == 1;
						break;
					case "mobHp":
						mobHp = MapleDataTool.getInt(infoSub, 0);
						break;
					case "mobID":
						mobID = MapleDataTool.getInt(infoSub, 0);
						break;
					case "durability":
						durability = MapleDataTool.getInt(infoSub, 0);
						break;
					case "evol":
						evol = MapleDataTool.getInt(infoSub, 0) == 1;
						break;
					case "evol1":
						evol1 = MapleDataTool.getInt(infoSub, 0);
						break;
					case "evol2":
						evol2 = MapleDataTool.getInt(infoSub, 0);
						break;
					case "evol3":
						evol3 = MapleDataTool.getInt(infoSub, 0);
						break;
					case "evol4":
						evol4 = MapleDataTool.getInt(infoSub, 0);
						break;
					case "evol5":
						evol5 = MapleDataTool.getInt(infoSub, 0);
						break;
					case "evolNo":
						evolNo = MapleDataTool.getInt(infoSub, 0);
						break;
					case "evolProb1":
						evolProb1 = MapleDataTool.getInt(infoSub, 0);
						break;
					case "evolProb2":
						evolProb2 = MapleDataTool.getInt(infoSub, 0);
						break;
					case "evolProb3":
						evolProb3 = MapleDataTool.getInt(infoSub, 0);
						break;
					case "evolProb4":
						evolProb4 = MapleDataTool.getInt(infoSub, 0);
						break;
					case "evolProb5":
						evolProb5 = MapleDataTool.getInt(infoSub, 0);
						break;
					case "evolReqItemID":
						evolReqItemID = MapleDataTool.getInt(infoSub, 0);
						break;
					case "evolReqPetLvl":
						evolReqPetLvl = MapleDataTool.getInt(infoSub, 0);
						break;
					case "autoReact":
						autoReact = MapleDataTool.getInt(infoSub, 0) == 1;
						break;
					case "permanent":
						permanent = MapleDataTool.getInt(infoSub, 0) == 1;
						break;
					case "sweepForDrop":
						sweepForDrop = MapleDataTool.getInt(infoSub, 0) == 1;
						break;
					case "interactByUserAction":
						interactByUserAction = MapleDataTool.getInt(infoSub, 0) == 1;
						break;
					case "timeLimited":
						timeLimited = MapleDataTool.getInt(infoSub, 0) == 1;
						break;
					case "preventslip":
						preventslip = MapleDataTool.getInt(infoSub, 0) == 1;
						break;
					case "warmsupport":
						warmsupport = MapleDataTool.getInt(infoSub, 0) == 1;
						break;
					case "recover":
						randstat = MapleDataTool.getInt(infoSub, 0) == 1;
						break;
					case "randstat":
						randstat = MapleDataTool.getInt(infoSub, 0) == 1;
						break;
					case "slotIndex":// ?? some extra slot thing
					case "addDay":// ^
						break;
					case "distanceX":
					case "distanceY":
					case "maxDiff":
					case "enchantCategory":// apparently for potential? --- important?
					case "mcType":// Used for monster carnival
					case "soldInform":// some hired merchants have this
					case "mesomin":// meso bag shit
					case "mesomax":
					case "mesostdev":
					case "pickupAll":// used for pet skills
					case "dropSweep":// ^
					case "consumeHP":// ^
					case "consumeMP":// ^
					case "ignorePickup":// ^
					case "longRange":// ^
					case "pickupItem":// ^
					case "add":// ^
					case "pachinko":// slot machine shit for kms
					case "lt":// used for some effect thing
					case "rb":
					case "isBgmOrEffect":// some weather shit, prob used but fuck it
					case "bgmPath":// same as ^
					case "repeat":// clientside
					case "noFlip":
					case "path":// used for maphelper shit.. idk if server needs
					case "emotion":
					case "floatType":// more weather maphelper shit.. prob only clientside
					case "direction":// more weather things
					case "speed":// more weather stuff
					case "addTime":// Some cash item. 5500006
					case "maxDays":// ^
					case "type":// Used for summoning bags.. might have a use idk
					case "random":// Also used for summoning bags.
					case "iconReward":// for christmas number things
					case "iconRaw":
					case "iconRawD":// dead pet
					case "icon":
					case "iconD":// dead pet
					case "sfx":
					case "afterImage":
					case "walk":
					case "stand":
					case "attack":// ?
					case "sample":// Used for stuff like rings.. clientside
					case "effect":// More clientside ring stuff
					case "incSwim":// unused
					case "incFatigue":
					case "incRMAF":
					case "incRMAS":
					case "incRMAI":
					case "incRMAL":
					case "incAttackCount":
					case "reqGuildLevel":
					case "bigSize":// Used for client, maybe use for item vac?
						break;
					default:
						// System.out.println("Unhandled infoSub: " + infoSub.getName() + " in item: " + itemid);
						break;
				}
				if(strings == null && info == null){
					exists = false;
				}
			}
		}
		if(slotMax == -1){
			if(ItemConstants.getInventoryType(itemid).getType() == MapleInventoryType.EQUIP.getType()){
				slotMax = 1;
			}else slotMax = 100;
		}
	}

	public boolean doesBinExist(String wzPath, int itemid){
		return new File("./wz/bin/Items/" + wzPath + "/" + itemid + ".bin").exists();
	}

	public void loadFromBin(String wzPath, int itemid){
		this.wzPath = wzPath;
		this.itemid = itemid;
		File file = new File("./wz/bin/Items/" + wzPath + "/" + itemid + ".bin");
		if(!file.exists()){
			exists = false;
			return;
		}
		try{
			byte[] in = Files.readAllBytes(file.toPath());
			ByteArrayByteStream babs = new ByteArrayByteStream(in);
			GenericLittleEndianAccessor glea = new GenericLittleEndianAccessor(babs);
			price = glea.readInt();
			unitPrice = glea.readDouble();
			recoveryHP = glea.readInt();
			recoveryMP = glea.readInt();
			recovery = glea.readDouble();
			tamingMob = glea.readInt();
			int size = glea.readInt();
			for(int i = 0; i < size; i++){
				String level = glea.readMapleAsciiString();
				LevelData data = new LevelData();
				data.load(glea);
				this.levelData.put(level, data);
			}
			isCash = glea.readBoolean();
			meso = glea.readInt();
			wholePrice = glea.readInt();
			islot = glea.readMapleAsciiString();
			reqJob = glea.readInt();
			reqLevel = glea.readInt();
			reqStr = glea.readInt();
			reqDex = glea.readInt();
			reqInt = glea.readInt();
			reqLuk = glea.readInt();
			reqPop = glea.readInt();
			incLEV = glea.readShort();
			incReqLevel = glea.readShort();
			incStr = glea.readShort();
			incInt = glea.readShort();
			incDex = glea.readShort();
			incLuk = glea.readShort();
			incAcc = glea.readShort();
			incPAD = glea.readShort();
			incPDD = glea.readShort();
			incMAD = glea.readShort();
			incMDD = glea.readShort();
			incHP = glea.readShort();
			incMHP = glea.readShort();
			incMaxHP = glea.readShort();
			incMHPr = glea.readShort();
			incMMP = glea.readShort();
			incMaxMP = glea.readShort();
			incMMPr = glea.readShort();
			incEVA = glea.readShort();
			incSpeed = glea.readShort();
			incJump = glea.readShort();
			incPVPDamage = glea.readShort();
			incCraft = glea.readShort();
			tuc = glea.readInt();
			cursed = glea.readInt();
			success = glea.readInt();
			fs = glea.readInt();
			rcount = glea.readInt();
			tradeBlock = glea.readBoolean();
			accountSharable = glea.readBoolean();
			quest = glea.readInt();
			equipTradeBlock = glea.readBoolean();
			noCancelMouse = glea.readBoolean();
			tradeAvailable = glea.readBoolean();
			pickupRestricted = glea.readBoolean();
			name = glea.readMapleAsciiString();
			msg = glea.readMapleAsciiString();
			onlyOnePickup = glea.readBoolean();
			stateChangeItem = glea.readInt();
			exp = glea.readInt();
			maxLevel = glea.readInt();
			consumeOnPickup = glea.readBoolean();
			scriptedItem.npc = glea.readInt();
			scriptedItem.script = glea.readMapleAsciiString();
			scriptedItem.runOnPickup = glea.readBoolean();
			attackSpeed = glea.readInt();
			lv = glea.readShort();
			randOption = glea.readInt();
			randStat = glea.readInt();
			slotMax = glea.readInt();
			if(glea.readBoolean()){
				int maxProb = glea.readInt();
				List<RewardItem> items = new ArrayList<>();
				int total = glea.readInt();
				for(int i = 0; i < total; i++){
					RewardItem item = new RewardItem();
					item.load(glea);
					items.add(item);
				}
				rewardItems = new Pair<Integer, List<RewardItem>>(maxProb, items);
			}
			if(glea.readBoolean()){
				itemEffect = new MapleStatEffect();
				itemEffect.load(glea);
			}
			size = glea.readInt();
			for(int i = 0; i < size; i++){
				petCanConsume.add(glea.readInt());
			}
			masterLevel = glea.readInt();
			reqSkillLevel = glea.readInt();
			size = glea.readInt();
			for(int i = 0; i < size; i++){
				skills.add(glea.readInt());
			}
			size = glea.readInt();
			for(int i = 0; i < size; i++){
				mobs.add(new Pair<Integer, Integer>(glea.readInt(), glea.readInt()));
			}
			size = glea.readInt();
			for(int i = 0; i < size; i++){
				allowedItems.add(glea.readInt());
			}
			chatBalloon = glea.readInt();
			nameTag = glea.readInt();
			hungry = glea.readInt();
			life = glea.readInt();
			size = glea.readInt();
			for(int i = 0; i < size; i++){
				String skillid = glea.readMapleAsciiString();
				PetData data = new PetData();
				data.load(glea);
				petData.put(skillid, data);
			}
			size = glea.readInt();
			for(int i = 0; i < size; i++){
				int level = glea.readInt();
				SkillData data = new SkillData();
				data.load(glea);
				skillData.put(level, data);
			}
			probForLevelSkill = glea.readInt();
			statIncreaseProb = glea.readInt();
			noRevive = glea.readBoolean();
			limitedLife = glea.readInt();
			noMoveToLocker = glea.readBoolean();
			size = glea.readInt();
			for(int i = 0; i < size; i++){
				absAction.add(glea.readMapleAsciiString());
			}
			notSale = glea.readBoolean();
			expireOnLogout = glea.readBoolean();
			size = glea.readInt();
			for(int i = 0; i < size; i++){
				weaponTypes.add(glea.readInt());
			}
			maplepoint = glea.readInt();
			rate = glea.readInt();
			// nx coupon time shit here
			protectTime = glea.readInt();
			karma = glea.readInt();
			npc = glea.readInt();
			recoveryRate = glea.readInt();
			monsterBook = glea.readBoolean();
			mob = glea.readInt();
			max = glea.readInt();
			mobPotion = glea.readBoolean();
			mobHp = glea.readInt();
			mobID = glea.readInt();
			durability = glea.readInt();
			evol = glea.readBoolean();
			autoReact = glea.readBoolean();
			evol1 = glea.readInt();
			evol2 = glea.readInt();
			evol3 = glea.readInt();
			evol4 = glea.readInt();
			evol5 = glea.readInt();
			evolNo = glea.readInt();
			evolProb1 = glea.readInt();
			evolProb2 = glea.readInt();
			evolProb3 = glea.readInt();
			evolProb4 = glea.readInt();
			evolProb5 = glea.readInt();
			evolReqItemID = glea.readInt();
			evolReqPetLvl = glea.readInt();
			permanent = glea.readBoolean();
			sweepForDrop = glea.readBoolean();
			interactByUserAction = glea.readBoolean();
			timeLimited = glea.readBoolean();
			preventslip = glea.readBoolean();
			warmsupport = glea.readBoolean();
			recover = glea.readBoolean();
			randstat = glea.readBoolean();
			glea = null;
			babs = null;
			in = null;
		}catch(Exception ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, "Failed to load bin item: " + wzPath + "/" + itemid);
		}
	}

	public void saveToBin(){
		if(!ServerConstants.BIN_DUMPING) return;
		try{
			File file = new File("./wz/bin/Items/" + wzPath + "/" + itemid + ".bin");
			if(file.exists()) return;
			if(file.getParentFile() != null) file.getParentFile().mkdirs();
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeInt(price);
			mplew.writeDouble(unitPrice);
			mplew.writeInt(recoveryHP);
			mplew.writeInt(recoveryMP);
			mplew.writeDouble(recovery);
			mplew.writeInt(tamingMob);
			mplew.writeInt(levelData.size());
			for(String level : levelData.keySet()){
				mplew.writeMapleAsciiString(level);
				levelData.get(level).save(mplew);
			}
			mplew.writeBoolean(isCash);
			mplew.writeInt(meso);
			mplew.writeInt(wholePrice);
			mplew.writeMapleAsciiString(islot);
			mplew.writeInt(reqJob);
			mplew.writeInt(reqLevel);
			mplew.writeInt(reqStr);
			mplew.writeInt(reqDex);
			mplew.writeInt(reqInt);
			mplew.writeInt(reqLuk);
			mplew.writeInt(reqPop);
			mplew.writeShort(incLEV);
			mplew.writeShort(incReqLevel);
			mplew.writeShort(incStr);
			mplew.writeShort(incInt);
			mplew.writeShort(incDex);
			mplew.writeShort(incLuk);
			mplew.writeShort(incAcc);
			mplew.writeShort(incPAD);
			mplew.writeShort(incPDD);
			mplew.writeShort(incMAD);
			mplew.writeShort(incMDD);
			mplew.writeShort(incHP);
			mplew.writeShort(incMHP);
			mplew.writeShort(incMaxHP);
			mplew.writeShort(incMHPr);
			mplew.writeShort(incMMP);
			mplew.writeShort(incMaxMP);
			mplew.writeShort(incMMPr);
			mplew.writeShort(incEVA);
			mplew.writeShort(incSpeed);
			mplew.writeShort(incJump);
			mplew.writeShort(incPVPDamage);
			mplew.writeShort(incCraft);
			mplew.writeInt(tuc);
			mplew.writeInt(cursed);
			mplew.writeInt(success);
			mplew.writeInt(fs);
			mplew.writeInt(rcount);
			mplew.writeBoolean(tradeBlock);
			mplew.writeBoolean(accountSharable);
			mplew.writeInt(quest);
			mplew.writeBoolean(equipTradeBlock);
			mplew.writeBoolean(noCancelMouse);
			mplew.writeBoolean(tradeAvailable);
			mplew.writeBoolean(pickupRestricted);
			mplew.writeMapleAsciiString(name);
			mplew.writeMapleAsciiString(msg);
			mplew.writeBoolean(onlyOnePickup);
			mplew.writeInt(stateChangeItem);
			mplew.writeInt(exp);
			mplew.writeInt(maxLevel);
			mplew.writeBoolean(consumeOnPickup);
			mplew.writeInt(scriptedItem.npc);
			mplew.writeMapleAsciiString(scriptedItem.script);
			mplew.writeBoolean(scriptedItem.runOnPickup);
			mplew.writeInt(attackSpeed);
			mplew.writeShort(lv);
			mplew.writeInt(randOption);
			mplew.writeInt(randStat);
			mplew.writeInt(slotMax);
			mplew.writeBoolean(rewardItems != null);
			if(rewardItems != null){
				mplew.writeInt(rewardItems.left);
				mplew.writeInt(rewardItems.right.size());
				for(RewardItem item : rewardItems.right){
					item.save(mplew);
				}
			}
			mplew.writeBoolean(itemEffect != null);
			if(itemEffect != null){
				itemEffect.save(mplew);
			}
			mplew.writeInt(petCanConsume.size());
			for(int id : petCanConsume){
				mplew.writeInt(id);
			}
			mplew.writeInt(masterLevel);
			mplew.writeInt(reqSkillLevel);
			mplew.writeInt(skills.size());
			for(int skill : skills){
				mplew.writeInt(skill);
			}
			mplew.writeInt(mobs.size());
			for(Pair<Integer, Integer> p : mobs){
				mplew.writeInt(p.left);
				mplew.writeInt(p.right);
			}
			mplew.writeInt(allowedItems.size());
			for(int item : allowedItems){
				mplew.writeInt(item);
			}
			mplew.writeInt(chatBalloon);
			mplew.writeInt(nameTag);
			mplew.writeInt(hungry);
			mplew.writeInt(life);
			mplew.writeInt(petData.size());
			for(String skillid : petData.keySet()){
				mplew.writeMapleAsciiString(skillid);
				petData.get(skillid).save(mplew);
			}
			mplew.writeInt(skillData.size());
			for(int level : skillData.keySet()){
				mplew.writeInt(level);
				skillData.get(level).save(mplew);
			}
			mplew.writeInt(probForLevelSkill);
			mplew.writeInt(statIncreaseProb);
			mplew.writeBoolean(noRevive);
			mplew.writeInt(limitedLife);
			mplew.writeBoolean(noMoveToLocker);
			mplew.writeInt(absAction.size());
			for(String s : absAction){
				mplew.writeMapleAsciiString(s);
			}
			mplew.writeBoolean(notSale);
			mplew.writeBoolean(expireOnLogout);
			mplew.writeInt(weaponTypes.size());
			for(int type : weaponTypes){
				mplew.writeInt(type);
			}
			mplew.writeInt(maplepoint);
			mplew.writeInt(rate);
			// time shit
			mplew.writeInt(protectTime);
			mplew.writeInt(karma);
			mplew.writeInt(npc);
			mplew.writeInt(recoveryRate);
			mplew.writeBoolean(monsterBook);
			mplew.writeInt(mob);
			mplew.writeInt(max);
			mplew.writeBoolean(mobPotion);
			mplew.writeInt(mobHp);
			mplew.writeInt(mobID);
			mplew.writeInt(durability);
			mplew.writeBoolean(evol);
			mplew.writeBoolean(autoReact);
			mplew.writeInt(evol1);
			mplew.writeInt(evol2);
			mplew.writeInt(evol3);
			mplew.writeInt(evol4);
			mplew.writeInt(evol5);
			mplew.writeInt(evolNo);
			mplew.writeInt(evolProb1);
			mplew.writeInt(evolProb2);
			mplew.writeInt(evolProb3);
			mplew.writeInt(evolProb4);
			mplew.writeInt(evolProb5);
			mplew.writeInt(evolReqItemID);
			mplew.writeInt(evolReqPetLvl);
			mplew.writeBoolean(permanent);
			mplew.writeBoolean(sweepForDrop);
			mplew.writeBoolean(interactByUserAction);
			mplew.writeBoolean(timeLimited);
			mplew.writeBoolean(preventslip);
			mplew.writeBoolean(warmsupport);
			mplew.writeBoolean(recover);
			mplew.writeBoolean(randstat);
			mplew.saveToFile(file);// 24
		}catch(Exception ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, "Failed to save bin item: " + wzPath + "/" + itemid);
		}
	}

	public boolean isDropRestricted(){
		return tradeBlock || accountSharable || quest > 0;
	}

	public short getSlotMax(MapleClient c){
		short ret = (short) slotMax;
		if(c != null){
			if(ItemConstants.isThrowingStar(itemid)){
				if(c.getPlayer().getJob().isA(MapleJob.NIGHTWALKER1)){
					ret += c.getPlayer().getSkillLevel(SkillFactory.getSkill(NightWalker.CLAW_MASTERY)) * 10;
				}else{
					ret += c.getPlayer().getSkillLevel(SkillFactory.getSkill(Assassin.CLAW_MASTERY)) * 10;
				}
			}else if(ItemConstants.isBullet(itemid)){
				ret += c.getPlayer().getSkillLevel(SkillFactory.getSkill(5200000)) * 10;
			}
		}
		return ret;
	}

	public static class SkillData{

		public List<Pair<Integer, Integer>> skills = new ArrayList<>();

		public void load(MapleData data){
			for(MapleData skill : data.getChildByPath("Skill").getChildren()){
				skills.add(new Pair<Integer, Integer>(MapleDataTool.getInt("id", skill, 0), MapleDataTool.getInt("level", skill, 0)));
			}
		}

		public void load(LittleEndianAccessor lea){
			int size = lea.readInt();
			for(int i = 0; i < size; i++){
				skills.add(new Pair<Integer, Integer>(lea.readInt(), lea.readInt()));
			}
		}

		public void save(LittleEndianWriter lew){
			lew.writeInt(skills.size());
			for(Pair<Integer, Integer> p : skills){
				lew.writeInt(p.left);
				lew.writeInt(p.right);
			}
		}
	}

	public static class LevelData{

		public int incDEXMin, incDEXMax, incSTRMin, incSTRMax, incINTMin, incINTMax, incLUKMin, incLUKMax, incMHPMin, incMHPMax, incMMPMin, incMMPMax, incPADMin, incPADMax, incMADMin, incMADMax, incPDDMin, incPDDMax, incMDDMin, incMDDMax, incACCMin, incACCMax, incEVAMin, incEVAMax, incSpeedMin, incSpeedMax, incJumpMin, incJumpMax;
		public int exp;
		public double statIncreaseProb;

		public List<Pair<String, Integer>> getLevelupStats(){
			List<Pair<String, Integer>> list = new LinkedList<>();
			if(incDEXMax != 0 && Math.random() < statIncreaseProb / 10){
				list.add(new Pair<>("incDEX", Randomizer.rand(incDEXMin, incDEXMax)));
			}
			if(incSTRMax != 0 && Math.random() < statIncreaseProb / 10){
				list.add(new Pair<>("incSTR", Randomizer.rand(incSTRMin, incSTRMax)));
			}
			if(incINTMax != 0 && Math.random() < statIncreaseProb / 10){
				list.add(new Pair<>("incINT", Randomizer.rand(incINTMin, incINTMax)));
			}
			if(incLUKMax != 0 && Math.random() < statIncreaseProb / 10){
				list.add(new Pair<>("incLUK", Randomizer.rand(incLUKMin, incLUKMax)));
			}
			if(incMHPMax != 0 && Math.random() < statIncreaseProb / 10){
				list.add(new Pair<>("incMHP", Randomizer.rand(incMHPMin, incMHPMax)));
			}
			if(incMMPMax != 0 && Math.random() < statIncreaseProb / 10){
				list.add(new Pair<>("incMMP", Randomizer.rand(incMMPMin, incMMPMax)));
			}
			if(incPADMax != 0 && Math.random() < statIncreaseProb / 10){
				list.add(new Pair<>("incPAD", Randomizer.rand(incPADMin, incPADMax)));
			}
			if(incMADMax != 0 && Math.random() < statIncreaseProb / 10){
				list.add(new Pair<>("incMAD", Randomizer.rand(incMADMin, incMADMax)));
			}
			if(incPDDMax != 0 && Math.random() < statIncreaseProb / 10){
				list.add(new Pair<>("incPDD", Randomizer.rand(incPDDMin, incPDDMax)));
			}
			if(incMDDMax != 0 && Math.random() < statIncreaseProb / 10){
				list.add(new Pair<>("incMDD", Randomizer.rand(incMDDMin, incMDDMax)));
			}
			if(incACCMax != 0 && Math.random() < statIncreaseProb / 10){
				list.add(new Pair<>("incACC", Randomizer.rand(incACCMin, incACCMax)));
			}
			if(incEVAMax != 0 && Math.random() < statIncreaseProb / 10){
				list.add(new Pair<>("incEVA", Randomizer.rand(incEVAMin, incEVAMax)));
			}
			if(incSpeedMax != 0 && Math.random() < statIncreaseProb / 10){
				list.add(new Pair<>("incSpeed", Randomizer.rand(incSpeedMin, incSpeedMax)));
			}
			if(incJumpMax != 0 && Math.random() < statIncreaseProb / 10){
				list.add(new Pair<>("incJump", Randomizer.rand(incJumpMin, incJumpMax)));
			}
			return list;
		}

		public void load(MapleData data, int statIncreaseProb){
			this.statIncreaseProb = statIncreaseProb;
			for(MapleData d : data.getChildren()){
				switch (d.getName()){
					case "incDEXMin":
						incDEXMin = MapleDataTool.getInt(d, 0);
						break;
					case "incDEXMax":
						incDEXMax = MapleDataTool.getInt(d, 0);
						break;
					case "incSTRMin":
						incSTRMin = MapleDataTool.getInt(d, 0);
						break;
					case "incSTRMax":
						incSTRMax = MapleDataTool.getInt(d, 0);
						break;
					case "incINTMin":
						incINTMin = MapleDataTool.getInt(d, 0);
						break;
					case "incINTMax":
						incINTMax = MapleDataTool.getInt(d, 0);
						break;
					case "incLUKMin":
						incLUKMin = MapleDataTool.getInt(d, 0);
						break;
					case "incLUKMax":
						incLUKMax = MapleDataTool.getInt(d, 0);
						break;
					case "incMHPMin":
						incMHPMin = MapleDataTool.getInt(d, 0);
						break;
					case "incMHPMax":
						incMHPMax = MapleDataTool.getInt(d, 0);
						break;
					case "incMMPMin":
						incMMPMin = MapleDataTool.getInt(d, 0);
						break;
					case "incMMPMax":
						incMMPMax = MapleDataTool.getInt(d, 0);
						break;
					case "incPADMin":
						incPADMin = MapleDataTool.getInt(d, 0);
						break;
					case "incPADMax":
						incPADMax = MapleDataTool.getInt(d, 0);
						break;
					case "incMADMin":
						incMADMin = MapleDataTool.getInt(d, 0);
						break;
					case "incMADMax":
						incMADMax = MapleDataTool.getInt(d, 0);
						break;
					case "incPDDMin":
						incPDDMin = MapleDataTool.getInt(d, 0);
						break;
					case "incPDDMax":
						incPDDMax = MapleDataTool.getInt(d, 0);
						break;
					case "incMDDMin":
						incMDDMin = MapleDataTool.getInt(d, 0);
						break;
					case "incMDDMax":
						incMDDMax = MapleDataTool.getInt(d, 0);
						break;
					case "incACCMin":
						incACCMin = MapleDataTool.getInt(d, 0);
						break;
					case "incACCMax":
						incACCMax = MapleDataTool.getInt(d, 0);
						break;
					case "incEVAMin":
						incEVAMin = MapleDataTool.getInt(d, 0);
						break;
					case "incEVAMax":
						incEVAMax = MapleDataTool.getInt(d, 0);
						break;
					case "incSpeedMin":
						incSpeedMin = MapleDataTool.getInt(d, 0);
						break;
					case "incSpeedMax":
						incSpeedMax = MapleDataTool.getInt(d, 0);
						break;
					case "incJumpMin":
						incJumpMin = MapleDataTool.getInt(d, 0);
						break;
					case "incJumpMax":
						incJumpMax = MapleDataTool.getInt(d, 0);
						break;
					case "exp":
						exp = MapleDataTool.getInt(d, 0);
						break;
					default:
						System.out.println("Invalid leveldata: " + d.getName());
						break;
				}
			}
		}

		public void load(GenericLittleEndianAccessor glea){
			incDEXMin = glea.readInt();
			incDEXMax = glea.readInt();
			incSTRMin = glea.readInt();
			incSTRMax = glea.readInt();
			incINTMin = glea.readInt();
			incINTMax = glea.readInt();
			incLUKMin = glea.readInt();
			incLUKMax = glea.readInt();
			incMHPMin = glea.readInt();
			incMHPMax = glea.readInt();
			incMMPMin = glea.readInt();
			incMMPMax = glea.readInt();
			incPADMin = glea.readInt();
			incPADMax = glea.readInt();
			incMADMin = glea.readInt();
			incMADMax = glea.readInt();
			incPDDMin = glea.readInt();
			incPDDMax = glea.readInt();
			incMDDMin = glea.readInt();
			incMDDMax = glea.readInt();
			incACCMin = glea.readInt();
			incACCMax = glea.readInt();
			incEVAMin = glea.readInt();
			incEVAMax = glea.readInt();
			incSpeedMin = glea.readInt();
			incSpeedMax = glea.readInt();
			incJumpMin = glea.readInt();
			incJumpMax = glea.readInt();
			exp = glea.readInt();
			statIncreaseProb = glea.readDouble();
		}

		public void save(MaplePacketLittleEndianWriter mplew){
			mplew.writeInt(incDEXMin);
			mplew.writeInt(incDEXMax);
			mplew.writeInt(incSTRMin);
			mplew.writeInt(incSTRMax);
			mplew.writeInt(incINTMin);
			mplew.writeInt(incINTMax);
			mplew.writeInt(incLUKMin);
			mplew.writeInt(incLUKMax);
			mplew.writeInt(incMHPMin);
			mplew.writeInt(incMHPMax);
			mplew.writeInt(incMMPMin);
			mplew.writeInt(incMMPMax);
			mplew.writeInt(incPADMin);
			mplew.writeInt(incPADMax);
			mplew.writeInt(incMADMin);
			mplew.writeInt(incMADMax);
			mplew.writeInt(incPDDMin);
			mplew.writeInt(incPDDMax);
			mplew.writeInt(incMDDMin);
			mplew.writeInt(incMDDMax);
			mplew.writeInt(incACCMin);
			mplew.writeInt(incACCMax);
			mplew.writeInt(incEVAMin);
			mplew.writeInt(incEVAMax);
			mplew.writeInt(incSpeedMin);
			mplew.writeInt(incSpeedMax);
			mplew.writeInt(incJumpMin);
			mplew.writeInt(incJumpMax);
			mplew.writeInt(exp);
			mplew.writeDouble(statIncreaseProb);
		}
	}
}