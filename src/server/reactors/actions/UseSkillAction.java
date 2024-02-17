package server.reactors.actions;

import java.util.ArrayList;
import java.util.List;

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
 * @since Jan 5, 2017
 */
public class UseSkillAction extends MapleReactorEvent{

	public int stateTo;
	public List<Integer> skills = new ArrayList<>();

	public UseSkillAction(){
		super(ReactorActionType.USE_SKILL);
	}

	public UseSkillAction(MapleData data){
		super(ReactorActionType.USE_SKILL);
		processData(data);
	}

	public UseSkillAction(LittleEndianAccessor slea){
		super(ReactorActionType.USE_SKILL);
		load(slea);
	}

	@Override
	public void processData(MapleData data){
		stateTo = MapleDataTool.getInt(data.getChildByPath("state"));
		MapleData skillData = data.getChildByPath("activeSkillID");
		if(skillData == null) return;
		for(MapleData skill : skillData.getChildren()){
			skills.add(MapleDataTool.getInt(skill));
		}
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
		for(int skill : skills){
			if(skill == info.skillid) return true;
		}
		return false;
	}

	@Override
	public UseSkillAction clone(){
		UseSkillAction useSkillAction = new UseSkillAction();
		useSkillAction.stateTo = stateTo;
		useSkillAction.skills = skills;
		return useSkillAction;
	}

	@Override
	public void save(MaplePacketLittleEndianWriter mplew){
		mplew.writeInt(stateTo);
		mplew.writeInt(skills.size());
		for(int skill : skills){
			mplew.writeInt(skill);
		}
	}

	@Override
	public void load(LittleEndianAccessor slea){
		stateTo = slea.readInt();
		int size = slea.readInt();
		for(int i = 0; i < size; i++){
			skills.add(slea.readInt());
		}
	}
}
