package net.center;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

import net.login.LoginCenterInterface;
import net.world.WorldCenterInterface;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Nov 12, 2016
 */
public class CenterRegistryImpl extends UnicastRemoteObject implements CenterRegistry{

	private static final long serialVersionUID = 2672175089194415311L;
	private static CenterRegistryImpl instance = null;
	private Map<Integer, LoginCenterInterface> loginServers = new HashMap<>();
	private Map<Integer, WorldCenterInterface> worldServers = new HashMap<>();

	private CenterRegistryImpl() throws RemoteException{
		super(0, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
	}

	public static CenterRegistryImpl getInstance(){
		if(instance == null){
			try{
				instance = new CenterRegistryImpl();
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
	public CenterLoginInterface registerLoginServer(int loginServerID, LoginCenterInterface lci) throws RemoteException{
		System.out.println("Registered login server: " + loginServerID);
		loginServers.put(loginServerID, lci);
		CenterServer.getInstance().updateServerStatus("Login", loginServerID, true);
		for(int world : worldServers.keySet()){
			Map<Integer, Integer> load = CenterServer.getInstance().getChanneList().get(world);
			if(load == null) continue;
			for(int channel : load.keySet()){
				if(load != null && load.get(channel) != null){
					lci.registerChannel(world, channel, load.get(channel));
				}else{
					lci.registerChannel(world, channel, 0);
				}
			}
		}
		return new CenterLoginInterfaceImpl();
	}

	@Override
	public void removeLoginServer(int loginServerID) throws RemoteException{
		loginServers.remove(loginServerID);
		CenterServer.getInstance().updateServerStatus("Login", loginServerID, false);
	}

	@Override
	public CenterWorldInterface registerWorldServer(int worldServerID, WorldCenterInterface lci) throws RemoteException{
		System.out.println("Registered world server: " + worldServerID);
		worldServers.put(worldServerID, lci);
		CenterServer.getInstance().updateServerStatus("World", worldServerID, true);
		return new CenterWorldInterfaceImpl();
	}

	@Override
	public void removeWorldServer(int worldServerID) throws RemoteException{
		worldServers.remove(worldServerID);
		CenterServer.getInstance().updateServerStatus("World", worldServerID, false);
	}

	@Override
	public Map<Integer, WorldCenterInterface> getWorldServers() throws RemoteException{
		return worldServers;
	}

	@Override
	public Map<Integer, LoginCenterInterface> getLoginServers() throws RemoteException{
		return loginServers;
	}
}
