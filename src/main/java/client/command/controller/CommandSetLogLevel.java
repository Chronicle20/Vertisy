package client.command.controller;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 11, 2016
 */
public class CommandSetLogLevel extends Command{

	public CommandSetLogLevel(){
		super("SetLogLevel", "", "!setloglevel loggerName logLevel", null);
		setGMLevel(PlayerGMRank.CONTROLLER);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length < 2){
			c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
			return false;
		}
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		Configuration config = ctx.getConfiguration();
		LoggerConfig loggerConfig = null;
		if(args[0].equalsIgnoreCase("root")){
			loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
		}else{
			loggerConfig = config.getLoggerConfig(args[0]);
		}
		if(args[1].equalsIgnoreCase("debug")){
			loggerConfig.setLevel(Level.DEBUG);
		}else if(args[1].equalsIgnoreCase("trace")){
			loggerConfig.setLevel(Level.TRACE);
		}else if(args[1].equalsIgnoreCase("info")){
			loggerConfig.setLevel(Level.INFO);
		}else if(args[1].equalsIgnoreCase("fatal")){
			loggerConfig.setLevel(Level.FATAL);
		}else if(args[1].equalsIgnoreCase("warn")){
			loggerConfig.setLevel(Level.WARN);
		}else if(args[1].equalsIgnoreCase("error")){
			loggerConfig.setLevel(Level.ERROR);
		}else if(args[1].equalsIgnoreCase("off")){
			loggerConfig.setLevel(Level.OFF);
		}else{
			c.getPlayer().dropMessage("Valid loglevels include: trace, debug, info, warn, error, fatal and off");
			return false;
		}
		ctx.updateLoggers();
		LogManager.getLogger(args[0]).log(loggerConfig.getLevel(), "Changed logger level to the current level.");
		c.getPlayer().dropMessage(5, "The log level for " + args[0] + " is now set to " + loggerConfig.getLevel().toString() + ".");
		return false;
	}
}
