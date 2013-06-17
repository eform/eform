package com.cansiny.eform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.content.Context;

/**
 * Read data from IDL archive(a Zip archive).
 */
public class IDLArchive
{
	static private final String IDL_ARCHIVE_NAME = "eform.idl.zip";
	private ZipFile zfile;

	static private IDLArchive _singleInstance = null;
	static public IDLArchive getIDLArchive() throws IOException {
		if (_singleInstance == null) {
			Context context = HomeActivity.getAppContext();
			_singleInstance = new IDLArchive(context.getFilesDir());
		}
		return _singleInstance;
	}

	private IDLArchive(File basepath) throws IOException {
		zfile = new ZipFile(new File(basepath, IDL_ARCHIVE_NAME), ZipFile.OPEN_READ);
	}

	public void close() throws IOException {
		if (zfile != null) {
			zfile.close();
			_singleInstance = null;
		}
	}

	public InputStream getInputStream(String filename) throws IOException {
		if (zfile == null)
			throw new IOException("Archive not open yet");

		ZipEntry entry = zfile.getEntry(filename);
		if (entry == null)
			throw new IOException(String.format("File '%s' not found", filename));

		return zfile.getInputStream(entry);
	}

	public InputStream getInfoInputStream() throws IOException {
		return getInputStream("idl/info.idl.xml");
	}

	public InputStream getIndexInputStream() throws IOException {
		return getInputStream("idl/index.idl.xml");
	}
}
