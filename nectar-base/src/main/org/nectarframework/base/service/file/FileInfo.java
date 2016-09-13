package org.nectarframework.base.service.file;

import java.io.File;

public class FileInfo {

	String path;
	String name;
	long lastModified;
	long length;
	String extension;
	byte[] contents;

	public String getPath() {
		return path;
	}
	public String getName() {
		return name;
	}
	public long getLastModified() {
		return lastModified;
	}
	public long getLength() {
		return length;
	}
	public String getFileExtension() {
		return extension;
	}
	public File getFile() {
		return new File(path);
	}
}
