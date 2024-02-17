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
 * @author Tyler
 */
public class HitAction extends MapleReactorEvent{

	public int stateTo;

	public HitAction(){
		super(ReactorActionType.HIT);
	}

	public HitAction(MapleData data){
		super(ReactorActionType.HIT);
		processData(data);
	}

	public HitAction(LittleEndianAccessor slea){
		super(ReactorActionType.HIT);
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
		return type != ReactorHitType.ITEM_TRIGGER;
	}

	@Override
	public HitAction clone(){
		HitAction hitAction = new HitAction();
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