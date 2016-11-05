package org.nectarframework.base.service.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.nectarframework.base.service.Log;
import org.nectarframework.base.tools.ByteArray;

/**
 * TODO: when trying to send a huge packet, it'll block any other packets behind
 * it from being sent. Instead, chunkify big packets, and transmit them in one
 * channel, while other packets can use other channels. the top 4 bits of the
 * connection options byte could be enough to designate 16 different channels.
 * 
 * @author skander
 *
 */

public class Connection extends Thread {

	private ConnectionService connService = null;

	private ByteArray readQueue = new ByteArray();

	private Deflater deflater = null;
	private Inflater inflater = null;

	private Socket socket;
	private OutputStream outputStream;
	private InputStream inputStream;
	private Boolean streamsReady = false;
	private int bufferSize = 1024;

	private boolean encryptionEnabled = true;
	private boolean compressionEnabled = true;

	private Cipher decryptCipher;
	private Cipher encryptCipher;

	private static final int MAX_PACKET_SIZE = 50 * 1024 * 1024;
	public static final byte STREAM_OPTION_COMPRESSION = 0x01;
	public static final byte STREAM_OPTION_ENCRYPTION = 0x02;
	public static final byte STREAM_OPTION_ENCRYPTION_RSA_KEY = 0x04;
	public static final byte STREAM_OPTION_ENCRYPTION_TWOFISH_KEY = 0x08;

	protected Connection(String name, ConnectionService cs, Socket socket) {
		super(name);
		connService = cs;
		this.socket = socket;
		encryptionEnabled = connService.isEncryptionEnabled();
		compressionEnabled = connService.isCompressionEnabled();
	}

	public Socket getSocket() {
		return socket;
	}

	public boolean isEncryptionEnabled() {
		return encryptionEnabled;
	}

	public void setEncryptionEnabled(boolean encryptionEnabled) {
		this.encryptionEnabled = encryptionEnabled;
	}

	public boolean isCompressionEnabled() {
		return compressionEnabled;
	}

	public void setCompressionEnabled(boolean compressionEnabled) {
		this.compressionEnabled = compressionEnabled;
	}

	public void read(int nBytes) {
		byte[] buffer = null;

		buffer = new byte[nBytes];
		int numRead = 0;
		try {
			numRead = inputStream.read(buffer);
		} catch (SocketTimeoutException e) {
		} catch (IOException e) {
			Log.warn(this.getName(), e);
			disconnect();
			return;
		}

		if (numRead < 0) {
			Log.warn(this.getName() + ": Remote forcibly closed the connection.");
			disconnect();
		} else if (numRead == 0) {
			// SO_TIMEOUT
			//Log.trace(this.getName() + ".tryToRead() numRead == 0");
		} else {
			readQueue.addRawBytes(buffer, 0, numRead);
		}
	}

	public void run() {

		try {
			outputStream = socket.getOutputStream();
			inputStream = socket.getInputStream();
		} catch (IOException e) {
			Log.warn(e);
			disconnect();
			return;
		}

		streamsReady = true;
		synchronized(streamsReady) {
			streamsReady.notify();
		}

		while (connService.keepRunning()) {
			int available = 0;
			try {
				available = inputStream.available();
			} catch (IOException e) {
				Log.warn(this.getName(), e);
				disconnect();
				return;
			}

			if (available <= 0) {
				read(bufferSize); // blocking read
			} else {
				read(available); // non blocking read
			}

			while (readQueue.length() >= 5) { // header available

				byte[] header = readQueue.remove(5);

				byte packetOptions = header[0];
				int incomingPacketSize = ByteArray.bytesToInt(header, 1);

				//Log.trace(this.getName() + " build header -> " + header[0] + "." + header[1] + "." + header[2] + "." + header[3] + "." + header[4] + " packetLen " + ByteArray.bytesToInt(header, 1));

				// header sanity check
				if (incomingPacketSize > MAX_PACKET_SIZE || incomingPacketSize <= 0) {
					Log.warn("incoming packet has illegal size (" + incomingPacketSize + ")");
					disconnect();
					return;
				}

				// now read the packet
				while (readQueue.length() < incomingPacketSize) {
					read(incomingPacketSize - readQueue.length());
				}

				byte[] packet = readQueue.remove(incomingPacketSize);

				// encryption and such
				packet = processPacketOptions(true, packetOptions, packet);

				if (packet != null) {
					connService.handlePacket(this, packetOptions, packet);
				}
			}
		}
	}

	private byte[] processPacketOptions(boolean read, byte packetOptions, byte[] packet) {

		// decrypt
		if (read && ((packetOptions & STREAM_OPTION_ENCRYPTION) > 0) && decryptCipher != null) {

			try {
				packet = decryptCipher.doFinal(packet);
			} catch (IllegalBlockSizeException e) {
				Log.warn(e);
				disconnect();
				return null;
			} catch (BadPaddingException e) {
				Log.warn(e);
				disconnect();
				return null;
			}

			packet = unpad(packet);

		}

		// Inflate
		if (read && ((packetOptions & STREAM_OPTION_COMPRESSION) > 0)) {
			if (inflater == null) {
				inflater = new Inflater();
			}
			// -int beforeLen = packet.length - 4;
			int inflatedSize = ByteArray.bytesToInt(packet, 0);
			if (inflatedSize > MAX_PACKET_SIZE || inflatedSize < 0) {
				Log.warn("packet's decompressed size is illegal (" + inflatedSize + ")");
				return null;
			}
			byte[] output = new byte[inflatedSize];
			inflater.reset();
			inflater.setInput(packet, 4, packet.length - 4);
			int outputLen;
			try {
				outputLen = inflater.inflate(output);
			} catch (DataFormatException e) {
				Log.warn(e);
				return null;
			}
			// inflater.end();
			if (outputLen < output.length) {
				packet = new byte[outputLen];
				System.arraycopy(output, 0, packet, 0, outputLen);
			} else {
				packet = output;
			}
			//Log.trace(this.getName() + " Decompression " + beforeLen + " -> " + inflatedSize + " (" + (inflatedSize * 100 / beforeLen) + "%");
		}

		// Deflate
		if (!read && ((packetOptions & STREAM_OPTION_COMPRESSION) > 0)) {
			if (deflater == null) {
				deflater = new Deflater();
			}
			int beforeLen = packet.length;
			byte[] output = new byte[packet.length + 8];
			deflater.reset();
			deflater.setInput(packet);
			deflater.finish();
			int compressedSize = deflater.deflate(output);

			packet = new byte[compressedSize + 4];
			ByteArray.intToBytes(beforeLen, packet, 0);
			System.arraycopy(output, 0, packet, 4, compressedSize);

		//	Log.trace(this.getName() + " Compression " + beforeLen + " -> " + compressedSize + " (" + (compressedSize * 100 / beforeLen) + "%");
		}

		// encrypt
		if (!read && ((packetOptions & STREAM_OPTION_ENCRYPTION) > 0) && encryptCipher != null) {

			packet = pad(packet, 8);
			try {
				packet = encryptCipher.doFinal(packet);
			} catch (IllegalBlockSizeException e) {
				Log.warn(e);
				disconnect();
				return null;
			} catch (BadPaddingException e) {
				Log.warn(e);
				disconnect();
				return null;
			}
		}

		return packet;
	}

	private byte[] pad(byte[] packet, int i) {
		int addLen = i - packet.length % i;
		byte[] buff = new byte[packet.length + addLen];

		System.arraycopy(packet, 0, buff, 0, packet.length);

		buff[buff.length - 1] = (byte) addLen;

		return buff;
	}

	private byte[] unpad(byte[] packet) {
		int addLen = packet[packet.length - 1];
		byte[] buff = new byte[packet.length - addLen];
		System.arraycopy(packet, 0, buff, 0, buff.length);
		return buff;
	}

	public void disconnect() {
		try {
			this.socket.close();
		} catch (IOException e) {
			Log.warn(e);
		}
		connService.notifyDisconnect(this);
	}

	private void waitForStreamsReady() {
		while (!streamsReady) {
			synchronized (streamsReady) {
				try {
					streamsReady.wait(1);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	protected void send(byte[] byteBuff) {
		waitForStreamsReady();
		byteBuff = processPacketOptions(false, optionsByte(), byteBuff);

		byte[] header = new byte[5];
		header[0] = optionsByte();
		ByteArray.intToBytes(byteBuff.length, header, 1);

		//Log.trace(this.getName() + " WRITE (" + byteBuff.length + ") header -> " + header[0] + "." + header[1] + "." + header[2] + "." + header[3] + "." + header[4] + " packetLen " + ByteArray.bytesToInt(header, 1));

		try {
			synchronized (outputStream) {
				outputStream.write(header);
				outputStream.write(byteBuff);
			}
		} catch (IOException e) {
			Log.warn(e);
			disconnect();
		}
	}

	private byte optionsByte() {
		byte options = 0;
		if (compressionEnabled) {
			options |= Connection.STREAM_OPTION_COMPRESSION;
		}
		if (encryptionEnabled) {
			options |= Connection.STREAM_OPTION_ENCRYPTION;
		}
		return options;
	}

	public void setBlowfishKey(SecretKey secretKey) {

		try {
			encryptCipher = Cipher.getInstance("Blowfish");
			encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey);

			decryptCipher = Cipher.getInstance("Blowfish");
			decryptCipher.init(Cipher.DECRYPT_MODE, secretKey);

		} catch (NoSuchAlgorithmException e) {
			Log.warn(e);
			disconnect();
			return;
		} catch (NoSuchPaddingException e) {
			Log.warn(e);
			disconnect();
			return;
		} catch (InvalidKeyException e) {
			Log.warn(e);
			disconnect();
			return;
		}
	}

	public String getRemoteIp() {
		String s = null;
		SocketAddress sa = socket.getRemoteSocketAddress();
		if (sa instanceof InetSocketAddress) {
			s = ((InetSocketAddress) sa).getAddress().getHostAddress();
		}
		return s;
	}

}
