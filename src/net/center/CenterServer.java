package net.center;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import constants.ServerConstants;
import net.login.LoginCenterInterface;
import net.rmi.VertisyClientSocketFactory;
import net.rmi.VertisyServerSocketFactory;
import net.world.WorldCenterInterface;
import server.TimerManager;
import tools.DatabaseConnection;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Nov 3, 2016
 */
public class CenterServer{

	private static CenterServer instance = new CenterServer();
	private Map<Integer, Map<Integer, Integer>> channelList = new HashMap<>();
	private ScheduledFuture<?> serverCheck = null;
	private boolean checkingServers = false;

	public CenterServer(){
		long start = System.currentTimeMillis();
		System.setProperty("java.rmi.server.hostname", ServerConstants.HOST);
		TimerManager tMan = TimerManager.getInstance();
		tMan.start();
		Logger.start();
		DatabaseConnection.getConnection();
		try{
			System.out.println("Binding to port: " + ServerConstants.CENTER_SERVER_PORT);
			Registry registry = LocateRegistry.createRegistry(ServerConstants.CENTER_SERVER_PORT, new VertisyClientSocketFactory((byte) 0xAF), new VertisyServerSocketFactory((byte) 0xAF));
			registry.rebind("CenterRegistry", CenterRegistryImpl.getInstance());
			System.out.println("CenterRegistry binded.");
			serverCheck = TimerManager.getInstance().register("WorldServerCheck", ()-> {
				if(checkingServers) return;
				checkingServers = true;
				try{
					for(int login : CenterRegistryImpl.getInstance().getLoginServers().keySet()){
						LoginCenterInterface lci = CenterRegistryImpl.getInstance().getLoginServers().get(login);
						try{
							lci.isConnected();
						}catch(Exception ex){
							CenterRegistryImpl.getInstance().getLoginServers().remove(login);
							updateServerStatus("Login", login, false);
							System.out.println("Lost connection to login server: " + login);
						}
					}
					for(int world : CenterRegistryImpl.getInstance().getWorldServers().keySet()){
						WorldCenterInterface wci = CenterRegistryImpl.getInstance().getWorldServers().get(world);
						try{
							wci.isConnected();
						}catch(Exception ex){
							channelList.remove(world);
							CenterRegistryImpl.getInstance().getWorldServers().remove(world);
							updateServerStatus("World", world, false);
							System.out.println("Lost connection to world: " + world);
							try{
								for(LoginCenterInterface lci : CenterRegistryImpl.getInstance().getLoginServers().values()){
									lci.removeWorld(world);
								}
							}catch(Exception exx){
								Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, exx);
							}
						}
					}
				}catch(Exception ex){
					Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
				}
				checkingServers = false;
			}, 30000);
		}catch(Exception ex){
			System.out.println("Could not initialize RMI system");
			ex.printStackTrace();
			shutdown();
			return;
		}
		System.out.println("Center server loaded in " + ((System.currentTimeMillis() - start) / 1000.0) + " seconds.");
		updateServerStatus("Center", 0, true);
	}

	public Map<Integer, Map<Integer, Integer>> getChanneList(){
		return channelList;
	}

	public void addChannel(int world, int channel, int connectedClients){
		Map<Integer, Integer> channels = channelList.get(world);
		if(channels == null) channels = new HashMap<>();
		channels.put(channel, connectedClients);
		channelList.put(world, channels);
		updateServerStatus("Channel", channel, true);
	}

	public void removeChannel(int world, int channel){
		Map<Integer, Integer> channels = channelList.get(world);
		if(channels == null) channels = new HashMap<>();
		channels.remove(channel);
		channelList.put(world, channels);
		updateServerStatus("Channel", channel, false);
	}

	public static CenterServer getInstance(){
		return instance;
	}

	public void updateServerStatus(String serverType, int id, boolean status){
		try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO serverStatus(server, status) VALUES(?, ?) ON DUPLICATE KEY UPDATE server = VALUES(server), status = VALUES(status)")){
			ps.setString(1, serverType + " " + (id + 1));
			ps.setBoolean(2, status);
			ps.executeUpdate();
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
	}

	public void shutdown(){
		if(serverCheck != null) serverCheck.cancel(true);
		updateServerStatus("Center", 0, false);
		TimerManager.getInstance().stop();
		System.exit(0);
	}

	public static void main(String[] args){
		//
	}
}
