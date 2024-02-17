package net.server.handlers.login;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.HexTool;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 27, 2017
 */
public class PacketErrorHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		short type = slea.readShort();
		/*String type_str = "Unknown?!";
		if(type == 0x01){
			type_str = "SendBackupPacket";
		}else if(type == 0x02){
			type_str = "Crash Report";
		}else if(type == 0x03){
			type_str = "Exception";
		}*/
		int errortype = slea.readInt(); // example error 38
		if(errortype == 0){ // i don't wanna log error code 0 stuffs, (usually some bounceback to login)
			// return;
		}
		short data_length = slea.readShort();
		slea.skip(4); // ?B3 86 01 00 00 00 FF 00 00 00 00 00 9E 05 C8 FF 02 00 CD 05 C9 FF 7D 00 00 00 3F 00 00 00 00 00 02 77 01 00 25 06 C9 FF 7D 00 00 00 40 00 00 00 00 00 02 C1 02
		short opcodeheader = slea.readShort();
		String data = "Error Type: " + errortype + "\r\n" + "Data Length: " + data_length + "\r\n" + "Character: " + (c.isPlayerNull() ? "" : c.getPlayer().getName()) + " Map: " + (c.isPlayerNull() ? "" : c.getPlayer().getMap().getId()) + " - Account: " + c.getAccountName() + "\r\n" + " Opcode: " + opcodeheader + "\r\n" + HexTool.toString(slea.read((int) slea.available())) + "\r\n";
		System.out.println(data);
		Logger.log(LogType.INFO, LogFile.PACKET_ERROR, data);
	}

	@Override
	public boolean validateState(MapleClient c){
		return true;
	}
}
