package server.maps;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import server.MaplePortal;
import server.life.MapleMonster;
import server.life.MapleNPC;
import server.life.SpawnPoint;
import server.maps.objects.MapleGenericPortal;
import server.maps.objects.MapleMapObject;
import server.maps.objects.PlayerNPC;
import server.reactors.MapleReactor;
import server.reactors.MapleReactorFactory;
import tools.Pair;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.LittleEndianWriter;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Aug 28, 2016
 */
public class MapleMapData{

	private String onFirstUserEnter, onUserEnter;
	private int returnMap, forcedReturnMap = 999999999;
	private int fieldLimit = 0;
	private int VRTop = 0, VRBottom = 0, VRLeft = 0, VRRight = 0;
	private short mobInterval = 5000;
	private byte monsterRate;
	private int mobCapacity = -1;
	private int fieldType;
	private int town;
	private int decHP = 0, protectItem = 0;
	//
	private long timeLimit;
	//
	private String mapName, streetName;
	private String mapMark;
	//
	private boolean everlast = false;
	private boolean clock;
	private boolean boat;
	//
	private MapleFootholdTree footholds = null;
	private Set<MapleLadderFoothold> ladderFootholds = new HashSet<>();
	private Pair<Integer, String> timeMob = null;
	private List<Rectangle> areas = Collections.synchronizedList(new ArrayList<>());
	private Collection<SpawnPoint> monsterSpawn = Collections.synchronizedList(new LinkedList<>());
	private Collection<SpawnPoint> customMonsterSpawn = Collections.synchronizedList(new LinkedList<>());
	private Collection<MapleMapObject> mapObjects = Collections.synchronizedList(new LinkedList<>());
	private Collection<MapleReactor> reactors = Collections.synchronizedList(new LinkedList<>());
	private Collection<MaplePortal> portals = Collections.synchronizedList(new LinkedList<>());
	private Map<Integer, Integer> backgroundTypes = new ConcurrentHashMap<>();
	// Monster Carnival
	private MonsterCarnivalSettings mcs;
	//
	private String bgm;
	// Anticheat
	private boolean swim, fly;
	//
	private double recovery;

	public void save(MaplePacketLittleEndianWriter mplew){
		mplew.writeMapleAsciiString(onFirstUserEnter);
		mplew.writeMapleAsciiString(onUserEnter);
		mplew.writeInt(returnMap);
		mplew.writeInt(forcedReturnMap);
		mplew.writeInt(fieldLimit);
		mplew.writeInt(VRTop);
		mplew.writeInt(VRBottom);
		mplew.writeInt(VRLeft);
		mplew.writeInt(VRRight);
		mplew.writeShort(mobInterval);
		mplew.write(monsterRate);
		mplew.writeInt(mobCapacity);
		mplew.writeInt(fieldType);
		mplew.writeInt(town);
		mplew.writeInt(decHP);
		mplew.writeInt(protectItem);
		//
		mplew.writeLong(timeLimit);
		//
		mplew.writeMapleAsciiString(mapName);
		mplew.writeMapleAsciiString(streetName);
		mplew.writeMapleAsciiString(mapMark);
		//
		mplew.writeBoolean(everlast);
		mplew.writeBoolean(clock);
		mplew.writeBoolean(boat);
		//
		footholds.save(mplew);// todo:
		mplew.writeInt(ladderFootholds.size());
		for(MapleLadderFoothold lfh : ladderFootholds){
			lfh.save(mplew);
		}
		mplew.writeBoolean(timeMob != null);
		if(timeMob != null){
			mplew.writeInt(timeMob.left);
			mplew.writeMapleAsciiString(timeMob.right);
		}
		mplew.writeInt(areas.size());
		for(Rectangle rect : areas){
			mplew.writeInt(rect.x);
			mplew.writeInt(rect.y);
			mplew.writeInt(rect.width);
			mplew.writeInt(rect.height);
		}
		mplew.writeInt(monsterSpawn.size());
		for(SpawnPoint sp : monsterSpawn){
			sp.save(mplew);
		}
		mplew.writeInt(customMonsterSpawn.size());
		for(SpawnPoint sp : customMonsterSpawn){
			sp.save(mplew);
		}
		mplew.writeInt(mapObjects.size());
		for(MapleMapObject mo : mapObjects){
			mplew.writeBoolean((mo instanceof MapleNPC));
			if(mo instanceof MapleNPC){
				((MapleNPC) mo).save(mplew);
			}else if(mo instanceof PlayerNPC){
				((PlayerNPC) mo).save(mplew);
			}else{
				System.out.println("Bad mmo in MapleMapData: " + mo.getType().name());
			}
		}
		mplew.writeInt(reactors.size());
		for(MapleReactor mr : reactors){
			saveReactor(mr, mplew);
		}
		mplew.writeInt(portals.size());
		for(MaplePortal mp : portals){
			mp.save(mplew);
		}
		mplew.writeInt(backgroundTypes.size());
		for(int in : backgroundTypes.keySet()){
			mplew.writeInt(in);
			mplew.writeInt(backgroundTypes.get(in));
		}
		// mcs
		mplew.writeMapleAsciiString(bgm);
		mplew.writeBoolean(swim);
		mplew.writeBoolean(fly);
		mplew.writeDouble(recovery);
	}

	public void load(LittleEndianAccessor slea){
		onFirstUserEnter = slea.readMapleAsciiString();
		onUserEnter = slea.readMapleAsciiString();
		returnMap = slea.readInt();
		forcedReturnMap = slea.readInt();
		fieldLimit = slea.readInt();
		VRTop = slea.readInt();
		VRBottom = slea.readInt();
		VRLeft = slea.readInt();
		VRRight = slea.readInt();
		mobInterval = slea.readShort();
		monsterRate = slea.readByte();
		mobCapacity = slea.readInt();
		fieldType = slea.readInt();
		town = slea.readInt();
		decHP = slea.readInt();
		protectItem = slea.readInt();
		//
		timeLimit = slea.readLong();
		//
		mapName = slea.readMapleAsciiString();
		streetName = slea.readMapleAsciiString();
		mapMark = slea.readMapleAsciiString();
		//
		everlast = slea.readBoolean();
		clock = slea.readBoolean();
		boat = slea.readBoolean();
		//
		footholds = new MapleFootholdTree();
		footholds.load(slea);
		int size = slea.readInt();
		for(int i = 0; i < size; i++){
			MapleLadderFoothold lfh = new MapleLadderFoothold();
			lfh.load(slea);
			ladderFootholds.add(lfh);
		}
		if(slea.readBoolean()){
			timeMob = new Pair<Integer, String>();
			timeMob.left = slea.readInt();
			timeMob.right = slea.readMapleAsciiString();
		}
		size = slea.readInt();
		for(int i = 0; i < size; i++){
			areas.add(new Rectangle(slea.readInt(), slea.readInt(), slea.readInt(), slea.readInt()));
		}
		size = slea.readInt();
		for(int i = 0; i < size; i++){
			SpawnPoint sp = new SpawnPoint();
			sp.load(slea);
			monsterSpawn.add(sp);
		}
		size = slea.readInt();
		for(int i = 0; i < size; i++){
			SpawnPoint sp = new SpawnPoint();
			sp.load(slea);
			customMonsterSpawn.add(sp);
		}
		size = slea.readInt();
		for(int i = 0; i < size; i++){
			if(slea.readBoolean()){
				MapleNPC npc = new MapleNPC();
				npc.load(slea);
				mapObjects.add(npc);
			}else{
				PlayerNPC npc = new PlayerNPC();
				npc.load(slea);
				mapObjects.add(npc);
			}
		}
		size = slea.readInt();
		for(int i = 0; i < size; i++){
			reactors.add(loadReactor(slea));
		}
		size = slea.readInt();
		for(int i = 0; i < size; i++){
			MaplePortal mp = new MapleGenericPortal();
			mp.load(slea);
			portals.add(mp);
		}
		size = slea.readInt();
		for(int i = 0; i < size; i++){
			backgroundTypes.put(slea.readInt(), slea.readInt());
		}
		// mcs
		bgm = slea.readMapleAsciiString();
		swim = slea.readBoolean();
		fly = slea.readBoolean();
		recovery = slea.readDouble();
	}

	private MapleReactor loadReactor(LittleEndianAccessor lea){
		int rid = lea.readInt();
		MapleReactor myReactor = new MapleReactor(MapleReactorFactory.getReactor(rid));
		myReactor.setPosition(lea.readPos());
		myReactor.setDelay(lea.readInt());
		myReactor.setState(0);
		if(lea.readBoolean()) myReactor.setName(lea.readMapleAsciiString());
		else myReactor.setName("");
		return myReactor;
	}

	private void saveReactor(MapleReactor mr, LittleEndianWriter lew){
		lew.writeInt(mr.getId());
		lew.writePos(mr.getPosition());
		lew.writeInt(mr.getDelay());
		lew.writeBoolean(mr.getName() != null);
		if(mr.getName() != null) lew.writeMapleAsciiString(mr.getName());
	}

	public void setMonsterRate(float monsterRate){
		this.monsterRate = (byte) Math.ceil(monsterRate);
		if(this.monsterRate == 0){
			this.monsterRate = 1;
		}
	}

	public void setReturnMap(int returnMap){
		this.returnMap = returnMap;
	}

	public int getReturnMap(){
		return this.returnMap;
	}

	public int getForcedReturnMap(){
		return forcedReturnMap;
	}

	public void setForcedReturnMap(int map){
		this.forcedReturnMap = map;
	}

	public void setOnUserEnter(String onUserEnter){
		this.onUserEnter = onUserEnter;
	}

	public String getOnUserEnter(){
		return onUserEnter;
	}

	public void setOnFirstUserEnter(String onFirstUserEnter){
		this.onFirstUserEnter = onFirstUserEnter;
	}

	public String getOnFirstUserEnter(){
		return onFirstUserEnter;
	}

	public void setFieldLimit(int fieldLimit){
		this.fieldLimit = fieldLimit;
	}

	public int getFieldLimit(){
		return fieldLimit;
	}

	public void setVRTop(int VRTop){
		this.VRTop = VRTop;
	}

	public int getVRTop(){
		return VRTop;
	}

	public void setVRBottom(int VRBottom){
		this.VRBottom = VRBottom;
	}

	public int getVRBottom(){
		return VRBottom;
	}

	public void setVRLeft(int VRLeft){
		this.VRLeft = VRLeft;
	}

	public int getVRLeft(){
		return VRLeft;
	}

	public void setVRRight(int VRRight){
		this.VRRight = VRRight;
	}

	public int getVRRight(){
		return VRRight;
	}

	public void setMobInterval(short interval){
		this.mobInterval = interval;
	}

	public short getMobInterval(){
		return mobInterval;
	}

	public void setFootholds(MapleFootholdTree footholds){
		this.footholds = footholds;
	}

	public MapleFootholdTree getFootholds(){
		return footholds;
	}

	public Set<MapleLadderFoothold> getLadderFootholds(){
		return ladderFootholds;
	}

	public void timeMob(int id, String msg){
		timeMob = new Pair<>(id, msg);
	}

	public Pair<Integer, String> getTimeMob(){
		return timeMob;
	}

	public void addMapleArea(Rectangle rec){
		areas.add(rec);
	}

	public List<Rectangle> getAreas(){
		return areas;
	}

	public Rectangle getArea(int index){
		return areas.get(index);
	}

	public void addCustomMonsterSpawn(MapleMonster monster, int mobTime, int team){
		Point newpos = calcPointBelow(monster.getPosition());
		newpos.y -= 1;
		SpawnPoint sp = new SpawnPoint(monster, newpos, !monster.isMobile(), mobTime, getMobInterval(), team);
		customMonsterSpawn.add(sp);
	}

	/**
	 * @return A cloned version of custom monster spawns
	 */
	public Collection<SpawnPoint> getCustomMonsterSpawns(){
		Collection<SpawnPoint> ret = Collections.synchronizedList(new LinkedList<>());
		customMonsterSpawn.forEach(sp-> ret.add(sp.clone()));
		return ret;
	}

	public void addMonsterSpawn(MapleMonster monster, int mobTime, int team){
		Point newpos = calcPointBelow(monster.getPosition());
		newpos.y -= 1;
		SpawnPoint sp = new SpawnPoint(monster, newpos, !monster.isMobile(), mobTime, getMobInterval(), team);
		monsterSpawn.add(sp);
	}

	/**
	 * @return A cloned version of monster spawns
	 */
	public Collection<SpawnPoint> getMonsterSpawns(){
		Collection<SpawnPoint> ret = Collections.synchronizedList(new LinkedList<>());
		monsterSpawn.forEach(sp-> ret.add(sp.clone()));
		return ret;
	}

	public void addMapObject(MapleMapObject mapobject){
		mapObjects.add(mapobject);
	}

	/**
	 * @return A cloned version of map objects
	 */
	public Collection<MapleMapObject> getMapObjects(){
		Collection<MapleMapObject> ret = Collections.synchronizedList(new LinkedList<>());
		mapObjects.forEach(mmo-> ret.add(mmo.clone()));
		return ret;
	}

	public void addPortal(MaplePortal portal){
		portals.add(portal);
	}

	/**
	 * @return A cloned version of portals
	 */
	public Collection<MaplePortal> getPortals(){
		Collection<MaplePortal> ret = Collections.synchronizedList(new LinkedList<>());
		portals.forEach(portal-> ret.add(portal.clone()));
		return ret;
	}

	public void addReactor(MapleReactor reactor){
		reactors.add(reactor);
	}

	/**
	 * @return A cloned version of reactors
	 */
	public Collection<MapleReactor> getReactors(){
		Collection<MapleReactor> ret = Collections.synchronizedList(new LinkedList<>());
		reactors.forEach(mr-> ret.add(mr.clone()));
		return ret;
	}

	public MonsterCarnivalSettings getMCS(){
		return mcs;
	}

	public void setMCS(MonsterCarnivalSettings mcs){
		this.mcs = mcs;
	}

	public void setBackgroundTypes(HashMap<Integer, Integer> backTypes){
		backgroundTypes.putAll(backTypes);
	}

	public Map<Integer, Integer> getBackgroundTypes(){
		return backgroundTypes;
	}

	public String getBGM(){
		return bgm;
	}

	public void setBGM(String bgm){
		this.bgm = bgm;
	}

	public boolean canSwim(){
		return swim;
	}

	public void setSwim(boolean b){
		swim = b;
	}

	public boolean canFly(){
		return fly;
	}

	public void setFly(boolean b){
		fly = b;
	}

	public void setMobCapacity(int capacity){
		this.mobCapacity = capacity;
	}

	public int getMobCapacity(){
		return this.mobCapacity;
	}

	public void setFieldType(int fieldType){
		this.fieldType = fieldType;
	}

	public int getFieldType(){
		return this.fieldType;
	}

	public String getMapMark(){
		return mapMark;
	}

	public void setMapMark(String mark){
		this.mapMark = mark;
	}

	public String getMapName(){
		return mapName;
	}

	public void setMapName(String mapName){
		this.mapName = mapName;
	}

	public String getStreetName(){
		return streetName;
	}

	public void setStreetName(String streetName){
		this.streetName = streetName;
	}

	public void setEverlast(boolean everlast){
		this.everlast = everlast;
	}

	public boolean getEverlast(){
		return everlast;
	}

	public void setTown(int isTown){
		this.town = isTown;
	}

	public boolean isTown(){
		return(town > 0);
	}

	public void setClock(boolean hasClock){
		this.clock = hasClock;
	}

	public boolean hasClock(){
		return clock;
	}

	public int getHPDec(){
		return decHP;
	}

	public void setHPDec(int delta){
		decHP = delta;
	}

	public int getHPDecProtect(){
		return protectItem;
	}

	public void setHPDecProtect(int delta){
		this.protectItem = delta;
	}

	public void setBoat(boolean hasBoat){
		this.boat = hasBoat;
	}

	public boolean hasBoat(){
		return this.boat;
	}

	public long getTimeLimit(){
		return timeLimit;
	}

	public void setTimeLimit(long timeLimit){
		this.timeLimit = timeLimit;
	}

	public void setRecovery(double recovery){
		this.recovery = recovery;
	}

	public double getRecovery(){
		return recovery;
	}

	private Point calcPointBelow(Point initial){
		MapleFoothold fh = getFootholds().findBelow(initial);
		if(fh == null) return null;
		int dropY = fh.getY1();
		if(!fh.isWall() && fh.getY1() != fh.getY2()){
			double s1 = Math.abs(fh.getY2() - fh.getY1());
			double s2 = Math.abs(fh.getX2() - fh.getX1());
			double s5 = Math.cos(Math.atan(s2 / s1)) * (Math.abs(initial.x - fh.getX1()) / Math.cos(Math.atan(s1 / s2)));
			if(fh.getY2() < fh.getY1()){
				dropY = fh.getY1() - (int) s5;
			}else{
				dropY = fh.getY1() + (int) s5;
			}
		}
		return new Point(initial.x, dropY);
	}
}
