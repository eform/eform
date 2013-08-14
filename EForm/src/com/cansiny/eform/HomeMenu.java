/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import java.io.IOException;

import com.cansiny.eform.Administrator.AdministratorLoginDialogListener;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

class ShutdownDialog extends Utils.DialogFragment implements AdministratorLoginDialogListener
{
    private boolean is_shutdown;
    private boolean is_reboot;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	super.onCreateDialog(savedInstanceState);

	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

	builder.setTitle("关闭设备");
	builder.setMessage("\n确定要关闭设备吗？\n");
	builder.setNegativeButton("取 消", null);
	builder.setNeutralButton("重启设备", new DialogInterface.OnClickListener() {
	    @Override
	    public void onClick(DialogInterface dialog, int which) {
		Administrator admin = Administrator.getAdministrator();
		if (!admin.isLogin()) {
		    showToast("需要管理员权限，请先登录...");
		    is_reboot = true;
		    admin.setLoginDialogListener(ShutdownDialog.this);
		    admin.login(getFragmentManager());
		} else {
		    try {
			Process su = Runtime.getRuntime().exec("/system/xbin/su");
			String command = "reboot";
			Utils.showToastLong("正在重启设备，请稍候 ...");
			su.getOutputStream().write(command.getBytes());
		    } catch (IOException e) {
			LogActivity.writeLog(e);
		    }
		}
	    }
	});
	builder.setPositiveButton("关闭设备", new DialogInterface.OnClickListener() {
	    @Override
	    public void onClick(DialogInterface dialog, int which) {
		Administrator admin = Administrator.getAdministrator();
		if (!admin.isLogin()) {
		    showToast("需要管理员权限，请先登录...");
		    is_shutdown = true;
		    admin.login(getFragmentManager());
		} else {
		    LogActivity.writeLog("关闭系统...");
		    Utils.showToastLong("正在关闭设备，请稍候 ...");
		    /* TODO: Shutdown device */
		}
	    }
	});

	return builder.create();
    }

    @Override
    public void onAdministratorLoginDialogDisapper(Administrator admin) {
	admin.setLoginDialogListener(null);

	if (admin.isLogin()) {
	    dismiss();

	    if (is_shutdown) {
		LogActivity.writeLog("关闭系统...");
		Utils.showToastLong("正在关闭设备，请稍候 ...");
		/* TODO: Shutdown device */
		return;
	    }

	    if (is_reboot) {
		try {
		    Utils.showToastLong("正在重启设备，请稍候 ...");
		    Process proc = Runtime.getRuntime().exec("/system/xbin/su");
		    proc.getOutputStream().write("reboot".getBytes());
		} catch (IOException e) {
		    LogActivity.writeLog(e);
		}
	    }
	}
    }
}


public class HomeMenu extends Utils.DialogFragment
	implements OnClickListener, Administrator.AdministratorLoginDialogListener
{
    static private final int BUTTON_TAG_MEMBER = 1;
    static private final int BUTTON_TAG_SETTINGS = 2;
    static private final int BUTTON_TAG_CONTACT = 3;
    static private final int BUTTON_TAG_SHUTDOWN = 4;

    private int  outtime = 15;
    private long starttime;
    private LinearLayout button_box;

    private Handler  handler = new Handler();
    private Runnable runable = new Runnable() {
	@Override
	public void run() {
	    long currtime = System.currentTimeMillis();
	    if (currtime - starttime >= outtime * 1000) {
		dismiss();
		return;
	    }
	    handler.postDelayed(this, 1000);
	}
    };

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	super.onCreateDialog(savedInstanceState);

	HomeMenuItem items[] = {
		new HomeMenuItem(R.drawable.member, "会员登录", BUTTON_TAG_MEMBER),
		new HomeMenuItem(R.drawable.settings, "系统设置", BUTTON_TAG_SETTINGS),
		new HomeMenuItem(R.drawable.contact, "联系方式", BUTTON_TAG_CONTACT),
		new HomeMenuItem(R.drawable.shutdown, "关闭设备", BUTTON_TAG_SHUTDOWN),
	};

	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

	button_box = new LinearLayout(getActivity());
	button_box.setOrientation(LinearLayout.HORIZONTAL);
	button_box.setBackgroundResource(R.color.white);
	button_box.setGravity(Gravity.CENTER_HORIZONTAL);

	for (int i = 0; i < items.length; i++) {
	    button_box.addView(items[i].get());
	}
	builder.setView(button_box);
	return builder.create();
    }

    @Override
    public void onStart() {
	super.onStart();

	FrameLayout.LayoutParams params =
		(FrameLayout.LayoutParams) button_box.getLayoutParams();
	params.gravity = Gravity.CENTER_HORIZONTAL;
	params.width = ViewGroup.LayoutParams.WRAP_CONTENT;

	View view = button_box;
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
	Administrator admin = Administrator.getAdministrator();

	switch (((Integer) view.getTag()).intValue()) {
	case BUTTON_TAG_MEMBER:
	    dismiss();
	    Member member = Member.getMember();
	    member.login(getFragmentManager());
	    break;
	case BUTTON_TAG_SETTINGS:
	    if (!admin.isLogin()) {
		Utils.showToast("需要管理员权限，请先登录...");
		handler.removeCallbacks(runable);
		admin.setLoginDialogListener(this);
		admin.login(getFragmentManager());
	    } else {
		dismiss();
		Preferences prefs = Preferences.getPreferences();
		prefs.showDialog(getFragmentManager());
	    }
	    break;
	case BUTTON_TAG_CONTACT:
	    dismiss();
	    ContactDialog dialog = new ContactDialog();
	    dialog.show(getFragmentManager(), "ContactDialog");
	    break;
	case BUTTON_TAG_SHUTDOWN:
	    ShutdownDialog shutdown = new ShutdownDialog();
	    shutdown.show(getFragmentManager(), "ShutdownDialog");
	    break;
	}
    }

    @Override
    public void onAdministratorLoginDialogDisapper(Administrator admin) {
	admin.setLoginDialogListener(null);

	if (admin.isLogin()) {
	    dismiss();
	    Preferences prefs = Preferences.getPreferences();
	    prefs.showDialog(getFragmentManager());
	} else {
	    starttime = System.currentTimeMillis();
	    handler.postDelayed(runable, 0);
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
