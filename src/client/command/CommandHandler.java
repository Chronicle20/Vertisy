package client.command;

import java.util.ArrayList;
import java.util.List;

import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.admin.*;
import client.command.controller.*;
import client.command.elite.CommandAutoSell;
import client.command.elite.CommandElite;
import client.command.elite.CommandEliteLeft;
import client.command.elite.CommandPetVac;
import client.command.gm.*;
import client.command.intern.*;
import client.command.normal.*;
import client.command.supergm.CommandChair;
import constants.FeatureSettings;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jan 18, 2016
 */
public class CommandHandler{

	public static List<Command> commands;
	public static String[] specialCommands = {""};

	public static void loadCommands(){
		commands = new ArrayList<Command>();
		commands.add(new CommandBattleAnalysis());
		commands.add(new CommandNight());
		// commands.add(new CommandBossHP());
		commands.add(new CommandBug());
		commands.add(new CommandCharacter());
		commands.add(new CommandCompare());
		commands.add(new CommandDispose());
		commands.add(new CommandFollow());
		commands.add(new CommandGM());
		commands.add(new CommandKillCount());
		commands.add(new CommandMusic());
		commands.add(new CommandNX());
		commands.add(new CommandOnline());
		commands.add(new CommandPlaytime());
		commands.add(new CommandRates());
		commands.add(new CommandReincarnate());
		commands.add(new CommandSkills());
		commands.add(new CommandTrack());
		commands.add(new CommandUptime());
		commands.add(new CommandWhatDropsFrom());
		// elite
		commands.add(new CommandAutoSell());
		commands.add(new CommandElite());
		commands.add(new CommandEliteLeft());
		commands.add(new CommandPetVac());
		// Intern
		commands.add(new CommandBack());
		commands.add(new CommandWarp());
		commands.add(new CommandWarpHere());
		commands.add(new CommandMute());
		commands.add(new CommandUnMute());
		// gm
		commands.add(new CommandAP());
		commands.add(new CommandApplyItemToMap());
		commands.add(new CommandBan());
		commands.add(new CommandBigBrother());
		commands.add(new CommandBlock());
		commands.add(new CommandBomb());
		commands.add(new CommandChat());
		commands.add(new CommandCheckDmg());
		commands.add(new CommandCheckStats());
		commands.add(new CommandClearDrops());
		commands.add(new CommandClearQuest());
		commands.add(new CommandCompleteQuest());
		commands.add(new CommandCopyChr());
		commands.add(new CommandDC());
		commands.add(new CommandDebuff());
		commands.add(new CommandEventDebug());
		commands.add(new CommandFace());
		commands.add(new CommandGC());
		commands.add(new CommandGmShop());
		commands.add(new CommandHair());
		commands.add(new CommandHide());
		commands.add(new CommandHorntail());
		commands.add(new CommandIgnore());
		commands.add(new CommandIgnored());
		commands.add(new CommandItem());
		commands.add(new CommandItemVac());
		commands.add(new CommandJob());
		commands.add(new CommandLastPlayers());
		commands.add(new CommandLevelup());
		commands.add(new CommandLookup());
		commands.add(new CommandMap());
		commands.add(new CommandMoveAction());
		commands.add(new CommandMuteMap());
		commands.add(new CommandNotice());
		commands.add(new CommandPosition());
		commands.add(new CommandResetSkills());
		commands.add(new CommandResetSP());
		commands.add(new CommandScripts());
		commands.add(new CommandSetSkillLevel());
		commands.add(new CommandSetStat());
		commands.add(new CommandSkin());
		commands.add(new CommandSP());
		commands.add(new CommandSpawn());
		commands.add(new CommandUIToggle());
		commands.add(new CommandUnban());
		commands.add(new CommandWhereAmI());
		commands.add(new CommandWhois());
		commands.add(new CommandZakum());
		// supergm
		commands.add(new CommandChair());
		// admin
		commands.add(new CommandDCAll());
		commands.add(new CommandExpeditions());
		commands.add(new CommandGiveFakeBuff());
		commands.add(new CommandNPC());
		commands.add(new CommandReload());
		commands.add(new CommandSaveAll());
		commands.add(new CommandServerMessage());
		commands.add(new CommandShutdown());
		commands.add(new CommandToggleFeature());
		// Controller
		commands.add(new CommandArnahIP());
		commands.add(new CommandChatMsg());
		commands.add(new CommandDebugTimers());
		commands.add(new CommandDropMessage());
		commands.add(new CommandDumpStats());
		commands.add(new CommandGMLevel());
		commands.add(new CommandIdCheck());
		commands.add(new CommandMapDataInfo());
		commands.add(new CommandModifyBackground());
		commands.add(new CommandMonitored());
		commands.add(new CommandMonitorPackets());
		commands.add(new CommandMorph());
		commands.add(new CommandRate());
		commands.add(new CommandResetTimerManager());
		commands.add(new CommandRuntime());
		commands.add(new CommandSetLogLevel());
		commands.add(new CommandShowEffect());
		commands.add(new CommandSpecialEffect());
		commands.add(new CommandStartupChannel());
		commands.add(new CommandTimerDebug());
	}

	public static Command getCommand(String str){
		String[] command = str.substring(1).split(" ");
		for(Command cmd : commands){
			if(cmd.getName().equalsIgnoreCase(command[0])) return cmd;
			if(cmd.getAliases() != null && cmd.getAliases().size() > 0) for(String s : cmd.getAliases()){
				if(s.equalsIgnoreCase(command[0])) return cmd;
			}
		}
		return null;
	}

	public static boolean handleCommand(MapleClient c, String str){
		str = str.trim();
		if(str.startsWith("!") || str.startsWith("@") || str.startsWith("/")){
			String[] command = str.substring(1).trim().split(" ");
			for(Command cmd : commands){
				if(cmd.getName().equalsIgnoreCase(command[0]) || cmd.isAlias(command[0])){
					String realCommand = "";
					boolean ignoreString = false;
					for(String s : command){
						if(!s.equalsIgnoreCase(command[0]) || ignoreString){
							if(realCommand.length() == 0){
								realCommand = s;
							}else{
								realCommand = realCommand + ", " + s;
							}
						}else{
							ignoreString = true;
						}
					}
					if((cmd.getGMLevel() == PlayerGMRank.ELITE.getLevel() && c.checkEliteStatus()) || c.getPlayer().getGMLevel() >= cmd.getGMLevel() || FeatureSettings.NO_PERMS){
						if(cmd.isAlias(command[0])) cmd.usedAlias(command[0]);
						try{
							String[] split = realCommand.trim().split(", ");
							cmd.execute(c, command[0], realCommand.trim().length() == 0 ? new String[0] : split);
						}catch(Exception ex){
							Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
							c.getPlayer().dropMessage(MessageType.ERROR, "An error has occured. Please try again at another time.");// : " + ex.getClass());
							return true;
						}
						return true;
					}else{
						return false;
					}
				}
			}
		}
		return false;
	}
}
