package server.maps;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import tools.Pair;
import tools.Triple;

public class MonsterCarnivalSettings{

	/**
	 * Amount of cp you lose on death
	 */
	private int deathCP;
	/**
	 * The effect location for loss.
	 */
	private String effectLose;
	/**
	 * The effect location for win.
	 */
	private String effectWin;
	// TODO: Guardian shit.. just numbers between 0-8
	/**
	 * Max amount of guardians in the map
	 * Can sometimes be null
	 */
	private int guardianGenMax;
	/**
	 * 0-10, key
	 * foothold
	 * x,y
	 */
	private Map<Integer, Pair<Integer, Point>> guardianGenPos = new HashMap<Integer, Pair<Integer, Point>>();
	/**
	 * If the map is divided.
	 * TODO: Why does nexon care?
	 */
	private int mapDivided;
	/**
	 * 0-9, key
	 * mob id
	 * mobTime
	 * spendCP - Amount of CP to spawn
	 */
	private Map<Integer, Triple<Integer, Integer, Integer>> mob = new HashMap<Integer, Triple<Integer, Integer, Integer>>();
	/*
	 * TODO: ?? Max amount of mobs? each side maybe??
	 * Can be -1
	 */
	private int mobGenMax;
	/**
	 * Key
	 * Position info for mobs. cy, fh, point(x, y)
	 */
	private Map<Integer, Triple<Integer, Integer, Point>> mobGenPos = new HashMap<Integer, Triple<Integer, Integer, Point>>();
	/**
	 * Reactor mob id for blue
	 */
	private int reactorBlue;
	/**
	 * Reactor mob id for red
	 */
	private int reactorRed;
	// TODO: rewards, find the point of all the properties
	/**
	 * Map to warp to on loss
	 */
	private int rewardMapLose;
	/**
	 * Map to warp to on win
	 */
	private int rewardMapWin;
	// TODO: skill stuff, 0-7, values are just the number.. no clue on the point
	/**
	 * Path to the lost sound
	 */
	private String soundLose;
	/**
	 * Path to the win sound
	 */
	private String soundWin;
	/**
	 * Amount of time in seconds you have by default.
	 */
	private int timeDefault;
	/**
	 * Amount of time added in seconds you get when expanded.
	 * TODO: no clue how to expand..
	 */
	private int timeExpand;
	/**
	 * TODO: ???
	 */
	private int timeFinish;
	/**
	 * TODO: ???
	 */
	private int timeMessage;

	public MonsterCarnivalSettings(){
		super();
	}

	public int getDeathCP(){
		return deathCP;
	}

	public void setDeathCP(int deathCP){
		this.deathCP = deathCP;
	}

	public String getEffectLose(){
		return effectLose;
	}

	public void setEffectLose(String effectLose){
		this.effectLose = effectLose;
	}

	public String getEffectWin(){
		return effectWin;
	}

	public void setEffectWin(String effectWin){
		this.effectWin = effectWin;
	}

	public int getGuardianGenMax(){
		return guardianGenMax;
	}

	public void setGuardianGenMax(int guardianGenMax){
		this.guardianGenMax = guardianGenMax;
	}

	public int getMapDivided(){
		return mapDivided;
	}

	public void setMapDivided(int mapDivided){
		this.mapDivided = mapDivided;
	}

	public int getMobGenMax(){
		return mobGenMax;
	}

	public void setMobGenMax(int mobGenMax){
		this.mobGenMax = mobGenMax;
	}

	public int getReactorBlue(){
		return reactorBlue;
	}

	public void setReactorBlue(int reactorBlue){
		this.reactorBlue = reactorBlue;
	}

	public int getReactorRed(){
		return reactorRed;
	}

	public void setReactorRed(int reactorRed){
		this.reactorRed = reactorRed;
	}

	public int getRewardMapLose(){
		return rewardMapLose;
	}

	public void setRewardMapLose(int rewardMapLose){
		this.rewardMapLose = rewardMapLose;
	}

	public int getRewardMapWin(){
		return rewardMapWin;
	}

	public void setRewardMapWin(int rewardMapWin){
		this.rewardMapWin = rewardMapWin;
	}

	public String getSoundLose(){
		return soundLose;
	}

	public void setSoundLose(String soundLose){
		this.soundLose = soundLose;
	}

	public String getSoundWin(){
		return soundWin;
	}

	public void setSoundWin(String soundWin){
		this.soundWin = soundWin;
	}

	public int getTimeDefault(){
		return timeDefault;
	}

	public void setTimeDefault(int timeDefault){
		this.timeDefault = timeDefault;
	}

	public int getTimeExpand(){
		return timeExpand;
	}

	public void setTimeExpand(int timeExpand){
		this.timeExpand = timeExpand;
	}

	public int getTimeFinish(){
		return timeFinish;
	}

	public void setTimeFinish(int timeFinish){
		this.timeFinish = timeFinish;
	}

	public int getTimeMessage(){
		return timeMessage;
	}

	public void setTimeMessage(int timeMessage){
		this.timeMessage = timeMessage;
	}

	public Map<Integer, Pair<Integer, Point>> getGuardianGenPos(){
		return guardianGenPos;
	}

	public void addGuardianGenPos(int key, int fh, Point pos){
		guardianGenPos.put(key, new Pair<Integer, Point>(fh, pos));
	}

	public Map<Integer, Triple<Integer, Integer, Integer>> getMob(){
		return mob;
	}

	public void addMob(int key, int mobid, int mobTime, int spendCP){
		mob.put(key, new Triple<Integer, Integer, Integer>(mobid, mobTime, spendCP));
	}

	public Map<Integer, Triple<Integer, Integer, Point>> getMobGenPos(){
		return mobGenPos;
	}

	public void addMobGenPos(int key, int cy, int fh, Point pos){
		mobGenPos.put(key, new Triple<Integer, Integer, Point>(cy, fh, pos));
	}

	/*
	 * mob id
	 * mobTime
	 * spendCP - Amount of CP to spawn
	 */
	public Triple<Integer, Integer, Integer> getMobInfo(int key){
		return mob.get(key);
	}
}
