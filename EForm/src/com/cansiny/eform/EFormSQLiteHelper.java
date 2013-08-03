/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import com.cansiny.eform.Member.MemberProfile;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Base64;


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
		+ "username TEXT NOT NULL,"
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

	static public boolean hasPassword(Context context) {
	    EFormSQLiteHelper helper = EFormSQLiteHelper.getSQLiteHelper(context);
	    SQLiteDatabase database = helper.getReadableDatabase();

	    Cursor cursor = database.query(TABLE_NAME, null,
		    "userid=0 and role=0", null , null, null, null);

	    if (cursor == null) {
		helper.close();
		LogActivity.writeLog("查询 " + TABLE_NAME + " 错误，返回cursor == null");
		return false;
	    }

	    int count = cursor.getCount();
	    cursor.close();
	    helper.close();

	    return (count > 0) ? true : false;
	}

	static public boolean setPassword(Context context, String password) {
	    if (password.length() != 6) {
		throw new IllegalArgumentException("管理员密码长度必须为6位");
	    }

	    EFormSQLiteHelper helper = EFormSQLiteHelper.getSQLiteHelper(context);
	    SQLiteDatabase database = helper.getReadableDatabase();

	    Cursor cursor = database.query(TABLE_NAME, new String[] { COLUMN_ID },
		    "userid=0 and role=0", null , null, null, null);

	    if (cursor == null) {
		helper.close();
		LogActivity.writeLog("查询 " + TABLE_NAME + " 错误，返回cursor == null");
		return false;
	    }

	    ContentValues values = new ContentValues();
	    values.put(COLUMN_PASSWORD, SHAPassword(password));

	    if (cursor.getCount() == 0) {
		values.put(COLUMN_USERID, 0);
		values.put(COLUMN_ROLE, Account.ROLE_ADMINISTRATOR);
		long rowid = database.insert(TABLE_NAME, null, values);
		return (rowid >= 0) ? true : false;
	    } else {
		cursor.moveToFirst();
		int rows = database.update(TABLE_NAME, values,
			"_id=?", new String[] { String.valueOf(cursor.getLong(0)) });
		return (rows == 1) ? true : false;
	    }
	}

	static public int login(Context context, String password) {
	    EFormSQLiteHelper helper = EFormSQLiteHelper.getSQLiteHelper(context);
	    SQLiteDatabase database = helper.getReadableDatabase();

	    Cursor cursor = database.query(TABLE_NAME, new String[] { COLUMN_PASSWORD },
		    "userid=0 and role=0", null , null, null, null);

	    if (cursor == null) {
		helper.close();
		LogActivity.writeLog("查询 " + TABLE_NAME + " 错误，返回cursor == null");
		return -1;
	    }

	    if (cursor.getCount() == 0) {
		cursor.close();
		helper.close();
		return password.equals("123456") ? 1 : -1;
	    }

	    cursor.moveToFirst();
	    String encoded_password = cursor.getString(0);

	    cursor.close();
	    helper.close();

	    return encoded_password.equals(SHAPassword(password)) ? 0 : -1;
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
//		if (rowid >= 0)
//		    Account.addMember(values, admin_passwd);
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

	static public ArrayList<MemberProfile> listAll(Context context) {
	    EFormSQLiteHelper helper = EFormSQLiteHelper.getSQLiteHelper(context);
	    SQLiteDatabase database = helper.getWritableDatabase();

	    String[] columns = { COLUMN_ID, COLUMN_USERID, COLUMN_USERNAME,
		    COLUMN_COMPANY, COLUMN_PHONE };

	    Cursor cursor = database.query(TABLE_NAME, columns,
		    null, null, null, null, "username ASC");

	    if (cursor == null || cursor.getCount() <= 0) {
		helper.close();
		return null;
	    }

	    ArrayList<MemberProfile> members = new ArrayList<MemberProfile>();
	    while(cursor.moveToNext()) {
		MemberProfile profile = new MemberProfile();

		profile.rowid = cursor.getLong(0);
		profile.userid = cursor.getString(1);
		profile.username = cursor.getString(2);
		profile.company = cursor.getString(3);
		profile.phone = cursor.getString(4);

		members.add(profile);
	    }
	    cursor.close();
	    helper.close();

	    return members;
	}
    }


    public class Voucher
    {
	static final public String TABLE_NAME = "voucher";
    }

}
