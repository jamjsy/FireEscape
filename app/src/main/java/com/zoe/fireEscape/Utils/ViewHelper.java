package com.ZOE.FireEscape.Utils;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.fengmap.android.map.FMMap;
import com.fengmap.android.map.animator.FMAnimation;
import com.fengmap.android.map.animator.FMAnimationFactory;
import com.fengmap.android.map.animator.FMValueAnimation;
import com.fengmap.android.map.geometry.FMMapCoord;
import com.fengmap.android.map.marker.FMImageMarker;

/**
 * @Email hezutao@fengmap.com
 * @Version 2.0.0
 * @Description 控件使用帮助类
 */
public class ViewHelper {

    /**
     * 获取控件
     *
     * @param activity Activity
     * @param id       控件id
     * @param <T>
     * @return
     */
    public static <T extends View> T getView(Activity activity, int id) {
        return (T) activity.findViewById(id);
    }

    /**
     * 获取控件
     *
     * @param view 视图
     * @param id   控件id
     * @param <T>
     * @return
     */
    public static <T extends View> T getView(View view, int id) {
        return (T) view.findViewById(id);
    }

    /**
     * 设置控件的点击事件
     *
     * @param activity Activity
     * @param id       控件id
     * @param listener 点击监听事件
     */
    public static void setViewClickListener(Activity activity, int id, View.OnClickListener listener) {
        View view = getView(activity, id);
        view.setOnClickListener(listener);
    }


    /**
     * 设置控件的点击事件
     *
     * @param activity Activity
     * @param id       控件id
     * @param listener CheckBox选中状态改变事件
     */
    public static void setViewCheckedChangeListener(Activity activity, int id,
                                                    CompoundButton.OnCheckedChangeListener listener) {
        CheckBox view = getView(activity, id);
        view.setOnCheckedChangeListener(listener);
    }

    /**
     * 设置控件的点击事件
     *
     * @param activity   Activity
     * @param id         控件id
     * @param visibility 控件显示状态
     */
    public static void setViewVisibility(Activity activity, int id, int visibility) {
        View view = getView(activity, id);
        view.setVisibility(visibility);
    }

    /**
     * 添加图片标注
     *
     * @param resId 资源id
     */
    public static FMImageMarker buildImageMarker(Resources resources, FMMapCoord mapCoord, int resId) {
        Bitmap bitmap = BitmapFactory.decodeResource(resources, resId);
        FMImageMarker imageMarker = new FMImageMarker(mapCoord, bitmap);
        //设置图片宽高
        imageMarker.setMarkerWidth(90);
        imageMarker.setMarkerHeight(90);
        //设置图片在模型之上
        imageMarker.setFMImageMarkerOffsetMode(FMImageMarker.FMImageMarkerOffsetMode.FMNODE_MODEL_ABOVE);
        return imageMarker;
    }

    /**
     * 移动至地图中心点
     *
     * @param map        地图
     * @param startCoord 目标坐标
     */
    public static void moveToCenter(final FMMap map, FMMapCoord startCoord,
                                    final Runnable runnable) {
        String centerAnim = "move_center_anim";
        FMValueAnimation animation = FMAnimationFactory.getFactory().getFMValueAnimation(centerAnim);
        if (animation != null) {
            animation.stop();
            FMAnimationFactory.getFactory().destroyFMValueAnimation(centerAnim);
        }
        FMMapCoord endCoord = map.getMapCenter();
        //创建动画对象
        animation = FMAnimationFactory.getFactory().createFMValueAnimation(centerAnim);
        animation.setOnFMAnimationListener(new FMAnimation.OnFMAnimationListener() {
            @Override
            public void beforeAnimation(String pName) {

            }

            @Override
            public void updateAnimationFrame(String pName, Object pLastValue, Object pCurrentValue) {
                FMMapCoord from = (FMMapCoord) pLastValue;
                FMMapCoord to = (FMMapCoord) pCurrentValue;
                map.move(from, to, false);
            }

            @Override
            public void afterAnimation(String pName) {
                runnable.run();
            }
        });
        animation.ofPosition(startCoord, endCoord).setDuration(800L).start();
    }


}
