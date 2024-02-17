package client;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Feb 13, 2016
 */
public enum RSSkill{
	Prayer(99, 50),
	Capacity(99, 25),
	Fishing(99, 1),
	Combat(99, 10),
	Health(99, 5),
	Mana(99, 5),
	Slayer(99, 10), // 9201043
	Smithing(99, 5),
	Mining(99, 5),
	Crafting(99, 2);

	private byte maxLevel;
	private int track;

	private RSSkill(int maxLevel, int track){// Java is retarded, needs to be an int.
		this.maxLevel = (byte) maxLevel;
		this.track = track;
	}

	public byte getMaxLevel(){
		return maxLevel;
	}

	/**
	 * @return How many exp gains till an update message.
	 */
	public int getTrack(){
		return track;
	}
}
