package com.ZOE.FireEscape.map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ZOE.FireEscape.R;
import com.ZOE.FireEscape.Utils.ActivityCollector;
import com.ZOE.FireEscape.Utils.Database;
import com.fengmap.android.analysis.navi.FMNaviAnalyser;
import com.fengmap.android.map.FMPickMapCoordResult;
import com.fengmap.android.map.event.OnFMMapClickListener;
import com.fengmap.android.map.geometry.FMMapCoord;

import java.util.List;


/**
 * @Email hezutao@fengmap.com
 * @Version 2.0.0
 * @Description 根据起点终点规划路径<br/>
 * <p>根据起始点规划线路，介绍导航{@link FMNaviAnalyser}的基础使用方法。在非地图页面进行路径规划时，
 * 可以使用{@link FMNaviAnalyser#getFMNaviAnalyserById(String)}传入mapId或者使用{@link FMNaviAnalyser#getFMNaviAnalyserByPath(String)}
 * 传入地图路径进行初始化</p>
 */
public class GetDataActivity extends BaseActivity implements OnFMMapClickListener {

    private  double RouteLength1,RouteLength2;
    //wifi对象的定义及实现
    private String wserviceName = Context.WIFI_SERVICE;
    private WifiManager wm;
    int flag=0;
    //矫正插入数据的临时变量
    private int[][] correct= new int[10][10];
    //将坐标及rssi信息存入表location中
    int[] location={0,0,0,0,0,0,0,0,0,0};
    //Runable中的变量
    int n = 0;
    //定义初始坐标
    private double left=0,top=0;
    private double correctL=12150000;
    private double correctT=4070000;
    Database db;
    //立一个flag看当前采样是否完毕
    boolean flagGetDataFinished=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_getdata);
        ActivityCollector.addActivity(this);
        mFMMap.setOnFMMapClickListener(this);
        db=new Database(this);
        wm = (WifiManager) getSystemService(wserviceName);
        //wm.startScan();
    }

    @Override
    public void onMapClick(float x, float y) {
        // 置空
        stCoord = null;
        endCoord1 = null;
        endCoord2 = null;
        // 获取屏幕点击位置的地图坐标
        final FMPickMapCoordResult mapCoordResult = mFMMap.pickMapCoord(x, y);
        if (mapCoordResult == null) {
            return;
        }
        // 起点
        if (stCoord == null) {
            clear();

            stCoord = mapCoordResult.getMapCoord();
            left=stCoord.x-correctL;
            top=stCoord.y-correctT;
            Log.d("存入的坐标", ""+left+"    "+top);
            stGroupId = mapCoordResult.getGroupId();
            createStartImageMarker();
           // return;
        }


        analyzeNavigation();
        //Log.d("距离", ""+sceneRouteLength);
    }

    /**
     * 开始分析导航
     */
    private void analyzeNavigation() {
        // 终点有两个
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
        //Log.d("出错原因", "analyzeNavigation: "+type);
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

    //
    public void OnGetDataClick(View v)
    {
        if(stCoord==null){
            Toast.makeText(this, "请先在地图上指出您当前所处坐标", Toast.LENGTH_SHORT).show();
            return;
        }
        wm.startScan();
        //注册监听事件
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    //注册广播事件
    private final BroadcastReceiver wifiReceiver = new BroadcastReceiver()
    {

        @Override
        public void onReceive(Context arg0, Intent intent)
        {

            flagGetDataFinished=false;
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        {
            wm.startScan();
            List<ScanResult> results = wm.getScanResults();
            //mac地址查重和插入
            for (ScanResult result : results)
            {
            db.AddMAC(result.BSSID);
        }
            db.AddCoord(left,top);
            for (ScanResult result : results)
            {
                db.AddRssi(result.BSSID,result.level);
            }
            unregisterReceiver(wifiReceiver);
            Toast.makeText(GetDataActivity.this, "插入成功", Toast.LENGTH_SHORT).show();
            flagGetDataFinished=true;
        }
        }

    };
    public boolean isGetdataFinished()
    {
        return flagGetDataFinished;
    }
    //q求二维数组第n列的平均值
    public int Average(int[][] array,int n)
    {

        //去掉一个最高值，一个最低值，再求平均
        int max,min,num,total;
        //提高精确度，将存入矫正变量的值做多次修正
        max=-200;
        min=0;
        num=0;
        total=0;
        //找出最大最小
        for(int j=0;j<10;j++)
        {
            if(array[j][n]>max)
                max=array[j][n];
            if(array[j][n]<min)
                min=array[j][n];
        }
        //去掉最大最小，并统计有效元素个数
        for(int j=0;j<10;j++)
        {
            if(array[j][n]!=0 && array[j][n]!=max && array[j][n]!=min)
            {
                total+=array[j][n];
                num++;
            }
        }
        if(num==0)
        {
            num=1;
            return (max+min)/2;
        }
        else
        {

            return total/num;
        }
    }
//    @Override
//    public void onBackPressed() {
//        if(flag!=0)
//            unregisterReceiver(wifiReceiver);
//        super.onBackPressed();
//    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
    @Override
    public void onBackPressed()
    {
        mFMMap.onDestroy();
        LocateActivity.init=true;
        startActivity(new Intent(GetDataActivity.this,LocateActivity.class));
        this.finish();
    }
}
