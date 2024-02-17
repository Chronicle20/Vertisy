/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.events;

/**
 * @author Julien
 */
public enum QuickInstanceType{
	DemonDoor_Marbas(1, 677000001, 9400612, null),
	DemonDoor_Valefor(2, 677000009, 9400613, null),
	DemonDoor_Amdusias(3, 677000003, 9400614, null),
	DemonDoor_Crocell(4, 677000007, 9400611, null),
	DemonDoor_Andras(5, 677000005, 9400610, null),
	Balrog_PQ(6, 0, -1, new int[]{});

	final private int mapid;
	final private int mob_id;
	final private int[] mobids;
	final private int instance_id;

	private QuickInstanceType(int _instance_id, int _mapid, int _mobid, int[] _mobids){
		mapid = _mapid;
		mob_id = _mobid;
		mobids = _mobids;
		instance_id = _instance_id;
	}

	public int getMapId(){
		return mapid;
	}

	public int getMobId(){
		return mob_id;
	}

	public int[] getMobIDs(){
		return mobids;
	}

	public int getId(){
		return instance_id;
	}

	public static QuickInstanceType getById(int id){
		for(QuickInstanceType qit : QuickInstanceType.values()){
			if(qit.getId() == id) return qit;
		}
		return null;
	}
}
/*DROPS


MARBAS GLOVES 1082257
MARBAS SHOES 1072420
MARBAS SACK 2101209

CROCELL GLOVES 1082260
CROCELL SHOES : 1072423
CROCELL SACK : 2101212

VALEFOR SACK : 2101211
GLOVES 1082258
SHOES 1072421

AMDUSIAS
2101210
1082259
1072422

ANDRAS
2101208
1082256
1072419

*/
