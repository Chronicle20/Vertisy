package client.command.controller;

import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Aug 18, 2016
 */
public class CommandIdCheck extends Command{

	public CommandIdCheck(){
		super("idcheck", "", "", null);
		setGMLevel(PlayerGMRank.CONTROLLER);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		/*Set<UUID> uniqueids = new HashSet<>();
		for(MapleInventoryType mit : MapleInventoryType.values()){
			Iterator<Item> it = c.getPlayer().getInventory(mit).iterator();
			while(it.hasNext()){
				Item item = it.next();
				System.out.println(item.getUniqueID() + " - " + item.getQuantity());
				if(uniqueids.contains(item.getUniqueID())){
					c.getPlayer().dropMessage(MessageType.ERROR, "Unique id: " + item.getUniqueID() + " is duplicated by: " + item.getItemId());
				}else uniqueids.add(item.getUniqueID());
			}
		}*/
		return false;
	}
}
