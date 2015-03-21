package com.heatclub.moro.db;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MoroHelper extends SQLiteOpenHelper{

	private static final String DATABASE_NAME = "moro.db";
	private static final int DATABASE_VERSION = 1;
	
	public static final String CALLS_TABLE_NAME = "calls";
	public static final String CUID = "_id";
	public static final String NUMBER = "phone_number";
	public static final String CALL_TIME = "call_time";
	
	public static final String SETTING_TABLE_NAME = "setting";
	public static final String SUID = "_id";
	public static final String KEY = "key";
	public static final String VALUE = "value";
	

	private static final String SQL_CREATE_CALLS_TABLE = "CREATE TABLE "
	+ CALLS_TABLE_NAME + " (" + CUID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
	+ NUMBER + " VARCHAR(255), "+CALL_TIME+" LONG);";

	private static final String SQL_DELETE_CALLS_TABLE = "DROP TABLE IF EXISTS "
	+ CALLS_TABLE_NAME;

	private static final String SQL_CREATE_SETTING_TABLE = "CREATE TABLE "
	+ SETTING_TABLE_NAME + " (" + SUID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
	+ KEY + " VARCHAR(25), "+VALUE+" VARCHAR(512));";

	private static final String SQL_DELETE_SETTING_TABLE = "DROP TABLE IF EXISTS "
	+ SETTING_TABLE_NAME;

	
	public MoroHelper(Context context) {
		// TODO Auto-generated constructor stub
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(SQL_CREATE_CALLS_TABLE);
		db.execSQL(SQL_CREATE_SETTING_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		Log.w("LOG_TAG", "Обновление базы данных с версии " + oldVersion
			  + " до версии " + newVersion + ", которое удалит все старые данные");
		// Удаляем предыдущую таблицу при апгрейде
		db.execSQL(SQL_DELETE_CALLS_TABLE);
		db.execSQL(SQL_DELETE_SETTING_TABLE);
		
		// Создаём новый экземпляр таблицы
		onCreate(db);
	}
}
