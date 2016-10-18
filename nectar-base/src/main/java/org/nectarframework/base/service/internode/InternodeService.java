package org.nectarframework.base.service.internode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.exception.ServiceUnavailableException;
import org.nectarframework.base.service.Service;
import org.nectarframework.base.service.ServiceParameters;
import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.service.thread.ThreadService;
import org.nectarframework.base.tools.StringTools;

/**
 * The InternodeService is quite similar to ClientService, as it allows two
 * separate instances of Nectar to communicate. While ClientService is based on
 * RequestHandlers and ActionService, this InternodeService is much simpler, and
 * just transports Packet objects between connected instances.
 * 
 * @author skander
 *
 */
public class InternodeService extends Service {

	private static long localTimeDifference = 0;
	private String listeningHost;
	private InetAddress listeningHostAddress;
	private int listeningPort;

	private String controllerHost;
	private InetAddress controllerHostAddress;
	private int controllerPort;

	private long reconnectDelay;

	private boolean keepDaemonRunning;
	private HashMap<SocketChannel, NodeContext> activeConnections;
	private ControllerDaemon serverDaemon;
	private NodeConnection nodeConnection;
	private ThreadService threadService;

	@Override
	public void checkParameters(ServiceParameters sp) throws ConfigurationException {
		listeningHost = sp.getValue("listeningHost");
		listeningPort = sp.getInt("listeningPort", 1, 65536, -1);
		controllerHost = sp.getValue("controllerHost");
		controllerPort = sp.getInt("controllerPort", 1, 65536, -1);

		reconnectDelay = sp.getLong("reconnectDelay", 1, Long.MAX_VALUE, 5000);

		if (listeningHost != null) {
			try {
				listeningHostAddress = InetAddress.getByName(listeningHost);
			} catch (UnknownHostException e) {
				throw new ConfigurationException("InternodeService", e);
			}
		}
		if (controllerHost != null) {
			try {
				controllerHostAddress = InetAddress.getByName(controllerHost);
			} catch (UnknownHostException e) {
				throw new ConfigurationException("InternodeService", e);
			}
		}

	}

	@Override
	protected boolean init() {
		this.keepDaemonRunning = true;
		activeConnections = new HashMap<SocketChannel, NodeContext>();
		return true;
	}

	@Override
	public boolean establishDependencies() throws ServiceUnavailableException {

		threadService = (ThreadService) dependency(ThreadService.class);
		return true;
	}

	@Override
	protected boolean run() {

		if (this.listeningHost != null && this.listeningPort > 0) {
			this.serverDaemon = new ControllerDaemon(this);
			serverDaemon.setHostAddress(this.listeningHostAddress);
			serverDaemon.setPort(this.listeningPort);
			try {
				this.serverDaemon.init();
			} catch (IOException e) {
				Log.fatal("", e);
				return false;
			}
			this.serverDaemon.start();
		}

		return connectToController();

		// TODO: sync time with masterNode.
	}

	protected boolean connectToController() {
		if (this.controllerHost != null && this.controllerPort > 0) {
			this.nodeConnection = new NodeConnection(this);
			try {
				this.nodeConnection.connect();
			} catch (IOException e) {
				Log.fatal(e);
			}
			this.nodeConnection.start();
		}
		return true;
	}

	@Override
	protected boolean shutdown() {
		return true;
	}

	public InetAddress getListeningHostAddress() {
		return listeningHostAddress;
	}

	public int getListeningPort() {
		return listeningPort;
	}

	public InetAddress getControllerHostAddress() {
		return controllerHostAddress;
	}

	public int getControllerPort() {
		return controllerPort;
	}

	public boolean keepDaemonRunning() {
		return this.keepDaemonRunning;
	}

	public void registerClient(SocketChannel socketChannel, NodeContext nodeContext) {
		activeConnections.put(socketChannel, nodeContext);
	}

	public boolean isConnectionRegistered(SocketChannel socketChannel) {
		return activeConnections.containsKey(socketChannel);
	}

	public void unregisterClient(SocketChannel socketChannel) {
		activeConnections.remove(socketChannel);
	}

	public void processNodeRead(Packet packet) {

	}

	public void processControllerRead(SocketChannel socketChannel, Packet packet) {
		// TODO Auto-generated method stub

	}

	// basic controller messages
	public void shutdownNode(String nodeName) {
		Packet shutdownPls = new Packet();
		shutdownPls.packetType = "shutdown";

		// find the socketChannel
		for (SocketChannel sc : activeConnections.keySet()) {
			if (nodeName == activeConnections.get(sc).nodeName) {
				serverDaemon.send(sc, shutdownPls);
			}
		}

	}

	public void resetAllNodes() {

	}

	public void startupNode(String nodeName, String nodeGroup) throws IOException {

		Runtime.getRuntime().exec("c:/");

		List<String> cmdarray = new ArrayList<String>();
		cmdarray.add(getJreExecutable().toString());
		cmdarray.add("nectar.base.Main");
		// cmdarray.add("-c " +
		// ServiceRegister.getInstance().getConfiguration().getConfigFile().getPath());
		cmdarray.add("-g " + nodeGroup);
		cmdarray.add("-n " + nodeName);

		ProcessBuilder processBuilder = new ProcessBuilder(StringTools.implode(cmdarray, " "));
		processBuilder.start();
	}

	private boolean isWindows() {
		String os = System.getProperty("os.name");
		if (os == null) {
			throw new IllegalStateException("os.name");
		}
		os = os.toLowerCase();
		return os.startsWith("windows");
	}

	private File getJreExecutable() throws FileNotFoundException {
		String jreDirectory = System.getProperty("java.home");
		if (jreDirectory == null) {
			throw new IllegalStateException("java.home");
		}
		File exe;
		if (isWindows()) {
			exe = new File(jreDirectory, "bin/java.exe");
		} else {
			exe = new File(jreDirectory, "bin/java");
		}
		if (!exe.isFile()) {
			throw new FileNotFoundException(exe.toString());
		}
		return exe;
	}

	public void reconnectLater() {
		ReconnectLaterTask task = new ReconnectLaterTask(this);

		threadService.executeLater(task, this.reconnectDelay);
	}

	/**
	 * if InternodeService is running, returns the master node's system time,
	 * else returns the local system time.
	 * 
	 * @return
	 */
	public static long getTime() {
		return System.currentTimeMillis() + localTimeDifference;

	}

}
