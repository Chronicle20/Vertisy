package client.player.buffs.twostate;

import tools.data.output.LittleEndianWriter;

/**
 * TemporaryStat_PartyBooster
 * 
 * @author Eric
 */
public class PartyBooster extends TwoStateTemporaryStat{

	public int tCurrentTime;

	public PartyBooster(){
		super(false);
		this.tCurrentTime = 0;
		this.usExpireTerm = 0;
	}

	@Override
	public void encodeForClient(LittleEndianWriter oPacket){
		super.encodeForClient(oPacket);
		oPacket.writeLong(tCurrentTime);
		oPacket.writeShort(usExpireTerm);
	}

	@Override
	public int GetExpireTerm(){
		return 1000 * usExpireTerm;
	}

	@Override
	public boolean IsExpiredAt(long tCur){
		return GetExpireTerm() < tCur - tCurrentTime;
	}

	@Override
	public void Reset(){
		super.Reset();
		tCurrentTime = 0;
	}
}