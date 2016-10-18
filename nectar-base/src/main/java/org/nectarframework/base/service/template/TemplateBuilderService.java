package org.nectarframework.base.service.template;

import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.exception.ServiceUnavailableException;
import org.nectarframework.base.service.Service;
import org.nectarframework.base.service.ServiceParameters;
import org.nectarframework.base.service.ServiceRegister;
import org.nectarframework.base.service.log.Log;

public class TemplateBuilderService extends Service {

	private String pathConfig;
	private String outputDir;

	@Override
	public void checkParameters(ServiceParameters sp) throws ConfigurationException {
		outputDir = sp.getString("outputDir", "src");
		pathConfig = sp.getString("pathConfig", "config/pathConfig.xml");
	}

	@Override
	public boolean establishDependencies() throws ServiceUnavailableException {
		dependency(CompiledTemplateService.class);
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
