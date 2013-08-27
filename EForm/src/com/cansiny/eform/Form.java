/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.cansiny.eform.Utils.Device;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.text.Editable;
import android.util.Xml;
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


public abstract class Form extends DefaultHandler
    implements OnClickListener, OnFocusChangeListener, OnTouchListener,
    Utils.GenericTextWatcher.TextWatcherListener
{
    static final private String TAG_KEY_WARNING = "Warning";
    static final private String TAG_CLEAR_BUTTON = "edittext_clear_button";

    protected Activity activity;
    protected String label;
    protected ArrayList<FormPage> pages;
    protected int active_page;
    protected ArrayList<Integer> cardno_edittexts;
    protected ArrayList<Integer> verify_edittexts;
    private FormListener listener;
    protected Button last_idcard_button;

    public Form(Activity activity, String label) {
	this.activity = activity;
	listener = null;
	this.label = label;
	pages = new ArrayList<FormPage>();
	active_page = -1;
	cardno_edittexts = new ArrayList<Integer>();
	verify_edittexts = new ArrayList<Integer>();
    }

    /* set event listener */
    public void setListener(FormListener listener) {
	this.listener = listener;
    }

    public Activity getActivity() {
	return activity;
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

    protected void findViewWithKeyTag(List<View> views, View view, String tag) {
	if (view instanceof ViewGroup) {
	    ViewGroup group = (ViewGroup) view;
	    for (int i = 0; i < group.getChildCount(); i++) {
		findViewWithKeyTag(views, group.getChildAt(i), tag);
	    }
	    return;
	}

	Object object = view.getTag(R.id.FormWarningViewTagKey);
	if (object == null)
	    return;

	if (object.equals(tag)) {
	    views.add(view);
	}
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
	Device magcard = Device.getDevice(Device.DEVICE_MAGCARD);
	if (magcard == null) {
	    Utils.showToast("不能刷卡，请联系管理员检查设备配置", R.drawable.cry);
	    return;
	}

	magcard.setClientData(textview);
	magcard.setListener(new Device.DeviceListener() {
	    @Override
	    public void onDeviceTaskSuccessed(Device device, Object result) {
		TextView textview = (TextView) device.getClientData();
		textview.setText((CharSequence) result);
	    }
	    @Override
	    public void onDeviceTaskStart(Device device) {
	    }
	    @Override
	    public void onDeviceTaskFailed(Device device) {
	    }
	    @Override
	    public void onDeviceTaskCancelled(Device device) {
	    }
	});
	magcard.startTask(activity.getFragmentManager(), Device.TASK_FLAG_MAGCARD_FORM);
    }

    protected void readIdCard(Button button) {
	Device idcard = Device.getDevice(Device.DEVICE_IDCARD);
	if (idcard == null) {
	    Utils.showToast("不能读身份证信息，请联系管理员检查设备配置", R.drawable.cry);
	    return;
	}

	idcard.setClientData(button);
	idcard.setListener(new Device.DeviceListener() {
	    @Override
	    public void onDeviceTaskSuccessed(Device device, Object result) {
		Button button = (Button) device.getClientData();
		onIDCardResponse(button, (IDCard.IDCardInfo) result);
	    }
	    @Override
	    public void onDeviceTaskStart(Device device) {
	    }
	    @Override
	    public void onDeviceTaskFailed(Device device) {
	    }
	    @Override
	    public void onDeviceTaskCancelled(Device device) {
	    }
	});
	idcard.startTask(activity.getFragmentManager(), Device.TASK_FLAG_IDCARD_FORM);
    }

    abstract void onIDCardResponse(Button button, IDCard.IDCardInfo info);

    public int getPageCount() {
	return pages.size();
    }

    public FormPage getPage(int page_no) {
	return pages.get(page_no);
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
	savePage(active_page);
	active_page = page_no;
	return loadPage(active_page);
    }

    private void savePage(int page_no) {
	if (page_no < 0 || page_no >= pages.size())
	    return;

	FormPage page = pages.get(page_no);
	page.saveToBundle();

	for (Integer viewid : cardno_edittexts) {
	    View view = page.findViewById(viewid.intValue());
	    if (view != null) {
		Object cardno = view.getTag(R.id.FormCardnoViewTagKey);
		if (cardno != null && ((CharSequence) cardno).length() > 0) {
		    String key = viewid.toString();
		    page.bundle.putString(key, cardno.toString());
		}
	    }
	}
    }

    private FormPage loadPage(int page_no) {
	if (page_no < 0 || page_no >= pages.size())
	    return null;

	FormPage page = pages.get(page_no);
	page.loadFromBundle();

	setListenerForViews(page);
		
	for (Integer viewid : cardno_edittexts) {
	    View view = page.findViewById(viewid.intValue());
	    if (view != null) {
		String key = viewid.toString();
		if (page.bundle.containsKey(key)) {
		    String cardno = page.bundle.getString(key);
		    view.setTag(R.id.FormCardnoViewTagKey, cardno);
		    if (!view.hasFocus()) {
			((TextView) view).setText(Magcard.formatCardno(cardno));
		    }
		}
	    }
	}
	return page;
    }

    public String getPagesContents() {
	StringBuilder builder = new StringBuilder();

	builder.append("<form ");
	builder.append("class=\"" + getClass().getName() + "\" ");
	builder.append(">\n");

	for (int i = 0; i < pages.size(); i++) {
	    savePage(i);
	    FormPage page = pages.get(i);
	    builder.append(String.format("  <page index='%d'>\n", i));
	    builder.append(page.toXmlString());
	    builder.append("  </page>\n");
	}
	builder.append("</form>\n");

	return builder.toString();
    }

    public void setPagesContents(String xmlstring) {
	if (xmlstring == null || pages == null)
	    return;

	try {
	    Xml.parse(xmlstring, new XmlContentsHandler());
	} catch (SAXException e) {
	    LogActivity.writeLog(e);
	}
    }

    private class XmlContentsHandler extends DefaultHandler
    {
	private int level = 0;
	FormPage page = null;

	@Override
	public void startElement(String uri, String localName,
		String qName, Attributes attrs) throws SAXException {
	    if (level == 0 && !localName.equals("form")) {
		throw new SAXException("解析页面内容失败，根结点名称必须是'form'");
	    }
	    level++;

	    if (localName.equals("form")) {
		if (level != 1) {
		    throw new SAXException("解析页面内容失败，元素'form'必须是根结点");
		}
		return;
	    }

	    if (localName.equals("page")) {
		if (level != 2) {
		    throw new SAXException("解析页面内容失败，元素'page'必须二级结点");
		}
		int page_no = Integer.parseInt(attrs.getValue("", "index"));
		if (page_no < 0 || page_no >= pages.size()) {
		    throw new SAXException("解析页面内容失败，页面索引" + page_no
			    + "超出范围" + pages.size());
		}
		page = pages.get(page_no);
		return;
	    }

	    if (localName.equals("view")) {
		if (level != 3) {
		    throw new SAXException("解析页面内容失败，元素'view'必须三级结点");
		}
		if (page == null) {
		    throw new SAXException("解析页面内容失败，'view'元素必须是'page'元素的子元素");
		}
		putViewValue(attrs);
		return;
	    }

	    LogActivity.writeLog("不可识别的元素 %s，暂时忽略", localName);
	}

	public void endElement(String uri, String localName, String qName) {
	    level--;

	    if (localName.equals("page")) {
		if (page != null && page.bundle.size() > 0) {
		    page.loadFromBundle();
		}
		page = null;
	    }
	}

	private void putViewValue(Attributes attrs) throws SAXException {
	    String resid = attrs.getValue("", "resid");
	    if (resid == null) {
		throw new SAXException("元素'view'缺少'resid'属性");
	    }

	    String vclass  = attrs.getValue("", "vclass");
	    if (vclass == null) {
		throw new SAXException("元素'view'缺少'vclass'属性");
	    }

	    String value   = attrs.getValue("", "value");
	    if (value == null) {
		throw new SAXException("元素'view'缺少'value'属性");
	    }

	    if (page != null) {
		page.putValue(resid, vclass, value);
	    }
	}
    }

    public String toPrintTemplate() {
	StringBuilder builder = new StringBuilder();

	builder.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n");
	builder.append("<!--\n");
	builder.append("本文件是通过程序生成的模板，请根据需要进行修改\n");
	builder.append("Copyright © 2013 Cansiny Trade Co.,Ltd.\n");
	builder.append("-->\n");

	builder.append("<form ");
	builder.append("class=\"" + getClass().getName() + "\" ");
	builder.append("label=\"" + label.replace("\n", "") + "\" ");
	builder.append(">\n");

	for (int i = 0; i < pages.size(); i++) {
	    FormPage page = pages.get(i);
	    builder.append("  <page ");
	    builder.append("index=\"" + i + "\" ");
	    builder.append(">\n");
	    builder.append(page.toPrintTemplate());
	    builder.append("  </page>\n");
	}
	builder.append("</form>\n");
	return builder.toString();
    }

    /* call by activity when page has been insert into layout */
    public void onPageStart(int page_no) {
	if (page_no < 0 || page_no >= pages.size())
	    return;

	FormPage page = pages.get(page_no);
	page.restoreFocusView();
    }

	
    /* traversing the layout tree to set view's listener. */
    private void setListenerForViews(Object object) {
	View view = (View) object;
	view.setClickable(true);
	view.setOnClickListener(this);
	view.setOnTouchListener(this);

	if (object instanceof ViewGroup) {
	    ViewGroup group = (ViewGroup) object;
	    for (int i = 0; i < group.getChildCount(); i++) {
		setListenerForViews(group.getChildAt(i));
	    }
	    return;
	}

	if (object instanceof View) {
	    view.setOnFocusChangeListener(this);
	}

	if (object instanceof TextView) {
	    TextView textview = (TextView) object;
	    Utils.GenericTextWatcher wathcer = new Utils.GenericTextWatcher(textview, this);
	    textview.addTextChangedListener(wathcer);
	}
    }

    public void scrollPageUp() {
	FormPage page = getActiveFormPage();
	if (page != null)
	    page.scrollUp();
    }

    public void scrollPageDown() {
	FormPage page = getActiveFormPage();
	if (page != null)
	    page.scrollDown();
    }

    @Override
    public void onClick(View view) {
	for (Integer viewid : cardno_edittexts) {
	    if (view.getId() == viewid.intValue()) {
		TextView textview = (TextView) view;
		Object cardno = textview.getTag(R.id.FormCardnoViewTagKey);
		if (cardno != null) {
		    textview.setText((CharSequence) cardno);
		}
	    } else {
		TextView textview = findTextView(viewid.intValue());
		if (textview != null) {
		    Object cardno = textview.getTag(R.id.FormCardnoViewTagKey);
		    if (cardno != null) {
			textview.setText(Magcard.formatCardno((CharSequence) cardno));
		    }
		}
	    }
	}

	/* clear edit text if clear button clicked*/
	if (view.getClass() == Button.class) {
	    Object tag = view.getTag();
	    if (tag != null && tag instanceof String) {
		if (((String) tag).equals(TAG_CLEAR_BUTTON)) {
		    ViewGroup parent = (ViewGroup) view.getParent();
		    int index = parent.indexOfChild(view);
		    if (index > 0) {
			EditText edittext = (EditText) parent.getChildAt(index - 1);
			if (edittext instanceof EditText) {
			    edittext.setText("");
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
	for (Integer viewid : cardno_edittexts) {
	    if (view.getId() == viewid.intValue()) {
		TextView textview = (TextView) view;
		Object cardno = textview.getTag(R.id.FormCardnoViewTagKey);
		if (cardno != null) {
		    if (hasfocus)
			textview.setText((CharSequence) cardno);
		    else
			textview.setText(Magcard.formatCardno((CharSequence) cardno));
		}
		break;
	    }
	}
    }

    @Override
    public void beforeTextChanged(TextView textview, CharSequence sequence,
				     int start, int count, int after) {
	if (listener != null) {
	    listener.onFormTextChanged(this, (EditText) textview);
	}
    }

    @Override
    public void onTextChanged(TextView textview, CharSequence sequence,
				 int start, int before, int count) {
	for (Integer viewid : cardno_edittexts) {
	    if (textview.getId() == viewid.intValue()) {
		if (sequence.toString().matches("[0-9]*")) {
		    textview.setTag(R.id.FormCardnoViewTagKey, sequence);
		}
	    }
	}
    }

    @Override
    public void afterTextChanged(TextView textview, Editable editable) {
	for (Integer viewid : verify_edittexts) {
	    if (textview.getId() == viewid.intValue()) {
		if (editable.length() > 0) {
		    removeWarningImage(textview);
		}
	    }
	}
    }

    protected ImageView createWarningImage(CharSequence description) {
	ImageView image = new ImageView(activity.getApplicationContext());
	image.setTag(R.id.FormWarningViewTagKey, TAG_KEY_WARNING);
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
	if (index > 0) {
	    Object tag = parent.getChildAt(index - 1).getTag(R.id.FormWarningViewTagKey);
	    if (tag != null && tag.equals(TAG_KEY_WARNING))
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
	    Object tag = parent.getChildAt(index - 1).getTag(R.id.FormWarningViewTagKey);
	    if (tag != null && tag.equals(TAG_KEY_WARNING)) {
		parent.removeViewAt(index - 1);
	    }
	}
    }

    protected void clearWarningImage() {
	FormPage page = pages.get(active_page);
	if (page == null)
	    return;

	List<View> views = new ArrayList<View>();
	findViewWithKeyTag(views, page, TAG_KEY_WARNING);
	for (View view : views) {
	    ViewGroup parent = (ViewGroup) view.getParent();
	    parent.removeView(view);
	}
    }


    public int verify(boolean verify_all, boolean markit) {
	clearWarningImage();

	int retval = 0;
	View focus_view = null;

	for (Integer viewid : verify_edittexts) {
	    View view = null;

	    if (verify_all) {
		for (FormPage page : pages) {
		    view = page.findViewById(viewid);
		    if (view != null)
			break;
		}
	    } else {
		view = findView(viewid.intValue());
	    }

	    if (view != null && (Object) view instanceof EditText) {
		if (((EditText) view).getText().length() == 0) {
		    retval++;

		    if (markit) {
			insertWarningImage(view, "此项不能为空");
		    }
		    if (focus_view == null) {
			focus_view = view;
		    }
		}
	    }
	}

	if (focus_view != null) {
	    focus_view.requestFocus();
	}
	return retval;
    }
	

    public boolean print() {
	return true;
    }

    public void readyPageForPrint(int page_no) {
	savePage(page_no);
    }

    public interface FormListener
    {
	public void onFormPageScrolled(Form form, ScrollView scroll_view);
	public void onFormViewTouched(Form form, View view);
	public void onFormTextChanged(Form form, EditText textview);
    }

    public class FormPage extends ScrollView
    {
	private String title;
	private int layout;
	private Bundle bundle;
	private View focus_view;

	private void initialize(String title, int layout) {
	    this.title = title;
	    this.layout = layout;
	    bundle = new Bundle();
	    focus_view = null;
	}

	private FormPage(Context context) {
	    super(context);
	}

	public FormPage(Context context, String title, int layout) {
	    super(context);
	    initialize(title, layout);
	}

	public FormPage(Context context, int title_id, int layout) {
	    super(context);
	    if (activity != null) {
		String title = activity.getResources().getString(title_id);
		initialize(title, layout);
	    }
	}

	public String getTitle() {
	    return title;
	}

	protected void restoreFocusView() {
	    if (focus_view != null && findViewById(focus_view.getId()) != null) {
		focus_view.requestFocus();
	    }
	}

	protected void loadFromBundle() {
	    if (getChildCount() == 0) {
		LayoutInflater inflater = activity.getLayoutInflater();
		inflater.inflate(this.layout, this);
	    }
	    setViewFromBundle(this);
	}

	private void setViewFromBundle(Object object) {
	    if (object instanceof ViewGroup) {
		ViewGroup group = (ViewGroup) object;
		for (int i = 0; i < group.getChildCount(); i++) {
		    setViewFromBundle(group.getChildAt(i));
		}
		return;
	    }

	    /* set edittext ime options */
	    if (object instanceof EditText) {
		((EditText) object).setImeOptions(EditorInfo.IME_ACTION_NEXT);
	    }

	    String key = String.valueOf(((View) object).getId());

	    if (bundle.containsKey(key)) {
		if (object instanceof EditText) {
		    ((EditText) object).setText(bundle.getString(key));
		} else if (object instanceof CheckBox) {
		    ((CheckBox) object).setChecked(bundle.getBoolean(key));
		}
	    }
	}


	protected void saveToBundle() {
	    focus_view = findFocus();
	    bundle.clear();
	    saveViewToBundle(this);
	}

	private void saveViewToBundle(Object object) {
	    if (object instanceof ViewGroup) {
		ViewGroup group = (ViewGroup) object;
		for (int i = 0; i < group.getChildCount(); i++) {
		    saveViewToBundle(group.getChildAt(i));
		}
		return;
	    }

	    int viewid = ((View) object).getId();
	    String key = String.valueOf(viewid);

	    if (object instanceof EditText) {
		EditText edittext = (EditText) object;
		if (viewid == -1) {
		    LogActivity.writeLog("界面设计错误: EditText没有指定ID，它当前的值为：%s",
			    edittext.getText());
		    return;
		}
		if (edittext.length() > 0) {
		    bundle.putString(key, edittext.getText().toString());
		}
	    } else if (object instanceof CheckBox) {
		CheckBox checkbox = (CheckBox) object;
		if (viewid == -1) {
		    LogActivity.writeLog("界面设计错误: CheckBox没有指定ID，它的名称为：%s",
			    checkbox.getText());
		    return;
		}
		if (checkbox.isChecked()) {
		    bundle.putBoolean(key, checkbox.isChecked());
		}
	    }
	}

	protected String toXmlString() {
	    StringBuilder builder = new StringBuilder();

	    for (String key : bundle.keySet()) {
		Object object = bundle.get(key);
		try {
		    int viewid = Integer.parseInt(key);
		    Resources res = activity.getResources();

		    builder.append("    <view ");
		    builder.append("resid=\"" + res.getResourceName(viewid) + "\" ");
		    String canon_name = object.getClass().getCanonicalName();
		    if (canon_name != null) {
			builder.append("vclass=\"" + canon_name + "\" ");
		    } else {
			builder.append("vclass=\"" + object.getClass().getName() + "\" ");
		    }
		    builder.append("value=\"" + object.toString() + "\" ");
		    builder.append(" />\n");
		} catch (Exception e) {
		    LogActivity.writeLog(e);
		    continue;
		}
	    }
	    return builder.toString();
	}

	public void putValue(String resid, String vclass, String value) {
	    int viewid = activity.getResources().getIdentifier(resid, null, null);
	    if (viewid == 0) {
		LogActivity.writeLog("不能找到ID为'%s'的资源", resid);
		return;
	    }

	    try {
		String key = String.valueOf(viewid);
		Object object = Class.forName(vclass).getConstructor(String.class).newInstance(value);
		if (object.getClass() == Boolean.class) {
		    bundle.putBoolean(key, (Boolean) object);
		} else if (object.getClass() == String.class) {
		    bundle.putString(key, (String) object);
		} else {
		    LogActivity.writeLog("未处理的值类型 %s !!!", object.getClass());
		}
	    } catch (Exception e) {
		LogActivity.writeLog(e);
	    }
	}

	public String toPrintTemplate() {
	    if (getChildCount() == 0) {
		LayoutInflater inflater = activity.getLayoutInflater();
		inflater.inflate(this.layout, this);
	    }
	    StringBuilder builder = new StringBuilder();

	    toPrintElement(builder, this, 1);
	    return builder.toString();
	}

	private void toPrintElement(StringBuilder builder, View object, int order) {
	    if (object instanceof ViewGroup) {
		ViewGroup group = (ViewGroup) object;
		for (int i = 0; i < group.getChildCount(); i++) {
		    toPrintElement(builder, group.getChildAt(i), order + 1);
		}
		return;
	    }

	    int viewid = object.getId();
	    if (viewid == -1) {
		if (object instanceof TextView) {
		    LogActivity.writeLog("%s(%s)没有指定ID，略过", object.getClass().getName(),
			    ((TextView) object).getText());
		} else {
		    LogActivity.writeLog("%s没有指定ID，略过", object.getClass().getName());
		}
		return;
	    }

	    builder.append("    <field ");
	    Resources res = activity.getResources();
	    builder.append("resid=\"" + res.getResourceName(viewid) + "\" ");
	    if (object instanceof EditText) {
		builder.append("want=\"" + String.class.getName() + "\" ");

		ViewGroup parent = (ViewGroup) object.getParent();
		int index = parent.indexOfChild(object);
		boolean name_set = false;
		if (index > 0) {
		    View sibling = parent.getChildAt(index - 1);
		    if (sibling instanceof TextView) {
			builder.append("name=\"" + ((TextView) sibling).getText() + "\" ");
			name_set = true;
		    }
		}
		if (!name_set) {
		    builder.append("name=\"TODO\" ");
		}
		builder.append("unit=\"mm\" ");
	    } else if (object instanceof CheckBox) {
		builder.append("want=\"" + Boolean.class.getName() + "\" ");
		builder.append("name=\"" + ((CheckBox) object).getText() + "\" ");
	    } else {
		builder.append("want=\"" + object.getClass().getName() + "\" ");
		builder.append("name=\"TODO\" ");
	    }
	    builder.append("x=\"0.0\" y=\"0.0\" spacing=\"0.0\" width=\"normal\" style=\"normal\" ");
	    builder.append("/>\n");
	}

	public String getPrintString(String resid, String want) {
	    int viewid = activity.getResources().getIdentifier(resid, null, null);
	    if (viewid == 0) {
		LogActivity.writeLog("不能找到ID为“%s”的资源", resid);
		return null;
	    }
	    View view = findViewById(viewid);
	    if (view == null) {
		if (BuildConfig.DEBUG) {
		    LogActivity.writeLog("找不到ID为“%d(%s)“的控件，可能界面还没有装载",
			    viewid, resid);
		}
		return null;
	    }
	    String key = String.valueOf(viewid);

	    if (view instanceof CheckBox) {
		if (want.equals(Boolean.class.getName())) {
		    return bundle.getBoolean(key) ? "√" : "";
		} else if (want.equals(String.class.getName())) {
		    return ((CheckBox) view).getText().toString();
		} else {
		    LogActivity.writeLog("不能处理打印字段 %s 期盼的类型 %s", resid, want);
		    return null;
		}
	    } else if (view instanceof EditText) {
		if (want.equals(Boolean.class.getName())) {
		    LogActivity.writeLog("打印字段 %s 不支持Boolean类型", resid);
		    return null;
		} else if (want.equals(String.class.getName())) {
		    return bundle.containsKey(key) ? bundle.getString(key) : "";
		} else {
		    LogActivity.writeLog("不能处理打印字段 %s 期盼的类型 %s", resid, want);
		    return null;
		}
	    } else {
		LogActivity.writeLog("打印字段类型 %s 不支持，略过", view.getClass().getName());
		return null;
	    }
	}

	public boolean canScrollUp() {
	    return getScrollY() == 0 ? false : true;
	}

	public boolean canScrollDown() {
	    if (getBottom() == 0)
		return false;

	    View child = getChildAt(getChildCount() - 1);
	    return child.getBottom() > getScrollY() + getBottom();
	}

	public void scrollUp() {
	    smoothScrollBy(0, -100);
	}

	public void scrollDown() {
	    smoothScrollBy(0, 100);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
            super.onScrollChanged(l, t, oldl, oldt);
	    if (listener != null) {
		listener.onFormPageScrolled(Form.this, this);
	    }
	}

	public Bitmap getBitmap() {
	    if (getChildCount() == 0) {
		loadFromBundle();
	    }
	    View frame = getChildAt(0);
	    if (frame != null) {
		Bitmap bitmap = Bitmap.createBitmap(frame.getWidth(),
			frame.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		frame.draw(canvas);
		return bitmap;
	    }
	    return null;
	}
    }

}
