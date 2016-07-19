package org.nectarframework.base.service.nanohttp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Default strategy for creating and cleaning up temporary files.
 * <p/>
 * <p>
 * By default, files are created by <code>File.createTempFile()</code> in the
 * directory specified.
 * </p>
 */
public class TempFile {

	private final File file;

	private final OutputStream fstream;

	public TempFile(File tempdir) throws IOException {
		this.file = File.createTempFile("NanoHTTPD-", "", tempdir);
		this.fstream = new FileOutputStream(this.file);
	}

	public void delete() throws Exception {
		Utils.safeClose(this.fstream);
		if (!this.file.delete()) {
			throw new Exception("could not delete temporary file: " + this.file.getAbsolutePath());
		}
	}

	
	public String getName() {
		return this.file.getAbsolutePath();
	}

	
	public OutputStream open() throws Exception {
		return this.fstream;
	}
}