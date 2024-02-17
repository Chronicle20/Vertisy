package client.command.gm;

import client.MapleCharacter;
import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;
import server.ItemData;
import server.ItemInformationProvider;
import tools.ObjectParser;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 11, 2016
 */
public class CommandApplyItemToMap extends Command{

	public CommandApplyItemToMap(){
		super("ApplyItemToMap", "", "!applyitemtomap <itemid>", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length > 0){
			Integer item = ObjectParser.isInt(args[0]);
			if(item == null){
				c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
				return false;
			}
			ItemData data = ItemInformationProvider.getInstance().getItemData(item);
			if(data.itemEffect == null){
				c.getPlayer().dropMessage(MessageType.ERROR, "Unknown item: " + item);
				return false;
			}
			for(MapleCharacter chr : c.getPlayer().getMap().getCharacters()){
				data.itemEffect.applyTo(chr);
			}
		}else{
			c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
		}
		return false;
	}
}
