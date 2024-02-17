package server.reactors.actions;

import client.MapleClient;
import provider.MapleData;
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
public class BreakAction extends MapleReactorEvent{

	public BreakAction(MapleData data){
		super(ReactorActionType.BREAK);
		processData(data);
	}

	public BreakAction(LittleEndianAccessor slea){
		super(ReactorActionType.BREAK);
		load(slea);
	}

	public BreakAction(){
		super(ReactorActionType.BREAK);
	}

	@Override
	public void processData(MapleData data){}

	@Override
	public void run(MapleClient c, MapleReactor reactor, ReactorHitInfo info){
		if(reactor.getStates().size() - 1 > reactor.getCurrState()){
			reactor.setState(reactor.getCurrState() + 1);
		}else{
			ReactorScriptManager.getInstance().act(c, reactor);
			reactor.getMap().destroyReactor(reactor);
		}
	}

	@Override
	public boolean check(MapleClient c, MapleReactor reactor, ReactorHitType type, ReactorHitInfo info){
		if(type == ReactorHitType.ITEM_TRIGGER) return false;
		return true;
	}

	@Override
	public BreakAction clone(){
		return new BreakAction();
	}

	@Override
	public void save(MaplePacketLittleEndianWriter mplew){}

	@Override
	public void load(LittleEndianAccessor slea){}
}