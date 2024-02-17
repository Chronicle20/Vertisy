package net.rmi;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Dec 18, 2016
 */
public class VertisyOutputStream extends FilterOutputStream{

	/*
	 * The byte used to "encrypt" each byte of data.
	 */
	private final byte pattern;

	/*
	 * Constructs an output stream that uses the specified pattern
	 * to "encrypt" each byte of data.
	 */
	public VertisyOutputStream(OutputStream out, byte pattern){
		super(out);
		this.pattern = pattern;
	}

	/*
	 * XOR's the byte being written with the pattern
	 * and writes the result.
	 */
	@Override
	public void write(int b) throws IOException{
		out.write((b ^ pattern) & 0xFF);
	}
}