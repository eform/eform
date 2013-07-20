/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 * 
 * HomeActivity - The application entry
 *
 * Authors:
 *   Xiaohu <xiaohu417@gmail.com>, 2013.7.3, hefei
 */
package com.cansiny.eform;

public class MagcardReader
{
    static CharSequence formatCardno(CharSequence cardno, int start, int end) {
	StringBuilder builder = new StringBuilder();
	int length = cardno.length();

	for (int i = 0; i < length; i++) {
	    if (i < start || i > length - end)
		builder.append(cardno.charAt(i));
	    else
		builder.append('*');

	    if ((i + 1) % 4 == 0)
		builder.append(' ');
	}
	return builder;
    }

    static CharSequence hideCardno(CharSequence cardno, int start, int end) {
	int length = cardno.length();
	if (length > start + end) {
	    StringBuilder builder = new StringBuilder();
	    builder.append(cardno.subSequence(0, 4));
	    for (int i = start + end; i < length; i ++) {
		builder.append('*');
	    }
	    builder.append(cardno.subSequence(length - 3, length));
	    return builder;
	}
	return cardno;
    }

    public CharSequence getCardno() {
	return "1234567890123320";
    }
}
