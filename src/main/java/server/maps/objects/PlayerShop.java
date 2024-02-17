package server.maps.objects;

import java.util.List;

import server.MaplePlayerShopItem;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jan 9, 2017
 */
public abstract class PlayerShop extends AbstractMapleMapObject{

	public abstract List<MaplePlayerShopItem> getItems();

	public abstract String getOwnerName();

	public abstract int getOwnerId();

	public abstract int getMapId();

	public abstract String getDescription();

	public abstract int getFreeSlot();

	public abstract int getChannel();
}
