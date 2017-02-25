package aiyuan1996.cn.firerunning.Utils;

import android.content.Context;

public class UserService 
{
	
	private static Database ud;

	public UserService(Context context)
	{
		ud = new Database(context);
	}

	//添加MAC
	public  String AddMAC(	String mac0,String mac1,String mac2,String mac3,String mac4,
							String mac5,String mac6,String mac7,String mac8,String mac9)
	{
		String s = ud.AddMAC(mac0,mac1,mac2,mac3,mac4,mac5,mac6,mac7,mac8,mac9);
		return s;
	}
	//添加坐标
	public  String AddLocation(	int rssi0,int rssi1,int rssi2,int rssi3,int rssi4,
								int rssi5,int rssi6,int rssi7,int rssi8,int rssi9,
								double left,double top)
	{
		String s = ud.AddLocation(rssi0,rssi1,rssi2,rssi3,rssi4,rssi5,rssi6,rssi7,rssi8,rssi9,left,top);
		return s;
	}
	//查找下标
	public int FindSub(String keyword)
	{
		int sub=ud.FindSub(keyword);
		return sub;
	}
	//flag
	public boolean Flag()
	{
		boolean flag=ud.Flag();
		return flag;
	}
	//查找左坐标记录
	public  String FindLeft(String colu,int level)
	{
		return ud.FindLeft(colu,level);
	}
	//查找上坐标记录
	public  String FindTop(String colu,int level)
	{
		return ud.FindTop(colu,level);
	}
}
