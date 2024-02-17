package client.command.admin;

import java.io.IOException;

import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.SkillFactory;
import client.command.Command;
import client.command.CommandHandler;
import constants.MobConstants;
import net.PacketProcessor;
import net.RecvOpcode;
import net.SendOpcode;
import net.channel.ChannelServer;
import net.server.channel.Channel;
import scripting.item.ItemScriptManager;
import scripting.map.MapScriptManager;
import scripting.portal.PortalScriptManager;
import scripting.reactor.ReactorScriptManager;
import server.ItemInformationProvider;
import server.cashshop.CashItemFactory;
import server.life.MapleLifeFactory;
import server.life.MapleMonsterInformationProvider;
import server.quest.MapleQuest;
import server.reactors.MapleReactorFactory;
import server.shops.MapleShopFactory;
import tools.ExternalCodeTableGetter;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 11, 2016
 */
public class CommandReload extends Command{

	public CommandReload(){
		super("Reload", "", "!Reload <type>", null);
		setGMLevel(PlayerGMRank.ADMIN);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length > 0){
			switch (args[0].toLowerCase()){
				case "drops":
					MapleMonsterInformationProvider.getInstance().clearDrops();
					ReactorScriptManager.getInstance().clearDrops();
					c.getPlayer().dropMessage(5, "Reloaded Drops");
					break;
				case "portals":
					PortalScriptManager.getInstance().reloadPortalScripts();
					c.getPlayer().dropMessage(5, "Reloaded Portals");
					break;
				case "reactors":
					MapleReactorFactory.reloadReactors();
					ReactorScriptManager.getInstance().clearDrops();
					c.getPlayer().dropMessage(5, "Reloaded Reactors");
					break;
				case "shops":
					MapleShopFactory.getInstance().clearShops();
					c.getPlayer().dropMessage(5, "Reloaded Shops.");
					break;
				case "mapscripts":
					MapScriptManager.getInstance().reloadScripts();
					c.getPlayer().dropMessage(5, "Reloaded MapScripts.");
					break;
				case "events":
					int chs = 0;
					for(Channel ch : ChannelServer.getInstance().getChannels()){
						if(ch != null){
							ch.reloadEventScriptManager();
							chs++;
						}
					}
					c.getPlayer().dropMessage(5, "Reloaded Events on " + chs + " channels.");
					break;
				case "itemscripts":
					ItemScriptManager.getInstance().clearScripts();
					c.getPlayer().dropMessage(5, "Reloaded Item Scripts");
					break;
				case "quests":
					MapleQuest.clearCache();
					c.getPlayer().dropMessage(5, "Reloaded Quests");
					break;
				case "questscripts":
					MapleQuest.clearCache();
					c.getPlayer().dropMessage(5, "Reloaded Quests");
					break;
				case "map":
					ChannelServer.getInstance().reloadMap(c.getPlayer().getMapId());
					c.getPlayer().dropMessage(5, "Reloaded Map");
					break;
				case "commands":
					CommandHandler.loadCommands();
					c.getPlayer().dropMessage(5, "Reloaded Commands");
					break;
				case "opcodes":
					try{
						ExternalCodeTableGetter.populateValues(RecvOpcode.getDefaultProperties(), RecvOpcode.values());
						ExternalCodeTableGetter.populateValues(SendOpcode.getDefaultProperties(), SendOpcode.values());
						for(int chid : ChannelServer.getInstance().getChannelIDs())
							PacketProcessor.getProcessor(ChannelServer.getInstance().getWorldID(), chid);
						c.getPlayer().dropMessage(5, "Reloaded opcodes");
					}catch(IOException e){
						Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
					}
					break;
				case "mobs":
					MapleLifeFactory.removeMobData();
					c.getPlayer().dropMessage(5, "Reloaded Mobs");
					break;
				case "npcs":
					MapleLifeFactory.removeMobData();
					c.getPlayer().dropMessage(5, "Reloaded Mobs");
					break;
				case "localscripts":
					c.clearLocalScripts();
					c.getPlayer().dropMessage(5, "Cleared your scripts");
					break;
				case "items":{
					ItemInformationProvider.getInstance().reload();
					c.getPlayer().dropMessage(5, "Reloaded item data");
					break;
				}
				case "cashshop":{
					CashItemFactory.reloadModifiedCashItems();
					c.getPlayer().dropMessage(5, "Reloaded cashshop data");
					break;
				}
				case "slayermobs":{
					MobConstants.loadSlayerMonsters(ChannelServer.getInstance());
					break;
				}
				case "skills":{
					SkillFactory.loadAllSkills();
					c.getPlayer().dropMessage(5, "Reloaded skill data");
					break;
				}
			}
		}else{
			c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
		}
		return false;
	}
}
