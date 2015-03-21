package com.heatclub.moro.db;


import android.database.sqlite.SQLiteDatabase;
import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;


public class MoroBase
{
	public static SQLiteDatabase db;
	public static ContentValues cv;
	
	public MoroBase(Context context){
		db = new MoroHelper(context).getWritableDatabase();
		cv = new ContentValues();
		
	}
	
	public void insertNumber(String phoneNumber){
		long curTime = System.currentTimeMillis(); 
		cv.put(MoroHelper.NUMBER, phoneNumber);
		cv.put(MoroHelper.CALL_TIME, curTime);
		// вызываем метод вставки
		db.insert(MoroHelper.CALLS_TABLE_NAME, MoroHelper.NUMBER, cv);
		cv.clear();
		
	}
	
	public boolean saveAppKey(String key){
		boolean result;
		cv.put(MoroHelper.KEY, "app_key");
		cv.put(MoroHelper.VALUE, key);
		// вызываем метод вставки
		if(db.insert(MoroHelper.SETTING_TABLE_NAME, MoroHelper.KEY, cv)!=0)
			result = true;	
		else
			result = false;
			
		cv.clear();
		
		return result;	

	}
	
	public String readAppKey(){
		// вызываем метод чтения
		Cursor cursor = db.query(MoroHelper.SETTING_TABLE_NAME, null,  "key = ?", new String[] {"app_key"}, null, null, null);
		while (cursor.moveToNext()) {
			// GET COLUMN INDICES + VALUES OF THOSE COLUMNS
			int id = cursor.getInt(cursor.getColumnIndex(MoroHelper.SUID));
			String result = cursor.getString(cursor
										   .getColumnIndex(MoroHelper.VALUE));
			if(!result.isEmpty())
				return result;
		}
		cursor.close();
		return null;
	}
	
	
	
}
