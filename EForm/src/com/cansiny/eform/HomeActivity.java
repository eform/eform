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
import java.util.ArrayList;

import org.xml.sax.SAXException;

import com.cansiny.eform.IDLIndexReader.IDLIndexItem;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * Home Activity, efrom will start form this activity.
 */
public class HomeActivity extends Activity
{
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
//	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
//	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
//	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
//	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
//	private SystemUiHider mSystemUiHider;

	private IDLArchive idl_archive;
	private CustomerInfo customer_info;

	/* get application context object, since SDK has no static method
	 * to get this object, so we add one.
	 */
	static private Context _app_context = null;
	static public Context getAppContext() {
		return _app_context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d("HomeActivity", "onCreate");

		_app_context = getApplicationContext();
		setContentView(R.layout.activity_home);

		try {
			idl_archive = IDLArchive.getIDLArchive();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("EForm", "Failed to open IDL archive.");
			show_recovery_flagment(R.string.error_idl_io);
			return;
		}

		rebuildLayout();


		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
//		mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
//		mSystemUiHider.setup();
//		mSystemUiHider.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
//			// Cached values.
//			int mControlsHeight;
//			int mShortAnimTime;
//
//			@Override
//			@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
//			public void onVisibilityChange(boolean visible) {
//				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
//					// If the ViewPropertyAnimator API is available (Honeycomb MR2 and later),
//					// use it to animate the in-layout UI controls at the bottom of the screen.
//					if (mControlsHeight == 0) {
//						mControlsHeight = controlsView.getHeight();
//					}
//					if (mShortAnimTime == 0) {
//						mShortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
//					}
//					controlsView.animate().translationY(visible ? 0 : mControlsHeight).setDuration(mShortAnimTime);
//				} else {
//					// If the ViewPropertyAnimator APIs aren't available, simply show or hide the
//					// in-layout UI controls.
//					controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
//				}
//
//				if (visible && AUTO_HIDE) {
//					// Schedule a hide().
//					delayedHide(AUTO_HIDE_DELAY_MILLIS);
//				}
//			}
//		});

		// Set up the user interaction to manually show or hide the system UI.
//		contentView.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View view) {
//				if (TOGGLE_ON_CLICK) {
//					mSystemUiHider.toggle();
//				} else {
//					mSystemUiHider.show();
//				}
//			}
//		});

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
//		findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
	}

//	@Override
//	protected void onPostCreate(Bundle savedInstanceState) {
//		super.onPostCreate(savedInstanceState);
//
//		// Trigger the initial hide() shortly after the activity has been
//		// created, to briefly hint to the user that UI controls
//		// are available.
//		delayedHide(100);
//	}

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

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
//	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
//		@Override
//		public boolean onTouch(View view, MotionEvent motionEvent) {
//			if (AUTO_HIDE) {
//				delayedHide(AUTO_HIDE_DELAY_MILLIS);
//			}
//			return false;
//		}
//	};
//
//	Handler mHideHandler = new Handler();
//	Runnable mHideRunnable = new Runnable() {
//		@Override
//		public void run() {
//			mSystemUiHider.hide();
//		}
//	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
//	private void delayedHide(int delayMillis) {
//		mHideHandler.removeCallbacks(mHideRunnable);
//		mHideHandler.postDelayed(mHideRunnable, delayMillis);
//	}
	/**
	 * This method converts dp unit to equivalent pixels, depending on device density. 
	 * 
	 * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
	 * @param context Context to get resources and device specific display metrics
	 * @return A float value to represent px equivalent to dp depending on device density
	 */
	public float convertDpToPixel(float dp) {
	    Resources resources = getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    return dp * (metrics.densityDpi / 160f);
	}

	/**
	 * Build home user interface layout. some information is fixed like label.
	 * some information read from IDL.
	 */
	private void rebuildLayout() {
		try {
			/* read customer info from IDL archive if need. */
			if (customer_info == null) {
				IDLInfoReader reader = new IDLInfoReader();
				reader.parse(idl_archive.getInfoInputStream());
				customer_info = CustomerInfo.getCustomerInfo(reader.getCustomerID());
				idl_archive.setCustomerName(customer_info.name);
			}

			/* setup global interface. */
			ImageView logo_view = (ImageView)findViewById(R.id.logo_image);
			logo_view.setImageResource(customer_info.logo_resid);
			View main_layout = findViewById(R.id.main_layout);
			main_layout.setBackgroundResource(customer_info.background_resid);

			/* add items to contents area. */
			addItemsFromIDL();
		} catch (SAXException e) {
			e.printStackTrace();
			show_recovery_flagment(R.string.error_idl_parse);
		} catch (IOException e) {
			e.printStackTrace();
			show_recovery_flagment(R.string.error_idl_io);
		}
	}

	private View createIconButton(String icon_name, int icon_size,
			String text, int text_size) throws IOException {
		LinearLayout button = new LinearLayout(getApplicationContext());
		button.setOrientation(LinearLayout.VERTICAL);

		ImageButton icon_view = new ImageButton(getApplicationContext());
		icon_view.setBackgroundResource(R.drawable.btn_background_black);
		InputStream stream = idl_archive.getIconInputStream(icon_name);
		Drawable drawable = Drawable.createFromStream(stream, icon_name);
		icon_view.setImageDrawable(drawable);
		icon_view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

		int icon_size_pixel = (int) convertDpToPixel(icon_size);
		Log.d("", String.format("icon size: %d->%d", icon_size, icon_size_pixel));
		icon_size_pixel = 96;
		LinearLayout.LayoutParams button_layout_params = new LinearLayout.LayoutParams(
				icon_size_pixel, icon_size_pixel);
		button_layout_params.bottomMargin = 8;
		button_layout_params.gravity = Gravity.CENTER_HORIZONTAL;
		button.addView(icon_view, button_layout_params);

		TextView text_view = new TextView(getApplicationContext());
		text_view.setText(text);
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
		IDLIndexReader reader = new IDLIndexReader();
		reader.parse(idl_archive.getIndexInputStream());

		TableLayout table = new TableLayout(getApplicationContext());
		//table.setBackgroundResource(R.color.red);

		int table_columns = reader.getTableColumns();
		int table_xpad = reader.getTableXPad();
		int table_ypad = reader.getTableYPad();
		int button_icon_size = reader.getButtonIconSize();
		int button_text_size = reader.getButtonTextSize();
		ArrayList<IDLIndexItem> items = reader.getItems();

		TableRow table_row = new TableRow(getApplicationContext());
		table_row.setBackgroundResource(R.color.translucence);
		table_row.setGravity(Gravity.CENTER_HORIZONTAL);
		int curr_column = 0;

		for (IDLIndexItem item : items) {
			View button = createIconButton(item.getName() + ".png", button_icon_size,
					item.getText(), button_text_size);
			TableRow.LayoutParams row_layout_params = new TableRow.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			row_layout_params.rightMargin = (int) convertDpToPixel(table_xpad);
			table_row.addView(button, row_layout_params);

			if (curr_column++ > table_columns) {
				TableLayout.LayoutParams table_layout_params = new TableLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				table_layout_params.bottomMargin = table_ypad;
				table.addView(table_row, table_layout_params);

				table_row = new TableRow(getApplicationContext());
				table_row.setGravity(Gravity.CENTER_HORIZONTAL);
				curr_column = 0;
			}
		}
		table.addView(table_row);

		ViewGroup group = (ViewGroup) findViewById(R.id.contents_layout);
		group.addView(table);
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


/**
 * A class hold customer information.
 * Each customer has a unique number, by this number, application can get
 * customer information.
 */
class CustomerInfo
{
	public int id;
	public String name;
	public int logo_resid;
	public int background_resid;
	public String flash_name;

	static public final int CUSTOMER_CIB = 1;	// Industrial Bank Co.,Ltd.
	static public final int CUSTOMER_CCB = 2;	// China Construction Bank
	static public final int CUSTOMER_ABC = 3;	// Agricultural Bank of China

	static public CustomerInfo getCustomerInfo(int customer_id) {
		CustomerInfo customer_info = new CustomerInfo();
		customer_info.id = customer_id;
		switch (customer_id) {
		case CUSTOMER_CIB:
			customer_info.name = "cib";
			customer_info.logo_resid = R.drawable.logo_cib;
			customer_info.background_resid = R.drawable.background_cib;
			customer_info.flash_name = "95561";
			break;
		case CUSTOMER_CCB:
			customer_info.name = "ccb";
			customer_info.logo_resid = R.drawable.logo_cib;
			break;
		case CUSTOMER_ABC:
			customer_info.name = "abc";
			customer_info.logo_resid = R.drawable.logo_cib;
			break;
		default:
			customer_info.logo_resid = R.drawable.logo_cib;
			break;
		}
		return customer_info;
	}
}
