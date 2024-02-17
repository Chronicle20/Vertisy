package client.command.normal;

import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since May 20, 2016
 */
public class CommandPlaytime extends Command{

	public CommandPlaytime(){
		super("Playtime", "", "", null);
		setGMLevel(PlayerGMRank.NORMAL);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		c.getPlayer().dropMessage(MessageType.MAPLETIP, "Playtime: " + getTime(c.getPlayer().getPlaytime()));
		return false;
	}

	public String getTime(long time){
		long secondsInMilli = 1000;
		long minutesInMilli = secondsInMilli * 60;
		long hoursInMilli = minutesInMilli * 60;
		long daysInMilli = hoursInMilli * 24;
		long monthsInMilli = daysInMilli * 30;
		long months = time / monthsInMilli;
		time = time % monthsInMilli;
		long days = time / daysInMilli;
		time = time % daysInMilli;
		long hours = time / hoursInMilli;
		time = time % hoursInMilli;
		long minutes = time / minutesInMilli;
		time = time % minutesInMilli;
		long seconds = time / secondsInMilli;
		time = time % secondsInMilli;
		StringBuilder sb = new StringBuilder();
		if(months > 0){
			sb.append(months + (months == 1 ? " month" : " months") + ((days > 0 || hours > 0 || minutes > 0) ? ", " : ""));
		}
		if(days > 0){
			sb.append(days + (days == 1 ? " day" : " days") + ((hours > 0 || minutes > 0) ? ", " : ""));
		}
		if(hours > 0){
			sb.append(hours + (hours == 1 ? " hour" : " hours") + (minutes > 0 ? ", " : ""));
		}
		if(minutes > 0){
			sb.append(minutes + (minutes == 1 ? " minute" : " minutes"));
		}
		if(sb.length() < 5){
			sb.append(seconds + (seconds == 1 ? " second" : " seconds"));
		}
		return sb.toString();
	}
}
