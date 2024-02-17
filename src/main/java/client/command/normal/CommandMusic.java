package client.command.normal;

import java.util.ArrayList;
import java.util.List;

import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;
import tools.MaplePacketCreator;
import tools.StringUtil;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jan 28, 2017
 */
public class CommandMusic extends Command{

	private static String[] songs = {"Jukebox/Congratulation", "Bgm00/SleepyWood", "Bgm00/FloralLife", "Bgm00/GoPicnic", "Bgm00/Nightmare", "Bgm00/RestNPeace", "Bgm01/AncientMove", "Bgm01/MoonlightShadow", "Bgm01/WhereTheBarlogFrom", "Bgm01/CavaBien", "Bgm01/HighlandStar", "Bgm01/BadGuys", "Bgm02/MissingYou", "Bgm02/WhenTheMorningComes", "Bgm02/EvilEyes", "Bgm02/JungleBook", "Bgm02/AboveTheTreetops", "Bgm03/Subway", "Bgm03/Elfwood", "Bgm03/BlueSky", "Bgm03/Beachway", "Bgm03/SnowyVillage", "Bgm04/PlayWithMe", "Bgm04/WhiteChristmas", "Bgm04/UponTheSky", "Bgm04/ArabPirate", "Bgm04/Shinin'Harbor", "Bgm04/WarmRegard", "Bgm05/WolfWood", "Bgm05/DownToTheCave", "Bgm05/AbandonedMine", "Bgm05/MineQuest", "Bgm05/HellGate", "Bgm06/FinalFight", "Bgm06/WelcomeToTheHell", "Bgm06/ComeWithMe", "Bgm06/FlyingInABlueDream", "Bgm06/FantasticThinking", "Bgm07/WaltzForWork", "Bgm07/WhereverYouAre", "Bgm07/FunnyTimeMaker", "Bgm07/HighEnough", "Bgm07/Fantasia", "Bgm08/LetsMarch", "Bgm08/ForTheGlory", "Bgm08/FindingForest", "Bgm08/LetsHuntAliens", "Bgm08/PlotOfPixie", "Bgm09/DarkShadow", "Bgm09/TheyMenacingYou", "Bgm09/FairyTale", "Bgm09/FairyTalediffvers", "Bgm09/TimeAttack", "Bgm10/Timeless", "Bgm10/TimelessB", "Bgm10/BizarreTales", "Bgm10/TheWayGrotesque", "Bgm10/Eregos", "Bgm11/BlueWorld", "Bgm11/Aquarium", "Bgm11/ShiningSea", "Bgm11/DownTown", "Bgm11/DarkMountain", "Bgm12/AquaCave", "Bgm12/DeepSee", "Bgm12/WaterWay", "Bgm12/AcientRemain", "Bgm12/RuinCastle", "Bgm12/Dispute", "Bgm13/CokeTown", "Bgm13/Leafre", "Bgm13/Minar'sDream", "Bgm13/AcientForest", "Bgm13/TowerOfGoddess", "Bgm14/DragonLoad", "Bgm14/HonTale", "Bgm14/CaveOfHontale", "Bgm14/DragonNest", "Bgm14/Ariant", "Bgm14/HotDesert", "Bgm15/MureungHill", "Bgm15/MureungForest", "Bgm15/WhiteHerb", "Bgm15/Pirate", "Bgm15/SunsetDesert", "Bgm16/Duskofgod", "Bgm16/FightingPinkBeen", "Bgm16/Forgetfulness", "Bgm16/Remembrance", "Bgm16/Repentance", "Bgm16/TimeTemple", "Bgm17/MureungSchool1", "Bgm17/MureungSchool2", "Bgm17/MureungSchool3", "Bgm17/MureungSchool4", "Bgm18/BlackWing", "Bgm18/DrillHall", "Bgm18/QueensGarden", "Bgm18/RaindropFlower", "Bgm18/WolfAndSheep", "Bgm19/BambooGym", "Bgm19/CrystalCave", "Bgm19/MushCatle", "Bgm19/RienVillage", "Bgm19/SnowDrop", "Bgm20/GhostShip", "Bgm20/NetsPiramid", "Bgm20/UnderSubway", "Bgm21/2021year", "Bgm21/2099year", "Bgm21/2215year", "Bgm21/2230year", "Bgm21/2503year", "Bgm21/KerningSquare", "Bgm21/KerningSquareField", "Bgm21/KerningSquareSubway", "Bgm21/TeraForest", "BgmEvent/FunnyRabbit", "BgmEvent/FunnyRabbitFaster", "BgmEvent/wedding", "BgmEvent/weddingDance", "BgmEvent/wichTower", "BgmGL/amoria", "BgmGL/Amorianchallenge", "BgmGL/chapel", "BgmGL/cathedral", "BgmGL/Courtyard", "BgmGL/CrimsonwoodKeep", "BgmGL/CrimsonwoodKeepInterior", "BgmGL/GrandmastersGauntlet", "BgmGL/HauntedHouse", "BgmGL/NLChunt", "BgmGL/NLCtown", "BgmGL/NLCupbeat", "BgmGL/PartyQuestGL", "BgmGL/PhantomForest", "BgmJp/Feeling", "BgmJp/BizarreForest", "BgmJp/Hana", "BgmJp/Yume", "BgmJp/Bathroom", "BgmJp/BattleField", "BgmJp/FirstStepMaster", "BgmMY/Highland", "BgmMY/KualaLumpur", "BgmSG/BoatQuay_field", "BgmSG/BoatQuay_town", "BgmSG/CBD_field", "BgmSG/CBD_town", "BgmSG/Ghostship", "BgmUI/ShopBgm", "BgmUI/Title"};
	public static List<String> sound = new ArrayList<>();

	public CommandMusic(){
		super("Music", "", "!Music <song>", "song");
		setGMLevel(PlayerGMRank.NORMAL);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length == 0){
			c.getPlayer().yellowMessage("Syntax: !music <song>");
			StringBuilder sb = new StringBuilder();
			for(String s : songs){
				// c.getPlayer().yellowMessage(s);
				sb.append(s + ", ");
			}
			sb.setLength(sb.length() - ", ".length());
			c.getPlayer().dropMessage(MessageType.MAPLETIP, sb.toString());
			return false;
		}
		String song = StringUtil.joinStringFrom(args, 0);
		for(String s : songs){
			if(s.equalsIgnoreCase(song)){
				c.getPlayer().announce(MaplePacketCreator.musicChange(s));
				c.getPlayer().yellowMessage("Now playing song " + s + ".");
				return true;
			}
		}
		c.getPlayer().yellowMessage("Song not found, please enter a song below.");
		StringBuilder sb = new StringBuilder();
		for(String s : songs){
			// c.getPlayer().yellowMessage(s);
			sb.append(s + ", ");
		}
		sb.setLength(sb.length() - ", ".length());
		c.getPlayer().dropMessage(MessageType.MAPLETIP, sb.toString());
		return false;
	}
}
