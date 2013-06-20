/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 * 
 * HomeActivity - The application entry
 *
 * Authors:
 *   Xiaohu <xiaohu417@gmail.com>, 2013.6.11, hefei
 */
package com.cansiny.eform;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

import org.xml.sax.SAXException;

import com.cansiny.eform.IDLArchive.IDLArchiveIndexItem;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * Home Activity, efrom will start form this activity.
 */
public class HomeActivity extends Activity implements OnClickListener, OnLongClickListener
{
	static public final int HOME_VIEW_ID_BASE = 0x3F000001;
	static public final int ITEM_VIEW_ID_BASE = 0x3F100001;

	static public final int ITEM_ACTIVITY_REQUEST_CODE = 1;
	
	private IDLArchive idl_archive;
	private AtomicInteger atomic_int;

	/* get application context object, since SDK has no static method
	 * to get this object, so we add one. */
	static private Context _app_context = null;
	static public Context getAppContext() {
		return _app_context;
	}

	/**
	 * This method converts dp unit to equivalent pixels, depending on device density. 
	 * 
	 * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
	 * @return A float value to represent px equivalent to dp depending on device density
	 */
	static public float convertDpToPixel(float dp) {
		Context context = HomeActivity.getAppContext();
	    DisplayMetrics metrics = context.getResources().getDisplayMetrics();
	    return dp * (metrics.densityDpi / 160f);
	}

	/**
	 * This method converts device specific pixels to density independent pixels.
	 * 
	 * @param px A value in px (pixels) unit. Which we need to convert into db
	 * @return A float value to represent dp equivalent to px value
	 */
	static public float convertPixelsToDp(float px) {
		Context context = HomeActivity.getAppContext();
	    DisplayMetrics metrics = context.getResources().getDisplayMetrics();
	    return px / (metrics.densityDpi / 160f);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d("HomeActivity", "onCreate");

		_app_context = getApplicationContext();
		atomic_int = new AtomicInteger(HOME_VIEW_ID_BASE);
		setContentView(R.layout.activity_home);

		try {
			idl_archive = IDLArchive.getIDLArchive(this);
		} catch (IOException e) {
			e.printStackTrace();
			show_recovery_flagment(R.string.error_idl_io);
			return;
		} catch (SAXException e) {
			e.printStackTrace();
			show_recovery_flagment(R.string.error_idl_parse);
			return;
		}

		rebuildLayout();

		View contents_layout = this.findViewById(R.id.contents_layout);
		contents_layout.setLongClickable(true);
		contents_layout.setOnLongClickListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d("HomeActivity", "on Paused");
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		Log.d("HomeActivity", "on Stoped");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d("HomeActivity", "on Resume");
	}

	@Override
	protected void onRestart() {
	    super.onRestart();
		Log.d("HomeActivity", "on Restart");
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("HomeActivity", "on destory");
		try {
			if (idl_archive != null)
				idl_archive.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getClass() == ImageButton.class) {
			Object object = view.getTag();
			if (object == null || !(object instanceof String)) {
				Log.e("HomeActivity", "Programming error: ImageButton missing tag");
				return;
			}
			Intent intent = new Intent(this, IDLItemActivity.class);
			intent.putExtra(IDLItemActivity.INTENT_MESSAGE_NAME, (String) object);
			startActivityForResult(intent, ITEM_ACTIVITY_REQUEST_CODE);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch(requestCode) {
		case ITEM_ACTIVITY_REQUEST_CODE:
			switch(resultCode) {
			case RESULT_CANCELED:
				int reason = intent.getIntExtra(IDLItemActivity.INTENT_RESULT_ERRREASON, 0);
				if (reason != 0)
					show_recovery_flagment(reason);
				break;
			case RESULT_OK:
				break;
			}
			break;
		}
	}

	@Override
	public boolean onLongClick(View view) {
		if (view.getId() == R.id.contents_layout) {
			Log.d("", "Long clicked");
			return true;
		}
		return false;
	}

	/**
	 * Build home user interface layout. some information is fixed like label.
	 * some information read from IDL.
	 */
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void rebuildLayout() {
		try {
			/* setup home user interface. */
			ImageView logo_view = (ImageView)findViewById(R.id.logo_image);
			logo_view.setImageDrawable(idl_archive.getLogoDrawable());
			View main_layout = findViewById(R.id.main_layout);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
				main_layout.setBackground(idl_archive.getBackgroundDrawable());
			else
				main_layout.setBackgroundDrawable(idl_archive.getBackgroundDrawable());

			/* add items to home contents area. */
			addItemsFromIDL();
		} catch (SAXException e) {
			e.printStackTrace();
			show_recovery_flagment(R.string.error_idl_parse);
		} catch (IOException e) {
			e.printStackTrace();
			show_recovery_flagment(R.string.error_idl_io);
		}
	}

	private View createIconButton(IDLArchiveIndexItem item, int icon_size, int text_size)
			throws IOException {
		LinearLayout button = new LinearLayout(getApplicationContext());
		button.setId(atomic_int.incrementAndGet());
		button.setOrientation(LinearLayout.VERTICAL);

		ImageButton icon_button = new ImageButton(getApplicationContext());
		icon_button.setId(atomic_int.incrementAndGet());
		icon_button.setTag(item.name);
		icon_button.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		icon_button.setBackgroundResource(R.drawable.btn_background_black);
		InputStream stream = idl_archive.getImageInputStream(item.name);
		Drawable drawable = Drawable.createFromStream(stream, item.name);
		stream.close();
		icon_button.setImageDrawable(drawable);
		icon_button.setClickable(true);
		icon_button.setOnClickListener(this);

		int icon_size_pixel = (int) convertDpToPixel(icon_size);
		LinearLayout.LayoutParams button_layout_params = new LinearLayout.LayoutParams(
				icon_size_pixel, icon_size_pixel);
		button_layout_params.bottomMargin = 4;
		button_layout_params.gravity = Gravity.CENTER_HORIZONTAL;
		button.addView(icon_button, button_layout_params);

		TextView text_view = new TextView(getApplicationContext());
		text_view.setId(atomic_int.incrementAndGet());
		text_view.setText(item.text);
		text_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, text_size);
		text_view.setTextColor(getResources().getColor(R.color.white));

		button_layout_params = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		button_layout_params.gravity = Gravity.CENTER_HORIZONTAL;
		button.addView(text_view, button_layout_params);

		return button;
	}

	private void addItemsFromIDL() throws SAXException, IOException {
		TableLayout table = new TableLayout(getApplicationContext());
		table.setId(atomic_int.incrementAndGet());

		TableRow table_row = new TableRow(getApplicationContext());
		table_row.setId(atomic_int.incrementAndGet());
		table_row.setGravity(Gravity.CENTER_HORIZONTAL);
		int curr_column = 1;
		IDLArchive.IDLArchiveIndex index = idl_archive.index;

		for (IDLArchiveIndexItem item : index.items) {
			View button = createIconButton(item, index.button_icon_size, index.button_text_size);
			TableRow.LayoutParams row_layout_params = new TableRow.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			if (curr_column < index.table_columns) {
				row_layout_params.rightMargin = (int) convertDpToPixel(index.table_xpad);
			}
			table_row.addView(button, row_layout_params);

			if (++curr_column > index.table_columns) {
				TableLayout.LayoutParams table_layout_params = new TableLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				table_layout_params.bottomMargin = index.table_ypad;
				table.addView(table_row, table_layout_params);

				table_row = new TableRow(getApplicationContext());
				table_row.setGravity(Gravity.CENTER_HORIZONTAL);
				curr_column = 1;
			}
		}
		if (curr_column > 1) {
			for (; curr_column <= index.table_columns; curr_column++) {
				LinearLayout placeholder = new LinearLayout(getApplicationContext());
				placeholder.setId(atomic_int.incrementAndGet());
				table_row.addView(placeholder);
			}
			table.addView(table_row);
		}

		FrameLayout.LayoutParams contents_params = new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		contents_params.topMargin = (int) convertDpToPixel(index.table_top_margin);
		ViewGroup group = (ViewGroup) findViewById(R.id.contents_layout);
		group.removeAllViews();
		group.addView(table, contents_params);
	}

	private void show_recovery_flagment(int reason) {
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		RecoveryFragment fragment = new RecoveryFragment();
		fragment.setErrorReason(reason);
		ViewGroup layout = (ViewGroup)findViewById(R.id.contents_layout);
		layout.removeAllViews();
		fragmentTransaction.add(R.id.contents_layout, fragment);
		fragmentTransaction.commit();
	}

}

