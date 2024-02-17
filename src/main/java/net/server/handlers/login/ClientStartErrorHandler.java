/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.server.handlers.login;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @author Tyler
 */
public class ClientStartErrorHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		Logger.log(LogType.ERROR, LogFile.EXCEPTION, slea.toString());
	}
}
