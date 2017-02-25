package aiyuan1996.cn.firerunning.Utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

public class Database {
	//创建该类的一个对象——实例化；
	private DBHelper db;
	
	
	//创建一个SQLiteDatabase的对象——实例化；
	private SQLiteDatabase sd;
	
	//向数据表中添加数据的格式（SQLite3语句）;
	private String insertMAC = "insert into mac(mac0,mac1,mac2,mac3,mac4," +
								"mac5,mac6,mac7,mac8,mac9) " +
								"values(?,?,?,?,?,?,?,?,?,?);";

	private String insertLocation = "insert into location(rssi0,rssi1,rssi2,rssi3,rssi4," +
									"rssi5,rssi6,rssi7,rssi8,rssi9,left,top) " +
									"values(?,?,?,?,?,?,?,?,?,?,?,?);";
	public Database(Context context)
	{
		db = new DBHelper(context);
	}

	//添加mac地址
	public String AddMAC(String mac0,String mac1,String mac2,String mac3,String mac4,String mac5,String mac6,String mac7,String mac8,String mac9)
	{
		String s = "";
			sd = db.getWritableDatabase();
			try
			{
				sd.execSQL(insertMAC, new String[]{mac0,mac1,mac2,mac3,mac4,mac5,mac6,mac7,mac8,mac9});
				s = "初始化完成！";
			}catch(SQLiteConstraintException e){
				s = "初始化异常！";
			}
			sd.close();
		return s;
	}
	//添加rssi地址
	public String AddLocation(	double rssi0,double rssi1,double rssi2,double rssi3,double rssi4,
								  double rssi5,double rssi6,double rssi7,double rssi8,double rssi9,
								double left,double top)
	{
		String s = "";
			sd = db.getWritableDatabase();
			try
			{
				sd.execSQL(insertLocation, new Double[]{rssi0,rssi1,rssi2,rssi3,rssi4,
														rssi5,rssi6,rssi7,rssi8,rssi9,
														left,top});
				s = "插入成功！";
			}catch(SQLiteConstraintException e){
				s = "插入出错！";
			}
			sd.close();
		return s;
	}
	//查找下标
	public int FindSub(String keyword)
	{
		int sub = -1;
		sd = db.getWritableDatabase(); 
		Cursor c = sd.rawQuery("select * from mac", null);
		while(c.moveToNext())
        {
			if(keyword.equals(c.getString(c.getColumnIndex("mac0"))))
			{
				sub=0;
			}
			else if(keyword.equals(c.getString(c.getColumnIndex("mac1"))))
			{
				sub=1;
			}
			else if(keyword.equals(c.getString(c.getColumnIndex("mac2"))))
			{
				sub=2;
			}
			else if(keyword.equals(c.getString(c.getColumnIndex("mac3"))))
			{
				sub=3;
			}
			else if(keyword.equals(c.getString(c.getColumnIndex("mac4"))))
			{
				sub=4;
			}
			else if(keyword.equals(c.getString(c.getColumnIndex("mac5"))))
			{
				sub=5;
			}
			else if(keyword.equals(c.getString(c.getColumnIndex("mac6"))))
			{
				sub=6;
			}
			else if(keyword.equals(c.getString(c.getColumnIndex("mac7"))))
			{
				sub=7;
			}
			else if(keyword.equals(c.getString(c.getColumnIndex("mac8"))))
			{
				sub=8;
			}
			else if(keyword.equals(c.getString(c.getColumnIndex("mac9"))))
			{
				sub=9;
			}
        }
        c.close();
        sd.close();
		return sub;
	}
	//flag
	public boolean Flag()
	{
		boolean flag = false;
		sd = db.getWritableDatabase(); 
		Cursor c = sd.rawQuery("select * from mac", null);
		if(c.moveToNext())
			flag=true;
        c.close();
        sd.close();
		return flag;

	}
	//左坐标查找
	public String FindLeft(String colu,int level)
	{
		String s = "";
		sd = db.getWritableDatabase(); 
		Cursor c = sd.rawQuery("select * from location", null);
		double temp=100;
		while(c.moveToNext())
        {
			if( Math.abs( c.getDouble(c.getColumnIndex(colu)) - level) < temp)
        	{
        		temp=Math.abs( c.getDouble(c.getColumnIndex(colu)) - level);
        		s=c.getString(c.getColumnIndex("left"));
        	}
        }

        c.close();
        sd.close();
        return s;//+"1001"+temp;
	}
	//上坐标查找
	public String FindTop(String colu,int level)
	{
		String s = "";
		sd = db.getWritableDatabase(); 
		Cursor c = sd.rawQuery("select * from location", null);
		double temp=100;
		
		while(c.moveToNext())
        {
			if( Math.abs( c.getDouble(c.getColumnIndex(colu)) - level) < temp)
        	{
        		temp=Math.abs( c.getDouble(c.getColumnIndex(colu)) - level);
        		s=c.getString(c.getColumnIndex("top"));
        	}
        }
        c.close();
        sd.close();
        return s;
	}
}