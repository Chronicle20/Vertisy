package client.command.controller;

import java.io.IOException;

import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;
import tools.TempStatistics;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 11, 2016
 */
public class CommandDumpStats extends Command{

	public CommandDumpStats(){
		super("DumpStats", "", "", null);
		setGMLevel(PlayerGMRank.CONTROLLER);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		try{
			TempStatistics.dumpResults();
			c.getPlayer().dropMessage("Done.");
		}catch(IOException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
			c.getPlayer().dropMessage("IO Exception thrown. " + ex.getMessage());
		}
		return false;
	}
}
