package org.nectarframework.base;

import org.apache.commons.cli.CommandLineParser;  
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.nectarframework.base.config.Configuration;
import org.nectarframework.base.service.ServiceRegister;
import org.nectarframework.base.service.log.Log;

/**
 * The base Launcher class of pretty much any server instance of Nectar. Here we
 * handle command line arguments, boot the Configuration and the
 * ServiceRegister, then let the ServiceRegister take over.
 * 
 */
public class Main {
	public static final String VERSION = "0.0.0";

	
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

		ServiceRegister sr = null;

		// startup
		if (line.hasOption("version")) {
			runVersion();
		} else if (line.hasOption("help")) {
			runHelp(options);
		} else if (line.hasOption("configCheck")) {
			sr = runStartup(options, line);
			if (sr != null) {
				sr.configCheck();
			}
		} else {
			sr = runStartup(options, line);
			if (sr != null && sr.configCheck() && sr.initNode()) {
				sr.begin();
			}
		}
		
		} catch (Throwable t) {
			Log.fatal("CRASH", t);
			System.exit(-1);
		}
		// main thread ends after startup.
	}

	private static Options buildArgumentOptions() {
		Options options = new Options();

		options.addOption("v", "version", false, "print Version number and exit");
		options.addOption("h", "help", false, "print this message");
		options.addOption("cc", "configCheck", false, "run some basic sanity checks on the configuration file");
		Option opt = new Option("c", "configFile", true, "path to the configuration XML file");
		opt.setArgName("PATH");
		options.addOption(opt);
		opt = new Option("n", "nodeName", true, "give this instance a name");
		opt.setArgName("NAME");
		options.addOption(opt);
		opt = new Option("g", "nodeGroup", true, "the run mode to use, as described in the config file");
		opt.setArgName("GROUP");
		options.addOption(opt);
		opt = new Option("x", "command", true, "execute the specified Java method after startup");
		opt.setArgName("METHOD");
		options.addOption(opt);

		return options;
	}

	private static ServiceRegister runStartup(Options options, CommandLine line) {
		ServiceRegister sr = new ServiceRegister();
		Configuration config = new Configuration(sr);
		if (config.parseArgs(options, line)) {
			sr.setConfiguration(config);
			msh = new MainShutdownHandler(sr);
			Runtime.getRuntime().addShutdownHook(msh);
			return sr;
		} else {
			return null;
		}
	}

	public static void runHelp(Options opts) {
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
	 * @author skander
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
	 * Begin a controlled shutdown procedure, where all running Services will be asked to shutdown in orderly fashion. 
	 */
	public static void exit() {
		ServiceRegister.getInstance().shutdown();
		System.exit(0);
	}
	
	/**
	 * Exit immediately, don't run shutdown hooks. This is obviously something that should never be done, unless you're purposely trying to break stuff.
	 */
	public static void crash() {
		Runtime.getRuntime().removeShutdownHook(msh);
		System.exit(-1);
	}
}
