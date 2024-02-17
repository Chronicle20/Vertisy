package server.item;

import provider.MapleData;
import provider.MapleDataTool;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.LittleEndianWriter;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Oct 27, 2017
 */
public class PotentialLevelData{

	@Override
	public String toString(){
		return "PotentialLevelData [boss=" + boss + ", RecoveryUP=" + RecoveryUP + ", RecoveryHP=" + RecoveryHP + ", RecoveryMP=" + RecoveryMP + ", mpRestore=" + mpRestore + ", mpconReduce=" + mpconReduce + ", ignoreTargetDEF=" + ignoreTargetDEF + ", ignoreDAM=" + ignoreDAM + ", ignoreDAMr=" + ignoreDAMr + ", DAMreflect=" + DAMreflect + ", HP=" + HP + ", MP=" + MP + ", incSTR=" + incSTR + ", incDEX=" + incDEX + ", incINT=" + incINT + ", incLUK=" + incLUK + ", incSpeed=" + incSpeed + ", incJump=" + incJump + ", incPAD=" + incPAD + ", incPDD=" + incPDD + ", incMAD=" + incMAD + ", incMDD=" + incMDD + ", incEVA=" + incEVA + ", incACC=" + incACC + ", incMHP=" + incMHP + ", incMMP=" + incMMP + ", incSTRr=" + incSTRr + ", incDEXr=" + incDEXr + ", incINTr=" + incINTr + ", incLUKr=" + incLUKr + ", incPADr=" + incPADr + ", incPDDr=" + incPDDr + ", incMADr=" + incMADr + ", incMDDr=" + incMDDr + ", incEVAr=" + incEVAr + ", incACCr=" + incACCr + ", incCr=" + incCr + ", incMHPr=" + incMHPr + ", incMMPr=" + incMMPr + ", incDAMr=" + incDAMr + ", incAllskill=" + incAllskill + ", incMesoProp=" + incMesoProp + ", incRewardProp=" + incRewardProp + ", face=" + face + ", attackType=" + attackType + ", time=" + time + ", prop=" + prop + ", level=" + level + "]";
	}

	public boolean boss;
	public int RecoveryUP, RecoveryHP, RecoveryMP, mpRestore, mpconReduce;
	//
	public int ignoreTargetDEF, ignoreDAM, ignoreDAMr;
	//
	public int DAMreflect;
	//
	public int HP, MP;// % chance to recover xx hp/mp after defeating a monster
	//
	public int incSTR, incDEX, incINT, incLUK, incSpeed, incJump, incPAD, incPDD, incMAD, incMDD, incEVA, incACC, incMHP, incMMP;
	public int incSTRr, incDEXr, incINTr, incLUKr, incPADr, incPDDr, incMADr, incMDDr, incEVAr, incACCr, incCr, incMHPr, incMMPr;
	public int incDAMr;
	public int incAllskill;// x levels to all skills
	public int incMesoProp, incRewardProp;// meso, item drop rate.
	//
	public String face;
	public int attackType;
	public int time;
	public int prop;
	public int level;

	public void load(MapleData data){
		for(MapleData stat : data.getChildren()){
			switch (stat.getName()){
				case "boss":{
					boss = MapleDataTool.getInt(stat) > 0;
					break;
				}
				case "RecoveryUP":{
					RecoveryUP = MapleDataTool.getInt(stat);
					break;
				}
				case "RecoveryHP":{
					RecoveryHP = MapleDataTool.getInt(stat);
					break;
				}
				case "RecoveryMP":{
					RecoveryMP = MapleDataTool.getInt(stat);
					break;
				}
				case "mpRestore":{
					mpRestore = MapleDataTool.getInt(stat);
					break;
				}
				case "mpconReduce":{
					mpconReduce = MapleDataTool.getInt(stat);
					break;
				}
				case "ignoreTargetDEF":{
					ignoreTargetDEF = MapleDataTool.getInt(stat);
					break;
				}
				case "ignoreDAM":{
					ignoreDAM = MapleDataTool.getInt(stat);
					break;
				}
				case "ignoreDAMr":{
					ignoreDAMr = MapleDataTool.getInt(stat);
					break;
				}
				case "DAMreflect":{
					DAMreflect = MapleDataTool.getInt(stat);
					break;
				}
				case "HP":{
					HP = MapleDataTool.getInt(stat);
					break;
				}
				case "MP":{
					MP = MapleDataTool.getInt(stat);
					break;
				}
				case "incSTR":{
					incSTR = MapleDataTool.getInt(stat);
					break;
				}
				case "incDEX":{
					incDEX = MapleDataTool.getInt(stat);
					break;
				}
				case "incINT":{
					incINT = MapleDataTool.getInt(stat);
					break;
				}
				case "incLUK":{
					incLUK = MapleDataTool.getInt(stat);
					break;
				}
				case "incSpeed":{
					incSpeed = MapleDataTool.getInt(stat);
					break;
				}
				case "incJump":{
					incJump = MapleDataTool.getInt(stat);
					break;
				}
				case "incPAD":{
					incPAD = MapleDataTool.getInt(stat);
					break;
				}
				case "incPDD":{
					incPDD = MapleDataTool.getInt(stat);
					break;
				}
				case "incMAD":{
					incMAD = MapleDataTool.getInt(stat);
					break;
				}
				case "incMDD":{
					incMDD = MapleDataTool.getInt(stat);
					break;
				}
				case "incEVA":{
					incEVA = MapleDataTool.getInt(stat);
					break;
				}
				case "incACC":{
					incACC = MapleDataTool.getInt(stat);
					break;
				}
				case "incMHP":{
					incMHP = MapleDataTool.getInt(stat);
					break;
				}
				case "incMMP":{
					incMMP = MapleDataTool.getInt(stat);
					break;
				}
				case "incSTRr":{
					incSTRr = MapleDataTool.getInt(stat);
					break;
				}
				case "incDEXr":{
					incDEXr = MapleDataTool.getInt(stat);
					break;
				}
				case "incINTr":{
					incINTr = MapleDataTool.getInt(stat);
					break;
				}
				case "incLUKr":{
					incLUKr = MapleDataTool.getInt(stat);
					break;
				}
				case "incPADr":{
					incPADr = MapleDataTool.getInt(stat);
					break;
				}
				case "incPDDr":{
					incPDDr = MapleDataTool.getInt(stat);
					break;
				}
				case "incMADr":{
					incMADr = MapleDataTool.getInt(stat);
					break;
				}
				case "incMDDr":{
					incMDDr = MapleDataTool.getInt(stat);
					break;
				}
				case "incEVAr":{
					incEVAr = MapleDataTool.getInt(stat);
					break;
				}
				case "incACCr":{
					incACCr = MapleDataTool.getInt(stat);
					break;
				}
				case "incCr":{
					incCr = MapleDataTool.getInt(stat);
					break;
				}
				case "incMHPr":{
					incMHPr = MapleDataTool.getInt(stat);
					break;
				}
				case "incMMPr":{
					incMMPr = MapleDataTool.getInt(stat);
					break;
				}
				case "incDAMr":{
					incDAMr = MapleDataTool.getInt(stat);
					break;
				}
				case "incAllskill":{
					incAllskill = MapleDataTool.getInt(stat);
					break;
				}
				case "incMesoProp":{
					incMesoProp = MapleDataTool.getInt(stat);
					break;
				}
				case "incRewardProp":{
					incRewardProp = MapleDataTool.getInt(stat);
					break;
				}
				case "face":{
					face = MapleDataTool.getString(stat);
					break;
				}
				case "attackType":{
					attackType = MapleDataTool.getInt(stat);
					break;
				}
				case "time":{
					time = MapleDataTool.getInt(stat);
					break;
				}
				case "prop":{
					prop = MapleDataTool.getInt(stat);
					break;
				}
				case "level":{
					level = MapleDataTool.getInt(stat);
					break;
				}
				default:{
					System.out.println("Unhandled pld: " + stat.getName() + " with value: " + stat.getData());
					break;
				}
			}
		}
	}

	public void load(LittleEndianAccessor lea){
		boss = lea.readBoolean();
		RecoveryUP = lea.readInt();
		RecoveryHP = lea.readInt();
		RecoveryMP = lea.readInt();
		mpRestore = lea.readInt();
		mpconReduce = lea.readInt();
		ignoreTargetDEF = lea.readInt();
		ignoreDAM = lea.readInt();
		ignoreDAMr = lea.readInt();
		DAMreflect = lea.readInt();
		HP = lea.readInt();
		MP = lea.readInt();
		incSTR = lea.readInt();
		incDEX = lea.readInt();
		incINT = lea.readInt();
		incLUK = lea.readInt();
		incSpeed = lea.readInt();
		incJump = lea.readInt();
		incPAD = lea.readInt();
		incPDD = lea.readInt();
		incMAD = lea.readInt();
		incMDD = lea.readInt();
		incEVA = lea.readInt();
		incACC = lea.readInt();
		incMHP = lea.readInt();
		incMMP = lea.readInt();
		incSTRr = lea.readInt();
		incDEXr = lea.readInt();
		incINTr = lea.readInt();
		incLUKr = lea.readInt();
		incPADr = lea.readInt();
		incPDDr = lea.readInt();
		incMADr = lea.readInt();
		incMDDr = lea.readInt();
		incEVAr = lea.readInt();
		incACCr = lea.readInt();
		incCr = lea.readInt();
		incMHPr = lea.readInt();
		incMMPr = lea.readInt();
		incDAMr = lea.readInt();
		incAllskill = lea.readInt();
		incMesoProp = lea.readInt();
		incRewardProp = lea.readInt();
		if(lea.readBoolean()) face = lea.readMapleAsciiString();
		attackType = lea.readInt();
		time = lea.readInt();
		prop = lea.readInt();
		level = lea.readInt();
	}

	public void save(LittleEndianWriter lew){
		lew.writeBoolean(boss);
		lew.writeInt(RecoveryUP);
		lew.writeInt(RecoveryHP);
		lew.writeInt(RecoveryMP);
		lew.writeInt(mpRestore);
		lew.writeInt(mpconReduce);
		lew.writeInt(ignoreTargetDEF);
		lew.writeInt(ignoreDAM);
		lew.writeInt(ignoreDAMr);
		lew.writeInt(DAMreflect);
		lew.writeInt(HP);
		lew.writeInt(MP);
		lew.writeInt(incSTR);
		lew.writeInt(incDEX);
		lew.writeInt(incINT);
		lew.writeInt(incLUK);
		lew.writeInt(incSpeed);
		lew.writeInt(incJump);
		lew.writeInt(incPAD);
		lew.writeInt(incPDD);
		lew.writeInt(incMAD);
		lew.writeInt(incMDD);
		lew.writeInt(incEVA);
		lew.writeInt(incACC);
		lew.writeInt(incMHP);
		lew.writeInt(incMMP);
		lew.writeInt(incSTRr);
		lew.writeInt(incDEXr);
		lew.writeInt(incINTr);
		lew.writeInt(incLUKr);
		lew.writeInt(incPADr);
		lew.writeInt(incPDDr);
		lew.writeInt(incMADr);
		lew.writeInt(incMDDr);
		lew.writeInt(incEVAr);
		lew.writeInt(incACCr);
		lew.writeInt(incCr);
		lew.writeInt(incMHPr);
		lew.writeInt(incMMPr);
		lew.writeInt(incDAMr);
		lew.writeInt(incAllskill);
		lew.writeInt(incMesoProp);
		lew.writeInt(incRewardProp);
		lew.writeBoolean(face != null);
		if(face != null) lew.writeMapleAsciiString(face);
		lew.writeInt(attackType);
		lew.writeInt(time);
		lew.writeInt(prop);
		lew.writeInt(level);
	}
}
