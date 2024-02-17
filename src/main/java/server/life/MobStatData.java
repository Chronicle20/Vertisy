package server.life;

import tools.data.input.LittleEndianAccessor;
import tools.data.output.LittleEndianWriter;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Dec 9, 2017
 */
public class MobStatData{

	public MobStat stat;
	public short nOption;
	/**
	 * Usually the skill id
	 */
	public int rOption;
	public int skillLevel;
	// public short duration;
	// public long startTime;
	public long endTime, duration;
	public boolean mobSkill;
	// Nexon just does duration as 'end time'
	// then in packet they do (duration - timestamp) / 500
	public int pCounter, mCounter;
	public int counterProb = 100;// not set atm

	// Poison:
	// n = damage
	// r = skillid
	// w = ownerid
	public MobStatData(){
		//
	}

	public MobStatData(MobStat stat, long duration){
		this.stat = stat;
		this.endTime = System.currentTimeMillis() + duration;
		this.duration = duration;
	}

	public MobStatData(MobStat stat, int nOption, int rOption, long duration){
		this.stat = stat;
		this.nOption = (short) nOption;
		this.rOption = rOption;
		this.endTime = System.currentTimeMillis() + duration;
		this.duration = duration;
		setCounterData(stat, nOption);
	}

	public MobStatData(MobStat stat, int nOption, int skillid, int skillLevel, long duration){
		this.stat = stat;
		this.nOption = (short) nOption;
		this.rOption = skillid;
		this.skillLevel = skillLevel;
		// this.rOption = skillid | ((skillLevel << 16) & 0xFF);
		// this.rOption = skillid;// | ((skillLevel << 16) & 0xFF);
		this.endTime = System.currentTimeMillis() + duration;
		this.duration = duration;
		mobSkill = true;
		setCounterData(stat, nOption);
	}

	public void setEndTime(){
		this.endTime = System.currentTimeMillis() + duration;
	}

	private void setCounterData(MobStat stat, int nOption){
		if(stat.equals(MobStat.PCounter)) pCounter = nOption;
		else if(stat.equals(MobStat.MCounter)) mCounter = nOption;
	}

	public void encode(LittleEndianWriter lew){
		lew.writeInt(stat.getShift());
		lew.writeShort(nOption);
		lew.writeInt(rOption);
		lew.writeInt(skillLevel);
		lew.writeLong(duration);
		lew.writeBoolean(mobSkill);
		lew.writeInt(pCounter);
		lew.writeInt(mCounter);
		lew.writeInt(counterProb);
	}

	public void decode(LittleEndianAccessor lea){
		stat = MobStat.getByValue(lea.readInt());
		nOption = lea.readShort();
		rOption = lea.readInt();
		skillLevel = lea.readInt();
		duration = lea.readLong();
		mobSkill = lea.readBoolean();
		pCounter = lea.readInt();
		mCounter = lea.readInt();
		counterProb = lea.readInt();
		setEndTime();
	}
}
