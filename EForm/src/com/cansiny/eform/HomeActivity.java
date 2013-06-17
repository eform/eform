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
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

//import com.cansiny.eform.util.SystemUiHider;

//import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
//import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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
	 * Build home user interface layout. some information is fixed like label.
	 * some information read from IDL.
	 */
	private void rebuildLayout() {
		try {
			IDLInfoReader reader = new IDLInfoReader();
			reader.parse(idl_archive.getInfoInputStream());

			CustomerInfo info = CustomerInfo.getCustomerInfo(reader.getCustomerID());
			ImageView logo_view = (ImageView)findViewById(R.id.logo_image);
			logo_view.setImageResource(info.logo_resid);
			View main_layout = findViewById(R.id.main_layout);
			main_layout.setBackgroundResource(info.background_resid);
		} catch (SAXException e) {
			e.printStackTrace();
			show_recovery_flagment(R.string.error_idl_parse);
		} catch (IOException e) {
			e.printStackTrace();
			show_recovery_flagment(R.string.error_idl_io);
		}
	}

	private void addItemsFromIDL() {
		try {
			IDLIndexReader reader = new IDLIndexReader();
			reader.parse(idl_archive.getIndexInputStream());
		} catch (IOException e) {
			e.printStackTrace();
			show_recovery_flagment(R.string.error_idl_io);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
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
 * customer logo, hotline, website and so on.
 */
class CustomerInfo
{
	public int logo_resid;
	public int background_resid;
	public String flash_name;

	static public final int CUSTOMER_CIB = 1;	// Industrial Bank Co.,Ltd.
	static public final int CUSTOMER_CCB = 2;	// China Construction Bank
	static public final int CUSTOMER_ABC = 3;	// Agricultural Bank of China

	static public CustomerInfo getCustomerInfo(int customer_num) {
		CustomerInfo customer_info = new CustomerInfo();
		switch (customer_num) {
		case CUSTOMER_CIB:
			customer_info.logo_resid = R.drawable.logo_cib;
			customer_info.background_resid = R.drawable.logo_cib;
			customer_info.flash_name = "95561";
			break;
		case CUSTOMER_CCB:
			customer_info.logo_resid = R.drawable.logo_cib;
			break;
		case CUSTOMER_ABC:
			customer_info.logo_resid = R.drawable.logo_cib;
			break;
		default:
			customer_info.logo_resid = R.drawable.logo_cib;
			break;
		}
		return customer_info;
	}
}
