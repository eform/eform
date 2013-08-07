/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;


public abstract class Form
    implements OnClickListener, OnFocusChangeListener, OnTouchListener
{
    protected Activity activity;
    protected ArrayList<FormPage> pages;
    protected int active_page;
    protected ArrayList<Integer> cardno_views;
    protected ArrayList<Integer> verify_views;
    private FormListener listener;

    public Form(Activity activity) {
	this.activity = activity;
	listener = null;
	pages = new ArrayList<FormPage>();
	active_page = -1;
	cardno_views = new ArrayList<Integer>();
	verify_views = new ArrayList<Integer>();
    }

    /* set event listener */
    public void setListener(FormListener listener) {
	this.listener = listener;
    }

    /* convenient method to find view and cast to corresponding type */
    protected View findView(int id) {
	return activity.findViewById(id);
    }
    protected EditText findEditText(int id) {
	return (EditText) activity.findViewById(id);
    }
    protected TextView findTextView(int id) {
	return (TextView) activity.findViewById(id);
    }
    protected CheckBox findCheckBox(int id) {
	return (CheckBox) activity.findViewById(id);
    }
    protected LinearLayout findLinearLayout(int id) {
	return (LinearLayout) activity.findViewById(id);
    }

    /* check off sibling in same layout */
    protected void checkOffSibling(CheckBox checkbox) {
	if (checkbox.isChecked()) {
	    ViewGroup parent = (ViewGroup) checkbox.getParent();
	    for (int i = 0; i < parent.getChildCount(); i++) {
		Object child = parent.getChildAt(i);
		if (child instanceof CheckBox && child != checkbox) {
		    ((CheckBox) child).setChecked(false);
		}
	    }
	}
    }


    protected void swipeMagcard(TextView textview) {
	MagcardReader reader = new MagcardReader();
	reader.readCardno(activity, textview);
    }

    protected void readIdCard() {
	IDCardReader reader = new IDCardReader();
	reader.show(activity.getFragmentManager(), "ReadIdcardDialog");
    }


    public int getActivePage() {
	return active_page;
    }

    public FormPage getActiveFormPage() {
	return pages.get(active_page);
    }

    public View setActivePage(int page_no) {
	if (page_no < 0 || page_no >= pages.size())
	    return null;
	if (page_no == active_page)
	    return null;

	/* store current page state and load new page state. */
	storePage(active_page);
	active_page = page_no;
	return loadPage(active_page);
    }

    private void storePage(int page_no) {
	if (page_no < 0 || page_no >= pages.size())
	    return;

	FormPage page = pages.get(active_page);
	page.store();

	/* save card no tag */
	if (page.scroll_view != null) {
	    for (Integer viewid : cardno_views) {
		View view = page.scroll_view.findViewById(viewid.intValue());
		if (view == null)
		    continue;
		Object tag = view.getTag();
		if (tag != null && ((CharSequence) tag).length() > 0) {
		    String key = viewid.toString();
		    if (page.bundle.containsKey(key)) {
			page.bundle.putCharSequence(key + "-tag", (CharSequence) tag);
		    }
		}
	    }
	}
    }

    private ScrollView loadPage(int page_no) {
	if (page_no < 0 || page_no >= pages.size())
	    return null;

	FormPage page = pages.get(page_no);

	ScrollView scroll_view = page.load();

	/* listen all views event of new page */
	addViewsListener(scroll_view);
		
	/* restore card no tag */
	for (Integer viewid : cardno_views) {
	    View view = page.scroll_view.findViewById(viewid.intValue());
	    if (view != null) {
		String key = viewid.toString() + "-tag";
		if (page.bundle.containsKey(key)) {
		    view.setTag(page.bundle.getCharSequence(key));
		}
	    }
	}
	return scroll_view;
    }

    public String getPagesContents() {
	return "hello中文";
    }

    /* call by activity when page has been insert into layout */
    public void onPageStart(int page_no) {
	if (page_no < 0 || page_no >= pages.size())
	    return;
		
	FormPage page = pages.get(page_no);
		
	/* restore view focus */
	int focus_id = page.bundle.getInt("focus-view");
	View view = page.scroll_view.findViewById(focus_id);
	if (view != null)
	    view.requestFocus();
    }

	
    /* traversing the layout tree to set view's listener. */
    private void addViewsListener(Object object) {
	View view = (View) object;
	view.setClickable(true);
	view.setOnClickListener(this);
	view.setOnTouchListener(this);

	if (object instanceof ViewGroup) {
	    ViewGroup group = (ViewGroup) object;
	    for (int i = 0; i < group.getChildCount(); i++) {
		addViewsListener(group.getChildAt(i));
	    }
	    return;
	}
		
	if (object instanceof View) {
	    view.setOnFocusChangeListener(this);
	}
		
	if (object instanceof TextView) {
	    TextView text_view = (TextView) object;
	    GenericTextWatcher wathcer = new GenericTextWatcher(text_view);
	    text_view.addTextChangedListener(wathcer);
	}
    }

    public boolean scrollUp() {
	FormPage page = getActiveFormPage();
	if (page != null && !page.canScrollUp()) {
	    page.scrollUp();
	    return true;
	}
	return false;
    }

    public boolean scrollDown() {
	FormPage page = getActiveFormPage();
	if (page != null && !page.canScrollDown()) {
	    page.scrollDown();
	    return true;
	}
	return false;
    }

    @Override
    public void onClick(View view) {
	/* format card no if its view lost focus */
	for (Integer viewid : cardno_views) {
	    if (view.getId() == viewid.intValue())
		continue;
			
	    TextView textview = findTextView(viewid.intValue());
	    if (textview != null) {
		CharSequence cardno = (CharSequence) textview.getTag();
		if (cardno != null)
		    textview.setText(MagcardReader.formatCardno(cardno , 4, 3));
	    }
	}

	/* clear edit text if clear button clicked*/
	if (view.getClass() == Button.class) {
	    Object tag = view.getTag();
	    if (tag != null && tag instanceof String) {
		if (((String) tag).equals("edittext_clear_button")) {
		    ViewGroup parent = (ViewGroup) view.getParent();
		    int index = parent.indexOfChild(view);
		    if (index > 0) {
			EditText edit_text = (EditText) parent.getChildAt(index - 1);
			if (edit_text instanceof EditText) {
			    edit_text.setText("");
			}
		    }
		}
	    }
	}
		
	/* hide the soft keyboard when click non-edit text widget */
	if (!(view instanceof EditText)) {
            InputMethodManager imm = 
		(InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
	}
    }


    @Override
    public boolean onTouch(View view, MotionEvent event) {
	if (event.getAction() == MotionEvent.ACTION_DOWN) {
	    if (listener != null) {
		listener.onFormViewTouched(this, view);
	    }
	}
	return false;
    }

    @Override
    public void onFocusChange(View view, boolean hasfocus) {
	/* switch card no display format */
	for (Integer viewid : cardno_views) {
	    if (view.getId() == viewid.intValue()) {
		TextView textview = findTextView(viewid.intValue());
		CharSequence cardno = (CharSequence) textview.getTag();
		if (cardno != null) {
		    if (hasfocus)
			textview.setText(cardno);
		    else
			textview.setText(MagcardReader.formatCardno(cardno , 4, 3));
		}
		break;
	    }
	}
    }

    /* callback for edit text listener */
    protected void beforeTextChanged(TextView textview, CharSequence sequence,
				     int start, int count, int after) {
	if (listener != null) {
	    listener.onFormTextChanged(this, (EditText) textview);
	}
    }
	
    /* callback for edit text listener */
    protected void onTextChanged(TextView textview, CharSequence sequence,
				 int start, int before, int count) {
	/* record real card no to tag, and display card no with formatter */
	for (Integer viewid : cardno_views) {
	    if (textview.getId() == viewid.intValue()) {
		if (sequence.toString().matches("[0-9]*"))
		    textview.setTag(sequence);
	    }
	}
    }

    /* callback for edit text listener */
    protected void afterTextChanged(TextView textview, Editable editable) {
	/* clear warning image for verify edit text */
	for (Integer viewid : verify_views) {
	    if (textview.getId() == viewid.intValue()) {
		if (editable.length() > 0) {
		    removeWarningImage(textview);
		}
	    }
	}
    }


    private class GenericTextWatcher implements TextWatcher
    {
	private TextView textview;
	private GenericTextWatcher(TextView view) {
	    textview = view;
	}
	@Override
	public void afterTextChanged(Editable editable) {
	    Form.this.afterTextChanged(textview, editable);
	}
	@Override
	public void beforeTextChanged(CharSequence sequence,
				      int start, int count, int after) {
	    Form.this.beforeTextChanged(textview, sequence, start, count, after);
	}
	@Override
	public void onTextChanged(CharSequence sequence,
				  int start, int before, int count) {
	    Form.this.onTextChanged(textview, sequence, start, before, count);
	}
    }

	
    protected ImageView createWarningImage(CharSequence description) {
	ImageView image = new ImageView(activity.getApplicationContext());
	image.setTag("verify-warning-image");
	image.setContentDescription(description);
	image.setImageResource(R.drawable.warning);
	image.setBackgroundResource(R.color.transparent);
	image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
	image.setAdjustViewBounds(true);
	image.setMaxHeight((int) Utils.convertDpToPixel(28));
	image.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View image) {
		    CharSequence desc = image.getContentDescription();
		    if (desc.length() > 0)
			Utils.showToast(desc, R.drawable.warning, Utils.IMAGE_SIZE_SMALL);
		}
	    });
	return image;
    }

    protected void insertWarningImage(View slibing, CharSequence description) {
	if (slibing.getParent() == null)
	    return;

	ViewGroup parent = (ViewGroup) slibing.getParent();

	int index = parent.indexOfChild(slibing);
	/* skip if warning image already exists */
	if (index > 0) {
	    Object tag = parent.getChildAt(index - 1).getTag();
	    if (tag instanceof String && tag.equals("verify-warning-image"))
		return;
	}

	ImageView image = createWarningImage(description);
	if (parent.getClass() == LinearLayout.class) {
	    LinearLayout.LayoutParams params =
		new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					      ViewGroup.LayoutParams.WRAP_CONTENT);
	    params.gravity = Gravity.CENTER_VERTICAL;
	    parent.addView(image, parent.indexOfChild(slibing), params);
	} else if (parent.getClass() == TableRow.class) {
	    TableRow.LayoutParams params =
		new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					  ViewGroup.LayoutParams.WRAP_CONTENT);
	    params.gravity = Gravity.CENTER_VERTICAL;
	    parent.addView(image, parent.indexOfChild(slibing), params);
	} else {
	    parent.addView(image, parent.indexOfChild(slibing));
	}
    }

    protected void removeWarningImage(View slibing) {
	ViewGroup parent = (ViewGroup) slibing.getParent();
	int index = parent.indexOfChild(slibing);
	if (index > 0) {
	    Object tag = parent.getChildAt(index - 1).getTag();
	    if (tag != null && tag.equals("verify-warning-image")) {
		parent.removeViewAt(index - 1);
	    }
	}
    }

    protected void clearWarningImage() {
	FormPage page = pages.get(active_page);
	if (page == null || page.scroll_view == null)
	    return;

	while (true) {
	    View view = page.scroll_view.findViewWithTag("verify-warning-image");
	    if (view == null)
		break;
	    ViewGroup parent = (ViewGroup) view.getParent();
	    parent.removeView(view);
	}
    }


    public int verify() {
	int retval = 0;
	View focus_view = null;

	/* clear all warning images first */
	clearWarningImage();

	for (Integer viewid : verify_views) {
	    View view = findView(viewid.intValue());
	    if (view == null)
		continue;
			
	    /* check for edit text */
	    if ((Object) view instanceof EditText) {
		if (((EditText) view).getText().length() == 0) {
		    insertWarningImage(view, "此项不能为空");
		    retval++;

		    if (focus_view == null)
			focus_view = view;
		}
	    }
	}
		
	/* let first problem view get focus */
	if (focus_view != null) {
	    focus_view.requestFocus();
	}
	/* return problem count */
	return retval;
    }
	
	
    public boolean print() {
	return true;
    }


    public interface FormListener
    {
	public void onFormPageScrolled(Form form, ScrollView scroll_view);
	public void onFormViewTouched(Form form, View view);
	public void onFormTextChanged(Form form, EditText textview);
    }


    interface FormScrollViewListener
    {
	public void onScollChanged(ScrollView scroll_view);
    }

    class FormScrollView extends ScrollView
    {
        private FormScrollViewListener listener;

        public FormScrollView(Context context) {
            super(context);
            listener = null;
            setSmoothScrollingEnabled(true);
        }
        public void setScrollListener(FormScrollViewListener listener) {
            this.listener = listener;
        }
        @Override
        protected void onScrollChanged(int l, int t, int oldl, int oldt) {
            super.onScrollChanged(l, t, oldl, oldt);
            if (listener != null) {
        	listener.onScollChanged(this);
            }
        }
    }


    public class FormPage implements FormScrollViewListener
    {
	public String title;
	public int layout;
	public FormScrollView scroll_view;
	public Bundle bundle;

	private void initialize(String title, int layout) {
	    this.title = title;
	    this.layout = layout;
	    scroll_view = null;
	    bundle = new Bundle();
	}

	public FormPage(String title, int layout) {
	    initialize(title, layout);
	}
	public FormPage(int title_id, int layout) {
	    String title = activity.getResources().getString(title_id);
	    initialize(title, layout);
	}

	public ScrollView load() {
	    FormScrollView scroll = new FormScrollView(activity);
	    LayoutInflater inflater = activity.getLayoutInflater();
	    scroll_view = (FormScrollView) inflater.inflate(layout, scroll);
	    scroll_view.setScrollListener(this);
	    loadViews(scroll_view);
	    return scroll_view;
	}

	private void loadViews(Object object) {
	    if (object instanceof ViewGroup) {
		ViewGroup group = (ViewGroup) object;
		for (int i = 0; i < group.getChildCount(); i++) {
		    loadViews(group.getChildAt(i));
		}
		return;
	    }

	    /* set edittext ime options */
	    if (object instanceof EditText) {
		((EditText) object).setImeOptions(EditorInfo.IME_ACTION_NEXT);
	    }

	    /* restore widgets value */
	    String key = Integer.valueOf(((View) object).getId()).toString();
	    if (bundle.containsKey(key)) {
		if (object instanceof EditText) {
		    ((EditText) object).setText(bundle.getCharSequence(key));
		} else if (object instanceof CheckBox) {
		    ((CheckBox) object).setChecked(true);
		}
	    }
	}


	public void store() {
	    if (scroll_view != null) {
		storeViews(scroll_view);

		View view = scroll_view.findFocus();
		if (view != null)
		    bundle.putInt("focus-view", view.getId());
		else
		    bundle.remove("focus-view");
	    }
	}

	private void storeViews(Object object) {
	    if (object instanceof ViewGroup) {
		ViewGroup group = (ViewGroup) object;
		for (int i = 0; i < group.getChildCount(); i++) {
		    storeViews(group.getChildAt(i));
		}
		return;
	    }
			
	    String key = Integer.valueOf(((View) object).getId()).toString();
	    bundle.remove(key);

	    if (object instanceof EditText) {
		EditText edit_text = (EditText) object;
		Editable text = edit_text.getText();
		if (text.length() > 0) {
		    bundle.putCharSequence(key, text);
		}
	    } else if (object instanceof CheckBox) {
		CheckBox checkbox = (CheckBox) object;
		if (checkbox.isChecked())
		    bundle.putBoolean(key, true);
	    }
	}


	public boolean canScrollUp() {
	    if (scroll_view == null)
		return false;
	    return scroll_view.getScrollY() == 0 ? false : true;
	}
	public boolean canScrollDown() {
	    if (scroll_view == null || scroll_view.getChildCount() == 0)
		return false;

	    if (scroll_view.getBottom() == 0)
		return false;

	    View child = scroll_view.getChildAt(scroll_view.getChildCount() - 1);
	    return child.getBottom() > scroll_view.getScrollY() + scroll_view.getBottom();
	}

	public void scrollUp() {
	    if (scroll_view == null || !((Object) scroll_view instanceof ScrollView))
		return;
	    scroll_view.smoothScrollBy(0, -100);
	}
	public void scrollDown() {
	    if (scroll_view == null || !((Object) scroll_view instanceof ScrollView))
		return;
	    scroll_view.smoothScrollBy(0, 100);
	}

	@Override
	public void onScollChanged(ScrollView scroll_view) {
	    if (listener != null)
		listener.onFormPageScrolled(Form.this, scroll_view);
	}
    }

}
