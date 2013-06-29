package com.cansiny.eform;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ScrollView;

public abstract class Form
{
	private Activity activity;
	public ArrayList<FormPage> pages;
	
	public Form(Activity activity) {
		this.activity = activity;
		pages = new ArrayList<FormPage>();
	}

	protected void addFormPage(String title, int xml_resid) {
		FormPage page = new FormPage();

		page.title = title;

		ScrollView scroll = new ScrollView(activity.getApplicationContext());
		LayoutInflater inflater = activity.getLayoutInflater();
		page.layout = inflater.inflate(xml_resid, scroll);;

		pages.add(page);
	}

	protected void addFormPage(int title_resid, int xml_resid) {
		String title = activity.getResources().getString(title_resid);
		addFormPage(title, xml_resid);
	}

	
	public class FormPage
	{
		String title;
		View layout;
	}
}
