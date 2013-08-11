/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
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
    static public final String INTENT_MESSAGE_VOUCHER   = "com.cansiny.eform.VOUCHER";
    static public final String INTENT_RESULT_ERRREASON  = "com.cansiny.eform.ERRREASON";

    /* constants uses for identified the difference of event source */
    static private final int VIEW_TAG_PAGE_TITLE_BUTTON = 1;
    static private final int VIEW_TAG_MEMBER_SAVE_BUTTON = 2;

    static private final int TIMEOUT_VALUE = 90;

    private AtomicInteger atomic_int;
    private Form form;
    private Voucher voucher;
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
	    voucher = getIntent().getParcelableExtra(INTENT_MESSAGE_VOUCHER);
	    if (voucher == null)
		throw new ClassNotFoundException("Intent missing 'voucher' attribute");

	    /* build form instance from class name */
	    form = (Form) Class.forName(voucher.getFormClass()).
		    getConstructor(Activity.class, String.class).
		    newInstance(this, voucher.getFormLabel());
	    form.setPagesContents(voucher.getContents());
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

	voucher.setDialogListener(new Voucher.VoucherDialogListener() {
	    @Override
	    public void onCommentTextChanged() {
		timeout_remains = TIMEOUT_VALUE;
		View view = findViewById(R.id.form_tip_layout);
		if (view.getVisibility() != View.GONE)
		    setTimeoutTipVisible(false);
	    }
	});
	setActivePage(0);
    }

    @Override
    protected void onStop() {
	super.onStop();

	voucher.setDialogListener(null);
    }

    @Override
    protected void onResume() {
	super.onResume();

	timeout_handler = new Handler();
	timeout_runnable = new Runnable() {
	    @Override
	    public void run() {
		if (timeout_remains <= 30) {
		    View view = findViewById(R.id.form_tip_layout);
		    if (view.getVisibility() != View.VISIBLE) {
			setTimeoutTipVisible(true);
		    } else {
			if (timeout_remains % 2 == 1) {
			    view.setBackgroundResource(R.color.yellow);
			} else {
			    view.setBackgroundResource(R.color.white);
			}
		    }
		    TextView textview = ((TextView) findViewById(R.id.form_tip_timeout_textview));
		    textview.setText("" + timeout_remains);

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
	final View layout = findViewById(R.id.form_tip_layout);
	AlphaAnimation anim = null;

	if (visible) {
	    layout.setVisibility(View.VISIBLE);
	    layout.setBackgroundResource(R.color.white);
	    for (int i = 0; i < ((ViewGroup) layout).getChildCount(); i++) {
		TextView textview = (TextView) ((ViewGroup) layout).getChildAt(i);
		if (textview.getId() == R.id.form_tip_timeout_textview)
		    textview.setTextColor(getResources().getColor(R.color.red));
		else
		    textview.setTextColor(getResources().getColor(R.color.black));
	    }
	    layout.setOnTouchListener(this);
	    anim = new AlphaAnimation(0.0f, 1.0f);
	} else {
	    anim = new AlphaAnimation(1.0f, 0.0f);
	    anim.setAnimationListener(new Animation.AnimationListener() {
		public void onAnimationStart(Animation animation) {
		}
		public void onAnimationRepeat(Animation animation) {
		}
		public void onAnimationEnd(Animation animation) {
		    layout.setOnTouchListener(null);
		    layout.setBackgroundResource(R.color.transparent);
		    for (int i = 0; i < ((ViewGroup) layout).getChildCount(); i++) {
			TextView textview = (TextView) ((ViewGroup) layout).getChildAt(i);
			textview.setTextColor(getResources().getColor(R.color.transparent));
		    }
		    layout.clearAnimation();
		    layout.setVisibility(View.GONE);
		}
	    });
	}

	AnimationSet anim_set = new AnimationSet(true);
	anim_set.addAnimation(anim);
	anim_set.setDuration(400);
	layout.startAnimation(anim_set);
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
	protected void onPreExecute() {
	    /* hide the soft keyboard until user touch the edit text */
	    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	    toast = showToast("正在加载页面...", 0);
	}

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
	    form.scrollPageUp();
	}
    }

    public void onScrollDownButtonClick(View view) {
	if (page_switcher.getStatus() == AsyncTask.Status.FINISHED) {
	    form.scrollPageDown();
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
	File cachedir = getCacheDir();
	try {
	    String fname = voucher.getFormClass() + ".xml";
	    FileOutputStream stream = new FileOutputStream(new File(cachedir, fname));
	    stream.write(form.toPrintTemplate().getBytes("utf-8"));
	} catch (Exception e) {
	    LogActivity.writeLog(e);
	}
	PrintDialog dialog = new PrintDialog();
	dialog.show(getFragmentManager(), "PrintDialog");
    }


    public void onExitButtonClick(View view) {
	setResult(RESULT_OK);
	finish();
    }

    public void onMemberSaveButtonClick(View view) {
	Member member = Member.getMember();
	if (!member.isLogin()) {
	    LogActivity.writeLog("会员还未登录???");
	    return;
	}

	voucher.setContents(form.getPagesContents());

	if (voucher.getRowid() == -1) {
	    voucher.insert(getFragmentManager());
	} else {
	    voucher.replace(getFragmentManager());
	}
    }


    @Override
    public boolean onTouch(View arg0, MotionEvent event) {
	if (event.getAction() == MotionEvent.ACTION_DOWN) {
	    timeout_remains = TIMEOUT_VALUE;
	    setTimeoutTipVisible(false);
	}
	return false;
    }

    @Override
    public void onFormViewTouched(Form form, View view) {
	timeout_remains = TIMEOUT_VALUE;
	setTimeoutTipVisible(false);
    }

    @Override
    public void onFormTextChanged(Form form, EditText textview) {
	timeout_remains = TIMEOUT_VALUE;
	setTimeoutTipVisible(false);
    }

}

class PrintDialog extends Utils.DialogFragment
{
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	super.onCreateDialog(savedInstanceState);

	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

	builder.setTitle("打印凭条");
	LayoutInflater inflater = getActivity().getLayoutInflater();
	builder.setView(inflater.inflate(R.layout.dialog_print, null));
	builder.setNegativeButton("取 消", null);
	builder.setPositiveButton("打 印", null);

	return builder.create();
    }

    @Override
    public void onStart() {
	super.onStart();

	final AlertDialog dialog = (AlertDialog) getDialog();

	View view = dialog.findViewById(R.id.coord_adjust_textview);
	view.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View view) {
		View edittext = dialog.findViewById(R.id.coord_adjust_x_edittext);
		edittext.setEnabled(!edittext.isEnabled());
		edittext.requestFocus();
		edittext = dialog.findViewById(R.id.coord_adjust_y_edittext);
		edittext.setEnabled(!edittext.isEnabled());

		if (edittext.isEnabled()) {
		    ((TextView) view).setTextColor(getResources().getColor(R.color.darkred));
		} else {
		    ((TextView) view).setTextColor(getResources().getColor(R.color.darkgray));
		}
	    }
	});

	Button button = (Button) dialog.findViewById(R.id.tips_button);
	button.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View view) {
		showToastLong("有时，同样的凭条因为切纸原因导致左边和上边的留白存在差异，" +
			"可以通过调整打印坐标来克服这个问题。\n\n如横向偏移小于0，" +
			"表示所有打印元素往左边偏移指定的距离，大于0则表示整体向右边偏移指定的距离，纵向亦然。");
	    }
	});
//	Button button = dialog.getButton(Dialog.BUTTON_POSITIVE);
//	button.setOnClickListener(new View.OnClickListener() {
//	    @Override
//	    public void onClick(View view) {
//		if (voucher == null || voucher.getRowid() < 0) {
//		    LogActivity.writeLog("不能更新凭条，voucher成员没有正确设置(程序错误)");
//		    return;
//		}
//
//		String comment = comment_edittext.getText().toString();
//		if (comment.length() == 0) {
//		    comment_edittext.setHint("描述信息不能为空");
//		    comment_edittext.setHintTextColor(getResources().getColor(R.color.red));
//		    return;
//		}
//		voucher.setComment(comment);
//
//		hideIme(view);
//
//		if (voucher.update(getActivity())) {
//		    Utils.showToast("凭条信息更新成功！", R.drawable.smile);
//		} else {
//		    Utils.showToast("凭条信息更新失败！", R.drawable.cry);
//		}
//		dismiss();
//	    }
//	});
    }

}