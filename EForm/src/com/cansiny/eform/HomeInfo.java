/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.content.res.AssetManager;

public final class HomeInfo
{
    static final public int CUSTOMER_CIB = 1;
    static final public int CUSTOMER_CCB = 2;

    static final public int CUSTOMER_CURRENT = CUSTOMER_CIB;


    static private HomeInfo single_home_info = null;
    static HomeInfo getHomeInfo() {
	if (single_home_info == null) {
	    single_home_info = new HomeInfo();
	}
	return single_home_info;
    }


    public int background;
    public String banner_url;
    public int item_columns;
    public int item_image_size;
    public ArrayList<HomeItem> items;
    public ArrayList<String> slogans;

    private HomeInfo() {
	items = new ArrayList<HomeItem>();
	slogans = new ArrayList<String>();

	background = R.drawable.dark;
	item_columns = 6;
	item_image_size = 88;

	slogans.add("欢迎使用电子填单系统");
    }


    private String getBannerURL() {
	return null;
    }

    private void getSlogans() {
	switch (CUSTOMER_CURRENT) {
	case CUSTOMER_CIB:
	    slogans.add("客服热线：95561");
	    slogans.add("贵宾专线：400 889 5561");
	    slogans.add("境外客服热线：86-21-3876 9999");
	    slogans.add("境外信用卡白金专线：86-21-3842 9696");
	    slogans.add("网上银行：http://www.cib.com.cn");
	    slogans.add("手机银行：http://wap.cib.com.cn");
	    break;
	case CUSTOMER_CCB:
	    break;
	}
    }

    private void loadCIBRes() {
	banner_url = getBannerURL();
	if (banner_url == null) {
	    try {
		AssetManager assets = EFormApplication.getContext().getAssets();
		InputStream stream = assets.open("cib/banner.html");
		stream.close();
		banner_url = "file:///android_asset/cib/banner.html";
	    } catch (IOException e) {
		LogActivity.writeLog(e);
		banner_url = null;
	    }
	}
	getSlogans();

	items.clear();
	items.add(new HomeItem("FormCIB01", R.drawable.cib01, R.string.form_label_cib01, 21));
	items.add(new HomeItem("FormCIB02", R.drawable.cib02, R.string.form_label_cib02));
	items.add(new HomeItem("FormCIB02", R.drawable.cib02, R.string.form_label_cib04, 21));
	items.add(new HomeItem("FormCIB02", R.drawable.cib02, R.string.form_label_cib03));
	items.add(new HomeItem("FormCIB01", R.drawable.cib01, R.string.form_label_cib01, 21));
	items.add(new HomeItem("FormCIB02", R.drawable.cib02, R.string.form_label_cib02));
	items.add(new HomeItem("FormCIB02", R.drawable.cib02, R.string.form_label_cib04, 21));
	items.add(new HomeItem("FormCIB02", R.drawable.cib02, R.string.form_label_cib03));
	items.add(new HomeItem("FormCIB01", R.drawable.cib01, R.string.form_label_cib01, 21));
	items.add(new HomeItem("FormCIB02", R.drawable.cib02, R.string.form_label_cib02));
	items.add(new HomeItem("FormCIB02", R.drawable.cib02, R.string.form_label_cib04, 21));
	items.add(new HomeItem("FormCIB02", R.drawable.cib02, R.string.form_label_cib03));
	items.add(new HomeItem("FormCIB01", R.drawable.cib01, R.string.form_label_cib01, 21));
	items.add(new HomeItem("FormCIB01", R.drawable.cib01, R.string.form_label_cib01, 21));
	items.add(new HomeItem("FormCIB02", R.drawable.cib02, R.string.form_label_cib02));
	items.add(new HomeItem("FormCIB02", R.drawable.cib02, R.string.form_label_cib04, 21));
	items.add(new HomeItem("FormCIB02", R.drawable.cib02, R.string.form_label_cib03));
    }

    private void loadCCBRes() {
	banner_url = "file:///android_asset/cib/banner.html";
    }

    
    public void loadCustomerRes() {
	switch (CUSTOMER_CURRENT) {
	case CUSTOMER_CIB: loadCIBRes(); break;
	case CUSTOMER_CCB: loadCCBRes(); break;
	default:
	    LogActivity.writeLog("Customer id %d unknown.", CUSTOMER_CURRENT);
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
	    this.label_size = 21;
	}
		
	public HomeItem(String klass, int image, int label, int label_size) {
	    this.klass = EFormApplication.getContext().getPackageName() + "." + klass;
	    this.image = image;
	    this.label = label;
	    this.label_size = label_size;
	}
    }
}
