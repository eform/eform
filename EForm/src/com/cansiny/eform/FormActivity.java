/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class FormActivity extends Activity implements OnClickListener, Form.FormListener
{
    /* constants uses for communication with previous activity. */
    static public final String INTENT_MESSAGE_CLASS    = "com.cansiny.eform.CLASS";
    static public final String INTENT_MESSAGE_LABEL    = "com.cansiny.eform.LABEL";
    static public final String INTENT_RESULT_ERRREASON = "com.cansiny.eform.ERRREASON";

    /* constants uses for identified the difference of event source */
    static private final String VIEW_TAG_PAGE_TITLE_BUTTON = "com.cansiny.eform.PAGE_TITLE_BUTTON";

    static private final int TOAST_IMAGE_NONE  = 0;
    static private final int TOAST_IMAGE_SMILE = -1;
    static private final int TOAST_IMAGE_CRY   = -2;
    
    private AtomicInteger atomic_int;
    private Form form;
    private long last_verify_time = 0;
    private FormPageSwitcher page_switcher;

    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	atomic_int = new AtomicInteger(HomeActivity.ITEM_VIEW_ID_BASE);

	setContentView(R.layout.activity_form);

	/* set activity background */
	HomeInfo home_info = HomeInfo.getHomeInfo();
	findViewById(R.id.form_layout).setBackgroundResource(home_info.background);

	/* show today date */
	String date = new SimpleDateFormat("yyyy年MM月dd日 EEE",
		   Locale.CHINA).format(Calendar.getInstance().getTime());
	((TextView) findViewById(R.id.date_textview)).setText(date);

	try {
	    /* get form class from home activity */
	    String klass = getIntent().getStringExtra(INTENT_MESSAGE_CLASS);
	    if (klass == null)
		throw new ClassNotFoundException("Intent missing 'klass' attribute");

	    /* build form instance from class name */
	    form = (Form) Class.forName(klass).getConstructor(Activity.class).newInstance(this);
	    form.setListener(this);

	    /* insert pages title */
	    ViewGroup page_title = (ViewGroup) findViewById(R.id.page_title_layout);
	    int n = 1;
	    for (Form.FormPage page : form.pages) {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
			 ViewGroup.LayoutParams.WRAP_CONTENT,
			 ViewGroup.LayoutParams.MATCH_PARENT);
		params.weight = 1;
		page_title.addView(buildPageButton(n, page.title), params);
		n++;
	    }
	    setResult(RESULT_OK, getIntent());
	} catch (InstantiationException e) {
	    LogActivity.writeLog(e);
	    getIntent().putExtra(INTENT_RESULT_ERRREASON, R.string.error_instantiation_exception);
	    setResult(RESULT_CANCELED, getIntent());
	    finish();
	} catch (IllegalAccessException e) {
	    LogActivity.writeLog(e);
	    getIntent().putExtra(INTENT_RESULT_ERRREASON, R.string.error_instantiation_exception);
	    setResult(RESULT_CANCELED, getIntent());
	    finish();
	} catch (ClassNotFoundException e) {
	    LogActivity.writeLog(e);
	    getIntent().putExtra(INTENT_RESULT_ERRREASON, R.string.error_class_not_found);
	    setResult(RESULT_CANCELED, getIntent());
	    finish();
	} catch (IllegalArgumentException e) {
	    LogActivity.writeLog(e);
	    getIntent().putExtra(INTENT_RESULT_ERRREASON, R.string.error_instantiation_exception);
	    setResult(RESULT_CANCELED, getIntent());
	    finish();
	} catch (InvocationTargetException e) {
	    LogActivity.writeLog(e);
	    getIntent().putExtra(INTENT_RESULT_ERRREASON, R.string.error_instantiation_exception);
	    setResult(RESULT_CANCELED, getIntent());
	    finish();
	} catch (NoSuchMethodException e) {
	    LogActivity.writeLog(e);
	    getIntent().putExtra(INTENT_RESULT_ERRREASON, R.string.error_instantiation_exception);
	    setResult(RESULT_CANCELED, getIntent());
	    finish();
	}		
    }
	

    protected void onStart() {
	super.onStart();

	/* default active the first step */
	setActivePage(0);
    }

	
    private View buildPageButton(int index, String title) {
	LinearLayout button_layout = new LinearLayout(getApplicationContext());
	button_layout.setId(atomic_int.incrementAndGet());
	button_layout.setOrientation(LinearLayout.VERTICAL);
		
	Button button = new Button(getApplicationContext());
	button.setId(atomic_int.incrementAndGet());
	button.setTag(VIEW_TAG_PAGE_TITLE_BUTTON);
	button.setText(String.format("%d.%s", index, title));
	button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
	button.setClickable(true);
	button.setOnClickListener(this);

	LinearLayout.LayoutParams button_params = new LinearLayout.LayoutParams(
		ViewGroup.LayoutParams.WRAP_CONTENT,
		ViewGroup.LayoutParams.MATCH_PARENT);
	button_params.gravity = Gravity.CENTER;
	button_params.weight = 1;
	button_layout.addView(button, button_params);

	View line = new View(getApplicationContext());
	line.setId(atomic_int.incrementAndGet());

	LinearLayout.LayoutParams line_params = new LinearLayout.LayoutParams(
		ViewGroup.LayoutParams.MATCH_PARENT,
		(int) HomeActivity.convertDpToPixel(6));
	button_layout.addView(line, line_params);

	return button_layout;
    }

    private void resetTitleButtons() {
	LinearLayout page_title = (LinearLayout) findViewById(R.id.page_title_layout);
	for (int i = 0; i < page_title.getChildCount(); i++) {
	    LinearLayout button_layout = (LinearLayout) page_title.getChildAt(i);

	    Button button = (Button) button_layout.getChildAt(0);
	    button.setBackgroundResource(R.color.transparent);
	    button.setTextColor(getResources().getColor(R.color.white));
	    button_layout.getChildAt(1).setBackgroundResource(R.color.blue);
	}
    }

    private void setActivePage(int index) {
	page_switcher = new FormPageSwitcher();
	page_switcher.execute(index);
    }

    @Override
    public void onClick(View view) {
	Object object = view.getTag();
	if (object == null) {
	    Log.e("ItemActivity", "Object " + view.toString() + " missing tag");
	    return;
	}

	if (object.getClass() == String.class) {
	    String string = (String) object;
			
	    /* page title button clicked */
	    if (string.equals(VIEW_TAG_PAGE_TITLE_BUTTON)) {
		if (page_switcher.getStatus() == AsyncTask.Status.FINISHED) {
		    LinearLayout step_layout = (LinearLayout) findViewById(R.id.page_title_layout);
		    setActivePage(step_layout.indexOfChild((View) view.getParent()));
		}
	    }
	}
    }

    public void onPreviousButtonClick(View view) {
	if (form.getActivePage() > 0) {
	    if (page_switcher.getStatus() == AsyncTask.Status.FINISHED) {
		setActivePage(form.getActivePage() - 1);
	    }
	}
    }

    public void onNextButtonClick(View view) {
	if (form.getActivePage() + 1 < form.pages.size()) {
	    if (page_switcher.getStatus() == AsyncTask.Status.FINISHED) {
		setActivePage(form.getActivePage() + 1);
	    }
	}
    }

    
    private class FormPageSwitcher extends AsyncTask<Integer, Void, View>
    {
	private int index;
	private Toast toast;

	@Override
	protected View doInBackground(Integer... args) {
	    this.index = args[0];

	    if (index == form.getActivePage()) {
		toast.cancel();
		return null;
	    }

	    LinearLayout title_layout = (LinearLayout) findViewById(R.id.page_title_layout);
	    if (index >= title_layout.getChildCount()) {
		Log.e("ItemActivity", String.format("Try to goto unexists page %d", index));
		return null;
	    }
	    return form.setActivePage(index);
	}
	
	@Override
	protected void onPreExecute() {
	    this.toast = showToast("正在加载页面...");
	}

	@Override
	protected void onPostExecute(View view) {
	    if (view == null) return;

	    /* change page button appearance */
	    resetTitleButtons();
	    LinearLayout title_layout = (LinearLayout) findViewById(R.id.page_title_layout);
	    LinearLayout button_layout = (LinearLayout) title_layout.getChildAt(index);
	    Button button = (Button) button_layout.getChildAt(0);
	    button.setBackgroundResource(R.color.drakgray);
	    button.setTextColor(getResources().getColor(R.color.yellow));
	    button_layout.getChildAt(1).setBackgroundResource(R.color.red);

	    /* change previous/next button state */
	    button = (Button) findViewById(R.id.previous_button);
	    button.setEnabled(index > 0);
	    button = (Button) findViewById(R.id.next_button);
	    button.setEnabled(index + 1 < form.pages.size());

	    FrameLayout page_layout = (FrameLayout) findViewById(R.id.page_layout);
	    page_layout.removeAllViews();
	    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
		    ViewGroup.LayoutParams.MATCH_PARENT,
		    ViewGroup.LayoutParams.WRAP_CONTENT);
	    params.gravity = Gravity.CENTER_VERTICAL;
	    page_layout.addView(view, params);
	    page_layout.forceLayout();

	    form.onPageStart();

	    /* hide the soft keyboard until user touch the edit text */
	    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	    
	    toast.cancel();
	}
    }


    public void onScrollUpButtonClick(View view) {
	if (page_switcher.getStatus() == AsyncTask.Status.FINISHED) {
	    if (!form.scrollUp())
		findViewById(R.id.up_button).setEnabled(false);
	}
    }

    public void onScrollDownButtonClick(View view) {
	if (page_switcher.getStatus() == AsyncTask.Status.FINISHED) {
	    if (!form.scrollDown())
		findViewById(R.id.down_button).setEnabled(false);
	}
    }


    @Override
    public void onScrollViewScrolled(Form form, ScrollView scroll_view) {
	Form.FormPage page = form.getActiveFormPage();
	findViewById(R.id.up_button).setEnabled((page.canScrollUp()));
	findViewById(R.id.down_button).setEnabled((page.canScrollDown()));
    }


    public Toast showToast(CharSequence sequence, int icon) {
	LinearLayout layout = new LinearLayout(getApplicationContext());
	layout.setBackgroundResource(R.color.translucence);
	layout.setPadding(20, 10, 20, 10);

	if (icon < 0) {
	    ImageView image = new ImageView(getApplicationContext());
	    switch (icon) {
	    case TOAST_IMAGE_SMILE:
		image.setBackgroundResource(R.drawable.smile);
		break;
	    case TOAST_IMAGE_CRY:
		image.setBackgroundResource(R.drawable.cry);
		break;
	    }
	    image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

	    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(60, 60);
	    params.gravity = Gravity.CENTER_VERTICAL;
	    params.rightMargin = 10;
	    layout.addView(image, params);
	}

	TextView text_view = new TextView(getApplicationContext());
	text_view.setText(sequence);
	text_view.setTextColor(getResources().getColor(R.color.yellow));
	text_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
	text_view.setGravity(Gravity.CENTER_VERTICAL);

	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
		ViewGroup.LayoutParams.MATCH_PARENT,
		ViewGroup.LayoutParams.MATCH_PARENT);
	params.gravity = Gravity.CENTER_VERTICAL;
	layout.addView(text_view, params);

	Toast toast = new Toast(getApplicationContext());
	toast.setView(layout);
	toast.setDuration(Toast.LENGTH_SHORT);
	View sidebar = findViewById(R.id.sidebar_layout);
	toast.setGravity(Gravity.CENTER, 0 - sidebar.getWidth() / 2, 0);
	toast.show();
	return toast;
    }

    public Toast showToast(CharSequence sequence) {
	return showToast(sequence, TOAST_IMAGE_NONE);
    }
   

    public void onVerifyButtonClick(View view) {
	/* prevent click too fast */
	long time = System.currentTimeMillis() / 1000;
	if (time - last_verify_time < 3)
	    return;

	last_verify_time = time;

	int retval = form.verify();
	if (retval > 0) {
	    showToast(String.format("本页共 %d 个必填项需要完善，点击警告标志可查看详细说明", retval),
		    TOAST_IMAGE_CRY);
	} else {
	    showToast("本页所有必填项已填写 ...", TOAST_IMAGE_SMILE);
	}
    }

	
    public void onPrintButtonClick(View view) {
	form.print();
    }

	
    public void onExitButtonClick(View view) {
	setResult(RESULT_OK);
	finish();
    }
}
