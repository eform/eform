package com.cansiny.eform;

import java.util.Calendar;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class FormCIB01 extends Form
{

	public FormCIB01(Activity activity) {
		super(activity);

		pages.add(new FormPage(R.string.form_title_cib01_1, R.layout.form_cib01_1));
		pages.add(new FormPage(R.string.form_title_cib01_2, R.layout.form_cib01_2));
		pages.add(new FormPage(R.string.form_title_cib01_3, R.layout.form_cib01_3));
		pages.add(new FormPage(R.string.form_title_cib01_4, R.layout.form_cib01_3));

		cardno_views.add(R.id.p1_oldcard_no_edittext);
		cardno_views.add(R.id.p2_credit1_edittext);
		cardno_views.add(R.id.p2_credit2_edittext);
	}

	
	@Override
	public void onPageStart(int page) {
		super.onPageStart(page);
		
		TextView year = findTextView(R.id.p1_date_year_edittext);
		if (year != null && year.getText().length() == 0) {
			Calendar cal = Calendar.getInstance();
			year.setText(String.format("%d", cal.get(Calendar.YEAR)));
			findTextView(R.id.p1_date_month_edittext).setText(
					String.format("%d", cal.get(Calendar.MONTH) + 1));
			findTextView(R.id.p1_date_day_edittext).setText(
					String.format("%d", cal.get(Calendar.DAY_OF_MONTH)));
		}
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
				findCheckBox(R.id.p1_cardtype_standard_checkbox).setChecked(false);
				findCheckBox(R.id.p1_cardtype_other_checkbox).setChecked(false);
				findView(R.id.p1_oldcard_no_edittext).requestFocus();
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
				findView(R.id.p1_cardtype_other_edittext).requestFocus();
			}
			break;
		case R.id.p1_oldcard_swipe_button:
			EditText edit_text = findEditText(R.id.p1_oldcard_no_edittext);
			edit_text.setText(swipeMagcard());
			edit_text.requestFocus();
			break;
		case R.id.p1_idcard_button:
			readIdCard();
			break;
		case R.id.p1_male_checkbox:
		case R.id.p1_famale_checkbox:
			checkOffSibling((CheckBox) view);
			break;
		case R.id.p1_idtype_idcard_checkbox:
		case R.id.p1_idtype_other_checkbox:
			checkOffSibling((CheckBox) view);
			if (view.getId() == R.id.p1_idtype_other_checkbox)
				findView(R.id.p1_idtype_other_edittext).requestFocus();
			break;
		case R.id.p1_resident_checkbox:
		case R.id.p1_inresident_checkbox:
			checkOffSibling((CheckBox) view);
			break;
		case R.id.p1_nationality_china_checkbox:
		case R.id.p1_nationality_other_checkbox:
			checkOffSibling((CheckBox) view);
			if (view.getId() == R.id.p1_nationality_other_checkbox)
				findView(R.id.p1_nationality_other_edittext).requestFocus();
			break;
		case R.id.p1_foreign_government_yes_checkbox:
		case R.id.p1_foreign_government_no_checkbox:
			checkOffSibling((CheckBox) view);
			break;
		case R.id.p1_job_01_checkbox:
		case R.id.p1_job_02_checkbox:
		case R.id.p1_job_03_checkbox:
		case R.id.p1_job_04_checkbox:
		case R.id.p1_job_05_checkbox:
		case R.id.p1_job_06_checkbox:
		case R.id.p1_job_07_checkbox:
		case R.id.p1_job_08_checkbox:
		case R.id.p1_job_09_checkbox:
		case R.id.p1_job_10_checkbox:
		case R.id.p1_job_11_checkbox:
		case R.id.p1_job_12_checkbox:
		case R.id.p1_job_13_checkbox:
		case R.id.p1_job_14_checkbox:
		case R.id.p1_job_15_checkbox:
		case R.id.p1_job_16_checkbox:
		case R.id.p1_job_17_checkbox:
		case R.id.p1_job_18_checkbox:
		case R.id.p1_job_19_checkbox:
		case R.id.p1_job_20_checkbox:
		case R.id.p1_job_21_checkbox:
		case R.id.p1_job_22_checkbox:
			if (((CheckBox) view).isChecked()) {
				ViewGroup table = (ViewGroup) view.getParent().getParent();
				for (int n = 0; n < table.getChildCount(); n++) {
					ViewGroup row = (ViewGroup) table.getChildAt(n);
					for (int i = 0; i < row.getChildCount(); i++) {
						CheckBox child = (CheckBox) row.getChildAt(i);
						if (child != view)
							child.setChecked(false);
					}
				}
			}
			break;
		case R.id.p1_agent_idcard_button:
			readIdCard();
			break;
		case R.id.p2_atm_transfer_open_checkbox:
		case R.id.p2_atm_transfer_modify_checkbox:
		case R.id.p2_atm_transfer_close_checkbox:
			checkOffSibling((CheckBox) view);
			break;
		case R.id.p2_credit_open_checkbox:
		case R.id.p2_credit_modify_checkbox:
		case R.id.p2_credit_close_checkbox:
			checkOffSibling((CheckBox) view);
			break;
		case R.id.p2_credit_repayment_full_checkbox:
		case R.id.p2_credit_repayment_low_checkbox:
			checkOffSibling((CheckBox) view);
			break;
		case R.id.p2_credit_currency_rmb_checkbox:
		case R.id.p2_credit_currency_usd_checkbox:
			checkOffSibling((CheckBox) view);
			break;
		case R.id.p2_reservation_transfer_rmb_checkbox:
		case R.id.p2_reservation_transfer_currency_checkbox:
			checkOffSibling((CheckBox) view);
			break;
		case R.id.p2_reservation_transfer_money_checkbox:
		case R.id.p2_reservation_transfer_exchange_checkbox:
			checkOffSibling((CheckBox) view);
			break;
		case R.id.p2_reservation_transfer_frequency_once_checkbox:
		case R.id.p2_reservation_transfer_frequency_week_checkbox:
		case R.id.p2_reservation_transfer_frequency_month_checkbox:
		case R.id.p2_reservation_transfer_frequency_quarterly_checkbox:
		case R.id.p2_reservation_transfer_frequency_halfyear_checkbox:
		case R.id.p2_reservation_transfer_frequency_year_checkbox:
		case R.id.p2_reservation_transfer_frequency_day_checkbox:
			checkOffSibling((CheckBox) view);
			break;
		case R.id.p2_reservation_transfer_stop_count_checkbox:
			if (((CheckBox) view).isChecked()) {
				findCheckBox(R.id.p2_reservation_transfer_stop_amount_checkbox).setChecked(false);
				findCheckBox(R.id.p2_reservation_transfer_stop_date_checkbox).setChecked(false);
				findView(R.id.p2_reservation_transfer_stop_count_edittext).requestFocus();
			}
			break;
		case R.id.p2_reservation_transfer_stop_amount_checkbox:
			if (((CheckBox) view).isChecked()) {
				findCheckBox(R.id.p2_reservation_transfer_stop_count_checkbox).setChecked(false);
				findCheckBox(R.id.p2_reservation_transfer_stop_date_checkbox).setChecked(false);
				findView(R.id.p2_reservation_transfer_stop_amount_edittext).requestFocus();
			}
			break;
		case R.id.p2_reservation_transfer_stop_date_checkbox:
			if (((CheckBox) view).isChecked()) {
				findCheckBox(R.id.p2_reservation_transfer_stop_count_checkbox).setChecked(false);
				findCheckBox(R.id.p2_reservation_transfer_stop_amount_checkbox).setChecked(false);
				findView(R.id.p2_reservation_transfer_stop_year_edittext).requestFocus();
			}
			break;
		case R.id.p2_ecash_open_checkbox:
		case R.id.p2_ecash_modify_checkbox:
		case R.id.p2_ecash_close_checkbox:
			checkOffSibling((CheckBox) view);
			break;
		case R.id.p2_ecash_account_open_checkbox:
		case R.id.p2_ecash_account_close_checkbox:
			checkOffSibling((CheckBox) view);
			if (view.getId() == R.id.p2_ecash_account_open_checkbox)
				findView(R.id.p2_ecash_account_edittext).requestFocus();
			break;
		case R.id.p2_unionpay_open_checkbox:
		case R.id.p2_unionpay_modify_checkbox:
		case R.id.p2_unionpay_close_checkbox:
			checkOffSibling((CheckBox) view);
			break;
		}
	}

	@Override
	public boolean verify() {
		return false;
	}


	@Override
	public boolean print() {
		return true;
	}
}
