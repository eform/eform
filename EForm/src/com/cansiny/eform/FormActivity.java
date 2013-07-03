package com.cansiny.eform;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
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
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class FormActivity extends Activity implements OnClickListener, Form.FormListener
{
	/* constants uses for communication with previous activity. */
	static public final String INTENT_MESSAGE_CLASS    = "com.cansiny.eform.CLASS";
	static public final String INTENT_MESSAGE_LABEL    = "com.cansiny.eform.LABEL";
	static public final String INTENT_RESULT_ERRREASON = "com.cansiny.eform.ERRREASON";

	/* constants uses for identified the difference of event source */
	static private final String VIEW_TAG_PAGE_TITLE_BUTTON = "com.cansiny.eform.PAGE_TITLE_BUTTON";

	private AtomicInteger atomic_int;
	private Form form;
	private int active_page;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		atomic_int = new AtomicInteger(HomeActivity.ITEM_VIEW_ID_BASE);
		active_page = -1;

		setContentView(R.layout.activity_form);

		/* set activity background */
		HomeInfo home_info = HomeInfo.getHomeInfo();
		findViewById(R.id.form_layout).setBackgroundResource(home_info.background);

		/* show today date */
		String date = new SimpleDateFormat("yyyyƒÍMM‘¬dd»’ EEE",
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
		if (index == active_page)
			return;

		LinearLayout title_layout = (LinearLayout) findViewById(R.id.page_title_layout);

		if (index >= title_layout.getChildCount()) {
			Log.e("ItemActivity", String.format("Try to goto unexists step %d", index));
			return;
		}
		resetTitleButtons();

		/* change page button appearance */
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

		/* save current page state */
		if (active_page >= 0) {
			Form.FormPage page = form.pages.get(active_page);
			page.saveState();
		}

		/* restore new page state and insert into layout */
		Form.FormPage page = form.pages.get(index);
		View view = page.restoreState();

		FrameLayout page_layout = (FrameLayout) findViewById(R.id.page_layout);
		page_layout.removeAllViews();
		page_layout.addView(view, new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));

		/* change up/down button state */
		findViewById(R.id.up_button).setEnabled((page.canScrollUp()));
		findViewById(R.id.down_button).setEnabled((page.canScrollDown()));

		/* save active page no and notify form start */
		active_page = index;
		form.onPageStart(index);

		/* hide the soft keyboard until user touch the edit text */
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
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
				LinearLayout step_layout = (LinearLayout) findViewById(R.id.page_title_layout);
				setActivePage(step_layout.indexOfChild((View) view.getParent()));
			}
		}
	}

	public void onPreviousButtonClick(View view) {
		if (active_page > 0)
			setActivePage(active_page - 1);
	}
	public void onNextButtonClick(View view) {
		if (active_page + 1 < form.pages.size())
			setActivePage(active_page + 1);
	}

	public void onScrollUpButtonClick(View view) {
		Form.FormPage page = form.pages.get(active_page);
		page.scrollUp();
	}
	public void onScrollDownButtonClick(View view) {
		Form.FormPage page = form.pages.get(active_page);
		page.scrollDown();
	}


	@Override
	public void onScrollViewScrolled(Form form, ScrollView scroll_view) {
		Form.FormPage page = form.pages.get(active_page);
		findViewById(R.id.up_button).setEnabled((page.canScrollUp()));
		findViewById(R.id.down_button).setEnabled((page.canScrollDown()));
	}
	
	
	public void onVerifyButtonClick(View view) {
		form.verify();
	}

	
	public void onPrintButtonClick(View view) {
		form.print();
	}
	
	
	public void onExitButtonClick(View view) {
		setResult(RESULT_OK);
		finish();
	}
}
