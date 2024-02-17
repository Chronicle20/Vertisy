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
package client.inventory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import client.MapleBuffStat;
import client.MapleClient;
import client.MapleStat;
import constants.ExpTable;
import constants.ItemConstants;
import server.ItemData;
import server.ItemData.SkillData;
import server.ItemInformationProvider;
import server.item.Potential;
import tools.Pair;
import tools.Randomizer;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.UserLocal;
import tools.packets.field.userpool.UserRemote;

public class Equip extends Item{

	public static enum ScrollResult{
		FAIL(0),
		SUCCESS(1),
		CURSE(2);

		private int value = -1;

		private ScrollResult(int value){
			this.value = value;
		}

		public int getValue(){
			return value;
		}
	}

	private byte upgradeSlots;
	private byte level, flag, itemLevel;
	public short str, dex, _int, luk, hp, mp, watk, matk, wdef, mdef, acc, avoid, hands, speed, jump, vicious;
	private int nDurability = -1;
	private float itemExp;
	private int ringid = -1;
	private boolean wear = false, learnedSkills;
	private byte grade;
	// 0 == nothing
	// 1,2,3 is magnifying glass(To identify what grade after reveal), 4 prob a spacer for legendary
	// 5,6,7 is actual pot
	//
	private byte chuc;// EE
	private short option1, option2, option3;

	public Equip(int id, short position){
		super(id, position, (short) 1);
		this.itemExp = 0;
		this.itemLevel = 1;
		this.nDurability = Math.max(ItemInformationProvider.getInstance().getItemData(id).durability, -1);
	}

	public Equip(int id, short position, int slots){
		super(id, position, (short) 1);
		this.upgradeSlots = (byte) slots;
		this.itemExp = 0;
		this.itemLevel = 1;
		this.nDurability = Math.max(ItemInformationProvider.getInstance().getItemData(id).durability, -1);
	}

	public Equip(ResultSet rs) throws SQLException{
		super(rs);
		setOwner(rs.getString("owner"));
		setAcc(rs.getShort("acc"));
		setAvoid(rs.getShort("avoid"));
		setDex(rs.getShort("dex"));
		setHands(rs.getShort("hands"));
		setHp(rs.getShort("hp"));
		setInt(rs.getShort("int"));
		setJump(rs.getShort("jump"));
		setVicious(rs.getShort("vicious"));
		setFlag(rs.getByte("flag"));
		setLuk(rs.getShort("luk"));
		setMatk(rs.getShort("matk"));
		setMdef(rs.getShort("mdef"));
		setMp(rs.getShort("mp"));
		setSpeed(rs.getShort("speed"));
		setStr(rs.getShort("str"));
		setWatk(rs.getShort("watk"));
		setWdef(rs.getShort("wdef"));
		setUpgradeSlots(rs.getByte("upgradeslots"));
		setLevel(rs.getByte("level"));
		setItemExp(rs.getFloat("itemexp"));
		setItemLevel(rs.getByte("itemlevel"));
		setExpiration(rs.getLong("expiration"));
		setLockExpiration(rs.getLong("lockExpiration"));
		setGiftFrom(rs.getString("giftFrom"));
		setRingId(rs.getInt("ringid"));
		setLearnedSkills(rs.getBoolean("learnedSkills"));
		setGrade(rs.getByte("grade"));
		setChuc(rs.getByte("chuc"));
		setOption1(rs.getShort("option1"));
		setOption2(rs.getShort("option2"));
		setOption3(rs.getShort("option3"));
		this.nDurability = rs.getInt("durability");
		this.dbFlag = 0;
	}

	@Override
	public Equip copy(){
		return copy(false);
	}

	@Override
	public Equip copy(boolean copySN){
		Equip ret = new Equip(id, position, quantity);
		ret.setPetId(ret.getPetId());
		ret.owner = owner;
		ret.expiration = expiration;
		ret.lockExpiration = lockExpiration;
		ret.giftFrom = giftFrom;
		ret.flag = flag;
		ret.perBundle = perBundle;
		ret.log = new LinkedList<>(log);
		ret.dbFlag = dbFlag;
		if(copySN) ret.nSN = nSN;
		//
		ret.str = str;
		ret.dex = dex;
		ret._int = _int;
		ret.luk = luk;
		ret.hp = hp;
		ret.mp = mp;
		ret.matk = matk;
		ret.mdef = mdef;
		ret.watk = watk;
		ret.wdef = wdef;
		ret.acc = acc;
		ret.avoid = avoid;
		ret.hands = hands;
		ret.speed = speed;
		ret.jump = jump;
		ret.flag = flag;
		ret.vicious = vicious;
		ret.upgradeSlots = upgradeSlots;
		ret.itemLevel = itemLevel;
		ret.itemExp = itemExp;
		ret.level = level;
		ret.learnedSkills = learnedSkills;
		ret.nDurability = nDurability;
		ret.chuc = chuc;
		ret.grade = grade;
		ret.option1 = option1;
		ret.option2 = option2;
		ret.option3 = option3;
		ret.ringid = ringid;
		return ret;
	}

	public void setStat(MapleStat stat, short amount){
		switch (stat){
			case INT:
				setInt(amount);
				break;
			case STR:
				setStr(amount);
				break;
			case LUK:
				setLuk(amount);
				break;
			case DEX:
				setDex(amount);
				break;
			case HP:
				setHp(amount);
				break;
			case MP:
				setMp(amount);
				break;
			default:
				Logger.log(LogType.INFO, LogFile.GENERAL_INFO, "invalid stat to set. use its setter instead.");
		}
	}

	public void setStat(MapleBuffStat stat, short amount){
		switch (stat){
			case WATK:
				setWatk(amount);
				break;
			case MATK:
				setMatk(amount);
				break;
			case ACC:
				setAcc(amount);
				break;
			case SPEED:
				setSpeed(amount);
				break;
			case JUMP:
				setJump(amount);
				break;
			case AVOID:
				setAvoid(amount);
				break;
			case WDEF:
				setWdef(amount);
				break;
			case MDEF:
				setMdef(amount);
				break;
			case HANDS:
				setHands(amount);
				break;
			default:
				Logger.log(LogType.INFO, LogFile.GENERAL_INFO, "invalid stat to set. use its setter instead.");
				return;
		}
	}

	public short getStat(MapleStat stat){
		switch (stat){
			case INT:
				return getInt();
			case STR:
				return getStr();
			case LUK:
				return getLuk();
			case DEX:
				return getDex();
			case HP:
				return getHp();
			case MP:
				return getMp();
			default:
				Logger.log(LogType.INFO, LogFile.GENERAL_INFO, "invalid stat to get. use its getter instead.");
		}
		return 0;
	}

	public short getStat(MapleBuffStat stat){
		switch (stat){
			case WATK:
				return getWatk();
			case MATK:
				return getMatk();
			case ACC:
				return getAcc();
			case SPEED:
				return getSpeed();
			case JUMP:
				return getJump();
			case AVOID:
				return getAvoid();
			case WDEF:
				return getWdef();
			case MDEF:
				return getMdef();
			default:
				Logger.log(LogType.INFO, LogFile.GENERAL_INFO, "invalid stat to get. use its getter instead.");
		}
		return 0;
	}

	@Override
	public byte getFlag(){
		return flag;
	}

	@Override
	public byte getType(){
		return 1;
	}

	public byte getUpgradeSlots(){
		return upgradeSlots;
	}

	public short getStr(){
		return str;
	}

	public short getDex(){
		return dex;
	}

	public short getInt(){
		return _int;
	}

	public short getLuk(){
		return luk;
	}

	public short getHp(){
		return hp;
	}

	public short getMp(){
		return mp;
	}

	public short getWatk(){
		return watk;
	}

	public short getMatk(){
		return matk;
	}

	public short getWdef(){
		return wdef;
	}

	public short getMdef(){
		return mdef;
	}

	public short getAcc(){
		return acc;
	}

	public short getAvoid(){
		return avoid;
	}

	public short getHands(){
		return hands;
	}

	public short getSpeed(){
		return speed;
	}

	public short getJump(){
		return jump;
	}

	public short getVicious(){
		return vicious;
	}

	public int getDurability(){
		return nDurability;
	}

	@Override
	public void setFlag(byte flag){
		this.flag = flag;
		addDBFlag(ItemDB.UPDATE);
	}

	public void setStr(short str){
		this.str = str;
		addDBFlag(ItemDB.UPDATE);
	}

	public void setDex(short dex){
		this.dex = dex;
		addDBFlag(ItemDB.UPDATE);
	}

	public void setInt(short _int){
		this._int = _int;
		addDBFlag(ItemDB.UPDATE);
	}

	public void setLuk(short luk){
		this.luk = luk;
		addDBFlag(ItemDB.UPDATE);
	}

	public void setHp(short hp){
		this.hp = hp;
		addDBFlag(ItemDB.UPDATE);
	}

	public void setMp(short mp){
		this.mp = mp;
		addDBFlag(ItemDB.UPDATE);
	}

	public void setWatk(short watk){
		this.watk = watk;
		addDBFlag(ItemDB.UPDATE);
	}

	public void setMatk(short matk){
		this.matk = matk;
		addDBFlag(ItemDB.UPDATE);
	}

	public void setWdef(short wdef){
		this.wdef = wdef;
		addDBFlag(ItemDB.UPDATE);
	}

	public void setMdef(short mdef){
		this.mdef = mdef;
		addDBFlag(ItemDB.UPDATE);
	}

	public void setAcc(short acc){
		this.acc = acc;
		addDBFlag(ItemDB.UPDATE);
	}

	public void setAvoid(short avoid){
		this.avoid = avoid;
		addDBFlag(ItemDB.UPDATE);
	}

	public void setHands(short hands){
		this.hands = hands;
		addDBFlag(ItemDB.UPDATE);
	}

	public void setSpeed(short speed){
		this.speed = speed;
		addDBFlag(ItemDB.UPDATE);
	}

	public void setJump(short jump){
		this.jump = jump;
		addDBFlag(ItemDB.UPDATE);
	}

	public void setVicious(short vicious){
		this.vicious = vicious;
		addDBFlag(ItemDB.UPDATE);
	}

	public void setUpgradeSlots(byte upgradeSlots){
		this.upgradeSlots = upgradeSlots;
		addDBFlag(ItemDB.UPDATE);
	}

	public byte getLevel(){
		return level;
	}

	public void setLevel(byte level){
		this.level = level;
		addDBFlag(ItemDB.UPDATE);
	}

	public void setDurability(int nDurability){
		this.nDurability = nDurability;
		addDBFlag(ItemDB.UPDATE);
	}

	public boolean needsRepair(){
		return nDurability != -1 && nDurability < 30000;
	}

	public void gainLevel(MapleClient c, boolean timeless){
		List<Pair<String, Integer>> stats = ItemInformationProvider.getInstance().getItemData(getItemId()).levelData.get("" + itemLevel).getLevelupStats();
		for(Pair<String, Integer> stat : stats){
			switch (stat.getLeft()){
				case "incDEX":
					dex += stat.getRight();
					break;
				case "incSTR":
					str += stat.getRight();
					break;
				case "incINT":
					_int += stat.getRight();
					break;
				case "incLUK":
					luk += stat.getRight();
					break;
				case "incMHP":
					hp += stat.getRight();
					break;
				case "incMMP":
					mp += stat.getRight();
					break;
				case "incPAD":
					watk += stat.getRight();
					break;
				case "incMAD":
					matk += stat.getRight();
					break;
				case "incPDD":
					wdef += stat.getRight();
					break;
				case "incMDD":
					mdef += stat.getRight();
					break;
				case "incEVA":
					avoid += stat.getRight();
					break;
				case "incACC":
					acc += stat.getRight();
					break;
				case "incSpeed":
					speed += stat.getRight();
					break;
				case "incJump":
					jump += stat.getRight();
					break;
			}
		}
		this.itemLevel++;
		c.announce(UserLocal.UserEffect.showEquipmentLevelUp());
		c.getPlayer().getMap().broadcastMessage(c.getPlayer(), UserRemote.UserEffect.showForeignEffect(c.getPlayer().getId(), 15));
		c.getPlayer().forceUpdateItem(this);
	}

	public float getItemExp(){
		return itemExp;
	}

	public void gainItemExp(MapleClient c, float gain, boolean timeless){
		ItemData data = ItemInformationProvider.getInstance().getItemData(getItemId());
		if(data != null && data.levelData != null && !data.levelData.isEmpty()){
			float expMultiplier = data.levelData.get("" + itemLevel).exp;
			if(expMultiplier != 0){
				expMultiplier /= 100F;
				float exp = gain;
				exp *= expMultiplier;
				itemExp += exp;
				int expNeeded = ExpTable.getExpNeededForLevel(getLevel() + 1);
				if(itemExp >= expNeeded){
					itemExp -= expNeeded;
					gainLevel(c, false);
					SkillData skillData = data.skillData.get((int) itemLevel);
					if(skillData != null){
						int rand = Randomizer.rand(0, 10);
						if(rand == data.probForLevelSkill - 1){
							learnedSkills = true;
							c.getPlayer().forceUpdateItem(this);
						}
					}
				}else c.getPlayer().forceUpdateItem(this);
			}
		}
	}

	public void setItemExp(float exp){
		this.itemExp = exp;
		addDBFlag(ItemDB.UPDATE);
	}

	public void setItemLevel(byte level){
		this.itemLevel = level;
		addDBFlag(ItemDB.UPDATE);
	}

	@Override
	public void setQuantity(short quantity){
		if(quantity < 0 || quantity > 1){ throw new RuntimeException("Setting the quantity to " + quantity + " on an equip (itemid: " + getItemId() + ")"); }
		super.setQuantity(quantity);
	}

	public void setUpgradeSlots(int i){
		this.upgradeSlots = (byte) i;
		addDBFlag(ItemDB.UPDATE);
	}

	public void setVicious(int i){
		this.vicious = (short) i;
		addDBFlag(ItemDB.UPDATE);
	}

	public int getRingId(){
		return ringid;
	}

	public void setRingId(int id){
		this.ringid = id;
		addDBFlag(ItemDB.UPDATE);
	}

	public boolean isWearing(){
		return wear;
	}

	public void wear(boolean yes){
		wear = yes;
	}

	public byte getItemLevel(){
		return itemLevel;
	}

	public boolean hasLearnedSkills(){
		return learnedSkills;
	}

	public void setLearnedSkills(boolean learnedSkills){
		this.learnedSkills = learnedSkills;
	}

	public byte getGrade(){
		return grade;
	}

	public void setGrade(byte grade){
		this.grade = grade;
		addDBFlag(ItemDB.UPDATE);
	}

	public byte getChuc(){
		return chuc;
	}

	public void setChuc(byte chuc){
		this.chuc = chuc;
		addDBFlag(ItemDB.UPDATE);
	}

	public short[] getOptionArray(){
		return new short[]{option1, option2, option3};
	}

	public short getOption1(){
		return option1;
	}

	public void setOption1(short option1){
		this.option1 = option1;
		addDBFlag(ItemDB.UPDATE);
	}

	public short getOption2(){
		return option2;
	}

	public void setOption2(short option2){
		this.option2 = option2;
		addDBFlag(ItemDB.UPDATE);
	}

	public short getOption3(){
		return option3;
	}

	public void setOption3(short option3){
		this.option3 = option3;
		addDBFlag(ItemDB.UPDATE);
	}

	public boolean handleGlass(Item glass){
		// glass level check
		int maxLevel = 30;
		if(glass.getItemId() == 2460001) maxLevel = 70;
		else if(glass.getItemId() == 2460002) maxLevel = 120;
		else if(glass.getItemId() == 2460003) maxLevel = 256;
		ItemInformationProvider ii = ItemInformationProvider.getInstance();
		ItemData data = ii.getItemData(this.getItemId());
		if(data.reqLevel > maxLevel){
			//
			return false;
		}
		if(grade == 0 || grade > 4){ return false; }
		if(option1 == 0 && option2 == 0 && option3 == 0){// brand new pot item
			option1 = (short) generateLine(grade);
		}else{
			boolean addNewLine = Randomizer.nextInt(100) + 1 <= 5;
			if(grade == 1 && Randomizer.nextInt(100) + 1 <= 10) grade++;
			else if(grade == 2 && Randomizer.nextInt(100) + 1 <= 1) grade++;
			int op1Grade = grade;
			int op2Grade = grade > 1 ? grade - 1 : grade;
			int op3Grade = grade > 1 ? grade - 1 : grade;
			if(grade == 2){// epic
				if(Randomizer.nextInt(100) + 1 <= 50) op2Grade = 2;
				if(Randomizer.nextInt(100) + 1 <= 25) op3Grade = 2;
			}else if(grade == 3){// unique
				if(Randomizer.nextInt(100) + 1 <= 50) op2Grade = 3;
				if(Randomizer.nextInt(100) + 1 <= 25) op3Grade = 3;
			}
			if(option1 != 0){
				option1 = (short) generateLine(op1Grade);
			}
			if(option2 != 0 || addNewLine){
				option2 = (short) generateLine(op2Grade);
				addNewLine = false;
			}
			if(option3 != 0 || addNewLine){
				option3 = (short) generateLine(op3Grade);
				addNewLine = false;
			}
		}
		grade += 4;
		addDBFlag(ItemDB.UPDATE);
		return true;
	}

	// Rare to epic = 10%
	// Epic to unique = 1%
	// Rare = 3 lines of Rare
	// Epic = 1 Line Guaranteed Epic, 2 lines = rare or epic
	// Unique = 1 Line of unique, 2 lines of epic/unique
	private int generateLine(int grade){
		ItemInformationProvider ii = ItemInformationProvider.getInstance();
		ItemData data = ii.getItemData(this.getItemId());
		int line = 0;
		int attempts = 0;
		int cycles = 0;
		List<Potential> pots = new ArrayList<>(ii.potentials.values());
		while(line == 0){
			Potential pot = pots.get(Randomizer.nextInt(pots.size()));
			if(data.reqLevel < pot.reqLevel) continue;
			if(!ItemConstants.isValidOptionType(pot, this.getItemId())) continue;
			// optionType check?
			// 1,2,3 is magnifying glass(To identify what grade after reveal), 4 prob a spacer for legendary
			// 5,6,7 is actual pot
			if(grade == 1 || grade == 5){// rare
				if(pot.id < 19999) line = pot.id;
			}else if(grade == 2 || grade == 6){// epic
				if(pot.id >= 901 && pot.id <= 905) line = pot.id;// face ones
				else if(pot.id >= 20000 && pot.id <= 29999) line = pot.id;
			}else if(grade == 3 || grade == 7){// unique
				if(pot.id >= 901 && pot.id <= 905) line = pot.id;// faces ones
				else if(pot.id >= 30000 && pot.id <= 39999) line = pot.id;
			}
			if(line != 0) break;
			if(++attempts >= ii.potentials.size()) cycles++;
			if(cycles >= 15){
				pots = null;
				return 901;// ?
			}
		}
		pots = null;
		return line;
	}

	/*@Override
	public String toString(){
		StringBuilder ret = new StringBuilder();
		try{
			for(Field field : this.getClass().getSuperclass().getDeclaredFields()){
				field.setAccessible(true);
				ret.append(field.getName() + ":");
				ret.append(field.get(this));
				ret.append(",");
			}
			for(Field field : this.getClass().getDeclaredFields()){
				field.setAccessible(true);
				ret.append(field.getName() + ":");
				ret.append(field.get(this));
				ret.append(",");
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		if(ret.toString().contains(",")) ret.setLength(ret.length() - 1);
		return ret.toString();
	}*/
	@Override
	public String toString(){
		return super.toString() + " Equip [upgradeSlots=" + upgradeSlots + ", level=" + level + ", flag=" + flag + ", itemLevel=" + itemLevel + ", str=" + str + ", dex=" + dex + ", _int=" + _int + ", luk=" + luk + ", hp=" + hp + ", mp=" + mp + ", watk=" + watk + ", matk=" + matk + ", wdef=" + wdef + ", mdef=" + mdef + ", acc=" + acc + ", avoid=" + avoid + ", hands=" + hands + ", speed=" + speed + ", jump=" + jump + ", vicious=" + vicious + ", nDurability=" + nDurability + ", itemExp=" + itemExp + ", ringid=" + ringid + ", wear=" + wear + ", learnedSkills=" + learnedSkills + ", grade=" + grade + ", chuc=" + chuc + ", option1=" + option1 + ", option2=" + option2 + ", option3=" + option3 + "]";
	}
}