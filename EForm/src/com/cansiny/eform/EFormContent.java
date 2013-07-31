/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import android.net.Uri;

public class EFormContent
{
    static final public String AUTHORITY = "content://com.cansiny.eform.EFormContentProvider";
    static final public Uri CONTENT_URI = Uri.parse(AUTHORITY);


    public class Administrator
    {
	static final public String _ID      = "_id";
	static final public String ROLE     = "role";
	static final public String ACCOUNT  = "account";
	static final public String PASSWORD = "password";
    }
    
    public class Member
    {
	static final public String _ID      = "_id";
	static final public String USERID   = "userid";
	static final public String USERNAME = "username";
	static final public String PASSWORD = "password";
	static final public String COMPANY  = "company";
	static final public String PHONE    = "phone";
    }
    
    public class Voucher
    {
	static final public String _ID       = "_id";
	static final public String MEMBERID  = "memberid";
	static final public String DATE      = "date";
	static final public String FORMCLASS = "formclass";
	static final public String CONTENTS  = "contents";
    }
}
