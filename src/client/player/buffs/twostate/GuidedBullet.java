package client.player.buffs.twostate;

import tools.data.output.LittleEndianWriter;

/**
 * TemporaryStat_GuidedBullet
 * 
 * @author Eric
 */
public class GuidedBullet extends TemporaryStatBase{

	public int dwMobID;
	public int dwUserID;

	public GuidedBullet(){
		super(false);
		this.dwMobID = 0;
		this.dwUserID = 0;
	}

	@Override
	public void encodeForClient(LittleEndianWriter oPacket){
		super.encodeForClient(oPacket);
		oPacket.writeInt(dwMobID);
	}

	public int GetMobID(){
		return dwMobID;
	}

	public int GetUserID(){
		return dwUserID;
	}

	@Override
	public void Reset(){
		super.Reset();
		this.dwMobID = 0;
		this.dwUserID = 0;
	}
}