package com.yuntongxun.ecdemo.ui.voip;

import org.webrtc.videoengine.ViERenderer;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yishiji.angelplatform.R;
import com.yuntongxun.ecdemo.common.utils.CommomUtil;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecsdk.CameraInfo;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECVoIPCallManager;
import com.yuntongxun.ecsdk.ECVoIPSetupManager;
import com.yuntongxun.ecsdk.VideoRatio;

/**
 * com.yuntongxun.ecdemo.ui.voip in ECDemo_Android
 * Created by Jorstin on 2015/7/8.
 */
public class VideoActivity extends ECVoIPBaseActivity implements View.OnClickListener{

    private static final String TAG = "VideoActivity";
    private static long lastClickTime;
    private Button mVideoStop;
    private Button mVideoBegin;
    private Button mVideoCancle;
    private ImageView mVideoIcon;
    private RelativeLayout mVideoTipsLy;

    private TextView mVideoTopTips;
    private TextView mVideoCallTips;
    private TextView mCallStatus;
    private SurfaceView mVideoView;
    // Remote Video
    private FrameLayout mVideoLayout;
    private Chronometer mChronometer;

    private View mCameraSwitch;
    boolean isConnect = false;
    CameraInfo[] cameraInfos;
    public RelativeLayout mLoaclVideoView;
    int numberOfCameras;
    private View video_switch;
    private int mWidth;
    private int mHeight;

    // The first rear facing camera
    public int defaultCameraId;

    public int cameraCurrentlyLocked;

    public int mCameraCapbilityIndex;


    @Override
    protected int getLayoutId() {
        return R.layout.ec_video_call;
    }

    @Override
    protected boolean isEnableSwipe() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(mIncomingCall) {
            // 来电
            mCallId = getIntent().getStringExtra(ECDevice.CALLID);
            mCallNumber = getIntent().getStringExtra(ECDevice.CALLER);
        } else {
            // 呼出
            mCallName = getIntent().getStringExtra(EXTRA_CALL_NAME);
            mCallNumber = getIntent().getStringExtra(EXTRA_CALL_NUMBER);
        }

        initResourceRefs();

        cameraInfos = ECDevice.getECVoIPSetupManager().getCameraInfos();

        // Find the ID of the default camera
        if (cameraInfos != null) {
            numberOfCameras = cameraInfos.length;
        }

        // Find the total number of cameras available
        for (int i = 0; i < numberOfCameras; i++) {
            if (cameraInfos[i].index == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
                defaultCameraId = i;
                mCameraCapbilityIndex = CommomUtil.comportCapabilityIndex(cameraInfos[cameraCurrentlyLocked].caps, 321 * 480);
            }
        }

        ECDevice.getECVoIPSetupManager().setVideoView(mVideoView, null);

        if(!mIncomingCall) {
            mCallId = VoIPCallHelper.makeCall(mCallType ,  mCallNumber);
        } else {
            mVideoCancle.setVisibility(View.GONE);
            mVideoTipsLy.setVisibility(View.VISIBLE);
            mVideoBegin.setVisibility(View.VISIBLE);
        }

        DisplayLocalSurfaceView();
        
        if(mIncomingCall){
        	mVideoStop.setEnabled(true);
        }
    }

    private void initResourceRefs() {

        mVideoTipsLy = (RelativeLayout) findViewById(R.id.video_call_in_ly);
        mVideoIcon = (ImageView) findViewById(R.id.video_icon);

        mVideoTopTips = (TextView) findViewById(R.id.notice_tips);
        mVideoCallTips = (TextView) findViewById(R.id.video_call_tips);
        mVideoTopTips.setText(R.string.ec_voip_call_connecting_server);
        mVideoCancle = (Button) findViewById(R.id.video_botton_cancle);
        mVideoBegin = (Button) findViewById(R.id.video_botton_begin);
        mVideoStop = (Button) findViewById(R.id.video_stop);
        mVideoStop.setEnabled(false);

        mVideoCancle.setOnClickListener(this);
        mVideoBegin.setOnClickListener(this);
        mVideoStop.setOnClickListener(this);

        mVideoView = (SurfaceView) findViewById(R.id.video_view);
        // mVideoView.setVisibility(View.INVISIBLE);
        mLoaclVideoView = (RelativeLayout) findViewById(R.id.localvideo_view);
        mVideoLayout = (FrameLayout) findViewById(R.id.Video_layout);
        mCameraSwitch = findViewById(R.id.camera_switch);
        mCameraSwitch.setOnClickListener(this);
        video_switch = findViewById(R.id.video_switch);
        video_switch.setOnClickListener(this);

        mCallStatus = (TextView) findViewById(R.id.call_status);
        mCallStatus.setVisibility(View.GONE);
        // mVideoView.getHolder().setFixedSize(width, height);
        mVideoView.getHolder().setFixedSize(240, 320);

        // SurfaceView localView = ViERenderer.CreateLocalRenderer(this);
        // mLoaclVideoView.addView(localView);
    }

    private void initResVideoSuccess() {
        isConnect = true;
        mVideoLayout.setVisibility(View.VISIBLE);
        mVideoIcon.setVisibility(View.GONE);
        mVideoTopTips.setVisibility(View.GONE);
        mCameraSwitch.setVisibility(View.VISIBLE);
        mVideoTipsLy.setVisibility(View.VISIBLE);
        mVideoBegin.setVisibility(View.GONE);
        // bottom ...
        mVideoCancle.setVisibility(View.GONE);
        mVideoCallTips.setVisibility(View.VISIBLE);
        mVideoCallTips.setText(getString(R.string.str_video_bottom_time, mCallNumber));
        mVideoStop.setVisibility(View.VISIBLE);
        mVideoStop.setEnabled(true);

        mChronometer = (Chronometer) findViewById(R.id.chronometer);
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.setVisibility(View.VISIBLE);
        mChronometer.start();

    }

    public void DisplayLocalSurfaceView() {
        if (mCallType == ECVoIPCallManager.CallType.VIDEO && mLoaclVideoView != null
                && mLoaclVideoView.getVisibility() == View.VISIBLE) {
            // Create a RelativeLayout container that will hold a SurfaceView,
            // and set it as the content of our activity.
            SurfaceView localView = ViERenderer.CreateLocalRenderer(this);
            // localView.setLayoutParams(layoutParams);
            localView.setZOrderOnTop(true);
            mLoaclVideoView.removeAllViews();
            mLoaclVideoView.setBackgroundColor(getResources().getColor(
                    R.color.white));
            mLoaclVideoView.addView(localView);
        }
    }

    /**
     * 根据状态,修改按钮属性及关闭操作
     */
    private void finishCalling() {
        try {
            // mChronometer.setVisibility(View.GONE);

            mVideoTopTips.setVisibility(View.VISIBLE);
            mCameraSwitch.setVisibility(View.GONE);
            mVideoTopTips.setText(R.string.ec_voip_calling_finish);

            if (isConnect) {
                // set Chronometer view gone..
                mChronometer.stop();
                mVideoLayout.setVisibility(View.GONE);
                mVideoIcon.setVisibility(View.VISIBLE);

                mLoaclVideoView.removeAllViews();
                mLoaclVideoView.setVisibility(View.GONE);

                // bottom can't click ...
                mVideoStop.setEnabled(false);
            } else {
                mVideoCancle.setEnabled(false);
            }
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            isConnect = false;
        }
    }

    private void finishCalling(int reason) {
        try {
            mVideoTopTips.setVisibility(View.VISIBLE);
            mCameraSwitch.setVisibility(View.GONE);
            mLoaclVideoView.removeAllViews();
            mLoaclVideoView.setVisibility(View.GONE);
            if (isConnect) {
                mChronometer.stop();
                mVideoLayout.setVisibility(View.GONE);
                mVideoIcon.setVisibility(View.VISIBLE);
                isConnect = false;
                // bottom can't click ...
                mVideoStop.setEnabled(false);
            } else {
                mVideoCancle.setEnabled(false);
            }
            isConnect = false;
            mVideoTopTips.setText(CallFailReason.getCallFailReason(reason));
            VoIPCallHelper.releaseCall(mCallId);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCallType == ECVoIPCallManager.CallType.VIDEO) {
            cameraCurrentlyLocked = defaultCameraId;
            if(ECDevice.getECVoIPSetupManager() != null) {

                ECDevice.getECVoIPSetupManager().selectCamera(cameraCurrentlyLocked,
                        mCameraCapbilityIndex, 15, ECVoIPSetupManager.Rotate.ROTATE_AUTO, true);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VoIPCallHelper.mHandlerVideoCall = false;
    }

    @Override
    public void onCallProceeding(String callId) {
        if (callId != null && callId.equals(mCallId)) {
            mVideoTopTips.setText(getString(R.string.ec_voip_call_connect));
        }
    }

    @Override
    public void onCallAlerting(String callId) {
        if (callId != null && callId.equals(mCallId)) {// 等待对方接受邀请...
            mVideoTopTips.setText(getString(R.string.str_tips_wait_invited));
        }
    }

    @Override
    public void onCallAnswered(String callId) {
        if (callId != null && callId.equals(mCallId) && !isConnect) {
            initResVideoSuccess();
        }
    }

    @Override
    public void onMakeCallFailed(String callId, int reason) {
        if (callId != null && callId.equals(mCallId)) {
            finishCalling(reason);

        }
    }

    @Override
    public void onCallReleased(String callId) {
        if (callId != null && callId.equals(mCallId)) {
            finishCalling();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.video_botton_begin:
                VoIPCallHelper.acceptCall(mCallId);
                break;

            case R.id.video_stop:
            case R.id.video_botton_cancle:

                doHandUpReleaseCall();
                break;
            case R.id.camera_switch:

                // check for availability of multiple cameras
                if (numberOfCameras == 1) {
                    return;
                }
                mCameraSwitch.setEnabled(false);

                // OK, we have multiple cameras.
                // Release this camera -> cameraCurrentlyLocked
                cameraCurrentlyLocked = (cameraCurrentlyLocked + 1)
                        % numberOfCameras;
                mCameraCapbilityIndex = CommomUtil.comportCapabilityIndex(cameraInfos[cameraCurrentlyLocked].caps, 321 * 480);

                ECDevice.getECVoIPSetupManager().selectCamera(cameraCurrentlyLocked,
                        mCameraCapbilityIndex, 15, ECVoIPSetupManager.Rotate.ROTATE_AUTO, false);

                if (cameraCurrentlyLocked == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    defaultCameraId = android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT;
                    Toast.makeText(VideoActivity.this,
                            R.string.camera_switch_front, Toast.LENGTH_SHORT)
                            .show();
                } else {
                    defaultCameraId = android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK;
                    Toast.makeText(VideoActivity.this, R.string.camera_switch_back,
                            Toast.LENGTH_SHORT).show();

                }
                mCameraSwitch.setEnabled(true);
                break;
            default:
                break;
        }
    }

    protected void doHandUpReleaseCall() {

        // Hang up the video call...
        LogUtil.d(TAG,
                "[VideoActivity] onClick: Voip talk hand up, CurrentCallId " + mCallId);
        try {
            if (mCallId != null) {
            	
            	if(mIncomingCall&&!isConnect){
            		VoIPCallHelper.rejectCall(mCallId);
            	}else {
                	VoIPCallHelper.releaseCall(mCallId);
            	}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!isConnect) {
            finish();
        }
    }

	@Override
	public void onMakeCallback(ECError arg0, String arg1, String arg2) {

	}

    @Override
    public void onVideoRatioChanged(VideoRatio videoRatio) {
        super.onVideoRatioChanged(videoRatio);
        if(mVideoView != null && videoRatio != null) {
            mVideoView.getHolder().setFixedSize(videoRatio.getWidth() , videoRatio.getHeight());
        }
    }

	@Override
	public void setDialerpadUI() {
		// TODO Auto-generated method stub
		
	}
}
