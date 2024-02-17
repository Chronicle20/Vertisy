package net.server.channel.handlers;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Feb 15, 2016
 */
public class CRCStateHandler extends AbstractMaplePacketHandler{

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		c.getPlayer().crcStateTask();
	}
}
