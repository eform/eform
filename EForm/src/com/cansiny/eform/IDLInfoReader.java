package com.cansiny.eform;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class IDLInfoReader
{
	private IDLInfo info;

	public IDLInfoReader() {
		info = new IDLInfo();
	}

	/* parse index.idl.xml */
	public void parse(InputStream inputStream) throws SAXException, IOException {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			IDLInfoHandler handler = new IDLInfoHandler(info);
			parser.parse(inputStream, handler);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			throw new SAXException("Configure XML parser error");
		}
	}
	
	public int getCustomerID() {
		return info.customerid;
	}

	/**
	 * A class to hold information
	 */
	private class IDLInfo
	{
		public int customerid;
	}

	/**
	 * XML event processor
	 */
	private class IDLInfoHandler extends DefaultHandler
	{
		private int level;
		private boolean customer_appear;
		private IDLInfo info;
		
		public IDLInfoHandler(IDLInfo info) {
			this.info = info;
		}

		@Override
		public void startDocument() throws SAXException {
			level = 0;
			customer_appear = false;
		}
		
		@Override
		public void endDocument() throws SAXException {
			if (!customer_appear)
				throw new SAXException("Document missing 'customer' element");
		}
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attrs)
				throws SAXException {
			if (level == 0) {
				if (localName != "info")
					throw new SAXException("The root element must be 'info");
			}
			level++;

			if (localName == "customer") {
				String value = attrs.getValue("", "id");
				if (value == null)
					throw new SAXException("Element 'customer' missing 'id' attribute");

				try {
					info.customerid = Integer.parseInt(value);
					if (info.customerid <= 0)
						throw new SAXException("Value of attribute 'customer->id' must large than 0");
				} catch (NumberFormatException e) {
					throw new SAXException("Value of attribute 'customer->id' must be integer");
				}
				customer_appear = true;
			}
		}
		
		@Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
			level--;
		}
		
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			
		}
	}

}
