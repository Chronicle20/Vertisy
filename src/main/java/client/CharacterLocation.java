package client;

import java.awt.Point;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.UUID;

import scripting.event.EventInstanceManager;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Feb 4, 2017
 */
public class CharacterLocation implements Externalizable{

	private static final long serialVersionUID = -4198297306579588236L;
	public String charName;
	public int chrid;
	public int mapid;
	public byte channel;
	public byte gmLevel;
	public String eventManager, eventInstance;
	public Point position;
	public UUID instanceMap;
	public byte buddylistCapacity;
	public boolean cashshop, mts;

	public CharacterLocation(){
		super();
	}

	public CharacterLocation(MapleCharacter player){
		charName = player.getName();
		chrid = player.getId();
		mapid = player.getMapId();
		channel = (byte) player.getClient().getChannel();
		gmLevel = (byte) player.getGMLevel();
		EventInstanceManager eim = player.getEventInstance();
		if(eim != null){
			eventManager = eim.getEm().getName();
			eventInstance = eim.getName();
		}
		position = player.getPosition();
		instanceMap = player.getMap().getInstanceID();
		buddylistCapacity = (byte) player.getBuddylist().getCapacity();
		cashshop = player.getCashShop().getOpenType() == 1;
		mts = player.getCashShop().getOpenType() == 2;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException{
		out.writeObject(charName);
		out.writeInt(chrid);
		out.writeInt(mapid);
		out.writeByte(channel);
		out.writeByte(gmLevel);
		out.writeObject(eventManager);
		out.writeObject(eventInstance);
		out.writeInt(position.x);
		out.writeInt(position.y);
		out.writeObject(instanceMap != null ? instanceMap.toString() : null);
		out.writeByte(buddylistCapacity);
		out.writeBoolean(cashshop);
		out.writeBoolean(mts);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException{
		charName = (String) in.readObject();
		chrid = in.readInt();
		mapid = in.readInt();
		channel = in.readByte();
		gmLevel = in.readByte();
		eventManager = (String) in.readObject();
		eventInstance = (String) in.readObject();
		position = new Point(in.readInt(), in.readInt());
		Object uuidO = in.readObject();
		if(uuidO == null) instanceMap = null;
		else instanceMap = UUID.fromString((String) uuidO);
		buddylistCapacity = in.readByte();
		cashshop = in.readBoolean();
		mts = in.readBoolean();
	}
}
