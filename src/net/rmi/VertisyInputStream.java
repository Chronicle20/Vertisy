package net.rmi;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Dec 18, 2016
 */
public class VertisyInputStream extends FilterInputStream{

	/*
	 * The byte being used to "decrypt" each byte of data.
	 */
	private final byte pattern;

	/*
	 * Constructs an input stream that uses the specified pattern
	 * to "decrypt" each byte of data.
	 */
	public VertisyInputStream(InputStream in, byte pattern){
		super(in);
		this.pattern = pattern;
	}

	/*
	 * Reads in a byte and xor's the byte with the pattern.
	 * Returns the byte.
	 */
	@Override
	public int read() throws IOException{
		int b = in.read();
		// If not end of file or an error, truncate b to one byte
		if(b != -1) b = (b ^ pattern) & 0xFF;
		return b;
	}

	/*
	 * Reads up to len bytes
	 */
	@Override
	public int read(byte b[], int off, int len) throws IOException{
		int numBytes = in.read(b, off, len);
		if(numBytes <= 0) return numBytes;
		for(int i = 0; i < numBytes; i++){
			b[off + i] = (byte) ((b[off + i] ^ pattern) & 0xFF);
		}
		return numBytes;
	}
}