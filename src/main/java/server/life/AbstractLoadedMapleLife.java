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
package server.life;

import server.maps.objects.AbstractAnimatedMapleMapObject;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;

public abstract class AbstractLoadedMapleLife extends AbstractAnimatedMapleMapObject{

	private int id;
	private int f;
	private boolean hide;
	private int fh;
	private int start_fh;
	private int cy;
	private int rx0;
	private int rx1;

	public AbstractLoadedMapleLife(int id){
		this.id = id;
	}

	public AbstractLoadedMapleLife(){
		super();
	}

	public AbstractLoadedMapleLife(AbstractLoadedMapleLife life){
		this(life.getId());
		this.f = life.f;
		this.hide = life.hide;
		this.fh = life.fh;
		this.start_fh = life.fh;
		this.cy = life.cy;
		this.rx0 = life.rx0;
		this.rx1 = life.rx1;
	}

	@Override
	public void save(MaplePacketLittleEndianWriter mplew){
		super.save(mplew);
		mplew.writeInt(id);
		mplew.writeInt(f);
		mplew.writeBoolean(hide);
		mplew.writeInt(fh);
		mplew.writeInt(start_fh);
		mplew.writeInt(cy);
		mplew.writeInt(rx0);
		mplew.writeInt(rx1);
	}

	@Override
	public void load(LittleEndianAccessor slea){
		super.load(slea);
		id = slea.readInt();
		f = slea.readInt();
		hide = slea.readBoolean();
		fh = slea.readInt();
		start_fh = slea.readInt();
		cy = slea.readInt();
		rx0 = slea.readInt();
		rx1 = slea.readInt();
	}

	public int getF(){
		return f;
	}

	public void setF(int f){
		this.f = f;
	}

	public boolean isHidden(){
		return hide;
	}

	public void setHide(boolean hide){
		this.hide = hide;
	}

	public int getFh(){
		return fh;
	}

	public void setFh(int fh){
		this.fh = fh;
	}

	public int getStartFh(){
		return start_fh;
	}

	public void setStartFh(int start_fh){
		this.start_fh = start_fh;
	}

	public int getCy(){
		return cy;
	}

	public void setCy(int cy){
		this.cy = cy;
	}

	public int getRx0(){
		return rx0;
	}

	public void setRx0(int rx0){
		this.rx0 = rx0;
	}

	public int getRx1(){
		return rx1;
	}

	public void setRx1(int rx1){
		this.rx1 = rx1;
	}

	public int getId(){
		return id;
	}

	public void setId(int id){
		this.id = id;
	}

	@Override
	public abstract AbstractLoadedMapleLife clone();
}
