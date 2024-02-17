package server.events.gm;

import java.rmi.RemoteException;
import java.util.concurrent.ScheduledFuture;

import client.MapleCharacter;
import net.channel.ChannelServer;
import server.TimerManager;
import tools.MaplePacketCreator;
import tools.StringUtil;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Feb 25, 2017
 */
public class Ola extends Event{

	private long start;
	private boolean started;
	private ScheduledFuture<?> startTimer;

	public Ola(int channel, int map){
		super(channel, map);
	}

	@Override
	public void start(){
		start = System.currentTimeMillis();
		try{
			ChannelServer.getInstance().getWorldInterface().broadcastPacket(MaplePacketCreator.serverNotice(0, "An Ola Ola Event has started. Go to Lith Harbor Channel " + getChannel() + " and talk to Tom to join."));
		}catch(RemoteException | NullPointerException ex){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
		}
		startTimer = TimerManager.getInstance().register("Ola", ()-> {
			if(start + (2 * 60 * 1000L) >= System.currentTimeMillis()){
				startTimer.cancel(true);
				startTimer = null;
				started = true;
				start = System.currentTimeMillis();
				try{
					ChannelServer.getInstance().getWorldInterface().broadcastPacket(MaplePacketCreator.serverNotice(0, "Registration for the Ola Ola Event has closed."));
				}catch(RemoteException | NullPointerException ex){
					Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
				}
				getMap().getPortal("join00").setPortalStatus(true);
			}else{
				try{
					ChannelServer.getInstance().getWorldInterface().broadcastPacket(MaplePacketCreator.serverNotice(0, "The Ola Ola Event in channel " + getChannel() + " will start in " + StringUtil.getReadableMillis(start, start + (2 * 60 * 1000L))));
				}catch(RemoteException | NullPointerException ex){
					Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
				}
			}
		}, 30 * 1000L, 30 * 1000L);
	}

	@Override
	public boolean enter(MapleCharacter chr){
		if(started) return false;// save original map
		chr.changeMap(getMap());
		return true;
	}

	public void finish(MapleCharacter chr){
		//
	}

	@Override
	public void end(){
		getMap().getPortal("join00").setPortalStatus(false);
	}
}
