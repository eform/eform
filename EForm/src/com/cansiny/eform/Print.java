/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;


class PrintDialog extends Utils.DialogFragment
{
    private Form form;
    private Spinner page_from_spinner;
    private Spinner page_to_spinner;

    public PrintDialog(Form form) {
	this.form = form;
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
	Utils.IntegerAdapter adapter = new Utils.IntegerAdapter(1, form.getPageCount());
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
	adapter = new Utils.IntegerAdapter(1, form.getPageCount());
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
//	LayoutInflater inflater = getActivity().getLayoutInflater();
//	builder.setView(inflater.inflate(R.layout.dialog_print, null));
	builder.setNegativeButton("取 消", null);
	builder.setPositiveButton("打 印", null);

	return builder.create();
    }

    @Override
    public void onStart() {
	super.onStart();

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

public class Print
{
    private Form form;

    public Print(Form form) {
	this.form = form;
    }

    public boolean print(FragmentManager manager) {
	if (form == null) {
	    LogActivity.writeLog("没有需要打印的凭条");
	    return false;
	}
	PrintDialog dialog = new PrintDialog(form);
	dialog.show(manager, "PrintDialog");
	return true;
    }
}
