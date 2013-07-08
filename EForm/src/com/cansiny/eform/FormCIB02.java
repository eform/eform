/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 * 
 * HomeActivity - The application entry
 *
 * Authors:
 *   Xiaohu <xiaohu417@gmail.com>, 2013.7.6, hefei
 */
package com.cansiny.eform;

import android.app.Activity;


public class FormCIB02 extends Form {

	public FormCIB02(Activity activity) {
		super(activity);

		pages.add(new FormPage(R.string.form_title_cib01_2, R.layout.form_cib02_1));
	}

}
