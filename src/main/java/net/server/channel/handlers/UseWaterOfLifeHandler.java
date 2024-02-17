package net.server.channel.handlers;

import java.util.ArrayList;
import java.util.List;

import client.MapleClient;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import scripting.npc.NPCScriptManager;
import server.ItemData;
import server.ItemInformationProvider;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jan 22, 2017
 */
public class UseWaterOfLifeHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		if(c.getPlayer().haveItem(5180000)){
			List<Item> pets = new ArrayList<>();
			for(Item item : c.getPlayer().getInventory(MapleInventoryType.CASH)){
				if(item.getPet() != null){
					ItemData data = ItemInformationProvider.getInstance().getItemData(item.getItemId());
					if(item.getExpiration() < System.currentTimeMillis() && !data.noRevive) pets.add(item);
				}
			}
			NPCScriptManager.getInstance().start(c, 1032102, "WaterOfLife", "start", pets);
		}
	}
}
