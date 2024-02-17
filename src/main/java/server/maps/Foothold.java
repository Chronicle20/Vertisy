package server.maps;

import java.awt.Point;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jun 17, 2016
 */
public interface Foothold{

	public int getID();

	public Point getStartPos();

	public Point getEndPos();

	public int getType();
}
