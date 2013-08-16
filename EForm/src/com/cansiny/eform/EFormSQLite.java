/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.cansiny.eform.Member.MemberProfile;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Base64;


public class EFormSQLite extends SQLiteOpenHelper
{
    static final private String DATABASE_NAME = "eform.db";
    static final private int DATABASE_VERSION = 1;

    static public EFormSQLite getSQLite(Context context) {
	return new EFormSQLite(context, DATABASE_NAME, null, DATABASE_VERSION);
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

    static public byte[] MD5Hash(String input) {
	try {
	    return MessageDigest.getInstance("MD5").digest(input.getBytes());
	} catch (NoSuchAlgorithmException e) {
	    LogActivity.writeLog(e);
	    return null;
	}
    }

    static public byte[] AESEncrypt(byte[] password, byte[] input) {
        try {
            SecretKeySpec key = new SecretKeySpec(password, "AES");
	    Cipher cipher = Cipher.getInstance("AES");
	    cipher.init(Cipher.ENCRYPT_MODE, key);
	    return cipher.doFinal(input);
	} catch (Exception e) {
	    LogActivity.writeLog(e);
	    return null;
	}
    }

    static public byte[] AESDecrypt(byte[] password, byte[] input) {
	try {
	    SecretKeySpec key = new SecretKeySpec(password, "AES");
	    Cipher cipher = Cipher.getInstance("AES");
	    cipher.init(Cipher.DECRYPT_MODE, key);
	    return cipher.doFinal(input);
	} catch (Exception e) {
	    LogActivity.writeLog(e);
	    return null;
	}
    }

    static public String AESEncryptPassword(byte[] password, String input) {
	byte[] ciphertext = AESEncrypt(password, input.getBytes());
	if (ciphertext == null)
	    return null;

	return Base64.encodeToString(ciphertext, Base64.NO_WRAP);
    }

    static public String AESDecryptPassword(byte[] password, String input) {
	try {
	    byte[] ciphertext = Base64.decode(input, Base64.NO_WRAP);
	    return new String(AESDecrypt(password, ciphertext));
	} catch (Exception e) {
	    LogActivity.writeLog(e);
	    return null;
	}
    }

    public EFormSQLite(Context context, String name,
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
		   + "phone    TEXT,"
		   + "datetime INTEGER NOT NULL"
		   + ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + Voucher.TABLE_NAME + " ("
		   + "_id       INTEGER PRIMARY KEY,"
		   + "userid    INTEGER NOT NULL,"
		   + "comment   TEXT NOT NULL,"
		   + "formclass TEXT NOT NULL,"
		   + "formlabel TEXT NOT NULL,"
		   + "formimage TEXT NOT NULL,"
		   + "contents  BLOB NOT NULL,"
		   + "ctime     INTEGER NOT NULL,"
		   + "mtime     INTEGER NOT NULL,"
		   + "atime     INTEGER NOT NULL"
		   + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//	db.execSQL("ALTER TABLE member ADD COLUMN datetime INTEGER NOT NULL DEFAULT 0;");
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
	super.onOpen(db);
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
	    EFormSQLite sqlite = EFormSQLite.getSQLite(context);
	    SQLiteDatabase database = sqlite.getReadableDatabase();

	    Cursor cursor = database.query(TABLE_NAME, null,
		    "userid=0 and role=0", null , null, null, null);

	    if (cursor == null) {
		sqlite.close();
		LogActivity.writeLog("查询 " + TABLE_NAME + " 错误，返回cursor == null");
		return false;
	    }

	    int count = cursor.getCount();

	    cursor.close();
	    sqlite.close();

	    return (count > 0) ? true : false;
	}

	static public boolean setPassword(Context context, String password) {
	    if (password.length() != 6) {
		throw new IllegalArgumentException("管理员密码长度必须为6位");
	    }

	    Administrator admin = Administrator.getAdministrator();
	    if (!admin.isLogin()) {
		LogActivity.writeLog("设置管理员密码需要管理员先登录");
		return false;
	    }
	    String old_password = admin.getPassword();
	    if (old_password == null) {
		LogActivity.writeLog("不能得到管理员旧密码");
		return false;
	    }

	    EFormSQLite sqlite = EFormSQLite.getSQLite(context);
	    SQLiteDatabase database = sqlite.getReadableDatabase();

	    Cursor cursor = database.query(TABLE_NAME, new String[] { COLUMN_ID },
		    "userid=0 and role=0", null , null, null, null);

	    if (cursor == null) {
		sqlite.close();
		LogActivity.writeLog("查询 " + TABLE_NAME + " 错误，返回cursor == null");
		return false;
	    }

	    long rowid = -1;
	    if (cursor.getCount() > 0) {
		cursor.moveToFirst();
		rowid = cursor.getLong(0);
	    }
	    cursor.close();

	    boolean retval = false;
	    database = sqlite.getWritableDatabase();
	    database.beginTransaction();
	    try {
		ContentValues values = new ContentValues();

		if (rowid == -1) {	// insert new
		    values.put(COLUMN_USERID, 0);
		    values.put(COLUMN_PASSWORD, SHAPassword(password));
		    values.put(COLUMN_ROLE, Account.ROLE_ADMINISTRATOR);
		    rowid = database.insert(TABLE_NAME, null, values);
		    retval = (rowid >= 0) ? true : false;
		} else {		// update exists
		    values.put(COLUMN_PASSWORD, SHAPassword(password));
		    int rows = database.update(TABLE_NAME, values,
			    "_id=?", new String[] { String.valueOf(rowid) });
		    retval = (rows == 1) ? true : false;
		}

		// continue update member passwords
		if (retval) {
		    cursor = database.query(TABLE_NAME, new String[] { COLUMN_ID,
			    COLUMN_PASSWORD }, "role=1", null, null, null, null);

		    byte[] old_hash = MD5Hash(old_password);
		    byte[] new_hash = MD5Hash(password);

		    while(cursor.moveToNext()) {
			String cleartext = AESDecryptPassword(old_hash, cursor.getString(1));
			if (cleartext == null) {
			    retval = false;
			    break;
			}
			String ciphetext = AESEncryptPassword(new_hash, cleartext);
			if (ciphetext == null) {
			    retval = false;
			    break;
			}
			values.clear();
			values.put(COLUMN_PASSWORD, ciphetext);
			if (database.update(TABLE_NAME, values, "_id=?",
				new String[] { cursor.getString(0) }) != 1) {
			    retval = false;
			    break;
			}
		    }
		    cursor.close();

		    if (retval) {
			database.setTransactionSuccessful();
		    }
		}
	    } finally {
		database.endTransaction();
		sqlite.close();
	    }
	    return retval;
	}

	static public int login(Context context, String password) {
	    EFormSQLite sqlite = EFormSQLite.getSQLite(context);
	    SQLiteDatabase database = sqlite.getReadableDatabase();

	    Cursor cursor = database.query(TABLE_NAME, new String[] { COLUMN_PASSWORD },
		    "userid=0 and role=0", null , null, null, null);

	    if (cursor == null) {
		sqlite.close();
		LogActivity.writeLog("查询 " + TABLE_NAME + " 错误，返回cursor == null");
		return -1;
	    }

	    if (cursor.getCount() == 0) {
		cursor.close();
		sqlite.close();
		return password.equals("123456") ? 1 : -1;
	    }

	    cursor.moveToFirst();
	    String encoded_password = cursor.getString(0);

	    cursor.close();
	    sqlite.close();

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
	static final public String COLUMN_DATETIME = "datetime";

	static public long register(Context context, String userid, String username,
		String password, String company, String phone, String admin_passwd) {
	    if (userid == null || userid.length() == 0 ||
		    username == null || username.length() == 0 ||
		    password == null || password.length() != 6 ||
		    admin_passwd == null || admin_passwd.length() != 6) {
		LogActivity.writeLog("注册会员的参数无效");
		return -1;
	    }

	    EFormSQLite sqlite = EFormSQLite.getSQLite(context);
	    SQLiteDatabase database = sqlite.getWritableDatabase();

	    Cursor cursor = database.query(TABLE_NAME, new String[] { COLUMN_ID },
		    "userid=?", new String[]{ userid }, null, null, null);

	    if (cursor == null) {
		sqlite.close();
		LogActivity.writeLog("查询 " + TABLE_NAME + " 错误，返回cursor == null");
		return -1;
	    }

	    if (cursor.getCount() > 0) {
		LogActivity.writeLog("会员 '%s' 已经存在。", userid);
		cursor.close();
		sqlite.close();
		return -2;
	    }
	    cursor.close();

	    long rowid = -1;
	    database.beginTransaction();
	    try {
		ContentValues values = new ContentValues();

		values.put(COLUMN_USERID, userid);
		values.put(COLUMN_USERNAME, username);
		values.put(COLUMN_PASSWORD, SHAPassword(password));
		values.put(COLUMN_COMPANY, company);
		values.put(COLUMN_PHONE, phone);
		values.put(COLUMN_DATETIME, System.currentTimeMillis());

		rowid = database.insert(TABLE_NAME, null, values);
		if (rowid >= 0) {
		    byte[] md5hash = MD5Hash(admin_passwd);
		    String member_passwd = AESEncryptPassword(md5hash, password);
		    if (member_passwd != null) {
			values.clear();
			values.put(Account.COLUMN_USERID, rowid);
			values.put(Account.COLUMN_PASSWORD, member_passwd);
			values.put(Account.COLUMN_ROLE, Account.ROLE_MEMBER);
			if (database.insert(Account.TABLE_NAME, null, values) >= 0) {
			    database.setTransactionSuccessful();
			}
		    }
		}
	    } finally {
		database.endTransaction();
		sqlite.close();
	    }
	    return rowid;
	}

	static public ContentValues login(Context context, String userid, String password) {
	    EFormSQLite sqlite = EFormSQLite.getSQLite(context);
	    SQLiteDatabase database = sqlite.getReadableDatabase();

	    String[] columns = { COLUMN_ID, COLUMN_USERNAME, COLUMN_PASSWORD,
		    COLUMN_COMPANY, COLUMN_PHONE, COLUMN_DATETIME };

	    Cursor cursor = database.query(TABLE_NAME, columns,
		    "userid=?", new String[]{ userid }, null, null, null);

	    if (cursor == null || cursor.getCount() <= 0) {
		sqlite.close();
		return null;
	    }
	    cursor.moveToFirst();

	    String sha_password = cursor.getString(2);
	    if (!sha_password.equals(SHAPassword(password))) {
		sqlite.close();
		return null;
	    }

	    ContentValues values = new ContentValues();

	    values.put(COLUMN_ID, cursor.getLong(0));
	    values.put(COLUMN_USERID, userid);
	    values.put(COLUMN_USERNAME, cursor.getString(1));
	    values.put(COLUMN_COMPANY, cursor.getString(3));
	    values.put(COLUMN_PHONE, cursor.getString(4));
	    values.put(COLUMN_DATETIME, cursor.getLong(5));

	    cursor.close();
	    sqlite.close();

	    return values;
	}

	static public long getid(Context context, String userid) {
	    EFormSQLite sqlite = EFormSQLite.getSQLite(context);
	    SQLiteDatabase database = sqlite.getReadableDatabase();

	    String[] columns = { COLUMN_ID };

	    Cursor cursor = database.query(TABLE_NAME, columns,
		    "userid=?", new String[]{ userid }, null, null, null);

	    if (cursor == null || cursor.getCount() <= 0) {
		sqlite.close();
		return -1;
	    }
	    cursor.moveToFirst();

	    long rowid = cursor.getLong(0);

	    cursor.close();
	    sqlite.close();

	    return rowid;
	}

	static public boolean update(Context context, long rowid, String password,
		String company, String phone, String admin_passwd) {
	    String new_password = null;
	    String old_password = null;

	    EFormSQLite sqlite = EFormSQLite.getSQLite(context);

	    if (password != null && password.length() > 0) {
		if (password.length() != 6) {
		    LogActivity.writeLog("更新会员信息失败，密码长度必须是6位");
		    return false;
		}
		if (admin_passwd == null || admin_passwd.length() != 6) {
		    LogActivity.writeLog("更新会员密码需要管理员密码");
		    return false;
		}
		new_password = password;

		SQLiteDatabase database = sqlite.getReadableDatabase();
		Cursor cursor = database.query(Account.TABLE_NAME,
			new String[] { Account.COLUMN_PASSWORD },
			"userid=?", new String[] { String.valueOf(rowid) },
			null, null, null);
		if (cursor == null || cursor.getCount() == 0) {
		    LogActivity.writeLog("更新会员信息失败，不能获取会员原密码");
		    sqlite.close();
		    return false;
		}
		cursor.moveToFirst();
		old_password = AESDecryptPassword(MD5Hash(admin_passwd), cursor.getString(0));
		cursor.close();

		if (old_password == null) {
		    LogActivity.writeLog("更新会员信息失败，不能解密会员原密码");
		    sqlite.close();
		    return false;
		}

		/* don't update password if old and new password is same */
		if (old_password.equals(new_password)) {
		    new_password = null;
		    old_password = null;
		}
	    }

	    ContentValues values = new ContentValues();

	    if (company != null) {
		values.put(COLUMN_COMPANY, company);
	    }
	    if (phone != null) {
		values.put(COLUMN_PHONE, phone);
	    }
	    if (new_password != null) {
		values.put(COLUMN_PASSWORD, SHAPassword(new_password));
	    }
	    if (values.size() == 0) {
		LogActivity.writeLog("更新会员信息失败，没有需要更新的信息");
		sqlite.close();
		return false;
	    }

	    boolean retval = false;
	    SQLiteDatabase database = sqlite.getWritableDatabase();
	    database.beginTransaction();
	    try {
		do {
		    // update member table first
		    int rows = database.update(TABLE_NAME, values, "_id=?",
			    new String[] { String.valueOf(rowid) });
		    if (rows != 1) {
			LogActivity.writeLog("更新会员信息失败，不能更新会员信息");
			break;
		    }
		    /* member password not update, so no other update is need */
		    if (new_password == null) {
			database.setTransactionSuccessful();
			retval = true;
			break;
		    }

		    // update account table if member password was modified
		    String member_passwd = AESEncryptPassword(MD5Hash(admin_passwd), new_password);
		    if (member_passwd == null) {
			LogActivity.writeLog("更新会员信息失败，加密会员密码失败");
			break;
		    }
		    values.clear();
		    values.put(Account.COLUMN_PASSWORD, member_passwd);
		    rows = database.update(Account.TABLE_NAME, values, "userid=?",
			    new String[] { String.valueOf(rowid) });
		    if (rows != 1) {
			LogActivity.writeLog("更新会员信息失败，更新会员密码失败");
			break;
		    }

		    // update voucher table to uses new password encrypt
		    Cursor cursor = database.query(Voucher.TABLE_NAME,
			    new String[] { Voucher.COLUMN_ID, Voucher.COLUMN_CONTENTS },
			    "userid=?", new String[] { String.valueOf(rowid) },
			    null, null, null);
		    if (cursor == null) {
			LogActivity.writeLog("更新会员信息失败，更新会员存储信息失败，查询返回空");
			break;
		    }
		    int update_rows = 0;
		    while (cursor.moveToNext()) {
			byte[] contents = cursor.getBlob(1);
			if (contents == null) {
			    LogActivity.writeLog("更新会员信息失败，不能得到会员保存的信息");
			    break;
			}
			byte[] cleartext = AESDecrypt(MD5Hash(old_password), contents);
			if (cleartext == null) {
			    LogActivity.writeLog("更新会员信息失败，解密会员存储信息失败");
			    break;
			}
			byte[] ciphetext = AESEncrypt(MD5Hash(new_password), cleartext);
			if (ciphetext == null) {
			    LogActivity.writeLog("更新会员信息失败，加密会员存储信息失败");
			    break;
			}
			values.clear();
			values.put(Voucher.COLUMN_CONTENTS, ciphetext);
			if (database.update(Voucher.TABLE_NAME, values, "_id=?",
				new String[] { String.valueOf(cursor.getLong(0)) }) != 1) {
			    LogActivity.writeLog("更新会员信息失败，更新会员存储信息失败");
			    break;
			}
			update_rows++;
		    }
		    if (update_rows == cursor.getCount()) {
			database.setTransactionSuccessful();
			retval = true;
		    }
		} while (false);
	    } finally {
		database.endTransaction();
		sqlite.close();
	    }
	    return retval;
	}

	static public boolean delete(Context context, long rowid) {
	    EFormSQLite sqlite = EFormSQLite.getSQLite(context);
	    SQLiteDatabase database = sqlite.getWritableDatabase();

	    database.beginTransaction();
	    boolean retval = false;
	    try {
		int rows = database.delete(TABLE_NAME, "_id=?",
			new String[] { String.valueOf(rowid) });
		if (rows == 1) {
		    rows = database.delete(Account.TABLE_NAME, "userid=?", 
			    new String[] { String.valueOf(rowid) });
		    if (rows == 1) {
			database.setTransactionSuccessful();
			retval = true;
		    } else {
			LogActivity.writeLog("从Account表删除会员返回 %d", rows);
		    }
		} else {
		    LogActivity.writeLog("从Member表删除会员返回 %d", rows);
		}
	    } finally {
		database.endTransaction();
		database.close();
	    }
	    return retval;
	}

	static public ArrayList<MemberProfile> listAll(Context context) {
	    EFormSQLite sqlite = EFormSQLite.getSQLite(context);
	    SQLiteDatabase database = sqlite.getWritableDatabase();

	    String[] columns = { COLUMN_ID, COLUMN_USERID, COLUMN_USERNAME,
		    COLUMN_COMPANY, COLUMN_PHONE, COLUMN_DATETIME };

	    Cursor cursor = database.query(TABLE_NAME, columns,
		    null, null, null, null, "_id ASC");

	    if (cursor == null || cursor.getCount() <= 0) {
		sqlite.close();
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
		profile.datetime = cursor.getLong(5);

		members.add(profile);
	    }
	    cursor.close();
	    sqlite.close();

	    return members;
	}

	static boolean hasUserWithRowid(Context context, long rowid) {
	    EFormSQLite sqlite = EFormSQLite.getSQLite(context);
	    SQLiteDatabase database = sqlite.getReadableDatabase();

	    Cursor cursor = database.query(TABLE_NAME, null,
		    "_id=?", new String[] { String.valueOf(rowid) },
		    null, null, null);

	    if (cursor == null) {
		sqlite.close();
		LogActivity.writeLog("查询 " + TABLE_NAME + " 错误，返回cursor == null");
		return false;
	    }

	    int count = cursor.getCount();

	    cursor.close();
	    sqlite.close();

	    return (count > 0) ? true : false;
	}
    }


    static public class Voucher
    {
	static final public String TABLE_NAME = "voucher";

	static final public String COLUMN_ID = "_id";
	static final public String COLUMN_USERID = "userid";
	static final public String COLUMN_COMMENT = "comment";
	static final public String COLUMN_FORMCLASS = "formclass";
	static final public String COLUMN_FORMLABEL = "formlabel";
	static final public String COLUMN_FORMIMAGE = "formimage";
	static final public String COLUMN_CONTENTS = "contents";
	static final public String COLUMN_CTIME = "ctime";
	static final public String COLUMN_MTIME = "mtime";
	static final public String COLUMN_ATIME = "atime";

	static public long insert(Context context, String member_password,
		long userid, String formclass, String formlabel, int formimage,
		String contents, String comment) {
	    if (comment == null || formclass == null ||
		    formlabel == null || contents == null) {
		LogActivity.writeLog("插入凭证失败，因为参数错误.");
		return -1;
	    }
	    if (!Member.hasUserWithRowid(context, userid)) {
		LogActivity.writeLog("插入凭证失败，因为会员 %d 不存在", userid);
		return -1;
	    }
	    if (member_password == null) {
		LogActivity.writeLog("插入凭证失败，未提供会员密码");
		return -1;
	    }

	    EFormSQLite sqlite = EFormSQLite.getSQLite(context);
	    SQLiteDatabase database = sqlite.getWritableDatabase();

	    ContentValues values = new ContentValues();

	    values.put(COLUMN_USERID, userid);
	    values.put(COLUMN_FORMCLASS, formclass);
	    values.put(COLUMN_FORMLABEL, formlabel);
	    values.put(COLUMN_FORMIMAGE, context.getResources().getResourceName(formimage));
	    try {
		byte[] utf8_contents = contents.getBytes("utf-8");
		values.put(COLUMN_CONTENTS, AESEncrypt(MD5Hash(member_password), utf8_contents));
	    } catch (UnsupportedEncodingException e) {
		LogActivity.writeLog(e);
		sqlite.close();
		return -1;
	    }
	    values.put(COLUMN_COMMENT, comment);

	    long currtime = System.currentTimeMillis();
	    values.put(COLUMN_CTIME, currtime);
	    values.put(COLUMN_MTIME, currtime);
	    values.put(COLUMN_ATIME, currtime);

	    long rowid = database.insert(TABLE_NAME, null, values);
	    sqlite.close();
	    return rowid;
	}

	static public boolean update(Context context, String member_password,
		long rowid, long userid, String contents, String comment) {
	    if (!Member.hasUserWithRowid(context, userid)) {
		LogActivity.writeLog("更新凭证失败，因为会员 %d 不存在", userid);
		return false;
	    }
	    if (contents != null && member_password == null) {
		LogActivity.writeLog("更新凭证失败，没有提供会员密码");
		return false;
	    }

	    ContentValues values = new ContentValues();

	    if (contents != null && contents.length() > 0) {
		try {
		    byte[] utf8_contents = contents.getBytes("utf-8");
		    values.put(COLUMN_CONTENTS,
			    AESEncrypt(MD5Hash(member_password), utf8_contents));
		} catch (UnsupportedEncodingException e) {
		    LogActivity.writeLog(e);
		    return false;
		}
	    }
	    if (comment != null && comment.length() > 0) {
		values.put(COLUMN_COMMENT, comment);
	    }

	    if (values.size() == 0) {
		LogActivity.writeLog("凭证没有需要更新的数据");
		return true;
	    }
	    long currtime = System.currentTimeMillis();
	    values.put(COLUMN_MTIME, currtime);
	    values.put(COLUMN_ATIME, currtime);

	    EFormSQLite sqlite = EFormSQLite.getSQLite(context);
	    SQLiteDatabase database = sqlite.getWritableDatabase();

	    int rows = database.update(TABLE_NAME, values, "_id=?",
		    new String[] { String.valueOf(rowid) });
	    sqlite.close();
	    return (rows == 1) ? true : false;
	}

	static public ArrayList<com.cansiny.eform.Voucher> listForUser(Context context, String member_password, long userid) {
	    if (userid < 0) {
		LogActivity.writeLog("不能得到用户的凭条，用户ID必须大于0");
		return null;
	    }
	    if (member_password == null) {
		LogActivity.writeLog("不能得到用户的凭条，未提供会员密码");
		return null;
	    }

	    EFormSQLite sqlite = EFormSQLite.getSQLite(context);
	    SQLiteDatabase database = sqlite.getWritableDatabase();

	    String[] columns = { COLUMN_ID, COLUMN_FORMCLASS, COLUMN_FORMLABEL,
		    COLUMN_FORMIMAGE, COLUMN_CONTENTS, COLUMN_COMMENT,
		    COLUMN_CTIME, COLUMN_MTIME, COLUMN_ATIME };

	    Cursor cursor = database.query(TABLE_NAME, columns,
		    "userid=?", new String[] { String.valueOf(userid) },
		    null, null, "atime DESC");

	    if (cursor == null || cursor.getCount() <= 0) {
		sqlite.close();
		return null;
	    }

	    ArrayList<com.cansiny.eform.Voucher> vouchers = new ArrayList<com.cansiny.eform.Voucher>();
	    while(cursor.moveToNext()) {
		try {
		    byte[] utf8_contents = AESDecrypt(MD5Hash(member_password), cursor.getBlob(4));
		    if (utf8_contents == null) {
			LogActivity.writeLog("解密会员数据失败");
			continue;
		    }
		    int image = context.getResources().getIdentifier(cursor.getString(3), null, null);

		    vouchers.add(new com.cansiny.eform.Voucher(
			    cursor.getLong(0), userid, cursor.getString(1),
			    cursor.getString(2), image,
			    new String(utf8_contents, "utf-8"), cursor.getString(5),
			    cursor.getLong(6), cursor.getLong(7), cursor.getLong(8)));
		} catch (UnsupportedEncodingException e) {
		    LogActivity.writeLog(e);
		    continue;
		}
	    }
	    cursor.close();
	    sqlite.close();

	    return vouchers;
	}

	static public boolean updateAccesstime(Context context, long rowid) {
	    EFormSQLite sqlite = EFormSQLite.getSQLite(context);
	    SQLiteDatabase database = sqlite.getWritableDatabase();

	    ContentValues values = new ContentValues();
	    values.put(COLUMN_ATIME, System.currentTimeMillis());

	    int rows = database.update(TABLE_NAME, values, "_id=?",
		    new String[] { String.valueOf(rowid) });
	    sqlite.close();
	    return (rows == 1) ? true : false;
	}

	static public boolean delete(Context context, long rowid) {
	    EFormSQLite sqlite = EFormSQLite.getSQLite(context);
	    SQLiteDatabase database = sqlite.getWritableDatabase();

	    int rows = database.delete(TABLE_NAME, "_id=?",
		    new String[] { String.valueOf(rowid) });
	    database.close();

	    return (rows == 1) ? true : false;
	}

    }

}
