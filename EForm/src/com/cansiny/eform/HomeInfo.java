/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 * 
 * HomeActivity - The application entry
 *
 * Authors:
 *   Xiaohu <xiaohu417@gmail.com>, 2013.6.21, hefei
 */
package com.cansiny.eform;

import java.util.ArrayList;

public final class HomeInfo
{
    /* Constants for customer id */
    static final public int CUSTOMER_CIB = 1;
    static final public int CUSTOMER_CCB = 2;

    /* change this to match current customer */
    static final public int CUSTOMER_CURRENT = CUSTOMER_CIB;
	
    static private HomeInfo single_home_info = null;
    static HomeInfo getHomeInfo() {
	if (single_home_info == null) {
	    single_home_info = new HomeInfo();
	}
	return single_home_info;
    }

    public int logo;
    public int background;
    public String flash;
    public ArrayList<HomeItem> items;
    public int item_columns;
    public int item_image_size;

    public HomeInfo() {
	this.items = new ArrayList<HomeItem>();
	item_columns = 5;
	item_image_size = 96;
    }

    private void setCustomerCIB() {
	logo = R.drawable.logo_cib;
	background = R.drawable.background_cib;
	flash = "cib.fls";

	items.clear();
	items.add(new HomeItem("FormCIB01", R.drawable.cib01, R.string.form_label_cib01, 20));
	items.add(new HomeItem("FormCIB02", R.drawable.cib02, R.string.form_label_cib02));
	items.add(new HomeItem("FormCIB02", R.drawable.cib02, R.string.form_label_cib03));
	items.add(new HomeItem("FormCIB02", R.drawable.cib02, R.string.form_label_cib04, 20));
    }

    private void setCustomerCCB() {
	logo = R.drawable.logo_cib;
	flash = "ccb.fls";
    }

    public void setCustomer(int customer_id) {
	switch (customer_id) {
	case CUSTOMER_CIB:
	    setCustomerCIB();
	    break;
	case CUSTOMER_CCB:
	    setCustomerCCB();
	    break;
	default:
	    LogActivity.writeLog("Customer id %d unknown.", customer_id);
	    break;
	}
    }

	
    public class HomeItem
    {
	public String klass;
	public int image;
	public int label;
	public int label_size;
		
	public HomeItem(String klass, int image, int label) {
	    this.klass = EFormApplication.getContext().getPackageName() + "." + klass;
	    this.image = image;
	    this.label = label;
	    this.label_size = 22;
	}
		
	public HomeItem(String klass, int image, int label, int label_size) {
	    this.klass = EFormApplication.getContext().getPackageName() + "." + klass;
	    this.image = image;
	    this.label = label;
	    this.label_size = label_size;
	}
    }
	
}
