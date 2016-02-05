package com.yuntongxun.ecdemo.ui.voip;

import android.os.Bundle;

import android.text.TextUtils;

import com.yishiji.angelplatform.R;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.ui.SDKCoreHelper;
import com.yuntongxun.ecdemo.ui.voip.ECCallHeadUILayout.OnSendDTMFDelegate;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECVoIPSetupManager;
import com.yuntongxun.ecsdk.ECVoIPCallManager.CallType;
import com.yuntongxun.ecsdk.SdkErrorCode;

/**
 * com.yuntongxun.ecdemo.ui.voip in ECDemo_Android
 * Created by Jorstin on 2015/7/3.
 */
public class VoIPCallActivity extends ECVoIPBaseActivity implements OnSendDTMFDelegate {

    private static final String TAG = "ECSDK_Demo.VoIPCallActivity";
	private boolean isCallBack;

    @Override
    protected int getLayoutId() {
        return R.layout.ec_call_interface;
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
            
            isCallBack = getIntent().getBooleanExtra(ACTION_CALLBACK_CALL, false);
        }

        initView();
		if (!mIncomingCall) {
			// 处理呼叫逻辑
			if (TextUtils.isEmpty(mCallNumber)) {
				ToastUtil.showMessage(R.string.ec_call_number_error);
				finish();
				return;
			}

			if (isCallBack) {
				VoIPCallHelper.makeCallBack(CallType.VOICE, mCallNumber);
			} else {
				mCallId = VoIPCallHelper.makeCall(mCallType, mCallNumber);
				if (TextUtils.isEmpty(mCallId)) {
					ToastUtil
							.showMessage(R.string.ec_app_err_disconnect_server_tip);
					LogUtil.d(TAG, "Call fail, callId " + mCallId);
					finish();
					return;
				}
			}
			mCallHeaderView .setCallTextMsg(R.string.ec_voip_call_connecting_server);
		}
        

    }

    private void initView() {
        mCallHeaderView = (ECCallHeadUILayout) findViewById(R.id.call_header_ll);
        mCallControlUIView = (ECCallControlUILayout) findViewById(R.id.call_control_ll);
        mCallControlUIView.setOnCallControlDelegate(this);
        mCallHeaderView.setCallName(mCallName);
        mCallHeaderView.setCallNumber(TextUtils.isEmpty(mPhoneNumber) ? mCallNumber : mPhoneNumber);
        mCallHeaderView.setCalling(false);

        ECCallControlUILayout.CallLayout callLayout = mIncomingCall ? ECCallControlUILayout.CallLayout.INCOMING
                : ECCallControlUILayout.CallLayout.OUTGOING;
        mCallControlUIView.setCallDirect(callLayout);
        
        mCallHeaderView.setSendDTMFDelegate(this);
    }


    @Override
    protected boolean isEnableSwipe() {
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    /**
     * 连接到服务器
     * @param callId 通话的唯一标识
     */
    @Override
    public void onCallProceeding(String callId) {
        if(mCallHeaderView == null || !needNotify(callId)) {
            return ;
        }
        LogUtil.d(TAG , "onUICallProceeding:: call id " + callId);
        mCallHeaderView.setCallTextMsg(R.string.ec_voip_call_connect);
    }

    /**
     * 连接到对端用户，播放铃音
     * @param callId 通话的唯一标识
     */
    @Override
    public void onCallAlerting(String callId) {
        if(!needNotify(callId) || mCallHeaderView == null) {
            return ;
        }
        LogUtil.d(TAG , "onUICallAlerting:: call id " + callId);
        mCallHeaderView.setCallTextMsg(R.string.ec_voip_calling_wait);
        mCallControlUIView.setCallDirect(ECCallControlUILayout.CallLayout.ALERTING);
    }

    /**
     * 对端应答，通话计时开始
     * @param callId 通话的唯一标识
     */
    @Override
    public void onCallAnswered(String callId) {
        if(!needNotify(callId)|| mCallHeaderView == null) {
            return ;
        }
        LogUtil.d(TAG , "onUICallAnswered:: call id " + callId);
        mCallHeaderView.setCalling(true);
        
    }

    @Override
    public void onMakeCallFailed(String callId , int reason) {
        if(mCallHeaderView == null || !needNotify(callId)) {
            return ;
        }
        LogUtil.d(TAG, "onUIMakeCallFailed:: call id " + callId + " ,reason " + reason);
        mCallHeaderView.setCalling(false);
        mCallHeaderView.setCallTextMsg(CallFailReason.getCallFailReason(reason));
        if(reason != SdkErrorCode.REMOTE_CALL_BUSY&&reason!=SdkErrorCode.REMOTE_CALL_DECLINED) {
            VoIPCallHelper.releaseCall(mCallId);
            finish();
        }
    }

    /**
     * 通话结束，通话计时结束
     * @param callId 通话的唯一标识
     */
    @Override
    public void onCallReleased(String callId) {
        if(mCallHeaderView == null || !needNotify(callId)) {
            return ;
        }
        LogUtil.d(TAG , "onUICallReleased:: call id " + callId);
        mCallHeaderView.setCalling(false);
        mCallHeaderView.setCallTextMsg(R.string.ec_voip_calling_finish);
        mCallControlUIView.setControlEnable(false);
        finish();
    }

	@Override
	public void onMakeCallback(ECError ecError, String caller, String called) {
		if(!TextUtils.isEmpty(mCallId)) {
			return ;
		}
		if(ecError.errorCode != SdkErrorCode.REQUEST_SUCCESS) {
			mCallHeaderView .setCallTextMsg("回拨呼叫失败[" + ecError.errorCode + "]");
		} else {
			mCallHeaderView .setCallTextMsg(R.string.ec_voip_call_back_success);
		}
		mCallHeaderView.setCalling(false);
        mCallControlUIView.setControlEnable(false);
		finish();
	}

	@Override
	public void setDialerpadUI() {

		mCallHeaderView.controllerDiaNumUI();
	}

	@Override
	public void sendDTMF(char c) {

		SDKCoreHelper.getVoIPCallManager().sendDTMF(mCallId, c);
	}
}
