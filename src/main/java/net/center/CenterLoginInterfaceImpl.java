package net.center;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

import net.server.guild.MapleGuildCharacter;
import net.world.WorldCenterInterface;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Nov 12, 2016
 */
public class CenterLoginInterfaceImpl extends UnicastRemoteObject implements CenterLoginInterface{

	private static final long serialVersionUID = -6248981033355214813L;

	public CenterLoginInterfaceImpl() throws RemoteException{
		super(0, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
	}

	@Override
	public String getIP(int world, int channel) throws RemoteException{
		return CenterRegistryImpl.getInstance().getWorldServers().get(world).getIP(channel);
	}

	@Override
	public void broadcastPacket(byte[] packet) throws RemoteException{
		for(WorldCenterInterface wci : CenterRegistryImpl.getInstance().getWorldServers().values()){
			wci.broadcastPacket(packet);
		}
	}

	@Override
	public void broadcastGMPacket(byte[] packet) throws RemoteException{
		for(WorldCenterInterface wci : CenterRegistryImpl.getInstance().getWorldServers().values()){
			wci.broadcastGMPacket(packet);
		}
	}

	// should be handled on worldserver
	@Override
	public void deleteGuildCharacter(int chrid, String name, int guildRank, int guildid, int allianceRank, int gp) throws RemoteException{
		for(WorldCenterInterface wci : CenterRegistryImpl.getInstance().getWorldServers().values()){
			wci.deleteGuildCharacter(new MapleGuildCharacter(chrid, 0, name, (byte) -1, (byte) -1, 0, guildRank, guildid, false, allianceRank, gp));
		}
	}

	@Override
	public int getChannelSize(int world) throws RemoteException{
		if(CenterRegistryImpl.getInstance().getWorldServers().isEmpty()) return 0;
		WorldCenterInterface wci = CenterRegistryImpl.getInstance().getWorldServers().get(world);
		if(wci == null) return 0;
		return wci.getChannels().size();
	}

	@Override
	public void checkHWID(int accountid, String hwid) throws RemoteException{
		// Server.getInstance().getWorlds().forEach(world-> world.checkHWID(accountid, hwid));
	}

	@Override
	public boolean disconnectCharacter(int world, int chrid) throws RemoteException{
		/*MapleCharacter player = Server.getInstance().getWorld(world).getCharacterById(chrid);
		if(player != null){
			player.getClient().disconnect(false, false);
			return true;
		}*/
		return false;
	}
}
