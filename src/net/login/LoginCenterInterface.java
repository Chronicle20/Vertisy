package net.login;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Nov 12, 2016
 */
public interface LoginCenterInterface extends Remote{

	public boolean isConnected() throws RemoteException;

	public void registerChannel(int world, int channel, int connectedClients) throws RemoteException;

	public void removeChannel(int world, int channel) throws RemoteException;

	public void updateConnectedClients(int world, int channel, int connectedClients) throws RemoteException;

	public void removeWorld(int world) throws RemoteException;
}
