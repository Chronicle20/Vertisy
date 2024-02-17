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
package server.events.gm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import client.MapleCharacter;
import server.TimerManager;
import server.maps.objects.MapleMapObject;
import tools.MaplePacketCreator;
import tools.Pair;

/**
 * @author iPoopMagic (David)
 */
public class MapleFindThatJewel{

	private final MapleCharacter chr;
	private long time = 0;
	private long timeStarted = 0;
	private ScheduledFuture<?> schedule = null;
	private List<Pair<String, Integer>> compilation = new ArrayList<>();

	public MapleFindThatJewel(final MapleCharacter chr){
		this.chr = chr;
		this.schedule = TimerManager.getInstance().schedule("findthatjewel", ()-> {
			if(chr.getMapId() >= 109010000 && chr.getMapId() <= 109010400){
				chr.changeMap(chr.getMap().getReturnMap());
			}
			resetTimes();
		}, 10 * 60 * 1000); // 10 minutes?
	}

	public void startJewel(){
		compilation.clear();
		chr.getMap().startEvent();
		chr.getClient().getChannelServer().getMap(109010100).startEvent();
		chr.getClient().getChannelServer().getMap(109010200).startEvent();
		chr.getClient().announce(MaplePacketCreator.getClock(10 * 60)); // 10 minutes
		this.timeStarted = System.currentTimeMillis();
		this.time = 10 * 60 * 1000; // 10 minutes
		chr.getMap().getPortal("join00").setPortalStatus(true);
		chr.getClient().announce(MaplePacketCreator.serverNotice(0, "The portal has now opened. Press the up arrow key at the portal to enter."));
		TimerManager.getInstance().schedule("startjewel1", ()-> {
			chr.getClient().announce(MaplePacketCreator.serverNotice(0, "There are 5 minutes remaining in the event!"));
		}, 5 * 60 * 1000); // 10 minutes?
		TimerManager.getInstance().schedule("startjewel2", ()-> {
			chr.getClient().announce(MaplePacketCreator.serverNotice(0, "There is 1 minute remaining in the event!"));
		}, 9 * 60 * 1000); // 10 minutes?
	}

	public boolean isTimerStarted(){
		return time > 0 && timeStarted > 0;
	}

	public long getTime(){
		return time;
	}

	private void resetTimes(){
		this.time = 0;
		this.timeStarted = 0;
		schedule.cancel(false);
	}

	public long getTimeLeft(){
		return time - (System.currentTimeMillis() - timeStarted);
	}

	public int getCompilationAmount(){
		for(Pair<String, Integer> p : compilation){
			if(p.getLeft() == chr.getName()) return p.getRight();
		}
		return 0;
	}

	public List<Pair<String, Integer>> getCompilation(){
		return compilation;
	}

	public void addCompilation(String name, int quantity){
		compilation.add(new Pair<>(name, quantity));
	}

	public String displayCompilations(){
		String text = "And here are the results thus far!#b";
		for(MapleMapObject mmo : chr.getMap().getAllPlayer()){
			MapleCharacter player = (MapleCharacter) mmo;
			for(Pair<String, Integer> p : player.getJewel().getCompilation()){
				text += "\r\n" + p.getLeft() + ": " + p.getRight() + " Jewels";
			}
		}
		return text;
	}
}
