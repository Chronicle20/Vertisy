package server.reactors.actions;

import client.MapleClient;
import provider.MapleData;
import provider.MapleDataTool;
import scripting.reactor.ReactorScriptManager;
import server.reactors.MapleReactor;
import server.reactors.ReactorActionType;
import server.reactors.ReactorHitInfo;
import server.reactors.ReactorHitType;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jun 5, 2017
 */
public class HitRightAction extends MapleReactorEvent{

	public int stateTo;

	public HitRightAction(){
		super(ReactorActionType.HITRIGHT);
	}

	public HitRightAction(MapleData data){
		super(ReactorActionType.HITRIGHT);
		processData(data);
	}

	public HitRightAction(LittleEndianAccessor slea){
		super(ReactorActionType.HITRIGHT);
		load(slea);
	}

	@Override
	public void processData(MapleData data){
		stateTo = MapleDataTool.getInt(data.getChildByPath("state"));
	}

	@Override
	public void run(MapleClient c, MapleReactor reactor, ReactorHitInfo info){
		reactor.setState(stateTo);
		if(reactor.getStates().size() == stateTo + 1){
			ReactorScriptManager.getInstance().act(c, reactor);
			reactor.getMap().destroyReactor(reactor);
		}
	}

	@Override
	public boolean check(MapleClient c, MapleReactor reactor, ReactorHitType type, ReactorHitInfo info){
		if(info.bMoveAction != 0) return false;
		return type != ReactorHitType.ITEM_TRIGGER;
	}

	@Override
	public HitRightAction clone(){
		HitRightAction hitAction = new HitRightAction();
		hitAction.stateTo = stateTo;
		return hitAction;
	}

	@Override
	public void save(MaplePacketLittleEndianWriter mplew){
		mplew.writeInt(stateTo);
	}

	@Override
	public void load(LittleEndianAccessor slea){
		stateTo = slea.readInt();
	}
}