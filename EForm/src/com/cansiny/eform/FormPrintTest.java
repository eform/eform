/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import android.app.Activity;
import android.widget.Button;

public class FormPrintTest extends Form {

    public FormPrintTest(Activity activity, String label) {
	super(activity, label);

	pages.add(new FormPage(activity, R.string.form_title_print_test, R.layout.form_print_test));
	setActivePage(0);
    }

    @Override
    void onIDCardResponse(Button button, IDCard.IDCardInfo info) {
    }

}
