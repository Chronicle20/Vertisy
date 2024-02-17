package server.reactors.actions;

import java.util.concurrent.ScheduledFuture;

import client.MapleClient;
import provider.MapleData;
import provider.MapleDataTool;
import server.TimerManager;
import server.reactors.MapleReactor;
import server.reactors.ReactorActionType;
import server.reactors.ReactorHitInfo;
import server.reactors.ReactorHitType;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 * @author Arnah
 */
public class TimeOutAction extends MapleReactorEvent{

	private int stateTo, timeout;
	private long lastRun = -1;
	private ScheduledFuture<?> checkIn;

	public TimeOutAction(){
		super(ReactorActionType.TIME_OUT);
	}

	public TimeOutAction(MapleData data){
		super(ReactorActionType.TIME_OUT);
		processData(data);
	}

	public TimeOutAction(LittleEndianAccessor slea){
		super(ReactorActionType.TIME_OUT);
		load(slea);
	}

	@Override
	public void processData(MapleData data){
		stateTo = MapleDataTool.getIntConvert(data.getChildByPath("state"));
	}

	@Override
	public void run(MapleClient c, MapleReactor reactor, ReactorHitInfo info){
		if(reactor.getCurrState() != stateTo){
			if(lastRun == -1 || (System.currentTimeMillis() - lastRun >= timeout)){
				// System.out.println(System.currentTimeMillis());
				lastRun = System.currentTimeMillis();
				// System.out.println("TimeOutAction-run");
				reactor.setState(stateTo);
				if(reactor.canSetAlive() && !reactor.isAlive()) reactor.setAlive(true);
				if(reactor != null && reactor.getMap() != null && reactor.getState() != null) reactor.getMap().activateItemReactors(c);
			}else{
				if(checkIn == null && reactor.getTimeoutHit() == null){
					long runIn = ((lastRun + timeout) - System.currentTimeMillis());
					// System.out.println("runIn: " + runIn);
					if(runIn >= 0){
						// System.out.println("Checking in: " + ((lastRun + timeout) - System.currentTimeMillis()));
						checkIn = TimerManager.getInstance().schedule("timeout-run", ()-> {
							checkIn = null;
							run(c, reactor, info);
						}, runIn);
					}else reactor.checkForTimeout();
				}
			}
		}
	}

	@Override
	public boolean check(MapleClient c, MapleReactor reactor, ReactorHitType type, ReactorHitInfo info){
		return lastRun == -1 || (System.currentTimeMillis() - lastRun >= timeout);
	}

	public void setTimeout(int timeout){
		this.timeout = timeout;
	}

	public int getTimeout(){
		return timeout;
	}

	@Override
	public TimeOutAction clone(){
		TimeOutAction action = new TimeOutAction();
		action.timeout = timeout;
		action.stateTo = stateTo;
		return action;
	}

	@Override
	public void save(MaplePacketLittleEndianWriter mplew){
		mplew.writeInt(timeout);
		mplew.writeInt(stateTo);
	}

	@Override
	public void load(LittleEndianAccessor slea){
		timeout = slea.readInt();
		stateTo = slea.readInt();
	}
}