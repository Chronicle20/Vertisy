package net.center;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

import net.login.LoginCenterInterface;
import net.world.WorldCenterInterface;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Feb 2, 2017
 */
public class CenterWorldInterfaceImpl extends UnicastRemoteObject implements CenterWorldInterface{

	private static final long serialVersionUID = -1111957702902012620L;

	protected CenterWorldInterfaceImpl() throws RemoteException{
		super(0, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
	}

	@Override
	public void registerChannel(int world, int channel, int connectedClients) throws RemoteException{
		CenterServer.getInstance().addChannel(world, channel, connectedClients);// do we need this on center?
		for(LoginCenterInterface lci : CenterRegistryImpl.getInstance().getLoginServers().values()){
			lci.registerChannel(world, channel, connectedClients);
		}
	}

	@Override
	public void updateConnectedClients(int world, int channel, int connectedClients) throws RemoteException{
		CenterServer.getInstance().addChannel(world, channel, connectedClients);// do we need this on center?
		for(LoginCenterInterface lci : CenterRegistryImpl.getInstance().getLoginServers().values()){
			lci.updateConnectedClients(world, channel, connectedClients);
		}
	}

	@Override
	public void removeChannel(int world, int channel) throws RemoteException{
		CenterServer.getInstance().removeChannel(world, channel);
		for(LoginCenterInterface lci : CenterRegistryImpl.getInstance().getLoginServers().values()){
			lci.removeChannel(world, channel);
		}
	}

	@Override
	public void updateLimitedGood(int nSN, int nRemainCount) throws RemoteException{
		for(WorldCenterInterface wci : CenterRegistryImpl.getInstance().getWorldServers().values()){
			wci.updateLimitedGood(nSN, nRemainCount);
		}
	}
}
