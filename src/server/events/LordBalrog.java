/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.events;

import java.util.concurrent.ScheduledFuture;

import net.channel.ChannelServer;
import net.server.guild.MapleGuild;
import server.maps.MapleMap;

/**
 * @author Julien
 *         bottom reactor : 1058004, left : 1058001, right : 1058003
 */
public class LordBalrog{

	// private MapleGuild group;
	// private int minLevel = 50;
	// private int maxLevel = 200;
	private int mapToEnter = 105100300;
	// private int exitMap = 105100100;
	// private int minParticipants = 6;
	// private int maxParticipants = 15;
	// private int world;
	private int channel;
	private ScheduledFuture<?> healingSchedule;
	// private int[] balrogBody = {8830000, 8830001, 8830002};
	// private MapleMonster sleepingBalrog = MapleLifeFactory.getMonster(8830003);
	// private Point posToSpawn = new Point(412, 258);

	public LordBalrog(MapleGuild guild, int _world, int _channel){
		// group = guild;
		// world = _world;
		channel = _channel;
	}

	/*private void startQuest(){
		getLobbyInstance().broadcastMessage(MaplePacketCreator.serverNotice(5, "The portal to Lord Balrog's tomb is now open. Any member of '" + group.getName() + "' may enter."));
	}*/
	private MapleMap getMapInstance(){
		return ChannelServer.getInstance().getChannel(channel).getMap(mapToEnter);
	}
	/*private MapleMap getLobbyInstance(){
		return net.server.Server.getInstance().getChannel(world, channel).getMapFactory().getMap(exitMap);
	}
	
	private MapleMonster getBalrog(){
		return getMapInstance().getMonsterById(8830000);
	}*/

	/*private class SleepSchedule implements Runnable{
	
		@Override
		public void run(){
			getMapInstance().broadcastMessage(MaplePacketCreator.makeMonsterInvisible(getBalrog()));
			getMapInstance().spawnMonsterOnGroundBelow(sleepingBalrog, posToSpawn);
			int delta = (int) (2 * Math.random());
			int reactor = 1058004;
			switch (delta){
				case 0:
					reactor = 1058004;
					break;
				case 1:
					reactor = 1058001;
					break;
				case 2:
					reactor = 1058003;
					break;
				default:
					reactor = 1058004;
					break;
			}
			MapleReactor target = getMapInstance().getReactorById(reactor);
			target.setState(1);
			target.setAlive(true);
			getMapInstance().broadcastMessage(MaplePacketCreator.triggerReactor(target, 0)); // assuming stance = 0;..
			healingSchedule = TimerManager.getInstance().register(new startHealingSchedule(), 5000);
		}
	}
	
	private class startHealingSchedule implements Runnable{
	
		@Override
		public void run(){
			MapleMonster lordBalrog = getMapInstance().getMonsterById(8830000);
			lordBalrog.heal(30000, 30000);
		}
	}
	
	private class warpOut implements Runnable{
	
		@Override
		public void run(){
			for(MapleCharacter chr : getMapInstance().getCharacters()){
				chr.changeMap(exitMap);
				chr.dropMessage(5, "Better luck next time...");
			}
			net.server.Server.getInstance().broadcastMessage(MaplePacketCreator.serverNotice(0, "Despite their best efforts, " + group.getName() + " was unable to prevent Lord Balrog from reviving."));
			dispose();
		}
	}*/
	public void dispose(){
		if(healingSchedule != null){
			healingSchedule.cancel(true);
			healingSchedule = null;
		}
		getMapInstance().toggleDrops();
		getMapInstance().killAllMonsters();
		getMapInstance().toggleDrops();
	}

	public void stopHealingSchedule(){
		healingSchedule.cancel(true);
	}
}
