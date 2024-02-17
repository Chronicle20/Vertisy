package net.login;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

import constants.ServerConstants;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;
import net.center.CenterLoginInterface;
import net.center.CenterRegistry;
import net.mina.MapleCodecFactory;
import net.rmi.VertisyClientSocketFactory;
import net.server.handlers.login.CreateCharHandler;
import server.TimerManager;
import tools.AutoJCE;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Nov 12, 2016
 */
public class LoginServer{

	private Channel channelConnection;
	private static int loginServerID = -1;
	private static LoginServer instance;
	private CenterRegistry centerRegistry;
	private LoginCenterInterface lci;
	private CenterLoginInterface cli;
	private VertisyClientSocketFactory socketFactory;
	private Map<Integer, Map<Integer, Integer>> channelList = new HashMap<>();
	private EventLoopGroup parentGroup, childGroup;
	private ServerBootstrap boot;
	private final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

	private LoginServer(){
		long start = System.currentTimeMillis();
		if(loginServerID == -1){
			System.out.println("Login Server ID not set. Shutting down.");
			return;
		}
		System.setProperty("wzpath", "wz");
		System.setProperty("java.rmi.server.hostname", ServerConstants.HOST);
		TimerManager tMan = TimerManager.getInstance();
		tMan.start();
		Logger.start();
		socketFactory = new VertisyClientSocketFactory((byte) 0xAF);
		tMan.register("LoginServer-RegistryConnection", ()-> {
			Registry registry;
			try{
				if(centerRegistry != null){
					centerRegistry.isConnected();
				}else{
					System.out.println("Connecting to center server: " + ServerConstants.CENTER_SERVER_HOST + ":" + ServerConstants.CENTER_SERVER_PORT);
					registry = LocateRegistry.getRegistry(ServerConstants.CENTER_SERVER_HOST, ServerConstants.CENTER_SERVER_PORT, socketFactory);
					centerRegistry = (CenterRegistry) registry.lookup("CenterRegistry");
					lci = new LoginCenterInterfaceImpl();
					cli = centerRegistry.registerLoginServer(loginServerID, lci);
					System.out.println("Connected to Center Server.");
				}
			}catch(Exception ex){
				// ex.printStackTrace();
				System.out.println("[LoginServer] Failed to reconnect to the Center Server. Trying again in 10 seconds");
				centerRegistry = null;
				lci = null;
				cli = null;
				registry = null;
			}
		}, 10000, 0);
		parentGroup = new NioEventLoopGroup(1);
		childGroup = new NioEventLoopGroup();
		boot = new ServerBootstrap().group(parentGroup, childGroup).channel(NioServerSocketChannel.class).childOption(ChannelOption.TCP_NODELAY, true).childOption(ChannelOption.SO_KEEPALIVE, true).childHandler(new MapleCodecFactory(-1, -1));
		CreateCharHandler.loadCreateCharItems();
		try{
			System.out.println("Binding Login Server to port: " + 8484);
			channelConnection = boot.bind(8484).sync().channel().closeFuture().channel();
			System.out.println("LoginServer " + loginServerID + " listening on port 8484. Took " + ((System.currentTimeMillis() - start) / 1000.0) + " seconds.");
			AutoJCE.removeCryptographyRestrictions();
		}catch(Exception ex){
			ex.printStackTrace();
			shutdown();
			return;
		}
	}

	public Map<Integer, Integer> getLoad(int world){
		return channelList.get(world);
	}

	public void addChannel(int world, int channel, int connectedClients){
		Map<Integer, Integer> channels = channelList.get(world);
		if(channels == null) channels = new HashMap<>();
		channels.put(channel, connectedClients);
		channelList.put(world, channels);
	}

	public void removeChannel(int world, int channel){
		Map<Integer, Integer> channels = channelList.get(world);
		if(channels == null) channels = new HashMap<>();
		channels.remove(channel);
		channelList.put(world, channels);
	}

	public void removeWorld(int world){
		channelList.remove(world);
	}

	public CenterRegistry getCenterRegistry(){
		return centerRegistry;
	}

	public CenterLoginInterface getCenterInterface(){// runs code on center, gives response to login.
		return cli;
	}

	public static LoginServer getInstance(){
		return instance;
	}

	public void shutdown(){
		System.out.println("Shutting down Login Server");
		try{
			if(centerRegistry != null) centerRegistry.removeLoginServer(loginServerID);
		}catch(RemoteException | NullPointerException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
		if(channelConnection != null) channelConnection.close();
		childGroup.shutdownGracefully();
		parentGroup.shutdownGracefully();
		centerRegistry = null;
		lci = null;
		cli = null;
		TimerManager.getInstance().stop();
		System.exit(0);
	}

	public static void main(String[] args){
		loginServerID = Integer.parseInt(args[0]);
		instance = new LoginServer();
	}

	public void addClientChannel(Channel channel){
		channels.add(channel);
	}

	public void removeClientChannel(Channel channel){
		channels.remove(channel);
		System.out.println("Removed a client channel, now have: " + channels.size());
	}

	public ChannelGroup getChannelGroup(){
		return channels;
	}
}
