package net.world;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import net.server.guild.MapleGuildCharacter;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Feb 2, 2017
 */
public interface WorldCenterInterface extends Remote{

	public boolean isConnected() throws RemoteException;

	public List<Integer> getChannels() throws RemoteException;

	public String getIP(int channel) throws RemoteException;

	public void broadcastPacket(byte[] packet) throws RemoteException;

	public void broadcastGMPacket(byte[] packet) throws RemoteException;

	public void deleteGuildCharacter(MapleGuildCharacter mgc) throws RemoteException;

	public void updateLimitedGood(int nSN, int nRemainCount) throws RemoteException;
}
