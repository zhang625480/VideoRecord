package com.zhang625480.video.record.util.PermissionUtil;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

/**
 * Created by zhangzhen on 2018/3/26.
 */

public class PermissionHelper {

    private Activity mActivity;
    private PermissionInterface mPermissionInterface;

    public PermissionHelper(@NonNull Activity activity, @NonNull PermissionInterface permissionInterface) {
        mActivity = activity;
        mPermissionInterface = permissionInterface;
    }

    /**
     * 开始请求权限。
     * 方法内部已经对Android M 或以上版本进行了判断，外部使用不再需要重复判断。
     * 如果设备还不是M或以上版本，则也会回调到requestPermissionsSuccess方法。
     */
    public void requestPermissions(){
        String[] deniedPermissions = PermissionUtil.getDeniedPermissions(mActivity, mPermissionInterface.getPermissions(),mPermissionInterface);
        if(deniedPermissions != null && deniedPermissions.length > 0){
            PermissionUtil.requestPermissions(mActivity, deniedPermissions, mPermissionInterface.getPermissionsRequestCode());
        }
    }

    /**
     * 在Activity中的onRequestPermissionsResult中调用
     * @param requestCode
     * @param permissions
     * @param grantResults
     * @return true 代表对该requestCode感兴趣，并已经处理掉了。false 对该requestCode不感兴趣，不处理。
     */
    public boolean requestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if(requestCode == mPermissionInterface.getPermissionsRequestCode()){
            boolean isAllGranted = true;//是否全部权限已授权
            for(int i=0;i<grantResults.length;i++){
                if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                    mPermissionInterface.requestPermissionsFail(permissions[i], ActivityCompat.shouldShowRequestPermissionRationale(mActivity,permissions[i]));
                    isAllGranted=false;
                }else{
                    mPermissionInterface.requestPermissionsSuccess(permissions[i]);
                }
            }
            if(isAllGranted){
                mPermissionInterface.requestPermissionsSuccess();
                //已全部授权
                return isAllGranted;

            }
        }
        return false;
    }
}
