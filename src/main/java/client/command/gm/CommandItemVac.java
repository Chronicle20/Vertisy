package client.command.gm;

import java.util.Arrays;
import java.util.List;

import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;
import server.MapleInventoryManipulator;
import server.maps.MapleMapItem;
import server.maps.objects.MapleMapObject;
import server.maps.objects.MapleMapObjectType;
import tools.packets.field.DropPool;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 11, 2016
 */
public class CommandItemVac extends Command{

	public CommandItemVac(){
		super("ItemVac", "Loots all items in the map.", "!ItemVac", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		List<MapleMapObject> items = c.getPlayer().getMap().getMapObjectsInRange(c.getPlayer().getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.ITEM));
		for(MapleMapObject item : items){
			MapleMapItem mapitem = (MapleMapItem) item;
			if(!mapitem.isPlayerDrop()){
				if(mapitem.getMeso() > 0){
					c.getPlayer().gainMeso(mapitem.getMeso(), true, true, false);
				}else if(mapitem.getItem() != null){
					if(!c.getPlayer().canHoldItem(mapitem.getItem())){
						continue;
					}else MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), true);
				}
				mapitem.setPickedUp(true);
				c.getPlayer().getMap().broadcastMessage(DropPool.removeItemFromMap(mapitem.getObjectId(), 2, c.getPlayer().getId()), mapitem.getPosition());
				c.getPlayer().getMap().removeMapObject(item);
			}
		}
		return false;
	}
}
