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
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class IDLIndexReader
{
	public IDLIndexReader() {
	}

	/* get a list of all items but without detail */
	public List<IDLItemReader> getItemList() {
		return null;
	}

	/* get detail for a item with special name */
	public IDLItemReader getItem(String name) {
		return null;
	}
	
	/* parse index.idl.xml */
	public void parse(InputStream inputStream)
			throws SAXException, IOException, ParserConfigurationException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		/* configure the xml parser with factory methods. */
		factory.setNamespaceAware(false);
		factory.setXIncludeAware(false);
		factory.setSchema(null);
		factory.setValidating(false);

		/* set a event handler and a input source, then start parse. */
		SAXParser parser = factory.newSAXParser();
		IndexReaderHandler handler = new IndexReaderHandler();
		parser.parse(inputStream, handler);
	}
	

	/**
	 * XML event processor for IDLHomeReader
	 */
	private class IndexReaderHandler extends DefaultHandler
	{
		@Override
		public void startDocument() throws SAXException {
			
		}
		
		@Override
		public void endDocument() throws SAXException {
			
		}
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attrs)
				throws SAXException {
			
		}
		
		@Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
			
		}
		
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			
		}
	}
}
