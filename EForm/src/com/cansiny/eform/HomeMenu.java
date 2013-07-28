/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;


class SupportDialogFragment extends DialogFragment
{
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	super.onCreateDialog(savedInstanceState);

	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	builder.setTitle("联系方式");

	LinearLayout linear = new LinearLayout(getActivity());
	linear.setOrientation(LinearLayout.VERTICAL);
	linear.setBackgroundResource(R.color.white);
	linear.setPadding(10, 10, 10, 10);

	builder.setView(linear);

	TextView textview = new TextView(getActivity());
	textview.setText("公司名称： 合肥市长欣宜科贸发展有限公司\n" +
			"联系电话： 0551-6342 0410(座机)   186 5595 3721(手机)\n" +
			"联系邮箱： 18655953721@163.com\n" +
			"公司地址： 合肥市包河区宣城路64号\n" +
			"公司网址： http://www.ucu.com");
	textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
	textview.setLineSpacing(HomeActivity.convertDpToPixel(2), 1.2f);
	linear.addView(textview);

	textview = new TextView(getActivity());
	textview.setText(
		"系统版本： v1.3-build1003\n" +
		"设备标识： 3121D2S32\n" +
		"授权单位： 兴业银行合肥市分行\n");
	textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
	textview.setLineSpacing(HomeActivity.convertDpToPixel(2), 1.2f);
	linear.addView(textview);

	builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	    @Override
	    public void onClick(DialogInterface dialog, int which) {
	    }
	});
	return builder.create();
    }
		
    @Override
    public void onStart() {
	super.onStart();
    }
		
    @Override
    public void onDismiss(DialogInterface dialog) {
	super.onDismiss(dialog);
    }

}


public class HomeMenu extends DialogFragment implements OnClickListener
{
    static private final int BUTTON_TAG_MEMBER = 1;
    static private final int BUTTON_TAG_UPGRADE = 2;
    static private final int BUTTON_TAG_SETTINGS = 3;
    static private final int BUTTON_TAG_CONTACT = 4;

    private int  total_seconds = 15;
    private long starttime;
    private LinearLayout buttonBox;

    private Handler  handler = new Handler();
    private Runnable runable = new Runnable() {
	@Override
	public void run() {
	    long currtime = System.currentTimeMillis();
	    long millis = currtime - starttime;
	    int seconds = (int) (millis / 1000);

	    total_seconds -= seconds;
	    if (total_seconds <= 0) {
		dismiss();
		return;
	    }
	    starttime = currtime;

	    handler.postDelayed(this, 1000);
	}
    };

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	super.onCreateDialog(savedInstanceState);

	HomeMenuItem items[] = {
		new HomeMenuItem(R.drawable.member, "会员登陆", BUTTON_TAG_MEMBER),
		new HomeMenuItem(R.drawable.upgrade, "系统升级", BUTTON_TAG_UPGRADE),
		new HomeMenuItem(R.drawable.settings, "系统设置", BUTTON_TAG_SETTINGS),
		new HomeMenuItem(R.drawable.contact, "联系方式", BUTTON_TAG_CONTACT),
	};

	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

	buttonBox = new LinearLayout(getActivity());
	buttonBox.setOrientation(LinearLayout.HORIZONTAL);
	buttonBox.setBackgroundResource(R.color.white);

	for (int i = 0; i < items.length; i++) {
	    buttonBox.addView(items[i].get());
	}
	builder.setView(buttonBox);
	return builder.create();
    }
		
    @Override
    public void onStart() {
	super.onStart();

	FrameLayout.LayoutParams params =
		(FrameLayout.LayoutParams) buttonBox.getLayoutParams();
	params.gravity = Gravity.CENTER_HORIZONTAL;
	params.width = ViewGroup.LayoutParams.WRAP_CONTENT;

	View view = buttonBox;
	do {
	    ViewParent parent = view.getParent();
	    if (parent != null) {
		try {
		    view = (View) parent;
		    view.setBackgroundResource(R.color.transparent);
		} catch (ClassCastException e) {
		    break;
		}
	    }
	} while(view.getParent() != null);

	starttime = System.currentTimeMillis();
	handler.postDelayed(runable, 0);
    }
		
    @Override
    public void onDismiss(DialogInterface dialog) {
	super.onDismiss(dialog);
	handler.removeCallbacks(runable);
    }


    @Override
    public void onClick(View view) {
	dismiss();

	switch (((Integer) view.getTag()).intValue()) {
	case BUTTON_TAG_MEMBER:
	    Member member = Member.getMember();
	    member.login();
	    break;
	case BUTTON_TAG_UPGRADE:
	    Log.d("", "settings");
	    break;
	case BUTTON_TAG_SETTINGS:
	    Log.d("", "settings");
	    break;
	case BUTTON_TAG_CONTACT:
	    ContactDialog dialog = new ContactDialog();
	    dialog.show(getFragmentManager(), "ContactDialog");
	    break;
	}
    }


    class HomeMenuItem
    {
	private int icon;
	private String label;
	private int tag;

	public HomeMenuItem(int icon, String label, int tag) {
	    this.icon = icon;
	    this.label = label;
	    this.tag = tag;
	}
	
	public Button get() {
	    Button button = new Button(getActivity());
	    button.setTag(tag);
	    button.setText(label);
	    button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
	    button.setTextColor(getResources().getColor(R.color.black));
	    button.setCompoundDrawablesWithIntrinsicBounds(0, icon, 0, 0);
	    button.setCompoundDrawablePadding(5);
	    button.setPadding(15, 10, 15, 10);
	    button.setBackgroundResource(R.drawable.button);
	    button.setOnClickListener(HomeMenu.this);
	    return button;
	}
    }
}
