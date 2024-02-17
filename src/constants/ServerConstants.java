package constants;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;

import tools.ObjectParser;

public class ServerConstants{

	public static short rev = 2;
	//
	public static short VERSION = 92;
	public static String PATCH = "1";
	public static String[] WORLD_NAMES = {"Scania", "Bera", "Broa", "Windia", "Khaini", "Bellocan", "Mardia", "Kradia", "Yellonde", "Demethos", "Galicia", "El Nido", "Zenith", "Arcenia", "Kastia", "Judis", "Plana", "Kalluna", "Stius", "Croa", "Medere"};
	// Login Configuration
	public static final int CHANNEL_LOAD = 200;// Players per channel
	public static final long RANKING_INTERVAL = 60 * 60 * 1000;// 60 minutes, 3600000
	public static final int QUEUE_INTERVAL = 30 * 1000; // 30 seconds.
	public static final long MTS_AUCTION_INTERVAL = 5 * 60 * 1000L;
	public static final boolean ENABLE_PIC = true, ENABLE_PIN = false;
	// Event Configuration
	public static final boolean PERFECT_PITCH = false;
	// IP Configuration
	public static String HOST;
	// Database Configuration
	public static String DB_URL = "";
	public static String DB_USER = "";
	public static String DB_PASS = "";
	// Other Configuration
	public static boolean JAVA_8;
	public static boolean SHUTDOWNHOOK;
	// Gameplay Configurations
	public static final boolean USE_DUEY = false;
	public static final boolean LOG_SHARK = false; // Logs packets in MapleShark binaries
	public static final boolean USE_PARTY_SEARCH = false;
	// Rates
	public static int PARTY_EXPERIENCE_MOD = 1; // change for event stuff
	public static final double PQ_BONUS_EXP_MOD = 1;
	// shutdown
	public static Thread shutdown = null;
	public static ScheduledFuture<?> ts = null;
	public static int secondsLeft = 0;
	public static int clockAll = 0;
	// Events
	public static double expEvent = 50;// percent
	public static long expEventEnd = 0;
	//
	public static boolean WZ_LOADING = true, BIN_DUMPING = false;
	// Search/Find command uses String.wz
	// MapleOxQuiz uses etc.wz for answers
	//
	public static boolean BCRYPT = true;
	//
	public static final String WORLD_SERVER_ERROR = "Failed to communicate with the WorldServer.";
	public static final String CENTER_SERVER_ERROR = "Failed to communicate with the CenterServer.";
	//
	public static String CENTER_SERVER_HOST = "192.168.2.217";
	public static final int CENTER_SERVER_PORT = 1111;
	public static String[] WORLD_SERVER_HOST = {"192.168.2.217"};
	public static int[] WORLD_SERVER_PORT = {1200};
	//
	public static long startup = System.currentTimeMillis();
	static{
		Properties p = new Properties();
		try{
			p.load(new FileInputStream("configuration.ini"));
			// SERVER
			ServerConstants.HOST = p.getProperty("HOST");
			ServerConstants.CENTER_SERVER_HOST = p.getProperty("CENTER_SERVER_HOST");
			ServerConstants.WORLD_SERVER_HOST = p.getProperty("WORLD_SERVER_HOST").split(", ");
			String[] ports = p.getProperty("WORLD_SERVER_PORT").split(", ");
			WORLD_SERVER_PORT = new int[ports.length];
			for(int i = 0; i < ports.length; i++){
				WORLD_SERVER_PORT[i] = ObjectParser.isInt(ports[i]);
			}
			// SQL DATABASE
			ServerConstants.DB_URL = p.getProperty("DB_URL");
			ServerConstants.DB_USER = p.getProperty("DB_USER");
			ServerConstants.DB_PASS = p.getProperty("DB_PASS");
			// OTHER
			ServerConstants.JAVA_8 = p.getProperty("JAVA8").equalsIgnoreCase("TRUE");
			ServerConstants.SHUTDOWNHOOK = p.getProperty("SHUTDOWNHOOK").equalsIgnoreCase("true");
			ServerConstants.WZ_LOADING = p.getProperty("wz_loading").equalsIgnoreCase("true");
			ServerConstants.BIN_DUMPING = p.getProperty("bin_dumping").equalsIgnoreCase("true");
		}catch(Exception e){
			System.out.println("Failed to load configuration.ini.");
			e.printStackTrace();
			System.exit(0);
		}
	}
}
