/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 * 
 * IDLHomeReader - Read home IDL
 *
 * Authors:
 *   Xiaohu <xiaohu417@gmail.com>, 2013.6.11, hefei
 */
package com.cansiny.eform;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;


public class IDLIndexReader
{
	public int table_columns;
	public int table_top_margin;
	public int table_xpad;
	public int table_ypad;
	public int button_icon_size;
	public int button_text_size;
	public ArrayList<IDLIndexItem> items;

	public IDLIndexReader() {
		table_columns = 5;
		table_top_margin = 30;
		table_xpad = 30;
		table_ypad = 30;
		button_icon_size = 48;
		button_text_size = 32;
		items = new ArrayList<IDLIndexItem>();
	}
	
	public void parse(InputStream inputStream) throws SAXException, IOException {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			IDLIndexHandler handler = new IDLIndexHandler(this);
			parser.parse(inputStream, handler);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			throw new SAXException("Configure XML parser error");
		}
	}
	

	/**
	 * class for hold index item information.
	 */
	public class IDLIndexItem
	{
		public String name;
		public String text;
	}

	/**
	 * XML event processor
	 */
	private class IDLIndexHandler extends DefaultHandler
	{
		private IDLIndexReader reader;
		private int version;
		private int level;
		private boolean inside_config;
		private boolean inside_items;

		public IDLIndexHandler(IDLIndexReader reader) {
			this.reader = reader;
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
			if (reader.items.size() == 0)
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
				reader.table_columns = Integer.parseInt(value);
				if (reader.table_columns <= 0) {
					throw new SAXException(String.format("Table columns '%d' too small",
							reader.table_columns));
				}
			}

			value = attrs.getValue("", "top_margin");
			if (value == null) {
				Log.w("IDLIndexReader",
					"Element 'table' missing 'top_margin' attribut, will use default value");
			} else {
				reader.table_top_margin = Integer.parseInt(value);
				if (reader.table_top_margin <= 0) {
					throw new SAXException(String.format("Table top margin '%d' too small",
							reader.table_top_margin));
				}
			}

			value = attrs.getValue("", "xpad");
			if (value == null) {
				Log.w("IDLIndexReader",
					"Element 'table' missing 'xpad' attribut, will use default value");
			} else {
				reader.table_xpad = Integer.parseInt(value);
				if (reader.table_xpad <= 0) {
					throw new SAXException(String.format("Table xpad '%d' too small",
							reader.table_xpad));
				}
			}

			value = attrs.getValue("", "ypad");
			if (value == null) {
				Log.w("IDLIndexReader",
					"Element 'table' missing 'ypad' attribut, will use default value");
			} else {
				reader.table_ypad = Integer.parseInt(value);
				if (reader.table_ypad <= 0) {
					throw new SAXException(String.format("Table ypad '%d' too small",
							reader.table_ypad));
				}
			}
		}
		
		private void parseConfigButton(Attributes attrs) throws SAXException {
			String value = attrs.getValue("", "icon_size");
			if (value == null) {
				Log.w("IDLIndexReader",
					"Element 'button' missing 'icon_size' attribute, will use default value");
			} else {
				reader.button_icon_size = Integer.parseInt(value);
				if (reader.button_icon_size <= 0) {
					throw new SAXException(String.format("Button icon size '%d' too small",
							reader.button_icon_size));
				}
			}

			value = attrs.getValue("", "text_size");
			if (value == null) {
				Log.w("IDLIndexReader",
					"Element 'button' missing 'text_size' attribute, will use default value");
			} else {
				reader.button_text_size = Integer.parseInt(value);
				if (reader.button_text_size <= 0) {
					throw new SAXException(String.format("Button text size '%d' too small",
							reader.button_text_size));
				}
			}
		}
		
		private void parseItemsItem(Attributes attrs) throws SAXException {
			IDLIndexItem item = new IDLIndexItem();

			item.name = attrs.getValue("", "name");
			if (item.name == null)
				throw new SAXException("Element 'item' missing attribute 'name'");

			for (IDLIndexItem i : reader.items) {
				if (i.name.equalsIgnoreCase(item.name)) {
					Log.w("IDLIndexReader",
						String.format("Item '%s' already exists, skip repeated item", i.name));
					return;
				}
			}
			item.text = attrs.getValue("", "text");
			if (item.text == null)
				throw new SAXException("Element 'item' missing attribute 'text'");

			reader.items.add(item);
		}
	}
}
