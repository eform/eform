/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import android.content.Context;


public class Voucher
{
    public long   rowid;
    public long   userid;
    public String formclass;
    public String formlabel;
    public int    formimage;
    public String contents;
    public String comment;
    public long   ctime;
    public long   mtime;
    public long   atime;

    public Voucher() {
	rowid = -1;
	userid = -1;
	formclass = null;
	formlabel = null;
	contents = null;
	comment = null;
    }

    public boolean insert(Context context) {
	return EFormSQLite.Voucher.insert(context, userid,
		formclass, formlabel, formimage, contents, comment);
    }

    public boolean update(Context context) {
	return EFormSQLite.Voucher.update(context, rowid, userid,
		formclass, formlabel, formimage, contents, comment);
    }

}
