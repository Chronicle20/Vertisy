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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import client.*;
import constants.skills.DarkKnight;
import net.AbstractMaplePacketHandler;
import server.ItemInformationProvider;
import server.MapleStatEffect;
import server.maps.objects.MapleSummon;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.UserLocal;
import tools.packets.field.SummonedPool;
import tools.packets.field.userpool.UserRemote;

/**
 * @author Arnah, with info from Eric
 */
public final class BeholderHandler extends AbstractMaplePacketHandler{// Summon Skills noobs

	public class BeholderBuff{

		public static final int BUFF_PDD = 0x0, BUFF_MDD = 0x1, BUFF_ACC = 0x2, BUFF_EVA = 0x3, BUFF_PAD = 0x4;
	}

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		// System.out.println(slea.toString());
		Collection<MapleSummon> summons = c.getPlayer().getSummons().values();
		int oid = slea.readInt();
		MapleSummon summon = null;
		for(MapleSummon sum : summons){
			if(sum.getObjectId() == oid){
				summon = sum;
			}
		}
		if(summon != null){
			int skillId = slea.readInt();
			Skill bBuff = SkillFactory.getSkill(skillId);
			if(c.getPlayer().getSkillLevel(bBuff) > 0){// TODO: a check on beholder skill to see if its been x time since last execution
				if(skillId == DarkKnight.AURA_OF_BEHOLDER){
					System.out.println("Aura of beholder: " + slea.toString());
					short unk = slea.readShort();
					c.getPlayer().addHP(bBuff.getEffect(c.getPlayer().getSkillLevel(bBuff)).getHp());
					Logger.log(LogType.INFO, LogFile.GENERAL_INFO, "Aura of beholder, unk: " + unk + " hp from buff: " + bBuff.getEffect(c.getPlayer().getSkillLevel(bBuff)).getHp());
				}else if(skillId == DarkKnight.HEX_OF_BEHOLDER){
					byte attackAction = slea.readByte();
					byte buffType = slea.readByte();
					// System.out.println("A");
					int itemid = 2022125 + buffType;// gives a name/desc to the items.
					MapleStatEffect effect = bBuff.getEffect(c.getPlayer().getSkillLevel(bBuff));
					List<Pair<MapleBuffStat, BuffDataHolder>> stat = new ArrayList<>();
					switch (buffType){
						case BeholderBuff.BUFF_PDD:
							stat.add(new Pair<MapleBuffStat, BuffDataHolder>(MapleBuffStat.WDEF, new BuffDataHolder(itemid, c.getPlayer().getSkillLevel(bBuff), effect.getWdef())));
							break;
						case BeholderBuff.BUFF_MDD:
							stat.add(new Pair<MapleBuffStat, BuffDataHolder>(MapleBuffStat.MDEF, new BuffDataHolder(itemid, c.getPlayer().getSkillLevel(bBuff), effect.getMdef())));
							break;
						case BeholderBuff.BUFF_ACC:
							stat.add(new Pair<MapleBuffStat, BuffDataHolder>(MapleBuffStat.ACC, new BuffDataHolder(itemid, c.getPlayer().getSkillLevel(bBuff), effect.getAcc())));
							break;
						case BeholderBuff.BUFF_EVA:
							stat.add(new Pair<MapleBuffStat, BuffDataHolder>(MapleBuffStat.AVOID, new BuffDataHolder(itemid, c.getPlayer().getSkillLevel(bBuff), effect.getAvoid())));
							break;
						case BeholderBuff.BUFF_PAD:
							stat.add(new Pair<MapleBuffStat, BuffDataHolder>(MapleBuffStat.WATK, new BuffDataHolder(itemid, c.getPlayer().getSkillLevel(bBuff), effect.getWatk())));
							break;
					}
					if(stat != null && !stat.isEmpty()){
						ItemInformationProvider ii = ItemInformationProvider.getInstance();
						ii.getItemData(itemid).itemEffect.applyTo(c.getPlayer(), c.getPlayer(), false, null, effect.getDuration(), true);
						c.announce(MaplePacketCreator.giveBuff(c.getPlayer(), -itemid, effect.getDuration(), stat));// does cancelling work?
						// effect.applyTo(c.getPlayer());// is this needed?
						c.getPlayer().getMap().broadcastMessage(c.getPlayer(), SummonedPool.summonSkill(c.getPlayer().getId(), skillId, attackAction), true);
						c.announce(UserLocal.UserEffect.showOwnBuffEffect(skillId, 2));// shows effect to you & 3rd party
						c.getPlayer().getMap().broadcastMessage(c.getPlayer(), UserRemote.UserEffect.showBuffeffect(c.getPlayer().getId(), skillId, 2), false);
					}
				}
			}
		}else{
			c.getPlayer().getSummons().clear();
		}
	}
}
