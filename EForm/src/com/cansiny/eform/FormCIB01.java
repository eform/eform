/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import java.util.Calendar;
import android.app.Activity;
import android.text.Editable;
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
	pages.add(new FormPage(R.string.form_title_cib01_4, R.layout.form_cib01_4));

	/* card no views for this form */
	cardno_views.add(R.id.p1_oldcard_no_edittext);
	cardno_views.add(R.id.p2_credit1_edittext);
	cardno_views.add(R.id.p2_credit2_edittext);

	/* verify views for this form */
	verify_views.add(R.id.p1_name_edittext);
	verify_views.add(R.id.p1_idno_edittext);
    }

	
    @Override
    public void onPageStart() {
	/* call super method */
	super.onPageStart();

	/* set current date to request date */
	TextView year = findTextView(R.id.p1_date_year_edittext);
	if (year != null && year.getText().length() == 0) {
	    Calendar cal = Calendar.getInstance();
	    year.setText(String.format("%d", cal.get(Calendar.YEAR)));
	    findTextView(R.id.p1_date_month_edittext).
		setText(String.format("%d", cal.get(Calendar.MONTH) + 1));
	    findTextView(R.id.p1_date_day_edittext).
		setText(String.format("%d", cal.get(Calendar.DAY_OF_MONTH)));
	}
    }

	
    @Override
    public void onClick(View view) {
	super.onClick(view);

	switch (view.getId()) {
	case R.id.p1_newcard_checkbox:
	    if (((CheckBox) view).isChecked()) {
		findCheckBox(R.id.p1_oldcard_checkbox).setChecked(false);
		removeWarningImage(findView(R.id.p1_request_info_textview));
		removeWarningImage(findView(R.id.p1_oldcard_no_edittext));

		verify_views.add(R.id.p1_issue_depart_edittext);
		verify_views.add(R.id.p1_address_edittext);
		verify_views.add(R.id.p1_company_edittext);
		verify_views.add(R.id.p1_enddate_year_edittext);
		verify_views.add(R.id.p1_enddate_month_edittext);
		verify_views.add(R.id.p1_enddate_day_edittext);
		verify_views.add(R.id.p1_date_year_edittext);
		verify_views.add(R.id.p1_date_month_edittext);
		verify_views.add(R.id.p1_date_day_edittext);
	    }
	    break;

	case R.id.p1_oldcard_checkbox:
	    if (((CheckBox) view).isChecked()) {
		findCheckBox(R.id.p1_newcard_checkbox).setChecked(false);
		findCheckBox(R.id.p1_cardtype_standard_checkbox).setChecked(false);
		findCheckBox(R.id.p1_cardtype_other_checkbox).setChecked(false);
		findView(R.id.p1_oldcard_no_edittext).requestFocus();

		removeWarningImage(findView(R.id.p1_request_info_textview));
		removeWarningImage(findView(R.id.p1_cardtype_other_edittext));
		removeWarningImage(findView(R.id.p1_cardtype_standard_checkbox));

		verify_views.clear();
		verify_views.add(R.id.p1_name_edittext);
		verify_views.add(R.id.p1_idno_edittext);
	    }
	    break;

	case R.id.p1_cardtype_standard_checkbox:
	    if (((CheckBox) view).isChecked()) {
		findCheckBox(R.id.p1_newcard_checkbox).setChecked(true);
		findCheckBox(R.id.p1_oldcard_checkbox).setChecked(false);
		findCheckBox(R.id.p1_cardtype_other_checkbox).setChecked(false);

		removeWarningImage(findView(R.id.p1_request_info_textview));
		removeWarningImage(findView(R.id.p1_cardtype_other_edittext));
		removeWarningImage(findView(R.id.p1_cardtype_standard_checkbox));
	    }
	    break;

	case R.id.p1_cardtype_other_checkbox:
	    if (((CheckBox) view).isChecked()) {
		findCheckBox(R.id.p1_newcard_checkbox).setChecked(true);
		findCheckBox(R.id.p1_oldcard_checkbox).setChecked(false);
		findCheckBox(R.id.p1_cardtype_standard_checkbox).setChecked(false);
		findView(R.id.p1_cardtype_other_edittext).requestFocus();

		removeWarningImage(findView(R.id.p1_request_info_textview));
		removeWarningImage(findView(R.id.p1_cardtype_standard_checkbox));
	    }
	    break;

	case R.id.p1_oldcard_swipe_button:
	    EditText edit_text = findEditText(R.id.p1_oldcard_no_edittext);
	    swipeMagcard(edit_text);
	    edit_text.requestFocus();
	    break;

	case R.id.p1_idcard_button:
	    readIdCard();
	    break;

	case R.id.p1_male_checkbox:
	case R.id.p1_famale_checkbox:
	    checkOffSibling((CheckBox) view);
	    removeWarningImage(findView(R.id.p1_male_checkbox));
	    break;

	case R.id.p1_idtype_idcard_checkbox:
	case R.id.p1_idtype_other_checkbox:
	    checkOffSibling((CheckBox) view);
	    removeWarningImage(findView(R.id.p1_idtype_idcard_checkbox));
	    if (view.getId() == R.id.p1_idtype_other_checkbox)
		findView(R.id.p1_idtype_other_edittext).requestFocus();
	    break;

	case R.id.p1_resident_checkbox:
	case R.id.p1_inresident_checkbox:
	    checkOffSibling((CheckBox) view);
	    removeWarningImage(findView(R.id.p1_resident_checkbox));
	    break;

	case R.id.p1_nationality_china_checkbox:
	case R.id.p1_nationality_other_checkbox:
	    checkOffSibling((CheckBox) view);
	    removeWarningImage(findView(R.id.p1_nationality_china_checkbox));
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
		removeWarningImage(findView(R.id.p1_job_tablelayout));
	    }
	    break;

	case R.id.p1_contact_mobile_checkbox:
	case R.id.p1_contact_office_checkbox:
	case R.id.p1_contact_home_checkbox:
	    removeWarningImage(findView(R.id.p1_contact_mobile_checkbox));
	    break;

	case R.id.p1_address_office_checkbox:
	case R.id.p1_address_home_checkbox:
	    removeWarningImage(findView(R.id.p1_address_office_checkbox));
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
	    if (((CheckBox) view).isChecked()) {
		if (view.getId() == R.id.p2_reservation_transfer_rmb_checkbox)
		    findCheckBox(R.id.p2_reservation_transfer_currency_checkbox).setChecked(false);
		else
		    findCheckBox(R.id.p2_reservation_transfer_rmb_checkbox).setChecked(false);
	    }
	    break;

	case R.id.p2_reservation_transfer_money_checkbox:
	case R.id.p2_reservation_transfer_exchange_checkbox:
	    if (((CheckBox) view).isChecked()) {
		if (view.getId() == R.id.p2_reservation_transfer_money_checkbox)
		    findCheckBox(R.id.p2_reservation_transfer_exchange_checkbox).setChecked(false);
		else
		    findCheckBox(R.id.p2_reservation_transfer_money_checkbox).setChecked(false);
	    }
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

	case R.id.p2_landline_transfer_open_checkbox:
	case R.id.p2_landline_transfer_modify_checkbox:
	case R.id.p2_landline_transfer_close_checkbox:
	    checkOffSibling((CheckBox) view);
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

	case R.id.p3_msgbank_open_checkbox:
	case R.id.p3_msgbank_close_checkbox:
	    checkOffSibling((CheckBox) view);
	    findEditText(R.id.p3_msgbank_phone_edittext).requestFocus();
	    break;
	    
	case R.id.p3_shield_apply_checkbox:
	case R.id.p3_shield_bind_checkbox:
	case R.id.p3_shield_unbind_checkbox:
	case R.id.p3_shield_change_checkbox:
	case R.id.p3_shield_reset_checkbox:
	case R.id.p3_shield_resend_checkbox:
	case R.id.p3_shield_extension_checkbox:
	case R.id.p3_shield_repeal_checkbox:
	    checkOffSibling((CheckBox) view);
	    break;

	case R.id.p3_token_apply_checkbox:
	case R.id.p3_token_bind_checkbox:
	case R.id.p3_token_unbind_checkbox:
	case R.id.p3_token_change_checkbox:
	case R.id.p3_token_reset_checkbox:
	case R.id.p3_token_unlock_checkbox:
	case R.id.p3_token_sync_checkbox:
	case R.id.p3_token_repeal_checkbox:
	case R.id.p3_token_lost_checkbox:
	case R.id.p3_token_unlost_checkbox:
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

	case R.id.p3_emissary_open_checkbox:
	case R.id.p3_emissary_close_checkbox:
	    checkOffSibling((CheckBox) view);
	    break;

	case R.id.p3_netpay_open_checkbox:
	case R.id.p3_netpay_close_checkbox:
	    checkOffSibling((CheckBox) view);
	    findTextView(R.id.p3_netpay_limit_edittext).requestFocus();
	    break;

	case R.id.p3_netbank_transfer_free_checkbox:
	    if (((CheckBox) view).isChecked()) {
		findCheckBox(R.id.p3_netbank_transfer_direct_checkbox).setChecked(false);
		findCheckBox(R.id.p3_netbank_transfer_delete_checkbox).setChecked(false);
		findCheckBox(R.id.p3_netbank_transfer_close_checkbox).setChecked(false);
		findTextView(R.id.p3_netbank_transfer_free_limit_edittext).requestFocus();
	    }
	    break;
	case R.id.p3_netbank_transfer_direct_checkbox:
	    if (((CheckBox) view).isChecked()) {
		findCheckBox(R.id.p3_netbank_transfer_free_checkbox).setChecked(false);
		findCheckBox(R.id.p3_netbank_transfer_delete_checkbox).setChecked(false);
		findCheckBox(R.id.p3_netbank_transfer_close_checkbox).setChecked(false);
		findTextView(R.id.p3_netbank_transfer_direct_limit_edittext).requestFocus();
	    }
	    break;
	case R.id.p3_netbank_transfer_delete_checkbox:
	    if (((CheckBox) view).isChecked()) {
		findCheckBox(R.id.p3_netbank_transfer_free_checkbox).setChecked(false);
		findCheckBox(R.id.p3_netbank_transfer_direct_checkbox).setChecked(false);
		findCheckBox(R.id.p3_netbank_transfer_close_checkbox).setChecked(false);
	    }
	    break;
	case R.id.p3_netbank_transfer_close_checkbox:
	    if (((CheckBox) view).isChecked()) {
		findCheckBox(R.id.p3_netbank_transfer_free_checkbox).setChecked(false);
		findCheckBox(R.id.p3_netbank_transfer_direct_checkbox).setChecked(false);
		findCheckBox(R.id.p3_netbank_transfer_delete_checkbox).setChecked(false);
	    }
	    break;

	case R.id.p3_mobbank_transfer_free_checkbox:
	    if (((CheckBox) view).isChecked()) {
		findCheckBox(R.id.p3_mobbank_transfer_direct_checkbox).setChecked(false);
		findCheckBox(R.id.p3_mobbank_transfer_delete_checkbox).setChecked(false);
		findCheckBox(R.id.p3_mobbank_transfer_close_checkbox).setChecked(false);
		findTextView(R.id.p3_mobbank_transfer_free_limit_edittext).requestFocus();
	    }
	    break;
	case R.id.p3_mobbank_transfer_direct_checkbox:
	    if (((CheckBox) view).isChecked()) {
		findCheckBox(R.id.p3_mobbank_transfer_free_checkbox).setChecked(false);
		findCheckBox(R.id.p3_mobbank_transfer_delete_checkbox).setChecked(false);
		findCheckBox(R.id.p3_mobbank_transfer_close_checkbox).setChecked(false);
		findTextView(R.id.p3_mobbank_transfer_direct_limit_edittext).requestFocus();
	    }
	    break;
	case R.id.p3_mobbank_transfer_delete_checkbox:
	    if (((CheckBox) view).isChecked()) {
		findCheckBox(R.id.p3_mobbank_transfer_free_checkbox).setChecked(false);
		findCheckBox(R.id.p3_mobbank_transfer_direct_checkbox).setChecked(false);
		findCheckBox(R.id.p3_mobbank_transfer_close_checkbox).setChecked(false);
	    }
	    break;
	case R.id.p3_mobbank_transfer_close_checkbox:
	    if (((CheckBox) view).isChecked()) {
		findCheckBox(R.id.p3_mobbank_transfer_free_checkbox).setChecked(false);
		findCheckBox(R.id.p3_mobbank_transfer_direct_checkbox).setChecked(false);
		findCheckBox(R.id.p3_mobbank_transfer_delete_checkbox).setChecked(false);
	    }
	    break;

	case R.id.p3_telbank_transfer_free_checkbox:
	    if (((CheckBox) view).isChecked()) {
		findCheckBox(R.id.p3_telbank_transfer_direct_checkbox).setChecked(false);
		findCheckBox(R.id.p3_telbank_transfer_delete_checkbox).setChecked(false);
		findCheckBox(R.id.p3_telbank_transfer_close_checkbox).setChecked(false);
		findTextView(R.id.p3_telbank_transfer_free_limit_edittext).requestFocus();
	    }
	    break;
	case R.id.p3_telbank_transfer_direct_checkbox:
	    if (((CheckBox) view).isChecked()) {
		findCheckBox(R.id.p3_telbank_transfer_free_checkbox).setChecked(false);
		findCheckBox(R.id.p3_telbank_transfer_delete_checkbox).setChecked(false);
		findCheckBox(R.id.p3_telbank_transfer_close_checkbox).setChecked(false);
		findTextView(R.id.p3_telbank_transfer_direct_limit_edittext).requestFocus();
	    }
	    break;
	case R.id.p3_telbank_transfer_delete_checkbox:
	    if (((CheckBox) view).isChecked()) {
		findCheckBox(R.id.p3_telbank_transfer_free_checkbox).setChecked(false);
		findCheckBox(R.id.p3_telbank_transfer_direct_checkbox).setChecked(false);
		findCheckBox(R.id.p3_telbank_transfer_close_checkbox).setChecked(false);
	    }
	    break;
	case R.id.p3_telbank_transfer_close_checkbox:
	    if (((CheckBox) view).isChecked()) {
		findCheckBox(R.id.p3_telbank_transfer_free_checkbox).setChecked(false);
		findCheckBox(R.id.p3_telbank_transfer_direct_checkbox).setChecked(false);
		findCheckBox(R.id.p3_telbank_transfer_delete_checkbox).setChecked(false);
	    }
	    break;

	case R.id.p3_netbank_payment_open_checkbox:
	case R.id.p3_netbank_payment_close_checkbox:
	    checkOffSibling((CheckBox) view);
	    break;

	case R.id.p3_mobbank_payment_open_checkbox:
	case R.id.p3_mobbank_payment_close_checkbox:
	    checkOffSibling((CheckBox) view);
	    break;

	case R.id.p3_telbank_payment_open_checkbox:
	case R.id.p3_telbank_payment_close_checkbox:
	    checkOffSibling((CheckBox) view);
	    break;

	case R.id.p4_convenient_open_checkbox:
	case R.id.p4_convenient_close_checkbox:
	    checkOffSibling((CheckBox) view);
	    break;

	case R.id.p4_earnings_open_checkbox:
	case R.id.p4_earnings_modify_checkbox:
	case R.id.p4_earnings_close_checkbox:
	    checkOffSibling((CheckBox) view);
	    break;

	case R.id.p4_fund_open_checkbox:
	case R.id.p4_fund_close_checkbox:
	    checkOffSibling((CheckBox) view);
	    break;

	case R.id.p4_forex_open_checkbox:
	case R.id.p4_forex_close_checkbox:
	    checkOffSibling((CheckBox) view);
	    break;

	case R.id.p4_gold_open_checkbox:
	case R.id.p4_gold_close_checkbox:
	case R.id.p4_gold_upgrade_checkbox:
	    checkOffSibling((CheckBox) view);
	    break;

	case R.id.p4_new_business_open_checkbox:
	case R.id.p4_new_business_close_checkbox:
	case R.id.p4_new_business_modify_checkbox:
	    checkOffSibling((CheckBox) view);
	    break;
	}
    }


    @Override
    protected void afterTextChanged(TextView textview, Editable editable) {
	super.afterTextChanged(textview, editable);

	if (editable.length() <= 0)
	    return;

	/* clear warning image when text changed */
	switch (textview.getId()) {
	case R.id.p1_cardtype_other_edittext:
	case R.id.p1_oldcard_no_edittext:
	case R.id.p1_idtype_other_edittext:
	case R.id.p1_nationality_other_edittext:
	case R.id.p1_contact_email_edittext:
	    removeWarningImage(textview);
	    break;
	}
    }

	
    @Override
    public int verify() {
	int retval = super.verify();

	switch (active_page) {
	case 0:
	    return retval + verifyPage1();
	}
	return retval;
    }

    protected int verifyPage1() {
	int retval = 0;
		
	if (!findCheckBox(R.id.p1_newcard_checkbox).isChecked() &&
	    !findCheckBox(R.id.p1_oldcard_checkbox).isChecked()) {
	    insertWarningImage(findView(R.id.p1_request_info_textview),
			       "\"新卡申请\"/\"功能维护\"中必须选择一项");
	    retval++;
	}

	if (findCheckBox(R.id.p1_newcard_checkbox).isChecked() &&
	    !findCheckBox(R.id.p1_cardtype_standard_checkbox).isChecked() &&
	    !findCheckBox(R.id.p1_cardtype_other_checkbox).isChecked()) {
	    insertWarningImage(findView(R.id.p1_cardtype_standard_checkbox),
			       "新卡申请必须选择一项\"卡种类\"");
	    retval++;
	}

	if (findCheckBox(R.id.p1_newcard_checkbox).isChecked() &&
	    findCheckBox(R.id.p1_cardtype_other_checkbox).isChecked() &&
	    findTextView(R.id.p1_cardtype_other_edittext).length() == 0) {
	    insertWarningImage(findView(R.id.p1_cardtype_other_edittext), "请输入其他卡种类名");
	    retval++;
	}

	if (findCheckBox(R.id.p1_oldcard_checkbox).isChecked() &&
	    findTextView(R.id.p1_oldcard_no_edittext).length() == 0) {
	    insertWarningImage(findView(R.id.p1_oldcard_no_edittext),
			       "请输入需要维护的卡／帐号");
	    retval++;
	}
		
	if (findCheckBox(R.id.p1_newcard_checkbox).isChecked() &&
	    !findCheckBox(R.id.p1_male_checkbox).isChecked() &&
	    !findCheckBox(R.id.p1_famale_checkbox).isChecked()) {
	    insertWarningImage(findView(R.id.p1_male_checkbox), "请选择性别");
	    retval++;
	}

	if (!findCheckBox(R.id.p1_idtype_idcard_checkbox).isChecked() &&
	    !findCheckBox(R.id.p1_idtype_other_checkbox).isChecked()) {
	    insertWarningImage(findView(R.id.p1_idtype_idcard_checkbox), "请选择证件类型");
	    retval++;
	}

	if (findCheckBox(R.id.p1_idtype_other_checkbox).isChecked() &&
	    findTextView(R.id.p1_idtype_other_edittext).length() == 0) {
	    insertWarningImage(findView(R.id.p1_idtype_other_edittext), "请输入证件类型");
	    retval++;
	}

	if (findCheckBox(R.id.p1_newcard_checkbox).isChecked() &&
	    !findCheckBox(R.id.p1_resident_checkbox).isChecked() &&
	    !findCheckBox(R.id.p1_inresident_checkbox).isChecked()) {
	    insertWarningImage(findView(R.id.p1_resident_checkbox), "请选择居民性质");
	    retval++;
	}

	if (findCheckBox(R.id.p1_newcard_checkbox).isChecked() &&
	    !findCheckBox(R.id.p1_nationality_china_checkbox).isChecked() &&
	    !findCheckBox(R.id.p1_nationality_other_checkbox).isChecked()) {
	    insertWarningImage(findView(R.id.p1_nationality_china_checkbox), "请选择国籍");
	    retval++;
	}

	if (findCheckBox(R.id.p1_nationality_other_checkbox).isChecked() &&
	    findTextView(R.id.p1_nationality_other_edittext).length() == 0) {
	    insertWarningImage(findView(R.id.p1_nationality_other_edittext), "请输入国籍名称");
	    retval++;
	}

	if (findCheckBox(R.id.p1_newcard_checkbox).isChecked() &&
	    !findCheckBox(R.id.p1_job_01_checkbox).isChecked() &&
	    !findCheckBox(R.id.p1_job_02_checkbox).isChecked() &&
	    !findCheckBox(R.id.p1_job_03_checkbox).isChecked() &&
	    !findCheckBox(R.id.p1_job_04_checkbox).isChecked() &&
	    !findCheckBox(R.id.p1_job_05_checkbox).isChecked() &&
	    !findCheckBox(R.id.p1_job_06_checkbox).isChecked() &&
	    !findCheckBox(R.id.p1_job_07_checkbox).isChecked() &&
	    !findCheckBox(R.id.p1_job_08_checkbox).isChecked() &&
	    !findCheckBox(R.id.p1_job_09_checkbox).isChecked() &&
	    !findCheckBox(R.id.p1_job_10_checkbox).isChecked() &&
	    !findCheckBox(R.id.p1_job_11_checkbox).isChecked() &&
	    !findCheckBox(R.id.p1_job_12_checkbox).isChecked() &&
	    !findCheckBox(R.id.p1_job_13_checkbox).isChecked() &&
	    !findCheckBox(R.id.p1_job_14_checkbox).isChecked() &&
	    !findCheckBox(R.id.p1_job_15_checkbox).isChecked() &&
	    !findCheckBox(R.id.p1_job_16_checkbox).isChecked() &&
	    !findCheckBox(R.id.p1_job_17_checkbox).isChecked() &&
	    !findCheckBox(R.id.p1_job_18_checkbox).isChecked() &&
	    !findCheckBox(R.id.p1_job_19_checkbox).isChecked() &&
	    !findCheckBox(R.id.p1_job_20_checkbox).isChecked() &&
	    !findCheckBox(R.id.p1_job_21_checkbox).isChecked() &&
	    !findCheckBox(R.id.p1_job_22_checkbox).isChecked()) {
	    insertWarningImage(findView(R.id.p1_job_tablelayout), "请选择一项职业");
	    retval++;
	}

	if (findCheckBox(R.id.p1_newcard_checkbox).isChecked() &&
	    !findCheckBox(R.id.p1_contact_mobile_checkbox).isChecked() &&
	    !findCheckBox(R.id.p1_contact_home_checkbox).isChecked() &&
	    !findCheckBox(R.id.p1_contact_office_checkbox).isChecked()) {
	    insertWarningImage(findView(R.id.p1_contact_mobile_checkbox), "请选择至少一项常用电话");
	    retval++;
	}

	if (findCheckBox(R.id.p1_newcard_checkbox).isChecked() &&
	    !findCheckBox(R.id.p1_address_office_checkbox).isChecked() &&
	    !findCheckBox(R.id.p1_address_home_checkbox).isChecked()) {
	    insertWarningImage(findView(R.id.p1_address_office_checkbox), "请选择至少一项常用地址");
	    retval++;
	}

	if (findCheckBox(R.id.p1_newcard_checkbox).isChecked() &&
	    findTextView(R.id.p1_contact_email_edittext).length() == 0) {
	    insertWarningImage(findView(R.id.p1_contact_email_edittext), "请输入电子邮箱");
	    retval++;
	}

	return retval;
    }

	
    @Override
    public boolean print() {
	return true;
    }
}
