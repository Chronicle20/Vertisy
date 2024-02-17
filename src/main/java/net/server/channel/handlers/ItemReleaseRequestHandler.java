package net.server.channel.handlers;

import client.MapleClient;
import client.MessageType;
import client.autoban.AutobanFactory;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.FeatureSettings;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CWvsContext;
import tools.packets.field.userpool.UserCommon;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Oct 27, 2017
 */
public class ItemReleaseRequestHandler extends AbstractMaplePacketHandler{

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		if(!FeatureSettings.CUBING){
			c.getPlayer().dropMessage(MessageType.POPUP, FeatureSettings.CUBING_DISABLED);
			c.announce(CWvsContext.enableActions());
			return;
		}
		slea.readInt();
		short glassPos = slea.readShort();// magnifying glass pos
		short equipPos = slea.readShort();// eqp pos
		Item item = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(glassPos);
		Item equip = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(equipPos);
		if(item == null || equip == null){
			AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to reveal a potential and either the glass or equip was null.");
			return;
		}
		if(item.getItemId() < 2460000 || item.getItemId() > 2460003){
			AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to reveal a potential with an invalid magnifying glass: " + item.getItemId());
			return;
		}
		Equip eqp = (Equip) equip;
		if(eqp.handleGlass(item)){
			c.getPlayer().forceUpdateItem(eqp);
			c.announce(UserCommon.showItemReleaseEffect(c.getPlayer().getId(), equipPos));
			MapleInventoryManipulator.removeItem(c, MapleInventoryType.USE, glassPos, 1, true, false);
		}
	}
}
