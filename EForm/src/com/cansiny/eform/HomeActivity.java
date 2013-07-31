/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import java.util.concurrent.atomic.AtomicInteger;

import com.cansiny.eform.HomeInfo.HomeItem;
import com.cansiny.eform.Member.MemberListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextSwitcher;
import android.widget.TextView;

/**
 * Home Activity, efrom will start form this activity.
 */
public class HomeActivity extends Activity
    implements OnClickListener, OnLongClickListener, MemberListener
{
    static public final int HOME_VIEW_ID_BASE = 0x3F000001;
    static public final int ITEM_VIEW_ID_BASE = 0x3F100001;

    static public final int ITEM_ACTIVITY_REQUEST_CODE = 1;
	
    private AtomicInteger atomic_int;
    private HomeInfo home_info;

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


    private int curr_slogan = 0;
    private Handler  handler = new Handler();
    private Runnable runable = new Runnable() {
	@Override
	public void run() {
	    TextSwitcher switcher = (TextSwitcher) findViewById(R.id.slogan_switcher);
	    switcher.setText(home_info.slogans.get(curr_slogan++));
	    if (curr_slogan >= home_info.slogans.size())
		curr_slogan = 0;
	    handler.postDelayed(this, 5000);
	}
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	Log.d("HomeActivity", "onCreate");

	app_context = getApplicationContext();
	atomic_int = new AtomicInteger(HOME_VIEW_ID_BASE);

	home_info = HomeInfo.getHomeInfo();
	home_info.loadCustomerRes();

	LogActivity.clearLog();

	setContentView(R.layout.activity_home);
	buildLayout();

	/* catch long click event to popup menu */
	View view = findViewById(R.id.main_layout);
	view.setLongClickable(true);
	view.setOnLongClickListener(this);
	view = findViewById(R.id.contents_frame);
	view.setLongClickable(true);
	view.setOnLongClickListener(this);
    }

    @Override
    protected void onPause() {
	super.onPause();

	Log.d("HomeActivity", "onPause");

	WebView webview = (WebView) findViewById(R.id.logo_webview);
	webview.onPause();

	handler.removeCallbacks(runable);
    }
	
    @Override
    protected void onResume() {
	super.onResume();

	Log.d("HomeActivity", "onResume");

	WebView webview = (WebView) findViewById(R.id.logo_webview);
	webview.onResume();

	TextSwitcher switcher = (TextSwitcher) findViewById(R.id.slogan_switcher);
	switcher.setCurrentText(home_info.slogans.get(curr_slogan));

	handler.postDelayed(runable, 0);
    }

    @Override
    protected void onStart() {
	super.onStart();

	Log.d("HomeActivity", "onStart");

	Member member = Member.getMember();
	member.setListener(this);
	setMemberLayoutVisible(member.isLogin());
    }

    @Override
    protected void onStop() {
	super.onStop();

	Log.d("HomeActivity", "onStop");

	Member member = Member.getMember();
	member.setListener(null);
    }
	
    @Override
    protected void onRestart() {
	super.onRestart();
	Log.d("HomeActivity", "onRestart");
    }
	
    @Override
    public void onDestroy() {
	super.onDestroy();

	Log.d("HomeActivity", "onDestory");

	handler.removeCallbacks(runable);
	Member member = Member.getMember();
	member.logout();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void buildLayout() {
	View layout = findViewById(R.id.root_layout);
	layout.setBackgroundResource(home_info.background);

	WebView webview = (WebView) findViewById(R.id.logo_webview);
	WebSettings settings = webview.getSettings();
	settings.setJavaScriptEnabled(true);
	settings.setPluginState(WebSettings.PluginState.ON);
	if (home_info.banner_url != null) {
	    webview.loadUrl(home_info.banner_url);
	} else {
	    String data = "<span style='color: red; font-size: large'>" +
		    "Assets not found !!!</span><br/><br/>" +
		    "Please contact technology supporter to solve this problem!";
	    webview.loadData(data, "text/html", "");
	}
	webview.setOnTouchListener(new View.OnTouchListener() {
	    @Override
	    public boolean onTouch(View v, MotionEvent event) {
		((WebView) findViewById(R.id.logo_webview)).reload();
	        return true;
	    }
	});

	TextSwitcher switcher = (TextSwitcher) findViewById(R.id.slogan_switcher);
	switcher.setInAnimation(AnimationUtils.loadAnimation(this,
		android.R.anim.fade_in));
	switcher.setOutAnimation(AnimationUtils.loadAnimation(this,
		android.R.anim.fade_out));
	switcher.setCurrentText(home_info.slogans.get(0));

	buildContentsLayout();
    }

	
    private void buildContentsLayout() {
	TableLayout table = new TableLayout(getApplicationContext());
	table.setId(atomic_int.incrementAndGet());

	TableRow table_row = new TableRow(getApplicationContext());
	table_row.setId(atomic_int.incrementAndGet());
	table_row.setGravity(Gravity.CENTER_HORIZONTAL);
	table_row.setLayoutParams(new TableLayout.LayoutParams(
		ViewGroup.LayoutParams.WRAP_CONTENT,
		ViewGroup.LayoutParams.WRAP_CONTENT));
	table.addView(table_row);

	int curr_column = 1;
	for (HomeInfo.HomeItem item : home_info.items) {
	    LinearLayout button = buildImageButton(item);
	    TableRow.LayoutParams row_params = new TableRow.LayoutParams(
		    ViewGroup.LayoutParams.MATCH_PARENT,
		    ViewGroup.LayoutParams.WRAP_CONTENT);
	    if (curr_column < home_info.item_columns) {
		row_params.rightMargin = (int) convertDpToPixel(80);
	    }
	    table_row.addView(button, row_params);

	    if (++curr_column > home_info.item_columns) {
		table_row = new TableRow(getApplicationContext());
		table_row.setGravity(Gravity.CENTER_HORIZONTAL);
		TableLayout.LayoutParams params = new TableLayout.LayoutParams(
			ViewGroup.LayoutParams.WRAP_CONTENT,
			ViewGroup.LayoutParams.WRAP_CONTENT);
		params.topMargin = (int) HomeActivity.convertDpToPixel(40);
		table.addView(table_row, params);
		curr_column = 1;
	    }
	}
	if (home_info.items.size() % home_info.item_columns != 0) {
	    int paddings = home_info.item_columns - 
		    (home_info.items.size() % home_info.item_columns);
	    for (int i = 0; i < paddings; i++) {
		View view = new View(this);
		view.setVisibility(View.INVISIBLE);
		table_row.addView(view);
	    }
	}

	FrameLayout contents = (FrameLayout) findViewById(R.id.contents_frame);
	contents.removeAllViews();
	contents.addView(table, new FrameLayout.LayoutParams(
		ViewGroup.LayoutParams.MATCH_PARENT,
		ViewGroup.LayoutParams.MATCH_PARENT));
    }

	
    private LinearLayout buildImageButton(HomeInfo.HomeItem item) {
	LinearLayout button_layout = new LinearLayout(getApplicationContext());
	button_layout.setId(atomic_int.incrementAndGet());
	button_layout.setOrientation(LinearLayout.VERTICAL);

	ImageButton button = new ImageButton(getApplicationContext());
	button.setId(atomic_int.incrementAndGet());
	button.setTag(item);
	button.setImageResource(item.image);
	button.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
	button.setBackgroundResource(R.drawable.home_button);
	button.setPadding(5, 5, 5, 5);
	button.setClickable(true);
	button.setOnClickListener(this);

	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
		(int) convertDpToPixel(home_info.item_image_size),
		(int) convertDpToPixel(home_info.item_image_size));
	params.gravity = Gravity.CENTER_HORIZONTAL;
	params.bottomMargin = 4;
	button_layout.addView(button, params);

	TextView text_view = new TextView(getApplicationContext());
	text_view.setId(atomic_int.incrementAndGet());
	text_view.setText(item.label);
	text_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, item.label_size);
	text_view.setTextColor(getResources().getColor(R.color.white));
	text_view.setGravity(Gravity.CENTER);

	button_layout.addView(text_view, new LinearLayout.LayoutParams(
		ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

	return button_layout;
    }

	
    public void refreshLayout() {
	ViewGroup group = (ViewGroup) findViewById(R.id.contents_frame);
	group.removeAllViews();
	buildLayout();
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
	    intent.putExtra(FormActivity.INTENT_MESSAGE_MEMBER, Member.getMember().isLogin());
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
		    showErrorFlagment(reason);
		break;
	    case RESULT_OK:
		break;
	    }
	    break;
	}
    }

    @Override
    public boolean onLongClick(View view) {
	switch (view.getId()) {
	case R.id.main_layout:
	case R.id.contents_frame:
	    HomeMenu menu = new HomeMenu();
	    menu.show(getFragmentManager(), "HomeMenu");
	    return true;
	default:
	    return false;
	}
    }


    public void onSloganSwitcherClick(View view) {
	TextSwitcher switcher = (TextSwitcher) findViewById(R.id.slogan_switcher);
	if (++curr_slogan >= home_info.slogans.size())
	    curr_slogan = 0;
	switcher.setText(home_info.slogans.get(curr_slogan));
    }

    private void showErrorFlagment(int reason) {
	FragmentManager fragmentManager = getFragmentManager();
	FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
	ErrorFragment fragment = new ErrorFragment();
	fragment.setErrorReason(reason);
	ViewGroup layout = (ViewGroup)findViewById(R.id.contents_frame);
	layout.removeAllViews();
	fragmentTransaction.add(R.id.contents_frame, fragment);
	fragmentTransaction.commit();
    }


    public void onMemberVoucherButtonClick(View view) {
	
    }


    public void onMemberProfileButtonClick(View view) {
	Member member = Member.getMember();
	member.update(getFragmentManager());
    }


    public void onMemberLogoutButtonClick(View view) {
	Member member = Member.getMember();
	member.logout();
    }


    private void setMemberLayoutVisible(boolean visible) {
	final View view = findViewById(R.id.member_layout);
	TranslateAnimation anim = null;

	if (visible) {
	    view.setVisibility(View.VISIBLE);
	    int width = (view.getWidth() == 0) ? 100 : view.getWidth();
	    anim = new TranslateAnimation(width, 0.0f, 0.0f, 0.0f);
	} else {
	    anim = new TranslateAnimation(0.0f, view.getWidth(), 0.0f, 0.0f);
	    anim.setAnimationListener(new Animation.AnimationListener() {
		public void onAnimationStart(Animation animation) {
		}
		public void onAnimationRepeat(Animation animation) {
		}
		public void onAnimationEnd(Animation animation) {
		    view.setVisibility(View.GONE);
		}
	    });
	}

	AnimationSet anim_set = new AnimationSet(true);
	anim_set.addAnimation(anim);
	anim_set.setDuration(400);
	anim_set.setInterpolator(new AccelerateInterpolator(1.0f));
	view.startAnimation(anim_set);
    }


    @Override
    public void onMemberLogin() {
	setMemberLayoutVisible(true);
    }

    @Override
    public void onMemberLogout() {
	setMemberLayoutVisible(false);
    }

}
