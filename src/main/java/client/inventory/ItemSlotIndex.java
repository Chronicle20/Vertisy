package client.inventory;

/**
 * All Items have a specific slot or position, Nexon calls these "Body Parts".
 * These are the layed out indexes of each of the set of parts.
 * Additionally, when it comes to retrieving equipped slots, the position is
 * always returned back as negative. This is checked upon GetItem/SetItem.
 * Equipped: aEquipped/aEquipped2[getPos()], Slot: aaItemSlot[getPosition()]
 * 
 * @author Eric
 */
public enum ItemSlotIndex{
    // Begin Body Part
	BP_BEGIN(0),
	Hair(0),
	Cap(1),
	FaceAcc(2),
	EyeAcc(3),
	EarAcc(4),
	Clothes(5),
	Top(5),
	Pants(6),
	Shoes(7),
	Gloves(8),
	Cape(9),
	Shield(10),
	Weapon(11),
	Ring1(12),
	Ring2(13),
	PetWear(14),
	Ring3(15),
	Ring4(16),
	Pendant(17),
	TamingMob(18),
	Saddle(19),
	MobEquip(20),
	PetRing_Label(21),
	PetAbil_Item(22),
	PetAbil_Meso(23),
	PetAbil_HPConsume(24),
	PetAbil_MPConsume(25),
	PetAbil_SweepForDrop(26),
	PetAbil_LongRange(27),
	PetAbil_PickupOthers(28),
	PetRing_Quote(29),
	PetWear2(30),
	PetRing_Label2(31),
	PetRing_Quote2(32),
	PetAbil_Item2(33),
	PetAbil_Meso2(34),
	PetAbil_SweepForDrop2(35),
	PetAbil_LongRange2(36),
	PetAbil_PickupOthers2(37),
	PetWear3(38),
	PetRing_Label3(39),
	PetRing_Quote3(40),
	PetAbil_Item3(41),
	PetAbil_Meso3(42),
	PetAbil_SweepForDrop3(43),
	PetAbil_LongRange3(44),
	PetAbil_PickupOthers3(45),
	PetAbil_IgnoreItems1(46),
	PetAbil_IgnoreItems2(47),
	PetAbil_IgnoreItems3(48),
	Medal(49),
	Belt(50),
	Shoulder(51),
    // Begin Nothing
	NOTHING_BEGIN(52),
    // No clue what 53 is kekeke
	Nothing3(54),
	Nothing2(55),
	Nothing1(56),
	Nothing0(57),
	NOHING_END(58),
    // Begin Extension
	EXT_BEGIN(59),
	EXT_Pendant1(59),
	EXT_1(60),
	EXT_2(61),
	EXT_3(62),
	EXT_4(63),
	EXT_5(64),
	EXT_6(65),
	BP_Count(59),
	EXT_END(59),
	EXT_Count(1),
	BP_END(60),
	Sticker(100),
    // Begin Dragon Part
	DP_BEGIN(1000),
	DP_Cap(1000),
	DP_Pendant(1001),
	DP_Wing(1002),
	DP_Shoes(1003),
	DP_END(1004),
	DP_Count(4),
    // Begin Mechanic Part
	MP_BEGIN(1100),
	MP_Engine(1100),
	MP_Arm(1101),
	MP_Leg(1102),
	MP_Frame(1103),
	MP_Transister(1104),
	MP_END(1105),
	MP_Count(5);

	// Android Part
	private final int pos;

	private ItemSlotIndex(int pos){
		this.pos = pos;
	}

	/**
	 * In OdinMS, positions are negative when sent.
	 * This still holds true to the GetItem function,
	 * and ONLY to be used for GetItem in CharacterData.
	 * 
	 * @return The OdinMS Position format.
	 */
	public int getPos(){
		return -pos;
	}

	/**
	 * Nexon's positions are positive when sent.
	 * When Nexon gets an index from the aEquipped[] array,
	 * they get a positive slot index from here.
	 * 
	 * @return The Nexon Position format.
	 */
	public int getPosition(){
		return pos;
	}
}