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

import client.MapleClient;
import server.life.MapleMonster;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import server.maps.objects.MapleMapObject;
import server.maps.objects.MapleMapObjectType;
import server.movement.Elem;
import server.movement.MovePath;
import tools.Pair;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.field.MobPool;

public final class MoveLifeHandler extends AbstractMovementPacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		int objectid = slea.readInt();
		short moveid = slea.readShort();// bMoveAction
		if(c.getPlayer() == null || c.getPlayer().getMap() == null) return;// somehow
		MapleMapObject mmo = c.getPlayer().getMap().getMapObject(objectid);
		if(mmo == null || mmo.getType() == null || mmo.getType() != MapleMapObjectType.MONSTER) return;
		MapleMonster monster = (MapleMonster) mmo;
		byte skillByte = slea.readByte();// nextAttackPossible
		byte skill = slea.readByte();// action
		// data(int)
		int skill_1 = slea.readByte() & 0xFF;
		byte skill_2 = slea.readByte();
		byte skill_3 = slea.readByte();
		byte skill_4 = slea.readByte();
		int size = slea.readInt();// ?
		for(int i = 0; i < size; i++){
			slea.readInt();
			slea.readInt();
		}
		size = slea.readInt();// ?
		for(int i = 0; i < size; i++){
			slea.readInt();
		}
		slea.readByte();
		slea.readInt();
		slea.readInt();
		slea.readInt();
		slea.readInt();
		MobSkill toUse = null;
		if(skillByte == 1 && monster.getNoSkills() > 0){
			int random = Randomizer.nextInt(monster.getNoSkills());
			Pair<Integer, Integer> skillToUse = monster.getSkills().get(random);
			toUse = MobSkillFactory.getMobSkill(skillToUse.getLeft(), skillToUse.getRight());
			int percHpLeft = (int) (((double) monster.getHp() / monster.getMaxHp()) * 100);
			if(toUse.getHP() < percHpLeft || !monster.canUseSkill(toUse)){
				toUse = null;
			}
		}
		if((skill_1 >= 100 && skill_1 <= 200) && monster.hasSkill(skill_1, skill_2)){
			MobSkill skillData = MobSkillFactory.getMobSkill(skill_1, skill_2);
			if(skillData != null && monster.canUseSkill(skillData)){
				skillData.applyEffect(c.getPlayer(), monster, true);
			}
		}
		monster.getPosition();
		MovePath res = new MovePath();
		res.decode(slea);
		if(monster.getController() != null && monster.getController().getId() != c.getPlayer().getId()){
			if(monster.isAttackedBy(c.getPlayer())){// aggro and controller change
				monster.switchController(c.getPlayer(), true);
			}else{
				return;
			}
		}else if(skill == -1 && monster.isControllerKnowsAboutAggro() && !monster.isMobile() && !monster.isFirstAttack()){
			monster.setControllerHasAggro(false);
			monster.setControllerKnowsAboutAggro(false);
		}
		boolean aggro = monster.isControllerHasAggro();
		if(toUse != null){
			c.announce(MobPool.moveMonsterResponse(objectid, moveid, monster.getMp(), aggro, toUse.getSkillId(), toUse.getSkillLevel()));
		}else{
			c.announce(MobPool.moveMonsterResponse(objectid, moveid, monster.getMp(), aggro));
		}
		if(aggro){
			monster.setControllerKnowsAboutAggro(true);
		}
		if(res != null){
			updatePosition(res, monster, -1);
			c.getPlayer().getMap().moveMonster(monster, monster.getPosition());
			if(c.getPlayer().bMoveAction != -1){
				for(Elem elem : res.lElem){
					elem.bMoveAction = c.getPlayer().bMoveAction;
				}
			}
			c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MobPool.moveMonster(skillByte, skill, skill_1, skill_2, skill_3, skill_4, objectid, res), monster.getPosition());
		}
	}
}
