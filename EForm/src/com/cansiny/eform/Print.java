/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.util.TypedValue;
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

    public PrintDialog(Print print) {
	this.print = print;
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

//	View view = dialog.findViewById(R.id.coord_adjust_textview);
//	view.setOnClickListener(new OnClickListener() {
//	    @Override
//	    public void onClick(View view) {
//		View edittext = dialog.findViewById(R.id.coord_adjust_x_edittext);
//		edittext.setEnabled(!edittext.isEnabled());
//		edittext.requestFocus();
//		edittext = dialog.findViewById(R.id.coord_adjust_y_edittext);
//		edittext.setEnabled(!edittext.isEnabled());
//
//		if (edittext.isEnabled()) {
//		    ((TextView) view).setTextColor(getResources().getColor(R.color.darkred));
//		} else {
//		    ((TextView) view).setTextColor(getResources().getColor(R.color.darkgray));
//		}
//	    }
//	});
//
//	Button button = (Button) dialog.findViewById(R.id.tips_button);
//	button.setOnClickListener(new OnClickListener() {
//	    @Override
//	    public void onClick(View view) {
//		showToastLong("有时，同样的凭条因为切纸原因导致左边和上边的留白存在差异，" +
//			"可以通过调整打印坐标来克服这个问题。\n\n如横向偏移小于0，" +
//			"表示所有打印元素往左边偏移指定的距离，大于0则表示整体向右边偏移指定的距离，纵向亦然。");
//	    }
//	});

	Button button = dialog.getButton(Dialog.BUTTON_NEUTRAL);
	button.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View view) {
		PrintPageSetupDialog dialog = new PrintPageSetupDialog(print);
		dialog.show(getFragmentManager(), "PrintPageSetupDialog");
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

    @Override
    public void onDismiss(DialogInterface dialog) {
	super.onDismiss(dialog);
	print.printStop();
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

	ListView listview = new ListView(getActivity());
	builder.setView(listview);

	PageSetupAdapter adapter = new PageSetupAdapter(print.getForm());
	listview.setAdapter(adapter);

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
	    return form.getPageCount() + 1;
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

	private View commentTextview(Context context) {
	    TextView textview = new TextView(context);
	    textview.setText("有时同样的凭条因为切纸原因导致左边和上边的留白存在差异，" +
			"可以通过调整页边距来克服这个问题。\n如左边距小于0，" +
			"表示所有打印元素往左边偏移指定的距离，大于0则表示整体向右边偏移指定的距离，纵向亦然。\n" +
			"偏移单位为毫米。");
	    textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
	    textview.setPadding(10, 10, 10, 5);
	    textview.setLineSpacing(1, 1.2f);
	    textview.setTextColor(context.getResources().getColor(R.color.black));

	    return textview;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    Context context = parent.getContext();

	    if (position >= form.getPageCount()) {
		return commentTextview(context);
	    }

	    Form.FormPage page = form.getPage(position);
	    if (page == null)
		return null;

	    LinearLayout linear = new LinearLayout(context);
	    linear.setGravity(Gravity.CENTER_VERTICAL);

	    TextView textview = new TextView(context);
	    textview.setText("" + (position + 1) + "." + page.getTitle());
	    textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
	    textview.setPadding(10, 0, 10, 0);
	    textview.setGravity(Gravity.CENTER_VERTICAL);
	    textview.setTextColor(context.getResources().getColor(R.color.darkred));
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


public class Print
{
    private Form form;
    private PrintListener listener;

    public Print(Form form) {
	this.form = form;
    }

    public Form getForm() {
	return form;
    }

    public void setPrintListener(PrintListener listener) {
	this.listener = listener;
    }

    public boolean print(FragmentManager manager) {
	if (form == null) {
	    LogActivity.writeLog("没有需要打印的凭条");
	    return false;
	}

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