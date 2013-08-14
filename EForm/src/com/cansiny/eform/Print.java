/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.util.TypedValue;
import android.util.Xml;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;


class PrintDialog extends Utils.DialogFragment
{
    private Print print;
    private Spinner page_from_spinner;
    private Spinner page_to_spinner;
    private boolean in_progress;

    public PrintDialog(Print print) {
	this.print = print;
	in_progress = false;
    }

    private View buildPagesLayout() {
	LinearLayout.LayoutParams params;

	LinearLayout linear = new LinearLayout(getActivity());
	linear.setOrientation(LinearLayout.HORIZONTAL);
	linear.setGravity(Gravity.CENTER_VERTICAL);

	TextView textview = new TextView(getActivity());
	textview.setText("打印页面：从第");
	textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
	linear.addView(textview);

	page_from_spinner = new Spinner(getActivity());
	page_from_spinner.setPadding(0, 0, 0, 0);
	Utils.IntegerAdapter adapter = new Utils.IntegerAdapter(1, print.getForm().getPageCount());
	page_from_spinner.setAdapter(adapter);
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
	adapter = new Utils.IntegerAdapter(1, print.getForm().getPageCount());
	page_to_spinner.setAdapter(adapter);
	page_to_spinner.setSelection(adapter.getCount() - 1);
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

	print.printStart();

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
		PrintPageSetupDialog dialog = new PrintPageSetupDialog(print);
		dialog.show(getFragmentManager(), "PrintPageSetupDialog");
	    }
	});

	button = dialog.getButton(Dialog.BUTTON_POSITIVE);
	button.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View view) {
		in_progress = true;
		int page_from = (int) page_from_spinner.getSelectedItemId();
		int page_to = (int) page_to_spinner.getSelectedItemId();
		PrintProgressDialog dialog = new PrintProgressDialog(print, page_from, page_to);
		dialog.show(getFragmentManager(), "PrintProgressDialog");
		dismiss();
	    }
	});
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
	super.onDismiss(dialog);

	if (!in_progress) {
	    print.printStop();
	}
    }
}


class PrintPageSetupDialog extends Utils.DialogFragment
{
    private Print print;

    public PrintPageSetupDialog(Print print) {
	this.print = print;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	super.onCreateDialog(savedInstanceState);

	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	builder.setTitle("页面设置");

	LinearLayout linear = new LinearLayout(getActivity());
	linear.setOrientation(LinearLayout.VERTICAL);

	ListView listview = new ListView(getActivity());
	PageSetupAdapter adapter = new PageSetupAdapter(print.getForm());
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

	Preferences prefs = Preferences.getPreferences();
	prefs.beginTransaction();

	return builder.create();
    }

    @Override
    public void onStart() {
	super.onStart();
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
	    if (position < form.getPageCount())
		return form.getPage(position);

	    return null;
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


class PrintProgressDialog extends Utils.DialogFragment
{
    private Print print;
    private int page_from;
    private int page_to;
    private TextView contents;
    private TextView status;
    private PrintTask print_task;

    public PrintProgressDialog(Print print, int page_from, int page_to) {
	this.print = print;
	this.page_from = (page_from < 0) ? 0 : page_from;
	this.page_to = (page_to > print.getForm().getPageCount())
		? print.getForm().getPageCount() : page_to;
    }

    private View buildLayout() {
	LinearLayout linear = new LinearLayout(getActivity());
	linear.setOrientation(LinearLayout.VERTICAL);

	contents = new TextView(getActivity());
	contents.setText("正在打印");
	contents.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
	contents.setGravity(Gravity.CENTER_HORIZONTAL);
	contents.setPadding(0, 20, 0, 20);
	linear.addView(contents, new LinearLayout.LayoutParams(
		ViewGroup.LayoutParams.MATCH_PARENT,
		ViewGroup.LayoutParams.WRAP_CONTENT));

	status = new TextView(getActivity());
	status.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
	status.setTextColor(getResources().getColor(R.color.red));
	status.setGravity(Gravity.CENTER_HORIZONTAL);
	status.setPadding(0, 0, 0, 10);
	linear.addView(status, new LinearLayout.LayoutParams(
		ViewGroup.LayoutParams.MATCH_PARENT,
		ViewGroup.LayoutParams.WRAP_CONTENT));

	return linear;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	super.onCreateDialog(savedInstanceState);

	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

	builder.setTitle("打印进度，共 " + (page_to - page_from + 1) + " 页");
	builder.setView(buildLayout());
	builder.setNegativeButton("停 止", null);
	builder.setPositiveButton("暂 停", null);

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
		if (print_task != null) {
		    print_task.pause();
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("停止打印");
		builder.setMessage("\n确认要停止打印吗？\n");
		builder.setNegativeButton("继续打印", null);
		builder.setPositiveButton("停止打印", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
			if (print_task != null) {
			    print_task.cancel(true);
			}
		    }
		});
		AlertDialog confirm = builder.create();

		confirm.setCancelable(true);

		confirm.setOnShowListener(new DialogInterface.OnShowListener() {
		    @Override
		    public void onShow(DialogInterface dialog) {
			Button button = ((AlertDialog) dialog).getButton(Dialog.BUTTON_NEGATIVE);
			button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
			button = ((AlertDialog) dialog).getButton(Dialog.BUTTON_POSITIVE);
			button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		    }
		});

		confirm.setOnDismissListener(new DialogInterface.OnDismissListener() {
		    @Override
		    public void onDismiss(DialogInterface dialog) {
			if (print_task != null && !print_task.isCancelled()) {
			    print_task.resume();
			}
		    }
		});
		confirm.show();
	    }
	});

	button = dialog.getButton(Dialog.BUTTON_POSITIVE);
	button.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View view) {
		if (print_task != null) {
		    if (print_task.isPaused()) {
			print_task.resume();
			((Button) view).setText("暂 停");
		    } else {
			print_task.pause();
			((Button) view).setText("继 续");
		    }
		}
	    }
	});

	print_task = new PrintTask();
	print_task.execute(page_from, page_to);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
	super.onDismiss(dialog);

	print.printStop();
    }

    public void setContents(String string) {
	contents.setText(string);
    }

    public void setStatus(String string, boolean important) {
	status.setText(string);
	if (important) {
	    status.setTextColor(getResources().getColor(R.color.red));
	} else {
	    status.setTextColor(getResources().getColor(R.color.darkgray));
	}
    }

    private class PrintTask extends AsyncTask<Integer, Integer, Long>
    {
	private Printer printer;
	private boolean paused = false;

	@Override
	protected Long doInBackground(Integer... args) {
	    for (int i = args[0]; i <= args[1]; i++) {
		this.publishProgress(i - 1);

		try {
		    Thread.sleep(3000);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		    return 0L;
		}

		if (isCancelled()) {
		    return 0L;
		}

		while(paused == true) {
		    try {
			if (isCancelled()) {
			    return 0L;
			}
			Thread.sleep(500);
		    } catch (InterruptedException e) {
			e.printStackTrace();
			return 0L;
		    }
		}
	    }
	    return null;
	}

	protected void onProgressUpdate(Integer... args) {
	    int page_no = args[0];
	    String title = print.getForm().getPage(page_no).getTitle();
	    setContents("正在打印第 " + (page_no + 1) + " 页：" + title + " ...");
	}

	protected void onPreExecute () {
	    paused = false;

	    printer = print.getPrinter();
	    if (printer == null) {
		LogActivity.writeLog("不能得到打印机，请检查配置");
		cancel(true);
		return;
	    }
	    if (!printer.open()) {
		LogActivity.writeLog("打开打印机失败");
		cancel(true);
		return;
	    }

	    try {
		InputStream is = getActivity().openFileInput("");
		Xml.parse(is, Xml.Encoding.UTF_8, new PrintParserHandler());
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    } catch (SAXException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}

	protected void onPostExecute(Long result) {
	    if (printer != null) {
		printer.close();
	    }
	    Utils.showToast("打印完成！");
	    dismiss();
	}

	protected void onCancelled (Long result) {
	    if (printer != null) {
		printer.close();
	    }
	    Utils.showToast("打印任务被取消！");
	    dismiss();
	}

	public void pause() {
	    paused = true;
	    showToast("暂停打印，点击“继续”按钮继续打印");
	    setStatus("暂停打印，点击“继续”按钮继续打印...", true);
	}

	public void resume() {
	    paused = false;
	    showToast("继续打印，点击“暂停”按钮暂停打印");
	}

	public boolean isPaused() {
	    return paused;
	}
    }

    private class PrintParserHandler extends DefaultHandler
    {
	
    }
}

public class Print
{
    private Form form;
    private PrintListener listener;
    private Printer printer;

    public Print(Form form) {
	this.form = form;

    }

    public Form getForm() {
	return form;
    }

    public void setPrintListener(PrintListener listener) {
	this.listener = listener;
    }

    public Printer getPrinter() {
	if (printer == null)
	    printer = Printer.getPrinter();
	return printer;
    }

    public boolean print(FragmentManager manager) {
	if (form == null) {
	    LogActivity.writeLog("没有需要打印的凭条");
	    return false;
	}
	printer = Printer.getPrinter();

	new PrintDialog(this).show(manager, "PrintDialog");

	if (listener != null) {
	    listener.onPrintStart(this);
	}
	return true;
    }

    public void printStart() {
	if (listener != null) {
	    listener.onPrintStart(this);
	}
    }

    public void printStop() {
	if (listener != null) {
	    listener.onPrintStop(this);
	}
    }

    public interface PrintListener
    {
	public void onPrintStart(Print print);
	public void onPrintStop(Print print);
    }
}
