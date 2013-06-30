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
import java.lang.reflect.InvocationTargetException;
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
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.widget.FrameLayout.LayoutParams;

@SuppressWarnings("unused")
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
	private ArrayList<IDLItemPage> pages;
	private int curr_step;

	public static Animation inFromRightAnimation() {
		Animation inFromRight = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, +1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		inFromRight.setDuration(350);
		inFromRight.setInterpolator(new AccelerateInterpolator());
		return inFromRight;
	}

	public static Animation outToLeftAnimation() {
		Animation outtoLeft = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, -1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		outtoLeft.setDuration(350);
		outtoLeft.setInterpolator(new AccelerateInterpolator());
		return outtoLeft;
	}

	// for the next movement
	public static Animation inFromLeftAnimation() {
		Animation inFromLeft = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, -1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		inFromLeft.setDuration(350);
		inFromLeft.setInterpolator(new AccelerateInterpolator());
		return inFromLeft;
	}

	public static Animation outToRightAnimation() {
		Animation outtoRight = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, +1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		outtoRight.setDuration(350);
		outtoRight.setInterpolator(new AccelerateInterpolator());
		return outtoRight;
	}
    
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		pages = new ArrayList<IDLItemPage>();

		atomic_int = new AtomicInteger(HomeActivity.ITEM_VIEW_ID_BASE);
		setContentView(R.layout.activity_form);
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

			/* top-left corner name */
			TextView text_view = (TextView) findViewById(R.id.label_textview);
			text_view.setText(name);
			text_view.setTextColor(getResources().getColor(R.color.white));
			text_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);

			ViewGroup step_layout = (ViewGroup) findViewById(R.id.step_layout);
			ViewFlipper flipper = (ViewFlipper) findViewById(R.id.flipper_view);
			flipper.setInAnimation(inFromLeftAnimation());
			flipper.setOutAnimation(outToRightAnimation());
			for (IDLItemPage page : pages) {
				/* step buttons */
				View button = createStepButton(page.title);
				
//				LinearLayout.LayoutParams button_params = new LinearLayout.LayoutParams(
//						ViewGroup.LayoutParams.WRAP_CONTENT,
//						ViewGroup.LayoutParams.WRAP_CONTENT);
//				top_layout.addView(button, button_params);
				step_layout.addView(button);

				/* contents */
				TableLayout table_layout = new TableLayout(getApplicationContext());
				table_layout.setId(atomic_int.incrementAndGet());

				ScrollView scroll = new ScrollView(getApplicationContext());
				scroll.setId(atomic_int.incrementAndGet());
//				FrameLayout.LayoutParams table_params = new FrameLayout.LayoutParams(
//						ViewGroup.LayoutParams.MATCH_PARENT,
//						ViewGroup.LayoutParams.MATCH_PARENT);
				scroll.addView(table_layout);
				
				FrameLayout.LayoutParams table_params = new FrameLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT);
				table_params.leftMargin = 120;
				table_params.topMargin = 100;
				table_params.rightMargin = 120;
				flipper.addView(scroll, table_params);

				for (IDLItemRow row : page.rows) {
					TableRow table_row = new TableRow(getApplicationContext());
					table_row.setId(atomic_int.incrementAndGet());
					table_layout.addView(table_row);

					text_view = new TextView(getApplicationContext());
					text_view.setId(atomic_int.incrementAndGet());
					text_view.setText(row.title);
					text_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
					table_row.addView(text_view);

					LinearLayout linear = new LinearLayout(getApplicationContext());
					for (IDLItemEntry entry : row.entries) {
						View view = createItemEntryView(entry);
						if (view != null)
							linear.addView(view);
					}
					TableRow.LayoutParams row_params = new TableRow.LayoutParams(
							ViewGroup.LayoutParams.MATCH_PARENT,
							ViewGroup.LayoutParams.WRAP_CONTENT);
					table_row.addView(linear, row_params);
				}
			}
			
			/* active the first step */
			resetStepLayout();
			curr_step = -1;
			gotoStep(0);
		} catch (IOException e) {
			LogActivity.writeLog(e);
			intent.putExtra(INTENT_RESULT_ERRREASON, R.string.error_idl_io);
			setResult(RESULT_CANCELED, intent);
			finish();
		} catch (SAXException e) {
			LogActivity.writeLog(e);
			intent.putExtra(INTENT_RESULT_ERRREASON, R.string.error_idl_parse);
			setResult(RESULT_CANCELED, intent);
			finish();
		} catch (InstantiationException e) {
			LogActivity.writeLog(e);
			intent.putExtra(INTENT_RESULT_ERRREASON, R.string.error_idl_syntax);
			setResult(RESULT_CANCELED, intent);
			finish();
		} catch (IllegalAccessException e) {
			LogActivity.writeLog(e);
			intent.putExtra(INTENT_RESULT_ERRREASON, R.string.error_idl_syntax);
			setResult(RESULT_CANCELED, intent);
			finish();
		} catch (ClassNotFoundException e) {
			LogActivity.writeLog(e);
			intent.putExtra(INTENT_RESULT_ERRREASON, R.string.error_idl_syntax);
			setResult(RESULT_CANCELED, intent);
			finish();
		} catch (IllegalArgumentException e) {
			LogActivity.writeLog(e);
			intent.putExtra(INTENT_RESULT_ERRREASON, R.string.error_idl_syntax);
			setResult(RESULT_CANCELED, intent);
			finish();
		} catch (InvocationTargetException e) {
			LogActivity.writeLog(e);
			intent.putExtra(INTENT_RESULT_ERRREASON, R.string.error_idl_syntax);
			setResult(RESULT_CANCELED, intent);
			finish();
		} catch (NoSuchMethodException e) {
			LogActivity.writeLog(e);
			intent.putExtra(INTENT_RESULT_ERRREASON, R.string.error_idl_syntax);
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
		if (index == curr_step)
			return;

		LinearLayout step_layout = (LinearLayout) findViewById(R.id.step_layout);

		if (index >= step_layout.getChildCount()) {
			Log.e("IDLItemActivity", String.format("Try to goto unexists step %d", index));
			return;
		}
		resetStepLayout();

		LinearLayout button_layout = (LinearLayout) step_layout.getChildAt(index);

		Button button = (Button) button_layout.getChildAt(0);
		button.setBackgroundResource(R.color.white);
		button.setTextColor(getResources().getColor(R.color.blue));
		
		ImageView image = (ImageView) button_layout.getChildAt(1);
		image.setBackgroundResource(R.color.red);

		ViewFlipper flipper = (ViewFlipper) findViewById(R.id.flipper_view);
		if (curr_step != index)
			flipper.setDisplayedChild(index);

		curr_step = index;

		button = (Button) findViewById(R.id.prev_button);
		if (curr_step == 0) {
			button.setEnabled(false);
		} else {
			button.setEnabled(true);
		}

		button = (Button) findViewById(R.id.next_button);
		if (curr_step + 1 == pages.size()) {
			button.setEnabled(false);
		} else {
			button.setEnabled(true);
		}
	}

	private View createItemEntryView(IDLItemEntry entry)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException {
		if (entry.klass == null || entry.klass.length() == 0)
			throw new ClassNotFoundException("Entry must has a class");

		Object object = Class.forName(entry.klass).getConstructor(Context.class).newInstance(getApplicationContext());
		if (object instanceof View)
			return (View) object;

		throw new InstantiationException(
			String.format("Class '%s' is not subclass of 'android.view.View'", entry.klass)
		);
	}

	public void onPrevButtonClicked(View view) {
		if (curr_step > 0)
			gotoStep(curr_step - 1);
	}

	public void onNextButtonClicked(View view) {
		if (curr_step + 1 < pages.size())
			gotoStep(curr_step + 1);
	}

	public void onExitButtonClicked(View view) {
		setResult(RESULT_OK);
		finish();
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
	
	
	/**
	 * class for hold item page information
	 */
	private class IDLItemPage
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
	private class IDLItemRow
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
	private class IDLItemEntry
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
				LogActivity.writeLog(e);
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
				if (entry.id == null) {
					throw new SAXException(
						formatMessage("Element 'entry' missing 'id' attribute")
					);
				}
				entry.klass = attrs.getValue("", "class");
				if (entry.klass == null) {
					throw new SAXException(
						formatMessage("Element 'entry' missing 'class' attribute")
					);
				}
				row.entries.add(entry);
			}
		}
	}
	
}
