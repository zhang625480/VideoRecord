package com.zhang625480.video.record.util.PermissionUtil;

/**
 * Created by zhangzhen on 2018/3/26.
 */

public interface PermissionInterface {
    /**
     * 可设置请求权限请求码
     */
    int getPermissionsRequestCode();

    /**
     * 设置需要请求的权限
     */
    String[] getPermissions();


    /**
     * 所有权限都获取成功
     */
    void requestPermissionsSuccess();


    /**
     * 请求权限成功回调
     */
    void requestPermissionsSuccess(String permission);

    /**
     * 请求权限失败回调
     *
     * @param permission  失败的权限
     * @param isShowAgain 是否勾选不再显示 false 勾选   true 未勾选
     */
    void requestPermissionsFail(String permission, boolean isShowAgain);
}
