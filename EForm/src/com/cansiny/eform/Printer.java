/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.cansiny.eform.Utils.Device;
import com.cansiny.eform.Utils.IntegerAdapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.util.SparseArray;
import android.util.TypedValue;
import android.util.Xml;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;


class PrinterPageSetupDialog extends Utils.DialogFragment
{
    private Printer printer;

    public PrinterPageSetupDialog(Printer printer) {
	this.printer = printer;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	super.onCreateDialog(savedInstanceState);

	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	builder.setTitle("页面设置");

	LinearLayout linear = new LinearLayout(getActivity());
	linear.setOrientation(LinearLayout.VERTICAL);

	ListView listview = new ListView(getActivity());
	PageSetupAdapter adapter = new PageSetupAdapter(printer.getForm());
	listview.setAdapter(adapter);
	linear.addView(listview);

	TextView textview = new TextView(getActivity());
	textview.setText("有时同样的凭条因为切纸原因导致左边和上边的留白存在差异，" +
		"可以通过调整页边距来克服这个问题。\n如左边距小于0，" +
		"表示所有打印元素往左边偏移指定的距离，大于0则表示整体向右边偏移指定的距离，" +
		"上边距则用于调整上下偏移。\n" +
		"偏移单位为毫米。");
	textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
	textview.setPadding(10, 10, 10, 5);
	textview.setLineSpacing(1, 1.2f);
	textview.setTextColor(getActivity().getResources().getColor(R.color.black));
	linear.addView(textview);

	builder.setView(linear);

	return builder.create();
    }

    @Override
    public void onStart() {
	super.onStart();

	Preferences prefs = Preferences.getPreferences();
	prefs.beginTransaction();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
	super.onDismiss(dialog);

	Preferences prefs = Preferences.getPreferences();
	prefs.endTransaction();
    }

    class PageSetupAdapter extends BaseAdapter
    	implements Utils.GenericTextWatcher.TextWatcherListener
    {
	private Form form;

	public PageSetupAdapter(Form form) {
	    this.form = form;
	}

	@Override
	public int getCount() {
	    return form.getPageCount();
	}

	@Override
	public Object getItem(int position) {
	    if (position < form.getPageCount()) {
		return form.getPage(position);
	    } else {
		return null;
	    }
	}

	@Override
	public long getItemId(int position) {
	    return position;
	}

	private View marginSetView(Context context, int page_no, boolean isleft) {
	    LinearLayout linear = new LinearLayout(context);
	    linear.setPadding(10, 5, 10, 5);
	    linear.setGravity(Gravity.CENTER_VERTICAL);
	    linear.setBackgroundResource(R.color.yellow);

	    TextView textview = new TextView(context);
	    if (isleft) {
		textview.setText("左边距");
	    } else {
		textview.setText("上边距");
	    }
	    textview.setTextColor(context.getResources().getColor(R.color.darkblue));
	    textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
	    linear.addView(textview);

	    Preferences prefs = Preferences.getPreferences();

	    EditText edittext = new EditText(context);
	    edittext.setTag(R.id.PrintPageSetupPageNoKey, page_no);
	    edittext.setTag(R.id.PrintPageSetupIsLeftKey, isleft);
	    edittext.setInputType(InputType.TYPE_CLASS_NUMBER);
	    edittext.setSingleLine();
	    edittext.setMinEms(3);
	    int margin = 0;
	    if (isleft) {
		margin = prefs.getPageLeftMargin(form, page_no);
	    } else {
		margin = prefs.getPageTopMargin(form, page_no);
	    }
	    edittext.setText(String.valueOf(margin));
	    edittext.setTextColor(context.getResources().getColor(R.color.purple));
	    edittext.addTextChangedListener(new Utils.GenericTextWatcher(edittext, this));
	    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
		    0, ViewGroup.LayoutParams.WRAP_CONTENT);
	    params.weight = 1;
	    params.leftMargin = 2;
	    params.rightMargin = 2;
	    linear.addView(edittext, params);

	    Button button = new Button(context);
	    button.setTag(edittext);
	    button.setBackgroundResource(R.drawable.add);
	    button.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View view) {
		    EditText edittext = (EditText) view.getTag();
		    int value = Integer.parseInt(edittext.getText().toString());
		    edittext.setText(String.valueOf(value + 1));
		}
	    });
	    params = new LinearLayout.LayoutParams(
		    (int) Utils.convertDpToPixel(24),
		    (int) Utils.convertDpToPixel(24));
	    params.leftMargin = 2;
	    linear.addView(button, params);

	    button = new Button(context);
	    button.setTag(edittext);
	    button.setBackgroundResource(R.drawable.subtract);
	    button.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View view) {
		    EditText edittext = (EditText) view.getTag();
		    int value = Integer.parseInt(edittext.getText().toString());
		    edittext.setText(String.valueOf(value - 1));
		}
	    });
	    params = new LinearLayout.LayoutParams(
		    (int) Utils.convertDpToPixel(24),
		    (int) Utils.convertDpToPixel(24));
	    params.leftMargin = 6;
	    linear.addView(button, params);

	    return linear;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    Context context = parent.getContext();

	    Form.FormPage page = form.getPage(position);
	    if (page == null)
		return null;

	    LinearLayout linear = new LinearLayout(context);
	    linear.setGravity(Gravity.CENTER_VERTICAL);

	    TextView textview = new TextView(context);
	    textview.setText("页" + (position + 1) + "." + page.getTitle());
	    textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
	    textview.setPadding(10, 0, 10, 0);
	    textview.setGravity(Gravity.CENTER_VERTICAL);
	    textview.setTextColor(context.getResources().getColor(R.color.blue));
	    textview.setBackgroundResource(R.color.silver);
	    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
		    ViewGroup.LayoutParams.WRAP_CONTENT,
		    ViewGroup.LayoutParams.MATCH_PARENT);
	    linear.addView(textview, params);

	    params = new LinearLayout.LayoutParams(
		    ViewGroup.LayoutParams.WRAP_CONTENT,
		    ViewGroup.LayoutParams.WRAP_CONTENT);
	    params.weight = 1;
	    params.leftMargin = 1;
	    linear.addView(marginSetView(context, position, true), params);

	    params = new LinearLayout.LayoutParams(
		    ViewGroup.LayoutParams.WRAP_CONTENT,
		    ViewGroup.LayoutParams.WRAP_CONTENT);
	    params.weight = 1;
	    params.leftMargin = 1;
	    linear.addView(marginSetView(context, position, false), params);

	    return linear;
	}

	@Override
	public void afterTextChanged(TextView textview, Editable editable) {
	    Preferences prefs = Preferences.getPreferences();

	    Integer page_no = (Integer) textview.getTag(R.id.PrintPageSetupPageNoKey);
	    Boolean isleft = (Boolean) textview.getTag(R.id.PrintPageSetupIsLeftKey);
	    int value = Integer.parseInt(editable.toString());
	    if (isleft) {
		prefs.setPageLeftMargin(form, page_no, value);
	    } else {
		prefs.setPageTopMargin(form, page_no, value);
	    }
	}

	@Override
	public void beforeTextChanged(TextView textview, CharSequence sequence,
		int start, int count, int after) {
	}

	@Override
	public void onTextChanged(TextView textview, CharSequence sequence,
		int start, int before, int count) {
	}
    }
}


class PrinterSetupDialog extends Utils.DialogFragment
{
    private Printer printer;
    private Spinner page_from_spinner;
    private Spinner page_to_spinner;

    public PrinterSetupDialog(Printer printer) {
	this.printer = printer;
    }

    private View buildPagesLayout() {
	LinearLayout.LayoutParams params;

	LinearLayout linear = new LinearLayout(getActivity());
	linear.setOrientation(LinearLayout.HORIZONTAL);
	linear.setGravity(Gravity.CENTER_VERTICAL);
	linear.setPadding(0, 0, 0, 20);

	TextView textview = new TextView(getActivity());
	textview.setText("打印页面：从第");
	textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
	linear.addView(textview);

	int page_count = printer.getForm().getPageCount();
	IntegerAdapter page_adapter = new IntegerAdapter(1, page_count);

	page_from_spinner = new Spinner(getActivity());
	page_from_spinner.setPadding(0, 0, 0, 0);
	page_from_spinner.setAdapter(page_adapter);
	page_from_spinner.setSelection(0);
	params = new LinearLayout.LayoutParams(
		ViewGroup.LayoutParams.WRAP_CONTENT,
		ViewGroup.LayoutParams.WRAP_CONTENT);
	params.leftMargin = 2;
	params.rightMargin = 2;
	linear.addView(page_from_spinner, params);

	textview = new TextView(getActivity());
	textview.setText("页到第");
	textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
	linear.addView(textview);

	page_to_spinner = new Spinner(getActivity());
	page_to_spinner.setPadding(0, 0, 0, 0);
	page_to_spinner.setAdapter(page_adapter);
	page_to_spinner.setSelection(page_adapter.getCount() - 1);
	params = new LinearLayout.LayoutParams(
		ViewGroup.LayoutParams.WRAP_CONTENT,
		ViewGroup.LayoutParams.WRAP_CONTENT);
	params.leftMargin = 2;
	params.rightMargin = 2;
	linear.addView(page_to_spinner, params);

	textview = new TextView(getActivity());
	textview.setText("页");
	textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
	linear.addView(textview);
	
	return linear;
    }

    private View buildLayout() {
	LinearLayout linear = new LinearLayout(getActivity());
	linear.setOrientation(LinearLayout.VERTICAL);
	linear.setPadding(10, 10, 10, 10);
	linear.addView(buildPagesLayout());
	return linear;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	super.onCreateDialog(savedInstanceState);

	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

	builder.setTitle("打印凭条");
	builder.setView(buildLayout());
	builder.setNegativeButton("取 消", null);
	builder.setNeutralButton("页面设置", null);
	builder.setPositiveButton("打 印", null);

	return builder.create();
    }

    @Override
    public void onStart() {
	super.onStart();

	setCancelable(false);

	final AlertDialog dialog = (AlertDialog) getDialog();

	page_from_spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
	    @Override
	    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		Integer from = (Integer) parent.getItemAtPosition(position);
		Integer to = (Integer) page_to_spinner.getSelectedItem();
		if (from.intValue() > to.intValue()) {
		    showToast("起始页不能大于结束页", R.drawable.cry);
		    parent.setSelection(0);
		}
	    }
	    @Override
	    public void onNothingSelected(AdapterView<?> parent) {
	    }
	});

	page_to_spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
	    @Override
	    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		Integer to = (Integer) parent.getItemAtPosition(position);
		Integer from = (Integer) page_from_spinner.getSelectedItem();
		if (from.intValue() > to.intValue()) {
		    showToast("终止页不能小于起始页", R.drawable.cry);
		    parent.setSelection(parent.getAdapter().getCount() - 1);
		}
	    }
	    @Override
	    public void onNothingSelected(AdapterView<?> parent) {
	    }
	});

	Button button = dialog.getButton(Dialog.BUTTON_NEUTRAL);
	button.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View view) {
		PrinterPageSetupDialog dialog = new PrinterPageSetupDialog(printer);
		dialog.show(getFragmentManager(), "PrinterPageSetupDialog");
	    }
	});

	button = dialog.getButton(Dialog.BUTTON_POSITIVE);
	button.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View view) {
		printer.startTask(getFragmentManager(),
			(int) page_from_spinner.getSelectedItemId() - 1,
			(int) page_to_spinner.getSelectedItemId());
		dismiss();
	    }
	});

	if (BuildConfig.DEBUG) {
	    button = dialog.getButton(Dialog.BUTTON_NEGATIVE);
	    button.setText("生成模板");
	    button.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View view) {
		    try {
			File cachedir = getActivity().getCacheDir();
			String fname = printer.getForm().getClass().getName() + ".print.xml";
			FileOutputStream stream = new FileOutputStream(new File(cachedir, fname));
			stream.write(printer.getForm().toPrintTemplate().getBytes("utf-8"));
		    } catch (Exception e) {
			LogActivity.writeLog(e);
		    }
		    dismiss();
		}
	    });
	}
    }
}


class PrinterStopDialog extends Utils.DialogFragment
{
    private Printer printer;
    private boolean need_resume;

    public PrinterStopDialog(Printer printer) {
	this.printer = printer;

	if (!printer.isTaskPaused()) {
	    printer.pauseTask();
	    need_resume = true;
	} else {
	    need_resume = false;
	}
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	super.onCreateDialog(savedInstanceState);

	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	builder.setTitle("停止打印");
	builder.setMessage("\n确定要停止打印吗？\n");
	builder.setNegativeButton("停 止", null);
	builder.setPositiveButton("继 续", null);

	return builder.create();
    }

    @Override
    public void onStart() {
	super.onStart();

	AlertDialog dialog = (AlertDialog) getDialog();

	Button button = dialog.getButton(Dialog.BUTTON_NEGATIVE);
	button.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View view) {
		printer.cancelTask();
		dismiss();
	    }
	});
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
	super.onDismiss(dialog);

	if (need_resume) {
	    printer.resumeTask();
	}
    }
}


class PrinterWaitPaperDialog extends Utils.DialogFragment
{
    private Printer printer;
    private TextView message_view;
    private CharSequence message;

    public PrinterWaitPaperDialog(Printer printer) {
	this.printer = printer;
	printer.pauseTask();
    }

    public void setMessage(CharSequence sequence) {
	this.message = sequence;
	if (message_view != null) {
	    message_view.setText(sequence);
	}
    }

    private View buildLayout() {
	LinearLayout linear = new LinearLayout(getActivity());
	linear.setOrientation(LinearLayout.HORIZONTAL);
	linear.setPadding(20, 20, 20, 20);
	linear.setGravity(Gravity.CENTER_VERTICAL);

	ProgressBar progressbar = new ProgressBar(getActivity(), null,
		android.R.attr.progressBarStyleInverse);
	progressbar.setIndeterminate(true);
	linear.addView(progressbar);

	message_view = new TextView(getActivity());
	if (message != null) {
	    message_view.setText(message);
	}
	message_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
	message_view.setPadding(10, 0, 0, 0);
	message_view.setTextColor(getResources().getColor(R.color.purple));
	linear.addView(message_view);

	return linear;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	super.onCreateDialog(savedInstanceState);

	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

	builder.setView(buildLayout());
	builder.setNegativeButton("停止打印", null);
	builder.setPositiveButton("开始打印", null);

	return builder.create();
    }

    @Override
    public void onStart() {
	super.onStart();

	setCancelable(false);

	AlertDialog dialog = (AlertDialog) getDialog();

	Button button = dialog.getButton(Dialog.BUTTON_NEGATIVE);
	button.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View view) {
		PrinterStopDialog stop_dialog = new PrinterStopDialog(printer);
		stop_dialog.show(getFragmentManager(), "PrinterStopDialog");
	    }
	});

	button = dialog.getButton(Dialog.BUTTON_POSITIVE);
	button.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View view) {
		printer.resumeTask();
		dismiss();
	    }
	});
    }
}


class PrinterProgressDialog extends Utils.DialogFragment
{
    private Printer printer;
    private TextView prompt_text;
    private ProgressBar progressbar;
    private TextView progress_text;
    private int field_count;
    private int field_step;
    private PrinterStopDialog stop_dialog;
    private PrinterWaitPaperDialog wait_paper_dialog;

    public PrinterProgressDialog(Printer printer) {
	this.printer = printer;
	stop_dialog = null;
	wait_paper_dialog = null;
	field_step = 0;
    }

    private View buildLayout() {
	LinearLayout linear = new LinearLayout(getActivity());
	linear.setOrientation(LinearLayout.VERTICAL);
	linear.setPadding(20, 20, 20, 20);

	prompt_text = new TextView(getActivity());
	prompt_text.setText("准备打印");
	prompt_text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
	linear.addView(prompt_text);

	progressbar = new ProgressBar(getActivity(), null,
		android.R.attr.progressBarStyleHorizontal);
	progressbar.setIndeterminate(false);
	Drawable drawable = getResources().getDrawable(R.drawable.progressbar);
	progressbar.setProgressDrawable(drawable);
	progressbar.setVisibility(View.VISIBLE);
	int page_count = printer.getPageTo() - printer.getPageFrom();
	progressbar.setMax(10000 * page_count);
	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
		ViewGroup.LayoutParams.MATCH_PARENT,
		ViewGroup.LayoutParams.WRAP_CONTENT);
	params.topMargin = 30;
	params.bottomMargin = 4;
	linear.addView(progressbar, params);

	progress_text = new TextView(getActivity());
	progress_text.setText("准备打印");
	progress_text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
	linear.addView(progress_text);

	return linear;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	super.onCreateDialog(savedInstanceState);

	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

	int page_count = printer.getPageTo() - printer.getPageFrom();
	builder.setTitle("打印进度，共 " + page_count + " 页");
	builder.setView(buildLayout());
	builder.setNegativeButton("停止打印", null);
	builder.setPositiveButton("暂停打印", null);

	return builder.create();
    }

    @Override
    public void onStart() {
	super.onStart();

	setCancelable(false);

	AlertDialog dialog = (AlertDialog) getDialog();

	Button button = dialog.getButton(Dialog.BUTTON_NEGATIVE);
	button.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View v) {
		stop_dialog = new PrinterStopDialog(printer);
		stop_dialog.show(getFragmentManager(), "PrinterStopDialog");
	    }
	});

	button = dialog.getButton(Dialog.BUTTON_POSITIVE);
	button.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View view) {
		if (printer.isTaskPaused()) {
		    printer.resumeTask();
		    ((Button) view).setText("暂停打印");
		    prompt_text.setText((CharSequence) prompt_text.getTag());
		    prompt_text.setTextColor(getResources().getColor(R.color.darkgray));
		} else {
		    printer.pauseTask();
		    ((Button) view).setText("继续打印");
		    prompt_text.setTag(prompt_text.getText());
		    prompt_text.setText("暂停打印，点击“继续打印”按钮将继续打印...");
		    prompt_text.setTextColor(getResources().getColor(R.color.fuchsia));
		}
	    }
	});
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
	super.onDismiss(dialog);

	if (stop_dialog != null) {
	    stop_dialog.dismiss();
	}
	if (wait_paper_dialog != null) {
	    wait_paper_dialog.dismiss();
	}
    }

    public void pageStart(int page_no, int field_count) {
	this.field_count = field_count;
	field_step = Math.round(progressbar.getMax() / field_count);
	String page_title = printer.getForm().getPage(page_no).getTitle();
	prompt_text.setText(String.format("准备打印第 %d 页 (%s)，请插入纸张 ...",
		page_no + 1, page_title));
	prompt_text.setTextColor(getResources().getColor(R.color.fuchsia));
	progressbar.setProgress(0);
	progress_text.setText("");
    }

    public void pageFinish(int page_no) {
	String page_title = printer.getForm().getPage(page_no).getTitle();
	prompt_text.setText(String.format("第 %d 页(%s)打印完成！",
		page_no + 1, page_title));
	int finish_pages = page_no - printer.getPageFrom() + 1;
	progressbar.setSecondaryProgress(finish_pages * 10000);
    }

    public void paperRequest(int page_no) {
	wait_paper_dialog = new PrinterWaitPaperDialog(printer);
	wait_paper_dialog.setMessage(prompt_text.getText());
	wait_paper_dialog.show(getFragmentManager(), "PrinterWaitPaperDialog");
    }

    public void paperReady(int page_no) {
	String page_title = printer.getForm().getPage(page_no).getTitle();
	prompt_text.setText(String.format("纸张就绪，正在打印第 %d 页（%s）...",
		page_no + 1, page_title));
	prompt_text.setTextColor(getResources().getColor(R.color.darkgray));
    }

    public void fieldStart(int field, String name) {
	progress_text.setText(String.format("正在打印第 %d／%d 项：%s ",
		field + 1, field_count, name));
    }

    public void fieldFinish(int field, String name) {
	if (field + 1 == field_count) {
	    progressbar.setProgress(progressbar.getMax());
	} else {
	    progressbar.setProgress(field_step * (field + 1));
	}
    }

}


public abstract class Printer extends Device
{
    static public final int CAPABILITY_AUTO_CHECK_PAPER = 1;

    protected Form form;
    protected PrinterTask task;
    protected boolean is_task_paused;
    protected int page_from;
    protected int page_to;

    abstract boolean hasCapability(int capability);
    protected boolean waitForPaperReady() { return false; }
    protected Object read() { return null; }
    abstract protected boolean write(PrinterField field);
    abstract protected boolean pageBegin(int page_no);
    abstract protected boolean pageEnd(int page_no);

    public void setForm(Form form) {
	this.form = form;
	is_task_paused = false;
    }

    public Form getForm() {
	return form;
    }

    public int getPageFrom() {
	return page_from;
    }

    public int getPageTo() {
	return page_to;
    }

    @Override
    protected void startTask(FragmentManager manager, int flags) {
	if (form == null) {
	    Utils.showToast("没有需要打印的内容");
	    return;
	}
	PrinterSetupDialog dialog = new PrinterSetupDialog(Printer.this);
	dialog.show(manager, "PrinterSetupDialog");
    }

    protected void startTask(FragmentManager manager, int from, int to) {
	page_from = from;
	page_to = to;
	task = new PrinterTask(this, manager);
	task.execute();
    }

    @Override
    protected void cancelTask() {
	if (task != null && !task.isCancelled()) {
	    task.cancel(true);
	    is_task_paused = false;
	}
    }

    protected void pauseTask() {
	is_task_paused = true;
    }

    protected void resumeTask() {
	is_task_paused = false;
    }

    protected boolean isTaskPaused() {
	return is_task_paused;
    }

    public class PrinterTask extends Device.Task<Void, String, Boolean>
    {
	private static final int PROGRESS_OPEN_FAILED   = 10;
	private static final int PROGRESS_PAGE_START    = 1;
	private static final int PROGRESS_PAGE_FINISH   = 2;
	private static final int PROGRESS_PAPER_REQUEST = 3;
	private static final int PROGRESS_PAPER_READY   = 4;
	private static final int PROGRESS_FIELD_START   = 5;
	private static final int PROGRESS_FIELD_FINISH  = 6;

	private FragmentManager manager;
	private PrinterProgressDialog dialog;

	public PrinterTask(Device device, FragmentManager manager) {
	    super(device);
	    this.manager = manager;
	}

	@Override
	protected void onPreExecute() {
	    super.onPreExecute();
	    dialog = new PrinterProgressDialog(Printer.this);
	    dialog.show(manager, "PrinterProgressDialog");
	}

	private SparseArray<ArrayList<PrinterField>> parsePrintConfig() {
	    String path = String.format("print/%s.print.xml", form.getClass().getName());
	    PrinterXMLHandler handler = new PrinterXMLHandler(form, page_from, page_to);
	    try {
		InputStream stream;
		File file = new File(EFormApplication.getContext().getFilesDir(), path);
		if (file.exists()) {
		    stream = new FileInputStream(file);
		} else {
		    AssetManager assets = EFormApplication.getContext().getAssets();
		    stream = assets.open(path);
		}
		Xml.parse(stream, Xml.Encoding.UTF_8, handler);
		return handler.getAllPages();
	    } catch (FileNotFoundException e) {
		LogActivity.writeLog(e);
		LogActivity.writeLog("未找到打印配置文件“%s”，请联系技术支持", path);
		return null;
	    } catch (IOException e) {
		LogActivity.writeLog(e);
		LogActivity.writeLog("不能读写打印配置文件”%s“", path);
		return null;
	    } catch (SAXException e) {
		LogActivity.writeLog(e);
		LogActivity.writeLog("打印配置文件”%s“格式错误，错误位置为第%d行",
			path, handler.getErrorLineNumber());
		return null;
	    }
	}

	private boolean checkPause() {
	    while (is_task_paused) {
		try {
		    Thread.sleep(50);
		} catch (InterruptedException e) {
		    LogActivity.writeLog("打印暂停（睡眠）被中断唤醒");
		    if (isCancelled()) {
			return false;
		    }
		}
	    }
	    return true;
	}

	@Override
	protected Boolean doInBackground(Void... params) {
	    if (!open()) {
		publishProgress(String.valueOf(PROGRESS_OPEN_FAILED));
		return false;
	    }

	    SparseArray<ArrayList<PrinterField>> print_pages = parsePrintConfig();
	    if (print_pages == null)
		return false;

	    for (int page_no = page_from; page_no < page_to; page_no++) {
		ArrayList<PrinterField> fields = print_pages.get(page_no);
		if (fields == null || fields.size() == 0) {
		    LogActivity.writeLog("打印页面“%d”没有配置打印数据", page_no);
		    continue;
		}

		publishProgress(String.valueOf(PROGRESS_PAGE_START), String.valueOf(page_no),
			String.valueOf(fields.size()));

		if (hasCapability(CAPABILITY_AUTO_CHECK_PAPER)) {
		    if (!waitForPaperReady()) {
			LogActivity.writeLog("打印等待插入纸张错误");
			return false;
		    }
		    publishProgress(String.valueOf(PROGRESS_PAPER_READY), String.valueOf(page_no));
		} else {
		    pauseTask();
		    publishProgress(String.valueOf(PROGRESS_PAPER_REQUEST), String.valueOf(page_no));
		    publishProgress(String.valueOf(PROGRESS_PAPER_READY), String.valueOf(page_no));
		}

		if (!checkPause()) {
		    return false;
		}

		form.readyPageForPrint(page_no);
		Form.FormPage page = form.getPage(page_no);

		pageBegin(page_no);

		for (int field_no = 0; field_no < fields.size(); field_no++) {
		    if (isCancelled()) {
			return false;
		    }
		    PrinterField field = fields.get(field_no);

		    publishProgress(String.valueOf(PROGRESS_FIELD_START),
			    String.valueOf(field_no), field.name);

		    String value = page.getPrintString(field.resid, field.want);
		    if (value != null) {
			field.value = value.trim();
			if (field.value.length() > 0) {
			    if (!write(field)) {
				LogActivity.writeLog("写入打印机错误");
				return false;
			    }
			}
		    } else {
			LogActivity.writeLog("取“%s(%s)”的值失败，跳过", field.name, field.resid);
		    }

		    publishProgress(String.valueOf(PROGRESS_FIELD_FINISH),
			    String.valueOf(field_no), field.name);

		    if (!checkPause()) {
			return false;
		    }
		}
		pageEnd(page_no);

		publishProgress(String.valueOf(PROGRESS_PAGE_FINISH), String.valueOf(page_no));
	    }
	    return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
	    super.onPostExecute(result);
	    dialog.dismiss();
	    device.close();
	}

	@Override
	protected void onCancelled(Boolean result) {
	    super.onCancelled(result);
	    dialog.dismiss();
	    device.close();
	    Utils.showToast("操作被取消 ...");
	}

	@Override
	protected void onProgressUpdate(String... args) {
	    switch (Integer.parseInt(args[0])) {
	    case PROGRESS_OPEN_FAILED:
		Utils.showToast("打开打印机失败", R.drawable.cry);
		break;
	    case PROGRESS_PAGE_START:
		dialog.pageStart(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
		break;
	    case PROGRESS_PAGE_FINISH:
		dialog.pageFinish(Integer.parseInt(args[1]));
		break;
	    case PROGRESS_PAPER_REQUEST:
		dialog.paperRequest(Integer.parseInt(args[1]));
		break;
	    case PROGRESS_PAPER_READY:
		dialog.paperReady(Integer.parseInt(args[1]));
		break;
	    case PROGRESS_FIELD_START:
		dialog.fieldStart(Integer.parseInt(args[1]), args[2]);
		break;
	    case PROGRESS_FIELD_FINISH:
		dialog.fieldFinish(Integer.parseInt(args[1]), args[2]);
		break;
	    }
	}
    }

    public class PrinterField
    {
	static public final int UNIT_POINT = 1;
	static public final int UNIT_MM    = 2;

	static public final int WIDTH_NORMAL        = 0;
	static public final int WIDTH_HALF_WIDTH    = 1;
	static public final int WIDTH_DOUBLE_WIDTH  = 2;
	static public final int WIDTH_DOUBLE_HEIGHT = 3;
	static public final int WIDTH_FOURFOLD      = 4;
	static public final int WIDTH_SUPERSCRIPT   = 5;
	static public final int WIDTH_SUBSCRIPT     = 6;

	static public final int STYLE_NORMAL     = 0;
	static public final int STYLE_ITALIC     = 1;
	static public final int STYLE_BOLD       = 2;
	static public final int STYLE_BOLDITALIC = 3;
	static public final int STYLE_UNDERLINE  = 4;
	static public final int STYLE_MIDLINE    = 5;
	static public final int STYLE_UPPERLINE  = 6;
	static public final int STYLE_BACKGROUND = 7;

	public String value;
	public String name;
	public String resid;
	public String want;
	public int unit;
	public float x;
	public float y;
	public float spacing;
	public int width;
	public int style;

	boolean isValid() {
	    if (name == null) {
		if (BuildConfig.DEBUG) {
		    LogActivity.writeLog("打印字段的“name“不能为空");
		    return false;
		}
	    }
	    if (resid == null) {
		LogActivity.writeLog("打印字段的“resid“不能为空");
		return false;
	    }
	    if (want == null) {
		LogActivity.writeLog("打印字段的“want“不能为空");
		return false;
	    }
	    if (unit != UNIT_MM && unit != UNIT_POINT) {
		LogActivity.writeLog("打印字段的单位无效，将使用默认单位毫米");
		unit = UNIT_MM;
	    }
	    if (x < 0.0 || y < 0.0) {
		LogActivity.writeLog("打印字段的“x“或“y“坐标不能小于0");
		return false;
	    }
	    return true;
	}
    }

    class PrinterXMLHandler extends DefaultHandler
    {
	private Form form;
	private int page_from;
	private int page_to;
	private SparseArray<ArrayList<PrinterField>> print_pages;
	private ArrayList<PrinterField> curr_page;
	private int level;
	private Locator locator;
	private int error_line;

	public PrinterXMLHandler(Form form, int page_from, int page_to) {
	    this.form = form;
	    this.page_from = page_from;
	    this.page_to = page_to;
	    print_pages = new SparseArray<ArrayList<PrinterField>>();
	    curr_page = null;
	    level = 0;
	    error_line = 0;
	}

	public SparseArray<ArrayList<PrinterField>> getAllPages() {
	    return print_pages;
	}

	@Override
	public void setDocumentLocator(Locator locator) {
	    this.locator = locator;
	}

	public int getErrorLineNumber() {
	    return error_line;
	}

	@Override
	public void startElement(String uri, String localName,
		String qName, Attributes attrs) throws SAXException {
	    if (level == 0 && !localName.equalsIgnoreCase("form")) {
		error_line = locator.getLineNumber();
		throw new SAXException("解析打印配置文件失败，根结点名称必须是'form'");
	    }
	    level++;

	    if (localName.equalsIgnoreCase("form")) {
		if (level != 1) {
		    error_line = locator.getLineNumber();
		    throw new SAXException("元素'form'必须是根结点");
		}
		String klass = attrs.getValue("", "class");
		if (klass == null) {
		    error_line = locator.getLineNumber();
		    throw new SAXException("元素'form'缺少'klass'属性");
		}
		if (!klass.equalsIgnoreCase(form.getClass().getName())) {
		    error_line = locator.getLineNumber();
		    throw new SAXException("打印配置文件和当前凭条类型不匹配");
		}
		return;
	    }

	    if (localName.equalsIgnoreCase("page")) {
		if (level != 2) {
		    error_line = locator.getLineNumber();
		    throw new SAXException("元素'page'必须是二级结点");
		}
		int page_no = Integer.parseInt(attrs.getValue("", "index"));
		if (page_no < 0) {
		    error_line = locator.getLineNumber();
		    throw new SAXException("解析打印配置文件失败，页面索引'" + page_no + "'无效");
		}
		if (print_pages.indexOfKey(page_no) >= 0) {
		    error_line = locator.getLineNumber();
		    throw new SAXException("解析打印配置文件失败，重复的页索引'" + page_no + "'");
		}
		if (page_no >= page_from && page_no < page_to) {
		    curr_page = new ArrayList<PrinterField>();
		    print_pages.put(page_no, curr_page);
		}
		return;
	    }

	    if (localName.equalsIgnoreCase("field")) {
		if (level != 3) {
		    error_line = locator.getLineNumber();
		    throw new SAXException("解析打印配置文件失败，元素'field'必须是三级结点");
		}

		if (curr_page != null) {
		    try {
			PrinterField field = parseField(attrs);
			if (!field.isValid()) {
			    error_line = locator.getLineNumber();
			    throw new SAXException("解析打印配置文件失败，'field'缺少某属性或某属性值错误");
			}
			curr_page.add(field);
		    } catch (NumberFormatException e) {
			error_line = locator.getLineNumber();
			throw new SAXException(e);
		    }
		}
		return;
	    }

	    LogActivity.writeLog("打印配置文件中有不可识别的元素”%s“（第%d行），忽略它",
		    localName, locator.getLineNumber());
	}

	public void endElement(String uri, String localName, String qName) {
	    level--;
	    if (localName.equalsIgnoreCase("page")) {
		curr_page = null;
	    }
	}

	private PrinterField parseField(Attributes attrs) throws NumberFormatException {
	    PrinterField field = new PrinterField();

	    for (int i = 0; i < attrs.getLength(); i++) {
		String name = attrs.getLocalName(i);
		String value = attrs.getValue(i);

		if (name.equalsIgnoreCase("resid")) {
		    field.resid = value;
		} else if (name.equalsIgnoreCase("name")) {
		    field.name = value;
		} else if (name.equalsIgnoreCase("want")) {
		    field.want = value;
		} else if (name.equalsIgnoreCase("unit")) {
		    if (value.equalsIgnoreCase("point") || value.equalsIgnoreCase("dpi")) {
			field.unit = PrinterField.UNIT_POINT;
		    } else if (value.equalsIgnoreCase("mm")) {
			field.unit = PrinterField.UNIT_MM;
		    } else {
			LogActivity.writeLog("打印单位“%s”无效，必须是“dip”或“mm”", value);
			field.unit = PrinterField.UNIT_MM;
		    }
		} else if (name.equalsIgnoreCase("x")) {
		    field.x = Float.parseFloat(value);
		} else if (name.equalsIgnoreCase("y")) {
		    field.y = Float.parseFloat(value);
		} else if (name.equalsIgnoreCase("spacing")) {
		    field.spacing = Float.parseFloat(value);
		} else if (name.equalsIgnoreCase("width")) {
		    if (value.equalsIgnoreCase("normal")) {
			field.width = PrinterField.WIDTH_NORMAL;
		    } else if (value.equalsIgnoreCase("half")) {
			field.width = PrinterField.WIDTH_HALF_WIDTH;
		    } else if (value.equalsIgnoreCase("double")) {
			field.width = PrinterField.WIDTH_DOUBLE_WIDTH;
		    } else if (value.equalsIgnoreCase("doubleheight")) {
			field.width = PrinterField.WIDTH_DOUBLE_HEIGHT;
		    } else if (value.equalsIgnoreCase("fourfold")) {
			field.width = PrinterField.WIDTH_FOURFOLD;
		    } else if (value.equalsIgnoreCase("superscript")) {
			field.width = PrinterField.WIDTH_SUPERSCRIPT;
		    } else if (value.equalsIgnoreCase("subscript")) {
			field.width = PrinterField.WIDTH_SUBSCRIPT;
		    } else {
			LogActivity.writeLog("打印字段宽度“%s”无效，请参考手册", value);
			field.width = PrinterField.WIDTH_NORMAL;
		    }
		} else if (name.equalsIgnoreCase("style")) {
		    if (value.equalsIgnoreCase("normal")) {
			field.style = PrinterField.STYLE_NORMAL;
		    } else if (value.equalsIgnoreCase("bold")) {
			field.style = PrinterField.STYLE_BOLD;
		    } else if (value.equalsIgnoreCase("italic")) {
			field.style = PrinterField.STYLE_ITALIC;
		    } else if (value.equalsIgnoreCase("bolditalic")) {
			field.style = PrinterField.STYLE_BOLDITALIC;
		    } else if (value.equalsIgnoreCase("underline")) {
			field.style = PrinterField.STYLE_UNDERLINE;
		    } else if (value.equalsIgnoreCase("midline")) {
			field.style = PrinterField.STYLE_MIDLINE;
		    } else if (value.equalsIgnoreCase("upperline")) {
			field.style = PrinterField.STYLE_UPPERLINE;
		    } else if (value.equalsIgnoreCase("background")) {
			field.style = PrinterField.STYLE_BACKGROUND;
		    } else {
			LogActivity.writeLog("打印字段风格“%s”无效，请参考手册", value);
			field.style = PrinterField.STYLE_NORMAL;
		    }
		} else {
		    LogActivity.writeLog("打印配置“field”元素有不可识别的属性“%s“（第%d行），忽略它",
			    name, locator.getLineNumber());
		}
	    }
	    return field;
	}

    }
}


class PrinterVirtual extends Printer
{
    private FileOutputStream stream;

    @Override
    public boolean probeDevice() {
	return true;
    }

    @Override
    protected boolean hasCapability(int capability) {
	return true;
    }

    @Override
    protected boolean open() {
	try {
	    Context context = EFormApplication.getContext();
	    File cachedir = context.getCacheDir();
	    String filename = form.getClass().getName() + ".print";
	    stream = new FileOutputStream(new File(cachedir, filename));
	    return (stream == null) ? false : true;
	} catch (FileNotFoundException e) {
	    LogActivity.writeLog(e);
	    return false;
	}
    }

    @Override
    protected void close() {
	try {
	    stream.close();
	} catch (IOException e) {
	    LogActivity.writeLog(e);
	}
    }

    @Override
    protected boolean waitForPaperReady() {
	if (stream == null) {
	    return false;
	}
	try {
	    Thread.sleep(1000);
	    return true;
	} catch (InterruptedException e) {
	    return false;
	}
    }

    @Override
    protected boolean pageBegin(int page_no) {
	return (write(">>> 开始打印第 " + page_no + " 页\n\n") < 0) ? false : true;
    }

    @Override
    protected boolean pageEnd(int page_no) {
	return (write(">>> 结束打印第 " + page_no + " 页\n\n") < 0) ? false : true;
    }

    @Override
    protected int write(String string) {
	try {
	    byte[] bytes = string.getBytes("UTF-8");
	    stream.write(bytes);
	    return bytes.length;
	} catch (Exception e) {
	    LogActivity.writeLog(e);
	    return -1;
	}
    }

    @Override
    protected boolean write(PrinterField field) {
	if (stream == null) {
	    LogActivity.writeLog("打印机未打开或已关闭");
	    return false;
	}
	String contents = String.format("字段名称(%s)，值(%s)，ID(%s)，" +
		"类型(%s)，单位(%d)，X(%.2f)，Y(%.2f)，间隙(%.2f)，宽度(%d)，风格(%d)\n",
		field.name, field.value, field.resid, field.want, field.unit,
		field.x, field.y, field.spacing, field.width, field.style);
	return (write(contents) < 0) ? false : true;
    }

}


class PrinterLQ90KP extends Printer
{
    public static final int VID = 0x04B8;
    public static final int PID = 0x0005;

    private UsbDeviceConnection connection;
    private UsbInterface iface;
    private UsbEndpoint endpoint;
    private UsbRequest request;

    @Override
    public int getDeviceType() {
	return DEVICE_TYPE_USB;
    }

    @Override
    public boolean probeDevice() {
	return (getUsbDevice(VID, PID) == null) ? false : true;
    }

    @Override
    protected boolean hasCapability(int capability) {
	switch(capability) {
	case CAPABILITY_AUTO_CHECK_PAPER:
	    return true;
	default:
	    return false;
	}
    }

    @Override
    protected boolean open() {
	UsbDevice device = this.getUsbDevice(VID, PID);
	if (device == null)
	    return false;

	Context context = EFormApplication.getContext();
	UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
	connection = manager.openDevice(device);
	if (connection == null) {
	    LogActivity.writeLog("打印失败，打开USB打印机失败");
	    return false;
	}

	iface = device.getInterface(0);
	if (!connection.claimInterface(iface, true)) {
	    LogActivity.writeLog("打印失败，申请USB接口独占访问失败");
	    return false;
	}

	endpoint = iface.getEndpoint(0);
	if (endpoint == null ||
		endpoint.getDirection() != UsbConstants.USB_DIR_OUT) {
	    LogActivity.writeLog("打印失败，设备端点选择错误");
	    return false;
	}

	if (hasCapability(CAPABILITY_AUTO_CHECK_PAPER) &&
		EFormApplication.printer_first_print) {
	    EFormApplication.printer_first_print = false;
	    return waitForPaperExit();
	} else {
	    return true;
	}
    }

    @Override
    protected void close() {
	if (connection != null) {
	    connection.releaseInterface(iface);
	    connection.close();
	}
    }

    @Override
    protected void cancelTask() {
	super.cancelTask();

	if (request != null) {
	    if (!request.cancel()) {
		close();
		connection = null;
	    }
	}
     }

    private int write(byte[] bytes, int length) {
	if (connection == null) {
	    return -1;
	}

	if (task.isCancelled()) {
	    return -1;
	}

	request = new UsbRequest();
	if (!request.initialize(connection, endpoint)) {
	    LogActivity.writeLog("打印失败，初始化USB请求错误");
	    return -1;
	}
	if (!request.queue(ByteBuffer.wrap(bytes), length)) {
	    request.close();
	    LogActivity.writeLog("打印失败，打印请求排队错误");
	    return -1;
	}

    	int result = (connection.requestWait() == request) ? length : -1;
    	request.close();
    	return result;
    }

    @Override
    protected int write(String string) {
	try {
	    byte[] bytes = string.getBytes("GB2312");
	    return write(bytes, bytes.length);
	} catch (UnsupportedEncodingException e) {
	    LogActivity.writeLog(e);
	    return -1;
	}
    }

    @Override
    protected boolean write(PrinterField field) {
	if (field.value == null || field.value.length() == 0) {
	    LogActivity.writeLog("没有要打印的数据");
	    return true;
	}

	switch (field.unit) {
	case PrinterField.UNIT_POINT:
	    if (!setAbsVerticalPoint(field.y) || !setAbsHorizontalPoint(field.x)) {
		return false;
	    }
	    break;
	case PrinterField.UNIT_MM:
	default:
	    if (!setAbsVerticalMM(field.y) || !setAbsHorizontalMM(field.x)) {
		return false;
	    }
	    break;
	}

	if (!setFieldWidth(field.width) || !setFieldStyle(field.style)) {
	    return false;
	}
	if (write(field.value) < 0 || !carriageReturn()) {
	    return false;
	}

	if (!cancelBoldFont() || !cancelItalicFont() || !unsetUnderline()) {
	    return false;
	}
	return true;
    }

    private boolean setFieldWidth(int width) {
	switch (width) {
	case PrinterField.WIDTH_HALF_WIDTH:
	    break;
	case PrinterField.WIDTH_DOUBLE_WIDTH:
	    if (!setSingleLineDoubleWidth()) {
		return false;
	    }
	    break;
	case PrinterField.WIDTH_DOUBLE_HEIGHT:
	    break;
	case PrinterField.WIDTH_FOURFOLD:
	    break;
	case PrinterField.WIDTH_SUPERSCRIPT:
	    break;
	case PrinterField.WIDTH_SUBSCRIPT:
	    break;
	default:
	    break;
	}
	return true;
    }

    private boolean setFieldStyle(int style) {
	switch (style) {
	case PrinterField.STYLE_ITALIC:
	    if (!selectItalicFont()) {
		return false;
	    }
	    break;
	case PrinterField.STYLE_BOLD:
	    if (!selectBoldFont()) {
		return false;
	    }
	    break;
	case PrinterField.STYLE_BOLDITALIC:
	    if (!selectBoldFont() || !selectItalicFont()) {
		return false;
	    }
	    break;
	case PrinterField.STYLE_UNDERLINE:
	    if (!setUnderline()) {
		return false;
	    }
	    break;
	case PrinterField.STYLE_MIDLINE:
	    break;
	case PrinterField.STYLE_UPPERLINE:
	    break;
	case PrinterField.STYLE_BACKGROUND:
	    break;
	default:
	    break;
	}
	return true;
    }

    @Override
    protected boolean waitForPaperReady() {
	if (connection == null) {
	    return false;
	}
	byte[] bytes = new byte[1];
	bytes[0] = 0x0D;
	return (write(bytes, 1) < 0) ? false : true;
    }

    private boolean waitForPaperExit() {
	byte[] bytes = new byte[1];
	bytes[0] = 0x0D;

	if (connection.bulkTransfer(endpoint, bytes, 1, 100) < 0) {
	    return true;
	}

	if (!formFeed()) {
	    return false;
	}

	while (true) {
	    try {
		Thread.sleep(500);
		if (connection.bulkTransfer(endpoint, bytes, 1, 100) < 0) {
		    return true;
		}
	    } catch (InterruptedException e) {
		return false;
	    }
	}
    }

    @Override
    protected boolean pageBegin(int page_no) {
	if (!cancelCondensedPrinting()) {
	    return false;
	}
	if (!initializePrinter()) {
	    return false;
	}
	if (!setUnit()) {
	    return false;
	}
	return true;
    }

    @Override
    protected boolean pageEnd(int page_no) {
	if (hasCapability(CAPABILITY_AUTO_CHECK_PAPER)) {
	    return waitForPaperExit();
	} else {
	    return formFeed();
	}
    }

    private boolean carriageReturn() {
	byte[] bytes = new byte[1];
	bytes[0] = 0x0D;
	return (write(bytes, 1) < 0) ? false : true;
    }

    private boolean formFeed() {
	byte[] bytes = new byte[1];
	bytes[0] = 0x0C;
	return (write(bytes, 1) < 0) ? false : true;
    }

    private boolean cancelCondensedPrinting() {
	byte[] bytes = new byte[1];
	bytes[0] = 0x18;
	return (write(bytes, 1) < 0) ? false : true;
    }

    private boolean initializePrinter() {
	byte[] bytes = new byte[2];
	bytes[0] = 0x1B;
	bytes[1] = 0x40;
	return (write(bytes, 2) < 0) ? false : true;
    }

    private boolean setUnit() {
	byte[] bytes = new byte[6];
	bytes[0] = 0x1B;
	bytes[1] = 0x28;
	bytes[2] = 0x55;
	bytes[3] = 0x01;
	bytes[4] = 0x00;
	bytes[5] = 0x0A;
	return (write(bytes, 6) < 0) ? false : true;
    }

    private boolean setAbsHorizontalPoint(float point) {
	byte[] bytes = new byte[4];
	bytes[0] = 0x1B;
	bytes[1] = 0x24;
	bytes[2] = (byte) (point % 256);
	bytes[3] = (byte) (point / 256);
	return (write(bytes, 4) < 0) ? false : true;
    }

    private boolean setAbsHorizontalMM(float mm) {
	float point = (float) (mm * 360 / 25.4);
	return setAbsHorizontalPoint(point);
    }

    private boolean setAbsVerticalPoint(float point) {
	byte[] bytes = new byte[7];
	bytes[0] = 0x1B;
	bytes[1] = 0x28;
	bytes[2] = 0x56;
	bytes[3] = 0x02;
	bytes[4] = 0x00;
	bytes[5] = (byte) (point % 256);
	bytes[6] = (byte) (point / 256);
	return (write(bytes, 7) < 0) ? false : true;
    }

    private boolean setAbsVerticalMM(float mm) {
	float point = (float) (mm * 360 / 25.4);
	return setAbsVerticalPoint(point);
    }

    private boolean selectItalicFont() {
	byte[] bytes = new byte[2];
	bytes[0] = 0x1B;
	bytes[1] = 0x34;
	return (write(bytes, 2) < 0) ? false : true;
    }

    private boolean cancelItalicFont() {
	byte[] bytes = new byte[2];
	bytes[0] = 0x1B;
	bytes[1] = 0x35;
	return (write(bytes, 2) < 0) ? false : true;
    }

    private boolean selectBoldFont() {
	byte[] bytes = new byte[2];
	bytes[0] = 0x1B;
	bytes[1] = 0x45;
	return (write(bytes, 2) < 0) ? false : true;
    }

    private boolean cancelBoldFont() {
	byte[] bytes = new byte[2];
	bytes[0] = 0x1B;
	bytes[1] = 0x46;
	return (write(bytes, 2) < 0) ? false : true;
    }

    private boolean setSingleLineDoubleWidth() {
	byte[] bytes = new byte[2];
	bytes[0] = 0x1B;
	bytes[1] = 0x0E;
	return (write(bytes, 2) < 0) ? false : true;
    }

    private boolean setUnderline() {
	byte[] bytes = new byte[8];
	bytes[0] = 0x1B;
	bytes[1] = 0x28;
	bytes[2] = 0x2D;
	bytes[3] = 0x03;
	bytes[4] = 0x00;
	bytes[5] = 0x01;
	bytes[6] = 0x02;
	bytes[7] = 0x02;
	return (write(bytes, 8) < 0) ? false : true;
    }

    private boolean unsetUnderline() {
	byte[] bytes = new byte[8];
	bytes[0] = 0x1B;
	bytes[1] = 0x28;
	bytes[2] = 0x2D;
	bytes[3] = 0x03;
	bytes[4] = 0x00;
	bytes[5] = 0x01;
	bytes[6] = 0x02;
	bytes[7] = 0x00;
	return (write(bytes, 8) < 0) ? false : true;
    }

}
