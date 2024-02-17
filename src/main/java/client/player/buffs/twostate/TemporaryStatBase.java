package client.player.buffs.twostate;

import tools.data.output.LittleEndianWriter;

/**
 * TemporaryStatBase
 * 
 * @author Eric
 */
public class TemporaryStatBase{

	public int nOption;// m_value
	public int rOption;// m_reason
	public long tLastUpdated;
	public int usExpireTerm;
	public final boolean bDynamicTermSet;

	public TemporaryStatBase(boolean bDynamicTermSet){
		this.nOption = 0;
		this.rOption = 0;
		this.tLastUpdated = System.currentTimeMillis();
		this.bDynamicTermSet = bDynamicTermSet;
	}

	public void encodeForClient(LittleEndianWriter lew){
		lew.writeInt(nOption);// value
		lew.writeInt(rOption);// reason
		lew.writeLong(tLastUpdated);
		if(bDynamicTermSet){
			lew.writeShort(usExpireTerm);
		}
	}

	public int GetExpireTerm(){
		if(bDynamicTermSet) return 1000 * usExpireTerm;
		return Integer.MAX_VALUE;
	}

	public int GetMaxValue(){
		return 10000;
	}

	public boolean IsActivated(){
		return nOption >= 10000;
	}

	public boolean IsExpiredAt(long tCur){
		if(bDynamicTermSet) return GetExpireTerm() > tCur - tLastUpdated;
		return false;
	}

	public int GetReason(){
		return rOption;
	}

	public int GetValue(){
		return nOption;
	}

	public void Reset(){
		nOption = 0;
		rOption = 0;
		tLastUpdated = System.currentTimeMillis();
	}
}