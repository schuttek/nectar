package org.nectarframework.base.service.xml;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.xerces.impl.dv.util.Base64;
import org.nectarframework.base.element.Element;
import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.exception.ServiceUnavailableException;
import org.nectarframework.base.service.Log;
import org.nectarframework.base.service.ServiceParameters;
import org.nectarframework.base.service.directory.DirectoryService;
import org.nectarframework.base.service.log.AccessLogService;
import org.nectarframework.base.service.session.Session;
import org.nectarframework.base.service.session.SessionService;
import org.nectarframework.base.service.thread.ThreadService;
import org.xml.sax.SAXException;

/**
 * This Service allows XmlClientService to connect to it over a network.
 * 
 * Both sides support compression, as well as RSA key exchange and Blowfish
 * encryption, as well multi-channel communication: ( large data sets are
 * segmented and reassembled on the other side).
 * 
 * The essential goal is for the XmlServerService and the XmlClientService to exchange nectar.base.xml.Element objects.
 * 
 * The XmlServerService responds to requests from from XmlClientService like RequestHandler. (see nectar.base.service.request). 
 * 
 * As a server, it operates as a one thread per client system (after much research into I/O polling) that keeps a single connection to the client. 
 * 
 * While server push notifications are supported in the protocol, they are not implemented as yet.
 * 
 * 
 * @author skander
 *
 */

public class XmlServerService extends ConnectionService {

	private String serverHost;
	private InetAddress serverHostAddress;
	private int serverPort = 8009;
	private int serverSocketBacklog = 50;
	private int maxConnections = 20000;
	private int soTimeout = 10000;
	private String rsaPublicKey = null;
	private String rsaPrivateKey = null;

	private KeyPair rsaKeyPair = null;

	private ThreadService threadService;
	private XmlService xmlService;
	private DirectoryService directoryService;
	private SessionService sessionService;

	private ServerSocket serverSocket;

	private HashMap<Socket, Connection> clients = new HashMap<Socket, Connection>();
	private HashMap<Connection, Session> sessions = new HashMap<Connection, Session>();

	private boolean keepRunning = true;

	private Thread acceptThread = null;
	private AccessLogService accessLogService;

	@Override
	public void checkParameters(ServiceParameters sp) throws ConfigurationException {
		serverHost = sp.getString("serverHost", "serverHost");
		serverPort = sp.getInt("serverPort", 1, Short.MAX_VALUE, serverPort);
		serverSocketBacklog = sp.getInt("serverSocketBacklog", 0, 10000, serverSocketBacklog);
		maxConnections = sp.getInt("maxConnections", 1, 100000, maxConnections);
		soTimeout = sp.getInt("soTimeout", 0, Integer.MAX_VALUE, soTimeout);

		rsaPublicKey = sp.getString("rsaPublicKey", null);
		rsaPrivateKey = sp.getString("rsaPrivateKey", null);

		if (serverHost != null) {
			try {
				serverHostAddress = InetAddress.getByName(serverHost);
			} catch (UnknownHostException e) {
				throw new ConfigurationException("XmlServerService.serverHost", e);
			}
		}

		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.decode(rsaPublicKey));
			PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

			PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(Base64.decode(rsaPrivateKey));
			PrivateKey privateKey;
			privateKey = keyFactory.generatePrivate(privateKeySpec);

			rsaKeyPair = new KeyPair(publicKey, privateKey);
		} catch (InvalidKeySpecException e) {
			throw new ConfigurationException("XmlServerService.rsaKeyPair", e);
		} catch (NoSuchAlgorithmException e) {
			throw new ConfigurationException("XmlServerService.rsaKeyPair", e);
		}

	}

	@Override
	protected boolean init() {
		if (connect()) {
			return true;
		}
		return false;
	}

	@Override
	public boolean establishDependencies() throws ServiceUnavailableException {
		threadService = (ThreadService) dependency(ThreadService.class);
		xmlService = (XmlService) dependency(XmlService.class);
		directoryService = (DirectoryService) dependency(DirectoryService.class);
		sessionService = (SessionService) dependency(SessionService.class);
		accessLogService = dependency(AccessLogService.class);
		return true;
	}

	@Override
	protected boolean run() {
		return true;
	}

	@Override
	protected boolean shutdown() {
		disconnect();
		return true;
	}

	private synchronized boolean connect() {
		try {
			serverSocket = new ServerSocket(serverPort, serverSocketBacklog, serverHostAddress);

			startAcceptThread();

			return true;
		} catch (IOException e) {
			Log.fatal(e);
			disconnect();
		}
		return false;
	}

	private void disconnect() {
		keepRunning = false;
		acceptThread.interrupt();

		for (Connection conn : clients.values()) {
			conn.interrupt();
			try {
				conn.join();
			} catch (InterruptedException e) {
			}
		}

		try {
			acceptThread.join();
		} catch (InterruptedException e) {
		}
		acceptThread = null;
	}

	private void startAcceptThread() {
		keepRunning = true;
		final XmlServerService thisService = this;
		acceptThread = new Thread("XmlServerService-AcceptTread") {
			public void run() {
				thisService.runAcceptThread();
			}
		};
		acceptThread.start();
	}

	protected void runAcceptThread() {
		Log.trace("XmlServerService.acceptThread started");
		while (keepRunning && !serverSocket.isClosed()) {
			Socket clientSocket = null;

			synchronized (clients) {
				while (clients.size() >= maxConnections) {
					try {
						clients.wait();
					} catch (InterruptedException e) {
						if (!keepRunning) {
							return;
						}
					}
				}
			}

			try {
				clientSocket = serverSocket.accept();
				clientSocket.setSoTimeout(soTimeout);
			} catch (IOException e) {
				Log.warn(e);
				continue;
			}

			String socketAddressString = "??";
			SocketAddress sa = clientSocket.getRemoteSocketAddress();
			if (sa instanceof InetSocketAddress) {
				socketAddressString = ((InetSocketAddress) sa).getAddress().getHostAddress();
			}

			Connection connection = new Connection("XmlServerService-Connection-" + socketAddressString, this, clientSocket);

			synchronized (clients) {
				clients.put(clientSocket, connection);
			}

			connection.start();

		}
		Log.trace("XmlServerService.serverDaemon ending");
	}

	@Override
	protected boolean keepRunning() {
		return keepRunning;
	}

	@Override
	protected void notifyDisconnect(Connection connection) {
		synchronized (clients) {
			clients.remove(connection.getSocket());
			clients.notify();
		}
	}

	@Override
	protected void handlePacket(Connection connection, byte packetOptions, byte[] packet) {
		Element element = null;
		try {
			element = XmlService.fromXml(packet);
		} catch (SAXException e) {
			Log.warn(e);
		}

		if (element != null) {

			Session session = sessions.get(connection);
			if (session == null) {
				session = sessionService.createSession();
				sessions.put(connection, session);
			}
			if (element.isName("request")) {
				XmlRequestHandler xrh = new XmlRequestHandler(this, directoryService, accessLogService, connection, session, element);
				threadService.execute(xrh);
			} else if (element.isName("internalRequest")) {
				handleInternal(connection, packetOptions, element);
			}
		}
	}

	private void handleInternal(Connection connection, byte packetOptions, Element element) {

		Element response = new Element("internalResponse");
		response.add("id", element.get("id"));

		if (element.isAttribute("action", "getPublicKey")) {
			response.add(new Element("publicKey").add("key", rsaPublicKey));

		} else if (element.isAttribute("action", "setBlowfishKey")) {

			Element form = element.getChildren().getFirst();
			if (form != null && form.isName("blowfishKey")) {
				byte[] encryptedKey = Base64.decode(form.get("key"));

				// server side
				// the server decrypts the Twofish key with it's RSA private key
				try {
					Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
					cipher.init(Cipher.DECRYPT_MODE, rsaKeyPair.getPrivate());
					byte[] decryptedKeyBytes = cipher.doFinal(encryptedKey);
					SecretKey blowfishKey = new SecretKeySpec(decryptedKeyBytes, "Blowfish");

					// Log.trace("server received key: " +
					// Base64.encode(decryptedKeyBytes) + "   len " +
					// decryptedKeyBytes.length);

					connection.setBlowfishKey(blowfishKey);

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
				} catch (IllegalBlockSizeException e) {
					Log.warn(e);
					disconnect();
					return;
				} catch (BadPaddingException e) {
					Log.warn(e);
					disconnect();
					return;
				}
			}

			response.add(new Element("success"));

		} else if (element.isAttribute("action", "setOptions")) {
			Element options = element.getChildren().getFirst();
			if (options != null) {
				for (Element option : options.getChildren()) {
					if (option.isAttribute("encryptionEnabled", "true")) {
						connection.setEncryptionEnabled(true);
					}
					if (option.isAttribute("encryptionEnabled", "false")) {
						connection.setEncryptionEnabled(false);
					}
					if (option.isAttribute("compressionEnabled", "true")) {
						connection.setCompressionEnabled(true);
					}
					if (option.isAttribute("compressionEnabled", "false")) {
						connection.setCompressionEnabled(false);
					}
				}
			}
			response.add(new Element("success"));

		}

		connection.send(XmlService.toXmlBytes(response));
	}

	public void sendResponse(Connection connection, String id, Element formElement) {
		Element responseElm = new Element("response");
		responseElm.add("id", id);
		responseElm.add(formElement);

		connection.send(XmlService.toXmlBytes(responseElm));
	}

	@Override
	protected XmlService getXmlService() {
		return xmlService;
	}

	@Override
	public boolean isClient() {
		return false;
	}

	@Override
	public boolean isEncryptionEnabled() {
		return false;
	}

	@Override
	public boolean isCompressionEnabled() {
		return false;
	}

}
