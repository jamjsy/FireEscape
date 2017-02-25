package aiyuan1996.cn.firerunning.Utils;

import android.content.Context;
import android.util.Log;

import com.bumptech.glide.Glide;

import aiyuan1996.cn.firerunning.entity.UserEntity;
import cn.bmob.v3.BmobUser;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by aiyuan on 2017/2/25.
 */

public class PictureUtil {
    private static final String TAG = "PictureUtil";
    CircleImageView userImage;
    private Context context;
    public PictureUtil(Context context) {
        this.context = context;
    }

    public void initview() {
        if (BmobUser.getCurrentUser()!=null) {
            Log.d(TAG, "BmobUser.getCurrentUser()!=null");
            UserEntity userEntity = BmobUser.getCurrentUser(UserEntity.class);
            if (userEntity.getAvatar()!=null) {
                Log.d(TAG, "userEntity.getAvatar()!=null");

                if(userEntity.getAvatar().getFileUrl() != null){
                    Log.d(TAG, "图片不为空");
                    Glide.with(context).load(userEntity.getAvatar().getFileUrl()).into(userImage);
                }else{
                    Log.d(TAG, "图片为空");
                }
            }
        }
    }

}
