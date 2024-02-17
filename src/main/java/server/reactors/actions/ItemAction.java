package server.reactors.actions;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collections;

import client.MapleClient;
import provider.MapleData;
import provider.MapleDataTool;
import scripting.reactor.ReactorScriptManager;
import server.maps.MapleMap;
import server.maps.MapleMapItem;
import server.maps.objects.MapleMapObjectType;
import server.reactors.MapleReactor;
import server.reactors.ReactorActionType;
import server.reactors.ReactorHitInfo;
import server.reactors.ReactorHitType;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.packets.field.DropPool;

/**
 * @author Tyler
 */
public class ItemAction extends MapleReactorEvent{

	public int stateTo, itemId, itemAmount;
	public Point lt, rb;

	public ItemAction(){
		super(ReactorActionType.ITEM);
	}

	public ItemAction(MapleData data){
		super(ReactorActionType.ITEM);
		processData(data);
	}

	public ItemAction(LittleEndianAccessor slea){
		super(ReactorActionType.ITEM);
		load(slea);
	}

	@Override
	public void processData(MapleData data){
		stateTo = MapleDataTool.getIntConvert(data.getChildByPath("state"));
		itemId = MapleDataTool.getIntConvert(data.getChildByPath("0"));
		itemAmount = MapleDataTool.getIntConvert(data.getChildByPath("1"));
		lt = MapleDataTool.getPoint(data.getChildByPath("lt"));
		rb = MapleDataTool.getPoint(data.getChildByPath("rb"));
	}

	@Override
	public void run(MapleClient c, MapleReactor reactor, ReactorHitInfo info){
		if(reactor.canSetAlive() && !reactor.isAlive()) return;
		MapleMap map = reactor.getMap();
		MapleMapItem item = (MapleMapItem) map.getMapObjectsInRect(getRect(reactor), Collections.singletonList(MapleMapObjectType.ITEM)).stream().filter(object-> {
			return checkIfItemMatches((MapleMapItem) object);
		}).findFirst().get();
		item.itemLock.lock();
		try{
			item.setPickedUp(true);
			reactor.setState(stateTo);
			map.broadcastMessage(DropPool.removeItemFromMap(item.getObjectId(), 0, 0), item.getPosition());
			ReactorScriptManager.getInstance().act(c, reactor);
			if(reactor.canSetAlive()) reactor.setAlive(false);
			if(reactor.getState() != null){
				if(reactor.getState().canRepeat()){// if the next state is a repeat. set to alive
					if(reactor.canSetAlive()) reactor.setAlive(true);
				}else{
					for(MapleReactorEvent event : reactor.getState().getEvents()){// erh? Seems some don't use repeat option
						if(event.getType().equals(ReactorActionType.ITEM)){
							if(reactor.canSetAlive()) reactor.setAlive(true);
							break;
						}
					}
				}
			}
		}finally{
			item.itemLock.unlock();
			map.removeMapObject(item);
		}
		item.setPickedUp(true);
	}

	@Override
	public boolean check(MapleClient c, MapleReactor reactor, ReactorHitType type, ReactorHitInfo info){
		if(reactor.canSetAlive() && !reactor.isAlive()) return false;
		if(type == ReactorHitType.HIT) return false;
		return reactor.getMap().getMapObjectsInRect(getRect(reactor), Collections.singletonList(MapleMapObjectType.ITEM)).stream().anyMatch(object-> {
			return checkIfItemMatches((MapleMapItem) object);
		});
	}

	private Rectangle getRect(MapleReactor reactor){
		return new Rectangle(reactor.getPosition().x + lt.x, reactor.getPosition().y + lt.y, rb.x - lt.x, rb.y - lt.y);
	}

	private boolean checkIfItemMatches(MapleMapItem item){
		if(item.getItemId() != itemId) return false;
		if(item.getItem().getQuantity() != itemAmount) return false;
		// if(!item.isPlayerDrop()) return false;
		if(item.isPickedUp()) return false;
		return true;
	}

	@Override
	public ItemAction clone(){
		ItemAction event = new ItemAction();
		event.stateTo = stateTo;
		event.itemId = itemId;
		event.itemAmount = itemAmount;
		event.lt = (Point) lt.clone();
		event.rb = (Point) rb.clone();
		return event;
	}

	@Override
	public void save(MaplePacketLittleEndianWriter mplew){
		mplew.writeInt(stateTo);
		mplew.writeInt(itemId);
		mplew.writeInt(itemAmount);
		mplew.writePos(lt);
		mplew.writePos(rb);
	}

	@Override
	public void load(LittleEndianAccessor slea){
		stateTo = slea.readInt();
		itemId = slea.readInt();
		itemAmount = slea.readInt();
		lt = slea.readPos();
		rb = slea.readPos();
	}
}