package client;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since May 7, 2016
 */
public class SlayerTask{

	private int targetID, targetLevel;
	private int kills;
	private int requiredKills;
	private String targetMap;

	public SlayerTask(int targetid, int targetLevel, String targetMap){
		this.targetID = targetid;
		this.targetLevel = targetLevel;
		this.targetMap = targetMap;
	}

	public int getTargetID(){
		return targetID;
	}

	public int getTargetLevel(){
		return targetLevel;
	}

	public void setKills(int kills){
		this.kills = kills;
	}

	public int getKills(){
		return kills;
	}

	public boolean incrementKills(){
		kills = Math.min(++kills, requiredKills);
		return isCompleted();
	}

	public void setRequiredKills(int requiredKills){
		this.requiredKills = requiredKills;
	}

	public int getRequiredKills(){
		return requiredKills;
	}

	public boolean isCompleted(){
		return kills >= requiredKills;
	}

	public String getMap(){
		return targetMap;
	}
}
