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
	private String customer_name;

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
		customer_name = null;
	}

	public void close() throws IOException {
		if (zfile != null) {
			zfile.close();
			_singleInstance = null;
		}
	}

	private String buildStreamPath(String... args) {
		String basepath = "idl";
		for (String arg : args) {
			basepath += File.separator;
			basepath += arg;
		}
		return basepath;
	}

	public void setCustomerName(String name) throws IOException {
		if (zfile == null)
			throw new IOException("Archive not open yet");

		String path = buildStreamPath(name, "index.idl.xml");
		if (zfile.getEntry(path) == null)
			throw new IOException(String.format("Customer '%s' not found", name));

		customer_name = name;
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
		String path = buildStreamPath("info.idl.xml");
		return getInputStream(path);
	}

	
	private InputStream getCustomerInputStream(String path) throws IOException {
		if (customer_name == null)
			throw new IOException("Customer name not set yet");
		return getInputStream(path);
	}

	public InputStream getIndexInputStream() throws IOException {
		String path = buildStreamPath(customer_name, "index.idl.xml");
		return getCustomerInputStream(path);
	}
	
	public InputStream getIconInputStream(String icon_name) throws IOException {
		String path = buildStreamPath(customer_name, "icons", icon_name);
		return getCustomerInputStream(path);
	}
}
