package server.life;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Dec 9, 2017
 */
public enum MobStat{
	PAD(0),
	PDR(1),
	MAD(2),
	MDR(3),
	ACC(4),
	EVA(5),
	Speed(6),
	Stun(7),
	Freeze(8),
	Poison(9),
	Seal(10),
	Darkness(11),
	PowerUp(12),
	MagicUp(13),
	PGuardUp(14),
	MGuardUp(15),
	PImmune(18),
	MImmune(19),
	Doom(16),
	Web(17),
	HardSkin(21),
	Ambush(22),
	Venom(24),
	Blind(25),
	SealSkill(26),
	Dazzle(28),
	PCounter(29),
	MCounter(30),
	RiseByToss(32),
	BodyPressure(33),
	Weakness(34),
	TimeBomb(35),
	Showdown(20),
	MagicCrash(36),
	Burned(27),
	Disable(31),;

	private final int shift;
	private final int mask;
	private final byte set;

	MobStat(int shift){
		this.shift = shift;
		this.mask = 1 << (shift >> 32);
		this.set = (byte) (shift >> 5);
	}

	public int getShift(){
		return shift;
	}

	public int getMask(){
		return mask;
	}

	public byte getSet(){
		return set;
	}

	public boolean isMovementAffectingStat(){
		switch (this){
			case Stun:
			case Freeze:
			case Doom:
			case RiseByToss:
				return true;
			default:
				return false;
		}
	}

	public static MobStat getByValue(int value){
		for(MobStat ms : values()){
			if(ms.getShift() == value) return ms;
		}
		return null;
	}
}
