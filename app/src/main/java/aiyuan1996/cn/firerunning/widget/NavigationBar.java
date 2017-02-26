package aiyuan1996.cn.firerunning.widget;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import aiyuan1996.cn.firerunning.R;
import aiyuan1996.cn.firerunning.map.GetDataActivity;
import aiyuan1996.cn.firerunning.map.LocateActivity;

/**
 * @Email hezutao@fengmap.com
 * @Version 2.0.0
 * @Description 自定义标题栏视图组件
 */
public class NavigationBar extends RelativeLayout {

    Context mContext;
    TextView mTitleTxt;
    ImageView mLeftImage;
    DrawerLayout drawer;
    private OnClickListener mDefaultClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            //点击左侧图标的事件
            Context context=v.getContext();
            if(context instanceof LocateActivity)
            {
                ((LocateActivity) context).openDrawer();
                //context.startActivity(new Intent(context , GetDataActivity.class));
            }
            if(context instanceof GetDataActivity)
            {
                ((GetDataActivity) context).openDrawer();
                //context.startActivity(new Intent(context , LocateActivity.class));
            }
        }
    };

    public NavigationBar(Context context) {
        super(context);
        initView(context, null);
    }

    public NavigationBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }


    public NavigationBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context, attrs);
    }

    /**
     * 初始化资源
     */
    private void initView(Context context, AttributeSet attrs) {
        this.mContext = context;
        LayoutInflater.from(context).inflate(R.layout.widget_navigationbar, this,
                true);
        mLeftImage = (ImageView) findViewById(R.id.img_left);
        mTitleTxt = (TextView) findViewById(R.id.txt_title);
        mTitleTxt.setText("地图显示");
        mLeftImage.setOnClickListener(mDefaultClickListener);
        this.setBackgroundResource(R.color.blue);
    }

    //设置标题栏文本
    public void setTitle(String title) {
        mTitleTxt.setText(title);
    }

}