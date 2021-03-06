package com.ZOE.FireEscape.ui.Map;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.ZOE.FireEscape.R;
import com.ZOE.FireEscape.Utils.ActivityCollector;
import com.ZOE.FireEscape.Utils.LocateMethutils;
import com.ZOE.FireEscape.Utils.PushUtil;
import com.ZOE.FireEscape.Utils.ToastUtils;
import com.ZOE.FireEscape.Utils.database.Database;
import com.ZOE.FireEscape.entity.UserEntity;
import com.ZOE.FireEscape.ui.ContactActivity;
import com.ZOE.FireEscape.ui.PushActivity.webViewActivity;
import com.ZOE.FireEscape.ui.SettingsActivity;
import com.ZOE.FireEscape.widget.MyDialog;
import com.bumptech.glide.Glide;
import com.fengmap.android.analysis.navi.FMNaviAnalyser;
import com.fengmap.android.map.geometry.FMMapCoord;

import org.litepal.tablemanager.Connector;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * @Email hezutao@fengmap.com
 * @Version 2.0.0
 * @Description 根据起点终点规划路径<br/>
 * <p>根据起始点规划线路，介绍导航{@link FMNaviAnalyser}的基础使用方法。在非地图页面进行路径规划时，
 * 可以使用{@link FMNaviAnalyser#getFMNaviAnalyserById(String)}传入mapId或者使用{@link FMNaviAnalyser#getFMNaviAnalyserByPath(String)}
 * 传入地图路径进行初始化</p>
 */
public class LocateActivity extends BaseActivity{

    private static final String TAG = "LocateActivity";
     public static boolean init=false;
    double RouteLength1,RouteLength2;
    //wifi服务
    private String wserviceName = Context.WIFI_SERVICE;
    private WifiManager wm;
    private double correctL=12150000;
    private double correctT=4070000;
    private LocateMethutils meutils;
    private long mExitTime = 0;
    CircleImageView userImage;
    TextView tel;
    private android.support.v7.app.AlertDialog photoDialog;
    public static final String PHOTO_IMAGE_FILE_NAME = "fileImg.jpg";
    public static final int CAMERA_REQUEST_CODE = 100;
    public static final int IMAGE_REQUEST_CODE = 101;
    public static final int RESULT_REQUEST_CODE = 102;
    private static final int REQUEST_BLUETOOTH_PERMISSION = 10;
    private File tempFile = null;
    public static boolean isForeground = false;
    private Database db;
    private Double point[]=new Double[2];
    private boolean isHere=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate);
        ActivityCollector.addActivity(this);

        //初始化数据库
        Connector.getDatabase();

        //WiFiManager初始化
        wm = (WifiManager) getSystemService(wserviceName);
        db=new Database(this);
        wm.startScan();
        //注册WiFi监听器
        if(db.init())
        {
            registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.drawer_view);
        navigationView.setNavigationItemSelectedListener(this);
        View navHeaderView = navigationView.inflateHeaderView(R.layout.nav_header_main);
        userImage = (CircleImageView)navHeaderView.findViewById(R.id.profile_image);
        String telString = getIntent().getStringExtra("tel");
        tel = (TextView)navHeaderView.findViewById(R.id.tel);
        tel.setText(telString);
        meutils=new LocateMethutils(this);
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
        initview();
        registerMessageReceiver();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent;
        if (id == R.id.nav_contacts) {
            startActivity(new Intent(LocateActivity.this, ContactActivity.class));
        } else if (id == R.id.nav_getdata) {
            mFMMap.onDestroy();
            startActivity(new Intent(LocateActivity.this,GetDataActivity.class));
            if(db.init()&&isHere)
                unregisterReceiver(wifiReceiver);
            unregisterReceiver(mMessageReceiver);
            this.finish();
        }else if (id == R.id.nav_push) {
            intent = new Intent(LocateActivity.this,webViewActivity.class);
            startActivity(intent);
            //mFMMap.onDestroy();

        } else if (id == R.id.nav_settings) {
            intent = new Intent(LocateActivity.this,SettingsActivity.class);
            startActivity(intent);
            //mFMMap.onDestroy();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void initview() {
        if (BmobUser.getCurrentUser()!=null) {
            Log.d(TAG, "BmobUser.getCurrentUser()!=null");
            UserEntity userEntity = BmobUser.getCurrentUser(UserEntity.class);
            if (userEntity.getAvatar()!=null) {
                Log.d(TAG, "userEntity.getAvatar()!=null");
                if(userEntity.getAvatar().getFileUrl() != null){
                    Log.d(TAG, "图片不为空");
                    Glide.with(LocateActivity.this).load(userEntity.getAvatar().getFileUrl()).into(userImage);
                }else{
                    Log.d(TAG, "图片为空");
                }
            }
        }
    }

    private void setStart(double x, double y) {
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
     * 点击头像的提示对话框
     */
    private void showDialog() {
        photoDialog = new android.support.v7.app.AlertDialog.Builder(LocateActivity.this).create();
        photoDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        photoDialog.show();
        Window window = photoDialog.getWindow();
        window.setContentView(R.layout.dialog_photo); // 修改整个dialog窗口的显示
        window.setGravity(Gravity.BOTTOM);

        WindowManager.LayoutParams lp = photoDialog.getWindow().getAttributes();
        DisplayMetrics dm = new DisplayMetrics();
        window.getWindowManager().getDefaultDisplay().getMetrics(dm);
        lp.width = dm.widthPixels;
        photoDialog.getWindow().setAttributes(lp); // 设置宽度

        photoDialog.findViewById(R.id.btn_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toCamera();
            }
        });
        photoDialog.findViewById(R.id.btn_picture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toPicture();
            }
        });
        photoDialog.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                photoDialog.dismiss();
            }
        });
    }

    /**
     * 跳转相机
     */
    public void toCamera() {
        requestWESPermission(); // 安卓6.0以上需要申请权限
        photoDialog.dismiss();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); // 调用系统的拍照功能
        // 判断内存卡是否可用，可用的话就进行储存
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(new File(Environment.getExternalStorageDirectory(), PHOTO_IMAGE_FILE_NAME)));
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    /**
     * 跳转相册
     */
    private void toPicture() {
        photoDialog.dismiss();
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, IMAGE_REQUEST_CODE);
    }

    /**
     * 动态申请权限
     */
    private void requestWESPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(LocateActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                // 判断是否需要 向用户解释，为什么要申请该权限
                if (ActivityCompat.shouldShowRequestPermissionRationale(LocateActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    ToastUtils.showShort(LocateActivity.this,"Need write external storage permission.");
                ActivityCompat.requestPermissions(LocateActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_BLUETOOTH_PERMISSION);
                return;
            } else {
            }
        } else {
        }
    }

    @Override
    protected void onResume() {
        onStart();
        isForeground = true;
        super.onResume();
    }


    @Override
    protected void onPause() {
        isForeground = false;
        super.onPause();
    }


    @Override
    public void onBackPressed() {
        unregisterReceiver(mMessageReceiver);
        mFMMap.onDestroy();
        ActivityCollector.removeActivity(this);
        finish();
    }
    @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
      if (keyCode == KeyEvent.KEYCODE_BACK) {
          if ((System.currentTimeMillis() - mExitTime) > 2000) {//
              // 如果两次按键时间间隔大于2000毫秒，则不退出
              Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
              mExitTime = System.currentTimeMillis();// 更新mExitTime
          } else {
              System.exit(0);// 否则退出程序
          }
          return true;
      }
          ActivityCollector.finishAll();
      return super.onKeyDown(keyCode, event);
  }

    public final Activity getActivity(){
        return LocateActivity.this;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case IMAGE_REQUEST_CODE: // 相册数据
                if (data != null) {
                    startPhotoZoom(data.getData());
                }
                break;
            case CAMERA_REQUEST_CODE: // 相机数据
                tempFile = new File(Environment.getExternalStorageDirectory(), PHOTO_IMAGE_FILE_NAME);
                startPhotoZoom(Uri.fromFile(tempFile));
                break;
            case RESULT_REQUEST_CODE: // 有可能点击舍弃
                if (data != null) {
                    // 拿到图片设置, 然后需要删除tempFile
                    setImageToView(data);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 裁剪
     * @param uri
     */
    private void startPhotoZoom(Uri uri) {
        if (uri == null) {
            //LogUtils.e("JAVA", "裁剪uri == null");
            return;
        }
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 设置裁剪
        intent.putExtra("crop", "true");
        // 裁剪宽高比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // 裁剪图片的质量
        intent.putExtra("outputX", 320);
        intent.putExtra("outputY", 320);
        // 发送数据
        intent.putExtra("return-data", true);
        startActivityForResult(intent, RESULT_REQUEST_CODE);
    }

    /**
     * 设置icon并上传服务器
     * @param data
     */
    private void setImageToView(Intent data) {
        Bundle bundle = data.getExtras();
        if (bundle != null) {
            final Bitmap bitmap = bundle.getParcelable("data");
            final BmobFile bmobFile = new BmobFile(bitmapToFile(bitmap));

            bmobFile.uploadblock(new UploadFileListener() {
                @Override
                public void done(BmobException e) {
                    if(e==null) {
                        // 此时上传成功
                        UserEntity userEntity = new UserEntity();
                        userEntity.setAvatar(bmobFile);// 获取文件并赋值给实体类
                        BmobUser bmobUser = BmobUser.getCurrentUser();
                        userEntity.update(bmobUser.getObjectId(), new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if (e == null) {
                                    userImage.setImageBitmap(bitmap);
                                    ToastUtils.showShort(LocateActivity.this, getString(R.string.avatar_editor_success));
                                } else {
                                    ToastUtils.showShort(LocateActivity.this, getString(R.string.avatar_editor_failure));
                                }
                            }
                        });
                    } else {
                        ToastUtils.showShort(LocateActivity.this, getString(R.string.avatar_editor_failure));
                    }
                    // 既然已经设置了图片，我们原先的就应该删除
                    if (tempFile != null) {
                        tempFile.delete();
                        //LogUtils.i("JAVA", "tempFile已删除");
                    }
                }
                @Override
                public void onProgress(Integer value) {
                    // 返回的上传进度（百分比）
                }
            });
        }
    }

    /**
     * Bitmap转File
     */
    public File bitmapToFile(Bitmap bitmap) {
        tempFile = new File(Environment.getExternalStorageDirectory(), PHOTO_IMAGE_FILE_NAME);
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tempFile));
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)) {
                bos.flush();
                bos.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tempFile;
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
        //Log.d("出错原因", "analyzeNavigation: "+type1+""+type2);
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

    //扫描wifi的广播
    private final BroadcastReceiver wifiReceiver = new BroadcastReceiver()
    {

        @Override
        public void onReceive(Context arg0, Intent intent)
        {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
            {
                wm.startScan();
                List<ScanResult> results = wm.getScanResults();
                 int  col=   meutils.Comper(results);
                //找不到任何匹配的点
                 if(col==0)
                 {
                     isHere=false;
                     unregisterReceiver(wifiReceiver);
                     (new MyDialog()).show(getFragmentManager(), "my dialog");
                 }
                else {
                     point = db.GetPoint(col);
                     setStart(point[0], point[1]);
                 }
                     //Log.d(TAG, "onReceive: "+point[0]+"   "+point[1]);
               // Toast.makeText(LocateActivity.this,""+p.x+" "+p.y,Toast.LENGTH_LONG).show();
            }
        }
    };


    //以下是极光推送的广播
    private MessageReceiver mMessageReceiver;
    public static final String MESSAGE_RECEIVED_ACTION = "com.example.jpushdemo.MESSAGE_RECEIVED_ACTION";
    public static final String KEY_TITLE = "title";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_EXTRAS = "extras";

    public void registerMessageReceiver() {
        mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        filter.addAction(MESSAGE_RECEIVED_ACTION);
        registerReceiver(mMessageReceiver, filter);
    }

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (MESSAGE_RECEIVED_ACTION.equals(intent.getAction())) {
                String messge = intent.getStringExtra(KEY_MESSAGE);
                String extras = intent.getStringExtra(KEY_EXTRAS);
                StringBuilder showMsg = new StringBuilder();
                showMsg.append(KEY_MESSAGE + " : " + messge + "\n");
                if (!PushUtil.isEmpty(extras)) {
                    showMsg.append(KEY_EXTRAS + " : " + extras + "\n");
                }
                new  android.support.v7.app.AlertDialog.Builder(LocateActivity.this).
                        setTitle("您的专属消息").setMessage(messge).setPositiveButton("确定" ,  null)
                        .setCancelable(false).show();
            }
        }
    }

}
