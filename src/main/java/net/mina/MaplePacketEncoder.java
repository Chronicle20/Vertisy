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
package net.mina;

import java.util.concurrent.locks.Lock;

import client.MapleClient;
import constants.ServerConstants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import server.shark.SharkPacket;
import tools.MapleAESOFB;

public class MaplePacketEncoder extends MessageToByteEncoder<Object>{

	@Override
	protected void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception{
		final MapleClient client = (MapleClient) ctx.channel().attr(MapleClient.CLIENT_KEY).get();
		if(client != null){
			final MapleAESOFB send_crypto = client.getSendCrypto();
			final byte[] input = (byte[]) in;
			if(ServerConstants.LOG_SHARK){
				final SharkPacket sp = new SharkPacket((byte[]) in, false);
				client.sl.log(sp);
			}
			final byte[] unencrypted = new byte[input.length];
			System.arraycopy(input, 0, unencrypted, 0, input.length);
			final byte[] ret = new byte[unencrypted.length + 4];
			final byte[] header = send_crypto.getPacketHeader(unencrypted.length);
			MapleCustomEncryption.encryptData(unencrypted);
			final Lock mutex = client.getLock();
			mutex.lock();
			try{
				send_crypto.crypt(unencrypted);
				System.arraycopy(header, 0, ret, 0, 4);
				System.arraycopy(unencrypted, 0, ret, 4, unencrypted.length);
				out.writeBytes(ret);
			}finally{
				mutex.unlock();
			}
		}else{
			out.writeBytes((byte[]) in);
		}
	}
}