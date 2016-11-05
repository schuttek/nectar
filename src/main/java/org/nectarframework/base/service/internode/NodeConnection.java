package org.nectarframework.base.service.internode;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.LinkedList;

import org.nectarframework.base.service.Log;
import org.nectarframework.base.service.ServiceRegister;

public class NodeConnection extends Thread {

	private InternodeService is;

	// The selector we'll be monitoring
	private Selector selector;

	private SocketChannel socketChannel;

	// A list of PendingChange instances
	private LinkedList<ChangeRequest> pendingChanges = new LinkedList<ChangeRequest>();

	// Maps a SocketChannel to a list of ByteBuffer instances
	private HashMap<SocketChannel, LinkedList<ByteBuffer>> pendingData = new HashMap<SocketChannel, LinkedList<ByteBuffer>>();
	private BufferedInputStream bis = null;
	private PipedOutputStream pos = new PipedOutputStream();

	private ByteBuffer readBuffer = ByteBuffer.allocate(65536);

	public NodeConnection(InternodeService is) {
		this.is = is;
		try {
			bis = new BufferedInputStream(new PipedInputStream(pos));
		} catch (IOException e) {
			Log.fatal(e); // can't happen.
		}
	}

	public void send(Packet data) throws IOException {
		// to write a packet, we're gonna use ObjectOutputStream to convert the
		// Packet object into a byte array first. In order for the other side to
		// be able to read the whole object without blocking, we'll send the
		// length of that byte array as an integer first.
		// Since we're dealing with NIO, we're gonna add this data to the
		// writing queue for the socket, then let the selector know we're ready
		// to write.

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);

		oos.writeObject(data);

		oos.flush();
		byte[] dataBA = baos.toByteArray();
		ByteBuffer dataArray = ByteBuffer.wrap(dataBA);
		int packetLength = dataBA.length;
		ByteBuffer packetLengthArray = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(packetLength);

		// And queue the data we want written
		synchronized (this.pendingData) {
			LinkedList<ByteBuffer> queue = this.pendingData.get(socketChannel);
			if (queue == null) {
				queue = new LinkedList<ByteBuffer>();
				this.pendingData.put(socketChannel, queue);
			}
			queue.add(packetLengthArray);
			queue.add(dataArray);
		}

		// Finally, wake up our selecting thread so it can make the required
		// changes
		this.selector.wakeup();
	}

	public void run() {
		try {
			while (true) {

				if (!is.keepDaemonRunning()) {
					this.disconnect();
					return;
				}

				if (this.selector == null) {
					throw new IOException("NodeConnection is not initialized properly.");
				}

				// Process any pending changes
				synchronized (this.pendingChanges) {
					for (ChangeRequest change : pendingChanges) {
						if (change.type == ChangeRequest.Type.CHANGEOPS) {
							SelectionKey key = change.socket.keyFor(this.selector);
							key.interestOps(change.ops);
						} else if (change.type == ChangeRequest.Type.REGISTER) {
							change.socket.register(this.selector, change.ops);
						}
					}
					this.pendingChanges.clear();
				}

				// Wait for an event one of the registered channels
				this.selector.select();

				// Iterate over the set of keys for which events are available
				for (SelectionKey key : selector.selectedKeys()) {

					if (!key.isValid()) {
						continue;
					}

					// Check what event is available and deal with it
					if (key.isConnectable()) {
						this.finishConnection(key);
					} else if (key.isReadable()) {
						this.read(key);
					} else if (key.isWritable()) {
						this.write(key);
					}
				}
			}
		} catch (IOException e) {
			Log.fatal("NodeConnection", e);
		}
	}

	private void read(SelectionKey key) throws IOException {

		SocketChannel socketChannel = (SocketChannel) key.channel();
		System.out.println("reading...");

		// Clear out our read buffer so it's ready for new data
		readBuffer.clear();

		// Attempt to read off the channel
		int numRead;
		try {
			numRead = socketChannel.read(readBuffer);
		} catch (IOException e) {
			// The remote forcibly closed the connection, cancel
			// the selection key and close the channel.
			key.cancel();
			socketChannel.close();
			e.printStackTrace();
			return;
		}

		// Handle the response
		if (numRead == -1) {
			// Remote entity shut the socket down cleanly. Do the
			// same from our end and cancel the channel.
			key.channel().close();
			key.cancel();
			return;
		} else if (numRead > 0) {
			// push the read data into ObjectStream

			pos.write(this.readBuffer.array(), 0, numRead);
			Log.trace("ServerDaemon read " + numRead + " bytes.");

			// See if something is ready to pops out of the ObjectStream
			if (bis.available() >= 4) {
				if (!bis.markSupported()) {
					throw new IOException("InputStream .markSupported() is false!");
				}
				bis.mark(4);
				byte[] packetLengthBA = new byte[4];
				bis.read(packetLengthBA);
				int packetLength = ByteBuffer.wrap(packetLengthBA).order(ByteOrder.LITTLE_ENDIAN).getInt();
				if (bis.available() < packetLength) {
					bis.reset();
				}

				if (bis.available() >= packetLength) {
					// rebuild the Packet object
					Packet packet;
					ObjectInputStream ois = new ObjectInputStream(bis);
					try {
						packet = (Packet) ois.readObject();
					} catch (ClassNotFoundException e) {
						// uh...
						Log.fatal(e);
						is.unregisterClient(socketChannel);
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
						// Hand the data off to the service
						is.processNodeRead(packet);
					}
				}
			}
		}

	}

	private void write(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		System.out.println("writing pending data");
		synchronized (this.pendingData) {
			LinkedList<ByteBuffer> queue = this.pendingData.get(socketChannel);

			if (queue == null) {
				return;
			}

			// Write until there's not more data ...
			while (!queue.isEmpty()) {
				ByteBuffer buf = queue.get(0);
				socketChannel.write(buf);
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

	private void finishConnection(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		// Finish the connection. If the connection operation failed
		// this will raise an IOException.
		try {
			socketChannel.finishConnect();
		} catch (ConnectException e) {
			if (e.getMessage().compareTo("Connection refused: no further information") == 0) {
				key.cancel();
				Log.trace("NodeConnection: can't connect to controller. will retry later.");
				is.reconnectLater();
				return;
			} else {
				Log.warn(e);
				key.cancel();
				return;
			}
		} catch (IOException e) {
			// Cancel the channel's registration with our selector
			Log.warn(e);
			key.cancel();
			return;
		}

		Log.trace("NodeConnection: established connection to controller.");

		// Register an interest in writing on this channel
		key.interestOps(SelectionKey.OP_WRITE);
		key.cancel();

		// introduce myself
		Packet packet = new Packet();
		HashMap<String, String> introMap = new HashMap<String, String>();
		introMap.put("nodeName", ServiceRegister.getInstance().getConfiguration().getNodeName());
		introMap.put("nodeGroup", ServiceRegister.getInstance().getConfiguration().getNodeGroup());
		packet.dataObject = introMap;

		this.send(packet);
	}

	public void connect() throws IOException {
		this.selector = SelectorProvider.provider().openSelector();

		// Create a non-blocking socket channel
		socketChannel = SocketChannel.open();
		socketChannel.configureBlocking(false);

		// Kick off connection establishment
		socketChannel.connect(new InetSocketAddress(this.is.getControllerHostAddress(), this.is.getControllerPort()));

		System.out.println("Connecting to: " + new InetSocketAddress(this.is.getControllerHostAddress(), this.is.getControllerPort()).toString());

		// Queue a channel registration since the caller is not the
		// selecting thread. As part of the registration we'll register
		// an interest in connection events. These are raised when a channel
		// is ready to complete connection establishment.
		synchronized (this.pendingChanges) {
			this.pendingChanges.add(new ChangeRequest(socketChannel, ChangeRequest.Type.REGISTER, SelectionKey.OP_CONNECT));
		}

	}

	private void disconnect() throws IOException {
		if (socketChannel != null) {
			socketChannel.close();
			socketChannel = null;
		}
		if (this.selector != null) {
			this.selector.close();
			this.selector = null;
		}
		this.pendingChanges.clear();

	}

}
