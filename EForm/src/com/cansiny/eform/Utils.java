/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Utils
{
    static public final int IMAGE_SIZE_LARGE = 1;
    static public final int IMAGE_SIZE_SMALL = 2;

    /**
     * This method converts dp unit to equivalent pixels, depending on
     * device density. 
     * 
     * @param dp A value in dp (density independent pixels) unit. Which we
     * 		need to convert into pixels
     * @return A float value to represent px equivalent to dp depending on
     *		device density
     */
    static public float convertDpToPixel(float dp) {
	Context context = EFormApplication.getContext();
	DisplayMetrics metrics = context.getResources().getDisplayMetrics();
	return dp * (metrics.densityDpi / 160f);
    }

    /**
     * This method converts device specific pixels to density independent pixels
     * 
     * @param px A value in px (pixels) unit. Which we need to convert into dp
     * @return A float value to represent dp equivalent to px value
     */
    static public float convertPixelsToDp(float px) {
	Context context = EFormApplication.getContext();
	DisplayMetrics metrics = context.getResources().getDisplayMetrics();
	return px / (metrics.densityDpi / 160f);
    }

    static public Toast makeToast(CharSequence sequence, int image_res, int image_size, int duration) {
	Context context = EFormApplication.getContext();

	LinearLayout layout = new LinearLayout(context);
	layout.setBackgroundResource(R.color.translucence);
	layout.setPadding(20, 10, 20, 10);

	if (image_res != 0) {
	    ImageView image = new ImageView(context);
	    image.setBackgroundResource(image_res);
	    image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

	    if (image_size == IMAGE_SIZE_SMALL) {
		image_size = (int) convertDpToPixel(48);
	    } else {
		image_size = (int) convertDpToPixel(64);
	    }
	    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
		    image_size, image_size);
	    params.gravity = Gravity.CENTER_VERTICAL;
	    params.rightMargin = 10;
	    layout.addView(image, params);
	}

	TextView text_view = new TextView(context);
	text_view.setText(sequence);
	text_view.setTextColor(context.getResources().getColor(R.color.yellow));
	text_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
	text_view.setGravity(Gravity.CENTER_VERTICAL);

	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
		ViewGroup.LayoutParams.WRAP_CONTENT,
		ViewGroup.LayoutParams.WRAP_CONTENT);
	params.gravity = Gravity.CENTER_VERTICAL;
	layout.addView(text_view, params);

	Toast toast = new Toast(context);
	toast.setView(layout);
	toast.setDuration(duration);
	toast.setGravity(Gravity.CENTER, 0, 0);

	return toast;
    }

    static void showToast(CharSequence sequence, int image_res, int image_size) {
	makeToast(sequence, image_res, image_size, Toast.LENGTH_SHORT).show();
    }

    static void showToast(CharSequence sequence, int image_res) {
	showToast(sequence, image_res, IMAGE_SIZE_LARGE);
    }

    static void showToast(CharSequence sequence) {
	showToast(sequence, 0, IMAGE_SIZE_LARGE);
    }

    static void showToastLong(CharSequence sequence, int image_res, int image_size) {
	makeToast(sequence, image_res, image_size, Toast.LENGTH_LONG).show();
    }

    static void showToastLong(CharSequence sequence, int image_res) {
	showToastLong(sequence, image_res, IMAGE_SIZE_LARGE);
    }

    static void showToastLong(CharSequence sequence) {
	showToastLong(sequence, 0, IMAGE_SIZE_LARGE);
    }

}
