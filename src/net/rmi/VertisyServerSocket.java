package net.rmi;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Dec 18, 2016
 */
public class VertisyServerSocket extends ServerSocket{

	/*
	 * The pattern used to "encrypt" and "decrypt" each byte sent
	 * or received by the socket.
	 */
	private final byte pattern;

	/*
	 * Constructor for class XorServerSocket.
	 */
	public VertisyServerSocket(int port, byte pattern) throws IOException{
		super(port);
		this.pattern = pattern;
	}

	/*
	 * Creates a socket of type XorSocket and then calls
	 * implAccept to wait for a client connection.
	 */
	@Override
	public Socket accept() throws IOException{
		Socket s = new VertisySocket(pattern);
		implAccept(s);
		return s;
	}
}