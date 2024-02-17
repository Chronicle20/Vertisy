package net.server.channel.handlers;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.field.AdminShop;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Aug 24, 2017
 */
public class AdminShopRequestHandler extends AbstractMaplePacketHandler{

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		//
		// if no commodities are sent, AdminShop.Request.Close is sent.
		byte mode = slea.readByte();
		switch (mode){
			case AdminShop.Request.Trade:{
				slea.readInt();// nSN
				slea.readInt();// quantity
				break;
			}
			case AdminShop.Request.Close:{
				//
				break;
			}
		}
	}
}
