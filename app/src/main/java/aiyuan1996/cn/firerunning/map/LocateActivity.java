package aiyuan1996.cn.firerunning.map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.fengmap.android.analysis.navi.FMNaviAnalyser;
import com.fengmap.android.map.geometry.FMMapCoord;

import java.util.List;

import aiyuan1996.cn.firerunning.R;
import aiyuan1996.cn.firerunning.Utils.UserService;


/**
 * @Email hezutao@fengmap.com
 * @Version 2.0.0
 * @Description 根据起点终点规划路径<br/>
 * <p>根据起始点规划线路，介绍导航{@link FMNaviAnalyser}的基础使用方法。在非地图页面进行路径规划时，
 * 可以使用{@link FMNaviAnalyser#getFMNaviAnalyserById(String)}传入mapId或者使用{@link FMNaviAnalyser#getFMNaviAnalyserByPath(String)}
 * 传入地图路径进行初始化</p>
 */
public class LocateActivity extends BaseActivity{

    double RouteLength1,RouteLength2;
    public UserService userService;
    //wifi服务
    private String wserviceName = Context.WIFI_SERVICE;
    private WifiManager wm;
    private TextView textview;
    //定义一个数组来存放结果
    private double[] resleft = new double[10];
    private double[] restop = new double[10];
    //定义初始坐标
    public  double left=0;
    public  double top=0;
    //定义一个数组，提高精确度
    private double[] positionleft = new double[10];
    private double[] positiontop = new double[10];
    private double correctL=12150000;
    private double correctT=4070000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate);
        userService = new UserService(this);
        //WiFiManager初始化
        wm = (WifiManager) getSystemService(wserviceName);
        wm.startScan();
        //注册WiFi监听器
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    private void SetStart(double x, double y) {
       // Log.d("是否执行", "onMapClick: "+x);
        // 获取屏幕点击位置的地图坐标
        // 起点
        if (stCoord == null) {
            stCoord=new FMMapCoord();
            clear();
            stCoord.x=x+correctL;
            stCoord.y=y+correctT;
            stCoord.z=0;
            stGroupId = 1;
            createStartImageMarker();
        }


        analyzeNavigation();
        //Log.d("距离", ""+sceneRouteLength);
        // 画完置空
        stCoord = null;
        endCoord1 = null;
        endCoord2 = null;
    }

    /**
     * 开始分析导航
     */
    private void analyzeNavigation() {
        // 终点有两个
        //Log.d("起点", "analyzeNavigation: "+stCoord.x+"    "+stCoord.y);
        if (endCoord1 == null && endCoord2==null) {
            endCoord1=new FMMapCoord();
            endCoord2=new FMMapCoord();
            //大楼梯口对应地图的坐标
            endCoord1.x=12154613;
            endCoord1.y=4078530;
            endCoord1.z=0;
            //小楼梯口
            endCoord2.x=12154704;
            endCoord2.y=4078530;
            endCoord2.z=0;

            endGroupId = stGroupId;
        }
         /**
         * 从两条出口选择距离较近的一条
         *
         */
        int type1 = mNaviAnalyser.analyzeNavi(stGroupId, stCoord, endGroupId, endCoord1,
                FMNaviAnalyser.FMNaviModule.MODULE_SHORTEST);
        RouteLength1=mNaviAnalyser.getSceneRouteLength();
        int type2 = mNaviAnalyser.analyzeNavi(stGroupId, stCoord, endGroupId, endCoord2,
                FMNaviAnalyser.FMNaviModule.MODULE_SHORTEST);
        RouteLength2=mNaviAnalyser.getSceneRouteLength();
        Log.d("出错原因", "analyzeNavigation: "+type1+""+type2);
        if (    type1 == FMNaviAnalyser.FMRouteCalcuResult.ROUTE_SUCCESS &&
                type2 == FMNaviAnalyser.FMRouteCalcuResult.ROUTE_SUCCESS) {
            if (RouteLength1>=RouteLength2) {
                createEndImageMarker(endCoord2);
                addLineMarker();
            }
            else
            {
                mNaviAnalyser.analyzeNavi(stGroupId, stCoord, endGroupId, endCoord1,
                    FMNaviAnalyser.FMNaviModule.MODULE_SHORTEST);
                createEndImageMarker(endCoord1);
                addLineMarker();}
        }
    }

    //找出数组中出现次数最多的数
    private double FindMost(double[] array)
    {
        double[] temparray={0,0,0,0,0,0,0,0,0,0};
        for(int i=0;i<10;i++)
        {
            for(int j=0;j<10;j++)
            {
                if(array[i]==array[j] && array[i]!=0)
                    temparray[i]++;
            }
        }

        //找出出现频率较高的坐标值
        int temp=0;
        for(int i=0;i<10;i++)
        {
            if(temparray[i]>temp)
                temp=i;
        }

        return array[temp];
    }

    //广播
    private final BroadcastReceiver wifiReceiver = new BroadcastReceiver()
    {

        @Override
        public void onReceive(Context arg0, Intent intent)
        {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
            {
                List<ScanResult> results = wm.getScanResults();
                for (ScanResult result : results) {
                    if(userService.FindSub(result.BSSID)>=0)
                    {
                        resleft[userService.FindSub(result.BSSID)]=Double.valueOf(	userService.FindLeft("rssi"+userService.FindSub(result.BSSID),
                                result.level) );
                        restop[userService.FindSub(result.BSSID)]=Double.valueOf(	userService.FindTop("rssi"+userService.FindSub(result.BSSID),
                                result.level) );
                    }
                }

                left=FindMost(resleft);
                top=FindMost(restop);
                Log.d("坐标", ""+left+"    "+top);
                //将position左右的每个元素向后移动一位
                for(int i=9;i>0;i--)
                {
                    positionleft[i]=positionleft[i-1];
                    positiontop[i]=positiontop[i-1];
                }
                positionleft[0]=left;
                positiontop[0]=top;
                left=FindMost(positionleft);
                top=FindMost(positiontop);
                SetStart(left,top);
                wm.startScan();
            }
        }
    };
    @Override
    public void onBackPressed() {
        unregisterReceiver(wifiReceiver);
        super.onBackPressed();
    }

}
