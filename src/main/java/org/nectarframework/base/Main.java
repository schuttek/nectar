package org.nectarframework.base;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.service.BasicConfig;
import org.nectarframework.base.service.Configuration;
import org.nectarframework.base.service.Log;
import org.nectarframework.base.service.ServiceRegister;
import org.nectarframework.base.service.log.LogLevel;
import org.nectarframework.base.service.xml.Element;
import org.nectarframework.base.service.xml.XmlService;
import org.xml.sax.SAXException;

/**
 * The base Launcher class of pretty much any server instance of Nectar. Here we
 * handle command line arguments, boot the Configuration and the
 * ServiceRegister, then let the ServiceRegister take over.
 * 
 */
public class Main {
	public static final String VERSION = "0.0.2";

	private static MainShutdownHandler msh = null;

	/**
	 * The start point to every Nectar instance.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		try {

			CommandLineParser parser = new DefaultParser();
			Options options = buildArgumentOptions();

			// parse the command line arguments
			CommandLine line;
			try {
				line = parser.parse(options, args);
			} catch (ParseException e) {
				e.printStackTrace();
				return;
			}

			if (line.hasOption("log")) {
				if (!setLogLevel(options, line)) {
					runHelp(options);
					return;
				}
			}
			// startup
			if (line.hasOption("version")) {
				runVersion();
			} else if (line.hasOption("help")) {
				runHelp(options);
			} else if (line.hasOption("configCheck")) {
				Optional<BasicConfig> obc = parseArgs(options, line);
				if (obc.isPresent()) {
					ServiceRegister sr = initNectar(obc.get());
					if (sr.configCheck()) {
						Log.info("Config Ok.");
						exit(0);
					} else {
						Log.info("Config Failed.");
						exit(-1);
					}
				}
			} else {
				Optional<BasicConfig> obc = parseArgs(options, line);

				if (obc.isPresent()) {
					runNectar(obc.get());
				} else {
					exit(-1);
				}

				if (line.hasOption("scriptMode")) {
					exit(0);
				}
			}

		} catch (Throwable t) {
			Log.fatal("CRASH", t);
			exit(-1);
		}
		// main thread ends after startup.
	}

	private static Options buildArgumentOptions() {
		Options options = new Options();

		options.addOption("v", "version", false, "print Version number and exit");
		options.addOption("h", "help", false, "print this message");
		options.addOption("cc", "configCheck", false, "run some basic sanity checks on the configuration file");
		options.addOption("s", "scriptMode", false,
				"Instead of running as a server, script mode starts and runs all configured Services, then shuts down.");
		Option opt = new Option("c", "configFile", true, "path to the configuration XML file");
		opt.setArgName("PATH");
		options.addOption(opt);
		opt = new Option("n", "nodeName", true, "give this instance a name");
		opt.setArgName("NAME");
		options.addOption(opt);
		opt = new Option("g", "nodeGroup", true, "the run mode to use, as described in the config file");
		opt.setArgName("GROUP");
		options.addOption(opt);
		opt = new Option("l", "log", true, "set initial log level to {trace, debug, info, warn, fatal, silent}");
		opt.setArgName("LOG");
		options.addOption(opt);

		return options;
	}

	private static boolean setLogLevel(Options options, CommandLine line) {
		if (line.hasOption("log")) {
			String logStr = line.getOptionValue("log");
			LogLevel ll = LogLevel.valueOf(logStr.toUpperCase());
			if (ll != null) {
				Log.logLevelOverride = ll;
				Log.debug("Log Level Override set to " + ll);
				return true;
			}
		}
		return false;
	}

	private static Optional<BasicConfig> parseArgs(Options options, CommandLine line) {
		boolean missingArgs = false;
		if (!line.hasOption("configFile")) {
			System.err.println("ERROR: configFile command line argument is required.");
			missingArgs = true;
		}
		if (!line.hasOption("nodeName")) {
			System.err.println("ERROR: nodeName command line argument is required.");
			missingArgs = true;
		}
		if (!line.hasOption("nodeGroup")) {
			System.err.println("ERROR: nodeGroup command line argument is required.");
			missingArgs = true;
		}

		if (missingArgs) {
			Main.runHelp(options);
			return null;
		}

		File configFile = new File(line.getOptionValue("configFile"));
		if (!configFile.exists()) {
			System.err.println("configuration file: " + line.getOptionValue("configFile") + " cannot be found.");
			return null;
		}
		if (!configFile.canRead()) {
			System.err.println("configuration file: " + line.getOptionValue("configFile") + " is not readable.");
			return null;
		}

		String nodeName = line.getOptionValue("nodeName");
		String nodeGroup = line.getOptionValue("nodeGroup");
		return Optional.of(new BasicConfig(configFile, nodeName, nodeGroup));
	}

	private static void runHelp(Options opts) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java nectar.base.Main", opts);
	}

	private static void runVersion() {
		System.out.println("Nectar Web Platform");
		System.out.println("Version: " + VERSION);
	}

	/**
	 * An emergency exit handler. will try to shutdown all services.
	 * 
	 */
	private static class MainShutdownHandler extends Thread {
		private ServiceRegister sr;

		public MainShutdownHandler(ServiceRegister sr) {
			this.sr = sr;
		}

		public void run() {
			sr.shutdown();
		}
	}

	/**
	 * Begin a controlled shutdown procedure, where all running Services will be
	 * asked to shutdown in orderly fashion.
	 */
	public static void exit(int exitCode) {
		ServiceRegister.getInstance().shutdown();
		Runtime.getRuntime().removeShutdownHook(msh);
		System.exit(exitCode);
	}

	/**
	 * Exit immediately, don't run shutdown hooks. This is obviously something
	 * that should never be done, unless you're purposely trying to break stuff.
	 */
	public static void crash__DO_NOT_USE_THIS_UNLESS_YOU_MEAN_HARM() {
		Runtime.getRuntime().removeShutdownHook(msh);
		System.exit(-1);
	}

	private static void runNectar(BasicConfig basicConfig) throws ConfigurationException, FileNotFoundException {
		runNectar(basicConfig);
	}

	public static void runNectar(InputStream configXmlFileIS, String nodeName, String nodeGroup, LogLevel level)
			throws ConfigurationException {
		startNectar(initNectar(configXmlFileIS, nodeName, nodeGroup, level));
	}

	public static ServiceRegister initNectar(BasicConfig basicConfig)
			throws ConfigurationException, FileNotFoundException {
		return initNectar(new FileInputStream(basicConfig.getConfigFile()), basicConfig.getNodeName(),
				basicConfig.getNodeGroup(), Log.DEFAULT_LOG_LEVEL);
	}

	public static ServiceRegister initNectar(InputStream configXmlFileIS, String nodeName, String nodeGroup,
			LogLevel level) throws ConfigurationException {
		Log.logLevelOverride = level;
		Log.info("Nectar is starting up");
		ServiceRegister sr = new ServiceRegister();
		Configuration config = new Configuration(sr);
		config.setNodeName(nodeName);
		config.setNodeGroup(nodeGroup);

		Element configElement;
		try {
			configElement = XmlService.fromXml(configXmlFileIS);
		} catch (SAXException e1) {
			Log.fatal(e1);
			return null;
		} catch (IOException e1) {
			Log.fatal(e1);
			return null;
		}

		sr.setConfiguration(config);
		sr.setConfigElement(configElement);
		msh = new MainShutdownHandler(sr);
		Runtime.getRuntime().addShutdownHook(msh);

		return sr;
	}

	private static void startNectar(ServiceRegister sr) {
		if (sr.configCheck() && sr.initNode()) {
			Log.trace("Configuration set, running Nectar " + sr.getConfiguration().getNodeName() + "@"
					+ sr.getConfiguration().getNodeGroup());
			sr.begin();
		}
	}

	public static void endNectar() {
		ServiceRegister.getInstance().shutdown();
		Runtime.getRuntime().removeShutdownHook(msh);
	}

	public static void runNectar(String configFilePath, String nodeName, String nodeGroup, LogLevel trace)
			throws FileNotFoundException, ConfigurationException {
		runNectar(new FileInputStream(configFilePath), nodeName, nodeGroup, trace);
	}
}
