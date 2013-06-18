/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 * 
 * IDLItemReader - Read a business item IDL
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


public class IDLItemReader
{
	private String name;
	ArrayList<IDLItemPage> pages;

	public IDLItemReader(String name) {
		this.name = name;
		pages = new ArrayList<IDLItemPage>();
	}

	public void parse(InputStream inputStream) throws SAXException, IOException {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			IDLItemHandler handler = new IDLItemHandler(this);
			parser.parse(inputStream, handler);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			throw new SAXException("Configure XML parser error");
		}
	}


	public class IDLItemPage
	{
		public String title;
		public ArrayList<IDLItemEntry> entries;

		public IDLItemPage() {
			entries = new ArrayList<IDLItemEntry>();
		}
	}


	public class IDLItemEntry
	{
		public String id;
		public String klass;
	}


	private class IDLItemHandler extends DefaultHandler
	{
		private IDLItemReader reader;
		private int version;
		private int level;
		private IDLItemPage page;
		
		public IDLItemHandler(IDLItemReader reader) {
			this.reader = reader;
			version = 1;
		}
		
		private void throwSAXException(String message) throws SAXException {
			throw new SAXException(String.format("%.idl.xml: %s", reader.name, message));
		}

		@Override
		public void startDocument() throws SAXException {
			level = 0;
			page = null;
		}
		
		@Override
		public void endDocument() throws SAXException {
			
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attrs)
				throws SAXException {
			if (level == 0) {
				if (!localName.equalsIgnoreCase("item"))
					throwSAXException("Root element must be 'item'");

				String value = attrs.getValue("", "version");
				if (value != null)
					version = Integer.parseInt(value);
				if (version < 1)
					throwSAXException(String.format("Version '%s' is not recognizable", value));
			}
			
			level++;
			
			if (localName.equalsIgnoreCase("page")) {
				if (level != 2)
					this.throwSAXException("Element 'page' must be child of root element");
				parsePage(attrs);
			} else if (localName.equalsIgnoreCase("entry")) {
				if (level != 3 || page == null)
					this.throwSAXException("Element 'entry' must inside 'page' element");
				parseEntry(attrs);
			}

		}
		
		@Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
			if (localName.equalsIgnoreCase("page")) {
				reader.pages.add(page);
				page = null;
			}
		}
		
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
		}
		
		
		private void parsePage(Attributes attrs) throws SAXException {
			page = new IDLItemPage();

			page.title = attrs.getValue("", "title");
			if (page.title == null)
				throwSAXException("Element 'page' missing 'title' attribute");
		}

		private void parseEntry(Attributes attrs) throws SAXException {
			IDLItemEntry entry = new IDLItemEntry();

			entry.id = attrs.getValue("", "id");
			if (entry.id == null)
				throwSAXException("Element 'entry' missing 'id' attribute");
			
			entry.klass = attrs.getValue("", "class");
			if (entry.klass == null)
				throwSAXException("Element 'entry' missing 'class' attribute");
			
			page.entries.add(entry);
		}
	}
}
