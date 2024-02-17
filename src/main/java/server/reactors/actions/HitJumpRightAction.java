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
public class HitJumpRightAction extends MapleReactorEvent{

	public int stateTo;

	public HitJumpRightAction(){
		super(ReactorActionType.HITJUMP_RIGHT);
	}

	public HitJumpRightAction(MapleData data){
		super(ReactorActionType.HITJUMP_RIGHT);
		processData(data);
	}

	public HitJumpRightAction(LittleEndianAccessor slea){
		super(ReactorActionType.HITJUMP_RIGHT);
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
		if(info.m_pfh != 0) return false;
		return type != ReactorHitType.ITEM_TRIGGER;
	}

	@Override
	public HitJumpRightAction clone(){
		HitJumpRightAction hitAction = new HitJumpRightAction();
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