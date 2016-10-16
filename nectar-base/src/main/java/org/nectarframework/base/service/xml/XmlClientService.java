package org.nectarframework.base.service.xml;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.LinkedList;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.service.ServiceParameters;
import org.nectarframework.base.service.ServiceUnavailableException;
import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.service.thread.ThreadService;
import org.nectarframework.base.service.thread.ThreadServiceTask;
import org.nectarframework.base.tools.Base64;
import org.xml.sax.SAXException;

/**
 * For a client to connect to a server and communicate with Element objects.
 * 
 * @author skander
 *
 */
public class XmlClientService extends ConnectionService {

	private String serverHost;
	private InetAddress serverHostAddress;
	private int serverPort;
	private boolean compressionEnabled = true;
	private boolean encryptionEnabled = true;

	private ThreadService threadService;
	private XmlService xmlService;

	private boolean keepRunning = true;

	private Connection connection = null;

	private HashMap<String, XmlResponse> responseMap = new HashMap<String, XmlResponse>();
	private long requestCounter = 0;
	private HashMap<String, LinkedList<XmlPushHandler>> pushHandlerMap = new HashMap<String, LinkedList<XmlPushHandler>>();
	private PublicKey publicKey;

	@Override
	public void checkParameters(ServiceParameters sp) throws ConfigurationException {
		serverHost = sp.getValue("serverHost");
		serverPort = sp.getInt("serverPort", 1, Short.MAX_VALUE, 8009);
		compressionEnabled = sp.getBoolean("compressionEnabled", compressionEnabled);
		encryptionEnabled = sp.getBoolean("encryptionEnabled", encryptionEnabled);
		if (serverHost != null) {
			try {
				serverHostAddress = InetAddress.getByName(serverHost);
			} catch (UnknownHostException e) {
				throw new ConfigurationException("XmlServerService", e);
			}
		}
	}

	@Override
	protected boolean init() {
		return true;
	}

	@Override
	public boolean establishDependancies() throws ServiceUnavailableException {
		threadService = (ThreadService) dependancy(ThreadService.class);
		xmlService = (XmlService) dependancy(XmlService.class);
		return true;
	}

	@Override
	protected boolean run() {
		boolean connectRet = connect();

		if (connectRet) {
			final XmlClientService thisService = this;
			threadService.executeLater(new ThreadServiceTask() {
				public void execute() throws Exception {
					thisService.runSanityCheck();
				}
			}, 1000);
		}

		return connectRet;
	}

	protected void runSanityCheck() {
		// FIXME Sanity not found...		
	}


	@Override
	protected boolean shutdown() {
		disconnect();
		return true;
	}

	public synchronized boolean connect() {
		if (connection != null) {
			throw new IllegalStateException("XmlClientService already connected!");
		}

		try {
			Socket socket = new Socket(serverHostAddress, serverPort);
			socket.setSoTimeout(10000);
			//Log.trace("client connected");
			connection = new Connection("XmlClientService-Connection", this, socket);
			connection.start();
			
			establishEncryption();
			return true;
		} catch (IOException e) {
			Log.warn(e);
		}
		disconnect();
		return false;
	}

	public void sendRequest(String actionPath, Element formElement, XmlResponseHandler responseHandler) throws IOException {
		sendPackage("request", actionPath, formElement, responseHandler);
	}
	
	protected void sendPackage(String packageType, String actionPath, Element formElement, XmlResponseHandler responseHandler) throws IOException {
		requestCounter++;
		String requestId = Long.toString(requestCounter);

		Element requestElm = new Element(packageType);
		requestElm.add("action", actionPath);
		requestElm.add("id", requestId);
		requestElm.add(formElement);

		XmlResponse xmlResponse = new XmlResponse(responseHandler, requestElm, requestId, connection);

		// FIXME: use a multi thread hashmap
		synchronized (responseMap) {
			responseMap.put(requestId, xmlResponse);
		}
		connection.send(XmlService.toXmlBytes(requestElm));
	}

	
	public void addListener(String channel, XmlPushHandler pushHandler) {
		synchronized (pushHandlerMap) {
			if (!pushHandlerMap.containsKey(channel)) {
				pushHandlerMap.put(channel, new LinkedList<XmlPushHandler>());
			}
			pushHandlerMap.get(channel).add(pushHandler);
		}
	}

	public synchronized void disconnect() {
		keepRunning = false;
		// TODO: gently???
		connection.disconnect();
		//Log.trace("XmlClientService.disconnect()" + Log.getStackTrace());
		if (connection != null) {
			connection.interrupt();
			try {
				connection.join();
			} catch (InterruptedException e) {
			}
			connection = null;
		}
		responseMap.clear();
		pushHandlerMap.clear();
	}

	private void establishEncryption() {

		BlockingResponseHandler brh = new BlockingResponseHandler(10000);
		
		try {
			sendPackage("internalRequest", "getPublicKey", null, brh);
		} catch (IOException e1) {
			Log.warn(e1);
			disconnect();
			return;
		}
		
		XmlResponse publicKeyElement = brh.waitForResponse();
		if (publicKeyElement == null) { // timeout
			Log.warn("XmlClientService.establishEncryptiong() Timeout 1.");
			disconnect();
			return;
		}
		
		//Log.trace(publicKeyElement.getResponse().getChildren().getFirst().get("key").toString());
		
		

		try {
			byte[] pkbytes = Base64.decode(publicKeyElement.getResponse().getChildren().getFirst().get("key"));
			X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(pkbytes);
			publicKey = KeyFactory.getInstance("RSA").generatePublic(pubKeySpec);

			// client side generates a Twofish key
			KeyGenerator keyGenerator;
			keyGenerator = KeyGenerator.getInstance("Blowfish");
			keyGenerator.init(128);
			SecretKey blowfishKey = keyGenerator.generateKey();
			
			

			// client side
			// read the server's RSA Public key from the server
			// and send the RSA encrypted Twofish key to the server
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			byte[] blowfishKeyBytes = blowfishKey.getEncoded();
			byte[] cipherText = cipher.doFinal(blowfishKeyBytes);

			//Log.trace("client blowfish key:" + Base64.encode(blowfishKeyBytes));
			
			Element setBlowfishKeyRequest = new Element("blowfishKey").add("key", Base64.encode(cipherText));
			brh = new BlockingResponseHandler(10000);
			try {
				sendPackage("internalRequest", "setBlowfishKey", setBlowfishKeyRequest, brh);
			} catch (IOException e) {
				Log.warn(e);
				disconnect();
				return;
			}
			
			XmlResponse blowfishKeyResponse = brh.waitForResponse();
			
			if (blowfishKeyResponse == null) { // timeout
				Log.warn("XmlClientService.establishEncryptiong() Timeout 2.");
				disconnect();
				return;
			}

			connection.setBlowfishKey(blowfishKey);
			

		} catch (NoSuchAlgorithmException e) {
			Log.fatal(e);
			disconnect();
			return;
		} catch (NoSuchPaddingException e) {
			Log.fatal(e);
			disconnect();
			return;
		} catch (InvalidKeyException e) {
			Log.fatal(e);
			disconnect();
			return;
		} catch (IllegalBlockSizeException e) {
			Log.fatal(e);
			disconnect();
			return;
		} catch (BadPaddingException e) {
			Log.fatal(e);
			disconnect();
			return;
		} catch (InvalidKeySpecException e1) {
			Log.fatal(e1);
			disconnect();
			return;
		}

	}

	@Override
	public void notifyDisconnect(Connection connection) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void handlePacket(Connection connection, byte packetOptions, byte[] packet) {
		Element element = null;
		try {
			element = XmlService.fromXml(packet);
		} catch (SAXException e) {
			Log.warn(e);
			return;
		}

		if (element.isName("response") || element.isName("internalResponse")) {
			String id = element.get("id");

			final XmlResponse xrh = responseMap.remove(id);
			if (xrh == null) {
				Log.warn("XmlClientService.handlePacket(); Received message with id " + id + " has no associated XmlResponse object waiting for it...");
			} else if (xrh.getXmlResponseHandler() == null) {
				// this isn't really an error...
				//Log.trace("XmlClientService.handlePacket(); Received message id " + id + " has null responseHandler");
			} else {
				xrh.setResponse(element);

				threadService.execute(xrh);
			}

		}

	}

	@Override
	public XmlService getXmlService() {
		return xmlService;
	}

	@Override
	protected boolean keepRunning() {
		return keepRunning;
	}

	@Override
	public boolean isClient() {
		return true;
	}

	@Override
	public boolean isEncryptionEnabled() {
		return this.encryptionEnabled;
	}

	@Override
	public boolean isCompressionEnabled() {
		return this.compressionEnabled;
	}
}
