package net.rmi;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Dec 18, 2016
 */
public class VertisyClientSocketFactory implements RMIClientSocketFactory, Serializable{

	private static final long serialVersionUID = 5333237577492235965L;
	private byte pattern;

	public VertisyClientSocketFactory(byte pattern){
		this.pattern = pattern;
	}

	@Override
	public Socket createSocket(String host, int port) throws IOException{
		return new VertisySocket(host, port, pattern);
	}

	@Override
	public int hashCode(){
		return pattern;
	}

	@Override
	public boolean equals(Object obj){
		return(getClass() == obj.getClass() && pattern == ((VertisyClientSocketFactory) obj).pattern);
	}
}