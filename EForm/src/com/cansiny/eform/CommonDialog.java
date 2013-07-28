/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.Button;

public class CommonDialog extends DialogFragment
{
    private String title;
    private String message;

    public void set(String title, String message) {
	this.title = title;
	this.message = message;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	super.onCreateDialog(savedInstanceState);

	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

	if (title != null) {
	    builder.setTitle(title);
	}
	if (message != null) {
	    builder.setMessage(message);
	}
	builder.setNegativeButton("关 闭", null);

	return builder.create();
    }

    @Override
    public void onStart() {
	super.onStart();

	final AlertDialog dialog = (AlertDialog) getDialog();

	Button button = dialog.getButton(Dialog.BUTTON_NEGATIVE);
	button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
    }

    @Override
    public void onDismiss (DialogInterface dialog) {
	super.onDismiss(dialog);
    }


    static public void showBox(FragmentManager manager,
	    String title, String message) {
	CommonDialog dialog = new CommonDialog();
	dialog.set(title, message);
	dialog.show(manager, "CommonDialog");
    }
}
