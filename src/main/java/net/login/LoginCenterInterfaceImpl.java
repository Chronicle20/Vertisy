package net.login;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

import client.MapleClient;
import io.netty.channel.Channel;
import net.server.handlers.login.ServerlistRequestHandler;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Nov 12, 2016
 */
public class LoginCenterInterfaceImpl extends UnicastRemoteObject implements LoginCenterInterface{

	private static final long serialVersionUID = -8597688077954427912L;

	public LoginCenterInterfaceImpl() throws RemoteException{
		super(0, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
	}

	@Override
	public void registerChannel(int world, int channel, int connectedClients) throws RemoteException{
		LoginServer.getInstance().addChannel(world, channel, connectedClients);
		for(Channel ch : LoginServer.getInstance().getChannelGroup()){
			MapleClient client = ch.attr(MapleClient.CLIENT_KEY).get();
			if(client != null){
				if(client.canSendServerList()){
					ServerlistRequestHandler.sendInfo(client);
				}
			}
		}
		/*for(IoSession session : LoginServer.getInstance().getManagedSessions().values()){
			MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
			if(client != null){
				if(client.canSendServerList()){
					ServerlistRequestHandler.sendInfo(client);
				}
			}
		}*/
	}

	@Override
	public void removeChannel(int world, int channel) throws RemoteException{
		LoginServer.getInstance().removeChannel(world, channel);
		/*for(IoSession session : LoginServer.getInstance().getManagedSessions().values()){
			MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
			if(client != null){
				if(client.canSendServerList()){
					ServerlistRequestHandler.sendInfo(client);
				}
			}
		}*/
	}

	@Override
	public void updateConnectedClients(int world, int channel, int connectedClients) throws RemoteException{
		LoginServer.getInstance().addChannel(world, channel, connectedClients);
		/*for(IoSession session : LoginServer.getInstance().getManagedSessions().values()){
			MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
			if(client != null){
				if(client.canSendServerList()){
					ServerlistRequestHandler.sendInfo(client);
				}
			}
		}*/
	}

	@Override
	public boolean isConnected() throws RemoteException{
		return true;
	}

	@Override
	public void removeWorld(int world) throws RemoteException{
		LoginServer.getInstance().removeWorld(world);
		/*for(IoSession session : LoginServer.getInstance().getManagedSessions().values()){
			MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
			if(client != null){
				if(client.canSendServerList()){
					ServerlistRequestHandler.sendInfo(client);
				}
			}
		}*/
	}
}
