package client.player.buffs.twostate;

/**
 * TSIndex
 * Handles the TemporaryStat indexes for a TwoStateTemporaryStat.
 * 
 * @author Eric
 */
public enum TSIndex{
	EnergyCharged(0),
	DashSpeed(1),
	DashJump(2),
	RideVehicle(3),
	PartyBooster(4),
	GuidedBullet(5),
	Undead(6);

	private final int index;

	private TSIndex(int index){
		this.index = index;
	}

	public int getIndex(){
		return index;
	}
}