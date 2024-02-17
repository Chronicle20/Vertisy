package net.rmi;

import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Dec 18, 2016
 */
public class VertisyServerSocketFactory implements RMIServerSocketFactory{

	private byte pattern;

	public VertisyServerSocketFactory(byte pattern){
		this.pattern = pattern;
	}

	@Override
	public ServerSocket createServerSocket(int port) throws IOException{
		return new VertisyServerSocket(port, pattern);
	}

	@Override
	public int hashCode(){
		return pattern;
	}

	@Override
	public boolean equals(Object obj){
		return(getClass() == obj.getClass() && pattern == ((VertisyServerSocketFactory) obj).pattern);
	}
}
