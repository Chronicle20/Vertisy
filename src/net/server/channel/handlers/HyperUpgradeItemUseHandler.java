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
 * @since Oct 30, 2017
 */
public final class HyperUpgradeItemUseHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		if(!FeatureSettings.HYPER_UPGRADE){
			c.getPlayer().dropMessage(MessageType.POPUP, FeatureSettings.HYPER_UPGRADE_DISABLED);
			c.announce(CWvsContext.enableActions());
			return;
		}
		slea.readInt();
		short scrollSlot = slea.readShort();
		short equipSlot = slea.readShort();
		boolean enchantSkill = slea.readBoolean();
		Item item = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(scrollSlot);
		if(item == null || (item.getItemId() != 2049300 && item.getItemId() != 2049301)){
			AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use an equip enhance scroll that is either null or not a equip enhance scroll.");
			return;
		}
		Item equipp = null;
		if(equipSlot >= 0) equipp = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(equipSlot);
		else equipp = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(equipSlot);
		if(equipp == null){
			AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use a equip enhance scroll on a null equip.");
			return;
		}
		if(equipSlot < 0 && enchantSkill){
			AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use a equip enhance scroll with Legendary Spirit on an equip currently equipped.");
			return;
		}
		if(enchantSkill){
			boolean found = false;
			for(Skill s : c.getPlayer().getSkills().keySet()){
				if(SkillConstants.isLegendarySpirit(s.getId())) found = true;
			}
			if(!found){
				AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use a equip enhance scroll with Legendary Spirit but didn't have Legendary Spirit.");
				return;
			}
		}
		Equip equip = (Equip) equipp;
		if(equip.getUpgradeSlots() != 0){
			AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use a equip enhance scroll on an Equip with slots.");
			return;
		}
		ItemData data = ItemInformationProvider.getInstance().getItemData(equip.getItemId());
		if(data.isCash){
			AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use a equip enhance scroll on a cash equip.");
			return;
		}
		boolean success = false, cursed = false;
		int stars = equip.getChuc();
		System.out.println("Stars: " + stars);
		double chance = item.getItemId() == 2049300 ? 100 : 80;
		if(stars > 0) chance -= stars * 10;
		chance = Math.max(chance, 10);
		System.out.println("Chance: " + chance);
		if(Randomizer.nextInt(101) <= chance){
			if(ItemInformationProvider.getInstance().hyperUpgradeItem(equip)){
				equip.setChuc((byte) (equip.getChuc() + 1));
				c.getPlayer().forceUpdateItem(equip);
				success = false;
			}
		}else{
			cursed = true;
			// List<ModifyInventory> mods = new ArrayList<>();
			// mods.add(new ModifyInventory(3, equip));
			// if(equip.getPosition() < 0) c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).removeItem(equip.getPosition());
			// else c.getPlayer().getInventory(MapleInventoryType.EQUIP).removeItem(equip.getPosition());
			// c.announce(MaplePacketCreator.modifyInventory(true, mods));
			// ItemFactory.deleteItem(equip);
		}
		MapleInventoryManipulator.removeItem(c, MapleInventoryType.USE, scrollSlot, 1, true, false);
		c.getPlayer().getMap().broadcastMessage(UserCommon.showItemHyperUpgradeEffect(c.getPlayer().getId(), success, cursed, enchantSkill, 0));
		c.getPlayer().equipChanged();
	}
}
