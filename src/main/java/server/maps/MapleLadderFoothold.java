package server.maps;

import java.awt.Point;

import tools.data.input.LittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jun 17, 2016
 */
public class MapleLadderFoothold implements Foothold{

	private int id;
	private Point startPos, endPos;
	private int type;// Rope is 0, Ladder is 1.

	public MapleLadderFoothold(){
		super();
	}

	public MapleLadderFoothold(int id, int x, int y1, int y2, int type, int layer){
		this.id = id;
		this.startPos = new Point(x, y1);
		this.endPos = new Point(x, y2);
		this.type = type;
	}

	@Override
	public int getID(){
		return id;
	}

	@Override
	public Point getStartPos(){
		return startPos;
	}

	@Override
	public Point getEndPos(){
		return endPos;
	}

	/**
	 * 0 is Rope
	 * 1 is Ladder
	 */
	@Override
	public int getType(){
		return type;
	}

	public void save(MaplePacketLittleEndianWriter mplew){
		mplew.writeInt(id);
		mplew.writePos(startPos);
		mplew.writePos(endPos);
		mplew.writeInt(type);
	}

	public void load(LittleEndianAccessor slea){
		id = slea.readInt();
		startPos = slea.readPos();
		endPos = slea.readPos();
		type = slea.readInt();
	}
}
