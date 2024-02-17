package server.movement;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

import tools.data.input.LittleEndianAccessor;
import tools.data.output.LittleEndianWriter;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jun 22, 2017
 */
public class MovePath{

	public List<Elem> lElem = new LinkedList<Elem>();
	public Point startPosition, velocity;

	public void decode(LittleEndianAccessor lea){
		startPosition = lea.readPos();
		velocity = lea.readPos();
		byte size = lea.readByte();
		for(int i = 0; i < size; i++){
			Elem elem = new Elem();
			elem.decode(lea);
			lElem.add(elem);
		}
	}

	public void encode(LittleEndianWriter lew){
		lew.writePos(startPosition);
		lew.writePos(velocity);
		lew.write(lElem.size());
		for(Elem elem : lElem){
			elem.encode(lew);
		}
	}
}
