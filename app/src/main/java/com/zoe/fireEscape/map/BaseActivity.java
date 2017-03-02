package com.ZOE.FireEscape.map;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.ZOE.FireEscape.R;
import com.ZOE.FireEscape.Utils.ActivityCollector;
import com.ZOE.FireEscape.Utils.FileUtils;
import com.ZOE.FireEscape.Utils.ViewHelper;
import com.ZOE.FireEscape.widget.NavigationBar;
import com.fengmap.android.analysis.navi.FMNaviAnalyser;
import com.fengmap.android.analysis.navi.FMNaviResult;
import com.fengmap.android.data.OnFMDownloadProgressListener;
import com.fengmap.android.exception.FMObjectException;
import com.fengmap.android.map.FMMap;
import com.fengmap.android.map.FMMapUpgradeInfo;
import com.fengmap.android.map.FMMapView;
import com.fengmap.android.map.event.OnFMMapInitListener;
import com.fengmap.android.map.geometry.FMMapCoord;
import com.fengmap.android.map.layer.FMImageLayer;
import com.fengmap.android.map.layer.FMLineLayer;
import com.fengmap.android.map.marker.FMImageMarker;
import com.fengmap.android.map.marker.FMLineMarker;
import com.fengmap.android.map.marker.FMSegment;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * @Email hezutao@fengmap.com
 * @Version 2.0.0
 * @Description 基类
 */
public abstract class BaseActivity extends Activity implements OnFMMapInitListener ,
                                NavigationView.OnNavigationItemSelectedListener {
    /**
     * 线图层
     */
    protected FMLineLayer mLineLayer;
    /**
     * 导航分析
     */
    protected FMNaviAnalyser mNaviAnalyser;
    /**
     * 地图视图
     */
    protected FMMapView mMapView;
    /**
     * 地图控制
     */
    protected FMMap mFMMap;
    /**
     * 起点坐标
     */
    protected FMMapCoord stCoord;
    /**
     * 起点楼层
     */
    protected int stGroupId;
    /**
     * 起点图层
     */
    protected FMImageLayer stImageLayer;
    /**
     * 终点坐标
     */
    protected FMMapCoord endCoord1,endCoord2;
    /**
     * 终点楼层id
     */
    protected int endGroupId;
    /**
     * 终点图层
     */
    protected FMImageLayer endImageLayer;
    private DrawerLayout drawer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.drawer);
        ActivityCollector.addActivity(this);
        setTitle();
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
//
       NavigationView navigationView = (NavigationView) findViewById(R.id.drawer_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public void openDrawer()
    {
        drawer.openDrawer(Gravity.LEFT);
    }
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return true;
    }

    protected void setTitle() {
        NavigationBar navigationBarView = ViewHelper.getView(BaseActivity.this, R.id.navigation_bar);
        navigationBarView.setTitle("地图显示");
    }
    /**
     * 加载地图数据
     */
    protected void openMapByPath() {
        mMapView = (FMMapView) findViewById(R.id.map_view);
        if (mMapView == null) {
            throw new NullPointerException("not defined id map_view");
        }
        mFMMap = mMapView.getFMMap();
        mFMMap.setOnFMMapInitListener(this);
        //加载地图数据
        mFMMap.openMapById("xust-18-2",true);
    }

    /**
     * 添加布局
     *
     * @param layoutId 资源id
     */
    public void setContentView(int layoutId) {
        View view = View.inflate(getBaseContext(), layoutId, null);
        //View view = View.inflate(getApplicationContext(), layoutId, null);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        lp.addRule(RelativeLayout.BELOW, R.id.navigation_bar);
        RelativeLayout viewGroup = (RelativeLayout) findViewById(R.id.layout_root);
        viewGroup.addView(view, lp);
        openMapByPath();
    }

    @Override
    public void onMapInitSuccess(String path) {
        //加载离线主题文件
        mFMMap.loadThemeByPath(FileUtils.getDefaultThemePath(this));
        //线图层
        mLineLayer = mFMMap.getFMLayerProxy().getFMLineLayer();
        mFMMap.addLayer(mLineLayer);

        //导航分析
        try {
            mNaviAnalyser = FMNaviAnalyser.getFMNaviAnalyserById("xust-18-2");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (FMObjectException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onMapInitFailure(String path, int errorCode) {

    }

    /**
     * 当{@link FMMap#openMapById(String, boolean)}设置openMapById(String, false)时地图不自动更新会
     * 回调此事件，可以调用{@link FMMap#upgrade(FMMapUpgradeInfo, OnFMDownloadProgressListener)}进行
     * 地图下载更新
     *
     * @param upgradeInfo 地图版本更新详情,地图版本号{@link FMMapUpgradeInfo#getVersion()},<br/>
     *                    地图id{@link FMMapUpgradeInfo#getMapId()}
     * @return 如果调用了{@link FMMap#upgrade(FMMapUpgradeInfo, OnFMDownloadProgressListener)}地图下载更新，
     * 返回值return true,因为{@link FMMap#upgrade(FMMapUpgradeInfo, OnFMDownloadProgressListener)}
     * 会自动下载更新地图，更新完成后会加载地图;否则return false。
     */
    @Override
    public boolean onUpgrade(FMMapUpgradeInfo upgradeInfo) {
        //TODO 获取到最新地图更新的信息，可以进行地图的下载操作
        return false;
    }
//
//    @Override
//    public void onBackPressed() {
//        if (mFMMap != null) {
//            mFMMap.onDestroy();
//        }
//        super.onBackPressed();
//    }

    /**
     * 清理所有的线与图层
     */
    protected void clear() {
        clearLineLayer();
        clearStartImageLayer();
        clearEndImageLayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }

    /**
     * 清除线图层
     */
    protected void clearLineLayer() {
        if (mLineLayer != null) {
            mLineLayer.removeAll();
        }
    }

    /**
     * 清除起点图层
     */
    protected void clearStartImageLayer() {
        if (stImageLayer != null) {
            stImageLayer.removeAll();
            mFMMap.removeLayer(stImageLayer); // 移除图层
            stImageLayer = null;
        }
    }

    /**
     * 清除终点图层
     */
    protected void clearEndImageLayer() {
        if (endImageLayer != null) {
            endImageLayer.removeAll();
            mFMMap.removeLayer(endImageLayer); // 移除图层

            endImageLayer = null;
        }
    }


    /**
     *  添加线标注
     */
    protected void addLineMarker() {
        ArrayList<FMNaviResult> results = mNaviAnalyser.getNaviResults();
        // 填充导航数据
        ArrayList<FMSegment> segments = new ArrayList<>();
        for (FMNaviResult r : results) {
            int groupId = r.getGroupId();
            FMSegment s = new FMSegment(groupId, r.getPointList());
            segments.add(s);
        }
        //添加LineMarker
        FMLineMarker lineMarker = new FMLineMarker(segments);
        lineMarker.setLineWidth(3f);
        mLineLayer.addMarker(lineMarker);
    }

    /**
     * 创建起点图标
     */
    protected void createStartImageMarker() {
        clearStartImageLayer();
        // 添加起点图层
        stImageLayer = new FMImageLayer(mFMMap, stGroupId);
        mFMMap.addLayer(stImageLayer);
        // 标注物样式
        FMImageMarker imageMarker = ViewHelper.buildImageMarker(getResources(), stCoord, R.drawable.start);
        stImageLayer.addMarker(imageMarker);
    }

    /**
     * 创建终点图层
     */
    protected void createEndImageMarker(FMMapCoord endCoord) {
        clearEndImageLayer();
        // 添加起点图层
        endImageLayer = new FMImageLayer(mFMMap, endGroupId);
        mFMMap.addLayer(endImageLayer);
        // 标注物样式
        FMImageMarker imageMarker = ViewHelper.buildImageMarker(getResources(), endCoord, R.drawable.end);
        endImageLayer.addMarker(imageMarker);
    }

}
