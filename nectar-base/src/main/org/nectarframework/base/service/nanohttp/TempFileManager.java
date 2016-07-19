package org.nectarframework.base.service.nanohttp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.nectarframework.base.service.log.Log;

/**
 * Default strategy for creating and cleaning up temporary files.
 * <p/>
 * <p>
 * This class stores its files in the standard location (that is, wherever
 * <code>java.io.tmpdir</code> points to). Files are added to an internal list,
 * and deleted when no longer needed (that is, when <code>clear()</code> is
 * invoked at the end of processing a request).
 * </p>
 */
public class TempFileManager {

	private final File tmpdir;

	private final List<TempFile> tempFiles;

	public TempFileManager() {
		this.tmpdir = new File(System.getProperty("java.io.tmpdir"));
		if (!tmpdir.exists()) {
			tmpdir.mkdirs();
		}
		this.tempFiles = new ArrayList<TempFile>();
	}

	
	public void clear() {
		for (TempFile file : this.tempFiles) {
			try {
				file.delete();
			} catch (Exception ignored) {
				Log.warn("could not delete file ", ignored);
			}
		}
		this.tempFiles.clear();
	}

	
	public TempFile createTempFile(String filename_hint) throws Exception {
		TempFile tempFile = new TempFile(this.tmpdir);
		this.tempFiles.add(tempFile);
		return tempFile;
	}
}