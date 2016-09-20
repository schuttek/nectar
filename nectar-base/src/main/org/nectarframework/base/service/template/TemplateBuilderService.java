package org.nectarframework.base.service.template;

import java.io.File;
import java.io.FileInputStream;
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
import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.service.Service;
import org.nectarframework.base.service.ServiceRegister;
import org.nectarframework.base.service.ServiceUnavailableException;
import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.service.xml.Element;
import org.nectarframework.base.service.xml.XmlService;
import org.xml.sax.SAXException;

public class TemplateBuilderService extends Service {

	private String pathConfig;
	private String outputDir;

	@Override
	public void checkParameters() throws ConfigurationException {
		outputDir = serviceParameters.getString("outputDir", "src");
		pathConfig = serviceParameters.getString("pathConfig", "config/pathConfig.xml");
	}

	@Override
	public boolean establishDependancies() throws ServiceUnavailableException {
		dependancy(CompiledTemplateService.class);
		return true;
	}

	@Override
	protected boolean init() {
		return true;
	}

	@Override
	protected boolean run() {
		CompiledTemplateService ts = (CompiledTemplateService) ServiceRegister
				.getService(CompiledTemplateService.class);
		try {
			ts.buildTemplates(pathConfig, outputDir);
		} catch (Exception e) {
			Log.fatal(e);
		}
		return true;
	}

	@Override
	protected boolean shutdown() {
		// TODO Auto-generated method stub
		return true;
	}
}
