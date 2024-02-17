package net.world;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

import net.channel.ChannelWorldInterface;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Feb 2, 2017
 */
public interface WorldRegistry extends Remote{

	public boolean isConnected() throws RemoteException;

	public WorldChannelInterface registerChannelServer(int channelServerID, ChannelWorldInterface cwi) throws RemoteException;

	public void removeChannelServer(int channelServerID, int[] channels) throws RemoteException;

	public Map<Integer, ChannelWorldInterface> getChannelServers() throws RemoteException;
}
