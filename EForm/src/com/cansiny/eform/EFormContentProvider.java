/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class EFormContentProvider extends ContentProvider implements
	DatabaseErrorHandler
{
    static final private String AUTHORITY = EFormContent.AUTHORITY;
    static final private Uri CONTENT_URI = EFormContent.CONTENT_URI;

    static final private String DATABASE_NAME = "eform.db";
    static final private int DATABASE_VERSION = 1;

    static final private int CODE_TABLE_ACCOUNT = 1;
    static final private int CODE_TABLE_ACCOUNT_ID = 1;
    static final private int CODE_TABLE_MEMBER = 1;
    static final private int CODE_TABLE_MEMBER_ID = 1;

    static final private UriMatcher uriMatcher;

    private ContentDatabaseHelper db_helper;

    static {
	uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	uriMatcher.addURI(AUTHORITY, "account", CODE_TABLE_ACCOUNT);
	uriMatcher.addURI(AUTHORITY, "account/#", CODE_TABLE_ACCOUNT_ID);
	uriMatcher.addURI(AUTHORITY, "member", CODE_TABLE_MEMBER);
	uriMatcher.addURI(AUTHORITY, "member/#", CODE_TABLE_MEMBER_ID);
    }


    @Override
    public boolean onCreate() {
	db_helper = new ContentDatabaseHelper(getContext(), DATABASE_NAME,
		null, DATABASE_VERSION, this);
	return true;
    }

    @Override
    public String getType(Uri uri) {
	Log.d("EFormContentProvider", "getType");
	return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
	    String[] selectionArgs, String sortOrder) {
	Log.d("EFormContentProvider", "query : " + uri.toString());
	return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
	synchronized(this) {
	    Log.d("EFormContentProvider", "insert");
	    return null;
	}
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
	    String[] selectionArgs) {
	synchronized(this) {
	    Log.d("EFormContentProvider", "update");
	    return 0;
	}
    }

    @Override
    public int delete(Uri arg0, String arg1, String[] arg2) {
	synchronized(this) {
	    Log.d("EFormContentProvider", "delete");
	    return 0;
	}
    }

    @Override
    public void shutdown() {
	super.shutdown();
    }

    @Override
    public void onLowMemory() {
	super.onLowMemory();
    }

    @Override
    public void onCorruption(SQLiteDatabase db) {
	LogActivity.writeLog("Database corruption: " + db.toString());
    }


    private class ContentDatabaseHelper extends SQLiteOpenHelper
    {
	static final public String TABLE_NAME_ACCOUNT = "account";
	static final public String TABLE_NAME_MEMBER  = "member";
	static final public String TABLE_NAME_VOUCHER = "voucher";

	public ContentDatabaseHelper(Context context, String name,
        	CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
            super(context, name, factory, version, errorHandler);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_NAME_ACCOUNT + " ("
        	    + EFormContent.Account._ID + " INTEGER PRIMARY KEY,"
        	    + EFormContent.Account.USERID + " INTEGER,"
        	    + EFormContent.Account.PASSWORD + " TEXT,"
        	    + EFormContent.Account.ROLE + " INTEGER,"
        	    + ");");

            db.execSQL("CREATE TABLE " + TABLE_NAME_MEMBER + " ("
        	    + EFormContent.Member._ID + " INTEGER PRIMARY KEY,"
        	    + EFormContent.Member.USERID + " TEXT,"
        	    + EFormContent.Member.USERNAME + " TEXT,"
        	    + EFormContent.Member.PASSWORD + " TEXT,"
        	    + EFormContent.Member.COMPANY + " TEXT,"
        	    + EFormContent.Member.PHONE + " TEXT,"
        	    + ");");

            db.execSQL("CREATE TABLE " + TABLE_NAME_VOUCHER + " ("
        	    + EFormContent.Voucher._ID + " INTEGER PRIMARY KEY,"
        	    + EFormContent.Voucher.MEMBERID + " TEXT,"
        	    + EFormContent.Voucher.FORMCLASS + " TEXT,"
        	    + EFormContent.Voucher.CONTENTS + " BLOB,"
        	    + EFormContent.Voucher.DATETIME + " TEXT,"
        	    + ");");

            initData(db, db.getVersion(), db.getVersion());
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onCreate(db);
            initData(db, oldVersion, newVersion);
        }

        private void initData(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("INSERT INTO " + TABLE_NAME_ACCOUNT
        	    + " VALUES(null, -1, 'password', 0);");
        }
    }

}
