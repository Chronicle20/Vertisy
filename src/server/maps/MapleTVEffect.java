/*
 * This file is part of the OdinMS Maple Story Server
 * Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 * Matthias Butz <matze@odinms.de>
 * Jan Christian Meyer <vimes@odinms.de>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation version 3 as published by
 * the Free Software Foundation. You may not use, modify or distribute
 * this program under any other version of the GNU Affero General Public
 * License.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package server.maps;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import client.MapleCharacter;
import net.channel.ChannelServer;
import server.TimerManager;
import tools.MaplePacketCreator;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/*
 * MapleTVEffect
 * @author MrXotic
 */
public class MapleTVEffect{

	private static boolean ACTIVE;
	private List<String> message = new ArrayList<>(5);
	private MapleCharacter user;
	private int type;
	private MapleCharacter partner;

	public MapleTVEffect(MapleCharacter u, MapleCharacter p, List<String> msg, int t){
		this.message = msg;
		this.user = u;
		this.type = t;
		this.partner = p;
		broadcastTV(true);
	}

	public static boolean isActive(){
		return ACTIVE;
	}

	private void broadcastTV(boolean activity){
		try{
			ACTIVE = activity;
			if(ACTIVE){
				ChannelServer.getInstance().getWorldInterface().broadcastPacket(MaplePacketCreator.enableTV());
				ChannelServer.getInstance().getWorldInterface().broadcastPacket(MaplePacketCreator.sendTV(user, message, type <= 2 ? type : type - 3, partner));
				int delay = 15000;
				if(type == 4){
					delay = 30000;
				}else if(type == 5){
					delay = 60000;
				}
				TimerManager.getInstance().schedule("broadcastTV", new Runnable(){

					@Override
					public void run(){
						broadcastTV(false);
					}
				}, delay);
			}else{
				ChannelServer.getInstance().getWorldInterface().broadcastPacket(MaplePacketCreator.removeTV());
			}
		}catch(RemoteException | NullPointerException ex){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
		}
	}
}
