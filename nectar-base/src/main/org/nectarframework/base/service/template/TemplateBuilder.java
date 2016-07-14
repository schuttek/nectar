package org.nectarframework.base.service.template;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jdom2.JDOMException;
import org.nectarframework.base.Main;
import org.nectarframework.base.config.Configuration;
import org.nectarframework.base.service.ServiceRegister;
import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.service.xml.Element;
import org.nectarframework.base.service.xml.XmlService;
import org.xml.sax.SAXException;

public class TemplateBuilder {

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

			// startup
			if (line.hasOption("version")) {
				runVersion();
			} else if (line.hasOption("help")) {
				runHelp(options);
			} else if (line.hasOption("configDir") && line.hasOption("output")&& line.hasOption("nodeName")&& line.hasOption("nodeGroup")) {
				String configDir = line.getOptionValue("configDir");
				String outputDir = line.getOptionValue("output");
				String nodeName = line.getOptionValue("nodeName");
				String nodeGroup = line.getOptionValue("nodeGroup");

				if (init(configDir, nodeName, nodeGroup)) {
					try {
						run(configDir, outputDir);
					} catch (Exception e) {
						Log.fatal(e);
					}
				}
			} else {
				runHelp(options);
			}

		} catch (Throwable t) {
			Log.fatal("CRASH", t);
			System.exit(-1);
		}
		System.exit(0);
	}

	private static void run(String pathConfig, String outputDir) throws IOException, TemplateParseException, SAXException, ParserConfigurationException, JDOMException {
		TemplateService ts = (TemplateService)ServiceRegister.getService(TemplateService.class);
		Log.info("TemplateBuilder: building Templates...");
		ts.buildTemplates(pathConfig, outputDir);
	}

	private static boolean init(String configDir, String nodeName, String nodeGroup) {
		ServiceRegister sr = new ServiceRegister();
		Configuration config = new Configuration(sr);
		config.setNodeName(nodeName);
		config.setNodeGroup(nodeGroup);

		Element configElement;
		try {
			configElement = XmlService.fromXml(new File(configDir + "/config.xml"));
		} catch (SAXException | IOException e) {
			Log.fatal(e);
			return false;
		}
		
		sr.setConfiguration(config);
		sr.setConfigElement(configElement);
		return (sr.configCheck() && sr.initNode() && sr.begin());
	}

	private static Options buildArgumentOptions() {
		Options options = new Options();

		options.addOption("h", "help", false, "print this message");
		Option opt = new Option("c", "configDir", true, "path to the config directory");
		opt.setArgName("CONFIGDIR");
		options.addOption(opt);
		opt = new Option("o", "output", true, "output directory");
		opt.setArgName("OUTPUT");
		options.addOption(opt);
		opt = new Option("n", "nodeName", true, "give this instance a name");
		opt.setArgName("NAME");
		options.addOption(opt);
		opt = new Option("g", "nodeGroup", true, "the run mode to use, as described in the config file");
		opt.setArgName("GROUP");
		options.addOption(opt);
		return options;
	}

	public static void runHelp(Options opts) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java nectar.base.service.template.TemplateBuilder", opts);
	}

	private static void runVersion() {
		System.out.println("Nectar Web Platform DataStoreObject Builder");
		System.out.println("Version: " + Main.VERSION);
	}
}
