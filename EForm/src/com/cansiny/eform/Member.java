/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.ScrollView;


class MemberDatabaseHelper extends SQLiteOpenHelper
{

    public MemberDatabaseHelper(Context context, String name,
	    CursorFactory factory, int version) {
	super(context, name, factory, version);
	// TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	// TODO Auto-generated method stub
	
    }

}


public class Member
{
    static private Member _singleInstance = null;
    static public Member getMember() {
	if (_singleInstance == null) {
	    _singleInstance = new Member();
	}
	return _singleInstance;
    }

    private boolean is_login;
    private MemberDatabaseHelper db_helper;
    private MemberListener listener;

    private Member() {
	is_login = false;
//	db_helper = new MemberDatabaseHelper();
    }


    public boolean is_login() {
	return is_login;
    }

    public boolean login(String uname, String passwd) {
	is_login = true;
	if (listener != null) {
	    listener.on_member_login();
	}
	return is_login;
    }

    public void login() {
	login("", "");
    }

    public void logout() {
	is_login = false;
	if (listener != null) {
	    listener.on_member_logout();
	}
    }
    
    public boolean register(String uname, String passwd) {
	return true;
    }


    public void setListener(MemberListener listener) {
	this.listener = listener;
    }


    public interface MemberListener
    {
	public void on_member_login();
	public void on_member_logout();
    }
}
