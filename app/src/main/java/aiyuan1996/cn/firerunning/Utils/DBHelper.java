package aiyuan1996.cn.firerunning.Utils;

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
	private String creatTableMAC = "create table mac(mac0 varchar(20) default null," +
													"mac1 varchar(20) default null," +
													"mac2 varchar(20) default null," +
													"mac3 varchar(20) default null," +
													"mac4 varchar(20) default null," +
													"mac5 varchar(20) default null," +
													"mac6 varchar(20) default null," +
													"mac7 varchar(20) default null," +
													"mac8 varchar(20) default null," +
													"mac9 varchar(20) default null);";
	private String creatTableLocation = "create table location(id integer primary key autoincrement," +
																"rssi0  default 0," +
																"rssi1  default 0," +
																"rssi2  default 0," +
																"rssi3  default 0," +
																"rssi4  default 0," +
																"rssi5  default 0," +
																"rssi6  default 0," +
																"rssi7  default 0," +
																"rssi8  default 0," +
																"rssi9  default 0," +
																"left  default 0," +
																"top  default 0)";
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