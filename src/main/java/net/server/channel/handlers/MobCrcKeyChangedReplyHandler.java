package net.server.channel.handlers;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jun 20, 2017
 */
public final class MobCrcKeyChangedReplyHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		// the other crc things cause this
	}
}
