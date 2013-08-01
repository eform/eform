/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.BaseColumns;

public class EFormContent
{
    static final public String AUTHORITY = "com.cansiny.eform.EFormContentProvider";
    static final public Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);


    static public class Account implements BaseColumns
    {
	static final public Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/account");

	static final public String _ID      = BaseColumns._ID;
	static final public String USERID   = "userid";
	static final public String PASSWORD = "password";
	static final public String ROLE     = "role";
    }


    static public class Member implements BaseColumns
    {
	static final public Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/member");

	static final public String _ID      = BaseColumns._ID;
	static final public String USERID   = "userid";
	static final public String USERNAME = "username";
	static final public String PASSWORD = "password";
	static final public String COMPANY  = "company";
	static final public String PHONE    = "phone";

	static public boolean login(Context context, String userid, String password) {
	    final ContentResolver resolver = context.getContentResolver();
	    String[] projection = new String[] { _ID, USERID, PASSWORD };
	    String selection = "";
	    String[] selectionArgs = null;
	    String sortOrder = "";
	    resolver.query(CONTENT_URI, projection, selection, selectionArgs, sortOrder);
	    return true;
	}
    }


    static public class Voucher implements BaseColumns
    {
	static final public Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/voucher");

	static final public String _ID       = BaseColumns._ID;
	static final public String MEMBERID  = "memberid";
	static final public String FORMCLASS = "formclass";
	static final public String CONTENTS  = "contents";
	static final public String DATETIME  = "datetime";
    }
}
