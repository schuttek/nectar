package org.nectarframework.base.tools;

import java.io.IOException;
import java.io.OutputStream;

public class ByteArrayOutputStream extends OutputStream {

	private ByteArray ba = new ByteArray();
	
	@Override
	public void write(int b) throws IOException {
		ba.add((byte)b);
	}

	public void close() {
		ba.coalesce();
	}
	
	public void flush() {
		ba.coalesce();
	}
	
	public void write(byte[] b) {
		write(b, 0, b.length);
	}
	
	public void write(byte[] b, int off, int len) {
		ba.add(b, off, len);
	}

	public void reset() {
		ba = new ByteArray();
	}
	
	public int size() {
		return ba.length();
	}
	
	public byte[] toByteArray() {
		return ba.getBytes();
	}
	
	public String toString() {
		return new String (ba.getBytes());
	}
	
	public void writeTo(OutputStream os) throws IOException {
		os.write(ba.getBytes());
	}
	
	public ByteArray getByteArray() {
		return ba;
	}
}
