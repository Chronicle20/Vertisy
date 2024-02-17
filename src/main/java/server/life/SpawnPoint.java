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

import java.awt.Point;
import java.util.concurrent.atomic.AtomicInteger;

import tools.data.input.LittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;

public class SpawnPoint{

	private int monster, mobTime, team, fh, f;
	private Point pos;
	private long nextPossibleSpawn;
	private int mobInterval = 5000;
	private AtomicInteger spawnedMonsters = new AtomicInteger(0);
	private boolean immobile;

	public SpawnPoint(){
		super();
	}

	public SpawnPoint(final MapleMonster monster, Point pos, boolean immobile, int mobTime, int mobInterval, int team){
		this.monster = monster.getId();
		this.pos = new Point(pos);
		this.mobTime = mobTime;
		this.team = team;
		this.fh = monster.getFh();
		this.f = monster.getF();
		this.immobile = immobile;
		this.mobInterval = mobInterval;
		this.nextPossibleSpawn = System.currentTimeMillis();
	}

	public boolean shouldSpawn(){
		double cap = 2;
		if(mobTime < 0 || ((mobTime != 0 || immobile) && spawnedMonsters.get() > 0) || spawnedMonsters.get() > Math.ceil(cap)){// lol
			return false;
		}
		return nextPossibleSpawn <= System.currentTimeMillis();
	}

	public boolean shouldForceSpawn(boolean horde){
		double cap = 2;
		if(horde) cap += 2;
		if(mobTime < 0 || ((mobTime != 0 || immobile) && spawnedMonsters.get() > 0) || spawnedMonsters.get() > Math.ceil(cap)){// lol
			return false;
		}
		return true;
	}

	public MapleMonster getMonster(){
		MapleMonster mob = new MapleMonster(MapleLifeFactory.getMonster(monster));
		mob.setPosition(new Point(pos));
		mob.setTeam(team);
		mob.setFh(fh);
		mob.setStartFh(fh);
		mob.setF(f);
		mob.setSpawnPosition(new Point(pos));
		spawnedMonsters.incrementAndGet();
		mob.addListener(new MonsterListener(){

			@Override
			public void monsterKilled(int aniTime){
				nextPossibleSpawn = System.currentTimeMillis();
				if(mobTime > 0){
					nextPossibleSpawn += mobTime * 1000;
				}else{
					nextPossibleSpawn += aniTime;
				}
				spawnedMonsters.decrementAndGet();
			}
		});
		if(mobTime == 0){
			nextPossibleSpawn = System.currentTimeMillis() + mobInterval;
		}
		return mob;
	}

	public MapleMonster getFakeMonster(){
		MapleMonster fake = new MapleMonster(MapleLifeFactory.getMonster(monster));
		fake.setTeam(team);
		fake.setFh(fh);
		fake.setStartFh(fh);
		fake.setF(f);
		fake.setPosition((Point) this.getPosition().clone());
		fake.setSpawnPosition((Point) this.getPosition().clone());
		return fake;
	}

	public int getMonsterId(){
		return monster;
	}

	public void setMonster(int mobid){
		this.monster = mobid;
	}

	public Point getPosition(){
		return pos;
	}

	public final int getF(){
		return f;
	}

	public final int getFh(){
		return fh;
	}

	public final int getTeam(){
		return team;
	}

	public final int getMobTime(){
		return mobTime;
	}

	public void cleanup(){
		spawnedMonsters.set(0);
		nextPossibleSpawn = System.currentTimeMillis();
	}

	public long getNextPossibleSpawn(){
		return nextPossibleSpawn;
	}

	public void setNextPossibleSpawn(long nextPossibleSpawn){
		this.nextPossibleSpawn = nextPossibleSpawn;
	}

	@Override
	public SpawnPoint clone(){
		return new SpawnPoint(getFakeMonster(), pos, immobile, mobTime, mobInterval, team);
	}

	public void save(MaplePacketLittleEndianWriter mplew){
		mplew.writeInt(monster);
		mplew.writeInt(mobTime);
		mplew.writeInt(team);
		mplew.writeInt(fh);
		mplew.writeInt(f);
		mplew.writePos(pos);
		mplew.writeInt(mobInterval);
		mplew.writeBoolean(immobile);
	}

	public void load(LittleEndianAccessor slea){
		monster = slea.readInt();
		mobTime = slea.readInt();
		team = slea.readInt();
		fh = slea.readInt();
		f = slea.readInt();
		pos = slea.readPos();
		mobInterval = slea.readInt();
		immobile = slea.readBoolean();
	}
}
