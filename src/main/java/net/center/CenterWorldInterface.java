package net.center;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Feb 2, 2017
 */
public interface CenterWorldInterface extends Remote{

	public void registerChannel(int world, int channel, int connectedClients) throws RemoteException;

	public void updateConnectedClients(int world, int channel, int connectedClients) throws RemoteException;

	public void removeChannel(int world, int channel) throws RemoteException;

	public void updateLimitedGood(int nSN, int nRemainCount) throws RemoteException;
}
