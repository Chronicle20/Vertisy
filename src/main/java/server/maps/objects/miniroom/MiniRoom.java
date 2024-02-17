package server.maps.objects.miniroom;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import client.MapleCharacter;
import client.MapleClient;
import server.maps.objects.AbstractMapleMapObject;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 14, 2017
 */
public abstract class MiniRoom extends AbstractMapleMapObject{// CMiniRoomBaseDlg::OnPacketBase

	private WeakReference<MapleCharacter> owner;
	private List<WeakReference<MapleCharacter>> visitors;
	private String title, password;
	public int miniRoomType;

	public MiniRoom(MapleCharacter owner, int miniRoomType){
		if(owner != null) this.owner = new WeakReference<MapleCharacter>(owner);
		this.miniRoomType = miniRoomType;
		visitors = new ArrayList<>();
	}

	public MapleCharacter getOwner(){
		return owner.get();
	}

	public boolean isOwner(MapleCharacter chr){
		if(owner == null || owner.get() == null) return false;
		return owner.get().getId() == chr.getId();
	}

	public int getMiniRoomType(){
		return miniRoomType;
	}

	public String getTitle(){
		return title;
	}

	public void setTitle(String title){
		this.title = title;
	}

	public String getPassword(){
		return password;
	}

	public void setPassword(String password){
		this.password = password;
	}

	public abstract int getGameType();

	public abstract int getMaxSlots();

	public abstract boolean hasFreeSlot();

	public int getCurrentUsers(){
		int cur = 0;
		if(owner != null && owner.get() != null) cur++;
		for(WeakReference<MapleCharacter> chr : visitors){
			if(chr != null && chr.get() != null) cur++;
		}
		return cur;
	}

	@Override
	public void sendSpawnData(MapleClient client){}

	@Override
	public void sendDestroyData(MapleClient client){}

	@Override
	public AbstractMapleMapObject clone(){
		return null;
	}
}
