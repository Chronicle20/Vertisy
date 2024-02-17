/*
 * This file is part of the OdinMS Maple Story Server
 * Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 * Matthias Butz <matze@odinms.de>
 * Jan Christian Meyer <vimes@odinms.de>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation version 3 as published by
 * the Free Software Foundation. You may not use, modify or distribute
 * this program unader any cother version of the GNU Affero General Public
 * License.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package client;

import java.awt.Point;
import java.lang.ref.WeakReference;
import java.rmi.RemoteException;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.Date;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import client.MapleQuestStatus.Status;
import client.autoban.AutobanFactory;
import client.autoban.AutobanManager;
import client.enums.AdminResult;
import client.inventory.*;
import client.player.PlayerStat;
import client.player.PlayerStats;
import client.player.SecondaryStat;
import client.player.boss.BossEntries;
import client.player.buffs.twostate.TSIndex;
import client.player.buffs.twostate.TwoStateTemporaryStat;
import constants.*;
import constants.skills.*;
import net.channel.ChannelServer;
import net.server.PlayerBuffValueHolder;
import net.server.PlayerCoolDownValueHolder;
import net.server.Server;
import net.server.guild.MapleGuild;
import net.server.guild.MapleGuildCharacter;
import net.server.world.*;
import scripting.event.EventInstanceManager;
import scripting.npc.NPCConversationManager;
import scripting.npc.NPCScriptManager;
import scripting.quest.QuestScriptManager;
import server.*;
import server.cashshop.CashShop;
import server.events.MapleEvents;
import server.events.RescueGaga;
import server.events.gm.MapleFindThatJewel;
import server.events.gm.MapleFitness;
import server.events.gm.MapleOla;
import server.item.Potential;
import server.item.PotentialLevelData;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.maps.*;
import server.maps.objects.*;
import server.partyquest.PartyQuest;
import server.propertybuilder.ExpProperty;
import server.quest.MapleQuest;
import server.shops.MapleShop;
import tools.*;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.*;
import tools.packets.field.MobPool;
import tools.packets.field.SummonedPool;
import tools.packets.field.userpool.UserCommon;
import tools.packets.field.userpool.UserRemote;

public class MapleCharacter extends AbstractAnimatedMapleMapObject{

	private static final String LEVEL_200 = "[Congrats] %s has reached Level 200! Let us congratulate %s on such an amazing achievement!";
	protected static final int[] DEFAULT_KEY = {18, 65, 2, 23, 3, 4, 5, 6, 16, 17, 19, 25, 26, 27, 31, 34, 35, 37, 38, 40, 43, 44, 45, 46, 50, 56, 59, 60, 61, 62, 63, 64, 57, 48, 29, 7, 24, 33, 41, 39};
	protected static final int[] DEFAULT_TYPE = {4, 6, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 4, 4, 5, 6, 6, 6, 6, 6, 6, 5, 4, 5, 4, 4, 4, 4, 4};
	protected static final int[] DEFAULT_ACTION = {0, 106, 10, 1, 12, 13, 18, 24, 8, 5, 4, 19, 14, 15, 2, 17, 11, 3, 20, 16, 9, 50, 51, 6, 7, 53, 100, 101, 102, 103, 104, 105, 54, 22, 52, 21, 25, 26, 23, 27};
	protected static final String[] BLOCKED_NAMES = {"admin", "owner", "moderator", "intern", "donor", "administrator", "help", "helper", "alert", "notice", "maplestory", "Vertisy", "wizet", "security", "official", "support", "gamemaster", "gm", "operate", "master", "GameMaster", "community", "message", "event", "test", "Obama", "Suushi", "Tiger", "FangBlade", "ZeroByDivide", "Misusing", "lnmateSearch", "MapleRoyals", "Extalia", "MapleLegends", "HawtMaple", "MapleDestiny", "FluffyMS"};
	private int world;
	protected int accountid;
	protected int id;
	protected int rank, rankMove, jobRank, jobRankMove;
	protected int level, highestLevel;
	protected int str, dex, luk, int_;
	protected int hp;
	protected int maxhp;
	protected int mp;
	protected int maxmp;
	private int hpMpApUsed;
	protected int hair;
	protected int face;
	protected int remainingAp;
	protected int[] remainingSp = new int[11];
	protected int fame;
	protected int initialSpawnPoint;
	protected int mapid;
	protected int gender;
	private int currentPage, currentType = 0, currentTab = 1;
	private byte currentSortType, currentSortColumn;
	private int chair;
	private int itemEffect;
	private int guildid, guildrank, allianceRank, gp;
	private int messengerposition = 4;
	private int slots = 0;
	private int energybar;
	protected int gmLevel;
	private int ci = 0;
	private int familyId = -1;
	private int bookCover;
	private int markedMonster = 0;
	private int battleshipHp = 0;
	private int mesosTraded = 0;
	private int possibleReports = 10;
	private int dojoPoints, vanquisherStage, dojoStage, dojoEnergy, vanquisherKills;
	private int omokwins, omokties, omoklosses, matchcardwins, matchcardties, matchcardlosses;
	private long dojoFinish, lastfametime, lastUsedCashItem, lastHealed, lastMesoDrop = -1, lastUsedSay = 0;
	public transient int localmaxhp, localmaxmp, localstr, localdex, localluk, localint, magic, watk;
	private boolean hidden, canDoor = true, hasMerchant, whiteChat = false, bigBrother = false;
	private int explorerLinkedLevel = 0, cygnusLinkedLevel;
	private String explorerLinkedName = null, cygnusLinkedName = null;
	private boolean finishedDojoTutorial, dojoParty, tag, isStunned, isSeduced;
	protected String name, randomizedName;
	private String chalktext;
	private String dataString, progressValues = "";
	private String search = null;
	protected AtomicInteger exp = new AtomicInteger();
	protected AtomicInteger gachaexp = new AtomicInteger();
	private AtomicInteger meso = new AtomicInteger();
	private int merchantmeso;
	protected BuddyList buddylist;
	private EventInstanceManager eventInstance = null;
	private HiredMerchant hiredMerchant = null;
	protected MapleClient client;
	private MapleGuildCharacter mgc = null;
	private MaplePartyCharacter mpc = null;
	private MapleInventory[] inventory;
	protected MapleJob job = MapleJob.BEGINNER;
	private short nSubJob;
	protected MapleMap map;// Make a Dojo pq instance
	private MapleMap dojoMap;
	private long lastMapChange;
	private MapleMessenger messenger = null;
	private MapleMiniGame miniGame;
	protected MapleMount maplemount;
	private MaplePet[] pets = new MaplePet[3];
	private MaplePlayerShop playerShop = null;
	private MapleShop shop = null;
	protected MapleSkinColor skinColor = MapleSkinColor.NORMAL;
	private MapleStorage storage = null;
	private MapleTrade trade = null;
	private SavedLocation savedLocations[];
	private SkillMacro[] skillMacros = new SkillMacro[5];
	private List<Integer> lastmonthfameids;
	private Map<Short, MapleQuestStatus> quests;
	private Set<MapleMonster> controlled = new LinkedHashSet<>();
	private Map<Integer, String> entered = new LinkedHashMap<>();
	private Set<MapleMapObject> visibleMapObjects = new LinkedHashSet<>();
	private Map<Skill, SkillEntry> skills = new ConcurrentHashMap<>(8, 0.75f, 8);
	private EnumMap<MapleBuffStat, MapleBuffStatDataHolder> effects = new EnumMap<>(MapleBuffStat.class);
	protected Map<Integer, MapleKeyBinding> keymap = new LinkedHashMap<>();
	private Map<Integer, MapleSummon> summons = new LinkedHashMap<>();
	private Map<Integer, MapleCoolDownValueHolder> coolDowns = new ConcurrentHashMap<>(50);
	private List<MapleDoor> doors = new ArrayList<>();
	private ScheduledFuture<?> dragonBloodSchedule;
	private ScheduledFuture<?> mapTimeLimitTask = null;
	private ScheduledFuture<?>[] fullnessSchedule = new ScheduledFuture<?>[3];
	private ScheduledFuture<?> hpDecreaseTask;
	private ScheduledFuture<?> expiretask;
	private ScheduledFuture<?> recoveryTask;
	private ScheduledFuture<?> crcSchedule, collisionCheck;
	private NumberFormat nf = new DecimalFormat("#,###,###,###");
	protected MonsterBook monsterbook;
	private List<MapleRing> crushRings = new ArrayList<>();
	private List<MapleRing> friendshipRings = new ArrayList<>();
	private static String[] ariantroomleader = new String[3];
	private static int[] ariantroomslot = new int[3];
	protected CashShop cashshop;
	private long portaldelay = 0, lastcombo = 0;
	private short combocounter = 0;
	private List<String> blockedPortals = new ArrayList<>();
	private Map<Short, String> area_info = new LinkedHashMap<>();
	private AutobanManager autoban;
	private boolean isbanned = false, chatBan = false;
	private int chatBanDuration;
	private Timestamp chatBanDate;
	private ScheduledFuture<?> pendantOfSpirit = null; // 1122017
	private byte pendantExp = 0, lastmobcount = 0;
	protected List<Integer> trockmaps = new ArrayList<>();
	protected List<Integer> viptrockmaps = new ArrayList<>();
	private Map<String, MapleEvents> events = new LinkedHashMap<>();
	private PartyQuest partyQuest = null;
	private boolean loggedIn = false;
	private MapleDragon dragon = null;
	private boolean autoSell;
	private List<String> autoSellIgnore = new ArrayList<>();
	private List<MapleInventoryType> autoSellInventoryTypeIgnore = new ArrayList<>();
	protected PlayerStats stats;
	private int ironMan, hardmode, reincarnations;
	// Skills
	protected Map<RSSkill, Byte> rsSkillLevel = new HashMap<RSSkill, Byte>();
	protected Map<RSSkill, Long> rsSkillExp = new HashMap<RSSkill, Long>();
	private Map<RSSkill, Integer> rsSkillTrack = new HashMap<RSSkill, Integer>();
	private byte combatProgress = 0;
	//
	private Set<Integer> bigbrotherMonitoring = new HashSet<>();
	// Marriage
	private int marriedto, marriageid;
	private int marriageringid, engagementringid;
	private MapleRing marriageRing;
	//
	public HashMap<String, Long> monsterKillHigher = new HashMap<>(), monsterKillTotal = new HashMap<>();

	// Slayer
	private SlayerTask slayerTask;
	private int tasksCompleted;
	// private Set<Integer> slayerExemptions;
	// Playtime
	private long playtimeStart;
	private long playtime;
	//
	public ClientData clientData = new ClientData();
	public ServerData serverData = new ServerData();
	//
	private boolean scriptDebug = false;
	//
	private RockPaperScissors rps = null;
	//
	private Map<String, ScheduledFuture<?>> timers = new ConcurrentHashMap<>();
	//
	private long expGained;
	private long firstExpGain = -1, lastExpGain = -1;
	//
	private List<Integer> previousMaps = new LinkedList<>();
	//
	private CRand crand;
	//
	private BossEntries bossEntries;
	//
	private boolean hasCheckedMapCRC;
	//
	// Random shit for gm commands
	public byte bMoveAction = -1;
	//
	public BattleAnalysis battleAnaylsis;
	//
	public boolean isFakeLogin;
	//
	public boolean uiToggle = false, gmFlyMode;
	//
	public List<Integer> requestedFollow = new ArrayList<>();
	public int driver = -1, passenger = -1;
	//
	public SecondaryStat secondaryStat;
	//
	public PlayerStat playerStat;
	//
	public int partyid = -1;

	protected MapleCharacter(){
		setStance(0);
		inventory = new MapleInventory[MapleInventoryType.values().length];
		savedLocations = new SavedLocation[SavedLocationType.values().length];
		for(int i = 0; i < remainingSp.length; i++){
			remainingSp[i] = 0;
		}
		for(MapleInventoryType type : MapleInventoryType.values()){
			byte b = 24;
			if(type == MapleInventoryType.CASH){
				b = 96;
			}
			inventory[type.ordinal()] = new MapleInventory(type, (byte) b);
		}
		for(int i = 0; i < SavedLocationType.values().length; i++){
			savedLocations[i] = null;
		}
		quests = new LinkedHashMap<>();
		setPosition(new Point(0, 0));
		bossEntries = new BossEntries();
		battleAnaylsis = new BattleAnalysis();
		secondaryStat = new SecondaryStat();
		playerStat = new PlayerStat();
	}

	public static MapleCharacter getDefault(MapleClient c){
		MapleCharacter ret = new MapleCharacter();
		ret.client = c;
		ret.gmLevel = 0;
		ret.hp = 50;
		ret.maxhp = 50;
		ret.mp = 5;
		ret.maxmp = 5;
		ret.str = 12;
		ret.dex = 5;
		ret.int_ = 4;
		ret.luk = 4;
		ret.map = null;
		ret.job = MapleJob.BEGINNER;
		ret.level = 1;
		ret.meso.set(20000);
		ret.accountid = c.getAccID();
		ret.buddylist = new BuddyList(20);
		ret.stats = new PlayerStats();
		ret.maplemount = null;
		ret.getInventory(MapleInventoryType.EQUIP).setSlotLimit(24);
		ret.getInventory(MapleInventoryType.USE).setSlotLimit(24);
		ret.getInventory(MapleInventoryType.SETUP).setSlotLimit(24);
		ret.getInventory(MapleInventoryType.ETC).setSlotLimit(24);
		for(int i = 0; i < DEFAULT_KEY.length; i++){
			ret.keymap.put(DEFAULT_KEY[i], new MapleKeyBinding(DEFAULT_TYPE[i], DEFAULT_ACTION[i]));
		}
		// to fix the map 0 lol
		for(int i = 0; i < 5; i++){
			ret.trockmaps.add(999999999);
		}
		for(int i = 0; i < 10; i++){
			ret.viptrockmaps.add(999999999);
		}
		return ret;
	}

	private long lastRecoveryTime = -1;
	public boolean berserk;

	/**
	 * Called by the Netty Channel Event loop scheduler every 1000ms.
	 */
	public void update(){
		// System.out.println("player update");
		//
		long updateTime = System.currentTimeMillis();
		synchronized(effects){
			for(Entry<MapleBuffStat, MapleBuffStatDataHolder> entry : this.effects.entrySet()){
				MapleStatEffect effect = entry.getValue().effect;
				if(updateTime >= entry.getValue().startTime + entry.getValue().duration && entry.getValue().duration != -1){
					cancelEffect(entry.getValue().effect, false, entry.getValue().startTime);
					continue;
				}
				if(effect.isRecovery()){
					if(updateTime - lastRecoveryTime >= 5000L){
						addHP(effect.getX());
						client.announce(UserLocal.UserEffect.showOwnRecovery((byte) effect.getX()));
						getMap().broadcastMessage(this, UserRemote.UserEffect.showRecovery(id, (byte) effect.getX()), false);
						lastRecoveryTime = updateTime;
					}
				}
				checkBerserk();
			}
		}
	}

	public void checkBerserk(){
		if(job.equals(MapleJob.DARKKNIGHT)){
			Skill BerserkX = SkillFactory.getSkill(DarkKnight.BERSERK);
			final int skilllevel = getSkillLevel(BerserkX);
			if(skilllevel > 0){
				berserk = getHp() * 100 / localmaxhp < BerserkX.getEffect(skilllevel).getX();
				client.announce(UserLocal.UserEffect.showOwnBerserk(skilllevel, berserk));
				getMap().broadcastMessage(this, UserRemote.UserEffect.showBerserk(getId(), skilllevel, berserk), false);
			}
		}
	}

	public void addCooldown(int skillId, long startTime, long length, ScheduledFuture<?> timer){
		if(this.coolDowns.containsKey(Integer.valueOf(skillId))){
			this.coolDowns.remove(skillId);
		}
		this.coolDowns.put(Integer.valueOf(skillId), new MapleCoolDownValueHolder(skillId, startTime, length, timer));
	}

	public void addCrushRing(MapleRing r){
		crushRings.add(r);
	}

	public MapleRing getRingById(int id){
		for(MapleRing ring : getCrushRings()){
			if(ring.getRingId() == id) return ring;
		}
		for(MapleRing ring : getFriendshipRings()){
			if(ring.getRingId() == id) return ring;
		}
		if(marriageRing != null && marriageRing.getRingId() == id) return marriageRing;
		return null;
	}

	public int addDojoPointsByMap(){
		int pts = 0;
		if(dojoPoints < 17000){
			pts = 1 + ((getMap().getId() - 1) / 100 % 100) / 6;
			if(!dojoParty){
				pts++;
			}
			this.dojoPoints += pts;
		}
		return pts;
	}

	public void addDoor(MapleDoor door){
		doors.add(door);
		if(mpc != null){
			this.mpc.addDoor(door);
			try{
				ChannelServer.getInstance().getWorldInterface().updateParty(getPartyId(), PartyOperation.UPDATE_DOOR, mpc);
			}catch(RemoteException | NullPointerException e){
				Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, e);
			}
		}
	}

	public void addFame(int famechange){
		this.fame += famechange;
	}

	public void addFriendshipRing(MapleRing r){
		friendshipRings.add(r);
	}

	public void addHP(int delta){
		setHp(hp + delta);
		updateSingleStat(MapleStat.HP, hp);
	}

	public void addMesosTraded(int gain){
		this.mesosTraded += gain;
	}

	public void addMP(int delta){
		setMp(mp + delta);
		updateSingleStat(MapleStat.MP, mp);
	}

	public void addMPHP(int hpDiff, int mpDiff){
		setHp(hp + hpDiff);
		setMp(mp + mpDiff);
		updateSingleStat(MapleStat.HP, getHp());
		updateSingleStat(MapleStat.MP, getMp());
	}

	public void addPet(MaplePet pet){
		for(int i = 0; i < 3; i++){
			if(pets[i] == null){
				pets[i] = pet;
				return;
			}
		}
	}

	public void addStat(int type, int up){
		if(type == 1){
			this.str += up;
			updateSingleStat(MapleStat.STR, str);
		}else if(type == 2){
			this.dex += up;
			updateSingleStat(MapleStat.DEX, dex);
		}else if(type == 3){
			this.int_ += up;
			updateSingleStat(MapleStat.INT, int_);
		}else if(type == 4){
			this.luk += up;
			updateSingleStat(MapleStat.LUK, luk);
		}
		recalcLocalStats();
	}

	public int addHP(MapleClient c){
		MapleCharacter player = c.getPlayer();
		MapleJob jobtype = player.getJob();
		int MaxHP = player.getMaxHp();
		if(player.getHpMpApUsed() > 9999 || MaxHP >= 30000) return MaxHP;
		if(jobtype.isA(MapleJob.BEGINNER)){
			MaxHP += 8;
		}else if(jobtype.isA(MapleJob.WARRIOR) || jobtype.isA(MapleJob.DAWNWARRIOR1)){
			if(player.getSkillLevel(player.isCygnus() ? SkillFactory.getSkill(10000000) : SkillFactory.getSkill(1000001)) > 0){
				MaxHP += 20;
			}else{
				MaxHP += 8;
			}
		}else if(jobtype.isA(MapleJob.MAGICIAN) || jobtype.isA(MapleJob.BLAZEWIZARD1)){
			MaxHP += 6;
		}else if(jobtype.isA(MapleJob.BOWMAN) || jobtype.isA(MapleJob.WINDARCHER1)){
			MaxHP += 8;
		}else if(jobtype.isA(MapleJob.THIEF) || jobtype.isA(MapleJob.NIGHTWALKER1)){
			MaxHP += 8;
		}else if(jobtype.isA(MapleJob.PIRATE) || jobtype.isA(MapleJob.THUNDERBREAKER1)){
			if(player.getSkillLevel(player.isCygnus() ? SkillFactory.getSkill(15100000) : SkillFactory.getSkill(5100000)) > 0){
				MaxHP += 18;
			}else{
				MaxHP += 8;
			}
		}
		return MaxHP;
	}

	public int addMP(MapleClient c){
		MapleCharacter player = c.getPlayer();
		int MaxMP = player.getMaxMp();
		if(player.getHpMpApUsed() > 9999 || player.getMaxMp() >= 30000) return MaxMP;
		if(player.getJob().isA(MapleJob.BEGINNER) || player.getJob().isA(MapleJob.NOBLESSE) || player.getJob().isA(MapleJob.LEGEND)){
			MaxMP += 6;
		}else if(player.getJob().isA(MapleJob.WARRIOR) || player.getJob().isA(MapleJob.DAWNWARRIOR1) || player.getJob().isA(MapleJob.ARAN1)){
			MaxMP += 2;
		}else if(player.getJob().isA(MapleJob.MAGICIAN) || player.getJob().isA(MapleJob.BLAZEWIZARD1)){
			if(player.getSkillLevel(player.isCygnus() ? SkillFactory.getSkill(12000000) : SkillFactory.getSkill(2000001)) > 0){
				MaxMP += 18;
			}else{
				MaxMP += 14;
			}
		}else if(player.getJob().isA(MapleJob.BOWMAN) || player.getJob().isA(MapleJob.THIEF)){
			MaxMP += 10;
		}else if(player.getJob().isA(MapleJob.PIRATE)){
			MaxMP += 14;
		}
		return MaxMP;
	}

	public void addSummon(int id, MapleSummon summon){
		summons.put(id, summon);
	}

	public void addVisibleMapObject(MapleMapObject mo){
		visibleMapObjects.add(mo);
	}

	public int getRollCallDate(String hwid){
		int date = 0;
		try{
			Connection con = DatabaseConnection.getConnection();
			try(PreparedStatement ps = con.prepareStatement("SELECT `date` FROM rollcall WHERE hwid = ?")){
				ps.setString(1, hwid);
				try(ResultSet rs = ps.executeQuery()){
					if(!rs.next()){
						rs.close();
						ps.close();
						return date;
					}
					date = rs.getInt("date");
				}
			}
			return date;
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
		return date;
	}

	public int getRollCallAmount(String hwid){
		int amount = 0;
		try{
			Connection con = DatabaseConnection.getConnection();
			try(PreparedStatement ps = con.prepareStatement("SELECT `amount` FROM rollcall WHERE hwid = ?")){
				ps.setString(1, hwid);
				try(ResultSet rs = ps.executeQuery()){
					if(!rs.next()){
						rs.close();
						ps.close();
						return amount;
					}
					amount = rs.getInt("amount");
				}
			}
			return amount;
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
		return amount;
	}

	public void setRollCall(String hwid, int amount, int date){
		try{
			Connection con = DatabaseConnection.getConnection();
			if(amount > 0){
				try(PreparedStatement ps = con.prepareStatement("UPDATE rollcall SET amount = ?, date = ? WHERE hwid = ?")){
					ps.setInt(1, ++amount);
					ps.setInt(2, date);
					ps.setString(3, hwid);
					ps.executeUpdate();
				}
			}else{
				try(PreparedStatement ps = con.prepareStatement("INSERT INTO rollcall (hwid, amount, date) VALUES (?, ?, ?)")){
					ps.setString(1, hwid);
					ps.setInt(2, ++amount);
					ps.setInt(3, date);
					ps.executeUpdate();
				}
			}
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
	}

	public void ban(String reason){
		Logger.log(LogType.INFO, LogFile.GENERAL_INFO, MapleCharacter.makeMapleReadable(this.name) + " was banned for " + reason);
		this.isbanned = true;
		try{
			Connection con = DatabaseConnection.getConnection();
			try(PreparedStatement ps = con.prepareStatement("UPDATE accounts SET banned = 1, banreason = ? WHERE id = ?")){
				ps.setString(1, reason);
				ps.setInt(2, accountid);
				ps.executeUpdate();
			}
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
	}

	public static boolean ban(String id, String reason, boolean accountId){
		Logger.log(LogType.INFO, LogFile.GENERAL_INFO, id + " was banned for " + reason);
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			Connection con = DatabaseConnection.getConnection();
			if(id.matches("/[0-9]{1,3}\\..*")){
				ps = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
				ps.setString(1, id);
				ps.executeUpdate();
				ps.close();
				return true;
			}
			if(accountId){
				ps = con.prepareStatement("SELECT id FROM accounts WHERE name = ?");
			}else{
				ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?");
			}
			boolean ret = false;
			ps.setString(1, id);
			rs = ps.executeQuery();
			if(rs.next()){
				try(PreparedStatement psb = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET banned = 1, banreason = ? WHERE id = ?")){
					psb.setString(1, reason);
					psb.setInt(2, rs.getInt(1));
					psb.executeUpdate();
				}
				ret = true;
			}
			rs.close();
			ps.close();
			return ret;
		}catch(SQLException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}finally{
			try{
				if(ps != null && !ps.isClosed()){
					ps.close();
				}
				if(rs != null && !rs.isClosed()){
					rs.close();
				}
			}catch(SQLException e){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
			}
		}
		return false;
	}

	public int calculateMaxBaseDamage(int watk){
		int maxbasedamage;
		Item weapon_item = getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11);
		Equip eqp = weapon_item != null ? (Equip) weapon_item : null;
		if(weapon_item != null && (eqp.getDurability() > 0 || eqp.getDurability() == -1)){
			MapleWeaponType weapon = ItemInformationProvider.getInstance().getWeaponType(weapon_item.getItemId());
			int mainstat;
			int secondarystat;
			if(getJob().isA(MapleJob.THIEF) && weapon == MapleWeaponType.DAGGER_OTHER){
				weapon = MapleWeaponType.DAGGER_THIEVES;
			}
			if(weapon == MapleWeaponType.BOW || weapon == MapleWeaponType.CROSSBOW || weapon == MapleWeaponType.GUN){
				mainstat = localdex;
				secondarystat = localstr;
			}else if(weapon == MapleWeaponType.CLAW || weapon == MapleWeaponType.DAGGER_THIEVES){
				mainstat = localluk;
				secondarystat = localdex + localstr;
			}else{
				mainstat = localstr;
				secondarystat = localdex;
			}
			maxbasedamage = (int) (((weapon.getMaxDamageMultiplier() * mainstat + secondarystat) / 100.0) * watk);
		}else{
			if(job.isA(MapleJob.PIRATE) || job.isA(MapleJob.THUNDERBREAKER1)){
				double weapMulti = 3;
				if(job.getId() % 100 != 0){
					weapMulti = 4.2;
				}
				int attack = (int) Math.min(Math.floor((2 * getLevel() + 31) / 3), 31);
				maxbasedamage = (int) (localstr * weapMulti + localdex) * attack / 100;
			}else{
				maxbasedamage = 1;
			}
		}
		return maxbasedamage;
	}

	public int calculateMinBaseDamage(int watk){
		int maxbasedamage;
		Item weapon_item = getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11);
		Equip eqp = weapon_item != null ? (Equip) weapon_item : null;
		if(weapon_item != null && (eqp.getDurability() > 0 || eqp.getDurability() == -1)){
			MapleWeaponType weapon = ItemInformationProvider.getInstance().getWeaponType(weapon_item.getItemId());
			int mainstat;
			int secondarystat;
			if(getJob().isA(MapleJob.THIEF) && weapon == MapleWeaponType.DAGGER_OTHER){
				weapon = MapleWeaponType.DAGGER_THIEVES;
			}
			if(weapon == MapleWeaponType.BOW || weapon == MapleWeaponType.CROSSBOW || weapon == MapleWeaponType.GUN){
				mainstat = localdex;
				secondarystat = localstr;
			}else if(weapon == MapleWeaponType.CLAW || weapon == MapleWeaponType.DAGGER_THIEVES){
				mainstat = localluk;
				secondarystat = localdex + localstr;
			}else{
				mainstat = localstr;
				secondarystat = localdex;
			}
			double mastery = 0.1;// todo mastery
			// System.out.println(weapon.toString() + ", " + mainstat + "," + secondarystat + ", " + watk);
			maxbasedamage = (int) ((mainstat * weapon.getMinDamageMultiplier() * weapon.getMaxDamageMultiplier() * mastery + secondarystat) / 100 * watk);
		}else{
			if(job.isA(MapleJob.PIRATE) || job.isA(MapleJob.THUNDERBREAKER1)){
				double weapMulti = 3;
				if(job.getId() % 100 != 0){
					weapMulti = 4.2;
				}
				int attack = (int) Math.min(Math.floor((2 * getLevel() + 31) / 3), 31);
				maxbasedamage = (int) (localstr * weapMulti + localdex) * attack / 100;
			}else{
				maxbasedamage = 1;
			}
		}
		return maxbasedamage;
	}

	public void cancelAllBuffs(){
		for(MapleBuffStatDataHolder mbsvh : new ArrayList<>(effects.values())){
			cancelEffect(mbsvh.effect, false, mbsvh.startTime);
		}
	}

	public void cancelBuffStats(MapleBuffStat stat){
		List<MapleBuffStat> buffStatList = Arrays.asList(stat);
		deregisterBuffStats(buffStatList);
		cancelPlayerBuffs(buffStatList);
	}

	public void setCombo(short count){
		if(count < combocounter){
			cancelEffectFromBuffStat(MapleBuffStat.ARAN_COMBO);
		}
		combocounter = (short) Math.min(30000, count);
		if(count > 0){
			announce(UserLocal.showCombo(combocounter));
		}
	}

	public void setLastCombo(long time){
		lastcombo = time;
	}

	public short getCombo(){
		return combocounter;
	}

	public long getLastCombo(){
		return lastcombo;
	}

	public int getLastMobCount(){ // Used for skills that have mobCount at 1. (a/b)
		return lastmobcount;
	}

	public void setLastMobCount(byte count){
		lastmobcount = count;
	}

	public void newClient(MapleClient c){
		this.loggedIn = true;
		c.setAccountName(this.client.getAccountName());// No null's for accountName
		c.setEliteStart(client.getEliteStart());
		c.setEliteLength(client.getEliteLength());
		c.setLastNameChange(client.getLastNameChange());
		c.setCharacterSlots(client.getCharacterSlots());
		c.setPetVac(client.getPetVac());
		c.setAlphaUser(client.isAlphaUser());
		c.skipTutorial = client.skipTutorial;
		c.setProgressValues(client.getProgressValues());
		c.parseMac(client.toStringMac());
		c.parseHWID(client.toStringHwid());
		this.client = c;
		MaplePortal portal = map.findClosestSpawnpoint(getPosition());
		if(portal == null){
			portal = map.getPortal(0);
		}
		this.setPosition(portal.getPosition());
		this.initialSpawnPoint = portal.getId();
		this.map = c.getChannelServer().getMap(getMapId());
	}

	public String getMedalText(){
		String medal = "";
		final Item medalItem = getInventory(MapleInventoryType.EQUIPPED).getItem((short) -49);
		if(medalItem != null){
			medal = "<" + ItemInformationProvider.getInstance().getItemData(medalItem.getItemId()).name + "> ";
		}
		return medal;
	}

	public static class CancelCooldownAction implements Runnable{

		private int skillId;
		private WeakReference<MapleCharacter> target;

		public CancelCooldownAction(MapleCharacter target, int skillId){
			this.target = new WeakReference<>(target);
			this.skillId = skillId;
		}

		@Override
		public void run(){
			MapleCharacter realTarget = target.get();
			if(realTarget != null){
				realTarget.removeCooldown(skillId);
				realTarget.client.announce(MaplePacketCreator.skillCooldown(skillId, 0));
			}
		}
	}

	public void cancelEffect(int itemId){
		cancelEffect(ItemInformationProvider.getInstance().getItemData(itemId).itemEffect, false, -1);
	}

	public void cancelEffect(MapleStatEffect effect, boolean overwrite, long startTime){
		// dropMessage("cancelEffect(), Override: " + effect.getSourceId() + ", " + overwrite);
		List<MapleBuffStat> buffstats;
		if(!overwrite){
			buffstats = getBuffStats(effect, startTime);
		}else{
			List<Pair<MapleBuffStat, BuffDataHolder>> statups = effect.getStatups();
			buffstats = new ArrayList<>(statups.size());
			for(Pair<MapleBuffStat, BuffDataHolder> statup : statups){
				buffstats.add(statup.getLeft());
			}
		}
		if(effect.getSourceId() >= 2022125 && effect.getSourceId() <= 2022129){// beholder
			for(MapleBuffStat stat : effects.keySet()){
				MapleBuffStatDataHolder mbsvh = effects.get(stat);
				if(mbsvh.value.getSourceID() == effect.getSourceId()){
					buffstats.add(stat);
				}
			}
		}
		deregisterBuffStats(buffstats);
		if(effect.isMagicDoor()){
			if(!getDoors().isEmpty()){
				MapleDoor door = getDoors().iterator().next();
				for(MapleCharacter chr : door.getTarget().getCharacters()){
					door.sendDestroyData(chr.client);
				}
				for(MapleCharacter chr : door.getTown().getCharacters()){
					door.sendDestroyData(chr.client);
				}
				for(MapleDoor destroyDoor : getDoors()){
					door.getTarget().removeMapObject(destroyDoor);
					door.getTown().removeMapObject(destroyDoor);
				}
				clearDoors();
			}
		}
		if(effect.getSourceId() == Spearman.HYPER_BODY || effect.getSourceId() == GM.HYPER_BODY || effect.getSourceId() == SuperGM.HYPER_BODY){
			List<Pair<MapleStat, Integer>> statup = new ArrayList<>(4);
			statup.add(new Pair<>(MapleStat.HP, Math.min(hp, maxhp)));
			statup.add(new Pair<>(MapleStat.MP, Math.min(mp, maxmp)));
			statup.add(new Pair<>(MapleStat.MAXHP, maxhp));
			statup.add(new Pair<>(MapleStat.MAXMP, maxmp));
			client.announce(CWvsContext.updatePlayerStats(statup, this));
		}
		if(effect.isMonsterRiding()){
			if(effect.getSourceId() != Corsair.BATTLE_SHIP){
				this.getMount().cancelSchedule();
				this.getMount().setActive(false);
			}
		}
		if(!overwrite){
			cancelPlayerBuffs(buffstats);
		}
	}

	public void cancelEffectFromBuffStat(MapleBuffStat stat){
		MapleBuffStatDataHolder effect = effects.get(stat);
		if(effect != null){
			cancelEffect(effect.effect, false, -1);
		}
	}

	public void Hide(boolean hide, boolean login){
		if(isGM() && hide != this.hidden){
			if(!hide){
				this.hidden = false;
				announce(MaplePacketCreator.getGMEffect(AdminResult.HIDE, (byte) 0));
				List<MapleBuffStat> dsstat = Collections.singletonList(MapleBuffStat.DARKSIGHT);
				getMap().broadcastGMMessage(this, MaplePacketCreator.cancelForeignBuff(id, dsstat), false);
				getMap().broadcastMessage(this, CUserPool.spawnPlayerMapobject(this), false);
				updatePartyCharacter();
			}else{
				this.hidden = true;
				announce(MaplePacketCreator.getGMEffect(AdminResult.HIDE, (byte) 1));
				if(!login){
					getMap().broadcastMessage(this, CUserPool.removePlayerFromMap(getId()), false);
					getMap().broadcastGMMessage(this, CUserPool.spawnPlayerMapobject(this), false);
					List<Pair<MapleBuffStat, BuffDataHolder>> dsstat = Collections.singletonList(new Pair<MapleBuffStat, BuffDataHolder>(MapleBuffStat.DARKSIGHT, new BuffDataHolder(0, 0, 0)));
					getMap().broadcastGMMessage(this, MaplePacketCreator.giveForeignBuff(this, dsstat), false);
				}
				for(MapleMonster mon : this.getControlledMonsters()){
					mon.setController(null);
					mon.setControllerHasAggro(false);
					mon.setControllerKnowsAboutAggro(false);
					mon.getMap().updateMonsterController(mon);
				}
			}
			if(hidden){
				if(uiToggle) dropMessage(MessageType.TITLE, "You are now hidden");
				dropMessage(MessageType.SYSTEM, "You are now hidden");
			}else{
				if(uiToggle) dropMessage(MessageType.TITLE, "You are now visible");
				dropMessage(MessageType.SYSTEM, "You are now visible");
			}
			announce(CWvsContext.enableActions());
		}
	}

	public void Hide(boolean hide){
		Hide(hide, false);
	}

	public void toggleHide(boolean login){
		Hide(!isHidden(), login);
	}

	private void cancelFullnessSchedule(int petSlot){
		if(fullnessSchedule[petSlot] != null && !fullnessSchedule[petSlot].isCancelled()){
			fullnessSchedule[petSlot].cancel(false);
		}
	}

	public void cancelAllFullnessSchedules(){
		for(ScheduledFuture<?> fullness : fullnessSchedule){
			if(fullness != null && !fullness.isCancelled()){
				fullness.cancel(false);
			}
		}
	}

	public void cancelMagicDoor(){
		for(MapleBuffStatDataHolder mbsvh : new ArrayList<>(effects.values())){
			if(mbsvh.effect.isMagicDoor()){
				cancelEffect(mbsvh.effect, false, mbsvh.startTime);
			}
		}
	}

	public void cancelMapTimeLimitTask(){
		if(mapTimeLimitTask != null){
			mapTimeLimitTask.cancel(false);
		}
	}

	private void cancelPlayerBuffs(List<MapleBuffStat> buffstats){
		if(client.getChannelServer().getPlayerStorage().getCharacterById(getId()) != null){
			// dropMessage("recalcLocalStats() in cancelPlayerBuffs()");
			recalcLocalStats();
			enforceMaxHpMp();
			client.announce(MaplePacketCreator.cancelBuff(buffstats));
			if(buffstats.size() > 0){
				getMap().broadcastMessage(this, MaplePacketCreator.cancelForeignBuff(getId(), buffstats), false);
			}
		}
	}

	public static boolean canCreateChar(String name){
		for(String nameTest : BLOCKED_NAMES){
			if(name.toLowerCase().contains(nameTest.toLowerCase())) return false;
		}
		return getIdByName(name) < 0 && Pattern.compile("[a-zA-Z0-9]{3,12}").matcher(name).matches();
	}

	public boolean canDoor(){
		return canDoor;
	}

	public FameStatus canGiveFame(MapleCharacter from){
		if(gmLevel > 0){
			return FameStatus.OK;
		}else if(lastfametime >= System.currentTimeMillis() - 3600000 * 24){
			return FameStatus.NOT_TODAY;
		}else if(lastmonthfameids.contains(Integer.valueOf(from.getId()))){
			return FameStatus.NOT_THIS_MONTH;
		}else{
			return FameStatus.OK;
		}
	}

	public void changeCI(int type){
		this.ci = type;
	}

	public void chatBan(String fullReason){
		chatBan = true;
		try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET chatBan = ?, chatBanReason = ? WHERE id = ?")){
			ps.setInt(1, 1); // Set chatBan to 1.
			ps.setString(2, fullReason);
			ps.setInt(3, accountid);
			ps.executeUpdate();
			ps.close();
		}catch(SQLException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
	}

	public void chatBan(String fullReason, int duration){
		// Set chat ban to true so nobody can talk :)
		chatBan = true;
		try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET chatBan = ?, chatBanReason = ?, chatBanDate = CURRENT_TIMESTAMP(), chatBanDuration = ? WHERE id = ?")){
			ps.setInt(1, 1); // Set chatBan to 1.
			ps.setString(2, fullReason);
			ps.setInt(3, duration);
			ps.setInt(4, accountid);
			ps.executeUpdate();
			ps.close();
		}catch(SQLException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
		try(PreparedStatement psx = DatabaseConnection.getConnection().prepareStatement("SELECT `chatBan`, `chatBanDate`, `chatBanDuration` FROM accounts WHERE id = ?")){
			psx.setInt(1, accountid);
			ResultSet rs = psx.executeQuery();
			if(rs.next()){
				chatBan = rs.getInt("chatBan") != 0;
				chatBanDate = rs.getTimestamp("chatBanDate");
				chatBanDuration = rs.getInt("chatBanDuration");
			}
			psx.close();
		}catch(SQLException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
	}

	public void unChatBan(){
		chatBan = false;
		try(PreparedStatement psx = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET chatBan = ?, chatBanDuration = ? WHERE id = ?")){
			psx.setInt(1, 0);
			psx.setInt(2, 0);
			psx.setInt(3, accountid);
			psx.executeUpdate();
			psx.close();
		}catch(SQLException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
	}

	public void setMasteries(int jobId){
		int[] skills = new int[4];
		for(int i = 0; i > skills.length; i++){
			skills[i] = 0; // that initalization meng
		}
		if(jobId == 112){
			skills[0] = Hero.ACHILLES;
			skills[1] = Hero.MONSTER_MAGNET;
			skills[2] = Hero.BRANDISH;
		}else if(jobId == 122){
			skills[0] = Paladin.ACHILLES;
			skills[1] = Paladin.MONSTER_MAGNET;
			skills[2] = Paladin.BLAST;
		}else if(jobId == 132){
			skills[0] = DarkKnight.BEHOLDER;
			skills[1] = DarkKnight.ACHILLES;
			skills[2] = DarkKnight.MONSTER_MAGNET;
		}else if(jobId == 212){
			skills[0] = FPArchMage.BIG_BANG;
			skills[1] = FPArchMage.MANA_REFLECTION;
			skills[2] = FPArchMage.PARALYZE;
		}else if(jobId == 222){
			skills[0] = ILArchMage.BIG_BANG;
			skills[1] = ILArchMage.MANA_REFLECTION;
			skills[2] = ILArchMage.CHAIN_LIGHTNING;
		}else if(jobId == 232){
			skills[0] = Bishop.BIG_BANG;
			skills[1] = Bishop.MANA_REFLECTION;
			skills[2] = Bishop.HOLY_SHIELD;
		}else if(jobId == 312){
			skills[0] = Bowmaster.BOW_EXPERT;
			skills[1] = Bowmaster.HAMSTRING;
			skills[2] = Bowmaster.SHARP_EYES;
		}else if(jobId == 322){
			skills[0] = Marksman.MARKSMAN_BOOST;
			skills[1] = Marksman.BLIND;
			skills[2] = Marksman.SHARP_EYES;
		}else if(jobId == 412){
			skills[0] = NightLord.SHADOW_STARS;
			skills[1] = NightLord.SHADOW_SHIFTER;
			skills[2] = NightLord.VENOMOUS_STAR;
		}else if(jobId == 422){
			skills[0] = Shadower.SHADOW_SHIFTER;
			skills[1] = Shadower.VENOMOUS_STAB;
			skills[2] = Shadower.BOOMERANG_STEP;
		}else if(jobId == 512){
			skills[0] = Buccaneer.BARRAGE;
			skills[1] = Buccaneer.ENERGY_ORB;
			skills[2] = Buccaneer.SPEED_INFUSION;
			skills[3] = Buccaneer.DRAGON_STRIKE;
		}else if(jobId == 522){
			skills[0] = Corsair.ELEMENTAL_BOOST;
			skills[1] = Corsair.BULLSEYE;
			skills[2] = Corsair.WRATH_OF_THE_OCTOPI;
			skills[3] = Corsair.RAPID_FIRE;
		}else if(jobId == 2112){
			skills[0] = Aran.OVER_SWING;
			skills[1] = Aran.HIGH_MASTERY;
			skills[2] = Aran.FREEZE_STANDING;
		}else if(jobId == 2217){
			skills[0] = Evan.MAPLE_WARRIOR;
			skills[1] = Evan.ILLUSION;
		}else if(jobId == 2218){
			skills[0] = Evan.BLESSING_OF_THE_ONYX;
			skills[1] = Evan.BLAZE;
		}
		for(Integer skillId : skills){
			if(skillId != 0){
				Skill skill = SkillFactory.getSkill(skillId);
				changeSkillLevel(skill, this.getSkillLevel(skill), 10, -1);
			}
		}
	}

	public void changeJob(MapleJob newJob){
		if(newJob == null) return;// the fuck you doing idiot!
		this.job = newJob;

		if(newJob.getId() % 10 > 1){
			this.remainingAp += 5;
		}
		int job_ = job.getId() % 1000; // lame temp "fix"
		if(job_ == 100){
			maxhp += Randomizer.rand(200, 250);
		}else if(job_ == 200){
			maxmp += Randomizer.rand(100, 150);
		}else if(job_ % 100 == 0){
			maxhp += Randomizer.rand(100, 150);
			maxhp += Randomizer.rand(25, 50);
		}else if(job_ > 0 && job_ < 200){
			maxhp += Randomizer.rand(300, 350);
		}else if(job_ < 300 || job.isA(MapleJob.EVAN1)){
			maxmp += Randomizer.rand(450, 500);
		} // handle KoC here (undone)
		else if(job_ > 0 && job_ != 1000){
			maxhp += Randomizer.rand(300, 350);
			maxmp += Randomizer.rand(150, 200);
		}
		if(maxhp >= 30000){
			maxhp = 30000;
		}
		if(maxmp >= 30000){
			maxmp = 30000;
		}
		List<Pair<MapleStat, Integer>> statup = new ArrayList<>(5);
		statup.add(new Pair<>(MapleStat.MAXHP, Integer.valueOf(maxhp)));
		statup.add(new Pair<>(MapleStat.MAXMP, Integer.valueOf(maxmp)));
		statup.add(new Pair<>(MapleStat.AVAILABLEAP, remainingAp));
		// statup.add(new Pair<>(MapleStat.AVAILABLESP, remainingSp[getJobIndexBasedOnLevel()]));
		statup.add(new Pair<>(MapleStat.JOB, Integer.valueOf(job.getId())));
		if(dragon != null){
			getMap().removeMapObject(dragon.getObjectId());
			getMap().broadcastMessage(MaplePacketCreator.removeDragon(dragon.getObjectId()));
			dragon = null;
		}
		recalcLocalStats();
		client.announce(CWvsContext.updatePlayerStats(statup, this));
		// client.announce(tools.packets.CWvsContext.OnMessage.incSPMessage(getJob().getId(), 0));
		try{
			silentPartyUpdate();
		}catch(RemoteException | NullPointerException ex){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
		}
		if(this.guildid > 0){
			try{
				ChannelServer.getInstance().getWorldInterface().guildMessage(guildid, MaplePacketCreator.jobMessage(0, job.getId(), name), this.getId());
			}catch(RemoteException | NullPointerException ex){
				Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
			}
		}
		setMasteries(this.job.getId());
		guildUpdate();
		getMap().broadcastMessage(this, UserRemote.UserEffect.showForeignEffect(getId(), 8), false);
		if(getJob().isA(MapleJob.EVAN1)){
			if(getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null){
				cancelBuffStats(MapleBuffStat.MONSTER_RIDING);
			}
			createDragon();
		}
	}

	public void changeKeybinding(int key, MapleKeyBinding keybinding){
		if(keybinding.getType() != 0){
			keymap.put(Integer.valueOf(key), keybinding);
		}else{
			keymap.remove(Integer.valueOf(key));
		}
	}

	public void warpToPortal(byte portalid){
		announce(MaplePacketCreator.portalTeleport(portalid));
	}

	public void changeMap(int map){
		changeMap(map, 0);
	}

	public void changeMap(int map, int portal){
		if(map == 999999999){
			map = 10000000;
			try{
				throw new RuntimeException();
			}catch(Exception ex){
				Logger.log(LogType.ERROR, LogFile.ACCOUNT_STUCK, ex, getName() + " warped to 999999999 and was caught. Sent to henesys.");
			}
		}
		MapleMap warpMap;
		if(getEventInstance() != null){
			warpMap = getEventInstance().getMapInstance(map);
		}else{
			warpMap = client.getChannelServer().getMap(map);
		}
		if(warpMap == null) return;
		changeMap(warpMap, warpMap.getPortal(portal));
	}

	public void changeMap(int map, MaplePortal portal){// used by commands
		if(map == 999999999){
			map = 10000000;
			try{
				throw new RuntimeException();
			}catch(Exception ex){
				Logger.log(LogType.ERROR, LogFile.ACCOUNT_STUCK, ex, getName() + " warped to 999999999 and was caught. Sent to henesys.");
			}
		}
		MapleMap warpMap;
		if(getEventInstance() != null){
			warpMap = getEventInstance().getMapInstance(map);
		}else{
			warpMap = client.getChannelServer().getMap(map);
		}
		changeMap(warpMap, portal);
	}

	public void changeMap(MapleMap to){
		if(to == null) return;
		MaplePortal portal = to.getPortal(0);
		if(portal == null){
			Optional<MaplePortal> newPortal = to.getPortals().stream().findFirst();
			if(newPortal.isPresent()){
				portal = newPortal.get();
			}
		}
		changeMap(to, portal);
	}

	public void changeMap(final MapleMap to, final Point pos){
		changeMapInternal(to, pos, CStage.getWarpToMap(to, (byte) 0x80, this));// Position :O (LEFT)
	}

	public void changeMapPosition(final MapleMap to, final Point pos){
		changeMapInternal(to, pos, CStage.getWarpToMap(to, (byte) 0x80, pos, this));// Position :O (LEFT)
	}

	public void changeMap(final MapleMap to, MaplePortal pto){
		if(to == null) return;
		if(pto == null){
			pto = to.getPortal(0);
			Logger.log(LogType.WARNING, LogFile.GENERAL_ERROR, "Attempted to warp to a null portal at map: " + to.getId());
		}
		if(pto == null) return;// ?
		changeMapInternal(to, pto.getPosition(), CStage.getWarpToMap(to, pto.getId(), this));
	}

	public void changeMapPortalPosition(final MapleMap to, MaplePortal pto){
		if(to == null) return;
		if(pto == null){
			pto = to.getPortal(0);
			Logger.log(LogType.WARNING, LogFile.GENERAL_ERROR, "Attempted to warp to a null portal at map: " + to.getId());
		}
		if(pto == null) return;// ?
		changeMapInternal(to, pto.getPosition(), CStage.getWarpToMap(to, pto.getId(), pto.getPosition(), this));
	}

	public void changeMap(final MapleMap to, String targetPortal){
		if(to == null) return;
		MaplePortal pto = to.getPortal(targetPortal);
		if(pto == null){
			pto = to.getPortal(0);
			Logger.log(LogType.WARNING, LogFile.GENERAL_ERROR, "Attempted to warp to a null portal at map: " + to.getId());
		}
		changeMapInternal(to, pto.getPosition(), CStage.getWarpToMap(to, pto.getId(), this));
	}

	public void changeMapBanish(int mapid, String portal, String msg){
		if(mapid == 999999999){
			mapid = 10000000;
			try{
				throw new RuntimeException();
			}catch(Exception ex){
				Logger.log(LogType.ERROR, LogFile.ACCOUNT_STUCK, ex, getName() + " warped to 999999999 and was caught. Sent to henesys.");
			}
		}
		dropMessage(5, msg);
		MapleMap map_ = client.getChannelServer().getMap(mapid);
		if(map_ == null) return;
		changeMap(map_, map_.getPortal(portal));
	}

	private void changeMapInternal(MapleMap to, final Point pos, final byte[] warpPacket){// test map logging
		if(to == null) return;
		if(this.getTrade() != null){
			MapleTrade.cancelTrade(this);
		}
		if(timers.get("exptracker") != null){
			// if(System.currentTimeMillis() - lastExpGain >= 30 * 1000){
			long duration = lastExpGain - firstExpGain;
			if(duration >= 30 * 1000L){
				timers.get("exptracker").cancel(true);
				timers.remove("exptracker");
			}
			// }
		}
		if(eventInstance != null){
			eventInstance.changedMap(this, to.getId());
			if(eventInstance == null){// If the above eim call unregistered you from the event instance change target map.
				int mapid = to.getId();
				to = null;
				to = client.getChannelServer().getMap(mapid);
			}
		}
		if(to.getId() >= 109010000 && to.getId() <= 109010400 && getJewel() != null){
			dropMessage("Jewel is in progress.");
			getMap().broadcastMessage(MaplePacketCreator.getClock((int) getJewel().getTimeLeft() / 1000));
		}
		client.announce(warpPacket);
		if(this.getMarriedTo() > 0){
			try{
				ChannelServer.getInstance().getWorldInterface().broadcastPacket(Arrays.asList(getMarriedTo()), MaplePacketCreator.onNotifyWeddingPartnerTransfer(getId(), to.getId()));
			}catch(RemoteException | NullPointerException ex){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
			}
		}
		if(map != null){
			map.removePlayer(this);
			// TODO: Cleanup?
			if(ServerConstants.clockAll == 0 && ServerConstants.secondsLeft == 0 && !map.getMapData().hasClock() && (map.getClockHours() == 0 && map.getClockMinutes() == 0 && map.getClockSeconds() == 0)){
				map.broadcastMessage(MaplePacketCreator.removeClock());
			}
		}
		hasCheckedMapCRC = false;
		if(client.getChannelServer().getPlayerStorage().getCharacterById(getId()) != null){
			lastMapChange = Calendar.getInstance().getTimeInMillis();
			map = to;
			setStance(0);
			setPosition(pos);
			map.addPlayer(this);
			if(eventInstance != null){
				int clockTime = eventInstance.getClockTimeLeft();
				if(clockTime > 0){
					announce(MaplePacketCreator.getClock(clockTime));
				}else{
					announce(MaplePacketCreator.removeClock());
				}
			}
			if(ServerConstants.secondsLeft > 0){// TODO: Move this to maplemap?
				map.broadcastMessage(MaplePacketCreator.getClock(ServerConstants.secondsLeft));
			}else if(ServerConstants.clockAll > 0){
				map.broadcastMessage(MaplePacketCreator.getClock(ServerConstants.clockAll));
			}
			if(isInParty()){
				mpc.setMapId(to.getId());
				try{
					silentPartyUpdate();
				}catch(RemoteException | NullPointerException ex){
					Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
				}
				client.announce(CWvsContext.Party.updateParty(client.getChannel(), getParty(), PartyOperation.SILENT_UPDATE, null));
				updatePartyCharacter();
			}
			getClient().announce(MaplePacketCreator.crcStatus());
			if(getMap().getMapData().getHPDec() > 0){
				hpDecreaseTask = TimerManager.getInstance().schedule("hpDecreaseTaska", new Runnable(){

					@Override
					public void run(){
						doHurtHp();
					}
				}, 10000);
			}
		}
	}

	public void changePage(int page){
		this.currentPage = page;
	}

	public void changeSkillLevel(Skill skill, byte newLevel, int newMasterlevel, long expiration){
		if(newLevel > -1){
			skills.put(skill, new SkillEntry(newLevel, newMasterlevel, expiration));
			if(!GameConstants.isHiddenSkills(skill.getId())){
				this.client.announce(MaplePacketCreator.updateSkill(skill.getId(), newLevel, newMasterlevel, expiration));
			}
		}else{
			skills.remove(skill);
			this.client.announce(MaplePacketCreator.updateSkill(skill.getId(), newLevel, newMasterlevel, -1)); // Shouldn't use expiration anymore :)
			try{
				Connection con = DatabaseConnection.getConnection();
				try(PreparedStatement ps = con.prepareStatement("DELETE FROM skills WHERE skillid = ? AND characterid = ?")){
					ps.setInt(1, skill.getId());
					ps.setInt(2, id);
					ps.execute();
				}
			}catch(SQLException ex){
				System.out.print("Error deleting skill: " + ex);
			}
		}
	}

	public void changeTab(int tab){
		this.currentTab = tab;
	}

	public void changeType(int type){
		this.currentType = type;
	}

	public void changeSortType(byte sortType){
		this.currentSortType = sortType;
	}

	public void changeSortColumn(byte sortColumn){
		this.currentSortColumn = sortColumn;
	}

	public void checkMessenger(){
		if(messenger != null && messengerposition < 4 && messengerposition > -1){
			try{
				ChannelServer.getInstance().getWorldInterface().silentJoinMessenger(messenger.getId(), new MapleMessengerCharacter(this, messengerposition), messengerposition);
				ChannelServer.getInstance().getWorldInterface().updateMessenger(getMessenger().getId(), name, client.getChannel());
			}catch(RemoteException | NullPointerException ex){
				Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
				dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
			}
		}
	}

	public void checkMonsterAggro(MapleMonster monster){
		if(!monster.isControllerHasAggro()){
			if(monster.getController() != null && monster.getController().getId() == getId()){
				monster.setControllerHasAggro(true);
			}else{
				monster.switchController(this, true);
			}
		}
	}

	public void clearSavedLocation(SavedLocationType type){
		savedLocations[type.ordinal()] = null;
	}

	public void controlMonster(MapleMonster monster, boolean aggro){
		monster.setController(this);
		controlled.add(monster);
		client.announce(MobPool.controlMonster(monster, false, aggro));
	}

	public int countItem(int itemid){
		return inventory[ItemInformationProvider.getInstance().getInventoryType(itemid).ordinal()].countById(itemid);
	}

	public void decreaseBattleshipHp(int decrease){
		this.battleshipHp -= decrease;
		if(battleshipHp <= 0){
			this.battleshipHp = 0;
			Skill battleship = SkillFactory.getSkill(Corsair.BATTLE_SHIP);
			int cooldown = battleship.getEffect(getSkillLevel(battleship)).getCooldown();
			announce(MaplePacketCreator.skillCooldown(Corsair.BATTLE_SHIP, cooldown));
			addCooldown(Corsair.BATTLE_SHIP, System.currentTimeMillis(), cooldown, TimerManager.getInstance().schedule("decreaseBattleshipHP", new CancelCooldownAction(this, Corsair.BATTLE_SHIP), cooldown * 1000));
			removeCooldown(5221999);
			cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
		}else{
			announce(MaplePacketCreator.skillCooldown(5221999, battleshipHp / 10)); // :D
			addCooldown(5221999, 0, battleshipHp, null);
		}
	}

	public void decreaseReports(){
		this.possibleReports--;
	}

	public void deleteGuild(int guildId){
		try{
			Connection con = DatabaseConnection.getConnection();
			try(PreparedStatement ps = con.prepareStatement("UPDATE characters SET guildid = 0, guildrank = 5 WHERE guildid = ?")){
				ps.setInt(1, guildId);
				ps.execute();
			}
			try(PreparedStatement ps = con.prepareStatement("DELETE FROM guilds WHERE guildid = ?")){
				ps.setInt(1, id);
				ps.execute();
			}
		}catch(SQLException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
	}

	private void deleteWhereCharacterId(Connection con, String sql) throws SQLException{
		try(PreparedStatement ps = con.prepareStatement(sql)){
			ps.setInt(1, id);
			ps.executeUpdate();
		}
	}

	public static void deleteWhereCharacterId(Connection con, String sql, int cid) throws SQLException{
		try(PreparedStatement ps = con.prepareStatement(sql)){
			ps.setInt(1, cid);
			ps.executeUpdate();
		}
	}

	private void deregisterBuffStats(List<MapleBuffStat> stats){
		synchronized(stats){
			for(MapleBuffStat stat : stats){
				if(stat.equals(MapleBuffStat.DASH_JUMP)){
					TwoStateTemporaryStat pStat = (TwoStateTemporaryStat) secondaryStat.getTemporaryState(TSIndex.DashJump.getIndex());
					pStat.nOption = 0;
					pStat.rOption = 0;
					pStat.tLastUpdated = System.currentTimeMillis();
					secondaryStat.setTemporaryState(TSIndex.DashJump.getIndex(), pStat);
				}else if(stat.equals(MapleBuffStat.DASH_SPEED)){
					TwoStateTemporaryStat pStat = (TwoStateTemporaryStat) secondaryStat.getTemporaryState(TSIndex.DashSpeed.getIndex());
					pStat.nOption = 0;
					pStat.rOption = 0;
					pStat.tLastUpdated = System.currentTimeMillis();
					secondaryStat.setTemporaryState(TSIndex.DashSpeed.getIndex(), pStat);
				}
				MapleBuffStatDataHolder mbsvh = effects.get(stat);
				if(mbsvh != null){
					if(stat == MapleBuffStat.COMBO) setBuffedValue(MapleBuffStat.COMBO, 1);
					effects.remove(stat);
					if(stat == MapleBuffStat.RECOVERY){
						if(recoveryTask != null){
							recoveryTask.cancel(false);
							recoveryTask = null;
						}
					}else if(stat == MapleBuffStat.SUMMON || stat == MapleBuffStat.PUPPET){
						int summonId = mbsvh.effect.getSourceId();
						MapleSummon summon = summons.get(summonId);
						if(summon != null){
							getMap().broadcastMessage(SummonedPool.removeSummon(summon, true), summon.getPosition());
							getMap().removeMapObject(summon);
							removeVisibleMapObject(summon);
							summons.remove(summonId);
							if(summon.getSkill() == DarkKnight.BEHOLDER){
								// TODO: Cancel buffs?
							}
						}
					}else if(stat == MapleBuffStat.DRAGONBLOOD){
						dragonBloodSchedule.cancel(false);
						dragonBloodSchedule = null;
					}
				}
			}
		}
	}

	public void disableDoor(){
		canDoor = false;
		TimerManager.getInstance().schedule("disableDoor", new Runnable(){

			@Override
			public void run(){
				canDoor = true;
			}
		}, 5000);
	}

	public void disbandGuild(){
		if(guildid < 1 || guildrank != 1) return;
		try{
			ChannelServer.getInstance().getWorldInterface().disbandGuild(guildid);
		}catch(RemoteException | NullPointerException ex){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
		}
	}

	public void dispel(){
		for(MapleBuffStatDataHolder mbsvh : new ArrayList<>(effects.values())){
			if(mbsvh.effect.isSkill()){
				cancelEffect(mbsvh.effect, false, mbsvh.startTime);
			}
		}
	}

	public final boolean hasDisease(final MapleBuffStat dis){
		for(final MapleBuffStat disease : effects.keySet()){
			if(disease == dis) return true;
		}
		return false;
	}

	public void dispelDebuffs(){
		List<MapleBuffStat> toDispel = new ArrayList<>();
		toDispel.add(MapleBuffStat.CURSE);
		toDispel.add(MapleBuffStat.DARKNESS);
		toDispel.add(MapleBuffStat.POISON);
		toDispel.add(MapleBuffStat.SEAL);
		toDispel.add(MapleBuffStat.WEAKEN);
		this.cancelPlayerBuffs(toDispel);
	}

	public void cancelAllDebuffs(){
		List<MapleBuffStat> toDispel = new ArrayList<>();
		toDispel.add(MapleBuffStat.CURSE);
		toDispel.add(MapleBuffStat.DARKNESS);
		toDispel.add(MapleBuffStat.POISON);
		toDispel.add(MapleBuffStat.SEAL);
		toDispel.add(MapleBuffStat.WEAKEN);
		toDispel.add(MapleBuffStat.SEDUCE);
		toDispel.add(MapleBuffStat.CONFUSE);
		this.cancelPlayerBuffs(toDispel);
	}

	public void dispelSkill(int skillid){
		LinkedList<MapleBuffStatDataHolder> allBuffs = new LinkedList<>(effects.values());
		for(MapleBuffStatDataHolder mbsvh : allBuffs){
			if(skillid == 0){
				if(mbsvh.effect.isSkill() && (mbsvh.effect.getSourceId() % 10000000 == 1004 || dispelSkills(mbsvh.effect.getSourceId()))){
					cancelEffect(mbsvh.effect, false, mbsvh.startTime);
				}
			}else if(mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skillid){
				cancelEffect(mbsvh.effect, false, mbsvh.startTime);
			}
		}
	}

	private boolean dispelSkills(int skillid){
		switch (skillid){
			case DarkKnight.BEHOLDER:
			case FPArchMage.ELQUINES:
			case ILArchMage.IFRIT:
			case Priest.SUMMON_DRAGON:
			case Bishop.BAHAMUT:
			case Ranger.PUPPET:
			case Ranger.SILVER_HAWK:
			case Sniper.PUPPET:
			case Sniper.GOLDEN_EAGLE:
			case Hermit.SHADOW_PARTNER:
				return true;
			default:
				return false;
		}
	}

	public void doHurtHp(){
		if(this.getInventory(MapleInventoryType.EQUIPPED).findById(getMap().getMapData().getHPDecProtect()) != null) return;
		addHP(-getMap().getMapData().getHPDec());
		hpDecreaseTask = TimerManager.getInstance().schedule("hpDecreaseTask", new Runnable(){

			@Override
			public void run(){
				doHurtHp();
			}
		}, 10000);
	}

	public void dropMessage(String message){
		dropMessage(0, message);
	}

	public void dropMessage(int type, String message){
		dropMessage(MessageType.getType(type), message);
	}

	public void dropMessage(MessageType type, MessageBuilder message){
		for(String str : message.getContent().split("\n")){
			dropMessage(type, str);
		}
	}

	public void dropMessage(MessageType type, String message){
		switch (type){
			case POPUP:
				client.announce(MaplePacketCreator.serverNotice(type.getValue(), message));
				client.announce(CWvsContext.enableActions());
				break;
			case NOTICE:
			case MEGAPHONE:
			case SUPER_MEGAPHONE:
			case SERVER_NOTICE:
			case ERROR:
			case SYSTEM:
				client.announce(MaplePacketCreator.serverNotice(type.getValue(), message));
				break;
			case MAPLETIP:
				yellowMessage(message);
				break;
			case TITLE:
				client.announce(MaplePacketCreator.earnTitleMessage(message));
				break;
			default:
				client.announce(MaplePacketCreator.serverNotice(MessageType.SYSTEM.getValue(), message));
		}
	}

	public String emblemCost(){
		return nf.format(MapleGuild.CHANGE_EMBLEM_COST);
	}

	public Map<String, ScheduledFuture<?>> getTimers(){
		return timers;
	}

	public void addTimer(String key, ScheduledFuture<?> future){
		timers.put(key, future);
	}

	public void removeTimer(String key){
		ScheduledFuture<?> old = timers.get(key);
		if(old != null){
			old.cancel(true);
			old = null;
			timers.remove(key);
		}
	}

	private void enforceMaxHpMp(){
		List<Pair<MapleStat, Integer>> stats = new ArrayList<>(2);
		if(getMp() > getCurrentMaxMp()){
			setMp(getMp());
			stats.add(new Pair<>(MapleStat.MP, Integer.valueOf(getMp())));
		}
		if(getHp() > getCurrentMaxHp()){
			setHp(getHp());
			stats.add(new Pair<>(MapleStat.HP, Integer.valueOf(getHp())));
		}
		if(stats.size() > 0){
			client.announce(CWvsContext.updatePlayerStats(stats, this));
		}
	}

	public void enteredScript(String script, int mapid){
		if(!entered.containsKey(mapid)){
			entered.put(mapid, script);
		}
	}

	public void equipChanged(){
		getMap().broadcastMessage(this, UserRemote.updateCharLook(this), false);
		recalcLocalStats();
		enforceMaxHpMp();
		if(getMessenger() != null){
			try{
				ChannelServer.getInstance().getWorldInterface().updateMessenger(getMessenger().getId(), getName(), getWorld(), client.getChannel());
			}catch(RemoteException | NullPointerException ex){
				Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
				dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
			}
		}
	}

	public void cancelExpirationTask(){
		if(expiretask != null){
			expiretask.cancel(false);
			expiretask = null;
		}
	}

	public void expirationTask(){
		if(expiretask == null){
			expiretask = TimerManager.getInstance().register("expireTask", new Runnable(){

				@Override
				public void run(){
					if(!loggedIn){
						empty(false);
						return;
					}
					long expiration, lockExpiration, currenttime = System.currentTimeMillis();
					Set<Skill> keys = getSkills().keySet();
					for(Iterator<Skill> i = keys.iterator(); i.hasNext();){
						Skill key = i.next();
						SkillEntry skill = getSkills().get(key);
						if(skill.expiration != -1 && skill.expiration < currenttime){
							changeSkillLevel(key, (byte) -1, 0, -1);
						}
					}
					for(MapleInventory inv : inventory){
						List<Item> toberemove = new ArrayList<>();
						for(Item item : inv.list()){
							if(item.getPet() != null){
								for(MaplePet pet : pets){
									if(pet == null) continue;
									if(pet.getUniqueId() == item.getPet().getUniqueId()){
										int nRemainLife = item.getPet().getRemainLife();
										if(nRemainLife > 0){
											nRemainLife -= 60000 / 1000L;
											if(nRemainLife == 0) nRemainLife--;
											item.getPet().setRemainLife(nRemainLife);
											forceUpdateItem(item);
										}
									}
								}
							}
							expiration = item.getExpiration();
							lockExpiration = item.getLockExpiration();
							if(lockExpiration != -1 && (lockExpiration < currenttime) && ((item.getFlag() & ItemConstants.LOCK) == ItemConstants.LOCK)){
								byte aids = item.getFlag();
								aids &= ~(ItemConstants.LOCK);
								item.setFlag(aids); // Probably need a check, else people can make expiring items into permanent items...
								item.setLockExpiration(-1);
								forceUpdateItem(item); // TEST :3
							}
							if((expiration != -1 && expiration < currenttime) || (item.getPet() != null && item.getPet().getRemainLife() < 0)){
								if((item.getPet() != null && item.getPet().getRemainLife() < 0) || item.getPet() == null){
									client.announce(CWvsContext.OnMessage.itemExpired(item.getItemId()));
									if(item.getPet() != null) unequipPet(item.getPet(), false);
									toberemove.add(item);
								}else{
									item.setExpiration(-1);
									unequipPet(item.getPet(), false);
									forceUpdateItem(item);
								}
							}
						}
						for(Item item : toberemove){
							if(item.getItemId() == 1122017) unequipPendantOfSpirit();
							MapleInventoryManipulator.removeItem(client, inv.getType(), item.getPosition(), item.getQuantity(), true, true);
						}
					}
					getStats().recalcLocalStats(MapleCharacter.this);
				}
			}, 60000);
		}
	}

	public enum FameStatus{
		OK,
		NOT_TODAY,
		NOT_THIS_MONTH
	}

	public void forceUpdateItem(Item item){
		final List<ModifyInventory> mods = new LinkedList<>();
		mods.add(new ModifyInventory(3, item));
		mods.add(new ModifyInventory(0, item));
		client.announce(MaplePacketCreator.modifyInventory(true, mods));
	}

	public void gainGachaExp(){
		int expgain = 0;
		int currentgexp = gachaexp.get();
		if((currentgexp + exp.get()) >= ExpTable.getExpNeededForLevel(level)){
			expgain += ExpTable.getExpNeededForLevel(level) - exp.get();
			int nextneed = ExpTable.getExpNeededForLevel(level + 1);
			if((currentgexp - expgain) >= nextneed){
				expgain += nextneed;
			}
			this.gachaexp.set(currentgexp - expgain);
		}else{
			expgain = this.gachaexp.getAndSet(0);
		}
		gainExp(new ExpProperty(ExpGainType.GACHA).gain(expgain));
		updateSingleStat(MapleStat.GACHAEXP, this.gachaexp.get());
	}

	public void gainGachaExp(int gain){
		updateSingleStat(MapleStat.GACHAEXP, gachaexp.addAndGet(gain));
	}


	// public void gainExp(int gain, boolean show, boolean inChat){
	// public void gainExp(int gain, int party, boolean show, boolean inChat, boolean white){
	public void gainExp(String gainType, int gain, String data, boolean inChat){
		gainExp(new ExpProperty(ExpGainType.valueOf(gainType)).inChat(inChat).gain(gain).show(true).logData(data));
	}

	public void gainExp(ExpProperty property){
		getStats().recalcLocalStats(this);
		if(this.getBuffedValue(MapleBuffStat.CURSE) != null){
			property.gain *= 0.5;
			property.party *= 0.5;
		}
		if(pendantExp > 0) property.equip((property.gain / 10) * pendantExp);
		if(property.type == ExpGainType.MONSTER){
			int bonus = (int) (property.gain * this.getStats().getBonusExpBuff() / 100);
			if(getStats().getBonusExpBuff() != 0){
				property.logData(" BonusEvent: " + bonus);
				property.bonusEvent = bonus;
			}
		}
		if(property.type == ExpGainType.PARTYQUEST){
			int partyBonus = (int) (property.gain * getStats().getPartyExpBuff() / 100);
			if(getStats().getPartyExpBuff() != 0){
				property.logData(" PartyBonus: " + partyBonus);
				property.party(partyBonus);
			}
		}
		int total = property.gain + property.equip + property.party + property.bonusEvent + property.cafe + property.rainbow + property.wedding;
		if(property.type == ExpGainType.MONSTER){
			this.expGained += property.gain;
			this.lastExpGain = System.currentTimeMillis();
			if(timers.get("exptracker") == null){
				this.expGained = property.gain;// don't count previous exp gains
				this.firstExpGain = System.currentTimeMillis();
				timers.put("exptracker", TimerManager.getInstance().register("exptracker", ()-> {
					if(System.currentTimeMillis() - lastExpGain >= 30 * 1000){
						long duration = lastExpGain - firstExpGain;
						timers.get("exptracker").cancel(true);
						timers.remove("exptracker");
					}
				}, 30 * 1000));
			}
		}else{
			Logger.log(LogType.INFO, LogFile.EXP_LOG, getClient().getAccountName(), getName() + " gained " + total + " exp Via: " + property.type.name() + " Data: " + property.logData);
		}
		if(property.type == ExpGainType.MONSTER){
			battleAnaylsis.addExp(total);
		}
		if(level < getMaxLevel()){
			if((long) this.exp.get() + (long) total > (long) Integer.MAX_VALUE){
				int gainFirst = ExpTable.getExpNeededForLevel(level) - this.exp.get();
				total -= gainFirst + 1;
				this.gainExp(property.clone().gain(gainFirst + 1).show(false));
			}
			updateSingleStat(MapleStat.EXP, this.exp.addAndGet(total));
			if(property.show && property.gain != 0){
				client.announce(CWvsContext.OnMessage.getShowExpGain(property));
			}
			if(exp.get() >= ExpTable.getExpNeededForLevel(level)){
				levelUp(true);
				int need = ExpTable.getExpNeededForLevel(level);
				if(exp.get() >= need){
					setExp(need - 1);
					updateSingleStat(MapleStat.EXP, need);
				}
			}
		}
	}

	public void loseExp(int lose, boolean loseLevel){
		lose = Math.abs(lose);
		if(exp.get() >= lose){
			setExp(exp.get() - lose);
			updateSingleStat(MapleStat.EXP, getExp());
		}else{
			if(level > 1){
				if(loseLevel){
					int leftOver = lose - exp.get();
					levelDown();
					setExp(ExpTable.getExpNeededForLevel(level) - leftOver);
					updateSingleStat(MapleStat.EXP, getExp());
				}else{
					setExp(0);
					updateSingleStat(MapleStat.EXP, getExp());
				}
			}else{
				setExp(0);
				updateSingleStat(MapleStat.EXP, getExp());
			}
		}
	}

	public void gainFame(int delta){
		this.addFame(delta);
		this.updateSingleStat(MapleStat.FAME, this.fame);
	}

	public void gainMeso(int gain, boolean show){
		gainMeso(gain, show, false, false);
	}

	public void gainMeso(int gain, boolean show, boolean enableActions, boolean inChat){
		if(meso.get() + gain < 0){
			client.announce(CWvsContext.enableActions());
			return;
		}
		updateSingleStat(MapleStat.MESO, meso.addAndGet(gain), enableActions);
		if(show){
			client.announce(CWvsContext.OnMessage.getShowMesoGain(gain, inChat));
		}
	}

	public void genericGuildMessage(int code){
		this.client.announce(MaplePacketCreator.genericGuildMessage((byte) code));
	}

	public int getAccountID(){
		return accountid;
	}

	public List<PlayerBuffValueHolder> getAllBuffs(){
		List<PlayerBuffValueHolder> ret = new ArrayList<>();
		for(MapleBuffStatDataHolder mbsvh : effects.values()){
			ret.add(new PlayerBuffValueHolder(mbsvh.startTime, mbsvh.duration, mbsvh.effect));
		}
		return ret;
	}

	public List<Pair<MapleBuffStat, BuffDataHolder>> getAllStatups(){
		List<Pair<MapleBuffStat, BuffDataHolder>> ret = new ArrayList<>();
		for(MapleBuffStat mbs : effects.keySet()){
			MapleBuffStatDataHolder mbsvh = effects.get(mbs);
			ret.add(new Pair<>(mbs, mbsvh.value));
		}
		return ret;
	}

	public List<PlayerCoolDownValueHolder> getAllCooldowns(){
		List<PlayerCoolDownValueHolder> ret = new ArrayList<>();
		for(MapleCoolDownValueHolder mcdvh : coolDowns.values()){
			ret.add(new PlayerCoolDownValueHolder(mcdvh.skillId, mcdvh.startTime, mcdvh.length));
		}
		return ret;
	}

	public int getAllianceRank(){
		return this.allianceRank;
	}

	public static String getAriantRoomLeaderName(int room){
		return ariantroomleader[room];
	}

	public static int getAriantSlotsRoom(int room){
		return ariantroomslot[room];
	}

	public int getBattleshipHp(){
		return battleshipHp;
	}

	public BuddyList getBuddylist(){
		return buddylist;
	}

	public static Map<String, String> getCharacterFromDatabase(String name){
		Map<String, String> character = new LinkedHashMap<>();
		try{
			try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT `id`, `accountid`, `name` FROM `characters` WHERE `name` = ?")){
				ps.setString(1, name);
				try(ResultSet rs = ps.executeQuery()){
					if(!rs.next()){
						rs.close();
						ps.close();
						return null;
					}
					for(int i = 1; i <= rs.getMetaData().getColumnCount(); i++){
						character.put(rs.getMetaData().getColumnLabel(i), rs.getString(i));
					}
				}
			}
		}catch(SQLException sqle){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, sqle);
		}
		return character;
	}

	public Long getBuffedStarttime(MapleBuffStat effect){
		MapleBuffStatDataHolder mbsvh = effects.get(effect);
		if(mbsvh == null) return null;
		return Long.valueOf(mbsvh.startTime);
	}

	public Integer getBuffedValue(MapleBuffStat effect){
		MapleBuffStatDataHolder mbsvh = effects.get(effect);
		if(mbsvh == null) return null;
		return Integer.valueOf(mbsvh.value.getValue());
	}

	public int getBuffSource(MapleBuffStat stat){
		MapleBuffStatDataHolder mbsvh = effects.get(stat);
		if(mbsvh == null) return -1;
		return mbsvh.effect.getSourceId();
	}

	public MapleStatEffect getBuffEffect(MapleBuffStat stat){
		MapleBuffStatDataHolder mbsvh = effects.get(stat);
		if(mbsvh == null){
			return null;
		}else{
			return mbsvh.effect;
		}
	}

	private List<MapleBuffStat> getBuffStats(MapleStatEffect effect, long startTime){
		List<MapleBuffStat> stats = new ArrayList<>();
		for(Entry<MapleBuffStat, MapleBuffStatDataHolder> stateffect : effects.entrySet()){
			if(stateffect == null || stateffect.getValue() == null || stateffect.getValue().effect == null) continue;
			if(stateffect.getValue().effect.sameSource(effect) && (startTime == -1 || startTime == stateffect.getValue().startTime)){
				stats.add(stateffect.getKey());
			}
		}
		return stats;
	}

	public int getChair(){
		return chair;
	}

	public String getChalkboard(){
		return this.chalktext;
	}

	public MapleClient getClient(){
		return client;
	}

	public final List<MapleQuestStatus> getCompletedQuests(){
		List<MapleQuestStatus> ret = new LinkedList<>();
		for(MapleQuestStatus q : quests.values()){
			if(q.getStatus().equals(MapleQuestStatus.Status.COMPLETED)){
				ret.add(q);
			}
		}
		return Collections.unmodifiableList(ret);
	}

	public Collection<MapleMonster> getControlledMonsters(){
		return Collections.unmodifiableCollection(controlled);
	}

	public List<MapleRing> getCrushRings(){
		if(crushRings.size() > 1) Collections.sort(crushRings);
		return crushRings;
	}

	public int getCurrentCI(){
		return ci;
	}

	public int getCurrentPage(){
		return currentPage;
	}

	public int getCurrentMaxHp(){
		return localmaxhp;
	}

	public int getCurrentMaxMp(){
		return localmaxmp;
	}

	public int getCurrentTab(){
		return currentTab;
	}

	public int getCurrentType(){
		return currentType;
	}

	public byte getCurrentSortType(){
		return currentSortType;
	}

	public byte getCurrentSortColumn(){
		return currentSortColumn;
	}

	public int getDex(){
		return dex;
	}

	public int getDojoEnergy(){
		return dojoEnergy;
	}

	public boolean getDojoParty(){
		return dojoParty;
	}

	public int getDojoPoints(){
		return dojoPoints;
	}

	public int getDojoStage(){
		return dojoStage;
	}

	public List<MapleDoor> getDoors(){
		return new ArrayList<>(doors);
	}

	public void clearDoors(){
		doors.clear();
		if(this.mpc != null){
			mpc.clearDoors();
			try{
				ChannelServer.getInstance().getWorldInterface().updateParty(getPartyId(), PartyOperation.UPDATE_DOOR, mpc);
			}catch(RemoteException | NullPointerException ex){
				Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
			}
		}
	}

	public int getEnergyBar(){
		return energybar;
	}

	public EventInstanceManager getEventInstance(){
		return eventInstance;
	}

	public int getExp(){
		return exp.get();
	}

	public int getGachaExp(){
		return gachaexp.get();
	}

	public int getFace(){
		return face;
	}

	public int getFame(){
		return fame;
	}

	public int getFamilyId(){
		return familyId;
	}

	public boolean getFinishedDojoTutorial(){
		return finishedDojoTutorial;
	}

	public List<MapleRing> getFriendshipRings(){
		Collections.sort(friendshipRings);
		return friendshipRings;
	}

	public int getGender(){
		return gender;
	}

	public boolean isMale(){
		return getGender() == 0;
	}

	public MapleGuild getGuild(){
		try{
			return ChannelServer.getInstance().getWorldInterface().getGuild(getGuildId(), null);
		}catch(Exception ex){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
			return null;
		}
	}

	public MapleGuild getGuildIfExists(){
		try{
			return ChannelServer.getInstance().getWorldInterface().getGuildIfExists(getGuildId());
		}catch(Exception ex){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
			return null;
		}
	}

	public int getGuildId(){
		return guildid;
	}

	public int getGuildRank(){
		return guildrank;
	}

	public int getHair(){
		return hair;
	}

	public HiredMerchant getHiredMerchant(){
		return hiredMerchant;
	}

	public int getHp(){
		return hp;
	}

	public int getHpMpApUsed(){
		return hpMpApUsed;
	}

	public int getId(){
		return id;
	}

	public static int getIdByName(String name){
		try{
			int id;
			try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT id FROM characters WHERE name = ? AND deleted = 0")){
				ps.setString(1, name);
				try(ResultSet rs = ps.executeQuery()){
					if(!rs.next()){
						rs.close();
						ps.close();
						return -1;
					}
					id = rs.getInt("id");
				}
			}
			return id;
		}catch(Exception e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
		return -1;
	}

	public static int getAccIdByName(String name){
		try{
			int id;
			try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT accountid FROM characters WHERE name = ? AND deleted = 0")){
				ps.setString(1, name);
				try(ResultSet rs = ps.executeQuery()){
					if(!rs.next()){
						rs.close();
						ps.close();
						return -1;
					}
					id = rs.getInt("accountid");
				}
			}
			return id;
		}catch(Exception e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
		return -1;
	}

	public static String getNameById(int id){
		try{
			String name;
			try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT name FROM characters WHERE id = ? AND deleted = 0")){
				ps.setInt(1, id);
				try(ResultSet rs = ps.executeQuery()){
					if(!rs.next()){
						rs.close();
						ps.close();
						return null;
					}
					name = rs.getString("name");
				}
			}
			return name;
		}catch(Exception e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
		return null;
	}

	public int getInitialSpawnpoint(){
		return initialSpawnPoint;
	}

	public int getInt(){
		return int_;
	}

	public MapleInventory getInventory(MapleInventoryType type){
		return inventory[type.ordinal()];
	}

	public int getItemEffect(){
		return itemEffect;
	}

	public int getItemQuantity(int itemid, boolean checkEquipped){
		int possesed = inventory[ItemInformationProvider.getInstance().getInventoryType(itemid).ordinal()].countById(itemid);
		if(checkEquipped){
			possesed += inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid);
		}
		return possesed;
	}

	public MapleJob getJob(){
		return job;
	}

	public int getJobRank(){
		return jobRank;
	}

	public int getJobRankMove(){
		return jobRankMove;
	}

	public int getJobType(){
		return job.getId() / 1000;
	}

	public Map<Integer, MapleKeyBinding> getKeymap(){
		return keymap;
	}

	public long getLastHealed(){
		return lastHealed;
	}

	public long getLastUsedCashItem(){
		return lastUsedCashItem;
	}

	public long getLastUsedSay(){
		return lastUsedSay;
	}

	public int getLevel(){
		return level;
	}

	public int getLuk(){
		return luk;
	}

	public int getFh(){
		Point pos = this.getPosition();
		pos.y -= 6;
		if(getMap().getMapData().getFootholds().findBelow(pos) == null){
			return 0;
		}else{
			return getMap().getMapData().getFootholds().findBelow(pos).getY1();
		}
	}

	public MapleMap getMap(){
		return map;
	}

	public int getMapId(){
		if(map != null) return map.getId();
		return mapid;
	}

	public int getMarkedMonster(){
		return markedMonster;
	}

	public int getMasterLevel(Skill skill){
		if(skills.get(skill) == null) return 0;
		return skills.get(skill).masterlevel;
	}

	public int getMaxHp(){
		return maxhp;
	}

	public int getMaxLevel(){
		return isGM() ? 256 : isCygnus() ? 120 : getReincarnations() > 0 ? 250 : 200;
	}

	public int getMaxMp(){
		return maxmp;
	}

	public int getMeso(){
		return meso.get();
	}

	public int getMerchantMeso(){
		return merchantmeso;
	}

	public int getMesosTraded(){
		return mesosTraded;
	}

	public int getMessengerPosition(){
		return messengerposition;
	}

	public MapleGuildCharacter getMGC(){
		return mgc;
	}

	public MaplePartyCharacter getMPC(){
		if(mpc == null){
			mpc = new MaplePartyCharacter(this);
		}
		return mpc;
	}

	public void setMPC(MaplePartyCharacter mpc){
		this.mpc = mpc;
	}

	public MapleMiniGame getMiniGame(){
		return miniGame;
	}

	public int getMiniGamePoints(String type, boolean omok){
		if(omok){
			switch (type){
				case "wins":
					return omokwins;
				case "losses":
					return omoklosses;
				default:
					return omokties;
			}
		}else{
			switch (type){
				case "wins":
					return matchcardwins;
				case "losses":
					return matchcardlosses;
				default:
					return matchcardties;
			}
		}
	}

	public MonsterBook getMonsterBook(){
		return monsterbook;
	}

	public int getMonsterBookCover(){
		return bookCover;
	}

	public MapleMount getMount(){
		return maplemount;
	}

	public int getMp(){
		return mp;
	}

	public MapleMessenger getMessenger(){
		return messenger;
	}

	public String getName(){
		return name;
	}


	public String getReadableName(){
		return MapleCharacter.makeMapleReadable(name);
	}

	public int getNextEmptyPetIndex(){
		for(int i = 0; i < 3; i++){
			if(pets[i] == null) return i;
		}
		return 3;
	}

	public int getNoPets(){
		int ret = 0;
		for(int i = 0; i < 3; i++){
			if(pets[i] != null){
				ret++;
			}
		}
		return ret;
	}

	public int getNumControlledMonsters(){
		return controlled.size();
	}

	public boolean isInParty(){
		return partyid >= 0;
	}

	public MapleParty getParty(){
		try{
			return ChannelServer.getInstance().getWorldInterface().getParty(getPartyId());
		}catch(Exception e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
			dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
		}
		return null;
	}

	public int getPartyId(){
		return partyid;
	}

	public MaplePlayerShop getPlayerShop(){
		return playerShop;
	}

	public MaplePet[] getPets(){
		return pets;
	}

	public MaplePet getPet(int index){
		return pets[index];
	}

	public byte getPetIndex(long petId){
		for(byte i = 0; i < 3; i++){
			if(pets[i] != null){
				if(pets[i].getUniqueId() == petId) return i;
			}
		}
		return -1;
	}

	public byte getPetIndex(MaplePet pet){
		for(byte i = 0; i < 3; i++){
			if(pets[i] != null){
				if(pets[i].getUniqueId() == pet.getUniqueId()) return i;
			}
		}
		return -1;
	}

	public int getPossibleReports(){
		return possibleReports;
	}

	public final byte getQuestStatus(final int quest){
		for(final MapleQuestStatus q : quests.values()){
			if(q.getQuest().getId() == quest) return (byte) q.getStatus().getId();
		}
		return 0;
	}

	public MapleQuestStatus getQuest(MapleQuest quest){
		if(!quests.containsKey(quest.getId())) return new MapleQuestStatus(quest, MapleQuestStatus.Status.NOT_STARTED);
		return quests.get(quest.getId());
	}

	public boolean needQuestItem(int questid, int itemid){
		if(questid <= 0) return true; // For non quest items :3
		MapleQuest quest = MapleQuest.getInstance(questid);
		if(getQuestStatus(questid) != 1) return false;
		return getInventory(ItemConstants.getInventoryType(itemid)).countById(itemid) < quest.getItemAmountNeeded(itemid);
	}

	public int getRank(){
		return rank;
	}

	public int getRankMove(){
		return rankMove;
	}

	public int getRemainingAp(){
		return remainingAp;
	}

	public int getRemainingSp(){
		return remainingSp[getJobIndexBasedOnLevel()]; // default
	}

	public int getRemainingSpBySkill(final int skillbook){
		return remainingSp[skillbook];
	}

	public int[] getRemainingSps(){
		return remainingSp;
	}

	public SavedLocation getSavedLocation(String type){
		SavedLocation sl = savedLocations[SavedLocationType.fromString(type).ordinal()];
		if(sl == null) return null;
		if(!SavedLocationType.fromString(type).equals(SavedLocationType.WORLDTOUR)){
			clearSavedLocation(SavedLocationType.fromString(type));
		}
		return sl;
	}

	public String getSearch(){
		return search;
	}

	public MapleShop getShop(){
		return shop;
	}

	public Map<Skill, SkillEntry> getSkills(){
		return Collections.unmodifiableMap(skills);
	}

	public int getSkillLevel(int skill){
		return getSkillLevel(SkillFactory.getSkill(skill));
	}

	public byte getSkillLevel(Skill skill){
		if(skill == null) return 0;
		SkillEntry entry = skills.get(skill);
		if(entry == null) return 0;
		return entry.skillevel;
	}

	public long getSkillExpiration(int skill){
		SkillEntry ret = skills.get(SkillFactory.getSkill(skill));
		if(ret == null) return -1;
		return ret.expiration;
	}

	public long getSkillExpiration(Skill skill){
		if(skills.get(skill) == null) return -1;
		return skills.get(skill).expiration;
	}

	public MapleSkinColor getSkinColor(){
		return skinColor;
	}

	public int getSlot(){
		return slots;
	}

	public final List<MapleQuestStatus> getStartedQuests(){
		List<MapleQuestStatus> ret = new LinkedList<>();
		for(MapleQuestStatus q : quests.values()){
			if(q.getStatus().equals(MapleQuestStatus.Status.STARTED)){
				ret.add(q);
			}
		}
		return Collections.unmodifiableList(ret);
	}

	public final int getStartedQuestsSize(){
		int i = 0;
		for(MapleQuestStatus q : quests.values()){
			if(q.getStatus().equals(MapleQuestStatus.Status.STARTED)){
				if(q.getQuest().startQuestData.infoNumber > 0){
					i++;
				}
				i++;
			}
		}
		return i;
	}

	public MapleStatEffect getStatForBuff(MapleBuffStat effect){
		MapleBuffStatDataHolder mbsvh = effects.get(effect);
		if(mbsvh == null) return null;
		return mbsvh.effect;
	}

	public MapleStorage getStorage(){
		return storage;
	}

	public int getStr(){
		return str;
	}

	public Map<Integer, MapleSummon> getSummons(){
		return summons;
	}

	public int getTotalStr(){
		return localstr;
	}

	public int getTotalDex(){
		return localdex;
	}

	public int getTotalInt(){
		return localint;
	}

	public int getTotalLuk(){
		return localluk;
	}

	public int getTotalMagic(){
		return magic;
	}

	public int getTotalWatk(){
		return watk;
	}

	public MapleTrade getTrade(){
		return trade;
	}

	public int getVanquisherKills(){
		return vanquisherKills;
	}

	public int getVanquisherStage(){
		return vanquisherStage;
	}

	public Collection<MapleMapObject> getVisibleMapObjects(){
		return visibleMapObjects;
	}

	public int getWorld(){
		return world;
	}

	public void giveCoolDowns(final int skillid, long starttime, long length){
		if(skillid == 5221999){
			this.battleshipHp = (int) length;
			addCooldown(skillid, 0, length, null);
		}else{
			int time = (int) ((length + starttime) - System.currentTimeMillis());
			addCooldown(skillid, System.currentTimeMillis(), time, TimerManager.getInstance().schedule("cancelCooldownAction", new CancelCooldownAction(this, skillid), time));
		}
	}

	public int getGMLevel(){
		return gmLevel;
	}

	public PlayerGMRank getGMRank(){
		return PlayerGMRank.getByLevel(gmLevel);
	}

	public boolean isController(){
		return gmLevel >= PlayerGMRank.CONTROLLER.getLevel();
	}

	public boolean isAdmin(){
		return gmLevel >= PlayerGMRank.ADMIN.getLevel();
	}

	public boolean isSuperGM(){
		return gmLevel >= PlayerGMRank.SUPERGM.getLevel();
	}

	public boolean isGM(){
		return gmLevel >= PlayerGMRank.GM.getLevel();
	}

	public boolean isIntern(){
		return gmLevel >= PlayerGMRank.INTERN.getLevel();
	}

	public boolean isElite(){
		return getClient().checkEliteStatus();
	}

	public String guildCost(){
		return nf.format(MapleGuild.CREATE_GUILD_COST);
	}

	private void guildUpdate(){
		if(this.guildid < 1) return;
		mgc.setLevel(level);
		mgc.setJobId(job.getId());
		try{
			ChannelServer.getInstance().getWorldInterface().memberLevelJobUpdate(this.mgc);
			int allianceId = getGuild().getAllianceId();
			if(allianceId > 0){
				ChannelServer.getInstance().getWorldInterface().allianceMessage(allianceId, MaplePacketCreator.updateAllianceJobLevel(this), getId(), -1);
			}
		}catch(Exception e){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, e);
		}
	}

	public void handleEnergyChargeGain(){ // to get here energychargelevel has to be > 0
		Skill energycharge = isCygnus() ? SkillFactory.getSkill(ThunderBreaker.ENERGY_CHARGE) : SkillFactory.getSkill(Marauder.ENERGY_CHARGE);
		MapleStatEffect ceffect;
		ceffect = energycharge.getEffect(getSkillLevel(energycharge));
		TimerManager tMan = TimerManager.getInstance();
		if(energybar < 10000){
			energybar += 102;
			if(energybar > 10000){
				energybar = 10000;
			}
			List<Pair<MapleBuffStat, BuffDataHolder>> stat = Collections.singletonList(new Pair<>(MapleBuffStat.ENERGY_CHARGE, new BuffDataHolder(energycharge.getId(), getSkillLevel(energycharge), energybar)));
			setBuffedValue(MapleBuffStat.ENERGY_CHARGE, energybar);
			client.announce(MaplePacketCreator.giveBuff(this, energybar, 0, stat));
			client.announce(UserLocal.UserEffect.showOwnBuffEffect(energycharge.getId(), 2));
			getMap().broadcastMessage(this, UserRemote.UserEffect.showBuffeffect(id, energycharge.getId(), 2));
			getMap().broadcastMessage(this, MaplePacketCreator.giveForeignBuff(MapleCharacter.this, stat));
		}
		if(energybar >= 10000 && energybar < 11000){
			energybar = 15000;
			final MapleCharacter chr = this;
			tMan.schedule("energyBar", new Runnable(){

				@Override
				public void run(){
					energybar = 0;
					List<Pair<MapleBuffStat, BuffDataHolder>> stat = Collections.singletonList(new Pair<>(MapleBuffStat.ENERGY_CHARGE, new BuffDataHolder(0, 0, energybar)));
					setBuffedValue(MapleBuffStat.ENERGY_CHARGE, energybar);
					client.announce(MaplePacketCreator.giveBuff(MapleCharacter.this, energybar, 0, stat));
					getMap().broadcastMessage(chr, MaplePacketCreator.giveForeignBuff(MapleCharacter.this, stat));
					recalcLocalStats();
				}
			}, ceffect.getDuration());
			recalcLocalStats();
		}
	}

	public void handleOrbconsume(){
		int skillid = isCygnus() ? DawnWarrior.COMBO : Crusader.COMBO;
		Skill combo = SkillFactory.getSkill(skillid);
		List<Pair<MapleBuffStat, BuffDataHolder>> stat = Collections.singletonList(new Pair<>(MapleBuffStat.COMBO, new BuffDataHolder(skillid, getSkillLevel(combo), 1)));
		setBuffedValue(MapleBuffStat.COMBO, 1);
		client.announce(MaplePacketCreator.giveBuff(this, skillid, combo.getEffect(getSkillLevel(combo)).getDuration() + (int) ((getBuffedStarttime(MapleBuffStat.COMBO) - System.currentTimeMillis())), stat));
		getMap().broadcastMessage(this, MaplePacketCreator.giveForeignBuff(this, stat), false);
	}

	public boolean hasEntered(String script){
		for(int mapId : entered.keySet()){
			if(entered.get(mapId).equals(script)) return true;
		}
		return false;
	}

	public boolean hasEntered(String script, int mapId){
		if(entered.containsKey(mapId)){
			if(entered.get(mapId).equals(script)) return true;
		}
		return false;
	}

	public void hasGivenFame(MapleCharacter to){
		lastfametime = System.currentTimeMillis();
		lastmonthfameids.add(Integer.valueOf(to.getId()));
		try{
			try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO famelog (characterid, characterid_to) VALUES (?, ?)")){
				ps.setInt(1, getId());
				ps.setInt(2, to.getId());
				ps.executeUpdate();
			}
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
	}

	public boolean hasMerchant(){
		return hasMerchant;
	}

	public boolean haveItem(int itemid){
		return haveItem(itemid, false);
		// return getItemQuantity(itemid, false) > 0; //ori, we dont need all the extra looping after item is found.
	}

	public boolean haveItem(int itemid, boolean checkEquipped){
		boolean possesed = inventory[ItemInformationProvider.getInstance().getInventoryType(itemid).ordinal()].findById(itemid) != null;
		if(!possesed && checkEquipped){
			possesed = inventory[MapleInventoryType.EQUIPPED.ordinal()].findById(itemid) != null;
		}
		return possesed;
	}

	public boolean checkEquippedFor(int itemId){
		return getInventory(MapleInventoryType.EQUIPPED).findById(itemId) != null;
	}

	public void increaseGuildCapacity(){ // hopefully nothing is null
		try{
			String response = ChannelServer.getInstance().getWorldInterface().increaseGuildCapacity(guildid);
			if(response.equalsIgnoreCase("max")){
				dropMessage(1, "Your guild has max capacity..");
			}else if(response.equalsIgnoreCase("Your guild doesn't have enough mesos.")){
				dropMessage(1, response);
			}
		}catch(RemoteException | NullPointerException ex){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
		}
	}

	public boolean isActiveBuffedValue(int skillid){
		LinkedList<MapleBuffStatDataHolder> allBuffs = new LinkedList<>(effects.values());
		for(MapleBuffStatDataHolder mbsvh : allBuffs){
			if(mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skillid) return true;
		}
		return false;
	}

	public boolean isAlive(){
		return hp > 0;
	}

	public boolean isBuffFrom(MapleBuffStat stat, Skill skill){
		MapleBuffStatDataHolder mbsvh = effects.get(stat);
		if(mbsvh == null) return false;
		return mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skill.getId();
	}

	public boolean isCygnus(){
		return getJobType() == 1;
	}

	public boolean isAran(){
		return getJob().getId() >= 2000 && getJob().getId() <= 2112;
	}

	public boolean isBeginnerJob(){
		return (getJob().getId() == 0 || getJob().getId() == 1000 || getJob().getId() == 2000 || getJob().getId() == 2001 || getJob().getId() == 3000) && getLevel() < 11;
	}

	public boolean isHidden(){
		return hidden;
	}

	public boolean isMapObjectVisible(MapleMapObject mo){
		return visibleMapObjects.contains(mo);
	}

	public boolean isPartyLeader(){
		MapleParty party = getParty();
		if(party == null) return false;
		return party.getLeader().getId() == getId();
	}

	public void leaveMap(){
		controlled.clear();
		visibleMapObjects.clear();
		if(chair != 0){
			chair = 0;
		}
		if(hpDecreaseTask != null){
			hpDecreaseTask.cancel(false);
		}
	}

	public void levelUp(boolean takeexp){
		boolean newLevel = (level + 1 > highestLevel);
		int jobIndex = getJobIndexBasedOnLevel();
		if(newLevel){
			highestLevel++;
			if(isBeginnerJob()){
				remainingAp = 0;
				if(getLevel() < 6){
					str += 5;
				}else{
					str += 4;
					dex += 1;
				}
			}else{
				remainingAp += 5;
				if(isCygnus() && level < 70){
					remainingAp++;
				}
			}
			if(!hasReincarnated()){
				Skill improvingMaxHP = null;
				Skill improvingMaxMP = null;
				int improvingMaxHPLevel = 0;
				int improvingMaxMPLevel = 0;
				if(job == MapleJob.BEGINNER || job == MapleJob.NOBLESSE || job == MapleJob.LEGEND){
					maxhp += Randomizer.rand(12, 16);
					maxmp += Randomizer.rand(10, 12);
				}else if(job.isA(MapleJob.WARRIOR) || job.isA(MapleJob.DAWNWARRIOR1)){
					improvingMaxHP = isCygnus() ? SkillFactory.getSkill(DawnWarrior.MAX_HP_INCREASE) : SkillFactory.getSkill(Swordsman.IMPROVED_MAX_HP_INCREASE);
					if(job.isA(MapleJob.CRUSADER)){
						improvingMaxMP = SkillFactory.getSkill(1210000);
					}else if(job.isA(MapleJob.DAWNWARRIOR2)){
						improvingMaxMP = SkillFactory.getSkill(11110000);
					}
					improvingMaxHPLevel = getSkillLevel(improvingMaxHP);
					maxhp += Randomizer.rand(24, 28);
					maxmp += Randomizer.rand(4, 6);
				}else if(job.isA(MapleJob.MAGICIAN) || job.isA(MapleJob.BLAZEWIZARD1) || job.isA(MapleJob.EVAN) || job.isA(MapleJob.EVAN1)){
					improvingMaxMP = isCygnus() ? SkillFactory.getSkill(BlazeWizard.INCREASING_MAX_MP) : SkillFactory.getSkill(Magician.IMPROVED_MAX_MP_INCREASE);
					improvingMaxMPLevel = getSkillLevel(improvingMaxMP);
					maxhp += Randomizer.rand(10, 20);
					maxmp += Randomizer.rand(22, 24);
				}else if(job.isA(MapleJob.BOWMAN) || job.isA(MapleJob.THIEF) || (job.getId() > 1299 && job.getId() < 1500)){
					maxhp += Randomizer.rand(20, 24);
					maxmp += Randomizer.rand(14, 16);
				}else if(job.isA(MapleJob.GM)){
					maxhp = 30000;
					maxmp = 30000;
				}else if(job.isA(MapleJob.PIRATE) || job.isA(MapleJob.THUNDERBREAKER1)){
					improvingMaxHP = isCygnus() ? SkillFactory.getSkill(ThunderBreaker.IMPROVE_MAX_HP) : SkillFactory.getSkill(5100000);
					improvingMaxHPLevel = getSkillLevel(improvingMaxHP);
					maxhp += Randomizer.rand(22, 28);
					maxmp += Randomizer.rand(18, 23);
				}else if(job.isA(MapleJob.ARAN1)){
					maxhp += Randomizer.rand(44, 48);
					int aids = Randomizer.rand(4, 8);
					maxmp += aids + Math.floor(aids * 0.1);
				}
				if(improvingMaxHPLevel > 0 && (job.isA(MapleJob.WARRIOR) || job.isA(MapleJob.PIRATE) || job.isA(MapleJob.DAWNWARRIOR1))){
					maxhp += improvingMaxHP.getEffect(improvingMaxHPLevel).getX();
				}
				if(improvingMaxMPLevel > 0 && (job.isA(MapleJob.MAGICIAN) || job.isA(MapleJob.CRUSADER) || job.isA(MapleJob.BLAZEWIZARD1))){
					maxmp += improvingMaxMP.getEffect(improvingMaxMPLevel).getX();
				}
				maxmp += localint / 10;
			}
		}
		if(takeexp){
			exp.addAndGet(-ExpTable.getExpNeededForLevel(level));
			if(exp.get() < 0){
				exp.set(0);
			}
		}
		level++;
		if(level == 10 && job.equals(MapleJob.EVAN)){
			announce(MaplePacketCreator.onSayImage(NPCConversationManager.NpcReplacedByUser, 0, (byte) 0, "UI/tutorial/evan/14/0"));
			changeJob(MapleJob.EVAN1);
			resetStats();
			// gainSp(2);
			createDragon();
			MapleQuest.getInstance(22100).forceComplete(this, 0);// Makes the dragon appear in skill ui, and gives you "Baby Dragon Awakens" quest
			if(dragon != null){
				dragon.setPosition(getPosition());
				getMap().addMapObject(dragon);
				if(isHidden()){
					getMap().broadcastGMMessage(this, MaplePacketCreator.spawnDragon(dragon));
				}else{
					getMap().broadcastMessage(this, MaplePacketCreator.spawnDragon(dragon));
				}
			}
		}
		if(job.isA(MapleJob.EVAN1)){
			Equip dragon = null;
			if(level == 50){
				dragon = new Equip(1902040, (short) 1);
			}else if(level == 80){
				dragon = new Equip(1902041, (short) 1);
			}else if(level == 120){
				dragon = new Equip(1902042, (short) 1);
			}
			if(dragon != null){
				dragon.setPosition((short) -18);
				getInventory(MapleInventoryType.EQUIPPED).addFromDB(dragon);
				if(level > 50) announce(MaplePacketCreator.modifyInventory(true, Collections.singletonList(new ModifyInventory(3, dragon))));
				announce(MaplePacketCreator.modifyInventory(true, Collections.singletonList(new ModifyInventory(0, dragon))));
			}
		}
		announce(MaplePacketCreator.showEffect("LevelUp/BackGround"));
		announce(MaplePacketCreator.showEffect("LevelUp/Number/" + level));
		if(level >= getMaxLevel()){
			exp.set(0);
			level = getMaxLevel(); // To prevent levels past 200
			highestLevel = getMaxLevel();
		}
		maxhp = Math.min(30000, maxhp);
		maxmp = Math.min(30000, maxmp);
		if(level == 200){
			exp.set(0);
		}
		hp = maxhp;
		mp = maxmp;
		recalcLocalStats();
		if(newLevel){
			List<Pair<MapleStat, Integer>> statup = new ArrayList<>(10);
			statup.add(new Pair<>(MapleStat.AVAILABLEAP, remainingAp));
			statup.add(new Pair<>(MapleStat.HP, localmaxhp));
			statup.add(new Pair<>(MapleStat.MP, localmaxmp));
			statup.add(new Pair<>(MapleStat.EXP, exp.get()));
			statup.add(new Pair<>(MapleStat.LEVEL, level));
			statup.add(new Pair<>(MapleStat.MAXHP, maxhp));
			statup.add(new Pair<>(MapleStat.MAXMP, maxmp));
			statup.add(new Pair<>(MapleStat.STR, str));
			statup.add(new Pair<>(MapleStat.DEX, dex));
			if(!JobConstants.is_beginner_job(job.getId())){
				addRemainingSp(jobIndex, 3);
				statup.add(new Pair<>(MapleStat.AVAILABLESP, remainingSp[jobIndex]));
				// remainingSp[GameConstants.getSkillBook(job.getId())] += 3;
				// statup.add(new Pair<>(MapleStat.AVAILABLESP, remainingSp[GameConstants.getSkillBook(job.getId())]));
			}
			client.announce(CWvsContext.updatePlayerStats(statup, this));
		}else{
			List<Pair<MapleStat, Integer>> statup = new ArrayList<>(2);
			statup.add(new Pair<>(MapleStat.EXP, exp.get()));
			statup.add(new Pair<>(MapleStat.LEVEL, level));
			client.announce(CWvsContext.updatePlayerStats(statup, this));
		}
		getMap().broadcastMessage(this, UserRemote.UserEffect.showForeignEffect(getId(), 0), false);
		recalcLocalStats();
		setMPC(new MaplePartyCharacter(this));
		try{
			silentPartyUpdate();
		}catch(RemoteException | NullPointerException ex){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
		}
		if(this.guildid > 0 && newLevel){
			int gain = hasReincarnated() ? 1 : 40;
			try{
				ChannelServer.getInstance().getWorldInterface().gainGP(guildid, id, gain);
				ChannelServer.getInstance().getWorldInterface().guildMessage(guildid, MaplePacketCreator.levelUpMessage(2, level, name), getId());
			}catch(RemoteException | NullPointerException ex){
				Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
			}
		}
		/*if(ServerConstants.PERFECT_PITCH && level >= 30){
			// milestones?
			if(MapleInventoryManipulator.checkSpace(client, 4310000, (short) 1, "")){
				MapleInventoryManipulator.addById(client, 4310000, (short) 1);
			}
		}*/
		if(level == 200 && !isGM() && newLevel){
			final String names = (getMedalText() + name);
			try{
				ChannelServer.getInstance().getWorldInterface().broadcastPacket(MaplePacketCreator.serverNotice(6, String.format(LEVEL_200, names, names)));
			}catch(RemoteException | NullPointerException ex){
				Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
			}
		}
		// levelUpMessages();
		guildUpdate();
		warpOutTraining();
	}

	public void levelDown(){
		level--;
		updateSingleStat(MapleStat.LEVEL, level);
		recalcLocalStats();
		setMPC(new MaplePartyCharacter(this));
		try{
			silentPartyUpdate();
		}catch(RemoteException | NullPointerException ex){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
		}
		guildUpdate();
		warpOutTraining();
	}

	public void warpOutTraining(){ // Training centers
		if((mapid >= 910060000 && mapid <= 910060004) && level == 20){
			this.changeMap(100010000);
		}else if((mapid >= 910120000 && mapid <= 910120004) && level == 20){
			this.changeMap(100050000);
		}else if((mapid >= 910220000 && mapid <= 910220004) && level == 20){
			this.changeMap(101040000);
		}else if((mapid >= 910310000 && mapid <= 910310004) && level == 20){
			this.changeMap(103010000);
		}else if((mapid >= 912030000 && mapid <= 912030004) && level == 20){
			this.changeMap(120010000);
		}
	}

	public void gainAp(int amount){
		List<Pair<MapleStat, Integer>> statup = new ArrayList<>(1);
		remainingAp += amount;
		statup.add(new Pair<>(MapleStat.AVAILABLEAP, remainingAp));
		client.announce(CWvsContext.updatePlayerStats(statup, this));
	}

	public int getJobIndexBasedOnLevel(){
		return getJobIndexBasedOnLevel(job);
	}

	public int getJobIndexBasedOnLevel(MapleJob job){
		if(level <= 7 && JobConstants.is_beginner_job(job.getId())){// beginner get its own sp
			return 0;
		}else if(JobConstants.is_evan_job(job.getId())){
			MapleJob actualJob = null;
			if(level >= 160) actualJob = MapleJob.EVAN10;
			else if(level >= 120) actualJob = MapleJob.EVAN9;
			else if(level >= 100) actualJob = MapleJob.EVAN8;
			else if(level >= 80) actualJob = MapleJob.EVAN7;
			else if(level >= 60) actualJob = MapleJob.EVAN6;
			else if(level >= 50) actualJob = MapleJob.EVAN5;
			else if(level >= 40) actualJob = MapleJob.EVAN4;
			else if(level >= 30) actualJob = MapleJob.EVAN3;
			else if(level >= 20) actualJob = MapleJob.EVAN2;
			else if(level >= 10) actualJob = MapleJob.EVAN1;
			else actualJob = MapleJob.EVAN;
			return JobConstants.getJobIndex(actualJob.getId());
		}/*else if(JobConstants.is_dualblade_job(job.getId()) || this.nSubJob == 1){
		 MapleJob actualJob = null;
		 if(level >= 120) actualJob = MapleJob.BLADE_MASTER;
		 else if(level >= 70) actualJob = MapleJob.BLADE_LORD;
		 else if(level >= 55) actualJob = MapleJob.BLADE_SPECIALIST;
		 else if(level >= 30) actualJob = MapleJob.BLADE_ACOLYTE;
		 else if(level >= 20) actualJob = MapleJob.BLADE_RECRUIT;
		 else if(level >= 10) actualJob = MapleJob.THIEF;
		 else actualJob = MapleJob.BEGINNER;
		 return JobConstants.getJobIndex(actualJob.getId());
		 }*/else{// Explorers share SP
			/*int index = 0;
			if(level >= 120) index = 3;
			else if(level >= 70) index = 2;
			else if(level >= 30) index = 1;
			else if(level >= 10) index = 0;
			actualJob = job.getJobTree()[index];*/
			return 1;
		}
	}

	public void gainSp(int amount){
		gainSp(job.getId(), amount);
	}

	public void gainSp(int jobid, int amount){
		List<Pair<MapleStat, Integer>> statup = new ArrayList<>(1);
		addRemainingSp(JobConstants.getSkillBookIndex(jobid), amount);
		statup.add(new Pair<>(MapleStat.AVAILABLESP, remainingSp[JobConstants.getSkillBookIndex(jobid)]));
		client.announce(CWvsContext.updatePlayerStats(statup, this));
		client.announce(CWvsContext.OnMessage.incSPMessage((short) jobid, (byte) amount));
	}

	public static MapleCharacter loadCharFromDB(int charid, MapleClient client, boolean channelserver) throws SQLException{
		try{
			MapleCharacter ret = new MapleCharacter();
			ret.client = client;
			ret.id = charid;
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE id = ?");
			ps.setInt(1, charid);
			ResultSet rs = ps.executeQuery();
			if(!rs.next()){
				rs.close();
				ps.close();
				throw new RuntimeException("Loading char failed (not found)");
			}
			ret.name = rs.getString("name");
			ret.level = rs.getInt("level");
			ret.highestLevel = rs.getInt("highestLevel");
			ret.fame = rs.getInt("fame");
			ret.str = rs.getInt("str");
			ret.dex = rs.getInt("dex");
			ret.int_ = rs.getInt("int");
			ret.luk = rs.getInt("luk");
			ret.exp.set(rs.getInt("exp"));
			ret.gachaexp.set(rs.getInt("gachaexp"));
			ret.hp = rs.getInt("hp");
			ret.maxhp = rs.getInt("maxhp");
			ret.mp = rs.getInt("mp");
			ret.maxmp = rs.getInt("maxmp");
			ret.hpMpApUsed = rs.getInt("hpMpUsed");
			ret.hasMerchant = rs.getInt("HasMerchant") == 1;
			String[] skillPoints = rs.getString("sp").split(",");
			for(int i = 0; i < skillPoints.length; i++){
				ret.remainingSp[i] = Integer.parseInt(skillPoints[i]);
			}
			ret.remainingAp = rs.getInt("ap");
			ret.meso.set(rs.getInt("meso"));
			ret.merchantmeso = rs.getInt("MerchantMesos");
			ret.gmLevel = rs.getInt("gm");
			ret.skinColor = MapleSkinColor.getById(rs.getInt("skincolor"));
			ret.gender = rs.getInt("gender");
			ret.job = MapleJob.getById(rs.getInt("job"));
			ret.nSubJob = rs.getShort("subjob");
			ret.finishedDojoTutorial = rs.getInt("finishedDojoTutorial") == 1;
			ret.vanquisherKills = rs.getInt("vanquisherKills");
			ret.omokwins = rs.getInt("omokwins");
			ret.omoklosses = rs.getInt("omoklosses");
			ret.omokties = rs.getInt("omokties");
			ret.matchcardwins = rs.getInt("matchcardwins");
			ret.matchcardlosses = rs.getInt("matchcardlosses");
			ret.matchcardties = rs.getInt("matchcardties");
			ret.hair = rs.getInt("hair");
			ret.face = rs.getInt("face");
			ret.accountid = rs.getInt("accountid");
			ret.mapid = rs.getInt("map");
			ret.initialSpawnPoint = rs.getInt("spawnpoint");
			ret.world = rs.getByte("world");
			ret.rank = rs.getInt("rank");
			ret.rankMove = rs.getInt("rankMove");
			ret.jobRank = rs.getInt("jobRank");
			ret.jobRankMove = rs.getInt("jobRankMove");
			int mountexp = rs.getInt("mountexp");
			int mountlevel = rs.getInt("mountlevel");
			int mounttiredness = rs.getInt("mounttiredness");
			ret.guildid = rs.getInt("guildid");
			ret.guildrank = rs.getInt("guildrank");
			ret.gp = rs.getInt("gp");
			ret.allianceRank = rs.getInt("allianceRank");
			ret.familyId = rs.getInt("familyId");
			ret.bookCover = rs.getInt("monsterbookcover");
			ret.monsterbook = new MonsterBook();
			ret.monsterbook.loadCards(charid);
			ret.vanquisherStage = rs.getInt("vanquisherStage");
			ret.dojoPoints = rs.getInt("dojoPoints");
			ret.dojoStage = rs.getInt("lastDojoStage");
			ret.dataString = rs.getString("dataString");
			ret.progressValues = rs.getString("progressValues");
			if(ret.guildid > 0 && ret.getClient() != null){
				ret.mgc = new MapleGuildCharacter(ret);
				ret.setAllianceRank(ret.getAllianceRank(), true);
			}
			int buddyCapacity = rs.getInt("buddyCapacity");
			ret.buddylist = new BuddyList(buddyCapacity);
			ret.stats = new PlayerStats();
			ret.ironMan = rs.getInt("ironMan");
			ret.hardmode = rs.getInt("hardmode");
			String skillIn = rs.getString("rsSkillLevel");
			if(skillIn != null && skillIn.length() > 0){
				for(String info : skillIn.split(",")){
					String[] split = info.split("=");
					ret.rsSkillLevel.put(RSSkill.valueOf(split[0]), Byte.valueOf(split[1]));
				}
			}
			for(RSSkill skill : RSSkill.values()){
				if(!ret.rsSkillLevel.containsKey(skill)) ret.rsSkillLevel.put(skill, (byte) 1);
			}
			skillIn = rs.getString("rsSkillExp");
			if(skillIn != null && skillIn.length() > 0){
				for(String info : skillIn.split(",")){
					String[] split = info.split("=");
					ret.rsSkillExp.put(RSSkill.valueOf(split[0]), Long.parseLong(split[1]));
				}
			}
			for(RSSkill skill : RSSkill.values()){
				if(!ret.rsSkillExp.containsKey(skill)) ret.rsSkillExp.put(skill, (long) 0);
			}
			ret.marriedto = rs.getInt("marriedto");
			ret.marriageringid = rs.getInt("marriageringid");
			ret.engagementringid = rs.getInt("engagementringid");
			ret.marriageid = rs.getInt("marriageid");
			ret.reincarnations = rs.getInt("reincarnations");
			ret.tasksCompleted = rs.getInt("tasksCompleted");
			ret.playtime = rs.getLong("playtime");
			ret.playtimeStart = Calendar.getInstance().getTimeInMillis();
			ret.bossEntries.loadFromTable(rs);
			ret.getInventory(MapleInventoryType.EQUIP).setSlotLimit(rs.getByte("equipslots"));
			ret.getInventory(MapleInventoryType.USE).setSlotLimit(rs.getByte("useslots"));
			ret.getInventory(MapleInventoryType.SETUP).setSlotLimit(rs.getByte("setupslots"));
			ret.getInventory(MapleInventoryType.ETC).setSlotLimit(rs.getByte("etcslots"));
			for(Pair<Item, MapleInventoryType> item : ItemFactory.INVENTORY.loadItems(ret.id, !channelserver)){
				ret.getInventory(item.getRight()).addFromDB(item.getLeft());
				if(item.getLeft().getPetId() > -1){
					MaplePet pet = item.getLeft().getPet();
					if(pet != null && pet.isSummoned()){
						ret.addPet(pet);
					}
					continue;
				}
				if(channelserver){
					if(item.getRight().equals(MapleInventoryType.EQUIP) || item.getRight().equals(MapleInventoryType.EQUIPPED)){
						Equip equip = (Equip) item.getLeft();
						if(equip.getRingId() > -1){
							MapleRing ring = MapleRing.loadFromDb(equip.getRingId());
							if(ring == null){
								Logger.log(LogType.ERROR, LogFile.GENERAL_ERROR, "Failed to load ring " + equip.getRingId() + " item " + equip.getItemId());
							}else{
								if(item.getRight().equals(MapleInventoryType.EQUIPPED)){
									ring.equip();
								}
								if(ring.getItemId() == 1112803 || ring.getItemId() == 1112806 || ring.getItemId() == 1112807 || ring.getItemId() == 1112809){
									if(ret.marriageringid == ring.getItemId()){
										ret.marriageRing = ring;
									}
								}else if(ring.getItemId() > 1112012){
									ret.addFriendshipRing(ring);
								}else{
									ret.addCrushRing(ring);
								}
							}
						}
					}
				}
			}
			if(channelserver){
				int partyid = rs.getInt("party");
				try{
					MapleParty party = ChannelServer.getInstance().getWorldInterface().getParty(partyid);
					if(party != null){
						ret.partyid = partyid;
						ret.mpc = party.getMemberById(ret.id);
						if(ret.mpc == null){
							ret.partyid = -1;
						}
					}else ret.partyid = -1;
				}catch(RemoteException | NullPointerException ex){
					Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
				}
				ret.map = ret.client.getChannelServer().getMap(ret.mapid);// no purchased maps.. just plop em in the normal one.
				if(ret.map == null) ret.map = ret.client.getChannelServer().getMap(100000000);// rip
				MaplePortal portal = ret.map.getPortal(ret.initialSpawnPoint);
				if(portal == null){
					portal = ret.map.getPortal(0);
					ret.initialSpawnPoint = 0;
				}
				if(portal != null) ret.setPosition(portal.getPosition());
				int messengerid = rs.getInt("messengerid");
				int position = rs.getInt("messengerposition");
				if(messengerid > 0 && position < 4 && position > -1){
					try{
						MapleMessenger messenger = ChannelServer.getInstance().getWorldInterface().getMessenger(messengerid);
						if(messenger != null){
							ret.messenger = messenger;
							ret.messengerposition = position;
						}
					}catch(RemoteException | NullPointerException ex){
						Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
					}
				}
				ret.loggedIn = true;
			}
			rs.close();
			ps.close();
			ps = con.prepareStatement("SELECT mapid,vip FROM trocklocations WHERE characterid = ? LIMIT 15");
			ps.setInt(1, charid);
			rs = ps.executeQuery();
			byte v = 0;
			byte r = 0;
			while(rs.next()){
				if(rs.getInt("vip") == 1){
					ret.viptrockmaps.add(rs.getInt("mapid"));
					v++;
				}else{
					ret.trockmaps.add(rs.getInt("mapid"));
					r++;
				}
			}
			while(v < 10){
				ret.viptrockmaps.add(999999999);
				v++;
			}
			while(r < 5){
				ret.trockmaps.add(999999999);
				r++;
			}
			rs.close();
			ps.close();
			if(ret.getClient() != null){
				ps = con.prepareStatement("SELECT name FROM accounts WHERE id = ?", Statement.RETURN_GENERATED_KEYS);
				ps.setInt(1, ret.accountid);
				rs = ps.executeQuery();
				if(rs.next()){
					ret.getClient().setAccountName(rs.getString("name"));
				}
				rs.close();
				ps.close();
			}
			ps = con.prepareStatement("SELECT `area`,`info` FROM area_info WHERE charid = ?");
			ps.setInt(1, ret.id);
			rs = ps.executeQuery();
			while(rs.next()){
				ret.area_info.put(rs.getShort("area"), rs.getString("info"));
			}
			rs.close();
			ps.close();
			ps = con.prepareStatement("SELECT `name`,`info` FROM eventstats WHERE characterid = ?");
			ps.setInt(1, ret.id);
			rs = ps.executeQuery();
			while(rs.next()){
				String name = rs.getString("name");
				if(rs.getString("name").equals("rescueGaga")){
					ret.events.put(name, new RescueGaga(rs.getInt("info")));
				}
				// ret.events = new MapleEvents(new RescueGaga(rs.getInt("rescuegaga")), new ArtifactHunt(rs.getInt("artifacthunt")));
			}
			rs.close();
			ps.close();
			ret.cashshop = new CashShop(ret.accountid, ret.id, ret.getJobType(), ret.isIronMan());
			ret.autoban = new AutobanManager(ret);
			ps = con.prepareStatement("SELECT name, level, job FROM characters WHERE accountid = ? AND id != ? AND world = ? AND deleted = 0 ORDER BY level DESC");
			ps.setInt(1, ret.accountid);
			ps.setInt(2, charid);
			ps.setInt(3, ret.world);
			rs = ps.executeQuery();
			if(rs.next()){
				int job = rs.getInt("job");
				int level = rs.getInt("level");
				if(job < 1000){
					if(level > ret.explorerLinkedLevel){
						ret.explorerLinkedName = rs.getString("name");
						ret.explorerLinkedLevel = level;
					}
				}else if(job >= 1000 && job < 2000){
					if(level > ret.cygnusLinkedLevel){
						ret.cygnusLinkedName = rs.getString("name");
						ret.cygnusLinkedLevel = level;
					}
				}
			}
			rs.close();
			ps.close();
			try(PreparedStatement s = con.prepareStatement("SELECT date, total, higher FROM monsterkills where id = ?")){
				s.setInt(1, charid);
				try(ResultSet result = s.executeQuery()){
					while(result.next()){
						String date = result.getString("date");
						ret.monsterKillTotal.put(date, result.getLong("total"));
						ret.monsterKillHigher.put(date, result.getLong("higher"));
					}
				}
			}
			if(ret.getClient() != null){
				ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
				ps.setInt(1, ret.accountid);
				rs = ps.executeQuery();
				if(rs.next()){
					byte charSlots = rs.getByte("characterslots");
					byte currCharSlots = ret.getClient().getCharacterSlots();
					if(charSlots > currCharSlots){
						ret.getClient().setCharacterSlots(charSlots);
					}else{
						ret.getClient().setCharacterSlots(currCharSlots);
					}
					ret.getClient().setGMLevel(rs.getInt("gm"));
					ret.getClient().setEliteStart(rs.getLong("eliteStart"));
					ret.getClient().setEliteLength(rs.getLong("eliteLength"));
					ret.getClient().setAlphaUser(rs.getByte("alpha") == 1);
					ret.getClient().setLastNameChange(rs.getLong("lastNameChange"));
					ret.getClient().parseMac(rs.getString("macs"));
					ret.getClient().parseHWID(rs.getString("hwid"));
					Map<String, Object> values = new HashMap<String, Object>();
					String progressValue = rs.getString("progressValues");
					if(progressValue != null && progressValue.length() > 0){
						for(String s : progressValue.split(",")){
							if(s.length() > 0) values.put(s.split("=")[0], s.split("=")[1]);
						}
					}
					ret.getClient().setProgressValues(values);
					ret.getClient().setPetVac(rs.getBoolean("petvac"));
					ret.chatBan = rs.getInt("chatBan") != 0;
					if(ret.chatBan){
						ret.chatBanDate = rs.getTimestamp("chatBanDate");
						ret.chatBanDuration = rs.getInt("chatBanDuration");
					}
					ret.setAutoSell(rs.getBoolean("autoSell"));
					String input = rs.getString("autoSellItems");
					if(input != null){
						if(input.contains(",")){
							String[] in = input.split(",");
							for(String i : in){
								ret.addAutoSellIgnore(i);
							}
						}
					}
					input = rs.getString("autoSellInventories");
					if(input != null){
						if(input.contains(",")){
							String[] in = input.split(",");
							for(String i : in){
								ret.addAutoSellInventoryIgnore(MapleInventoryType.valueOf(i));
							}
						}
					}
				}
				rs.close();
				ps.close();
			}
			ps = con.prepareStatement("SELECT * FROM SlayerTasks WHERE chrid = ?");
			ps.setInt(1, ret.id);
			rs = ps.executeQuery();
			while(rs.next()){
				ret.slayerTask = new SlayerTask(rs.getInt("targetid"), MapleLifeFactory.getMonster(rs.getInt("targetid")).getStats().getLevel(), rs.getString("map"));
				ret.slayerTask.setKills(rs.getInt("kills"));
				ret.slayerTask.setRequiredKills(rs.getInt("requiredKills"));
			}
			rs.close();
			ps.close();
			if(channelserver){
				ps = con.prepareStatement("SELECT * FROM queststatus WHERE characterid = ?");
				ps.setInt(1, charid);
				rs = ps.executeQuery();
				try(PreparedStatement pse = con.prepareStatement("SELECT progressid, progress FROM questprogress WHERE queststatusid = ?")){
					try(PreparedStatement psf = con.prepareStatement("SELECT mapid FROM medalmaps WHERE queststatusid = ?")){
						while(rs.next()){
							MapleQuest q = MapleQuest.getInstance(rs.getShort("quest"));
							MapleQuestStatus status = new MapleQuestStatus(q, MapleQuestStatus.Status.getById(rs.getInt("status")));
							long cTime = rs.getLong("time");
							if(cTime > -1){
								status.setCompletionTime(cTime * 1000);
							}
							status.setForfeited(rs.getInt("forfeited"));
							pse.setInt(1, rs.getInt("queststatusid"));
							List<Integer> mobs = q.getRelevantMobs();
							if(!mobs.isEmpty()){
								int index = 0;
								try(ResultSet rsProgress = pse.executeQuery()){
									while(rsProgress.next()){
										int mobid = rsProgress.getInt("progressid");
										while(mobs.get(index) != mobid){// If the current index isn't the one we are inserting, insert all the blanks.
											status.setProgress(mobs.get(index), "000");
											index++;
										}
										status.setProgress(mobid, rsProgress.getString("progress"));
										index++;// +1 since we are going to the next mobid in the list.
									}
								}
								for(int mobid : mobs){
									if(status.getProgress(mobid).isEmpty()){// returns an empty string if its null.
										status.setProgress(mobid, "000");
									}
								}
							}
							psf.setInt(1, rs.getInt("queststatusid"));
							try(ResultSet medalmaps = psf.executeQuery()){
								while(medalmaps.next()){
									status.addMedalMap(medalmaps.getInt("mapid"));
								}
							}
							ret.quests.put(q.getId(), status);
						}
					}
					rs.close();
					ps.close();
				}
				ps = con.prepareStatement("SELECT skillid,skilllevel,masterlevel,expiration FROM skills WHERE characterid = ?");
				ps.setInt(1, charid);
				rs = ps.executeQuery();
				while(rs.next()){
					ret.skills.put(SkillFactory.getSkill(rs.getInt("skillid")), new SkillEntry(rs.getByte("skilllevel"), rs.getInt("masterlevel"), rs.getLong("expiration")));
				}
				rs.close();
				ps.close();
				ps = con.prepareStatement("SELECT SkillID,StartTime,length FROM cooldowns WHERE charid = ?");
				ps.setInt(1, ret.getId());
				rs = ps.executeQuery();
				while(rs.next()){
					final int skillid = rs.getInt("SkillID");
					final long length = rs.getLong("length"), startTime = rs.getLong("StartTime");
					if(skillid != 5221999 && (length + startTime < System.currentTimeMillis())){
						continue;
					}
					ret.giveCoolDowns(skillid, startTime, length);
				}
				rs.close();
				ps.close();
				ps = con.prepareStatement("DELETE FROM cooldowns WHERE charid = ?");
				ps.setInt(1, ret.getId());
				ps.executeUpdate();
				ps.close();
				ps = con.prepareStatement("SELECT * FROM skillmacros WHERE characterid = ?");
				ps.setInt(1, charid);
				rs = ps.executeQuery();
				while(rs.next()){
					int position = rs.getInt("position");
					SkillMacro macro = new SkillMacro(rs.getInt("skill1"), rs.getInt("skill2"), rs.getInt("skill3"), rs.getString("name"), rs.getInt("shout"), position);
					ret.skillMacros[position] = macro;
				}
				rs.close();
				ps.close();
				ps = con.prepareStatement("SELECT `key`,`type`,`action` FROM keymap WHERE characterid = ?");
				ps.setInt(1, charid);
				rs = ps.executeQuery();
				while(rs.next()){
					int key = rs.getInt("key");
					int type = rs.getInt("type");
					int action = rs.getInt("action");
					ret.keymap.put(Integer.valueOf(key), new MapleKeyBinding(type, action));
				}
				rs.close();
				ps.close();
				ps = con.prepareStatement("SELECT `locationtype`,`map`,`portal` FROM savedlocations WHERE characterid = ?");
				ps.setInt(1, charid);
				rs = ps.executeQuery();
				while(rs.next()){
					ret.savedLocations[SavedLocationType.valueOf(rs.getString("locationtype")).ordinal()] = new SavedLocation(rs.getInt("map"), rs.getInt("portal"));
				}
				rs.close();
				ps.close();
				ps = con.prepareStatement("SELECT `characterid_to`,`when` FROM famelog WHERE characterid = ? AND DATEDIFF(NOW(),`when`) < 30");
				ps.setInt(1, charid);
				rs = ps.executeQuery();
				ret.lastfametime = 0;
				ret.lastmonthfameids = new ArrayList<>(31);
				while(rs.next()){
					ret.lastfametime = Math.max(ret.lastfametime, rs.getTimestamp("when").getTime());
					ret.lastmonthfameids.add(Integer.valueOf(rs.getInt("characterid_to")));
				}
				rs.close();
				ps.close();
				ret.buddylist.loadFromDb(charid);
				ret.storage = MapleStorage.loadOrCreateFromDB(ret.accountid, ret.world);
				ret.recalcLocalStats();
				// ret.resetBattleshipHp();
				ret.silentEnforceMaxHpMp();
			}
			int mountid = ret.getJobType() * 10000000 + 1004;
			if(ret.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -18) != null){
				ret.maplemount = new MapleMount(ret, ret.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -18).getItemId(), mountid);
			}else{
				ret.maplemount = new MapleMount(ret, 0, mountid);
			}
			ret.maplemount.setExp(mountexp);
			ret.maplemount.setLevel(mountlevel);
			ret.maplemount.setTiredness(mounttiredness);
			ret.maplemount.setActive(false);
			return ret;
		}catch(Exception e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
		return null;
	}

	public static String makeMapleReadable(String in){
		String i = in.replace('I', 'i');
		i = i.replace('l', 'L');
		i = i.replace("rn", "Rn");
		i = i.replace("vv", "Vv");
		i = i.replace("VV", "Vv");
		return i;
	}

	private static class MapleBuffStatDataHolder{

		public MapleStatEffect effect;
		public long startTime, duration;
		public BuffDataHolder value;

		public MapleBuffStatDataHolder(MapleStatEffect effect, long startTime, long duration, BuffDataHolder value){
			super();
			this.effect = effect;
			this.startTime = startTime;
			this.duration = duration;
			this.value = value;
		}
	}

	public static class MapleCoolDownValueHolder{

		public int skillId;
		public long startTime, length;
		public ScheduledFuture<?> timer;

		public MapleCoolDownValueHolder(int skillId, long startTime, long length, ScheduledFuture<?> timer){
			super();
			this.skillId = skillId;
			this.startTime = startTime;
			this.length = length;
			this.timer = timer;
		}
	}

	public void message(String m){
		dropMessage(5, m);
	}

	public void yellowMessage(String m){
		announce(MaplePacketCreator.sendYellowTip(m));
	}

	public void mobKilled(int id){
		for(Entry<Integer, List<Integer>> e : MapleLifeFactory.questCountGroup.entrySet()){
			for(int mob : e.getValue()){
				if(mob == id) mobKilled(e.getKey());
			}
		}
		int lastQuestProcessed = 0;
		try{
			for(MapleQuestStatus q : quests.values()){
				lastQuestProcessed = q.getQuest().getId();
				if(q.getStatus() == MapleQuestStatus.Status.COMPLETED || q.getQuest().canComplete(this, null)){
					continue;
				}
				String progress = q.getProgress(id);
				if(!progress.isEmpty() && Integer.parseInt(progress) >= q.getQuest().getMobAmountNeeded(id)){
					continue;
				}
				if(q.progress(id)){
					client.announce(CWvsContext.OnMessage.updateQuest(q, 0));
				}
			}
		}catch(Exception e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e, "MapleCharacter.mobKilled. CID: " + this.id + " last Quest Processed: " + lastQuestProcessed);
		}
	}

	public void mount(int id, int skillid){
		maplemount = new MapleMount(this, id, skillid);
	}

	/*public void playerNPC(MapleCharacter v, int scriptId){
		int npcId;
		try{
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT id FROM playernpcs WHERE ScriptId = ?");
			ps.setInt(1, scriptId);
			ResultSet rs = ps.executeQuery();
			if(!rs.next()){
				rs.close();
				ps = con.prepareStatement("INSERT INTO playernpcs (name, hair, face, skin, x, cy, map, ScriptId, Foothold, rx0, rx1) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, v.getName());
				ps.setInt(2, v.getHair());
				ps.setInt(3, v.getFace());
				ps.setInt(4, v.getSkinColor().getId());
				ps.setInt(5, getPosition().x);
				ps.setInt(6, getPosition().y);
				ps.setInt(7, getMapId());
				ps.setInt(8, scriptId);
				ps.setInt(9, getMap().getFootholds().findBelow(getPosition()).getId());
				ps.setInt(10, getPosition().x + 50);
				ps.setInt(11, getPosition().x - 50);
				ps.executeUpdate();
				rs = ps.getGeneratedKeys();
				rs.next();
				npcId = rs.getInt(1);
				ps.close();
				ps = con.prepareStatement("INSERT INTO playernpcs_equip (NpcId, equipid, equippos) VALUES (?, ?, ?)");
				ps.setInt(1, npcId);
				for(Item equip : getInventory(MapleInventoryType.EQUIPPED)){
					int position = Math.abs(equip.getPosition());
					if((position < 12 && position > 0) || (position > 100 && position < 112)){
						ps.setInt(2, equip.getItemId());
						ps.setInt(3, equip.getPosition());
						ps.addBatch();
					}
				}
				ps.executeBatch();
				ps.close();
				rs.close();
				ps = con.prepareStatement("SELECT * FROM playernpcs WHERE ScriptId = ?");
				ps.setInt(1, scriptId);
				rs = ps.executeQuery();
				rs.next();
				PlayerNPC pn = new PlayerNPC(rs);
				for(Channel channel : Server.getInstance().getChannelsFromWorld(world)){
					MapleMap m = channel.getMapFactory().getMap(getMapId());
					m.broadcastMessage(MaplePacketCreator.spawnPlayerNPC(pn));
					m.broadcastMessage(MaplePacketCreator.getPlayerNPC(pn));
					m.addMapObject(pn);
				}
			}
			ps.close();
			rs.close();
		}catch(SQLException e){
			e.printStackTrace();
		}
	}*/
	private void playerDead(MapleCharacter damager){
		cancelAllBuffs();
		dispelDebuffs();
		if(getEventInstance() != null){
			getEventInstance().playerKilled(this);
		}
		int[] charmID = {5130000, 4031283, 4140903};
		int possesed = 0;
		int i;
		for(i = 0; i < charmID.length; i++){
			int quantity = getItemQuantity(charmID[i], false);
			if(possesed == 0 && quantity > 0){
				possesed = quantity;
				break;
			}
		}
		if(possesed > 0 && !isHardMode()){
			message("You have used a safety charm, so your EXP points have not been decreased.");
			MapleInventoryManipulator.removeById(client, ItemInformationProvider.getInstance().getInventoryType(charmID[i]), charmID[i], 1, true, true);
		}else if(mapid > 925020000 && mapid < 925030000){
			this.dojoStage = 0;
		}else if(getJob() != MapleJob.BEGINNER && getJob() != MapleJob.NOBLESSE && getJob() != MapleJob.LEGEND && !isHardMode()){ // Hmm...
			if(!getMap().getMapData().isTown() && !FieldLimit.EXPLOSS_PORTALSCROLL.check(getMap().getMapData().getFieldLimit()) && !FieldLimit.NOEXPDECREASE.check(getMap().getMapData().getFieldLimit())){
				double XPdummy = getExp();
				XPdummy *= (1.00 - (getRSSkillLevel(RSSkill.Prayer) * 0.01));
				gainExp(new ExpProperty(ExpGainType.DEATH).gain((int) -XPdummy));
			}
		}
		if(getBuffedValue(MapleBuffStat.MORPH) != null){
			cancelEffectFromBuffStat(MapleBuffStat.MORPH);
		}
		if(getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null){
			cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
		}
		if(getChair() == -1){
			setChair(0);
			client.announce(MaplePacketCreator.cancelChair(-1));
			getMap().broadcastMessage(this, MaplePacketCreator.showChair(getId(), 0), false);
		}
		if(isHardMode()){
			long shit = MapleJob.getBy5ByteEncoding(job);
			if(shit <= 16){
				changeJob(MapleJob.BEGINNER);
			}else if(shit <= 32){
				changeJob(MapleJob.PIRATE);
			}else if(shit <= 32768){
				changeJob(MapleJob.NOBLESSE);
			}
			remainingAp = 0;
			localmaxhp = 50;
			hp = 50;
			localmaxmp = 5;
			mp = 5;
			exp.set(0);
			level = 1;
			maxhp = 50;
			maxmp = 5;
			str = 4;
			dex = 4;
			int_ = 4;
			luk = 4;
			meso.set(0);
			keymap.clear();
			for(int in = 0; in < DEFAULT_KEY.length; in++){
				keymap.put(DEFAULT_KEY[in], new MapleKeyBinding(DEFAULT_TYPE[in], DEFAULT_ACTION[in]));
			}
			for(int in = 0; in < 5; in++){
				trockmaps.add(999999999);
			}
			for(int in = 0; in < 10; in++){
				viptrockmaps.add(999999999);
			}
			List<Pair<MapleStat, Integer>> statup = new ArrayList<>(10);
			statup.add(new Pair<>(MapleStat.AVAILABLEAP, remainingAp));
			statup.add(new Pair<>(MapleStat.HP, localmaxhp));
			statup.add(new Pair<>(MapleStat.MP, localmaxmp));
			statup.add(new Pair<>(MapleStat.EXP, exp.get()));
			statup.add(new Pair<>(MapleStat.LEVEL, level));
			statup.add(new Pair<>(MapleStat.MAXHP, maxhp));
			statup.add(new Pair<>(MapleStat.MAXMP, maxmp));
			statup.add(new Pair<>(MapleStat.STR, str));
			statup.add(new Pair<>(MapleStat.DEX, dex));
			statup.add(new Pair<>(MapleStat.INT, int_));
			statup.add(new Pair<>(MapleStat.LUK, luk));
			statup.add(new Pair<>(MapleStat.MESO, meso.get()));
			for(int p = 0; p < remainingSp.length; p++){
				remainingSp[p] = 0;
			}
			statup.add(new Pair<>(MapleStat.AVAILABLESP, 0));
			inventory = new MapleInventory[MapleInventoryType.values().length];// wipe inventory completely
			savedLocations = new SavedLocation[SavedLocationType.values().length];// Wipe more shit
			skillMacros = new SkillMacro[5];// Wipe skill macros
			quests = new LinkedHashMap<>();
			for(MapleInventoryType type : MapleInventoryType.values()){// Reset inventory slots
				byte b = 24;
				if(type == MapleInventoryType.CASH){
					b = 96;
				}
				inventory[type.ordinal()] = new MapleInventory(type, (byte) b);
			}
			client.announce(CWvsContext.updatePlayerStats(statup, this));
			for(int l = 1; l < 10; l++){// Set to lvl 10
				levelUp(false);
			}
			// Update all the players shit we wiped clean
			sendKeymap();
			sendMacros();
			announce(CStage.getCharInfo(this));
			for(RSSkill skill : RSSkill.values()){
				rsSkillLevel.put(skill, (byte) 1);
				rsSkillExp.put(skill, (long) 0);
			}
			MapleInventoryManipulator.addFromDrop(getClient(), new Equip(1302000, getInventory(MapleInventoryType.EQUIP).getNextFreeSlot()), false);
			changeMap(100000000);
		}else{
			if(FeatureSettings.REVIVE_MOB){
				if(!isIronMan()){
					if(getLevel() >= 10 && !map.containsAnAliveBoss()){
						MapleMonster mob = MapleLifeFactory.getMonster(100);
						if(mob != null){
							mob.setOwner(getName());
							getMap().spawnMonsterOnGroundBelow(mob, getPosition(), true);
						}
					}
				}
			}
		}
		client.announce(CWvsContext.enableActions());
	}

	private void prepareDragonBlood(final MapleStatEffect bloodEffect){
		if(dragonBloodSchedule != null){
			dragonBloodSchedule.cancel(false);
		}
		dragonBloodSchedule = TimerManager.getInstance().register("dragonBlood", new Runnable(){

			@Override
			public void run(){
				addHP(-bloodEffect.getX());
				client.announce(UserLocal.UserEffect.showOwnBuffEffect(bloodEffect.getSourceId(), 5));
				getMap().broadcastMessage(MapleCharacter.this, UserRemote.UserEffect.showBuffeffect(getId(), bloodEffect.getSourceId(), 5), false);
			}
		}, 4000, 4000);
	}

	public void recalcLocalStats(){
		if(stats == null){// Shouldn't happen. I don't want errors tho.
			stats = new PlayerStats();
		}
		int oldmaxhp = localmaxhp;
		localmaxhp = getMaxHp();
		localmaxmp = getMaxMp();
		localdex = getDex();
		localint = getInt();
		localstr = getStr();
		localluk = getLuk();
		double STRr = 0, DEXr = 0, INTr = 0, LUKr = 0, MHPr = 0, MMPr = 0, ACCr = 0;
		// int speed = 100, jump = 100;
		magic = localint;
		watk = 0;
		ItemInformationProvider ii = ItemInformationProvider.getInstance();
		for(Item item : getInventory(MapleInventoryType.EQUIPPED)){
			Equip equip = (Equip) item;
			if(equip.getDurability() == 0 && equip.getDurability() != -1) continue;
			localmaxhp += equip.getHp();
			localmaxmp += equip.getMp();
			localdex += equip.getDex();
			localint += equip.getInt();
			localstr += equip.getStr();
			localluk += equip.getLuk();
			magic += equip.getMatk() + equip.getInt();
			watk += equip.getWatk();
			for(short option : equip.getOptionArray()){
				if(option != 0){
					Potential pot = ii.potentials.get(option);
					if(pot == null) continue;
					PotentialLevelData data = pot.getLevelData(equip);
					if(data == null) continue;
					localstr += data.incSTR;
					STRr += data.incSTRr;
					localdex += data.incDEX;
					DEXr += data.incDEXr;
					localint += data.incINT;
					INTr += data.incINTr;
					localluk += data.incLUK;
					LUKr += data.incLUKr;
					//
				}
			}
			// speed += equip.getSpeed();
			// jump += equip.getJump();
		}
		// Handles passives
		for(Entry<Skill, SkillEntry> skillData : this.getSkills().entrySet()){
			if(skillData.getValue().skillevel > 0){
				if(skillData.getKey().getId() % 10000 < 1000){
					MapleStatEffect effect = skillData.getKey().getEffect(skillData.getValue().skillevel);
					watk += effect.getWatk();
					magic += effect.getMatk();
					switch (skillData.getKey().getId()){
						case Evan.MAGIC_MASTERY:{
							magic += effect.getX();
							break;
						}
						case Beginner.BLESSING_OF_THE_FAIRY:
						case Noblesse.BLESSING_OF_THE_FAIRY:
						case Legend.BLESSING_OF_THE_FAIRY:
						case Evan.BLESSING_OF_THE_FAIRY:
						case Citizen.BLESSING_OF_THE_FAIRY:
							watk += effect.getX();
							magic += effect.getY();
							break;
					}
				}
			}
		}
		Integer mwarr = getBuffedValue(MapleBuffStat.MAPLE_WARRIOR);
		if(mwarr != null){
			STRr += mwarr;
			DEXr += mwarr;
			INTr += mwarr;
			LUKr += mwarr;
		}
		Integer matkbuff = getBuffedValue(MapleBuffStat.MATK);
		if(matkbuff != null){
			magic += matkbuff;
		}
		Integer watkbuff = getBuffedValue(MapleBuffStat.WATK);
		if(watkbuff != null){
			watk += watkbuff;
		}
		Integer hbhp = getBuffedValue(MapleBuffStat.HYPERBODYHP);
		if(hbhp != null){
			localmaxhp += (hbhp.doubleValue() / 100) * localmaxhp;
		}
		Integer hbmp = getBuffedValue(MapleBuffStat.HYPERBODYMP);
		if(hbmp != null){
			localmaxmp += (hbmp.doubleValue() / 100) * localmaxmp;
		}
		MapleStatEffect combo = getBuffEffect(MapleBuffStat.ARAN_COMBO);
		if(combo != null){
			watk += combo.getX();
		}
		if(energybar == 15000){
			Skill energycharge = isCygnus() ? SkillFactory.getSkill(ThunderBreaker.ENERGY_CHARGE) : SkillFactory.getSkill(Marauder.ENERGY_CHARGE);
			MapleStatEffect ceffect = energycharge.getEffect(getSkillLevel(energycharge));
			watk += ceffect.getWatk();
		}
		//
		Integer speedbuff = getBuffedValue(MapleBuffStat.SPEED);
		if(speedbuff != null){
			// speed += speedbuff;
		}
		Integer jumpbuff = getBuffedValue(MapleBuffStat.JUMP);
		if(jumpbuff != null){
			// jump += jumpbuff;
		}
		if(job.isA(MapleJob.THIEF) || job.isA(MapleJob.BOWMAN) || job.isA(MapleJob.PIRATE) || job.isA(MapleJob.NIGHTWALKER1) || job.isA(MapleJob.WINDARCHER1)){
			Item weapon_item = getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11);
			if(weapon_item != null){
				MapleWeaponType weapon = ItemInformationProvider.getInstance().getWeaponType(weapon_item.getItemId());
				boolean bow = weapon == MapleWeaponType.BOW;
				boolean crossbow = weapon == MapleWeaponType.CROSSBOW;
				boolean claw = weapon == MapleWeaponType.CLAW;
				boolean gun = weapon == MapleWeaponType.GUN;
				if(bow || crossbow || claw || gun){
					// Also calc stars into this.
					MapleInventory inv = getInventory(MapleInventoryType.USE);
					for(short i = 1; i <= inv.getSlotLimit(); i++){
						Item item = inv.getItem(i);
						if(item != null){
							if((claw && ItemConstants.isThrowingStar(item.getItemId())) || (gun && ItemConstants.isBullet(item.getItemId())) || (bow && ItemConstants.isArrowForBow(item.getItemId())) || (crossbow && ItemConstants.isArrowForCrossBow(item.getItemId()))){
								if(item.getQuantity() > 0){
									// Finally there!
									watk += ItemInformationProvider.getInstance().getItemData(item.getItemId()).incPAD;
									break;
								}
							}
						}
					}
				}
			}
		}
		if(STRr > 0) localstr += localstr * (STRr / 100);
		if(DEXr > 0) localdex += localdex * (DEXr / 100);
		if(INTr > 0) localint += localint * (INTr / 100);
		if(LUKr > 0) localluk += localluk * (LUKr / 100);
		// Cap shit
		magic = Math.min(magic, 2000);
		localmaxhp = Math.min(30000, localmaxhp);
		localmaxmp = Math.min(30000, localmaxmp);
		//
		if(oldmaxhp != 0 && oldmaxhp != localmaxhp){
			updatePartyCharacter();
		}
		stats.recalcLocalStats(this);// This is good here? Having all the updated stats before doing this sounds good.
		playerStat.reset();
		playerStat.setFrom(this);
	}

	/**
	 * Grabs the MapleParty and updates any information from the members.
	 */
	public void receivePartyMembers(){
		if(isInParty()){
			int channel = client.getChannel();
			for(MaplePartyCharacter partychar : getParty().getMembers()){
				if(partychar.getMapId() == getMapId() && partychar.getChannel() == channel){
					client.announce(CWvsContext.Party.updatePartyMemberHP(partychar.getId(), partychar.getHP(), partychar.getMaxHP()));
					/*MapleCharacter other = Server.getInstance().getWorld(world).getChannel(channel).getPlayerStorage().getCharacterByName(partychar.getName());
					if(other != null){
						client.announce(MaplePacketCreator.updatePartyMemberHP(other.getId(), other.getHp(), other.getCurrentMaxHp()));
					}*/
				}
			}
		}
	}

	/**
	 * Updates the MapleParty in the WorldServer with a completely updated MaplePartyCharacter.
	 * Does a silent update so it requires an upgraded grab.
	 */
	public void updatePartyCharacter(){
		if(isInParty()){
			this.mpc = new MaplePartyCharacter(this);
			try{
				ChannelServer.getInstance().getWorldInterface().updateParty(partyid, PartyOperation.SILENT_UPDATE, mpc);
				// ChannelServer.getInstance().getWorldInterface().broadcastPacket(target, CWvsContext.Party.updatePartyMemberHP(getId(), this.hp, this.localmaxhp));
			}catch(Exception ex){
				Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
				dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
			}
			/*for(MaplePartyCharacter partychar : party.getMembers()){
				if(partychar.getMapId() == getMapId() && partychar.getChannel() == channel){
					List<Integer> target = new ArrayList<>();
					target.add(partychar.getId());
					try{
						ChannelServer.getInstance().getWorldInterface().broadcastPacket(target, CWvsContext.Party.updatePartyMemberHP(getId(), this.hp, this.localmaxhp));
					}catch(Exception ex){
						Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
					}
				}
			}*/
		}
	}

	public void registerEffect(MapleStatEffect effect, long starttime, long duration){
		if(effect.isDragonBlood()){
			prepareDragonBlood(effect);
		}else if(effect.isBerserk()){
			checkBerserk();
		}else if(effect.isRecovery()){
			lastRecoveryTime = starttime;
		}
		for(Pair<MapleBuffStat, BuffDataHolder> statup : effect.getStatups()){
			effects.put(statup.getLeft(), new MapleBuffStatDataHolder(effect, starttime, duration, statup.getRight()));
		}
		// dropMessage("recalcLocalStats() in registerEffect()");
		recalcLocalStats();
	}

	public void removeAllCooldownsExcept(int id, boolean packet){
		for(MapleCoolDownValueHolder mcvh : coolDowns.values()){
			if(mcvh.skillId != id){
				coolDowns.remove(mcvh.skillId);
				if(packet){
					client.announce(MaplePacketCreator.skillCooldown(mcvh.skillId, 0));
				}
			}
		}
	}

	public static void removeAriantRoom(int room){
		ariantroomleader[room] = "";
		ariantroomslot[room] = 0;
	}

	public void removeCooldown(int skillId){
		if(this.coolDowns.containsKey(skillId)){
			this.coolDowns.remove(skillId);
		}
	}

	public void removePet(MaplePet pet, boolean shift_left){
		int slot = -1;
		for(int i = 0; i < 3; i++){
			if(pets[i] != null){
				if(pets[i].getUniqueId() == pet.getUniqueId()){
					pets[i] = null;
					slot = i;
					break;
				}
			}
		}
		if(shift_left){
			if(slot > -1){
				for(int i = slot; i < 3; i++){
					if(i != 2){
						pets[i] = pets[i + 1];
					}else{
						pets[i] = null;
					}
				}
			}
		}
	}

	public void removeVisibleMapObject(MapleMapObject mo){
		visibleMapObjects.remove(mo);
	}

	public void resetStats(){
		List<Pair<MapleStat, Integer>> statup = new ArrayList<>(5);
		int tap = 0, tsp = 1;
		int tstr = 4, tdex = 4, tint = 4, tluk = 4;
		int levelap = (isCygnus() ? 6 : 5);
		switch (job.getId()){
			case 100:
			case 1100:
			case 2100:// ?
				tstr = 35;
				tap = ((getLevel() - 10) * levelap) + 14;
				tsp += ((getLevel() - 10) * 3);
				break;
			case 200:
			case 1200:
				tint = 20;
				tap = ((getLevel() - 8) * levelap) + 29;
				tsp += ((getLevel() - 8) * 3);
				break;
			case 2200:
				tint = 20;
				tap = ((getLevel() - 10) * levelap) + 38;
				tsp += ((getLevel() - 10) * 3);
				break;
			case 300:
			case 1300:
			case 400:
			case 1400:
				tdex = 25;
				tap = ((getLevel() - 10) * levelap) + 24;
				tsp += ((getLevel() - 10) * 3);
				break;
			case 500:
			case 1500:
				tdex = 20;
				tap = ((getLevel() - 10) * levelap) + 29;
				tsp += ((getLevel() - 10) * 3);
				break;
		}
		this.remainingAp = tap;
		setRemainingSp(tsp, getJobIndexBasedOnLevel());
		this.dex = tdex;
		this.int_ = tint;
		this.str = tstr;
		this.luk = tluk;
		statup.add(new Pair<>(MapleStat.AVAILABLEAP, tap));
		statup.add(new Pair<>(MapleStat.AVAILABLESP, tsp));
		statup.add(new Pair<>(MapleStat.STR, tstr));
		statup.add(new Pair<>(MapleStat.DEX, tdex));
		statup.add(new Pair<>(MapleStat.INT, tint));
		statup.add(new Pair<>(MapleStat.LUK, tluk));
		announce(CWvsContext.updatePlayerStats(statup, this));
	}

	public void resetBattleshipHp(){
		// this.battleshipHp = 4000 * getSkillLevel(SkillFactory.getSkill(Corsair.BATTLE_SHIP)) + ((getLevel() - 120) * 2000);
		this.battleshipHp = 300 * getLevel() + 500 * (getSkillLevel(SkillFactory.getSkill(Corsair.BATTLE_SHIP)) - 72);
	}

	public void resetEnteredScript(){
		if(entered.containsKey(map.getId())){
			entered.remove(map.getId());
		}
	}

	public void resetEnteredScript(int mapId){
		if(entered.containsKey(mapId)){
			entered.remove(mapId);
		}
	}

	public void resetEnteredScript(String script){
		for(int mapId : entered.keySet()){
			if(entered.get(mapId).equals(script)){
				entered.remove(mapId);
			}
		}
	}

	public void resetMGC(){
		this.mgc = null;
	}

	public synchronized void saveCooldowns(){
		if(getAllCooldowns().size() > 0){
			try{
				Connection con = DatabaseConnection.getConnection();
				deleteWhereCharacterId(con, "DELETE FROM cooldowns WHERE charid = ?");
				try(PreparedStatement ps = con.prepareStatement("INSERT INTO cooldowns (charid, SkillID, StartTime, length) VALUES (?, ?, ?, ?)")){
					ps.setInt(1, getId());
					for(PlayerCoolDownValueHolder cooling : getAllCooldowns()){
						ps.setInt(2, cooling.skillId);
						ps.setLong(3, cooling.startTime);
						ps.setLong(4, cooling.length);
						ps.addBatch();
					}
					ps.executeBatch();
				}
			}catch(SQLException se){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, se);
			}
		}
	}

	public void saveGuildStatus(){
		try{
			try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characters SET guildid = ?, guildrank = ?, gp = ?, allianceRank = ? WHERE id = ?")){
				ps.setInt(1, guildid);
				ps.setInt(2, guildrank);
				ps.setInt(3, gp);
				ps.setInt(4, allianceRank);
				ps.setInt(5, id);
				ps.execute();
			}
		}catch(SQLException se){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, se);
		}
	}

	public void saveLocation(String type){
		MaplePortal closest = map.findClosestPortal(getPosition());
		savedLocations[SavedLocationType.fromString(type).ordinal()] = new SavedLocation(getMapId(), closest != null ? closest.getId() : 0);
	}

	public final boolean insertNewChar(){
		final Connection con = DatabaseConnection.getConnection();
		PreparedStatement ps = null;
		try{
			con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			con.setAutoCommit(false);
			ps = con.prepareStatement("INSERT INTO characters (str, dex, luk, `int`, gm, skincolor, gender, job, subjob, hair, face, map, meso, spawnpoint, accountid, name, world, progressValues) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", DatabaseConnection.RETURN_GENERATED_KEYS);
			int pos = 0;
			ps.setInt(++pos, 12);
			ps.setInt(++pos, 5);
			ps.setInt(++pos, 4);
			ps.setInt(++pos, 4);
			ps.setInt(++pos, gmLevel);
			ps.setInt(++pos, skinColor.getId());
			ps.setInt(++pos, gender);
			ps.setInt(++pos, getJob().getId());
			ps.setShort(++pos, nSubJob);
			ps.setInt(++pos, hair);
			ps.setInt(++pos, face);
			ps.setInt(++pos, mapid);
			ps.setInt(++pos, Math.abs(meso.get()));
			ps.setInt(++pos, 0);
			ps.setInt(++pos, accountid);
			ps.setString(++pos, name);
			ps.setInt(++pos, world);
			ps.setString(++pos, progressValues);
			int updateRows = ps.executeUpdate();
			if(updateRows < 1){
				ps.close();
				Logger.log(LogType.WARNING, LogFile.GENERAL_ERROR, "Error inserting new character ({}) into the database. {}", name, ps.toString());
				return false;
			}
			ResultSet rs = ps.getGeneratedKeys();
			if(rs.next()){
				this.id = rs.getInt(1);
				rs.close();
				ps.close();
			}else{
				rs.close();
				ps.close();
				Logger.log(LogType.WARNING, LogFile.GENERAL_ERROR, "Error inserting new character ({}) into the database. {}", name, ps.toString());
				return false;
			}
			ps = con.prepareStatement("INSERT INTO keymap (characterid, `key`, `type`, `action`) VALUES (?, ?, ?, ?)");
			ps.setInt(1, id);
			for(int i = 0; i < DEFAULT_KEY.length; i++){
				ps.setInt(2, DEFAULT_KEY[i]);
				ps.setInt(3, DEFAULT_TYPE[i]);
				ps.setInt(4, DEFAULT_ACTION[i]);
				ps.execute();
			}
			ps.close();
			final List<Pair<Item, MapleInventoryType>> itemsWithType = new ArrayList<>();
			for(MapleInventory iv : inventory){
				for(Item item : iv.list()){
					itemsWithType.add(new Pair<>(item, iv.getType()));
				}
			}
			ItemFactory.INVENTORY.saveItems(itemsWithType, id, con);
			con.commit();
			return true;
		}catch(Throwable t){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, t, "Error inserting new character (" + name + ") into the database.");
			try{
				con.rollback();
			}catch(SQLException se){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, t, "Error trying to rollback new character. (" + name + ")");
			}
			return false;
		}finally{
			try{
				if(ps != null && !ps.isClosed()){
					ps.close();
				}
				con.setAutoCommit(true);
				con.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
			}catch(SQLException e){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
			}
		}
	}

	// synchronize this call instead of trying to give access all at once (?)
	public synchronized void saveToDB(){
		if(id < 0 || isFakeLogin) return;// bot
		Connection con = DatabaseConnection.getConnection();
		try{
			int pos = 0;
			con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			con.setAutoCommit(false);
			PreparedStatement ps;
			ps = con.prepareStatement("UPDATE characters SET level = ?, fame = ?, str = ?, dex = ?, luk = ?, `int` = ?, exp = ?, gachaexp = ?, hp = ?, mp = ?, maxhp = ?, maxmp = ?, sp = ?, ap = ?, gm = ?, skincolor = ?, gender = ?, job = ?, subjob = ?, hair = ?, face = ?, map = ?, meso = ?, hpMpUsed = ?, spawnpoint = ?, party = ?, gp = ?, buddyCapacity = ?, messengerid = ?, messengerposition = ?, mountlevel = ?, mountexp = ?, mounttiredness= ?, equipslots = ?, useslots = ?, setupslots = ?, etcslots = ?, familyId = ?, monsterbookcover = ?, vanquisherStage = ?, dojoPoints = ?, lastDojoStage = ?, finishedDojoTutorial = ?, vanquisherKills = ?, matchcardwins = ?, matchcardlosses = ?, matchcardties = ?, omokwins = ?, omoklosses = ?, omokties = ?, dataString = ?, ironMan = ?, hardmode = ?, rsSkillLevel = ?, rsSkillExp = ?, marriedto = ?, marriageringid = ?, engagementringid = ?, marriageid = ?, progressValues = ?, reincarnations = ?, tasksCompleted = ?, playtime = ?, highestLevel = ?, bossEntries = ? WHERE id = ?", Statement.RETURN_GENERATED_KEYS);
			if(gmLevel < 1 && level > 199){
				ps.setInt(++pos, 200);
			}else{
				ps.setInt(++pos, level);
			}
			ps.setInt(++pos, fame);
			ps.setInt(++pos, str);
			ps.setInt(++pos, dex);
			ps.setInt(++pos, luk);
			ps.setInt(++pos, int_);
			ps.setInt(++pos, Math.abs(exp.get()));
			ps.setInt(++pos, Math.abs(gachaexp.get()));
			ps.setInt(++pos, hp);
			ps.setInt(++pos, mp);
			ps.setInt(++pos, maxhp);
			ps.setInt(++pos, maxmp);
			StringBuilder sps = new StringBuilder();
			for(int i = 0; i < remainingSp.length; i++){
				sps.append(remainingSp[i]);
				sps.append(",");
			}
			String sp = sps.toString();
			ps.setString(++pos, sp.substring(0, sp.length() - 1));
			ps.setInt(++pos, remainingAp);
			ps.setInt(++pos, gmLevel);
			ps.setInt(++pos, skinColor.getId());
			ps.setInt(++pos, gender);
			ps.setInt(++pos, job.getId());
			ps.setShort(++pos, nSubJob);
			ps.setInt(++pos, hair);
			ps.setInt(++pos, face);
			if(map == null || (cashshop != null && cashshop.isOpened())){
				ps.setInt(++pos, mapid);
			}else{
				if(map.getMapData().getForcedReturnMap() != 999999999){
					ps.setInt(++pos, map.getMapData().getForcedReturnMap());
				}else{
					ps.setInt(++pos, getHp() < 1 ? map.getMapData().getForcedReturnMap() : map.getId());
				}
			}
			ps.setInt(++pos, meso.get());
			ps.setInt(++pos, hpMpApUsed);
			if(map == null || map.getId() == 610020000 || map.getId() == 610020001){
				ps.setInt(++pos, 0);
			}else{
				MaplePortal closest = map.findClosestSpawnpoint(getPosition());
				if(closest != null){
					ps.setInt(++pos, closest.getId());
				}else{
					ps.setInt(++pos, 0);
				}
			}
			ps.setInt(++pos, partyid);
			ps.setInt(++pos, gp);
			ps.setInt(++pos, buddylist.getCapacity());
			if(messenger != null){
				ps.setInt(++pos, messenger.getId());
				ps.setInt(++pos, messengerposition);
			}else{
				ps.setInt(++pos, 0);
				ps.setInt(++pos, 4);
			}
			if(maplemount != null){
				ps.setInt(++pos, maplemount.getLevel());
				ps.setInt(++pos, maplemount.getExp());
				ps.setInt(++pos, maplemount.getTiredness());
			}else{
				ps.setInt(++pos, 1);
				ps.setInt(++pos, 0);
				ps.setInt(++pos, 0);
			}
			for(int i = 1; i < 5; i++){
				ps.setInt(++pos, getSlots(i));
			}
			ps.setInt(++pos, familyId);
			monsterbook.saveCards(getId());
			ps.setInt(++pos, bookCover);
			ps.setInt(++pos, vanquisherStage);
			ps.setInt(++pos, dojoPoints);
			ps.setInt(++pos, dojoStage);
			ps.setInt(++pos, finishedDojoTutorial ? 1 : 0);
			ps.setInt(++pos, vanquisherKills);
			ps.setInt(++pos, matchcardwins);
			ps.setInt(++pos, matchcardlosses);
			ps.setInt(++pos, matchcardties);
			ps.setInt(++pos, omokwins);
			ps.setInt(++pos, omoklosses);
			ps.setInt(++pos, omokties);
			ps.setString(++pos, dataString);
			ps.setInt(++pos, ironMan);
			ps.setInt(++pos, hardmode);
			StringBuilder dataOut = new StringBuilder("");
			for(RSSkill skill : rsSkillLevel.keySet()){
				dataOut.append(skill.name()).append("=").append(rsSkillLevel.get(skill)).append(",");
			}
			dataOut.setLength(dataOut.length() - ",".length());
			ps.setString(++pos, dataOut.toString());
			dataOut = new StringBuilder("");
			for(RSSkill skill : rsSkillExp.keySet()){
				dataOut.append(skill.name()).append("=").append(rsSkillExp.get(skill)).append(",");
			}
			dataOut.setLength(dataOut.length() - ",".length());
			ps.setString(++pos, dataOut.toString());
			ps.setInt(++pos, marriedto);
			ps.setInt(++pos, marriageringid);
			ps.setInt(++pos, engagementringid);
			ps.setInt(++pos, marriageid);
			ps.setString(++pos, progressValues);
			ps.setInt(++pos, reincarnations);
			ps.setInt(++pos, tasksCompleted);
			ps.setLong(++pos, getPlaytime());
			ps.setInt(++pos, highestLevel);
			bossEntries.saveToTable(ps, ++pos);
			ps.setInt(++pos, id);
			int updateRows = ps.executeUpdate();
			if(updateRows < 1){ throw new RuntimeException("Character not in database (" + id + ")"); }
			for(int i = 0; i < 3; i++){
				if(pets[i] != null){
					pets[i].saveToDb();
				}
			}
			deleteWhereCharacterId(con, "DELETE FROM keymap WHERE characterid = ?");
			ps = con.prepareStatement("INSERT INTO keymap (characterid, `key`, `type`, `action`) VALUES (?, ?, ?, ?)");
			ps.setInt(1, id);
			for(Entry<Integer, MapleKeyBinding> keybinding : keymap.entrySet()){
				ps.setInt(2, keybinding.getKey().intValue());
				ps.setInt(3, keybinding.getValue().getType());
				ps.setInt(4, keybinding.getValue().getAction());
				ps.addBatch();
			}
			ps.executeBatch();
			deleteWhereCharacterId(con, "DELETE FROM skillmacros WHERE characterid = ?");
			ps = con.prepareStatement("INSERT INTO skillmacros (characterid, skill1, skill2, skill3, name, shout, position) VALUES (?, ?, ?, ?, ?, ?, ?)");
			ps.setInt(1, getId());
			for(int i = 0; i < 5; i++){
				SkillMacro macro = skillMacros[i];
				if(macro != null){
					ps.setInt(2, macro.getSkill1());
					ps.setInt(3, macro.getSkill2());
					ps.setInt(4, macro.getSkill3());
					ps.setString(5, macro.getName());
					ps.setInt(6, macro.getShout());
					ps.setInt(7, i);
					ps.addBatch();
				}
			}
			ps.executeBatch();
			saveInventory(con);
			deleteWhereCharacterId(con, "DELETE FROM skills WHERE characterid = ?");
			ps = con.prepareStatement("INSERT INTO skills (characterid, skillid, skilllevel, masterlevel, expiration) VALUES (?, ?, ?, ?, ?)");
			ps.setInt(1, id);
			for(Entry<Skill, SkillEntry> skill : skills.entrySet()){
				ps.setInt(2, skill.getKey().getId());
				ps.setInt(3, skill.getValue().skillevel);
				ps.setInt(4, skill.getValue().masterlevel);
				ps.setLong(5, skill.getValue().expiration);
				ps.addBatch();
			}
			ps.executeBatch();
			deleteWhereCharacterId(con, "DELETE FROM savedlocations WHERE characterid = ?");
			ps = con.prepareStatement("INSERT INTO savedlocations (characterid, `locationtype`, `map`, `portal`) VALUES (?, ?, ?, ?)");
			ps.setInt(1, id);
			for(SavedLocationType savedLocationType : SavedLocationType.values()){
				if(savedLocations[savedLocationType.ordinal()] != null){
					ps.setString(2, savedLocationType.name());
					ps.setInt(3, savedLocations[savedLocationType.ordinal()].getMapId());
					ps.setInt(4, savedLocations[savedLocationType.ordinal()].getPortal());
					ps.addBatch();
				}
			}
			ps.executeBatch();
			deleteWhereCharacterId(con, "DELETE FROM trocklocations WHERE characterid = ?");
			ps = con.prepareStatement("INSERT INTO trocklocations(characterid, mapid, vip) VALUES (?, ?, 0)");
			for(int i = 0; i < getTrockSize(); i++){
				if(trockmaps.get(i) != 999999999){
					ps.setInt(1, getId());
					ps.setInt(2, trockmaps.get(i));
					ps.addBatch();
				}
			}
			ps.executeBatch();
			ps = con.prepareStatement("INSERT INTO trocklocations(characterid, mapid, vip) VALUES (?, ?, 1)");
			for(int i = 0; i < getVipTrockSize(); i++){
				if(viptrockmaps.get(i) != 999999999){
					ps.setInt(1, getId());
					ps.setInt(2, viptrockmaps.get(i));
					ps.addBatch();
				}
			}
			ps.executeBatch();
			deleteWhereCharacterId(con, "DELETE FROM buddies WHERE characterid = ? AND pending = 0");
			ps = con.prepareStatement("INSERT INTO buddies (characterid, `buddyid`, `pending`, `group`) VALUES (?, ?, 0, ?)");
			ps.setInt(1, id);
			for(BuddylistEntry entry : buddylist.getBuddies()){
				if(entry.isVisible()){
					ps.setInt(2, entry.getCharacterId());
					ps.setString(3, entry.getGroup());
					ps.addBatch();
				}
			}
			ps.executeBatch();
			deleteWhereCharacterId(con, "DELETE FROM area_info WHERE charid = ?");
			ps = con.prepareStatement("INSERT INTO area_info (id, charid, area, info) VALUES (DEFAULT, ?, ?, ?)");
			ps.setInt(1, id);
			for(Entry<Short, String> area : area_info.entrySet()){
				ps.setInt(2, area.getKey());
				ps.setString(3, area.getValue());
				ps.addBatch();
			}
			ps.executeBatch();
			deleteWhereCharacterId(con, "DELETE FROM eventstats WHERE characterid = ?");
			deleteWhereCharacterId(con, "DELETE FROM queststatus WHERE characterid = ?");
			ps = con.prepareStatement("INSERT INTO queststatus (`queststatusid`, `characterid`, `quest`, `status`, `time`, `forfeited`) VALUES (DEFAULT, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			PreparedStatement psf;
			try(PreparedStatement pse = con.prepareStatement("INSERT INTO questprogress VALUES (DEFAULT, ?, ?, ?)")){
				psf = con.prepareStatement("INSERT INTO medalmaps VALUES (DEFAULT, ?, ?)");
				ps.setInt(1, id);
				for(MapleQuestStatus q : quests.values()){
					ps.setInt(2, q.getQuest().getId());
					ps.setInt(3, q.getStatus().getId());
					ps.setInt(4, (int) (q.getCompletionTime() / 1000));
					ps.setInt(5, q.getForfeited());
					ps.executeUpdate();
					try(ResultSet rs = ps.getGeneratedKeys()){
						rs.next();
						for(int mob : q.getProgress().keySet()){
							String progress = q.getProgress(mob);
							if(!progress.equals("000")){
								pse.setInt(1, rs.getInt(1));
								pse.setInt(2, mob);
								pse.setString(3, progress);
								pse.addBatch();
							}
						}
						for(int i = 0; i < q.getMedalMaps().size(); i++){
							psf.setInt(1, rs.getInt(1));
							psf.setInt(2, q.getMedalMaps().get(i));
							psf.addBatch();
						}
						pse.executeBatch();
						psf.executeBatch();
					}
				}
			}
			psf.close();
			deleteWhereCharacterId(con, "DELETE FROM SlayerTasks WHERE chrid = ?");
			if(slayerTask != null){
				ps = con.prepareStatement("INSERT INTO SlayerTasks(chrid, targetid, kills, requiredKills, map) VALUES(?, ?, ?, ?, ?)");
				ps.setInt(1, id);
				ps.setInt(2, slayerTask.getTargetID());
				ps.setInt(3, slayerTask.getKills());
				ps.setInt(4, slayerTask.getRequiredKills());
				ps.setString(5, slayerTask.getMap());
				ps.executeUpdate();
				ps.close();
			}
			Calendar cal = Calendar.getInstance();
			String yearMonth = cal.get(Calendar.YEAR) + "-" + cal.get(Calendar.MONDAY);
			try(PreparedStatement p = con.prepareStatement("DELETE FROM monsterkills WHERE id = ? AND date = ?")){
				p.setInt(1, id);
				p.setString(2, yearMonth);
				p.executeUpdate();
			}
			if(monsterKillTotal.containsKey(yearMonth) || monsterKillHigher.containsKey(yearMonth)){
				try(PreparedStatement p = con.prepareStatement("INSERT INTO monsterkills (id, date, total, higher) VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE id = VALUES(id), date = VALUES(date), total = VALUES(total), higher = VALUES(higher)")){
					p.setInt(1, id);
					p.setString(2, yearMonth);
					Long total = monsterKillTotal.get(yearMonth);
					if(total == null) total = 0L;
					p.setLong(3, total);
					Long higher = monsterKillHigher.get(yearMonth);
					if(higher == null) higher = 0L;
					p.setLong(4, higher);
					p.executeUpdate();
				}
			}
			ps = con.prepareStatement("UPDATE accounts SET gm = ?, eliteStart = ?, eliteLength = ?, autoSell = ?, autoSellItems = ?, autoSellInventories = ?, progressValues = ?, petvac = ?, lastNameChange = ? WHERE id = ?");
			pos = 0;
			ps.setInt(++pos, getClient().getGMLevel());
			ps.setLong(++pos, getClient().getEliteStart());
			ps.setLong(++pos, getClient().getEliteLength());
			ps.setBoolean(++pos, autoSell);
			StringBuilder sb = new StringBuilder();
			for(String item : autoSellIgnore){
				sb.append(item + ",");
			}
			ps.setString(++pos, sb.toString());
			sb = new StringBuilder();
			for(MapleInventoryType mit : autoSellInventoryTypeIgnore){
				sb.append(mit.name() + ",");
			}
			if(sb.length() > 0) sb.setLength(sb.length() - ",".length());
			ps.setString(++pos, sb.toString());
			sb = new StringBuilder();
			Map<String, Object> values = getClient().getProgressValues();
			if(values != null && !values.isEmpty()){
				for(String key : values.keySet()){
					sb.append(key + "=" + values.get(key) + ",");
				}
			}
			ps.setString(++pos, sb.toString());
			ps.setBoolean(++pos, client.getPetVac());
			ps.setLong(++pos, client.getLastNameChange());
			ps.setInt(++pos, client.getAccID());
			ps.executeUpdate();
			ps.close();
			con.commit();
			con.setAutoCommit(true);
			if(cashshop != null){
				cashshop.save(con);
			}
			if(storage != null){
				storage.saveToDB(con);
			}
		}catch(SQLException | RuntimeException t){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, t, "Error saving " + name + " Level: " + level + " Job: " + job.getId());
			try{
				con.rollback();
			}catch(SQLException se){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, se, "Error trying to rollback " + name);
			}
		}finally{
			try{
				con.setAutoCommit(true);
				con.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
			}catch(Exception e){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
			}
		}
	}

	public void saveInventory(Connection con) throws SQLException{
		List<Pair<Item, MapleInventoryType>> itemsWithType = new ArrayList<>();
		for(MapleInventory iv : inventory){
			for(Item item : iv.list()){
				itemsWithType.add(new Pair<>(item, iv.getType()));
			}
		}
		ItemFactory.INVENTORY.saveItems(itemsWithType, id, con);
	}

	public void sendPolice(int greason, String reason, int duration){
		announce(MaplePacketCreator.sendPolice(String.format("You have been blocked by the#b %s Police for %s.#k", "Vertisy", reason)));
		this.isbanned = true;
		TimerManager.getInstance().schedule("sendPolice", new Runnable(){

			@Override
			public void run(){
				client.disconnect(false, false);
			}
		}, duration);
	}

	public void sendPolice(String text){
		String message = getName() + " received this - " + text;
		//// if(Server.getInstance().isGmOnline()){ // Alert and log if a GM is online
		try{
			ChannelServer.getInstance().getWorldInterface().broadcastGMPacket(MaplePacketCreator.sendYellowTip(message));
		}catch(RemoteException | NullPointerException ex){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
		}
		// FilePrinter.print("autobanwarning.txt", message + "\r\n");
		// }else{ // Auto DC and log if no GM is online
		client.disconnect(false, false);
		Logger.log(LogType.INFO, LogFile.GENERAL_INFO, message);
		// }
		// Server.getInstance().broadcastGMMessage(0, MaplePacketCreator.serverNotice(1, getName() + " received this - " + text));
		// announce(MaplePacketCreator.sendPolice(text));
		// this.isbanned = true;
		// TimerManager.getInstance().schedule(new Runnable() {
		// @Override
		// public void run() {
		// client.disconnect(false, false);
		// }
		// }, 6000);
	}

	public void sendKeymap(){
		client.announce(FuncKeyMappedMan.getKeymap(keymap));
	}

	public void sendMacros(){
		// Always send the macro packet to fix a client side bug when switching characters.
		client.announce(CWvsContext.getMacros(skillMacros));
	}

	public void sendNote(String to, String msg, byte fame) throws SQLException{
		try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO notes (`to`, `from`, `message`, `timestamp`, `fame`) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)){
			ps.setString(1, to);
			ps.setString(2, this.getName());
			ps.setString(3, msg);
			ps.setLong(4, System.currentTimeMillis());
			ps.setByte(5, fame);
			ps.executeUpdate();
		}
	}

	public static void sendNote(String to, String from, String msg, byte fame) throws SQLException{
		try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO notes (`to`, `from`, `message`, `timestamp`, `fame`) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)){
			ps.setString(1, to);
			ps.setString(2, from);
			ps.setString(3, msg);
			ps.setLong(4, System.currentTimeMillis());
			ps.setByte(5, fame);
			ps.executeUpdate();
		}
	}

	public void setAllianceRank(int rank, boolean updateWorld){
		allianceRank = rank;
		if(mgc != null){
			mgc.setAllianceRank(rank);
			if(updateWorld){
				MapleGuild guild = getGuild();
				if(guild != null){
					try{
						ChannelServer.getInstance().getWorldInterface().setAllianceRank(this.guildid, this.id, rank);
					}catch(RemoteException | NullPointerException ex){
						Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
					}
					// guild.getMGC(mgc.getId()).setAllianceRank(rank);
				}
			}
		}
	}

	public static void setAriantRoomLeader(int room, String charname){
		ariantroomleader[room] = charname;
	}

	public static void setAriantSlotRoom(int room, int slot){
		ariantroomslot[room] = slot;
	}

	public void setBattleshipHp(int battleshipHp){
		this.battleshipHp = battleshipHp;
	}

	public void setBuddyCapacity(int capacity){
		buddylist.setCapacity(capacity);
		client.announce(CWvsContext.Friend.updateBuddyCapacity(capacity));
	}

	public void setBuffedValue(MapleBuffStat effect, int value){
		MapleBuffStatDataHolder mbsvh = effects.get(effect);
		if(mbsvh == null) return;
		mbsvh.value.setValue(value);
	}

	public void setChair(int chair){
		this.chair = chair;
	}

	public void setChalkboard(String text){
		this.chalktext = text;
	}

	public void setDex(int dex){
		this.dex = dex;
		recalcLocalStats();
	}

	public void setDojoEnergy(int x){
		this.dojoEnergy = x;
	}

	public void setDojoParty(boolean b){
		this.dojoParty = b;
	}

	public void setDojoPoints(int x){
		this.dojoPoints = x;
	}

	public void setDojoStage(int x){
		this.dojoStage = x;
	}

	public void setDojoStart(){
		this.dojoMap = map;
		// int stage = (map.getId() / 100) % 100;
		// this.dojoFinish = System.currentTimeMillis() + (stage > 36 ? 15 : stage / 6 + 5) * 60000;
		this.dojoFinish = System.currentTimeMillis() + 1 * 60 * 60000;
	}

	/*public void setRates() {
	    Calendar cal = Calendar.getInstance();
	    cal.setTimeZone(TimeZone.getTimeZone("GMT-8"));
	    World worldz = Server.getInstance().getWorld(world);
	    int hr = cal.get(Calendar.HOUR_OF_DAY);
	    if ((haveItem(5360001) && hr > 6 && hr < 12) || (haveItem(5360002) && hr > 9 && hr < 15) || (haveItem(536000) && hr > 12 && hr < 18) || (haveItem(5360004) && hr > 15 && hr < 21) || (haveItem(536000) && hr > 18) || (haveItem(5360006) && hr < 5) || (haveItem(5360007) && hr > 2 && hr < 6) || (haveItem(5360008) && hr >= 6 && hr < 11)) {
	        this.dropRate = 2 * worldz.getDropRate();
	        this.mesoRate = 2 * worldz.getMesoRate();
	    } else {
	        this.dropRate = worldz.getDropRate();
	        this.mesoRate = worldz.getMesoRate();
	    }
	    if ((haveItem(5211000) && hr > 17 && hr < 21) || (haveItem(5211014) && hr > 6 && hr < 12) || (haveItem(5211015) && hr > 9 && hr < 15) || (haveItem(5211016) && hr > 12 && hr < 18) || (haveItem(5211017) && hr > 15 && hr < 21) || (haveItem(5211018) && hr > 14) || (haveItem(5211039) && hr < 5) || (haveItem(5211042) && hr > 2 && hr < 8) || (haveItem(5211045) && hr > 5 && hr < 11) || haveItem(5211048)) {
	        if (isBeginnerJob()) {
	            this.expRate = 2;
	        } else {
	            this.expRate = 2 * worldz.getExpRate();
	        }
	    } else {
	        if (isBeginnerJob()) {
	            this.expRate = 1;
	        } else {
	            this.expRate = worldz.getExpRate();
	        }
	    }
	}*/
	public void setEnergyBar(int set){
		energybar = set;
	}

	public void setEventInstance(EventInstanceManager eventInstance){
		this.eventInstance = eventInstance;
	}

	public void setExp(int amount){
		this.exp.set(amount);
	}

	public void setGachaExp(int amount){
		this.gachaexp.set(amount);
	}

	public void setFace(int face){
		this.face = face;
	}

	public void setFame(int fame){
		this.fame = fame;
	}

	public void setFamilyId(int familyId){
		this.familyId = familyId;
	}

	public void setFinishedDojoTutorial(){
		this.finishedDojoTutorial = true;
	}

	public void setGender(int gender){
		this.gender = gender;
	}

	public void setGM(int level){
		this.gmLevel = level;
	}

	public void setGuildId(int _id){
		guildid = _id;
		if(guildid > 0){
			if(mgc == null){
				mgc = new MapleGuildCharacter(this);
			}else{
				mgc.setGuildId(guildid);
			}
		}else{
			mgc = null;
		}
	}

	public void setGuildRank(int _rank){
		guildrank = _rank;
		if(mgc != null){
			mgc.setGuildRank(_rank);
		}
	}

	public void setHair(int hair){
		this.hair = hair;
	}

	public void setHasMerchant(boolean set){
		try{
			try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characters SET HasMerchant = ? WHERE id = ?")){
				ps.setInt(1, set ? 1 : 0);
				ps.setInt(2, id);
				ps.executeUpdate();
			}
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
		hasMerchant = set;
	}

	public void addMerchantMesos(int add){
		try{
			try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characters SET MerchantMesos = ? WHERE id = ?", Statement.RETURN_GENERATED_KEYS)){
				ps.setInt(1, merchantmeso + add);
				ps.setInt(2, id);
				ps.executeUpdate();
			}
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
			return;
		}
		merchantmeso += add;
	}

	public void setMerchantMeso(int set){
		try{
			try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characters SET MerchantMesos = ? WHERE id = ?", Statement.RETURN_GENERATED_KEYS)){
				ps.setInt(1, set);
				ps.setInt(2, id);
				ps.executeUpdate();
			}
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
			return;
		}
		merchantmeso = set;
	}

	public void setHiredMerchant(HiredMerchant merchant){
		this.hiredMerchant = merchant;
	}

	public void setHp(int newhp){
		setHp(null, newhp, false);
	}

	public void setHp(MapleCharacter damager, int newhp){
		setHp(damager, newhp, false);
	}

	public void setHp(int newhp, boolean silent){
		setHp(null, newhp, silent);
	}

	public void setHp(MapleCharacter damager, int newhp, boolean silent){
		int oldHp = hp;
		int thp = newhp;
		if(thp < 0){
			thp = 0;
		}
		if(thp > localmaxhp){
			thp = localmaxhp;
		}
		this.hp = thp;
		if(!silent){
			updatePartyCharacter();
		}
		if(oldHp > hp && !isAlive()){
			playerDead(damager);
		}else{
			if(getMap() != null && FeatureSettings.REVIVE_MOB){
				for(MapleMonster monster : getMap().getMonsters()){
					if(monster.getOwner() != null && monster.getOwner().equals(getName())){
						getMap().killMonster(monster, null, false);
						break;
					}
				}
			}
		}
		checkBerserk();
	}

	public void setHpMpApUsed(int mpApUsed){
		this.hpMpApUsed = mpApUsed;
	}

	public void setHpMp(int x){
		setHp(x);
		setMp(x);
		updateSingleStat(MapleStat.HP, hp);
		updateSingleStat(MapleStat.MP, mp);
	}

	public void setInt(int int_){
		this.int_ = int_;
		recalcLocalStats();
	}

	public void setInventory(MapleInventoryType type, MapleInventory inv){
		inventory[type.ordinal()] = inv;
	}

	public void setItemEffect(int itemEffect){
		this.itemEffect = itemEffect;
	}

	public void setJob(MapleJob job){
		this.job = job;
	}

	public void setLastHealed(long time){
		this.lastHealed = time;
	}

	public void setLastUsedCashItem(long time){
		this.lastUsedCashItem = time;
	}

	public void setLastUsedSay(long time){
		this.lastUsedSay = time;
	}

	public void setLevel(int level){
		this.level = level;
	}

	public void setLuk(int luk){
		this.luk = luk;
		recalcLocalStats();
	}

	public void setMap(int PmapId){
		this.mapid = PmapId;
	}

	public void setMap(MapleMap newmap){
		this.map = newmap;
	}

	public void setMarkedMonster(int markedMonster){
		this.markedMonster = markedMonster;
	}

	public void setMaxHp(int hp){
		this.maxhp = Math.min(30000, hp);
		recalcLocalStats();
	}

	public void setMaxHp(int hp, boolean ap){
		if(ap){
			setHpMpApUsed(getHpMpApUsed() + 1);
		}
		this.maxhp = Math.min(30000, hp);
		recalcLocalStats();
	}

	public void setMaxMp(int mp){
		this.maxmp = Math.min(30000, mp);
		recalcLocalStats();
	}

	public void setMaxMp(int mp, boolean ap){
		if(ap){
			setHpMpApUsed(getHpMpApUsed() + 1);
		}
		this.maxmp = Math.min(30000, mp);
		recalcLocalStats();
	}

	public void setMessenger(MapleMessenger messenger){
		this.messenger = messenger;
	}

	public void setMessengerPosition(int position){
		this.messengerposition = position;
	}

	public void setMiniGame(MapleMiniGame miniGame){
		this.miniGame = miniGame;
	}

	public void setMiniGamePoints(MapleCharacter visitor, int winnerslot, boolean omok){
		if(omok){
			if(winnerslot == 1){
				this.omokwins++;
				visitor.omoklosses++;
			}else if(winnerslot == 2){
				visitor.omokwins++;
				this.omoklosses++;
			}else{
				this.omokties++;
				visitor.omokties++;
			}
		}else{
			if(winnerslot == 1){
				this.matchcardwins++;
				visitor.matchcardlosses++;
			}else if(winnerslot == 2){
				visitor.matchcardwins++;
				this.matchcardlosses++;
			}else{
				this.matchcardties++;
				visitor.matchcardties++;
			}
		}
	}

	public void setMonsterBookCover(int bookCover){
		this.bookCover = bookCover;
	}

	public void setMp(int newmp){
		int tmp = newmp;
		if(tmp < 0){
			tmp = 0;
		}
		if(tmp > localmaxmp){
			tmp = localmaxmp;
		}
		this.mp = tmp;
	}

	public void setName(String name){
		this.name = name;
	}

	public void changeName(String name){
		this.name = name;
		try{
			if(mgc != null){
				mgc.setName(name);
				ChannelServer.getInstance().getWorldInterface().changeName(this.mgc);// updates guild only atm
			}
		}catch(Exception e1){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, e1);
		}
		try{
			PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE `characters` SET `name` = ? WHERE `id` = ?");
			ps.setString(1, name);
			ps.setInt(2, id);
			ps.executeUpdate();
			ps.close();
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
	}

	public void setParty(MapleParty party){
		if(party == null){
			this.mpc = null;
			this.partyid = -1;
		}else{
			this.mpc = new MaplePartyCharacter(this);
			this.partyid = party.getId();
		}
	}

	public void setPlayerShop(MaplePlayerShop playerShop){
		this.playerShop = playerShop;
	}

	public void setRemainingAp(int remainingAp){
		this.remainingAp = remainingAp;
	}

	public void setRemainingSp(int remainingSp){
		this.remainingSp[getJobIndexBasedOnLevel()] = remainingSp; // default
	}

	public void setRemainingSp(int remainingSp, int skillbook){
		this.remainingSp[skillbook] = remainingSp;
	}

	public void addRemainingSp(int skillbook, int remainingSp){
		this.remainingSp[skillbook] += remainingSp;
	}

	public void setSearch(String find){
		search = find;
	}

	public void setSkinColor(MapleSkinColor skinColor){
		this.skinColor = skinColor;
	}

	public byte getSlots(int type){
		return type == MapleInventoryType.CASH.getType() ? 96 : inventory[type].getSlotLimit();
	}

	public boolean gainSlots(int type, int slots){
		return gainSlots(type, slots, true);
	}

	public boolean gainSlots(int type, int slots, boolean update){
		if(inventory[type].getSlotLimit() < 96){
			slots += inventory[type].getSlotLimit();
			if(slots > 96) slots = 96;
			inventory[type].setSlotLimit(slots);
			saveToDB();
			if(update){
				client.announce(MaplePacketCreator.updateInventorySlotLimit(type, slots));
			}
			return true;
		}
		return false;
	}

	public void setShop(MapleShop shop){
		this.shop = shop;
	}

	public void setSlot(int slotid){
		slots = slotid;
	}

	public void setStr(int str){
		this.str = str;
		recalcLocalStats();
	}

	public void setTrade(MapleTrade trade){
		this.trade = trade;
	}

	public void setVanquisherKills(int x){
		this.vanquisherKills = x;
	}

	public void setVanquisherStage(int x){
		this.vanquisherStage = x;
	}

	public void setWorld(int world){
		this.world = world;
	}

	public void shiftPetsRight(){
		if(pets[2] == null){
			pets[2] = pets[1];
			pets[1] = pets[0];
			pets[0] = null;
		}
	}

	public void showDojoClock(){
		int stage = (map.getId() / 100) % 100;
		long time;
		if(stage % 6 == 1){
			// time = (stage > 36 ? 15 : stage / 6 + 5) * 60;
			time = 1 * 60 * 60;
		}else{
			time = (dojoFinish - System.currentTimeMillis()) / 1000;
		}
		if(stage % 6 > 0){
			client.announce(MaplePacketCreator.getClock((int) time));
		}
		boolean rightmap = true;
		int clockid = (dojoMap.getId() / 100) % 100;
		if(map.getId() > clockid / 6 * 6 + 6 || map.getId() < clockid / 6 * 6){
			rightmap = false;
		}
		final boolean rightMap = rightmap; // lol
		TimerManager.getInstance().schedule("showDojoClock", new Runnable(){

			@Override
			public void run(){
				if(rightMap){
					client.getPlayer().changeMap(client.getChannelServer().getMap(925020000));
				}
			}
		}, time * 1000 + 3000); // let the TIMES UP display for 3 seconds, then warp
	}

	public void showNote(){
		try{
			try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM notes WHERE `to`=? AND `deleted` = 0", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)){
				ps.setString(1, this.getName());
				try(ResultSet rs = ps.executeQuery()){
					rs.last();
					int count = rs.getRow();
					rs.first();
					client.announce(MaplePacketCreator.showNotes(rs, count));
				}
			}
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
	}

	private void silentEnforceMaxHpMp(){
		setMp(getMp());
		setHp(getHp(), true);
	}

	public void silentGiveBuffs(List<PlayerBuffValueHolder> buffs){
		for(PlayerBuffValueHolder mbsvh : buffs){
			if(mbsvh.sourceid != Priest.MYSTIC_DOOR) mbsvh.getEffect().silentApplyBuff(this, mbsvh.startTime, mbsvh.duration);
		}
	}

	public void silentPartyUpdate() throws RemoteException, NullPointerException{
		if(isInParty()){
			ChannelServer.getInstance().getWorldInterface().updateParty(getPartyId(), PartyOperation.SILENT_UPDATE, getMPC());
		}
	}

	public static class SkillEntry{

		public int masterlevel;
		public byte skillevel;
		public long expiration;

		public SkillEntry(byte skillevel, int masterlevel, long expiration){
			this.skillevel = skillevel;
			this.masterlevel = masterlevel;
			this.expiration = expiration;
		}

		@Override
		public String toString(){
			return skillevel + ":" + masterlevel;
		}
	}

	public boolean skillisCooling(int skillId){
		return coolDowns.containsKey(Integer.valueOf(skillId));
	}

	public void startFullnessSchedule(final int decrease, final MaplePet pet, final int petSlot){
		if(petSlot == -1){
			Logger.log(LogType.ERROR, LogFile.GENERAL_ERROR, ExceptionUtil.buildException());
			return;
		}
		if(fullnessSchedule[petSlot] != null){// incase
			fullnessSchedule[petSlot].cancel(true);
			fullnessSchedule[petSlot] = null;
		}
		ScheduledFuture<?> schedule = TimerManager.getInstance().register("startFullnessSchedule", new Runnable(){

			@Override
			public void run(){
				if(getCashShop().isOpened() || getClient().getPlayer() == null || getClient().getPlayer().getMap() == null) return;
				int newFullness = pet.getFullness() - decrease;
				if(newFullness <= 5){
					pet.setFullness(15);
					pet.saveToDb();
					unequipPet(pet, true);
				}else{
					boolean fed = false;
					if(newFullness <= 50){
						if(getClient().checkEliteStatus()){
							if(haveItem(2120000)){
								MapleInventoryManipulator.removeById(getClient(), MapleInventoryType.USE, 2120000, 1, true, false);
								pet.feed(getClient().getPlayer());
								fed = true;
								getMap().broadcastMessage(getClient().getPlayer(), MaplePacketCreator.petChat(getId(), (byte) petSlot, 0, 0, "Pet Food x" + getItemQuantity(2120000, false)), true);
							}
						}
					}
					if(!fed) pet.setFullness(newFullness);
					pet.saveToDb();
					Item petz = getInventory(MapleInventoryType.CASH).getItem(pet.getPosition());
					if(petz != null){
						forceUpdateItem(petz);
					}
				}
			}
		}, 180000, 180000);
		fullnessSchedule[petSlot] = schedule;
	}

	public void startMapEffect(String msg, int itemId){
		startMapEffect(msg, itemId, 30000);
	}

	public void startMapEffect(String msg, int itemId, int duration){
		final MapleMapEffect mapEffect = new MapleMapEffect(msg, itemId);
		getClient().announce(mapEffect.makeStartData());
		TimerManager.getInstance().schedule("startMapEffect", new Runnable(){

			@Override
			public void run(){
				getClient().announce(mapEffect.makeDestroyData());
			}
		}, duration);
	}

	public void stopControllingMonster(MapleMonster monster){
		controlled.remove(monster);
	}

	public void unequipAllPets(){
		for(int i = 0; i < 3; i++){
			if(pets[i] != null){
				unequipPet(pets[i], true);
			}
		}
	}

	public void unequipPet(MaplePet pet, boolean shift_left){
		unequipPet(pet, shift_left, false);
	}

	public void unequipPet(MaplePet pet, boolean shift_left, boolean hunger){
		int index = getPetIndex(pet);
		if(index == -1) return;
		if(this.getPet(index) != null){
			pet.setSummoned(false);
			pet.saveToDb();
		}
		cancelFullnessSchedule(index);
		getMap().broadcastMessage(this, MaplePacketCreator.showPet(this, pet, true, hunger), true);
		client.announce(MaplePacketCreator.petStatUpdate(this));
		client.announce(CWvsContext.enableActions());
		removePet(pet, shift_left);
	}

	public void updateMacros(int position, SkillMacro updateMacro){
		skillMacros[position] = updateMacro;
	}

	public SkillMacro[] getMacros(){
		return skillMacros;
	}

	public String getQuestInfo(int quest){
		MapleQuestStatus qs = getQuest(MapleQuest.getInstance(quest));
		return qs.getInfo();
	}

	public void updateQuestInfo(int quest, String info){
		Logger.log(LogType.INFO, LogFile.QUESTS, getClient().getAccountName() + ".txt", getName() + " Updated quest info for quest " + quest + " with info: " + info);
		MapleQuest q = MapleQuest.getInstance(quest);
		MapleQuestStatus qs = getQuest(q);
		qs.setInfo(info);
		quests.put(q.getId(), qs);
		announce(CWvsContext.OnMessage.updateQuest(qs, 0));
		if(qs.getQuest().startQuestData.infoNumber > 0){
			announce(CWvsContext.OnMessage.updateQuest(qs, qs.getQuest().startQuestData.infoNumber));
		}
		if(qs.getStatus() == Status.STARTED){
			if(qs.getQuest().completeQuestData.infoNumber > 0){
				announce(CWvsContext.OnMessage.updateQuest(qs, qs.getQuest().completeQuestData.infoNumber));
			}
		}
		// announce(MaplePacketCreator.updateQuestInfo((short) qs.getQuest().getId(), qs.getNpc(), qs.getInfo()));
	}

	public void updateQuest(MapleQuestStatus quest){
		updateQuest(quest, false);
	}

	public void updateQuest(MapleQuestStatus quest, boolean infoOnly){
		updateQuest(quest, infoOnly, true);
	}

	public void updateQuest(MapleQuestStatus quest, boolean infoOnly, boolean info){
		quests.put(quest.getQuestID(), quest);
		if(quest.getStatus().equals(MapleQuestStatus.Status.STARTED)){
			announce(CWvsContext.OnMessage.updateQuest(quest, 0));
			if(info){
				if(quest.getQuest().startQuestData.infoNumber > 0){
					if(quest.getQuestID() == quest.getQuest().startQuestData.infoNumber){
						updateQuest(this.getQuest(MapleQuest.getInstance(quest.getQuest().startQuestData.infoNumber)), true, false);
					}else updateQuest(this.getQuest(MapleQuest.getInstance(quest.getQuest().startQuestData.infoNumber)), true, true);
				}
			}
			if(!infoOnly){
				announce(MaplePacketCreator.updateQuestInfo((short) quest.getQuest().getId(), quest.getNpc(), quest.getInfo()));
			}
		}else if(quest.getStatus().equals(MapleQuestStatus.Status.COMPLETED)){
			announce(CWvsContext.OnMessage.completeQuest((short) quest.getQuest().getId(), quest.getCompletionTime()));
			announce(UserLocal.UserEffect.questComplete());
			getMap().broadcastMessage(UserRemote.UserEffect.questComplete(getId()));
			if(info){
				if(quest.getQuest().completeQuestData.infoNumber > 0){
					if(quest.getQuestID() == quest.getQuest().completeQuestData.infoNumber){
						updateQuest(this.getQuest(MapleQuest.getInstance(quest.getQuest().completeQuestData.infoNumber)), true, false);
					}else updateQuest(this.getQuest(MapleQuest.getInstance(quest.getQuest().completeQuestData.infoNumber)), true, true);
				}
			}
		}else if(quest.getStatus().equals(MapleQuestStatus.Status.NOT_STARTED)){
			announce(CWvsContext.OnMessage.updateQuest(quest, 0));
			if(info){
				if(quest.getQuest().startQuestData.infoNumber > 0){
					if(quest.getQuestID() == quest.getQuest().startQuestData.infoNumber){
						updateQuest(this.getQuest(MapleQuest.getInstance(quest.getQuest().startQuestData.infoNumber)), true, false);
					}else updateQuest(this.getQuest(MapleQuest.getInstance(quest.getQuest().startQuestData.infoNumber)), true, true);
				}
			}
		}
	}

	// quest info
	public void updateQuestProgress(int questid, String data){
		updateQuestProgress(questid, 0, data);
	}

	// quest progress/ info
	public void updateQuestProgress(int questid, int progressid, String data){
		MapleQuestStatus status = getQuest(MapleQuest.getInstance(questid));
		status.setStatus(MapleQuestStatus.Status.STARTED);
		status.setProgress(progressid, data);// override old if exists
		updateQuest(status);
	}

	// since the data is int most of the time :D
	// quest info
	public void updateQuestProgress(int questid, int data){
		updateQuestProgress(questid, 0, data);
	}

	// quest progress/ info
	public void updateQuestProgress(int questid, int progressid, int data){
		MapleQuestStatus status = getQuest(MapleQuest.getInstance(questid));
		status.setStatus(MapleQuestStatus.Status.STARTED);
		status.setProgress(progressid, data + "");// override old if exists
		updateQuest(status);
	}

	public int getQuestProgress(int qid){
		return getQuestProgress(qid, 0);
	}

	public int getQuestProgress(int qid, int progressid){
		try{
			return Integer.parseInt(getQuest(MapleQuest.getInstance(qid)).getProgress(progressid));
		}catch(NumberFormatException ex){
			// Just return a 0 since it isn't there.
			return 0;
		}
	}

	public void questTimeLimit(final MapleQuest quest, int time){
		if(timers.containsKey("" + quest.getId())){
			timers.get("" + quest.getId()).cancel(true);
			timers.remove("" + quest.getId());
		}
		timers.put("" + quest.getId(), TimerManager.getInstance().schedule("questTimeLimit", new Runnable(){

			@Override
			public void run(){
				announce(MaplePacketCreator.questExpire(quest.getId()));
				MapleQuestStatus newStatus = new MapleQuestStatus(quest, MapleQuestStatus.Status.NOT_STARTED);
				newStatus.setForfeited(getQuest(quest).getForfeited() + 1);
				updateQuest(newStatus);
			}
		}, time * 60 * 1000));
		announce(MaplePacketCreator.addQuestTimeLimit(quest.getId(), time * 60 * 1000));
	}

	public final MapleQuestStatus getQuestNAdd(final short quest){
		if(!quests.containsKey(quest)){
			final MapleQuestStatus status = new MapleQuestStatus(MapleQuest.getInstance(quest), MapleQuestStatus.Status.NOT_STARTED);
			quests.put((short) quest, status);
			return status;
		}
		return quests.get(quest);
	}

	public void updateSingleStat(MapleStat stat, int newval){
		updateSingleStat(stat, newval, false);
	}

	private void updateSingleStat(MapleStat stat, int newval, boolean itemReaction){
		announce(CWvsContext.updatePlayerStats(Collections.singletonList(new Pair<>(stat, Integer.valueOf(newval))), itemReaction, this));
	}

	public void announce(final byte[] packet){
		client.announce(packet);
	}

	@Override
	public int getObjectId(){
		return getId();
	}

	@Override
	public MapleMapObjectType getType(){
		return MapleMapObjectType.PLAYER;
	}

	@Override
	public void sendDestroyData(MapleClient client){
		client.announce(CUserPool.removePlayerFromMap(this.getObjectId()));
	}

	@Override
	public void sendSpawnData(MapleClient client){
		if(!this.isHidden() || client.getPlayer().isGM()){
			client.announce(CUserPool.spawnPlayerMapobject(this));
			if(driver != -1) client.announce(UserCommon.followCharacter(id, driver));
			if(passenger != -1) client.announce(UserCommon.followCharacter(passenger, id));
			if(getMount() != null && getMount().isActive()) client.announce(MaplePacketCreator.showMonsterRiding(getId(), getMount()));
		}
		if(this.isHidden()){
			List<Pair<MapleBuffStat, BuffDataHolder>> dsstat = Collections.singletonList(new Pair<>(MapleBuffStat.DARKSIGHT, new BuffDataHolder(0, 0, 0)));
			getMap().broadcastGMMessage(this, MaplePacketCreator.giveForeignBuff(this, dsstat), false);
		}
	}

	@Override
	public void setObjectId(int id){}

	@Override
	public String toString(){
		return name;
	}

	public int getExplorerLinkedLevel(){
		return explorerLinkedLevel;
	}

	public String getExplorerLinkedName(){
		return explorerLinkedName;
	}

	public int getCygnusLinkedLevel(){
		return cygnusLinkedLevel;
	}

	public String getCygnusLinkedName(){
		return cygnusLinkedName;
	}

	public CashShop getCashShop(){
		return cashshop;
	}

	public void portalDelay(long delay){
		this.portaldelay = System.currentTimeMillis() + delay;
	}

	public long portalDelay(){
		return portaldelay;
	}

	public void blockPortal(String scriptName){
		if(!blockedPortals.contains(scriptName) && scriptName != null){
			blockedPortals.add(scriptName);
			client.announce(CWvsContext.enableActions());
		}
	}

	public void unblockPortal(String scriptName){
		if(blockedPortals.contains(scriptName) && scriptName != null){
			blockedPortals.remove(scriptName);
		}
	}

	public List<String> getBlockedPortals(){
		return blockedPortals;
	}

	public boolean containsAreaInfo(int area, String info){
		Short area_ = Short.valueOf((short) area);
		if(area_info.containsKey(area_)) return area_info.get(area_).contains(info);
		return false;
	}

	public void updateAreaInfo(int area, String info){
		area_info.put(Short.valueOf((short) area), info);
		announce(CWvsContext.OnMessage.updateAreaInfo(area, info));
	}

	public String getAreaInfo(int area){
		return area_info.get(Short.valueOf((short) area));
	}

	public Map<Short, String> getAreaInfos(){
		return area_info;
	}

	public void autoban(String reason, AutobanFactory fac){
		if(!isGM()) this.ban(reason);
		announce(MaplePacketCreator.sendPolice(String.format("You have been blocked from#b %s #k for #b" + fac.name() + ".#k", "Vertisy")));
		if(!isGM()) client.disconnect(false, getCashShop().isOpened());
		/*TimerManager.getInstance().schedule("autoban", new Runnable(){
		
			@Override
			public void run(){
				client.disconnect(false, false);
			}
		}, 8000);*/
		try{
			ChannelServer.getInstance().getWorldInterface().broadcastGMPacket(MaplePacketCreator.serverNotice(6, MapleCharacter.makeMapleReadable(this.name) + " was autobanned for " + reason));
		}catch(RemoteException | NullPointerException ex){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
		}
		Logger.log(LogType.INFO, LogFile.GENERAL_INFO, MapleCharacter.makeMapleReadable(this.name) + " was autobanned for " + reason);
	}

	public void block(int reason, int days, String desc){
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, days);
		Timestamp TS = new Timestamp(cal.getTimeInMillis());
		try{
			Connection con = DatabaseConnection.getConnection();
			try(PreparedStatement ps = con.prepareStatement("UPDATE accounts SET banreason = ?, tempban = ?, greason = ? WHERE id = ?")){
				ps.setString(1, desc);
				ps.setTimestamp(2, TS);
				ps.setInt(3, reason);
				ps.setInt(4, accountid);
				ps.executeUpdate();
			}
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
	}

	public static void block(String id, int reason, int days, String desc){
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, days);
		Timestamp TS = new Timestamp(cal.getTimeInMillis());
		int accountid = -1;
		Connection con = DatabaseConnection.getConnection();
		try{
			try(PreparedStatement ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?")){
				ps.setString(1, id);
				try(ResultSet rs = ps.executeQuery()){
					if(rs.next()) accountid = rs.getInt(1);
				}
			}
			if(accountid != -1){
				try(PreparedStatement ps = con.prepareStatement("UPDATE accounts SET banreason = ?, tempban = ?, greason = ? WHERE id = ?")){
					ps.setString(1, desc);
					ps.setTimestamp(2, TS);
					ps.setInt(3, reason);
					ps.setInt(4, accountid);
					ps.executeUpdate();
				}
			}
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
	}

	public boolean isBanned(){
		return isbanned;
	}

	final long hour = 60 * 60 * 1000;

	public boolean isChatBanned(){
		if(getChatBanDate() != null && getChatBanDate().getTime() > 0 && getChatBanDuration() > 0){
			if(getChatBanDate().getTime() + (getChatBanDuration() * hour) > Calendar.getInstance().getTimeInMillis()){
				chatBan = false;
				return chatBan;
			}
		}
		return chatBan;
	}

	public Timestamp getChatBanDate(){
		return chatBanDate;
	}

	public int getChatBanDuration(){
		return chatBanDuration;
	}

	public List<Integer> getTrockMaps(){
		return trockmaps;
	}

	public List<Integer> getVipTrockMaps(){
		return viptrockmaps;
	}

	public int getTrockSize(){
		int ret = trockmaps.indexOf(999999999);
		if(ret == -1){
			ret = 5;
		}
		return ret;
	}

	public void deleteFromTrocks(int map){
		trockmaps.remove(Integer.valueOf(map));
		while(trockmaps.size() < 10){
			trockmaps.add(999999999);
		}
	}

	public void addTrockMap(){
		int index = trockmaps.indexOf(999999999);
		if(index != -1){
			trockmaps.set(index, getMapId());
		}
	}

	public boolean isTrockMap(int id){
		int index = trockmaps.indexOf(id);
		if(index != -1) return true;
		return false;
	}

	public int getVipTrockSize(){
		int ret = viptrockmaps.indexOf(999999999);
		if(ret == -1){
			ret = 10;
		}
		return ret;
	}

	public void deleteFromVipTrocks(int map){
		viptrockmaps.remove(Integer.valueOf(map));
		while(viptrockmaps.size() < 10){
			viptrockmaps.add(999999999);
		}
	}

	public void addVipTrockMap(){
		int index = viptrockmaps.indexOf(999999999);
		if(index != -1){
			viptrockmaps.set(index, getMapId());
		}
	}

	public boolean isVipTrockMap(int id){
		int index = viptrockmaps.indexOf(id);
		if(index != -1) return true;
		return false;
	}

	// EVENTS
	private byte team = 0;
	private MapleFitness fitness;
	private MapleOla ola;
	private long snowballattack;
	private MapleFindThatJewel jewel;

	public byte getTeam(){
		return team;
	}

	public void setTeam(int team){
		this.team = (byte) team;
	}

	public MapleOla getOla(){
		return ola;
	}

	public void setOla(MapleOla ola){
		this.ola = ola;
	}

	public MapleFitness getFitness(){
		return fitness;
	}

	public void setFitness(MapleFitness fit){
		this.fitness = fit;
	}

	public long getLastSnowballAttack(){
		return snowballattack;
	}

	public void setLastSnowballAttack(long time){
		this.snowballattack = time;
	}

	public MapleFindThatJewel getJewel(){
		return jewel;
	}

	public void setJewel(MapleFindThatJewel jewel){
		this.jewel = jewel;
	}

	public AutobanManager getAutobanManager(){
		if(autoban != null){
			return autoban;
		}else{
			autoban = new AutobanManager(this);
			return autoban;
		}
	}

	public void equipPendantOfSpirit(){
		if(pendantOfSpirit == null){
			pendantOfSpirit = TimerManager.getInstance().register("pendant_of_spirit", new Runnable(){

				@Override
				public void run(){
					if(pendantExp < 3){
						pendantExp++;
						if(pendantExp > 0) message("Pendant of the Spirit has been equipped for " + pendantExp + " hour(s), you will now receive " + pendantExp + "0% bonus exp.");
					}else{
						pendantOfSpirit.cancel(false);
					}
				}
			}, 3600000, 3600000); // 1 hour
		}
	}

	public void unequipPendantOfSpirit(){
		if(pendantOfSpirit != null){
			pendantOfSpirit.cancel(false);
			pendantOfSpirit = null;
		}
		pendantExp = 0;
	}

	public void increaseEquipExp(float mobexp){
		ItemInformationProvider mii = ItemInformationProvider.getInstance();
		for(Item item : getInventory(MapleInventoryType.EQUIPPED).list()){
			Equip nEquip = (Equip) item;
			String itemName = mii.getItemData(nEquip.getItemId()).name;
			if(itemName == null){
				continue;
			}
			if((itemName.contains("Reverse") && nEquip.getItemLevel() < 4) || itemName.contains("Timeless") && nEquip.getItemLevel() < 6){
				nEquip.gainItemExp(client, mobexp, itemName.contains("Timeless"));
			}
		}
	}

	public Map<String, MapleEvents> getEvents(){
		return events;
	}

	public PartyQuest getPartyQuest(){
		return partyQuest;
	}

	public void setPartyQuest(PartyQuest pq){
		this.partyQuest = pq;
	}

	public void changeChannelCancel(){
		if(collisionCheck != null){
			collisionCheck.cancel(false);
		}
		cancelAllFullnessSchedules();
		cancelExpirationTask();
	}

	public final void empty(final boolean remove){
		if(dragonBloodSchedule != null){
			dragonBloodSchedule.cancel(false);
		}
		if(hpDecreaseTask != null){
			hpDecreaseTask.cancel(false);
		}
		if(recoveryTask != null){
			recoveryTask.cancel(false);
		}
		if(crcSchedule != null){
			crcSchedule.cancel(false);
		}
		for(ScheduledFuture<?> sf : timers.values()){
			sf.cancel(false);
		}
		timers.clear();
		changeChannelCancel();
		if(maplemount != null){
			maplemount.empty();
			maplemount = null;
		}
		if(remove){
			partyQuest = null;
			events = null;
			mpc = null;
			mgc = null;
			events = null;
			client = null;
			map = null;
			timers = null;
		}
	}

	public void logOff(){
		this.loggedIn = false;
	}

	public boolean isLoggedin(){
		return loggedIn;
	}

	public void setMapId(int mapid){
		this.mapid = mapid;
	}

	public boolean bigBrother(){
		return !isGM() ? false : bigBrother;
	}

	public void toggleBigBrother(){
		bigBrother = !bigBrother;
	}

	public boolean isBigBrother(){
		return bigBrother;
	}

	public boolean getWhiteChat(){
		return !isGM() ? false : whiteChat;
	}

	public void toggleWhiteChat(){
		whiteChat = !whiteChat;
	}

	public boolean canDropMeso(){
		if(System.currentTimeMillis() - lastMesoDrop >= 150 || lastMesoDrop == -1){ // About 200 meso drops a minute
			lastMesoDrop = System.currentTimeMillis();
			return true;
		}
		return false;
	}

	// These need to be renamed, but I am too lazy right now to go through the scripts and rename them...
	public String getPartyQuestItems(){
		return dataString;
	}

	public boolean gotPartyQuestItem(String partyquestchar){
		return dataString.contains(partyquestchar);
	}

	public void removePartyQuestItem(String letter){
		if(gotPartyQuestItem(letter)){
			dataString = dataString.substring(0, dataString.indexOf(letter)) + dataString.substring(dataString.indexOf(letter) + letter.length());
		}
	}

	public void setPartyQuestItemObtained(String partyquestchar){
		if(!dataString.contains(partyquestchar)){
			this.dataString += partyquestchar;
		}
	}

	public void createDragon(){
		dragon = new MapleDragon(this);
	}

	public MapleDragon getDragon(){
		return dragon;
	}

	public int getRemainingSpSize(){
		int sp = 0;
		for(int i = 0; i < remainingSp.length; i++){
			if(remainingSp[i] > 0){
				sp++;
			}
		}
		return sp;
	}

	public boolean getTag(){
		return tag;
	}

	public void setTag(boolean tag){
		this.tag = tag;
	}

	public boolean isStunned(){
		return isStunned;
	}

	public void setStunned(boolean stun){
		this.isStunned = stun;
	}

	public boolean isSeduced(){
		return isSeduced;
	}

	public void setSeduced(boolean seduce){
		this.isSeduced = seduce;
	}

	public boolean getAutoSell(){
		return autoSell;
	}

	public void setAutoSell(boolean b){
		autoSell = b;
	}

	public void toggleAutoSell(){
		autoSell = !autoSell;
	}

	public boolean isItemAutoSellable(int itemid){
		ItemInformationProvider ii = ItemInformationProvider.getInstance();
		String itemName = ii.getItemData(itemid).name;
		if(itemName != null){
			if(!autoSellInventoryTypeIgnore.contains(ii.getInventoryType(itemid))){
				return !autoSellIgnore.stream().anyMatch(s-> itemName.toLowerCase().contains(s));
			}else return false;
		}
		return true;
	}

	public String addAutoSellIgnore(int itemid){
		ItemInformationProvider ii = ItemInformationProvider.getInstance();
		String itemName = ii.getItemData(itemid).name.toLowerCase();
		addAutoSellIgnore(itemName);
		return itemName;
	}

	public void addAutoSellIgnore(String itemName){
		String item = itemName.toLowerCase();
		if(!autoSellIgnore.stream().anyMatch(s-> s.contains(item))) autoSellIgnore.add(item);
	}

	public boolean removeAutoSellIgnore(int itemid){
		ItemInformationProvider ii = ItemInformationProvider.getInstance();
		String itemName = ii.getItemData(itemid).name.toLowerCase();
		return removeAutoSellIgnore(itemName);
	}

	public boolean removeAutoSellIgnore(String itemName){
		itemName = itemName.toLowerCase();
		int pos = -1;
		for(int i = 0; i < autoSellIgnore.size(); i++){
			if(autoSellIgnore.get(i).equalsIgnoreCase(itemName)){
				pos = i;
				break;
			}
		}
		if(pos != -1){
			autoSellIgnore.remove(pos);
			return true;
		}
		return false;
	}

	public List<String> getAutoSellIgnore(){
		return autoSellIgnore;
	}

	public void addAutoSellInventoryIgnore(MapleInventoryType type){
		if(!autoSellInventoryTypeIgnore.contains(type)) autoSellInventoryTypeIgnore.add(type);
	}

	public void removeAutoSellInventoryIgnore(MapleInventoryType type){
		autoSellInventoryTypeIgnore.remove(type);
	}

	public List<MapleInventoryType> getAutoSellInventoryIgnore(){
		return autoSellInventoryTypeIgnore;
	}

	public PlayerStats getStats(){
		return stats;
	}

	public boolean isIronMan(){
		return ironMan > 0;
	}

	public int getIronMan(){
		return ironMan;
	}

	public void setIronMan(int type){
		ironMan = type;
	}

	public boolean isHardMode(){
		return hardmode > 0;
	}

	public int getHardMode(){
		return hardmode;
	}

	public void setHardMode(int type){
		hardmode = type;
	}

	public void gainRSSkillExp(RSSkill skill, long amount){
		long exp = rsSkillExp.get(skill) + amount;
		byte level = rsSkillLevel.get(skill);
		long exp2 = exp;
		while(level < skill.getMaxLevel() && exp2 >= ExpTable.getRSSkillExpNeededForLevel(level + 1)){
			exp2 -= ExpTable.getRSSkillExpNeededForLevel(level + 1);
			gainRSSkillLevel(skill, (byte) 1);
		}
		exp = Math.min(999999999, exp);
		rsSkillExp.put(skill, exp);
		Integer track = rsSkillTrack.get(skill);
		if(track != null){
			if(++track >= skill.getTrack()){
				track = 0;
				dropMessage(MessageType.MAPLETIP, "Current " + skill.name() + " progress: " + exp + "/" + ExpTable.getRSSkillExpNeededForLevel(level + 1));
			}
			rsSkillTrack.put(skill, track);
		}
	}

	public long getRSSkillExp(RSSkill skill){
		return rsSkillExp.get(skill);
	}

	public void gainRSSkillLevel(RSSkill skill, byte amount){
		byte level = rsSkillLevel.get(skill);
		for(byte diff = 1; diff <= amount; diff++){// If for some reason they get more than 1 level.
			byte newLevel = (byte) (level + diff);
			if(newLevel < skill.getMaxLevel()){
				switch (skill){// To do stuff based on levelup
					case Capacity:
						int slots = 0;
						switch (newLevel){
							case 5:
							case 10:
							case 20:
							case 30:
							case 40:
							case 50:
							case 60:
								slots = 4;
								break;
							case 70:
							case 80:
								slots = 8;
								break;
							case 90:
								slots = 12;
								break;
							case 99:
								slots = 16;
								break;
						}
						if(slots > 0){
							for(MapleInventoryType mit : MapleInventoryType.values()){
								if(!mit.equals(MapleInventoryType.CASH) && !mit.equals(MapleInventoryType.UNDEFINED) && !mit.equals(MapleInventoryType.EQUIPPED)){
									gainSlots(mit.ordinal(), slots);
								}
							}
						}
						break;
					case Fishing:
						int itemid = 0;
						switch (newLevel){
							case 10:
								itemid = 3010002;
								break;
							case 20:
								itemid = 3010003;
								break;
							case 30:
								itemid = 3010006;
								break;
							case 40:
								itemid = 3010007;
								break;
							case 50:
								itemid = 3010008;
								break;
							case 60:
								itemid = 3010010;
								break;
							case 70:
								itemid = 3010016;
								break;
							case 80:
								itemid = 3010017;
								break;
							case 90:
								itemid = 3010013;
								break;
							case 99:
								itemid = 3010018;
								break;
						}
						if(itemid != 0){
							MapleInventoryManipulator.addFromDrop(client, new Item(itemid, (short) 1), true);
						}
						break;
					case Combat:
						List<Pair<MapleStat, Integer>> statup = new ArrayList<>(5);
						int ap = 0;
						if(getStr() < 999){
							setStr(getStr() + 1);
							statup.add(new Pair<>(MapleStat.STR, getStr()));
						}else{
							ap++;
						}
						if(getDex() < 999){
							setDex(getDex() + 1);
							statup.add(new Pair<>(MapleStat.DEX, getDex()));
						}else{
							ap++;
						}
						if(getInt() < 999){
							setInt(getInt() + 1);
							statup.add(new Pair<>(MapleStat.INT, getInt()));
						}else{
							ap++;
						}
						if(getLuk() < 999){
							setLuk(getLuk() + 1);
							statup.add(new Pair<>(MapleStat.LUK, getLuk()));
						}else{
							ap++;
						}
						if(ap > 0){
							remainingAp += ap;
							statup.add(new Pair<>(MapleStat.AVAILABLEAP, getRemainingAp()));
						}
						if(statup.size() > 0) client.announce(CWvsContext.updatePlayerStats(statup, this));
						break;
					case Health:
						setMaxHp(getMaxHp() + 40);
						updateSingleStat(MapleStat.MAXHP, getMaxHp());
						break;
					case Mana:
						setMaxMp(getMaxMp() + 40);
						updateSingleStat(MapleStat.MAXMP, getMaxMp());
						break;
					default:// To make eclipse stfu
						break;
				}
			}
		}
		if(level + amount <= skill.getMaxLevel()) level += amount;
		else level = skill.getMaxLevel();
		rsSkillLevel.put(skill, level);
		// Change this color the SECOND we get more options
		dropMessage(MessageType.SYSTEM, "Congrats your " + skill.name() + " level has been increased to " + level + ".");
		map.broadcastMessage(UserRemote.UserEffect.showForeignEffect(id, 15));
		announce(UserLocal.UserEffect.showItemLevelup());
	}

	public byte getRSSkillLevel(RSSkill skill){
		return rsSkillLevel.get(skill);
	}

	public void crcStateTask(){
		if(crcSchedule != null) crcSchedule.cancel(true);
		crcSchedule = TimerManager.getInstance().register("crcStateTask", new Runnable(){

			@Override
			public void run(){
				getClient().announce(MaplePacketCreator.crcStatus());
			}
		}, 420000, 420000);
	}

	public int getGP(){
		return gp;
	}

	public void gainGP(int amount){
		gp += amount;
		mgc.gainGP(amount);
		announce(CWvsContext.OnMessage.getGPMessage(amount));
	}

	public void removeGP(int amount){
		if(amount > gp){
			mgc.removeGP(gp);
			gp = 0;
		}else{
			gp -= amount;
			mgc.removeGP(amount);
		}
	}

	public boolean isTracking(RSSkill skill){
		return rsSkillTrack.containsKey(skill);
	}

	public void startTracking(RSSkill skill){
		rsSkillTrack.put(skill, 0);
	}

	public void stopTracking(RSSkill skill){
		rsSkillTrack.remove(skill);
	}

	public void incrementCombatProgress(){
		if(++combatProgress >= 5){
			combatProgress = 0;
			gainRSSkillExp(RSSkill.Combat, 1);
		}
	}

	public void addBigBrotherMonitor(int chrid){
		bigbrotherMonitoring.add(chrid);
	}

	public void removeBigBrotherMonitor(int chrid){
		bigbrotherMonitoring.remove(chrid);
	}

	public boolean isBigBrotherMonitoring(int chrid){
		return bigbrotherMonitoring.contains(chrid);
	}

	public Set<Integer> getBigBrotherMonitors(){
		return bigbrotherMonitoring;
	}

	public int getMarriedTo(){// Can be only engagement
		return marriedto;
	}

	public void setMarriedTo(int id){// Can be only engagement
		marriedto = id;
	}

	public int getEngagementRingID(){
		return engagementringid;
	}

	public void setEngagementRingID(int id){
		engagementringid = id;
	}

	public int getMarriageRingID(){
		return marriageringid;
	}

	public void setMarriageRingID(int id){
		marriageringid = id;
	}

	public boolean getMarried(){
		return marriageringid > 0;
	}

	public int getMarriageID(){
		return marriageid;
	}

	public void setMarriageID(int id){
		this.marriageid = id;
	}

	public MapleCharacter getPartner(){
		if(getMarriedTo() > 0) return ChannelServer.getInstance().getCharacterById(getMarriedTo());
		else return null;
	}

	public MapleWedding getWedding(){
		return Server.getInstance().getWeddingByID(marriageid);
	}

	public String getProgressValues(){
		return progressValues;
	}

	public void addProgressValue(String key, Object value){
		progressValues += key + "=" + value + ",";
	}

	public void setProgressValue(String key, Object value){
		String newValues = "";
		for(String info : progressValues.split(",")){
			if(info.split("=")[0].equalsIgnoreCase(key)){
				newValues += key + "=" + value + ",";
			}else{
				newValues += info + ",";
			}
		}
		if(!isProgressValueSet(key)){// No new value.. Add it instead of setting
			addProgressValue(key, value);
		}else{
			progressValues = newValues;
		}
	}

	public Object getProgressValue(String key){
		for(String info : progressValues.split(",")){
			if(info.length() > 0){
				if(info.split("=")[0].equalsIgnoreCase(key)) return info.split("=")[1];
			}
		}
		return "";
	}

	public boolean isProgressValueSet(String key){
		Object value = getProgressValue(key);
		return value != null && ((value instanceof String && ((String) value).length() > 0) || !(value instanceof String));
	}

	public int getReincarnations(){
		return reincarnations;
	}

	public void addReincarnation(){
		reincarnations++;
	}

	public boolean hasReincarnated(){
		return reincarnations > 0;
	}


	public void addMonsterKillCount(boolean higher){
		Calendar cal = Calendar.getInstance();
		String yearMonth = cal.get(Calendar.YEAR) + "-" + cal.get(Calendar.MONDAY);
		Long amount = this.monsterKillTotal.get(yearMonth);
		if(amount != null){
			amount++;
		}else{
			amount = 1L;
		}
		monsterKillTotal.put(yearMonth, amount);
		//
		if(higher){
			amount = this.monsterKillHigher.get(yearMonth);
			if(amount != null){
				amount++;
			}else{
				amount = 1L;
			}
			monsterKillHigher.put(yearMonth, amount);
		}
	}

	public SlayerTask getSlayerTask(){
		return slayerTask;
	}

	public void setSlayerTask(SlayerTask slayerTask){
		this.slayerTask = slayerTask;
	}

	public int getTasksCompleted(){
		return tasksCompleted;
	}

	public void incrementTasksCompleted(){
		tasksCompleted++;
	}

	public long getPlaytime(){
		long time = Calendar.getInstance().getTimeInMillis();
		playtime += time - playtimeStart;
		playtimeStart = time;
		return playtime;
	}

	public void removeClock(){// zz
		client.announce(MaplePacketCreator.removeClock());
	}

	public long getLastMapChange(){
		return lastMapChange;
	}

	public void setMarriageRing(MapleRing ring){
		this.marriageRing = ring;
	}

	public MapleRing getMarriageRing(){
		return this.marriageRing;
	}

	public void toggleScriptDebug(){
		scriptDebug = !scriptDebug;
	}

	public boolean getScriptDebug(){
		return scriptDebug;
	}

	public void removeAll(int id){
		MapleInventoryType invType = ItemInformationProvider.getInstance().getInventoryType(id);
		int possessed = getInventory(invType).countById(id);
		if(possessed > 0){
			MapleInventoryManipulator.removeById(getClient(), ItemInformationProvider.getInstance().getInventoryType(id), id, possessed, true, false);
			announce(UserLocal.UserEffect.getShowItemGain(id, (short) -possessed, true));
		}
		if(invType == MapleInventoryType.EQUIP){
			if(getInventory(MapleInventoryType.EQUIPPED).countById(id) > 0){
				MapleInventoryManipulator.removeById(getClient(), MapleInventoryType.EQUIPPED, id, 1, true, false);
				announce(UserLocal.UserEffect.getShowItemGain(id, (short) -1, true));
			}
		}
	}

	public RockPaperScissors getRPS(){
		return rps;
	}

	public void setRPS(RockPaperScissors rps){
		this.rps = rps;
	}

	public MapleCharacter clone(){
		return null;
	}

	public static class PQRankRecord{

		public String pq;
		public int tries;
		public int completed;
		public byte completeRate; // completed / tries
		public int fastestTime; // in seconds
		public Date fastestDate; // the date in which fastestTime occurs (based on pq end date)
		public String rank;

		public PQRankRecord(String pq, int tries, int completed, byte completeRate, int fastestTime, Date fastestDate, String rank){
			this.pq = pq;
			this.tries = tries;
			this.completed = completed;
			this.completeRate = completeRate;
			this.fastestTime = fastestTime;
			this.fastestDate = fastestDate;
			this.rank = rank;
		}

		public PQRankRecord(){}
	}

	public boolean canGetItem(int itemId){
		return canGetItem(itemId, (short) 1);
	}

	public boolean canGetItem(int itemId, short quantity){
		return canHoldItem(new Item(itemId, quantity));
	}

	public boolean beforeGetItem(int itemId){
		return beforeGetItem(itemId, (short) 1);
	}

	// check for space and prompt msg if there isnt space for that item.
	public boolean beforeGetItem(int itemId, short quantity){
		boolean able = canGetItem(itemId, quantity);
		if(!able){
			dropMessage(1, "Your inventory is full. Please remove an item from your " + ItemInformationProvider.getInstance().getInventoryType(itemId).name() + " inventory.");
		}
		return able;
	}

	public boolean endPQ(boolean success, long duration){
		return endPQ(success, duration, true);
	}

	// success = true = PQ is completed successfully.
	public boolean endPQ(boolean success, long duration, boolean disposeEim){
		if(partyQuest != null){
			return partyQuest.end(this, success, duration, disposeEim);
		}else return false;
	}

	// writeHaveItem = true means the player has the pq item now and he/she didnt have it
	// so it needs to be updated in the db.
	public boolean updatePQRank(String pq, String rank_){
		if(rank_ == null){ // gtfo.
			return false;
		}
		try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("update pqranks set rank=? where pq=? and characterid=?")){
			int index = 1; // heeh. //if rank to be updated
			ps.setString(index++, rank_); // rank
			ps.setString(index++, pq); // pq name
			ps.setInt(index, getId()); // char id
			ps.executeUpdate();
			return true;
		}catch(SQLException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, "update rank \nPQ: " + pq + " character id: " + getId());
		}
		return false;
	}

	public void setStat(MapleStat stat, int value){
		switch (stat){
			case AVAILABLEAP:
				this.remainingAp = value;
				break;
			case AVAILABLESP:
				setRemainingSp(value, getJobIndexBasedOnLevel());
				break;
			case DEX:
				this.dex = value;
				break;
			case EXP:
				this.exp.set(value);
				break;
			case FACE:
				this.face = value;
				break;
			case FAME:
				this.fame = value;
				break;
			case GACHAEXP:
				this.gachaexp.set(value);
				break;
			case HAIR:
				this.hair = value;
				break;
			case HP:
				this.hp = value;
				break;
			case INT:
				this.int_ = value;
				break;
			case JOB:
				this.job = MapleJob.getById(value);
				break;
			case LEVEL:
				this.level = value;
				break;
			case LUK:
				this.luk = value;
				break;
			case MAXHP:
				this.maxhp = value;
				break;
			case MAXMP:
				this.maxmp = value;
				break;
			case MESO:
				this.meso.set(value);
				break;
			case MP:
				this.mp = value;
				break;
			case SKIN:
				this.skinColor = MapleSkinColor.values()[value];
				break;
			case STR:
				this.str = value;
				break;
			default:
				break;
		}
	}

	public boolean canHoldItems(List<Item> items){
		for(Item item : items){
			if(!canHoldItem(item)) return false;
		}
		return true;
	}

	public boolean canHoldItemsType(List<Pair<Item, MapleInventoryType>> items){
		for(Pair<Item, MapleInventoryType> item : items){
			if(!canHoldItem(item.left)) return false;
		}
		return true;
	}

	public boolean canHoldItem(Item item){
		ItemInformationProvider ii = ItemInformationProvider.getInstance();
		MapleInventoryType type = ii.getInventoryType(item.getItemId());
		MapleInventory inventory = this.getInventory(type);
		return inventory.hasRoom(client, item);
	}

	public Item getPetItemByPetId(int petid){
		for(Item item : getInventory(MapleInventoryType.CASH).list()){
			if(item.getPetId() == petid) return item;
		}
		return null;
	}

	public void addPreviousMap(int mapid){
		previousMaps.add(mapid);
	}

	public List<Integer> getPreviousMaps(){
		return previousMaps;
	}

	public void setHighestLevel(int highestLevel){
		this.highestLevel = highestLevel;
	}

	public CRand getCRand(){
		if(crand == null) crand = new CRand();
		return crand;
	}

	public BossEntries getBossEntries(){
		return bossEntries;
	}

	public boolean hasCheckedMapCRC(){
		return hasCheckedMapCRC;
	}

	public void checkedMapCRC(){
		hasCheckedMapCRC = true;
	}

	public short getSubJob(){
		return nSubJob;
	}

	public void setSubJob(short nSubJob){
		this.nSubJob = nSubJob;
	}

	public void dispose(){
		NPCScriptManager.getInstance().dispose(client);
		QuestScriptManager.getInstance().dispose(client);
		client.clearLocalScripts();
		client.announce(CWvsContext.enableActions());
		client.removeClickedNPC();
	}
}
