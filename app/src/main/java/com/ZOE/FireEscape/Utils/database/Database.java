package com.ZOE.FireEscape.Utils.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Database {
	//创建该类的一个对象——实例化；
	private DBHelper db;
	String TAG = "MAC";
	//创建一个SQLiteDatabase的对象——实例化；
	private SQLiteDatabase sd;

	public Database(Context context)
	{
		db = new DBHelper(context);	}

	//添加mac地址
	public boolean AddMAC(String mac)
	{
		sd = db.getWritableDatabase();
		Cursor c = sd.rawQuery("select * from mac", null);
		int flag=0;
		for(int i=0;i<c.getCount();i++) {
			c.moveToPosition(i);
			if (mac.equals(c.getString(c.getColumnIndex("mac")))) {
				flag = 1;
				break;
			}
		}
		if(flag==0)
		{
			try{
				sd.execSQL("insert into mac(mac) values(?);",new String[]{mac});
				sd.execSQL("alter table rssi add column rssi"+(c.getCount()+1)+" default 0");
			} catch(SQLiteConstraintException e){
				c.close();
				sd.close();
				return false;
			}
		}
		c.close();
		sd.close();
		return true;
	}

	//添加坐标
	public boolean AddCoord(double x,double y)
	{
		sd = db.getWritableDatabase();
		try
	{
		sd.execSQL("insert into rssi(x,y) values(?,?);", new Double[]{x,y});
	}catch(SQLiteConstraintException e){
		sd.close();
		return false;
	}
		sd.close();
		return true;
	}

	//添加rssi值)
	public boolean AddRssi(String mac,int rssi)
	{
		sd = db.getWritableDatabase();
		Cursor c = sd.rawQuery("select * from mac", null);
		int id=0;
		for(int i=0;i<c.getCount();i++) {
			c.moveToPosition(i);
			if (mac.equals(c.getString(c.getColumnIndex("mac")))) {
				id = i+1;
				break;
			}
		}
		c = sd.rawQuery("select * from rssi", null);
		//Log.d(TAG, "AddRssi: "+c.getCount());
		if(id!=0)
		{
			sd.execSQL("update rssi set rssi"+id+" = "+rssi+" where id = "+(c.getCount()));
		}
		c.close();
		sd.close();
		return true;
	}

	public Double[] GetPoint(int col){
		Double point[] = new Double[2];
		sd = db.getWritableDatabase();
		Log.d(TAG, "GetPoint: "+col);
		Cursor cus=null;
		try {
			cus = sd.rawQuery("select * from rssi where id=?", new String[]{col + ""});
			cus.moveToFirst();
			point[0] = cus.getDouble(cus.getColumnIndex("x"));
			point[1] = cus.getDouble(cus.getColumnIndex("y"));
			cus.close();
			sd.close();
		}catch(SQLiteConstraintException e){
			cus.close();
			sd.close();
		}
		return point;
	}
public  boolean init(){
	sd = db.getWritableDatabase();
	Cursor cus= sd.rawQuery("select x from rssi",null);
	if(cus.moveToNext()){
		cus.close();
		sd.close();
		return true;
	}
	cus.close();
	sd.close();
	return false;
}

}