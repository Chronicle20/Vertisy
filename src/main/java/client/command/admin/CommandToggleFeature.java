package client.command.admin;

import java.lang.reflect.Field;

import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;
import constants.FeatureSettings;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Oct 15, 2016
 */
public class CommandToggleFeature extends Command{

	public CommandToggleFeature(){
		super("ToggleFeature", "", "!ToggleFeature", null);
		setGMLevel(PlayerGMRank.ADMIN);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length > 0){
			String feature = args[0];
			for(Field field : FeatureSettings.class.getFields()){
				if(field.getName().equalsIgnoreCase(feature)){
					if(field.getType().isAssignableFrom(Boolean.class) || field.getType().isAssignableFrom(boolean.class)){
						try{
							boolean val = (boolean) field.get(null);
							field.set(null, !val);
							c.getPlayer().dropMessage(MessageType.SYSTEM, feature + " has been " + (val ? "disabled" : "enabled") + ".");
						}catch(IllegalArgumentException | IllegalAccessException e){
							Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
						}
					}
					return false;
				}
			}
			c.getPlayer().dropMessage(MessageType.ERROR, "Unable to find a feature named: " + feature);
		}
		return false;
	}
}
