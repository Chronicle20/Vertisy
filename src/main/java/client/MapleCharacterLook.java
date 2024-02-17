package client;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import client.inventory.Item;
import client.inventory.MapleInventoryType;
import server.ItemInformationProvider;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Feb 5, 2017
 */
public class MapleCharacterLook implements Externalizable{

	private static final long serialVersionUID = 2058609046116597762L;
	private int id;
	private int face;
	private int hair;
	private byte gender;
	private MapleSkinColor color;
	private HashMap<Short, Integer> equips = new HashMap<Short, Integer>();

	public MapleCharacterLook(){
		super();
	}

	public MapleCharacterLook(MapleCharacter mc){
		id = mc.getId();
		gender = (byte) mc.getGender();
		color = mc.getSkinColor();
		face = mc.getFace();
		hair = mc.getHair();
		ItemInformationProvider ii = ItemInformationProvider.getInstance();
		for(Item i : ii.canWearEquipment(mc, mc.getInventory(MapleInventoryType.EQUIPPED).list())){
			equips.put(i.getPosition(), i.getItemId());
		}
	}

	public int getId(){
		return id;
	}

	public byte getGender(){
		return gender;
	}

	public MapleSkinColor getSkinColor(){
		return color;
	}

	public int getFace(){
		return face;
	}

	public int getHair(){
		return hair;
	}

	public Map<Short, Integer> getEquips(){
		return equips;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException{
		out.writeInt(id);
		out.writeByte(gender);
		out.writeByte(color.getId());
		out.writeInt(face);
		out.writeInt(hair);
		out.writeInt(getEquips().size());
		for(Entry<Short, Integer> equip : equips.entrySet()){
			out.writeShort(equip.getKey());
			out.writeInt(equip.getValue());
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException{
		id = in.readInt();
		gender = in.readByte();
		color = MapleSkinColor.getById(in.readByte());
		face = in.readInt();
		hair = in.readInt();
		int equipAmount = in.readInt();
		for(int i = 0; i < equipAmount; i++){
			equips.put(in.readShort(), in.readInt());
		}
	}
}
