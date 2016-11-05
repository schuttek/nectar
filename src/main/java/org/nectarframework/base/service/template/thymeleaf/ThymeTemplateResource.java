package org.nectarframework.base.service.template.thymeleaf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.nectarframework.base.service.Log;
import org.thymeleaf.templateresource.ITemplateResource;

public class ThymeTemplateResource implements ITemplateResource {

	private ThymeTemplateResolver ttr;
	private String name;
	private byte[] fileContents;
	
	public ThymeTemplateResource(ThymeTemplateResolver ttr, String name, byte[] fileContents) {
		this.ttr = ttr;
		this.name = name;
		this.fileContents = fileContents;
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public String getBaseName() {
		// TODO Auto-generated method stub
		return name;
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public Reader reader() throws IOException {
		// TODO: not sure if copying this is really necessary, but just to be safe...0
		
		byte[] copy = new byte[fileContents.length]; 
		System.arraycopy(fileContents, 0, copy, 0, fileContents.length);
		ByteArrayInputStream bais = new ByteArrayInputStream(copy);
		
		return new InputStreamReader(bais);
	}

	@Override
	public ITemplateResource relative(String relativeLocation) {
		try {
			return ttr.getTemplateResource(relativeLocation);
		} catch (IOException e) {
			Log.fatal(e);
			return null;
		}
	}

}
