/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 * 
 * IDLItemActivity - Parse IDL item
 *
 * Authors:
 *   Xiaohu <xiaohu417@gmail.com>, 2013.6.18, hefei
 */
package com.cansiny.eform;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class IDLItemActivity extends Activity implements OnClickListener
{
	/* constants uses for communication with previous activity. */
	static public final String INTENT_MESSAGE_NAME = "com.cansiny.eform.NAME";
	static public final String INTENT_RESULT_ERRREASON = "com.cansiny.eform.ERRREASON";

	/* constants uses for identified the difference of event source */
	static private final String VIEW_TAG_STEP_BUTTON = "com.cansiny.eform.STEP_BUTTON";

	private IDLArchive idl_archive;
	private AtomicInteger atomic_int;
	private String name;
	ArrayList<IDLItemPage> pages;

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		pages = new ArrayList<IDLItemPage>();

		atomic_int = new AtomicInteger(HomeActivity.ITEM_VIEW_ID_BASE);
		setContentView(R.layout.activity_item);
		Intent intent = getIntent();

		try {
			idl_archive = IDLArchive.getIDLArchive(this);

			name = intent.getStringExtra(INTENT_MESSAGE_NAME);
			if (name == null) {
				Log.e("IDLItemActivity", "Intent missing 'name' attribute");
				finish();
				return;
			}

			View item_layout = findViewById(R.id.item_layout);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
				item_layout.setBackground(idl_archive.getBackgroundDrawable());
			else
				item_layout.setBackgroundDrawable(idl_archive.getBackgroundDrawable());

			IDLItemReader reader = new IDLItemReader();
			InputStream stream = idl_archive.getLayoutInputStream(name);
			reader.parse(stream);
			stream.close();

			TextView text_view = (TextView) findViewById(R.id.name_textview);
			text_view.setText(name);
			text_view.setTextColor(getResources().getColor(R.color.white));
			text_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);

			ViewGroup step_layout = (ViewGroup) findViewById(R.id.step_layout);

			for (IDLItemPage page : pages) {
				View button = createStepButton(page.title);
				
//				LinearLayout.LayoutParams button_params = new LinearLayout.LayoutParams(
//						ViewGroup.LayoutParams.WRAP_CONTENT,
//						ViewGroup.LayoutParams.WRAP_CONTENT);
//				top_layout.addView(button, button_params);
				step_layout.addView(button);
			}
			resetStepLayout();
			gotoStep(0);
		} catch (IOException e) {
			e.printStackTrace();
			intent.putExtra(INTENT_RESULT_ERRREASON, R.string.error_idl_io);
			setResult(RESULT_CANCELED, intent);
			finish();
		} catch (SAXException e) {
			e.printStackTrace();
			intent.putExtra(INTENT_RESULT_ERRREASON, R.string.error_idl_parse);
			setResult(RESULT_CANCELED, intent);
			finish();
		}
		setResult(RESULT_OK);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d("Des", "destroyed");
	}

	private View createStepButton(String title) {
		LinearLayout button_layout = new LinearLayout(getApplicationContext());
		button_layout.setId(atomic_int.incrementAndGet());
		button_layout.setOrientation(LinearLayout.VERTICAL);
		
		Button button = new Button(getApplicationContext());
		button.setId(atomic_int.incrementAndGet());
		button.setTag(VIEW_TAG_STEP_BUTTON);
		button.setText(title);
		button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
		button.setPadding(4, 2, 4, 2);
		button.setClickable(true);
		button.setOnClickListener(this);

		LinearLayout.LayoutParams button_params = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		button_params.gravity = Gravity.CENTER;
		button_layout.addView(button, button_params);

		ImageView line = new ImageView(getApplicationContext());
		line.setId(atomic_int.incrementAndGet());

		LinearLayout.LayoutParams line_params = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				(int) HomeActivity.convertDpToPixel(6));
		button_layout.addView(line, line_params);

		return button_layout;
	}

	private void resetStepLayout() {
		LinearLayout step_layout = (LinearLayout) findViewById(R.id.step_layout);
		for (int i = 0; i < step_layout.getChildCount(); i++) {
			LinearLayout button_layout = (LinearLayout) step_layout.getChildAt(i);

			Button button = (Button) button_layout.getChildAt(0);
			button.setBackgroundResource(R.color.translucence);
			button.setTextColor(getResources().getColor(R.color.white));
			
			ImageView line = (ImageView) button_layout.getChildAt(1);
			line.setBackgroundResource(R.color.blue);
		}
	}

	private void gotoStep(int index) {
		resetStepLayout();

		LinearLayout step_layout = (LinearLayout) findViewById(R.id.step_layout);
		LinearLayout button_layout = (LinearLayout) step_layout.getChildAt(index);

		Button button = (Button) button_layout.getChildAt(0);
		button.setBackgroundResource(R.color.white);
		button.setTextColor(getResources().getColor(R.color.blue));
		
		ImageView image = (ImageView) button_layout.getChildAt(1);
		image.setBackgroundResource(R.color.red);
	}

	@Override
	public void onClick(View view) {
		Object object = view.getTag();
		if (object == null) {
			Log.e("IDLItemActivity", "Clickable object missing tag");
			return;
		}

		if (object.getClass() == String.class) {
			String string = (String) object;
			if (string.equals(VIEW_TAG_STEP_BUTTON)) {
				LinearLayout step_layout = (LinearLayout) findViewById(R.id.step_layout);
				int index = step_layout.indexOfChild((View) view.getParent());
				Log.d("", String.format("click %d", index));
				gotoStep(index);
			}
		}
	}
	
	public void onExitButtonClicked(View view) {
		Log.d("", "Exit");
	}
	
	public void onPrevButtonClicked(View view) {
		Log.d("", "Previous");
	}

	public void onNextButtonClicked(View view) {
		Log.d("", "Next");
	}

	
	/**
	 * class for hold item page information
	 */
	public class IDLItemPage
	{
		public String title;
		public ArrayList<IDLItemRow> rows;

		public IDLItemPage() {
			rows = new ArrayList<IDLItemRow>();
		}
	}


	/**
	 * class for hold item row information
	 */
	public class IDLItemRow
	{
		public String title;
		public ArrayList<IDLItemEntry> entries;
		
		public IDLItemRow() {
			entries = new ArrayList<IDLItemEntry>();
		}
	}


	/**
	 * class for hold item entry information
	 */
	public class IDLItemEntry
	{
		public String id;
		public String klass;
	}


	/**
	 * read item layout file
	 */
	private class IDLItemReader
	{
		public IDLItemReader() {
		}

		public void parse(InputStream inputStream) throws SAXException, IOException {
			try {
				SAXParserFactory factory = SAXParserFactory.newInstance();
				SAXParser parser = factory.newSAXParser();
				IDLItemHandler handler = new IDLItemHandler();
				parser.parse(inputStream, handler);
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
				throw new SAXException("Configure XML parser error");
			}
		}


		private class IDLItemHandler extends DefaultHandler
		{
			private String exc_prefix;
			private Locator locator;
			private int version;
			private int level;
			private IDLItemPage page;
			private IDLItemRow row;
			
			public IDLItemHandler() {
				version = 1;
				exc_prefix = name + ".idl.xml: ";
			}

			@SuppressLint("DefaultLocale")
			private String formatMessage(String message) {
				return String.format("%s%s(line:%d, col:%d)", exc_prefix, 
						message, locator.getLineNumber(), locator.getColumnNumber());
			}

			@Override
			public void setDocumentLocator(Locator locator) {
				this.locator = locator;
			}

			@Override
			public void startDocument() throws SAXException {
				level = 0;
				page = null;
				row = null;
			}
			
			@Override
			public void endDocument() throws SAXException {
			}

			@Override
			public void startElement(String uri, String localName, String qName, Attributes attrs)
					throws SAXException {
				if (level == 0) {
					if (!localName.equalsIgnoreCase("item"))
						throw new SAXException(formatMessage("Root element must be 'item'"));

					String value = attrs.getValue("", "version");
					if (value != null)
						version = Integer.parseInt(value);
					if (version < 1)
						throw new SAXException(
							formatMessage(String.format("Version '%s' is not recognizable", value))
						);
				}

				level++;
				
				if (localName.equalsIgnoreCase("page")) {
					if (level != 2)
						throw new SAXException(
							formatMessage("Element 'page' must be child of root element")
						);
					parsePage(attrs);
				} else if (localName.equalsIgnoreCase("row")) {
					if (level != 3 || page == null)
						throw new SAXException(
							formatMessage("Element 'row' must inside 'page' element")
						);
					parseRow(attrs);
				} else if (localName.equalsIgnoreCase("entry")) {
					if (level != 4 || row == null)
						throw new SAXException(
							formatMessage("Element 'entry' must inside 'row' element")
						);
					parseEntry(attrs);
				}

			}

			@Override
	        public void endElement(String uri, String localName, String qName) throws SAXException {
				level--;

				if (localName.equalsIgnoreCase("page")) {
					if (page.rows.size() == 0) {
						Log.w("IDLItemReader",
								String.format("Page '%s' has not any row, discard", page.title));
					} else {
						pages.add(page);
					}
				} else if (localName.equalsIgnoreCase("row")) {
					if (row.entries.size() == 0) {
						Log.w("IDLItemReader",
								String.format("Row '%s' has not any entry, discard", row.title));
					} else {
						page.rows.add(row);
					}
				}
			}
			
			@Override
			public void characters(char[] ch, int start, int length) throws SAXException {
			}
			

			private void parsePage(Attributes attrs) throws SAXException {
				page = new IDLItemPage();

				page.title = attrs.getValue("", "title");
				if (page.title == null || page.title.trim().length() == 0) {
					throw new SAXException(
						formatMessage("Element 'page' missing 'title' attribute")
					);
				}
			}


			private void parseRow(Attributes attrs) throws SAXException {
				row = new IDLItemRow();

				row.title = attrs.getValue("", "title");
				if (row.title == null || row.title.trim().length() == 0) {
					throw new SAXException(
						formatMessage("Element 'row' missing 'title' attribute")
					);
				}
			}


			private void parseEntry(Attributes attrs) throws SAXException {
				IDLItemEntry entry = new IDLItemEntry();

				entry.id = attrs.getValue("", "id");
				if (entry.id == null)
					throw new SAXException(
						formatMessage("Element 'entry' missing 'id' attribute")
					);
				entry.klass = attrs.getValue("", "class");
				if (entry.klass == null)
					throw new SAXException(
						formatMessage("Element 'entry' missing 'class' attribute")
					);
				row.entries.add(entry);
			}
		}
	}
	
}
