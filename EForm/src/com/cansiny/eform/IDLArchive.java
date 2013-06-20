/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 * 
 * IDLArchive - IDL Archive reader
 *
 * Authors:
 *   Xiaohu <xiaohu417@gmail.com>, 2013.6.11, hefei
 */
package com.cansiny.eform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * Read data from IDL archive(a Zip archive).
 */
public class IDLArchive
{
	static private final String IDL_ARCHIVE_NAME = "eform.idl.zip";
	private ZipFile zfile;
	public IDLArchiveInfo info;
	public IDLArchiveIndex index;

	static private IDLArchive _singleInstance = null;
	static private Activity _currentActivity = null;
	static public IDLArchive getIDLArchive(Activity activity)
			throws IOException, SAXException {
		if (_singleInstance == null) {
			Context context = HomeActivity.getAppContext();
			_singleInstance = new IDLArchive(context.getFilesDir());
		}
		_currentActivity = activity;
		return _singleInstance;
	}

	private IDLArchive(File basepath) throws IOException, SAXException {
		zfile = new ZipFile(new File(basepath, IDL_ARCHIVE_NAME), ZipFile.OPEN_READ);
		refresh();
	}

	public void refresh() throws IOException, SAXException {
		info = new IDLArchiveInfo();
		index = new IDLArchiveIndex();

		/* read info.idl.xml */
		String path = buildStreamPath(true, "info.idl.xml");
		ZipEntry entry = zfile.getEntry(path);
		if (entry == null)
			throw new IOException(String.format("File '%s' not found", path));
		InputStream stream = zfile.getInputStream(entry);
		IDLArchiveInfoReader info_reader = new IDLArchiveInfoReader();
		info_reader.parse(stream);
		stream.close();

		/* read index.idl.xml */
		path = buildStreamPath(true, info.customer_name, "index.idl.xml");
		entry = zfile.getEntry(path);
		if (entry == null)
			throw new IOException(String.format("File '%s' not found", path));
		stream = zfile.getInputStream(entry);
		IDLArchiveIndexReader index_reader = new IDLArchiveIndexReader();
		index_reader.parse(stream);
		stream.close();
	}

	public void close() throws IOException {
		if (zfile != null) {
			_singleInstance = null;
			_currentActivity = null;
			zfile.close();
		}
	}

	private String buildStreamPath(boolean with_root, String... args) {
		String basepath = "";
		if (with_root)
			basepath += "idl";

		for (String arg : args) {
			if (basepath.length() > 0)
				basepath += File.separator;
			basepath += arg;
		}
		return basepath;
	}

	private InputStream getCustomerInputStream(String filename) throws IOException {
		if (zfile == null)
			throw new IOException("IDL Archive not open yet");

		String path = buildStreamPath(true, info.customer_name, filename);

		ZipEntry entry = zfile.getEntry(path);
		if (entry == null)
			throw new IOException(String.format("File '%s' not found", path));

		return zfile.getInputStream(entry);
	}

	/* get a icon stream for current customer */
	public InputStream getImageInputStream(String name) throws IOException {
		final String[] suffixes = { ".png", ".jpg", ".jpeg" };
		for (String suffix : suffixes) {
			try {
				String path = buildStreamPath(false, "images", name + suffix);
				return getCustomerInputStream(path);
			} catch (IOException e) {
				if (BuildConfig.DEBUG)
					e.printStackTrace();
				continue;
			}
		}
		/* icon file not found */
		throw new IOException(
			String.format("No corresponding icon can be found for '%s'", name)
		);
	}

	public Drawable getImageDrawable(String name) throws IOException {
		InputStream stream = getImageInputStream(name);
		Drawable drawable = Drawable.createFromStream(stream, name);
		stream.close();
		return drawable;
	}

	public InputStream getLayoutInputStream(String name) throws IOException {
		String path = buildStreamPath(false, "layout", name + ".idl.xml");
		return getCustomerInputStream(path);
	}

	public Drawable getLogoDrawable() throws IOException {
		try {
			return getImageDrawable(info.logo);
		} catch (IOException e) {
			if (BuildConfig.DEBUG)
				e.printStackTrace();
			/* feedback to default logo */
			AssetManager assets = _currentActivity.getAssets();
			InputStream stream = assets.open("logo.png");
			Drawable drawable = Drawable.createFromStream(stream, "logo.png");
			stream.close();
			assets.close();
			return drawable;
		}
	}

	public Drawable getBackgroundDrawable() throws IOException {
		try {
			return getImageDrawable(info.background);
		} catch (IOException e) {
			if (BuildConfig.DEBUG)
				e.printStackTrace();
			/* feedback to default background */
			AssetManager assets = _currentActivity.getAssets();
			InputStream stream = assets.open("background.png");
			Drawable drawable = Drawable.createFromStream(stream, "background.png");
			stream.close();
			assets.close();
			return drawable;
		}
	}


	/**
	 * A class hold global summary information.
	 * So we can customize our home interface with these information
	 */
	public class IDLArchiveInfo
	{
		public String customer_name;
		public String logo;
		public String background;
		public String flash;
	}

	
	/**
	 * Parse info.idl.xml
	 * Result will be save to IDLArchive object.
	 */
	private class IDLArchiveInfoReader
	{
		public IDLArchiveInfoReader() {
		}

		public void parse(InputStream inputStream) throws SAXException, IOException {
			try {
				SAXParserFactory factory = SAXParserFactory.newInstance();
				SAXParser parser = factory.newSAXParser();
				IDLInfoHandler handler = new IDLInfoHandler();
				parser.parse(inputStream, handler);
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
				throw new SAXException("Configure XML parser error");
			}
		}

		/**
		 * XML event processor
		 */
		private class IDLInfoHandler extends DefaultHandler
		{
			private int version;
			private int level;
			
			public IDLInfoHandler() {
				version = 1;
			}

			@Override
			public void startDocument() throws SAXException {
				level = 0;
			}
			
			@Override
			public void endDocument() throws SAXException {
				if (info.customer_name == null) {
					throw new SAXException("Customer name unknown, may be missing " +
							"element 'customer' or missing attribute 'name'");
				}
				
				/* set default value when them is missing. */
				if (info.logo == null || info.logo.trim().length() == 0)
					info.logo = "logo";
				if (info.background == null || info.background.trim().length() == 0)
					info.background = "background";
				if (info.flash == null || info.flash.trim().length() == 0)
					info.flash = "flash";
			}
			
			@Override
			public void startElement(String uri, String localName, String qName, Attributes attrs)
					throws SAXException {
				if (level == 0) {
					if (!localName.equalsIgnoreCase("info"))
						throw new SAXException("The root element must be 'info");

					String value = attrs.getValue("", "version");
					if (value != null)
						version = Integer.parseInt(value);
					if (version < 1)
						throw new SAXException(String.format("Version '%s' is not recognizable", value));
				}

				level++;

				if (localName.equalsIgnoreCase("customer")) {
					if (level != 2)
						throw new SAXException("Element 'customer' must be child of root");
					parseCustomer(attrs);
				}
			}
			
			@Override
	        public void endElement(String uri, String localName, String qName) throws SAXException {
				level--;
			}

			@Override
			public void characters(char[] ch, int start, int length) throws SAXException {
			}
			
			private void parseCustomer(Attributes attrs) throws SAXException {
				info.customer_name = attrs.getValue("", "name");
				if (info.customer_name == null)
					throw new SAXException("Element 'customer' missing 'name' attribute");

				info.logo = attrs.getValue("", "logo");
				if (info.customer_name == null)
					throw new SAXException("Element 'customer' missing 'logo' attribute");

				info.background = attrs.getValue("", "background");
				if (info.customer_name == null)
					throw new SAXException("Element 'customer' missing 'background' attribute");

				info.flash = attrs.getValue("", "flash");
				if (info.customer_name == null)
					throw new SAXException("Element 'customer' missing 'flash' attribute");
			}
		}

	}


	/**
	 * A class hold customer contents information.
	 * A archive can contains multiple contents information, but only one will
	 * be uses at time. this class hold current active contents information.
	 */
	public class IDLArchiveIndex
	{
		public int table_columns;
		public int table_top_margin;
		public int table_xpad;
		public int table_ypad;
		public int button_icon_size;
		public int button_text_size;
		public ArrayList<IDLArchiveIndexItem> items;

		public IDLArchiveIndex() {
			table_columns = 5;
			table_top_margin = 30;
			table_xpad = 30;
			table_ypad = 30;
			button_icon_size = 48;
			button_text_size = 32;
			items = new ArrayList<IDLArchiveIndexItem>();
		}
	}


	/**
	 * class for hold index item information.
	 */
	public class IDLArchiveIndexItem
	{
		public String name;
		public String text;
	}


	/**
	 * Parse index.idl.xml
	 * Result will be save to IDLArchive object.
	 */
	private class IDLArchiveIndexReader
	{
		public IDLArchiveIndexReader() {
		}
		
		public void parse(InputStream inputStream) throws SAXException, IOException {
			try {
				SAXParserFactory factory = SAXParserFactory.newInstance();
				SAXParser parser = factory.newSAXParser();
				IDLArchiveIndexHandler handler = new IDLArchiveIndexHandler();
				parser.parse(inputStream, handler);
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
				throw new SAXException("Configure XML parser error");
			}
		}
		

		/**
		 * XML event processor
		 */
		private class IDLArchiveIndexHandler extends DefaultHandler
		{
			private int version;
			private int level;
			private boolean inside_config;
			private boolean inside_items;

			public IDLArchiveIndexHandler() {
				version = 1;
			}

			@Override
			public void startDocument() throws SAXException {
				level = 0;
				inside_config = false;
				inside_items = false;
			}
			
			@Override
			public void endDocument() throws SAXException {
				if (index.items.size() == 0)
					throw new SAXException("No item found, please check your configure");
			}
			
			@Override
			public void startElement(String uri, String localName, String qName, Attributes attrs)
					throws SAXException {
				if (level == 0) {
					if (!localName.equalsIgnoreCase("index"))
						throw new SAXException("Root element must be 'index'");
					String value = attrs.getValue("", "version");
					if (value != null)
						version = Integer.parseInt(value);
					if (version < 1)
						throw new SAXException(String.format("Version '%s' is not recognizable", value));
				}

				level++;
				
				if (localName.equalsIgnoreCase("config")) {
					if (level != 2)
						throw new SAXException("Elements 'config' must be child of root element");
					inside_config = true;
				} else if (localName.equalsIgnoreCase("table")) {
					if (level != 3 || !inside_config)
						throw new SAXException("Elements 'table' must be child of 'config'");
					parseConfigTable(attrs);
				} else if (localName.equalsIgnoreCase("button")) {
					if (level != 3 || !inside_config)
						throw new SAXException("Elements 'button' must be child of 'config'");
					parseConfigButton(attrs);
				} else if (localName.equalsIgnoreCase("items")) {
					if (level != 2)
						throw new SAXException("Elements 'items' must be child of root element");
					inside_items = true;
				} else if (localName.equalsIgnoreCase("item")) {
					if (level != 3 || !inside_items)
						throw new SAXException("Element 'item' must be child of 'items'");
					this.parseItemsItem(attrs);
				}
			}
			
			@Override
	        public void endElement(String uri, String localName, String qName) throws SAXException {
				level--;

				if (localName.equalsIgnoreCase("config")) {
					inside_config = false;
				} else if (localName.equalsIgnoreCase("items")) {
					inside_items = false;
				}
			}
			
			@Override
			public void characters(char[] ch, int start, int length) throws SAXException {
				
			}
			
			private void parseConfigTable(Attributes attrs) throws SAXException {
				String value = attrs.getValue("", "columns");
				if (value == null) {
					Log.w("IDLIndexReader",
						"Element 'table' missing 'columns' attribut, will use default value");
				} else {
					index.table_columns = Integer.parseInt(value);
					if (index.table_columns <= 0) {
						throw new SAXException(String.format("Table columns '%d' too small",
								index.table_columns));
					}
				}

				value = attrs.getValue("", "top_margin");
				if (value == null) {
					Log.w("IDLIndexReader",
						"Element 'table' missing 'top_margin' attribut, will use default value");
				} else {
					index.table_top_margin = Integer.parseInt(value);
					if (index.table_top_margin <= 0) {
						throw new SAXException(String.format("Table top margin '%d' too small",
								index.table_top_margin));
					}
				}

				value = attrs.getValue("", "xpad");
				if (value == null) {
					Log.w("IDLIndexReader",
						"Element 'table' missing 'xpad' attribut, will use default value");
				} else {
					index.table_xpad = Integer.parseInt(value);
					if (index.table_xpad <= 0) {
						throw new SAXException(String.format("Table xpad '%d' too small",
								index.table_xpad));
					}
				}

				value = attrs.getValue("", "ypad");
				if (value == null) {
					Log.w("IDLIndexReader",
						"Element 'table' missing 'ypad' attribut, will use default value");
				} else {
					index.table_ypad = Integer.parseInt(value);
					if (index.table_ypad <= 0) {
						throw new SAXException(String.format("Table ypad '%d' too small",
								index.table_ypad));
					}
				}
			}
			
			private void parseConfigButton(Attributes attrs) throws SAXException {
				String value = attrs.getValue("", "icon_size");
				if (value == null) {
					Log.w("IDLIndexReader",
						"Element 'button' missing 'icon_size' attribute, will use default value");
				} else {
					index.button_icon_size = Integer.parseInt(value);
					if (index.button_icon_size <= 0) {
						throw new SAXException(String.format("Button icon size '%d' too small",
								index.button_icon_size));
					}
				}

				value = attrs.getValue("", "text_size");
				if (value == null) {
					Log.w("IDLIndexReader",
						"Element 'button' missing 'text_size' attribute, will use default value");
				} else {
					index.button_text_size = Integer.parseInt(value);
					if (index.button_text_size <= 0) {
						throw new SAXException(String.format("Button text size '%d' too small",
								index.button_text_size));
					}
				}
			}
			
			private void parseItemsItem(Attributes attrs) throws SAXException {
				IDLArchiveIndexItem item = new IDLArchiveIndexItem();

				item.name = attrs.getValue("", "name");
				if (item.name == null)
					throw new SAXException("Element 'item' missing attribute 'name'");

				for (IDLArchiveIndexItem i : index.items) {
					if (i.name.equalsIgnoreCase(item.name)) {
						Log.w("IDLIndexReader",
							String.format("Item '%s' already exists, skip repeated item", i.name));
						return;
					}
				}
				item.text = attrs.getValue("", "text");
				if (item.text == null)
					throw new SAXException("Element 'item' missing attribute 'text'");

				index.items.add(item);
			}
		}
	}

}
