package org.nectarframework.base.service.file;

import java.io.File;

import org.nectarframework.base.service.cache.CacheableObject;
import org.nectarframework.base.tools.ByteArray;

public class FileInfo implements CacheableObject {

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

	@Override
	public FileInfo fromBytes(ByteArray ba) {
		path = ba.getString();
		name = ba.getString();
		lastModified = ba.getLong();
		length = ba.getLong();
		extension = ba.getString();
		contents = ba.getByteArray();
		return this;
	}

	@Override
	public ByteArray toBytes(ByteArray ba) {
		ba.add(path);
		ba.add(name);
		ba.add(lastModified);
		ba.add(length);
		ba.add(extension);
		ba.addByteArray(contents);
		return ba;
	}
}
