package org.nectarframework.base.service.file;

import java.io.ByteArrayInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Optional;

import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.exception.ServiceUnavailableException;
import org.nectarframework.base.service.Log;
import org.nectarframework.base.service.Service;
import org.nectarframework.base.service.ServiceParameters;
import org.nectarframework.base.service.cache.CacheService;
import org.nectarframework.base.service.cache.CacheableObject;

import static java.nio.file.StandardCopyOption.*;

//TODO add cache layer

public class FileService extends Service {

	private String rootDirectory;
	private int totalFileCacheSize;
	private int maxFilesInCache;
	private int maxCachedFileSize;

	private boolean recheckLastModified = true;

	private CacheService cacheService;
	private File tempDirectory;
	private ArrayList<File> tempFilesList;

	private static final int maxReadBufferSize = 10485760; // 10MB
	private static final int maxTotalFileCacheSize = Integer.MAX_VALUE; // 2GB
	private static final int maxMaxFilesInCache = 50000;
	private static final int maxMaxCachedFileSize = Integer.MAX_VALUE; // 2GB

	private static final int defaultReadBufferSize = 65536; // 64KB
	private static final int defaultTotalFileCacheSize = 104857600; // 100MB
	private static final int defaultMaxFilesInCache = 1000;
	private static final int defaultMaxCachedFileSize = 134217728; // 128KB

	@Override
	protected boolean init() {
		boolean b = true;
		b &= initTempFiles();
		return b;
	}

	@Override
	protected boolean run() {
		return true;
	}

	@Override
	protected boolean shutdown() {
		boolean b = true;
		b &= shutdownTempFiles();
		return b;
	}


	@Override
	public void checkParameters(ServiceParameters sp) throws ConfigurationException {
		String rootDir = sp.getString("rootDirectory", "files");
		if (rootDir != null) {
			File rootDirFile = new File(rootDir);
			if (!rootDirFile.exists()) {
				throw new ConfigurationException("FileService: rootDirectory couldn't be found.");
			}
			this.rootDirectory = rootDirFile.getAbsolutePath();
		}

		totalFileCacheSize = sp.getInt("totalFileCacheSize", -1, maxTotalFileCacheSize,
				defaultTotalFileCacheSize);
		maxFilesInCache = sp.getInt("maxFilesInCache", -1, maxMaxFilesInCache, defaultMaxFilesInCache);
		maxCachedFileSize = sp.getInt("maxCachedFileSize", -1, maxMaxCachedFileSize,
				defaultMaxCachedFileSize);
		recheckLastModified = sp.getBoolean("recheckLastModified", true);
	}

	@Override
	public boolean establishDependencies() throws ServiceUnavailableException {
		cacheService = (CacheService) this.dependency(CacheService.class);
		return true;
	}

	public long getFileContentLength(String filename) {
		File f = new File(rootDirectory + "/" + filename);
		long contentLength = f.length();
		return contentLength;
	}

	private void testFile(File f)
			throws ReadFileNotFoundException, ReadFileAccessDeniedException, ReadFileNotAFileException {
		if (!f.exists()) {
			Log.trace(f.getAbsolutePath());
			throw new ReadFileNotFoundException();
		}
		if (!f.canRead() && !f.isHidden()) {
			throw new ReadFileAccessDeniedException();
		}
		// check that we're not going above the root directory.
		if (!f.getAbsolutePath().startsWith(rootDirectory)) {
			throw new ReadFileAccessDeniedException();
		}
		if (!f.isFile()) {
			throw new ReadFileNotAFileException();
		}
	}

	public InputStream getFileAsInputStream(String path, long cacheExpiry) throws IOException {
		// attempt a cache hit
		Optional<CacheableObject> cachedCO = null;
		try {
			cachedCO = cacheService.getObject(this, cacheKey(path), true);
		} catch (Exception e) {
			Log.warn(e);
		}

		FileInfo fi = null;
		if (cachedCO.isPresent()) {
			fi = (FileInfo) cachedCO.get();
			if (this.recheckLastModified) {
				// cache is out of date
				if (fi.lastModified < getFileInfo(path).lastModified) {
					cacheService.remove(this, cacheKey(path));
					fi = null;
				}
			}
		}

		if (fi == null) {
			fi = getFileInfo(path, cacheExpiry);
		}
		if (fi.contents == null) {
			if (fi.length <= this.maxCachedFileSize) {
				fi.contents = Files.readAllBytes(fi.getFile().toPath());
				cacheService.set(this, cacheKey(fi.getPath()), fi, cacheExpiry);
				return new ByteArrayInputStream(fi.contents);
			} else {
				return new FileInputStream(fi.getFile());
			}
		} else {
			return new ByteArrayInputStream(fi.contents);
		}
	}

	public File getFile(String path)
			throws ReadFileNotFoundException, ReadFileAccessDeniedException, ReadFileNotAFileException {
		File f = new File(rootDirectory + "/" + path);
		testFile(f);
		return f;
	}

	public FileInfo getFileInfo(String path)
			throws ReadFileNotFoundException, ReadFileAccessDeniedException, ReadFileNotAFileException {
		return getFileInfo(path, -1);
	}

	public FileInfo getFileInfo(String path, long cacheExpiry)
			throws ReadFileNotFoundException, ReadFileAccessDeniedException, ReadFileNotAFileException {

		FileInfo fi = new FileInfo();

		File f = new File(rootDirectory + "/" + path);
		testFile(f);

		fi.path = rootDirectory + "/" + path;
		fi.name = f.getName();
		fi.extension = "";

		int i = fi.name.lastIndexOf('.');
		if (i > 0) {
			fi.extension = fi.name.substring(i + 1);
		}
		fi.lastModified = f.lastModified();
		fi.length = f.length();

		cacheService.set(this, cacheKey(path), fi, cacheExpiry);

		return fi;
	}

	public byte[] readAllBytes(String path, long cacheExpiry) throws IOException {

		// attempt a cache hit
		Optional<CacheableObject> cachedCO = null;
		try {
			cachedCO = cacheService.getObject(this, cacheKey(path), true);
		} catch (Exception e) {
			Log.warn(e);
		}

		FileInfo fi = null;
		if (cachedCO.isPresent()) {
			fi = (FileInfo) cachedCO.get();
			if (this.recheckLastModified) {
				// cache is out of date
				if (fi.lastModified < getFileInfo(path).lastModified) {
					cacheService.remove(this, cacheKey(path));
					fi = null;
				}
			}
		}

		if (fi == null) {
			fi = getFileInfo(path, cacheExpiry);
		}
		if (fi.contents == null) {

			fi.contents = Files.readAllBytes(fi.getFile().toPath());
			if (fi.length <= this.maxCachedFileSize) {
				cacheService.set(this, cacheKey(fi.getPath()), fi, cacheExpiry);
			}
		}
		return fi.contents;
	}

	protected String cacheKey(String path) {
		return "FileService:" + path;
	}

	public void append(String path, byte[] ba) throws IOException {
		FileOutputStream fos = new FileOutputStream(new File(rootDirectory + "/" + path), true);
		fos.write(ba);
		fos.close();

		cacheService.remove(this, cacheKey(path));
	}

	public void write(String path, byte[] ba) throws IOException {
		FileOutputStream fos = new FileOutputStream(new File(rootDirectory + "/" + path), false);
		fos.write(ba);
		fos.close();

		cacheService.remove(this, cacheKey(path));
	}

	public void replace(String path, byte[] ba) throws IOException {
		write(path + ".partial", ba);
		Files.move(new File(path + ".partial").toPath(), new File(path).toPath(), REPLACE_EXISTING, ATOMIC_MOVE);
		cacheService.remove(this, cacheKey(path));
	}



	private boolean initTempFiles() {
		this.tempDirectory = new File(System.getProperty("java.io.tmpdir"));
		if (!tempDirectory.exists()) {
			tempDirectory.mkdirs();
		}
		this.tempFilesList = new ArrayList<File>();
		return true;
	}

	private boolean shutdownTempFiles() {
		for (File file : this.tempFilesList) {
			try {
				file.delete();
			} catch (Exception e) {
				Log.warn("could not delete file ", e);
			}
		}
		this.tempFilesList.clear();
		return false;
	}


	public File createTempFile(String filename_hint) throws Exception {
		File f = File.createTempFile("Nectar-", "", this.tempDirectory);
		this.tempFilesList.add(f);
		return f;
	}
	
}
