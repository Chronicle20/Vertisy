/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.events;

import java.awt.Point;
import java.util.concurrent.ScheduledFuture;

import client.MapleCharacter;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import server.TimerManager;
import server.life.MapleLifeFactory;
import server.maps.MapleMap;
import tools.MaplePacketCreator;

/**
 * @author Julien
 */
public class DemonDoor{

	private MapleMap instance;
	private MapleParty group;
	private QuickInstanceType qi_type = QuickInstanceType.DemonDoor_Marbas;
	private ScheduledFuture<?> warpOutInstance;

	public DemonDoor(MapleCharacter leader, int type){
		if(leader.isInParty()){ // is in a party
			if(leader.isPartyLeader()){
				// is Party leader
				group = leader.getParty();
			}else{
				// why the fuck is he here then
				return;
			}
		}
		qi_type = QuickInstanceType.getById(type);
		instance = leader.getClient().getChannelServer().getMap(qi_type.getMapId(), true);
	}

	public void warpParty(){
		for(MaplePartyCharacter mpc : group.getMembers()){
			MapleCharacter chr = mpc.getPlayerInChannel();
			if(chr != null){
				chr.changeMap(instance);
				chr.announce(MaplePacketCreator.earnTitleMessage("Brace yourselves! It is coming..."));
				chr.announce(MaplePacketCreator.getClock(10));
			}
			TimerManager.getInstance().schedule("demondoor-start", new start(), 10 * 1000);
			warpOutInstance = TimerManager.getInstance().schedule("demondoor-warpout", new warpOut(), 1000 * 60 * 20);
		}
	}

	public class warpOut implements Runnable{

		@Override
		public void run(){
			dispose(false);
		}
	}

	public void dispose(boolean force){
		if(force){
			this.warpOutInstance.cancel(false);
			for(MaplePartyCharacter mpc : group.getMembers()){
				MapleCharacter chr = mpc.getPlayerInChannel();
				if(chr != null){
					chr.announce(MaplePacketCreator.removeClock());
				}
			}
		}
		instance = null;
		qi_type = null;
	}

	public MapleMap getMapInstance(){
		return instance;
	}

	public class start implements Runnable{

		@Override
		public void run(){
			for(MaplePartyCharacter mpc : group.getMembers()){
				MapleCharacter chr = mpc.getPlayerInChannel();
				if(chr != null){
					chr.announce(MaplePacketCreator.getClock((1000 * 60 * 20) - 1000 * 10));
				}
			}
			Point spawnPoint;
			switch (qi_type.getId()){
				case 1:
					spawnPoint = new Point(512, 60);
					break;
				case 2:
					spawnPoint = new Point(355, 66);
					break;
				case 3:
					spawnPoint = new Point(563, 35);
					break;
				case 4:
					spawnPoint = new Point(171, 73);
					break;
				case 5:
					spawnPoint = new Point(275, 96);
					break;
				default:
					spawnPoint = new Point(0, 0);
					System.out.println("[ERROR] Unhandled type in DemonDoor.java.");
					break;
			}
			// This is where we spawn the boss!
			getMapInstance().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(qi_type.getMobId()), spawnPoint);
		}
	}
}
