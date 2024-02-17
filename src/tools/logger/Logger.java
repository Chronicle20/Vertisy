package tools.logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Calendar;

import client.PlayerGMRank;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import server.TimerManager;
import tools.ExceptionUtil;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since May 13, 2017
 */
public class Logger{

	public enum LogType{
		ERROR,
		INFO,
		WARNING,;
	}

	public enum LogFile{
		EXCEPTION("exceptions.txt", PlayerGMRank.CONTROLLER),
		CASH_ITEM("cashItem/", PlayerGMRank.GM),
		BUY_CASH_ITEM("buyCashItem/", PlayerGMRank.GM),
		GUILD_FUNDS("guildfunds/", PlayerGMRank.GM),
		ATTACK_SPEED("attackspeed.txt", PlayerGMRank.CONTROLLER), // debug
		LOGIN("logins.txt", PlayerGMRank.GM),
		LOGIN_BAN("loginBans.txt", PlayerGMRank.GM),
		LOGIN_INFO("loginInfo.txt", PlayerGMRank.GM),
		GENERAL_CHAT("generalChat/", PlayerGMRank.GM),
		WHISPER("whisper/", PlayerGMRank.GM),
		PARTY_CHAT("partyChat/", PlayerGMRank.GM),
		BUDDY_CHAT("buddyChat/", PlayerGMRank.GM),
		GUILD_CHAT("guildChat/", PlayerGMRank.GM),
		TRADE_CHAT("tradeChat/", PlayerGMRank.GM),
		MESSENGER_CHAT("messengerChat/", PlayerGMRank.GM),
		PET_CHAT("petChat/", PlayerGMRank.INTERN),
		GENERAL_ERROR("generalError.txt", PlayerGMRank.GM),
		ACCOUNT_STUCK("accountStuck.txt", PlayerGMRank.ADMIN),
		REMOTE_EXCEPTION("RemoteExceptions.txt", PlayerGMRank.CONTROLLER),
		QUESTS("quests/", PlayerGMRank.GM),
		SESSIONS("sessions.txt", PlayerGMRank.SUPERGM),
		ANTICHEAT("anticheat.txt", PlayerGMRank.GM),
		ANTICHEAT_2("anticheat2.txt", PlayerGMRank.GM),
		STORAGE("storage/", PlayerGMRank.GM),
		GENERAL_INFO("generalInfo.txt", PlayerGMRank.GM),
		ITEM_GAIN_GACH("itemGainGach/", PlayerGMRank.GM),
		EXP_LOG("expLog/", PlayerGMRank.GM),
		PACKET_LOGS("packetLogs/", PlayerGMRank.CONTROLLER),
		COMMAND("commands/", PlayerGMRank.GM),
		BUG_REPORT("bugreport.txt", PlayerGMRank.GM),
		GM_CALL("gmcall.txt", PlayerGMRank.GM),
		UNCODED("uncoded.txt", PlayerGMRank.GM),
		TRADE("trades/", PlayerGMRank.GM),
		EXPEDITIONS("expeditions.txt", PlayerGMRank.GM),
		MISSING_HANDLER("missingHandler.txt", PlayerGMRank.CONTROLLER),
		CLIENT_ERROR("clienterror.txt", PlayerGMRank.GM),
		PACKET_ERROR("packetError.txt", PlayerGMRank.GM),
		GM_LOGIN_FAIL("gmloginfail.txt", PlayerGMRank.GM);

		private final String fileName;
		private final PlayerGMRank gmRank;

		private LogFile(String fileName, PlayerGMRank gmRank){
			this.fileName = fileName;
			this.gmRank = gmRank;
		}

		public String getFileName(){
			return fileName;
		}

		public PlayerGMRank getRank(){
			return gmRank;
		}
	}

	private static String FILE_PATH = null;
	// private static LoggerInitializer loggerInitializer;
	public static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

	public static void start(){
		// Update the path with correct date every 30 minutes
		TimerManager.getInstance().register("Logger-TimeUpdater", ()-> {
			Calendar cal = Calendar.getInstance();
			FILE_PATH = "./logs/" + cal.get(Calendar.YEAR) + "/" + cal.get(Calendar.MONTH) + "/" + cal.get(Calendar.DAY_OF_MONTH) + "/";
		}, 30 * 60 * 1000);
		Calendar cal = Calendar.getInstance();
		FILE_PATH = "./logs/" + cal.get(Calendar.YEAR) + "/" + cal.get(Calendar.MONTH) + "/" + cal.get(Calendar.DAY_OF_MONTH) + "/";
	}

	public static void log(LogType type, LogFile file, final Throwable throwable){
		log(type, file, throwable, null);
	}

	public static void log(LogType type, LogFile file, final Throwable throwable, String info){
		log(type, file, null, throwable, info);
	}

	public static void log(LogType type, LogFile file, String fileName, final Throwable throwable, String info){
		System.out.println("Stacktrace info: " + info);
		throwable.printStackTrace();
		String data = Calendar.getInstance().getTime().toString() + "\r\n";
		if(info != null){
			data += info;
			data += "\r\n";
		}
		data += ExceptionUtil.getStringFromThrowable(throwable);
		data += "\n";// hmmm
		byte[] saveData = data.getBytes();// we save to binary.
		Path path = Paths.get(FILE_PATH, type.name() + "/");
		try{
			Files.createDirectories(path);
			path = path.resolve(file.getFileName());
			if(fileName != null && fileName.length() > 0){
				if(!fileName.contains(".")) fileName += ".txt";
				Files.createDirectories(path);
				path = path.resolve(fileName);
			}
			Files.write(path, saveData, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		}catch(IOException e){// The logger failed to log.. what do we log this to
			e.printStackTrace();
		}
	}

	public static void log(LogType type, LogFile file, String data){
		log(type, file, "", data);
	}

	public static void log(LogType type, LogFile file, String fileName, String data){
		log(type, file, fileName, data, "");
	}

	public static void log(LogType type, LogFile file, String fileName, String data, Object... args){
		String date = Calendar.getInstance().getTime().toString();
		data = date + "\r\n" + data;
		data += "\n";// hmmm
		if(data.contains("{}") || data.contains("%s") || data.contains("%d") || data.contains("%f")) data = String.format(data.replace("{}", "%s"), args);
		byte[] saveData = data.getBytes();// we save to binary.
		Path path = Paths.get(FILE_PATH, type.name() + "/");
		try{
			if(!Files.exists(path)) Files.createDirectories(path);
			path = path.resolve(file.getFileName());
			if(fileName != null && fileName.length() > 0){
				if(!fileName.contains(".")) fileName += ".txt";
				if(!Files.exists(path)) Files.createDirectories(path);
				path = path.resolve(fileName);
			}
			Files.write(path, saveData, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		}catch(IOException e){// The logger failed to log.. what do we log this to
			e.printStackTrace();
		}
	}
}
