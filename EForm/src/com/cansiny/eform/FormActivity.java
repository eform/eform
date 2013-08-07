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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


public class FormActivity extends Activity implements
	OnClickListener, OnTouchListener, Form.FormListener
{
    /* constants uses for communication with previous activity. */
    static public final String INTENT_MESSAGE_FORMCLASS = "com.cansiny.eform.FORMCLASS";
    static public final String INTENT_MESSAGE_FORMLABEL = "com.cansiny.eform.FORMLABEL";
    static public final String INTENT_MESSAGE_FORMIMAGE = "com.cansiny.eform.FORMIMAGE";
    static public final String INTENT_MESSAGE_VOUCHER   = "com.cansiny.eform.VOUCHER";
    static public final String INTENT_RESULT_ERRREASON  = "com.cansiny.eform.ERRREASON";

    /* constants uses for identified the difference of event source */
    static private final int VIEW_TAG_PAGE_TITLE_BUTTON = 1;
    static private final int VIEW_TAG_MEMBER_SAVE_BUTTON = 2;

    static private final int TIMEOUT_VALUE = 90;

    private AtomicInteger atomic_int;
    private Form form;
    private long last_verify_time = 0;
    private FormPageSwitcher page_switcher;
    private int timeout_remains = TIMEOUT_VALUE;
    private Handler timeout_handler;
    private Runnable timeout_runnable;


    @Override
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
	    String klass = getIntent().getStringExtra(INTENT_MESSAGE_FORMCLASS);
	    if (klass == null)
		throw new ClassNotFoundException("Intent missing 'klass' attribute");

	    /* build form instance from class name */
	    form = (Form) Class.forName(klass).getConstructor(Activity.class).newInstance(this);
	    form.setListener(this);

	    /* insert pages title buttons */
	    ViewGroup page_title_layout = (ViewGroup) findViewById(R.id.page_title_layout);
	    int n = 1;
	    for (Form.FormPage page : form.pages) {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
			 ViewGroup.LayoutParams.WRAP_CONTENT,
			 ViewGroup.LayoutParams.MATCH_PARENT);
		params.weight = 1;
		page_title_layout.addView(buildPageTitleButton(n, page.title), params);
		n++;
	    }

	    Member member = Member.getMember();
	    if (member.isLogin()) {
		addMemberButtons();
	    }
	    View view = findViewById(R.id.form_main_layout);
	    view.setOnTouchListener(this);

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

    @Override
    protected void onDestroy() {
	super.onDestroy();
    }

    @Override
    protected void onStart() {
	super.onStart();

	setActivePage(0);
    }

    @Override
    protected void onStop() {
	super.onStop();
    }

    @Override
    protected void onResume() {
	super.onResume();

	timeout_handler = new Handler();
	timeout_runnable = new Runnable() {
	    @Override
	    public void run() {
		if (timeout_remains <= 30) {
		    setTimeoutTipVisible(true);
		    if (timeout_remains <= 0) {
			finish();
		    }
		}
		timeout_remains--;
		timeout_handler.postDelayed(this, 1000);
	    }
	};
	timeout_handler.postDelayed(timeout_runnable, 1000);
    }

    @Override
    protected void onPause() {
	super.onPause();

	timeout_handler.removeCallbacks(timeout_runnable);
    }

    public Form getForm() {
	return this.form;
    }

    private void setTimeoutTipVisible(boolean visible) {
	final View view = findViewById(R.id.form_tip_layout);
	int state = view.getVisibility();

	AlphaAnimation anim = null;

	if (visible) {
	    TextView textview = ((TextView) findViewById(R.id.tip_timeout_textview));
	    textview.setText("" + timeout_remains);

	    if (state == View.VISIBLE)
		return;

	    view.setVisibility(View.VISIBLE);
	    view.setOnTouchListener(this);
	    anim = new AlphaAnimation(0.0f, 1.0f);
	} else {
	    timeout_remains = TIMEOUT_VALUE;

	    if (state == View.GONE)
		return;

	    anim = new AlphaAnimation(1.0f, 0.0f);
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
	view.startAnimation(anim_set);
    }

    private void addMemberButtons() {
	LinearLayout linear = new LinearLayout(this);
	linear.setOrientation(LinearLayout.VERTICAL);

	Button button = new Button(this);
	button.setId(atomic_int.incrementAndGet());
	button.setTag(VIEW_TAG_MEMBER_SAVE_BUTTON);
	button.setBackgroundResource(R.drawable.save);
	button.setGravity(Gravity.CENTER_HORIZONTAL);
	button.setOnClickListener(this);
	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
		(int) Utils.convertDpToPixel(60),
		(int) Utils.convertDpToPixel(60));
	linear.addView(button, params);

	TextView textview = new TextView(this);
	textview.setText("保 存");
	textview.setTextColor(getResources().getColor(R.color.white));
	textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
	textview.setGravity(Gravity.CENTER_HORIZONTAL);
	params = new LinearLayout.LayoutParams(
		ViewGroup.LayoutParams.WRAP_CONTENT,
		ViewGroup.LayoutParams.WRAP_CONTENT);
	params.topMargin = (int) Utils.convertDpToPixel(2);
	params.gravity = Gravity.CENTER_HORIZONTAL;
	linear.addView(textview, params);

	View print_linear = (View) findViewById(R.id.print_button).getParent();
	ViewGroup parent = (ViewGroup) print_linear.getParent();
	int index = parent.indexOfChild(print_linear);
	params = new LinearLayout.LayoutParams(
		ViewGroup.LayoutParams.WRAP_CONTENT,
		ViewGroup.LayoutParams.WRAP_CONTENT);
	params.topMargin = (int) Utils.convertDpToPixel(20);
	parent.addView(linear, index + 1, params);
    }


    private View buildPageTitleButton(int index, String title) {
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
		(int) Utils.convertDpToPixel(6));
	button_layout.addView(line, line_params);

	return button_layout;
    }

    private void resetPageTitleButtons() {
	LinearLayout page_title = (LinearLayout) findViewById(R.id.page_title_layout);
	for (int i = 0; i < page_title.getChildCount(); i++) {
	    LinearLayout button_layout = (LinearLayout) page_title.getChildAt(i);

	    Button button = (Button) button_layout.getChildAt(0);
	    button.setBackgroundResource(R.color.transparent);
	    button.setTextColor(getResources().getColor(R.color.white));
	    button_layout.getChildAt(1).setBackgroundResource(R.color.blue);
	}
    }

    @Override
    public void onClick(View view) {
	Object object = view.getTag();
	if (object != null) {
	    if (object.getClass() == Integer.class) {
		switch (((Integer) object).intValue()) {
		case VIEW_TAG_PAGE_TITLE_BUTTON: /* page title button clicked */
		    if (page_switcher.getStatus() == AsyncTask.Status.FINISHED) {
			LinearLayout title_layout = (LinearLayout) findViewById(R.id.page_title_layout);
			setActivePage(title_layout.indexOfChild((View) view.getParent()));
		    }
		    break;
		case VIEW_TAG_MEMBER_SAVE_BUTTON:
		    onMemberSaveButtonClick(view);
		    break;
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

    private void setActivePage(int index) {
	page_switcher = new FormPageSwitcher();
	page_switcher.execute(index);
    }

    private class FormPageSwitcher extends AsyncTask<Integer, Void, View>
    {
	private int index;
	private Toast toast;

	@Override
	protected View doInBackground(Integer... args) {
	    index = args[0];

	    if (index == form.getActivePage()) {
		toast.cancel();
		return null;
	    }
	    if (index < 0 || index >= form.pages.size()) {
		LogActivity.writeLog("尝试切换到不存在的页面 %d", index);
		return null;
	    }
	    return form.setActivePage(index);
	}
	
	@Override
	protected void onPreExecute() {
	    /* hide the soft keyboard until user touch the edit text */
	    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	    
	    toast = showToast("正在加载页面...", 0);
	}

	@Override
	protected void onPostExecute(View page_view) {
	    if (page_view == null) return;

	    /* change page title button appearance */
	    resetPageTitleButtons();
	    LinearLayout title_layout = (LinearLayout) findViewById(R.id.page_title_layout);
	    LinearLayout button_layout = (LinearLayout) title_layout.getChildAt(index);
	    Button button = (Button) button_layout.getChildAt(0);
	    button.setBackgroundResource(R.color.darkgray);
	    button.setTextColor(getResources().getColor(R.color.yellow));
	    button_layout.getChildAt(1).setBackgroundResource(R.color.red);

	    /* change previous/next button state */
	    button = (Button) findViewById(R.id.previous_button);
	    button.setEnabled(index > 0);
	    button = (Button) findViewById(R.id.next_button);
	    button.setEnabled(index + 1 < form.pages.size());

	    /* replace form page layout */
	    FrameLayout page_layout = (FrameLayout) findViewById(R.id.page_layout);
	    page_layout.removeAllViews();
	    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
		    ViewGroup.LayoutParams.MATCH_PARENT,
		    ViewGroup.LayoutParams.WRAP_CONTENT);
	    params.gravity = Gravity.CENTER_VERTICAL;
	    page_layout.addView(page_view, params);
	    page_layout.forceLayout();

	    toast.cancel();
	    form.onPageStart(index);
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
    public void onFormPageScrolled(Form form, ScrollView scroll_view) {
	Form.FormPage page = form.getActiveFormPage();
	findViewById(R.id.up_button).setEnabled((page.canScrollUp()));
	findViewById(R.id.down_button).setEnabled((page.canScrollDown()));
    }


    public Toast showToast(CharSequence sequence, int image) {
	Toast toast = Utils.makeToast(sequence, image,
		Utils.IMAGE_SIZE_LARGE, Toast.LENGTH_SHORT);
	View sidebar = findViewById(R.id.sidebar_layout);
	toast.setGravity(Gravity.CENTER, 0 - sidebar.getWidth() / 2, 0);
	toast.show();

	return toast;
    }


    public void onVerifyButtonClick(View view) {
	/* prevent click too fast */
	long time = System.currentTimeMillis() / 1000;
	if (time - last_verify_time < 3)
	    return;

	last_verify_time = time;

	int retval = form.verify();
	if (retval > 0) {
	    showToast(String.format("本页共 %d 个必填项未填写，点击警告标志可查看详细说明...", retval),
		    R.drawable.cry);
	} else {
	    showToast("检查完成，没有发现问题！", R.drawable.smile);
	}
    }


    public void onPrintButtonClick(View view) {
	form.print();
    }


    public void onExitButtonClick(View view) {
	setResult(RESULT_OK);
	finish();
    }

    public void onMemberSaveButtonClick(View view) {
	Member member = Member.getMember();
	if (!member.isLogin()) {
	    showToast("会员未登录或已退出登录！", R.drawable.cry);
	    return;
	}

	long voucher_id = getIntent().getLongExtra(INTENT_MESSAGE_VOUCHER, -1);
	if (voucher_id == -1) {
	    VoucherInsertDialog dialog = new VoucherInsertDialog();
	    dialog.show(getFragmentManager(), "VoucherInsertDialog");
	} else {
	    
	}
    }


    @Override
    public boolean onTouch(View arg0, MotionEvent event) {
	if (event.getAction() == MotionEvent.ACTION_DOWN) {
	    setTimeoutTipVisible(false);
	}
	return false;
    }

    @Override
    public void onFormViewTouched(Form form, View view) {
	setTimeoutTipVisible(false);
    }

    @Override
    public void onFormTextChanged(Form form, EditText textview) {
	setTimeoutTipVisible(false);
    }

}


class VoucherInsertDialog extends Utils.DialogFragment
{
    private EditText comment_edittext;

    private View buildLayout() {
	LinearLayout layout = new LinearLayout(getActivity());
	layout.setOrientation(LinearLayout.VERTICAL);
	layout.setPadding(10, 10, 10, 10);

	TextView textview = new TextView(getActivity());
	textview.setText("请简单描述一下要保存的凭证信息");
	textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
	layout.addView(textview);

	LinearLayout linear = new LinearLayout(getActivity());
	linear.setOrientation(LinearLayout.HORIZONTAL);
	linear.setGravity(Gravity.CENTER_VERTICAL);
	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
		ViewGroup.LayoutParams.MATCH_PARENT,
		ViewGroup.LayoutParams.WRAP_CONTENT);
	params.topMargin = 20;
	layout.addView(linear, params);

//	textview = new TextView(getActivity());
//	textview.setText("描 述：");
//	textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
//	linear.addView(textview);
//
	comment_edittext = new EditText(getActivity());
	params = new LinearLayout.LayoutParams(
		0, ViewGroup.LayoutParams.WRAP_CONTENT);
	params.weight = 1;
	linear.addView(comment_edittext, params);

	addClearButton(linear);

	return layout;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	super.onCreateDialog(savedInstanceState);

	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

	builder.setTitle("保存凭条");
	builder.setView(buildLayout());
	builder.setNegativeButton("放 弃", null);
	builder.setPositiveButton("保 存", null);

	return builder.create();
    }

    @Override
    public void onStart() {
	super.onStart();

	final AlertDialog dialog = (AlertDialog) getDialog();

	Button button = dialog.getButton(Dialog.BUTTON_NEGATIVE);
	button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

	button = dialog.getButton(Dialog.BUTTON_POSITIVE);
	button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
	button.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View view) {
		Member member = Member.getMember();
		if (!member.isLogin() || member.getProfile() == null) {
		    LogActivity.writeLog("程序错误，不能得到会员信息");
		    dismiss();
		    return;
		}
		Voucher voucher = new Voucher();

		voucher.userid = member.getProfile().rowid;
		voucher.comment = comment_edittext.getText().toString();
		if (voucher.comment.length() == 0) {
		    comment_edittext.setHint("描述信息不能为空");
		    comment_edittext.setHintTextColor(getResources().getColor(R.color.red));
		    return;
		}
		Intent intent = getActivity().getIntent();
		int formlabel = intent.getIntExtra(FormActivity.INTENT_MESSAGE_FORMLABEL, 0);
		if (formlabel == 0) {
		    LogActivity.writeLog("程序错误，不能得到Form的标题(formlabel)，请检查传递的Intent参数");
		    dismiss();
		    return;
		}
		voucher.formclass = intent.getStringExtra(FormActivity.INTENT_MESSAGE_FORMCLASS);
		voucher.formlabel = getResources().getString(formlabel);
		voucher.formimage = intent.getIntExtra(FormActivity.INTENT_MESSAGE_FORMIMAGE, 0);
		voucher.contents = ((FormActivity) getActivity()).getForm().getPagesContents();

		if (voucher.insert(getActivity())) {
		    Utils.showToast("凭条信息保存成功！", R.drawable.smile);
		} else {
		    Utils.showToast("凭条信息保存失败！", R.drawable.cry);
		}
		dismiss();
	    }
	});
    }

}
