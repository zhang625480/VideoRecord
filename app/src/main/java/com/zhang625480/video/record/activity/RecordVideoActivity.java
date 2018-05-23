package com.zhang625480.video.record.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zhang625480.video.record.R;
import com.zhang625480.video.record.util.BaseEvent;
import com.zhang625480.video.record.util.EventType;
import com.zhang625480.video.record.util.PermissionUtil.PermissionHelper;
import com.zhang625480.video.record.util.PermissionUtil.PermissionInterface;
import com.zhang625480.video.record.view.MovieRecorderView;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

/**
 * 视频拍摄页面
 */
public class RecordVideoActivity extends Activity implements View.OnClickListener, PermissionInterface {
    private static final String LOG_TAG = "RecordVideoActivity";
    private static final int REQ_CODE = 110;
    private static final int RES_CODE = 111;
    /**
     * 录制进度
     */
    private static final int RECORD_PROGRESS = 100;
    /**
     * 录制结束
     */
    private static final int RECORD_FINISH = 101;

    private MovieRecorderView movieRecorderView;
    private Button buttonShoot;
    private ProgressBar progressVideo;
    private TextView textViewCountDown;
    private TextView textViewUpToCancel;//上移取消
    private TextView textViewReleaseToCancel;//释放取消

    private boolean isSendMessage = false;
    /**
     * 是否结束录制
     */
    private boolean isFinish = true;
    /**
     * 是否触摸在松开取消的状态
     */
    private boolean isTouchOnUpToCancel = false;
    /**
     * 当前进度
     */
    private int currentTime = 0;

    private PermissionHelper mPermissionHelper;


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RECORD_PROGRESS:
                    progressVideo.setProgress(currentTime);
                    if (currentTime < 10) {
                        textViewCountDown.setText("00:0" + currentTime);
                    } else {
                        textViewCountDown.setText("00:" + currentTime);
                    }
                    break;
                case RECORD_FINISH:
                    if (isTouchOnUpToCancel) {//录制结束，还在上移删除状态没有松手，就复位录制
                        resetData();
                    } else {//录制结束，在正常位置，录制完成跳转页面
                        isFinish = true;
                        buttonShoot.setEnabled(false);
                        finishActivity();
                    }
                    break;
            }
        }
    };
    /**
     * 按下的位置
     */
    private float startY;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermision();
    }

    private void checkPermision() {
        mPermissionHelper = new PermissionHelper(this, this);
        mPermissionHelper.requestPermissions();
    }

    public void initView() {
        setContentView(R.layout.activity_record_video);
        movieRecorderView = (MovieRecorderView) findViewById(R.id.movieRecorderView);
        buttonShoot = (Button) findViewById(R.id.button_shoot);
        progressVideo = (ProgressBar) findViewById(R.id.progressBar_loading);
        textViewCountDown = (TextView) findViewById(R.id.textView_count_down);
        textViewCountDown.setText("00:00");
        textViewUpToCancel = (TextView) findViewById(R.id.textView_up_to_cancel);
        textViewReleaseToCancel = (TextView) findViewById(R.id.textView_release_to_cancel);

        findViewById(R.id.change_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                movieRecorderView.changeCamera();
            }
        });
        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) movieRecorderView.getLayoutParams();
        movieRecorderView.setLayoutParams(layoutParams);

        //处理触摸事件
        buttonShoot.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    textViewUpToCancel.setVisibility(View.VISIBLE);//提示上移取消

                    isFinish = false;//开始录制
                    startY = event.getY();//记录按下的坐标
                    movieRecorderView.record(new MovieRecorderView.OnRecordFinishListener() {
                        @Override
                        public void onRecordFinish() {
                            if (!isSendMessage) {
                                handler.sendEmptyMessage(RECORD_FINISH);
                            }
                            isSendMessage = true;
                        }
                    });
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    textViewUpToCancel.setVisibility(View.GONE);
                    textViewReleaseToCancel.setVisibility(View.GONE);

                    if (startY - event.getY() > 100) {//上移超过一定距离取消录制，删除文件
                        if (!isFinish) {
                            resetData();
                        }
                    } else {
                        if (movieRecorderView.getTimeCount() > 3) {//录制时间超过三秒，录制完成

                            if (!isSendMessage) {
                                handler.sendEmptyMessage(RECORD_FINISH);
                            }
                            isSendMessage = true;
                        } else {//时间不足取消录制，删除文件
                            Toast.makeText(RecordVideoActivity.this, "视频录制时间太短", Toast.LENGTH_SHORT).show();
                            resetData();
                        }
                    }
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    //根据触摸上移状态切换提示
                    if (startY - event.getY() > 100) {
                        isTouchOnUpToCancel = true;//触摸在松开就取消的位置
                        if (textViewUpToCancel.getVisibility() == View.VISIBLE) {
                            textViewUpToCancel.setVisibility(View.GONE);
                            textViewReleaseToCancel.setVisibility(View.VISIBLE);
                        }
                    } else {
                        isTouchOnUpToCancel = false;//触摸在正常录制的位置
                        if (textViewUpToCancel.getVisibility() == View.GONE) {
                            textViewUpToCancel.setVisibility(View.VISIBLE);
                            textViewReleaseToCancel.setVisibility(View.GONE);
                        }
                    }
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    resetData();

                }
                return true;
            }
        });

        progressVideo.setMax(10);
        movieRecorderView.setOnRecordProgressListener(new MovieRecorderView.OnRecordProgressListener() {
            @Override
            public void onProgressChanged(int maxTime, int currentTime) {
                RecordVideoActivity.this.currentTime = currentTime;
                handler.sendEmptyMessage(RECORD_PROGRESS);
            }
        });
    }

    /**
     * 重置状态
     */
    private void resetData() {
        if (movieRecorderView.getRecordFile() != null)
            movieRecorderView.getRecordFile().delete();
        movieRecorderView.stop();
        isFinish = true;
        currentTime = 0;
        progressVideo.setProgress(0);
        textViewCountDown.setText("00:00");
        buttonShoot.setEnabled(true);
        textViewUpToCancel.setVisibility(View.GONE);
        textViewReleaseToCancel.setVisibility(View.GONE);
        movieRecorderView.initCamera();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        isFinish = true;
        movieRecorderView.stop();
    }

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     *
     * @param dir 将要删除的文件目录
     * @return
     */
    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i = 0; i < children.length; i++) {
                if (!deleteDir(new File(dir, children[i]))) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    @Override
    public void onDestroy() {
        if (null != movieRecorderView) {
            if (movieRecorderView.getRecordFile() != null) {
                File file = new File(movieRecorderView.getRecordFile().getAbsolutePath());
                if (file != null && file.exists()) {
                    Log.e(LOG_TAG, "file.exists():" + file.exists());
                }
            }
            movieRecorderView.freeCameraResource();
            isSendMessage = false;
        }

        super.onDestroy();
    }

    /**
     * TODO 录制完成需要做的事情
     */
    private void finishActivity() {
        if (isFinish) {
            movieRecorderView.stop();
            Intent intent = new Intent(this, VideoPreviewActivity.class);
            intent.putExtra("path", movieRecorderView.getRecordFile().getAbsolutePath());
            startActivityForResult(intent, REQ_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE && resultCode == RES_CODE) {
            BaseEvent mBaseEvent = new BaseEvent(EventType.WHAT_POST_DYNAMIC_VIDEO_DATA);
            String path = data.getExtras().getString("path", "");
            mBaseEvent.content = path;
            EventBus.getDefault().post(mBaseEvent);
            finish();
        }
    }


    @Override
    public void onClick(View v) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (mPermissionHelper.requestPermissionsResult(requestCode, permissions, grantResults)) {
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public int getPermissionsRequestCode() {
        return 0;
    }

    @Override
    public String[] getPermissions() {
        return new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    }

    @Override
    public void requestPermissionsSuccess() {
        initView();
    }

    @Override
    public void requestPermissionsSuccess(String permission) {
        Toast.makeText(this, "权限" + permission + "获取成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void requestPermissionsFail(String permission, boolean isShowAgain) {
        Toast.makeText(this, "权限" + permission + "获取失败，请授权后使用\n," + (isShowAgain ? "未勾选" : "勾选") + "不再弹框按钮", Toast.LENGTH_SHORT).show();

    }
}
