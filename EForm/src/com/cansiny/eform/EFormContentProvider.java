/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

class ContentDatabaseHelper extends SQLiteOpenHelper
{

    public ContentDatabaseHelper(Context context, String name,
	    CursorFactory factory, int version,
	    DatabaseErrorHandler errorHandler) {
	super(context, name, factory, version, errorHandler);
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


public class EFormContentProvider extends ContentProvider implements DatabaseErrorHandler
{
    static final public String MIME_TYPE_SINGLE_RECORD = "vnd.android.cursor.item/vnd.cansiny.eform";

    private ContentDatabaseHelper db_helper;

    @Override
    public boolean onCreate() {
	Log.d("EFormContentProvider", "OnCreate");
	// TODO Auto-generated method stub
	db_helper = new ContentDatabaseHelper(getContext(), "", null, 1, this);
	return false;
    }

    @Override
    public String getType(Uri uri) {
	Log.d("EFormContentProvider", "getType");
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
	    String[] selectionArgs, String sortOrder) {
	Log.d("EFormContentProvider", "query");
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
	synchronized(this) {
	    Log.d("EFormContentProvider", "insert");
	    // TODO Auto-generated method stub
	    return null;
	}
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
	    String[] selectionArgs) {
	Log.d("EFormContentProvider", "update");
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public int delete(Uri arg0, String arg1, String[] arg2) {
	Log.d("EFormContentProvider", "delete");
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public void onCorruption(SQLiteDatabase dbObj) {
	// TODO Auto-generated method stub
	
    }

}
