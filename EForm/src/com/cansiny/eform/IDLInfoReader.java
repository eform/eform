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
	private int customer_id;

	public IDLInfoReader() {
		customer_id = 0;
	}

	/* parse index.idl.xml */
	public void parse(InputStream inputStream) throws SAXException, IOException {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			IDLInfoHandler handler = new IDLInfoHandler(this);
			parser.parse(inputStream, handler);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			throw new SAXException("Configure XML parser error");
		}
	}
	
	public int getCustomerID() {
		return customer_id;
	}

	/**
	 * XML event processor
	 */
	private class IDLInfoHandler extends DefaultHandler
	{
		private int level;
		private boolean customer_appear;
		private IDLInfoReader reader;
		
		public IDLInfoHandler(IDLInfoReader reader) {
			this.reader = reader;
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
					reader.customer_id = Integer.parseInt(value);
					if (reader.customer_id <= 0)
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
