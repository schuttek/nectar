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
		ba.addRawBytes(b, off, len);
	}

	public void reset() {
		ba = new ByteArray();
	}
	
	public int size() {
		return ba.length();
	}
	
	public ByteArray toByteArray() {
		return new ByteArray(ba);
	}
	
	public byte[] toBytes() {
		return ba.getAllBytes();
	}
	
	public String toString() {
		return new String ("ByteArrayOutputStream: " + new String(ba.getAllBytes()));
	}
	
	public void writeTo(OutputStream os) throws IOException {
		os.write(ba.getAllBytes());
	}
	
	public ByteArray getByteArray() {
		return ba;
	}
}
