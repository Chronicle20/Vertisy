package server.maps.objects.miniroom;

import client.MapleCharacter;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 14, 2017
 */
public class Omok extends MiniGame{

	private final byte omokType;
	private int[][] pieces = null;

	public Omok(MapleCharacter owner, byte omokType){
		super(owner, MiniGameType.OMOK);
		this.omokType = omokType;
		pieces = new int[15][15];
	}

	public int getOmokType(){
		return omokType;
	}

	public void create(){
		getOwner().getMap().addMapObject(this);
	}

	public void place(int x, int y, int type){
		if((x < 0 || x > pieces.length) || (y < 0 || y > pieces.length)){
			// ban?
			return;
		}
	}

	@Override
	public int getMaxSlots(){
		return 2;
	}
}
