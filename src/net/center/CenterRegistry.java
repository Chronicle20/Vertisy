package net.center;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

import net.login.LoginCenterInterface;
import net.world.WorldCenterInterface;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Nov 3, 2016
 */
public interface CenterRegistry extends Remote{

	public boolean isConnected() throws RemoteException;

	public CenterLoginInterface registerLoginServer(int loginServerID, LoginCenterInterface lci) throws RemoteException;

	public void removeLoginServer(int loginServerID) throws RemoteException;

	public CenterWorldInterface registerWorldServer(int worldServerID, WorldCenterInterface lci) throws RemoteException;

	public void removeWorldServer(int worldServerID) throws RemoteException;

	public Map<Integer, WorldCenterInterface> getWorldServers() throws RemoteException;

	public Map<Integer, LoginCenterInterface> getLoginServers() throws RemoteException;
}
