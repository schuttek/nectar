package org.nectarframework.base.service.file;

public class FileInfo {

	String path;
	String name;
	long lastModified;
	long length;
	String extension;

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
}
