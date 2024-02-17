package net.server.channel.handlers;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CWvsContext.Friend.AccountMoreInfo;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Aug 4, 2017
 */
public final class AccountMoreInfoHandler extends AbstractMaplePacketHandler{

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		byte type = slea.readByte();
		switch (type){
			case AccountMoreInfo.First:
				System.out.println("First: " + slea.toString());
				break;
			case AccountMoreInfo.LoadRequest:
				// Need to send data to update shit
				break;
			case AccountMoreInfo.LoadResult:
				System.out.println("LoadResult: " + slea.toString());
				break;
			case AccountMoreInfo.SaveRequest:
				System.out.println("SaveRequest: " + slea.toString());
				break;
			case AccountMoreInfo.SaveResult:
				System.out.println("SaveResult: " + slea.toString());
				break;
		}
	}
}
