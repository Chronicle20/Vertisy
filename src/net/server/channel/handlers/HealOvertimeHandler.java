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
package net.server.channel.handlers;

import client.*;
import client.autoban.AutobanFactory;
import client.autoban.AutobanManager;
import client.autoban.AutobanManager.UpdateType;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.EquipSlot;
import net.AbstractMaplePacketHandler;
import server.ItemData;
import server.ItemInformationProvider;
import tools.data.input.SeekableLittleEndianAccessor;

public final class HealOvertimeHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		MapleCharacter chr = c.getPlayer();
		AutobanManager abm = chr.getAutobanManager();
		int timestamp = slea.readInt();// TODO: Readd
		abm.setTimestamp(0, timestamp, 3);
		int flag = slea.readInt();
		int nHP = 0, nMP = 0;
		if((flag & MapleStat.HP.getValue()) > 0) nHP = slea.readShort();
		if((flag & MapleStat.MP.getValue()) > 0) nMP = slea.readShort();
		int nOption = slea.readByte();
		double recovery = c.getPlayer().getMap().getMapData().getRecovery();
		if((nOption & 0x2) > 0){
			recovery *= 1.5D;
		}
		Item item = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short) EquipSlot.OVERALL.getSlots()[0]);
		if(item != null){
			if(recovery > 1.0D){
				ItemData data = ItemInformationProvider.getInstance().getItemData(item.getItemId());
				if(data.recovery != 1){
					recovery *= data.recovery;
				}
			}
		}
		long time = System.currentTimeMillis();
		if(nHP > 0){
			long hpDuration = 10000;
			if((nOption & 1) > 0){
				hpDuration = getEndureDuration(chr);
			}
			if((time - abm.getLastUpdate(UpdateType.CHARACTER_HP_INC)) < hpDuration - 2000){
				abm.increaseIllegalUpdateType(UpdateType.CHARACTER_HP_INC);
				return;
			}
			double hp = ((getHPRecoveryUpgrade(chr) + 10) * recovery);
			// System.out.println("HP: " + hp);
			if(chr.getChair() > 100){
				ItemData chair = ItemInformationProvider.getInstance().getItemData(chr.getChair());
				hp += chair.recoveryHP;
			}
			// System.out.println("hp Calculated: " + hp + " given: " + nHP);
			if(hp < nHP){
				AutobanFactory.HIGH_HP_HEALING.alert(chr, "HP: " + nHP + "; Max is " + hp + ".");
				abm.increaseIllegalUpdateType(UpdateType.HP_INC_SIZE);
				return;
			}
			chr.addHP((int) Math.round(hp));
		}
		if(nMP > 0){
			if((time - abm.getLastUpdate(UpdateType.CHARACTER_MP_INC)) < 8000){
				abm.increaseIllegalUpdateType(UpdateType.CHARACTER_MP_INC);
				return;
			}
			double mp = ((getMPRecoveryUpgrade(chr) + 5) * recovery);
			if(chr.getChair() > 100){
				ItemData chair = ItemInformationProvider.getInstance().getItemData(chr.getChair());
				mp += chair.recoveryMP;
			}
			// System.out.println("mp Calculated: " + mp + " given: " + nMP);
			if(mp < nMP){
				AutobanFactory.HIGH_MP_HEALING.alert(chr, "MP: " + nMP + "; Max is " + mp + ".");
				abm.increaseIllegalUpdateType(UpdateType.MP_INC_SIZE);
				return;
			}
			chr.addMP((int) Math.round(mp));
		}
		/*System.out.println("option: " + nOption);
		boolean endure = ((nOption & 0x1) > 0);
		double recoveryRate = ((nOption & 0x2) > 0) ? 1.5D : 1D;
		if(nHP > 0){
			if((abm.getLastSpam(0) + 1500) > timestamp) AutobanFactory.FAST_HP_HEALING.addPoint(abm, "Fast hp healing");
			int maxHP = (int) Math.round(chr.getMaxHPRecovery(endure) * recoveryRate);
			System.out.println("Healed: " + nHP + " max: " + maxHP);
			if(nHP > maxHP){
				AutobanFactory.HIGH_HP_HEALING.alert(chr, "Healing: " + nHP + "; Max is " + maxHP + ".");
				return;
			}
			chr.addHP(nHP);
			chr.healMKDummy(nHP);
			// chr.getMap().broadcastMessage(chr, MaplePacketCreator.showHpHealed(chr.getId(), healHP), false);
			abm.spam(0, timestamp);
		}
		if(nMP > 0){
			if((abm.getLastSpam(1) + 1500) > timestamp) AutobanFactory.FAST_MP_HEALING.addPoint(abm, "Fast mp healing");
			int maxMP = (int) Math.round(chr.getMaxMPRecovery(endure) * recoveryRate);
			System.out.println("MP Gained: " + nMP + " max: " + maxMP);
			if(nMP > maxMP){
				AutobanFactory.HIGH_MP_HEALING.alert(chr, "Healing: " + nMP + "; Max is " + maxMP + ".");
				return;
			}
			chr.addMP(nMP);
			abm.spam(1, timestamp);
		}*/
	}

	public long getEndureDuration(MapleCharacter chr){
		Skill skill = SkillFactory.getSkill(4100002);// thief
		int skillLevel = chr.getSkillLevel(skill);
		if(skill != null && skillLevel > 0) return skill.getEffect(skillLevel).getDuration();
		skill = SkillFactory.getSkill(4200001);
		skillLevel = chr.getSkillLevel(skill);
		if(skill != null && skillLevel > 0) return skill.getEffect(skillLevel).getDuration();
		skill = SkillFactory.getSkill(4310000);// endure(dual blade)
		skillLevel = chr.getSkillLevel(skill);
		if(skill != null && skillLevel > 0) return skill.getEffect(skillLevel).getDuration();
		return 0;
	}

	public int getHPRecoveryUpgrade(MapleCharacter chr){
		Skill skill = SkillFactory.getSkill(1000000);// warrior
		int skillLevel = chr.getSkillLevel(skill);
		if(skill != null && skillLevel > 0) return skill.getEffect(skillLevel).getHp();
		skill = SkillFactory.getSkill(4100002);// thief
		skillLevel = chr.getSkillLevel(skill);
		if(skill != null && skillLevel > 0) return skill.getEffect(skillLevel).getHp();
		skill = SkillFactory.getSkill(4200001);
		skillLevel = chr.getSkillLevel(skill);
		if(skill != null && skillLevel > 0) return skill.getEffect(skillLevel).getHp();
		skill = SkillFactory.getSkill(4310000);// endure(dual blade)
		skillLevel = chr.getSkillLevel(skill);
		if(skill != null && skillLevel > 0) return skill.getEffect(skillLevel).getHp();
		return 0;
	}

	public int getMPRecoveryUpgrade(MapleCharacter chr){
		if(!chr.getJob().isMagician()){
			Skill skill = SkillFactory.getSkill(1110000);
			int skillLevel = chr.getSkillLevel(skill);
			if(skill != null && skillLevel > 0) return skill.getEffect(skillLevel).getMp();
			skill = SkillFactory.getSkill(4100002);//
			skillLevel = chr.getSkillLevel(skill);
			if(skill != null && skillLevel > 0) return skill.getEffect(skillLevel).getMp();
			skill = SkillFactory.getSkill(4200001);
			skillLevel = chr.getSkillLevel(skill);
			if(skill != null && skillLevel > 0) return skill.getEffect(skillLevel).getMp();
			skill = SkillFactory.getSkill(1210000);//
			skillLevel = chr.getSkillLevel(skill);
			if(skill != null && skillLevel > 0) return skill.getEffect(skillLevel).getMp();
			return 0;
		}
		Skill skill = SkillFactory.getSkill(2000000);
		double skillLevel = chr.getSkillLevel(skill);
		if(skillLevel > 0){ return (int) Math.round(skillLevel * chr.getLevel() * 0.1); }
		return 0;
	}
}
