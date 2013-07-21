/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import java.util.concurrent.atomic.AtomicInteger;

import com.cansiny.eform.HomeInfo.HomeItem;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
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
public class HomeActivity extends Activity
    implements OnClickListener, OnLongClickListener
{
    static public final int HOME_VIEW_ID_BASE = 0x3F000001;
    static public final int ITEM_VIEW_ID_BASE = 0x3F100001;

    static public final int ITEM_ACTIVITY_REQUEST_CODE = 1;
	
    private AtomicInteger atomic_int;
    private HomeInfo home_info;

    /* get application context object, since SDK has no static method
     * to get this object, so we add one. */
    static private Context app_context = null;
    static public Context getAppContext() {
	return app_context;
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on
     * device density. 
     * 
     * @param dp A value in dp (density independent pixels) unit. Which we
     * 		need to convert into pixels
     * @return A float value to represent px equivalent to dp depending on
     *		device density
     */
    static public float convertDpToPixel(float dp) {
	Context context = HomeActivity.getAppContext();
	DisplayMetrics metrics = context.getResources().getDisplayMetrics();
	return dp * (metrics.densityDpi / 160f);
    }

    /**
     * This method converts device specific pixels to density independent pixels
     * 
     * @param px A value in px (pixels) unit. Which we need to convert into dp
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

	Runtime rt = Runtime.getRuntime();
	long maxMemory = rt.maxMemory();
	Log.v("onCreate", "maxMemory:" + Long.toString(maxMemory));

	ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	int memoryClass = am.getMemoryClass();
	Log.v("onCreate", "memoryClass:" + Integer.toString(memoryClass));
		
	//		dalvik.system.VMRuntime runtime = 	dalvik.system.VMRuntime.getRuntime();
	//		runtime.setMinimumHeapSize(64 * 1024 * 1024);

	app_context = getApplicationContext();
	atomic_int = new AtomicInteger(HOME_VIEW_ID_BASE);

	home_info = HomeInfo.getHomeInfo();
	home_info.setCustomer(HomeInfo.CUSTOMER_CURRENT);

	LogActivity.clearLog();

	setContentView(R.layout.activity_home);

	/* configure home layout with HomeInfo */
	buildLayout();

	/* catch long click event */
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
    }

    @Override
    public void onClick(View view) {
	if (view.getClass() == ImageButton.class) {
	    Object object = view.getTag();
	    if (object == null || !(object instanceof HomeInfo.HomeItem)) {
		Log.e("HomeActivity", "Programming error: Image Button missing class tag");
		return;
	    }
	    HomeInfo.HomeItem item = (HomeItem) object;
	    Intent intent = new Intent(this, FormActivity.class);
	    intent.putExtra(FormActivity.INTENT_MESSAGE_CLASS, item.klass);
	    intent.putExtra(FormActivity.INTENT_MESSAGE_LABEL, item.label);
	    startActivityForResult(intent, ITEM_ACTIVITY_REQUEST_CODE);
	}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
	switch(requestCode) {
	case ITEM_ACTIVITY_REQUEST_CODE:
	    switch(resultCode) {
	    case RESULT_CANCELED:
		int reason = intent.getIntExtra(FormActivity.INTENT_RESULT_ERRREASON, 0);
		if (reason != 0)
		    show_error_flagment(reason);
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
    private void buildLayout() {
	/* setup home user interface. */
	ImageView logo_view = (ImageView) findViewById(R.id.logo_image);
	logo_view.setImageResource(home_info.logo);
	View main_layout = findViewById(R.id.main_layout);
	main_layout.setBackgroundResource(home_info.background);
	/* add items to home contents area. */
	buildContentsLayout();
    }

	
    private LinearLayout buildImageButton(HomeInfo.HomeItem item) {
	LinearLayout button_layout = new LinearLayout(getApplicationContext());
	button_layout.setId(atomic_int.incrementAndGet());
	button_layout.setOrientation(LinearLayout.VERTICAL);

	ImageButton button = new ImageButton(getApplicationContext());
	button.setId(atomic_int.incrementAndGet());
	button.setTag(item);
	button.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
	button.setBackgroundResource(item.image);
	button.setClickable(true);
	button.setOnClickListener(this);

	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
		(int) convertDpToPixel(home_info.item_image_size),
		(int) convertDpToPixel(home_info.item_image_size));
	params.bottomMargin = 0;
	params.gravity = Gravity.CENTER_HORIZONTAL;
	button_layout.addView(button, params);

	TextView text_view = new TextView(getApplicationContext());
	text_view.setId(atomic_int.incrementAndGet());
	text_view.setText(item.label);
	text_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, item.label_size);
	text_view.setTextColor(getResources().getColor(R.color.white));
	text_view.setGravity(Gravity.CENTER);

	params = new LinearLayout.LayoutParams(
		ViewGroup.LayoutParams.MATCH_PARENT,
		ViewGroup.LayoutParams.MATCH_PARENT);
	button_layout.addView(text_view, params);

	return button_layout;
    }

	
    private void buildContentsLayout() {
	TableLayout table = new TableLayout(getApplicationContext());
	table.setId(atomic_int.incrementAndGet());

	FrameLayout.LayoutParams contents_params = new FrameLayout.LayoutParams(
		ViewGroup.LayoutParams.MATCH_PARENT,
		ViewGroup.LayoutParams.MATCH_PARENT);
	FrameLayout contents = (FrameLayout) findViewById(R.id.contents_layout);
	contents.removeAllViews();
	contents.addView(table, contents_params);

	TableRow table_row = new TableRow(getApplicationContext());
	table_row.setId(atomic_int.incrementAndGet());
	table_row.setGravity(Gravity.CENTER_HORIZONTAL);
	table_row.setLayoutParams(new TableLayout.LayoutParams(
		ViewGroup.LayoutParams.MATCH_PARENT,
		ViewGroup.LayoutParams.WRAP_CONTENT));
	table.addView(table_row);

	int curr_column = 1;

	for (HomeInfo.HomeItem item : home_info.items) {
	    LinearLayout button = buildImageButton(item);
	    TableRow.LayoutParams row_params = new TableRow.LayoutParams(
		    ViewGroup.LayoutParams.MATCH_PARENT,
		    ViewGroup.LayoutParams.WRAP_CONTENT);
	    if (curr_column < home_info.item_columns) {
		row_params.rightMargin = (int) convertDpToPixel(50);
	    }
	    table_row.addView(button, row_params);

	    if (++curr_column > home_info.item_columns) {
		table_row = new TableRow(getApplicationContext());
		table_row.setGravity(Gravity.CENTER_HORIZONTAL);
		table_row.setLayoutParams(new TableLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.WRAP_CONTENT));
		table.addView(table_row);
		curr_column = 1;
	    }
	}
    }

	
    public void refreshLayout() {
	ViewGroup group = (ViewGroup) findViewById(R.id.contents_layout);
	group.removeAllViews();
	buildLayout();
    }

	
    private void show_error_flagment(int reason) {
	FragmentManager fragmentManager = getFragmentManager();
	FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
	ErrorFragment fragment = new ErrorFragment();
	fragment.setErrorReason(reason);
	ViewGroup layout = (ViewGroup)findViewById(R.id.contents_layout);
	layout.removeAllViews();
	fragmentTransaction.add(R.id.contents_layout, fragment);
	fragmentTransaction.commit();
    }

}
