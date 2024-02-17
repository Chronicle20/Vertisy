package net.world;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

import constants.WorldConstants.WorldInfo;
import net.channel.ChannelWorldInterface;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Feb 2, 2017
 */
public class WorldRegistryImpl extends UnicastRemoteObject implements WorldRegistry{

	private static final long serialVersionUID = -3695887368306851001L;
	private static WorldRegistryImpl instance = null;
	private Map<Integer, ChannelWorldInterface> channelServer = new HashMap<>();

	protected WorldRegistryImpl() throws RemoteException{
		super(0, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
	}

	public static WorldRegistryImpl getInstance(){
		if(instance == null){
			try{
				instance = new WorldRegistryImpl();
			}catch(RemoteException | NullPointerException e){
				throw new RuntimeException(e);
			}
		}
		return instance;
	}

	@Override
	public boolean isConnected() throws RemoteException{
		return true;
	}

	@Override
	public WorldChannelInterface registerChannelServer(int channelServerID, ChannelWorldInterface cwi) throws RemoteException{
		System.out.println("Registered channel server: " + channelServerID);
		channelServer.put(channelServerID, cwi);
		for(int otherChannels : cwi.getAllChannels()){
			if(otherChannels != channelServerID && WorldServer.getInstance().getCenterInterface() != null){
				WorldServer.getInstance().getCenterInterface().registerChannel(WorldServer.getInstance().getID(), otherChannels, cwi.getChannelLoad(otherChannels));
			}
		}
		if(WorldServer.getInstance().getCenterInterface() != null){
			WorldServer.getInstance().getCenterInterface().registerChannel(WorldServer.getInstance().getID(), channelServerID, cwi.getChannelLoad(channelServerID));
		}
		WorldInfo info = WorldInfo.values()[WorldServer.getInstance().getID()];
		cwi.setExpRate(info.getExpRate(), info.getQuestExpRate(), info.getDropRate(), info.getMesoRate());
		cwi.setTopGuilds(WorldServer.getInstance().getTopGuilds());
		return new WorldChannelInterfaceImpl();
	}

	@Override
	public void removeChannelServer(int channelServerID, int[] channels) throws RemoteException{
		channelServer.remove(channelServerID);
		if(WorldServer.getInstance().getCenterInterface() != null){
			for(int ch : channels){
				WorldServer.getInstance().getCenterInterface().removeChannel(WorldServer.getInstance().getID(), ch);
			}
		}
	}

	@Override
	public Map<Integer, ChannelWorldInterface> getChannelServers() throws RemoteException{
		return channelServer;
	}
}
