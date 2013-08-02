/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Base64;
import android.util.Log;


public class EFormSQLiteHelper extends SQLiteOpenHelper
{
    static final private String DATABASE_NAME = "eform.db";
    static final private int DATABASE_VERSION = 1;

    static public EFormSQLiteHelper getSQLiteHelper(Context context) {
	return new EFormSQLiteHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    static public String SHAPassword(String input) {
	try {
	    byte[] digest = MessageDigest.getInstance("SHA-1").digest(input.getBytes());
	    return Base64.encodeToString(digest, Base64.NO_WRAP);
	} catch (NoSuchAlgorithmException e) {
	    LogActivity.writeLog(e);
	    return null;
	}
    }


    public EFormSQLiteHelper(Context context, String name,
	    SQLiteDatabase.CursorFactory factory, int version) {
	super(context, name, factory, version);
    }

    @SuppressLint("NewApi")
    @Override
    public void onConfigure(SQLiteDatabase db) {
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
	    setWriteAheadLoggingEnabled(true);
	}
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
	createTables(db);
        initData(db, db.getVersion(), db.getVersion());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	createTables(db);
	initData(db, oldVersion, newVersion);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
    
    }

    private void createTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + Account.TABLE_NAME + " ("
        	+ "_id      INTEGER PRIMARY KEY,"
        	+ "userid   INTEGER UNIQUE,"
        	+ "password TEXT NOT NULL,"
        	+ "role     INTEGER NOT NULL"
        	+ ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + Member.TABLE_NAME + " ("
        	+ "_id      INTEGER PRIMARY KEY,"
        	+ "userid   TEXT UNIQUE,"
        	+ "usrename TEXT NOT NULL,"
        	+ "password TEXT NOT NULL,"
        	+ "company  TEXT,"
        	+ "phone    TEXT"
        	+ ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + Voucher.TABLE_NAME + " ("
        	+ "_id       INTEGER PRIMARY KEY,"
        	+ "userid    INTEGER UNIQUE,"
        	+ "formclass TEXT NOT NULL,"
        	+ "contents  BLOB NOT NULL,"
        	+ "datetime  TEXT NOT NULL"
        	+ ");");
    }

    private void initData(SQLiteDatabase db, int oldVersion, int newVersion) {
	ContentValues values = new ContentValues();

	values.put("_id", 0);
	values.put("userid", 0);
	values.put("password", SHAPassword("123456"));
	values.put("role", Account.ROLE_ADMINISTRATOR);

	long rowid = db.insert("account", null, values);
	if (rowid != 0) {
	    LogActivity.writeLog("Administrator must has rowid 0, current is %d", rowid);
	}
    }


    static public class Account
    {
	static final public String TABLE_NAME = "account";

	static final public String COLUMN_ID = "_id";
	static final public String COLUMN_USERID = "userid";
	static final public String COLUMN_PASSWORD = "password";
	static final public String COLUMN_ROLE = "role";

	static final public int ROLE_ADMINISTRATOR = 0;
	static final public int ROLE_MEMBER = 1;

	static public long addMember(ContentValues values, String admin_password) {
	    return -1;
	}
    }


    static public class Member
    {
	static final public String TABLE_NAME = "member";

	static final public String COLUMN_ID = "_id";
	static final public String COLUMN_USERID = "userid";
	static final public String COLUMN_USERNAME = "username";
	static final public String COLUMN_PASSWORD = "password";
	static final public String COLUMN_COMPANY = "company";
	static final public String COLUMN_PHONE = "phone";

	static public ContentValues login(Context context, String userid, String password) {
	    EFormSQLiteHelper helper = EFormSQLiteHelper.getSQLiteHelper(context);
	    SQLiteDatabase database = helper.getReadableDatabase();

	    String[] columns = { COLUMN_ID, COLUMN_USERNAME, COLUMN_PASSWORD,
		    COLUMN_COMPANY, COLUMN_PHONE };

	    Cursor cursor = database.query(TABLE_NAME, columns,
		    "userid=?", new String[]{ userid }, null, null, null);

	    if (cursor == null || cursor.getCount() != 1) {
		helper.close();
		return null;
	    }

	    String encoded_password = cursor.getString(2);
	    if (!encoded_password.equals(SHAPassword(password))) {
		helper.close();
		return null;
	    }

	    ContentValues values = new ContentValues();

	    values.put(COLUMN_ID, cursor.getLong(0));
	    values.put(COLUMN_USERID, userid);
	    values.put(COLUMN_USERNAME, cursor.getLong(1));
	    values.put(COLUMN_COMPANY, cursor.getLong(3));
	    values.put(COLUMN_PHONE, cursor.getLong(4));

	    helper.close();

	    return values;
	}

	static public long register(Context context, ContentValues values, String admin_passwd) {
	    EFormSQLiteHelper helper = EFormSQLiteHelper.getSQLiteHelper(context);
	    SQLiteDatabase database = helper.getWritableDatabase();

	    String userid = values.getAsString(COLUMN_USERID);
	    String username = values.getAsString(COLUMN_USERNAME);
	    String password = values.getAsString(COLUMN_PASSWORD);

	    if (userid == null || username == null || password == null) {
		helper.close();
		return -1;
	    }

	    String[] columns = { COLUMN_ID };
	    Cursor cursor = database.query(TABLE_NAME, columns,
		    "userid=?", new String[]{ userid }, null, null, null);
	    if (cursor != null && cursor.getCount() > 0) {
		helper.close();
		return -2;
	    }

	    long rowid = -1;
	    database.beginTransaction();
	    try {
		values.put(COLUMN_PASSWORD, SHAPassword(password));
		rowid = database.insert(TABLE_NAME, null, values);
		if (rowid >= 0)
		    Account.addMember(values, admin_passwd);
		database.setTransactionSuccessful();
	    } finally {
		database.endTransaction();
	    }
	    helper.close();
	    return rowid;
	}

	public void update(Context context, ContentValues values) {
	    
	}

	public void delete(Context context) {
	    
	}
    }


    public class Voucher
    {
	static final public String TABLE_NAME = "voucher";
    }

}
