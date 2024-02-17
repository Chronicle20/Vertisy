package constants;

import server.ItemData;

/**
 * @author The Spookster
 */
public enum EquipSlot{
	HAT("Cp", -1),
	SPECIAL_HAT("HrCp", -1),
	FACE_ACCESSORY("Af", -2),
	EYE_ACCESSORY("Ay", -3),
	EARRINGS("Ae", -4),
	TOP("Ma", -5),
	OVERALL("MaPn", -5),
	PANTS("Pn", -6),
	SHOES("So", -7),
	GLOVES("GlGw", -8),
	CASH_GLOVES("Gv", -8),
	CAPE("Sr", -9),
	SHIELD("Si", -10),
	WEAPON("Wp", -11),
	WEAPON_2("WpSi", -11),
	LOW_WEAPON("WpSp", -11),
	RING("Ri", -12, -13, -15, -16),
	PENDANT("Pe", -17),
	TAMED_MOB("Tm", -18, -1000, -1001, -1002, -1003),
	SADDLE("Sd", -19),
	MEDAL("Me", -49),
	BELT("Be", -50),
	DP_BEGIN("", -1000),
	DP_END("", -1004),
    // DP_CAP("Tm", 1000),
    // DP_Pendant("Tm", 1001),
    // DP_Wing("Tm", 1002),
    // DP_Shoes("Tm", 1003),
	PET_EQUIP;
	// Item Ignore Pendant: -46, -47, -48

	private String name;
	private int[] allowed;

	private EquipSlot(){}

	private EquipSlot(String wz, int... in){
		name = wz;
		allowed = in;
	}

	public String getName(){
		return name;
	}

	public int[] getSlots(){
		return allowed;
	}

	public boolean isAllowed(int slot, ItemData data){
		if(slot < 0){
			if(allowed != null){
				for(Integer allow : allowed){
					int condition = data.isCash ? allow - 100 : allow;
					if(slot == condition) return true;
				}
				return false;
			}
		}
		return data.isCash && slot < 0;
	}

	public static EquipSlot getFromTextSlot(String slot){
		if(!slot.isEmpty()){
			for(EquipSlot c : values()){
				if(c.getName() != null){
					if(c.getName().equals(slot)) return c;
				}
			}
		}
		return PET_EQUIP;
	}
}
