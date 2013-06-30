package com.cansiny.eform;

import android.app.Activity;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FormCIB01 extends Form
{

	public FormCIB01(Activity activity) {
		super(activity);
		
		addFormPage(R.string.form_title_cib01_1, R.layout.form_cib01_1);
//		addFormPage(R.string.form_title_cib01_2, R.layout.form_cib01_2);
	}

	
	@Override
	public void pageVisibled(Form.FormPage page) {
		super.pageVisibled(page);

		switch (pages.indexOf(page)) {
		case 0:
			cardno_textviews.add(findTextView(R.id.p1_oldcard_no_edittext));
			break;
		}
	}

	
	@Override
	public boolean validate() {
		// TODO Auto-generated method stub
		return false;
	}

	
	@Override
	public void onClick(View view) {
		super.onClick(view);

		switch (view.getId()) {
		case R.id.p1_newcard_checkbox:
			if (((CheckBox) view).isChecked()) {
				findCheckBox(R.id.p1_oldcard_checkbox).setChecked(false);
			}
			break;
		case R.id.p1_oldcard_checkbox:
			if (((CheckBox) view).isChecked()) {
				findCheckBox(R.id.p1_newcard_checkbox).setChecked(false);
			}
			break;
		case R.id.p1_cardtype_standard_checkbox:
			if (((CheckBox) view).isChecked()) {
				findCheckBox(R.id.p1_newcard_checkbox).setChecked(true);
				findCheckBox(R.id.p1_oldcard_checkbox).setChecked(false);
				findCheckBox(R.id.p1_cardtype_other_checkbox).setChecked(false);
			}
			break;
		case R.id.p1_cardtype_other_checkbox:
			if (((CheckBox) view).isChecked()) {
				findCheckBox(R.id.p1_newcard_checkbox).setChecked(true);
				findCheckBox(R.id.p1_oldcard_checkbox).setChecked(false);
				findCheckBox(R.id.p1_cardtype_standard_checkbox).setChecked(false);
			}
			break;
		case R.id.p1_oldcard_swipe_button:
			EditText edit_text = findEditText(R.id.p1_oldcard_no_edittext);
			edit_text.setText(swipeMagcard());
			edit_text.requestFocus();
			break;
		}
	}

	private void p1_adjust_to_newcard(boolean to_newcard) {
		LinearLayout newcard_linear = findLinearLayout(R.id.p1_newcard_linearlayout);
		LinearLayout oldcard_linear = findLinearLayout(R.id.p1_oldcard_linearlayout);
		for (int i = 0; i < newcard_linear.getChildCount(); i++) {
			newcard_linear.getChildAt(i).setEnabled(to_newcard);
		}
		for (int i = 0; i < oldcard_linear.getChildCount(); i++) {
			oldcard_linear.getChildAt(i).setEnabled(!to_newcard);
		}
		if (to_newcard) {
			findCheckBox(R.id.p1_oldcard_checkbox).setChecked(false);
			if (!findCheckBox(R.id.p1_cardtype_other_checkbox).isChecked()) {
				findView(R.id.p1_cardtype_other_edittext).setEnabled(false);
			}
		} else {
			findCheckBox(R.id.p1_newcard_checkbox).setChecked(false);
		}
	}

	@Override
	public void afterTextChanged(TextView textview, Editable editable) {
		super.afterTextChanged(textview, editable);
	}

	@Override
	protected void beforeTextChanged(TextView textview, CharSequence sequence,
			int start, int count, int after) {
		super.beforeTextChanged(textview, sequence, start, count, after);
	}

	@Override
	protected void onTextChanged(TextView textview, CharSequence sequence,
			int start, int before, int count) {
		super.onTextChanged(textview, sequence, start, before, count);
	}

	@Override
	public void onFocusChange(View view, boolean hasfocus) {
		super.onFocusChange(view, hasfocus);
	}

}
