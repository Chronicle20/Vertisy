package client.player;

import java.util.ArrayList;
import java.util.List;

import client.BuffDataHolder;
import client.MapleBuffStat;
import client.player.buffs.twostate.*;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.output.LittleEndianWriter;

public class SecondaryStat{

	public final List<TemporaryStatBase> temporaryStat = new ArrayList<>(7);

	public SecondaryStat(){
		for(TSIndex enIndex : TSIndex.values()){
			if(enIndex == TSIndex.PartyBooster){
				temporaryStat.add(new PartyBooster());
			}else if(enIndex == TSIndex.GuidedBullet){
				temporaryStat.add(new GuidedBullet());
			}else if(enIndex == TSIndex.EnergyCharged){
				temporaryStat.add(new TemporaryStatBase(true));
			}else{
				temporaryStat.add(new TwoStateTemporaryStat(enIndex != TSIndex.RideVehicle));
			}
		}
	}

	public TemporaryStatBase getTemporaryState(int index){
		return temporaryStat.get(index);
	}

	public void setTemporaryState(int index, TemporaryStatBase statBase){
		temporaryStat.set(index, statBase);
	}

	// Maybe works?
	public void encodeLocal(LittleEndianWriter lew, List<Pair<MapleBuffStat, BuffDataHolder>> statups, int buffid, int bufflength){// SecondaryStat::DecodeForLocal
		MaplePacketCreator.writeLongMask(lew, statups);
		for(Pair<MapleBuffStat, BuffDataHolder> statup : statups){
			if(statup.getLeft().isDisease()){
				lew.writeShort(statup.getRight().getValue());
				lew.writeShort(statup.getRight().getSourceID());
				lew.writeShort(statup.getRight().getSourceLevel());
			}else{
				if(statup.getLeft().equals(MapleBuffStat.SPEED_INFUSION)){
					lew.writeShort(0);
					lew.writeInt(statup.getRight().getValue());
					lew.writeInt(buffid);
				}else{
					lew.writeShort(statup.getRight().getValue());
					lew.writeInt(buffid);
				}
			}
			lew.writeInt(bufflength);
		}
		lew.write(0);// defenseAtt
		lew.write(0);// defenseState
		temporaryStat.forEach(stat-> stat.encodeForClient(lew));
		lew.writeShort(0);// tDelay
		boolean isMovementAffectingStat = statups.stream().anyMatch(stat-> stat.getLeft().isMovementAffectingStat());
		if(isMovementAffectingStat) lew.write(0);
	}

	// this shit is broken
	public void encodeRemote(LittleEndianWriter lew, List<Pair<MapleBuffStat, BuffDataHolder>> statups){// SecondaryStat::DecodeForRemote
		int[] mask = new int[4];
		// This are always sent in User Enter Field. Maybe make encodeRemote take a mask then add them just for UserEnterField?
		// not sure if giveForeignBuff requires all of em except for the one we are updating.
		//
		/*mask[MapleBuffStat.ENERGY_CHARGE.getSet()] |= MapleBuffStat.ENERGY_CHARGE.getMask();
		mask[MapleBuffStat.DASH_SPEED.getSet()] |= MapleBuffStat.DASH_SPEED.getMask();
		mask[MapleBuffStat.DASH_JUMP.getSet()] |= MapleBuffStat.DASH_JUMP.getMask();
		mask[MapleBuffStat.MONSTER_RIDING.getSet()] |= MapleBuffStat.MONSTER_RIDING.getMask();
		mask[MapleBuffStat.SPEED_INFUSION.getSet()] |= MapleBuffStat.SPEED_INFUSION.getMask();
		mask[MapleBuffStat.HOMING_BEACON.getSet()] |= MapleBuffStat.HOMING_BEACON.getMask();
		mask[MapleBuffStat.UNDEAD.getSet()] |= MapleBuffStat.UNDEAD.getMask();*/
		MaplePacketCreator.writeLongMask(lew, mask, statups);
		for(Pair<MapleBuffStat, BuffDataHolder> statup : statups){
			if(statup.getLeft().isDisease()){
				if(statup.getLeft() == MapleBuffStat.POISON){
					lew.writeShort(statup.getRight().getValue());
				}
				lew.writeShort(statup.getRight().getSourceID());
				lew.writeShort(statup.getRight().getSourceLevel());
			}else{
				lew.writeInt(statup.getRight().getValue());
			}
		}
		lew.write(0);
		lew.write(0);
		temporaryStat.forEach(stat-> stat.encodeForClient(lew));
	}
}
