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
import constants.skills.*;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class SkillEffectHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		int skillId = slea.readInt();
		int level = slea.readByte();
		byte flags = slea.readByte();
		int speed = slea.readByte();
		byte aids = slea.readByte();
		switch (skillId){
			case FPMage.EXPLOSION:
			case FPArchMage.BIG_BANG:
			case ILArchMage.BIG_BANG:
			case Bishop.BIG_BANG:
			case Bowmaster.HURRICANE:
			case Marksman.PIERCING_ARROW:
			case ChiefBandit.CHAKRA:
			case Brawler.CORKSCREW_BLOW:
			case Gunslinger.GRENADE:
			case Corsair.RAPID_FIRE:
			case WindArcher.HURRICANE:
			case NightWalker.POISON_BOMB:
			case ThunderBreaker.CORKSCREW_BLOW:
			case Paladin.MONSTER_MAGNET:
			case DarkKnight.MONSTER_MAGNET:
			case Hero.MONSTER_MAGNET:
			case Evan.FIRE_BREATH:
			case Evan.ICE_BREATH:
			case BladeMaster.MONSTER_BOMB:
			case BladeMaster.FINAL_CUT:
			case BladeMaster.MAPLE_WARRIOR:
				c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.skillEffect(c.getPlayer(), skillId, level, flags, speed, aids), false);
				return;
		}
	}
}