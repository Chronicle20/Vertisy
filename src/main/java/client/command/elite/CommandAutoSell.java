package client.command.elite;

import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;
import client.inventory.MapleInventoryType;
import server.ItemInformationProvider;
import tools.ObjectParser;
import tools.StringUtil;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jan 21, 2016
 */
public class CommandAutoSell extends Command{

	public CommandAutoSell(){
		super("AutoSell", "Automatically sell items with a 15% cut in price.", "@AutoSell <toggle, ignore, unignore, list> <item, inventory>", null);
		setGMLevel(PlayerGMRank.ELITE);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length > 0){
			if(args[0].equalsIgnoreCase("toggle")){
				c.getPlayer().toggleAutoSell();
				c.getPlayer().dropMessage(MessageType.SYSTEM, "AutoSell is now " + (c.getPlayer().getAutoSell() ? "enabled." : "disabled."));
			}else if(args[0].equalsIgnoreCase("ignore")){
				if(args.length > 1){
					for(MapleInventoryType mit : MapleInventoryType.values()){
						if(mit.name().equalsIgnoreCase(args[1])){
							c.getPlayer().addAutoSellInventoryIgnore(mit);
							c.getPlayer().dropMessage(MessageType.SYSTEM, "AutoSell is now ignoring the " + mit.name() + " inventory.");
							return false;
						}
					}
					Integer in = ObjectParser.isInt(args[1]);
					if(in != null){
						String itemName = c.getPlayer().addAutoSellIgnore(in);
						c.getPlayer().dropMessage(MessageType.SYSTEM, "AutoSell is now ignoring the item " + itemName + " with the id " + in + ".");
					}else{
						String input = StringUtil.joinStringFrom(args, 1);
						ItemInformationProvider ii = ItemInformationProvider.getInstance();
						in = ii.getItemIDFromString(input);
						if(in != null && in != 0){
							c.getPlayer().addAutoSellIgnore(in);
							c.getPlayer().dropMessage(MessageType.SYSTEM, "AutoSell is now ignoring: " + input);
						}else{// Failed to find an exact match
							c.getPlayer().addAutoSellIgnore(StringUtil.joinStringFrom(args, 1));
							c.getPlayer().dropMessage(MessageType.SYSTEM, "AutoSell failed to find an item with the name: " + input + " and is now ignoring items containing it.");
						}
					}
				}else{
					c.getPlayer().dropMessage(getUsage());
				}
			}else if(args[0].equalsIgnoreCase("unignore")){
				for(MapleInventoryType mit : MapleInventoryType.values()){
					if(mit.name().equalsIgnoreCase(args[1])){
						c.getPlayer().removeAutoSellInventoryIgnore(mit);
						c.getPlayer().dropMessage(MessageType.SYSTEM, "AutoSell is no longer ignoring the " + mit.name() + " inventory.");
						return false;
					}
				}
				Integer in = ObjectParser.isInt(args[1]);
				if(in != null){
					c.getPlayer().removeAutoSellIgnore(in);
					c.getPlayer().dropMessage(MessageType.SYSTEM, "AutoSell is no longer ignoring " + in + ".");
				}else{
					String input = StringUtil.joinStringFrom(args, 1);
					ItemInformationProvider ii = ItemInformationProvider.getInstance();
					in = ii.getItemIDFromString(input);
					if(in != null && in != 0){// Found exact match, remove.
						if(c.getPlayer().removeAutoSellIgnore(in)){
							c.getPlayer().dropMessage(MessageType.SYSTEM, "AutoSell is no longer ignoring items with the name: " + input);
							return false;
						}
					}
					if(c.getPlayer().removeAutoSellIgnore(input)){
						c.getPlayer().dropMessage(MessageType.SYSTEM, "AutoSell is no longer ignoring items containing: " + input);
						return false;
					}
					c.getPlayer().dropMessage(MessageType.SYSTEM, "Unable to unignore anything with the input: " + input);
				}
			}else if(args[0].equalsIgnoreCase("list")){
				StringBuilder ignoring = new StringBuilder("Ignoring: ");
				for(String str : c.getPlayer().getAutoSellIgnore()){
					ignoring.append(str);
					ignoring.append(", ");
				}
				for(MapleInventoryType inv : c.getPlayer().getAutoSellInventoryIgnore()){
					ignoring.append(StringUtil.makeEnumHumanReadable(inv.name()));
					ignoring.append(", ");
				}
				if(ignoring.toString().contains(",")) ignoring.setLength(ignoring.length() - ", ".length());
				c.getPlayer().dropMessage(MessageType.SYSTEM, ignoring.toString());
			}else{
				c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
			}
		}else{
			c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
		}
		return false;
	}
}
