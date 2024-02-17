package net.center;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Nov 12, 2016
 */
public interface CenterLoginInterface extends Remote{

	public String getIP(int world, int channel) throws RemoteException;

	public void broadcastPacket(byte[] packet) throws RemoteException;

	public void broadcastGMPacket(byte[] packet) throws RemoteException;

	public void deleteGuildCharacter(int chrid, String name, int guildRank, int guildid, int allianceRank, int gp) throws RemoteException;

	public int getChannelSize(int world) throws RemoteException;

	public void checkHWID(int accountid, String hwid) throws RemoteException;

	public boolean disconnectCharacter(int world, int chrid) throws RemoteException;
}
