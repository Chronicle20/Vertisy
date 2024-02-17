package net.world;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

import net.channel.ChannelWorldInterface;
import net.server.guild.MapleGuildCharacter;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Feb 2, 2017
 */
public class WorldCenterInterfaceImpl extends UnicastRemoteObject implements WorldCenterInterface{

	private static final long serialVersionUID = -650249976780542099L;

	public WorldCenterInterfaceImpl() throws RemoteException{
		super(0, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
	}

	@Override
	public List<Integer> getChannels() throws RemoteException{
		List<Integer> channels = new ArrayList<>();
		for(Integer ch : WorldRegistryImpl.getInstance().getChannelServers().keySet()){
			channels.add(ch);
		}
		return channels;
	}

	@Override
	public String getIP(int channel) throws RemoteException{
		ChannelWorldInterface c = WorldRegistryImpl.getInstance().getChannelServers().get(channel);
		if(c != null) return c.getIP(channel);
		for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
			String ip = cwi.getIP(channel);
			if(ip != null) return ip;
		}
		return null;
	}

	@Override
	public boolean isConnected() throws RemoteException{
		return true;
	}

	@Override
	public void broadcastPacket(byte[] packet) throws RemoteException{
		for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
			cwi.broadcastPacket(packet);
		}
	}

	@Override
	public void broadcastGMPacket(byte[] packet) throws RemoteException{
		for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
			cwi.broadcastGMPacket(packet);
		}
	}

	@Override
	public void deleteGuildCharacter(MapleGuildCharacter mgc) throws RemoteException{
		WorldServer.getInstance().setGuildMemberOnline(mgc, false, (byte) -1);
		if(mgc.getGuildRank() > 1){
			WorldServer.getInstance().leaveGuild(mgc);
		}else{
			WorldServer.getInstance().disbandGuild(mgc.getGuildId());
		}
	}

	@Override
	public void updateLimitedGood(int nSN, int nRemainCount) throws RemoteException{
		for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
			cwi.updateLimitedGood(nSN, nRemainCount);
		}
	}
}
