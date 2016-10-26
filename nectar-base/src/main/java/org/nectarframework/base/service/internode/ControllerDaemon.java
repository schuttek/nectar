package org.nectarframework.base.service.internode;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

import org.nectarframework.base.service.Log;

//TODO: this needs better security and error handling
//TODO: add in a firewall? ie limit clients by IP?

public class ControllerDaemon extends Thread {
	// The host:port combination to listen on
	private InetAddress hostAddress;
	private int port;
	private boolean locked = false;

	protected InternodeService is;

	// The channel on which we'll accept connections
	private ServerSocketChannel serverChannel;

	// The selector we'll be monitoring
	private Selector selector;

	// The buffer into which we'll read data when it's available
	private ByteBuffer readBuffer = ByteBuffer.allocate(8192);

	private LinkedList<ChangeRequest> changeRequests = new LinkedList<ChangeRequest>();

	// Maps a SocketChannel to a list of ByteBuffer instances
	private HashMap<SocketChannel, LinkedList<ByteBuffer>> pendingData = new HashMap<SocketChannel, LinkedList<ByteBuffer>>();
	private HashMap<SocketChannel, ObjectInputStream> oisMap = new HashMap<SocketChannel, ObjectInputStream>();
	private HashMap<SocketChannel, PipedOutputStream> oosMap = new HashMap<SocketChannel, PipedOutputStream>();

	public ControllerDaemon(InternodeService is) {
		this.is = is;
	}

	public void init() throws IOException {
		this.selector = initSelector();
	}

	private Selector initSelector() throws IOException {
		// Create a new selector
		Selector socketSelector = SelectorProvider.provider().openSelector();

		// Create a new non-blocking server socket channel
		this.serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);

		// Bind the server socket to the specified address and port
		InetSocketAddress isa = new InetSocketAddress(this.hostAddress, this.port);
		serverChannel.socket().bind(isa);

		// Register the server socket channel, indicating an interest in
		// accepting new connections
		serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);

		return socketSelector;
	}

	@Override
	public void run() {
		while (is.keepDaemonRunning()) {
			try {
				// Process any pending changes
				synchronized (this.changeRequests) {
					for (ChangeRequest change : changeRequests) {
						if (change.type == ChangeRequest.Type.CHANGEOPS) {
							SelectionKey key = change.socket.keyFor(this.selector);
							key.interestOps(change.ops);
						} else if (change.type == ChangeRequest.Type.REGISTER) {
							change.socket.register(this.selector, change.ops);
						}
					}
					this.changeRequests.clear();
				}

				// Wait for an event one of the registered channels
				this.selector.select();

				// Iterate over the set of keys for which events are available
				Set<SelectionKey> keySet = selector.selectedKeys();
				for (SelectionKey key : keySet) {
					if (!key.isValid()) {
						continue;
					}
					// Check what event is available and deal with it
					if (key.isAcceptable()) {
						this.accept(key);
					} else if (key.isReadable()) {
						this.read(key);
					} else if (key.isWritable()) {
						this.write(key);
					}
				}
			} catch (Exception e) {
				Log.warn("ServerDaemon.run()", e);
			}
		}
	}

	private void accept(SelectionKey key) throws IOException {
		// For an accept to be pending the channel must be a server socket
		// channel.
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

		// Accept the connection and make it non-blocking
		SocketChannel socketChannel = serverSocketChannel.accept();

		socketChannel.configureBlocking(false);

		Log.trace("ServerDaemon accepting " + socketChannel.toString());

		key.cancel();

		PipedOutputStream pos = new PipedOutputStream();
		oisMap.put(socketChannel, new ObjectInputStream(new BufferedInputStream(new PipedInputStream(pos))));
		oosMap.put(socketChannel, pos);

		// Register the new SocketChannel with our Selector, indicating
		// we'd like to be notified when there's data waiting to be read
		socketChannel.register(this.selector, SelectionKey.OP_READ);

	}

	private void read(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		Log.trace("ServerDaemon reading " + socketChannel.toString());
		// Clear out our read buffer so it's ready for new data
		this.readBuffer.clear();

		// Attempt to read off the channel
		int numRead;
		try {
			numRead = socketChannel.read(this.readBuffer);
		} catch (IOException e) {
			// The remote forcibly closed the connection, cancel
			// the selection key and close the channel.
			is.unregisterClient(socketChannel);
			oisMap.remove(socketChannel);
			oosMap.remove(socketChannel);
			key.cancel();
			socketChannel.close();
			Log.trace("on read", e);
			return;
		}

		if (numRead == -1) {
			// Remote entity shut the socket down cleanly. Do the
			// same from our end and cancel the channel.
			is.unregisterClient(socketChannel);
			oisMap.remove(socketChannel);
			oosMap.remove(socketChannel);
			key.channel().close();
			key.cancel();
			socketChannel.close();
			return;
		}

		if (numRead > 0) {
			// push the read data into ObjectStream

			oosMap.get(socketChannel).write(this.readBuffer.array(), 0, numRead);
			Log.trace("ServerDaemon read " + numRead + " bytes.");

			// See if something is ready to pops out of the ObjectStream
			ObjectInputStream ois = oisMap.get(socketChannel);
			if (ois.available() >= 4) {
				ois.mark(4);
				int packetLength = ois.readInt();
				if (ois.available() < packetLength) {
					ois.reset();
				}

				if (ois.available() >= packetLength) {
					// rebuild the Packet object
					Packet packet;
					try {
						packet = (Packet) ois.readObject();
					} catch (ClassNotFoundException e) {
						// uh...
						Log.fatal(e);
						is.unregisterClient(socketChannel);
						oisMap.remove(socketChannel);
						oosMap.remove(socketChannel);
						key.channel().close();
						key.cancel();
						socketChannel.close();
						return;
					}
					if (packet.packetType == "InternodeService.nodeContext") {
						@SuppressWarnings("unchecked")
						HashMap<String, String> nodeContextMap = (HashMap<String, String>) (packet.dataObject);
						NodeContext nc = new NodeContext(socketChannel, nodeContextMap.get("nodeName"), nodeContextMap.get("nodeGroup"));
						is.registerClient(socketChannel, nc);
						Log.trace("ServerDaemon read nodeContext: name=" + nodeContextMap.get("nodeName") + " group=" + nodeContextMap.get("nodeGroup"));
					} else if (is.isConnectionRegistered(socketChannel)) {

						// Hand the data off to our worker thread
						is.processControllerRead(socketChannel, packet);
					}
				}
			}
		} else {
			Log.trace("ServerDaemon nothing to read on " + socketChannel.toString());
		}
	}

	public void send(SocketChannel socket, Packet data) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;

		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(data);
			oos.flush();
		} catch (IOException e) {
			Log.fatal(e);
		}
		byte[] dataBA = baos.toByteArray();
		ByteBuffer dataArray = ByteBuffer.wrap(dataBA);
		int packetLength = dataBA.length;
		ByteBuffer packetLengthArray = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(packetLength);

		// And queue the data we want written
		synchronized (this.pendingData) {
			LinkedList<ByteBuffer> queue = this.pendingData.get(socket);
			if (queue == null) {
				queue = new LinkedList<ByteBuffer>();
				this.pendingData.put(socket, queue);
			}
			queue.add(packetLengthArray);
			queue.add(dataArray);
		}

		// Finally, wake up our selecting thread so it can make the required
		// changes
		this.selector.wakeup();
	}

	private void write(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		Log.trace("Preping to wire to " + socketChannel);

		synchronized (this.pendingData) {
			LinkedList<ByteBuffer> queue = this.pendingData.get(socketChannel);

			// Write until there's not more data ...
			while (!queue.isEmpty()) {
				ByteBuffer buf = queue.get(0);
				socketChannel.write(buf);
				Log.trace("Writing to " + socketChannel + " data: " + buf.toString() + " " + new String(buf.array()));
				if (buf.remaining() > 0) {
					// ... or the socket's buffer fills up
					break;
				}
				queue.remove(0);
			}

			if (queue.isEmpty()) {
				// We wrote away all data, so we're no longer interested
				// in writing on this socket. Switch back to waiting for
				// data.
				key.interestOps(SelectionKey.OP_READ);
			}
		}
	}

	public void setHostAddress(InetAddress hostAddress) {
		this.hostAddress = hostAddress;
	}

	public InetAddress getHostAddress() {
		return hostAddress;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getPort() {
		return port;
	}
}
