package net.server.channel.handlers;

import client.MapleClient;
import client.MessageType;
import client.Skill;
import client.autoban.AutobanFactory;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.FeatureSettings;
import constants.SkillConstants;
import net.AbstractMaplePacketHandler;
import server.ItemData;
import server.ItemInformationProvider;
import server.MapleInventoryManipulator;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CWvsContext;
import tools.packets.field.userpool.UserCommon;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Oct 31, 2017
 */
public final class ItemOptionUpgradeItemUseHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		if(!FeatureSettings.ITEM_OPTION_UPGRADE){
			c.getPlayer().dropMessage(MessageType.POPUP, FeatureSettings.ITEM_OPTION_UPGRADE_DISABLED);
			c.announce(CWvsContext.enableActions());
			return;
		}
		slea.readInt();// timestamp
		short potSlot = slea.readShort();// pot scroll
		short equipSlot = slea.readShort();// equip slot. Can be negative(currently equipped), or positive(bEnchantSkill)
		boolean enchantSkill = slea.readBoolean();// bEnchantSkill
		Item item = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(potSlot);
		if(item == null || (item.getItemId() != 2049400 && item.getItemId() != 2049401)){
			AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use a potential scroll that is either null or not a potential scroll.");
			return;
		}
		Item equipp = null;
		if(equipSlot >= 0) equipp = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(equipSlot);
		else equipp = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(equipSlot);
		if(equipp == null){
			AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use a potential scroll on a null equip.");
			return;
		}
		if(equipSlot < 0 && enchantSkill){
			AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use a potential scroll with Legendary Spirit on an equip currently equipped.");
			return;
		}
		if(enchantSkill){
			boolean found = false;
			for(Skill s : c.getPlayer().getSkills().keySet()){
				if(SkillConstants.isLegendarySpirit(s.getId())) found = true;
			}
			if(!found){
				AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use a potential scroll with Legendary Spirit but didn't have Legendary Spirit.");
				return;
			}
		}
		Equip equip = (Equip) equipp;
		if(equip.getGrade() != 0){
			AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use a potential scroll on an Equip with a potential.");
			return;
		}
		ItemData data = ItemInformationProvider.getInstance().getItemData(equip.getItemId());
		if(data.isCash){
			AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use a potential scroll on a cash equip.");
			return;
		}
		boolean success = false;
		int chance = item.getItemId() == 2049400 ? 90 : item.getItemId() == 2049401 ? 70 : 0;
		if(Randomizer.nextInt(100) + 1 <= chance){
			success = true;
			equip.setGrade((byte) 1);
			c.getPlayer().forceUpdateItem(equip);
		}else c.announce(CWvsContext.enableActions());
		MapleInventoryManipulator.removeItem(c, MapleInventoryType.USE, potSlot, 1, true, false);
		c.getPlayer().getMap().broadcastMessage(UserCommon.showItemOptionUpgradeEffect(c.getPlayer().getId(), success, false, enchantSkill, 0));
		c.getPlayer().equipChanged();
	}
}
