package client.command.gm;

import client.MapleClient;
import client.MessageType;
import client.command.Command;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import constants.ItemConstants;
import server.ItemData;
import server.ItemInformationProvider;
import server.MapleInventoryManipulator;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Oct 3, 2017
 */
public class CommandItem extends Command{

	public CommandItem(){
		super("item", "", "", "drop");
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		int itemId = Integer.parseInt(args[0]);
		short quantity = 1;
		try{
			if(args.length > 1) quantity = Short.parseShort(args[1]);
		}catch(Exception e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
		ItemData data = ItemInformationProvider.getInstance().getItemData(itemId);
		if(data != null && data.exists){
			if(data.tamingMob > 0 && (itemId < 1902000 && itemId > 1932012)){
				c.getPlayer().dropMessage(MessageType.ERROR, "This item has a tamingMob > 0");
				return true;
			}
			if(commandLabel.equals("item")){
				int petid = -1;
				long expiration = -1;
				if(ItemConstants.isPet(itemId)){
					petid = MaplePet.createPet(itemId);
					expiration = System.currentTimeMillis() + (90 * 24 * 60 * 60 * 1000L);
				}
				Item item = null;
				if(ItemInformationProvider.getInstance().getInventoryType(itemId) == MapleInventoryType.EQUIP){
					item = ItemInformationProvider.getInstance().getEquipById(itemId);
				}else if(petid != -1){
					item = MaplePet.loadFromDb(itemId, (short) -1, petid);
				}else{
					item = new Item(itemId, quantity);
				}
				item.setOwner(c.getPlayer().getName());
				item.setPetId(petid);
				item.setExpiration(expiration);
				MapleInventoryManipulator.addFromDrop(c, item, true);
			}else{
				Item toDrop;
				if(ItemInformationProvider.getInstance().getInventoryType(itemId) == MapleInventoryType.EQUIP){
					toDrop = ItemInformationProvider.getInstance().getEquipById(itemId);
				}else{
					toDrop = new Item(itemId, (short) 0, quantity);
				}
				toDrop.setOwner(c.getPlayer().getName());
				c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), toDrop, c.getPlayer().getPosition(), true, true);
			}
		}else{
			c.getPlayer().dropMessage(MessageType.ERROR, "Unknown item.");
		}
		return true;
	}
}
