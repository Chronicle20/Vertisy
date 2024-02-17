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

import java.util.List;

import client.MapleClient;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.AttributeKey;
import tools.MapleAESOFB;

public class MaplePacketDecoder extends ByteToMessageDecoder{

	private static final AttributeKey<DecoderState> DECODER_STATE_KEY = AttributeKey.valueOf(MaplePacketDecoder.class.getName() + ".STATE");

	private static class DecoderState{

		public int packetlength = -1;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception{
		if(!ctx.channel().hasAttr(MapleClient.CLIENT_KEY)) return;
		final MapleClient client = (MapleClient) ctx.channel().attr(MapleClient.CLIENT_KEY).get();
		if(client == null) return;
		DecoderState decoderState = (DecoderState) ctx.channel().attr(DECODER_STATE_KEY).get();
		if(decoderState == null){
			decoderState = new DecoderState();
			ctx.channel().attr(DECODER_STATE_KEY).set(decoderState);
		}
		if(in.readableBytes() >= 4 && decoderState.packetlength == -1){
			int packetHeader = in.readInt();
			if(!client.getReceiveCrypto().checkPacket(packetHeader)){
				ctx.channel().close();
				return;
			}
			decoderState.packetlength = MapleAESOFB.getPacketLength(packetHeader);
		}else if(in.readableBytes() < 4 && decoderState.packetlength == -1) return;
		if(in.readableBytes() >= decoderState.packetlength){
			byte decryptedPacket[] = new byte[decoderState.packetlength];
			in.readBytes(decryptedPacket, 0, decoderState.packetlength);
			decoderState.packetlength = -1;
			client.getReceiveCrypto().crypt(decryptedPacket);
			MapleCustomEncryption.decryptData(decryptedPacket);
			out.add(decryptedPacket);
		}
		return;
	}
}
