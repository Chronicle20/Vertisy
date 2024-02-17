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
package server.maps.objects;

import tools.data.input.LittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;

public abstract class AbstractAnimatedMapleMapObject extends AbstractMapleMapObject implements AnimatedMapleMapObject{

	private int stance;

	@Override
	public int getStance(){
		return stance;
	}

	@Override
	public void setStance(int stance){
		this.stance = stance;
	}

	@Override
	public boolean isFacingLeft(){
		return Math.abs(stance) % 2 == 1;
	}

	@Override
	public abstract AbstractAnimatedMapleMapObject clone();

	@Override
	public void save(MaplePacketLittleEndianWriter mplew){
		super.save(mplew);
		mplew.writeInt(stance);
	}

	@Override
	public void load(LittleEndianAccessor slea){
		super.load(slea);
		stance = slea.readInt();
	}
}
