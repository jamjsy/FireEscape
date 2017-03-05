package com.ZOE.FireEscape.Utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper
{

	
	//声明数据库的版本一般为整数即可；
	private static final int DBVERSION = 1;
	
	//用户和管理员的账户所在的数据库；
	private static final String DBNAME = "WiFiFingerPrint.db";
	
	//用户的属性：int id(数据表中的主键),String name(唯一的不可重复),String pass(不能为空), int item(默认值是0),
	private String creatTableMAC = "create table mac(id integer primary key autoincrement,mac varchar(20) not null);";
	private String creatTableLocation = "create table rssi(id integer primary key autoincrement," +
																"x    default 0," +
																"y    default 0);";
	//重载类方法；
	public DBHelper(Context context)
	{
		this(context, DBNAME, null, DBVERSION);
	}
	
	//这个方法是继承时自动生成的方法：
	public DBHelper(Context context, String name, CursorFactory factory, int version) 
	{
		super(context, name, factory, version);
	}
	
	//程序第一次运行的时候执行该方法：即创建数据库；
	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		db.execSQL(creatTableMAC);
		db.execSQL(creatTableLocation);
	}
	
	//数据库升级时执行该方法：
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		db.execSQL(creatTableMAC);
		db.execSQL(creatTableLocation);
	}
}