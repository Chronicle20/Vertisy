package net.rmi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Dec 18, 2016
 */
public class VertisySocket extends Socket{

	/*
	 * The pattern used to "encrypt" and "decrypt" each byte sent
	 * or received by the socket.
	 */
	private final byte pattern;
	/* The InputStream used by the socket. */
	private InputStream in = null;
	/* The OutputStream used by the socket */
	private OutputStream out = null;

	/*
	 * Constructor for class XorSocket.
	 */
	public VertisySocket(byte pattern) throws IOException{
		super();
		this.pattern = pattern;
	}

	/*
	 * Constructor for class XorSocket.
	 */
	public VertisySocket(String host, int port, byte pattern) throws IOException{
		super(host, port);
		this.pattern = pattern;
	}

	/*
	 * Returns a stream of type XorInputStream.
	 */
	@Override
	public synchronized InputStream getInputStream() throws IOException{
		if(in == null){
			in = new VertisyInputStream(super.getInputStream(), pattern);
		}
		return in;
	}

	/*
	 *Returns a stream of type XorOutputStream.
	 */
	@Override
	public synchronized OutputStream getOutputStream() throws IOException{
		if(out == null){
			out = new VertisyOutputStream(super.getOutputStream(), pattern);
		}
		return out;
	}
}
