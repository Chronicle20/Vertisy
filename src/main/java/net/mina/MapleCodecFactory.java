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

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import net.MapleServerHandler;

public class MapleCodecFactory extends ChannelInitializer<SocketChannel>{

	private final int world, channel;

	public MapleCodecFactory(int world, int channel){
		this.world = world;
		this.channel = channel;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception{
		ch.pipeline().addLast("decoder", new MaplePacketDecoder());
		ch.pipeline().addLast("encoder", new MaplePacketEncoder());
		ch.pipeline().addLast("idleStateHandler", new IdleStateHandler(30, 30, 0));
		ch.pipeline().addLast("handler", new MapleServerHandler(world, channel));
	}
}
