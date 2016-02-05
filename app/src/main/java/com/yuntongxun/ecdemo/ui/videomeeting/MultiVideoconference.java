/*
 *  Copyright (c) 2013 The CCP project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a Beijing Speedtong Information Technology Co.,Ltd license
 *  that can be found in the LICENSE file in the root of the web site.
 *
 *   http://www.cloopen.com
 *
 *  An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */
package com.yuntongxun.ecdemo.ui.videomeeting;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.webrtc.videoengine.ViERenderer;

import com.yishiji.angelplatform.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.base.OverflowAdapter;
import com.yuntongxun.ecdemo.common.base.OverflowHelper;
import com.yuntongxun.ecdemo.common.dialog.ECAlertDialog;
import com.yuntongxun.ecdemo.common.utils.CommomUtil;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.common.view.CCPAlertDialog;
import com.yuntongxun.ecdemo.ui.SDKCoreHelper;
import com.yuntongxun.ecsdk.CameraInfo;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECMeetingManager.ECCreateMeetingParams;
import com.yuntongxun.ecsdk.ECMeetingManager.OnCreateOrJoinMeetingListener;
import com.yuntongxun.ecsdk.ECMeetingManager.OnDeleteMeetingListener;
import com.yuntongxun.ecsdk.ECMeetingManager.OnInviteMembersJoinToMeetingListener;
import com.yuntongxun.ecsdk.ECMeetingManager.OnMemberVideoFrameChangedListener;
import com.yuntongxun.ecsdk.ECMeetingManager.OnQueryMeetingMembersListener;
import com.yuntongxun.ecsdk.ECMeetingManager.OnRemoveMemberFromMeetingListener;
import com.yuntongxun.ecsdk.ECMeetingManager.OnSelfVideoFrameChangedListener;
import com.yuntongxun.ecsdk.ECVoIPSetupManager;
import com.yuntongxun.ecsdk.SdkErrorCode;
import com.yuntongxun.ecsdk.ECMeetingManager.ECMeetingType;
import com.yuntongxun.ecsdk.ECVoIPSetupManager.Rotate;
import com.yuntongxun.ecsdk.meeting.ECVideoMeetingMember;
import com.yuntongxun.ecsdk.meeting.video.ECVideoMeetingDeleteMsg;
import com.yuntongxun.ecsdk.meeting.video.ECVideoMeetingExitMsg;
import com.yuntongxun.ecsdk.meeting.video.ECVideoMeetingJoinMsg;
import com.yuntongxun.ecsdk.meeting.video.ECVideoMeetingMsg;
import com.yuntongxun.ecsdk.meeting.video.ECVideoMeetingRejectMsg;
import com.yuntongxun.ecsdk.meeting.video.ECVideoMeetingRemoveMemberMsg;
import com.yuntongxun.ecsdk.meeting.video.ECVideoMeetingVideoFrameActionMsg;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 *
 * @author luhuashan 视频会议聊天界面
 *
 */
public class MultiVideoconference extends VideoconferenceBaseActivity implements
		View.OnClickListener, CCPMulitVideoUI.OnVideoUIItemClickListener,
		CCPAlertDialog.OnPopuItemClickListener {

	/**
	 * The definition of video conference pattern of joining if the creator or
	 * join Invitation model
	 *
	 * @see #modeType
	 * @see #MODE_VIDEO_C_INITIATED_INTERCOM
	 */
	private static final int MODE_VIDEO_C_INVITATION = 0x0;

	/**
	 * Creator pattern model
	 *
	 * @see #modeType
	 * @see #MODE_VIDEO_C_INVITATION
	 */
	private static final int MODE_VIDEO_C_INITIATED_INTERCOM = 0x1;

	/**
	 * Unique identifier defined message queue
	 *
	 * @see #getBaseHandle()
	 */
	public static final int WHAT_ON_VIDEO_NOTIFY_TIPS = 0X2;

	/**
	 * Unique identifier defined message queue
	 *
	 * @see #getBaseHandle()
	 */
	public static final int WHAT_ON_VIDEO_REFRESH_VIDEOUI = 0X3;

	/**
	 * The definition of the status bar at the top of the transition time to
	 * update the state background
	 */
	public static final int ANIMATION_DURATION = 2000;

	/**
	 * The definition of the status bar at the top of the transition time to
	 * update the state background
	 */
	public static final int ANIMATION_DURATION_RESET = 1000;

	/**
	 *
	 */
	public static final String PREFIX_LOCAL_VIDEO = "local_";

	protected static final String TAG = "MultiVideoconference";

	public HashMap<String, Integer> mVideoMemberUI = new HashMap<String, Integer>();

	private TextView mVideoTips;
	private ImageButton mCameraControl;
	private CCPMulitVideoUI mVideoConUI;
	private ImageButton mMuteControl;
	private ImageButton mVideoControl;

	private FrameLayout mVideoUIRoot;
	private View instructionsView;
	private View videoMainView;
	private Button mExitVideoCon;

	private String mVideoMainScreenVoIP;
	private String mVideoConferenceId;
	private String mVideoCreate;
	private CameraInfo[] cameraInfos;
	// The first rear facing camera
	int defaultCameraId;
	int numberOfCameras;
	int cameraCurrentlyLocked;

	/**
	 * Capbility index of pixel.
	 */
	int mCameraCapbilityIndex;

	private int modeType;
	private boolean isMute = false;
	private boolean isVideoConCreate = false;
	private boolean isVideoChatting = false;
	private boolean mPubish = true;

	private OverflowAdapter.OverflowItem[] mItems;

	// Whether to display all the members including frequency
	@Deprecated
	private boolean isDisplayAllMembers = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initResourceRefs();

		initialize(savedInstanceState);
		if (isVideoConCreate) {

			getTopBarView().setTopBarToStatus(1, R.drawable.topbar_back_bt,
					R.drawable.btn_style_green, null,
					getString(R.string.app_title_right_button_pull_down),
					getString(R.string.videomeeting_chatting), null, this);
			mOverflowHelper = new OverflowHelper(this);
			initOverflowItems();

		} else {

			getTopBarView().setTopBarToStatus(1, R.drawable.topbar_back_bt, -1,
					null, null, getString(R.string.videomeeting_chatting),
					null, this);
		}

		ECVoIPSetupManager voIPSetupManager = ECDevice.getECVoIPSetupManager();
		if (voIPSetupManager == null) {

			finish();
			return;
		}
		cameraInfos = ECDevice.getECVoIPSetupManager().getCameraInfos();

		if (cameraInfos != null) {
			numberOfCameras = cameraInfos.length;
		}

		for (int i = 0; i < numberOfCameras; i++) {
			if (cameraInfos[i].index == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
				defaultCameraId = i;
				mCameraCapbilityIndex = CommomUtil.comportCapabilityIndex(cameraInfos[cameraCurrentlyLocked].caps, 352 * 288);
			}
		}
		ECDevice.getECVoIPSetupManager().enableLoudSpeaker(true);

	}

	void initOverflowItems() {

		if (mItems == null) {
			mItems = new OverflowAdapter.OverflowItem[2];
		}

		mItems[0] = new OverflowAdapter.OverflowItem(
				getString(R.string.videomeeting_invite_voip));
		mItems[1] = new OverflowAdapter.OverflowItem(
				getString(R.string.videomeeting_invite_phone));

	}

	private void initResourceRefs() {
		mVideoTips = (TextView) findViewById(R.id.notice_tips);

		mVideoConUI = (CCPMulitVideoUI) findViewById(R.id.video_ui);
		mVideoConUI.setOnVideoUIItemClickListener(this);

		mCameraControl = (ImageButton) findViewById(R.id.camera_control);
		mMuteControl = (ImageButton) findViewById(R.id.mute_control);
		mVideoControl = (ImageButton) findViewById(R.id.video_control);
		mVideoControl.setVisibility(View.GONE);
		mCameraControl.setOnClickListener(this);
		mMuteControl.setOnClickListener(this);
		mMuteControl.setEnabled(false);
		mCameraControl.setEnabled(false);
		mVideoControl.setOnClickListener(this);

		mExitVideoCon = (Button) findViewById(R.id.out_video_c_submit);
		mExitVideoCon.setOnClickListener(this);

	}

	private OverflowHelper mOverflowHelper;

	private final AdapterView.OnItemClickListener mOverflowItemClicKListener = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			controlPlusSubMenu();

			showInputCodeDialog(null,
					getString(R.string.videomeeting_invite_member),position!=0);

		}

	};


	protected void handleInput(android.widget.EditText editText , boolean isLandCall) {


		if(editText!=null){

			String text=editText.getText().toString().trim();
			if(!TextUtils.isEmpty(text)&&ECDevice.getECMeetingManager()!=null){

				showProcessDialog();
				ECDevice.getECMeetingManager().inviteMembersJoinToMeeting(mVideoConferenceId,new String[]{text}, isLandCall, new OnInviteMembersJoinToMeetingListener() {

					@Override
					public void onInviteMembersJoinToMeeting(ECError reason, String arg1) {
						// TODO Auto-generated method stub

						dismissPostingDialog();
						if(reason.errorCode==SdkErrorCode.REQUEST_SUCCESS){


							ToastUtil.showMessage("邀请成功");
						}else {

							ToastUtil.showMessage("邀请失败,错误码"+reason.errorCode);
						}

					}
				});
			}
		}

	};

	private void controlPlusSubMenu() {
		if (mOverflowHelper == null) {
			return;
		}

		if (mOverflowHelper.isOverflowShowing()) {
			mOverflowHelper.dismiss();
			return;
		}
		mOverflowHelper.setOverflowItems(mItems);
		mOverflowHelper
				.setOnOverflowItemClickListener(mOverflowItemClicKListener);
		mOverflowHelper.showAsDropDown(findViewById(R.id.text_right));
	}



	private void initialize(Bundle savedInstanceState) {
		Intent intent = getIntent();
		String roomName = null;
		boolean is_auto_close = true;
		int autoDelete = 1;
		int voiceMode = 1;
		if (intent.hasExtra(ECGlobalConstants.AUTO_DELETE)) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				autoDelete = extras.getInt(ECGlobalConstants.AUTO_DELETE);
			}
		}
		if (intent.hasExtra(ECGlobalConstants.VOICE_MOD)) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				voiceMode = extras.getInt(ECGlobalConstants.VOICE_MOD);
			}
		}
		if (intent.hasExtra(ECGlobalConstants.IS_AUTO_CLOSE)) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				is_auto_close = extras
						.getBoolean(ECGlobalConstants.IS_AUTO_CLOSE);
			}
		}
		if (intent.hasExtra(ECGlobalConstants.IS_AUTO_CLOSE)) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				is_auto_close = extras
						.getBoolean(ECGlobalConstants.IS_AUTO_CLOSE);
			}
		}
		if (intent.hasExtra(ECGlobalConstants.CHATROOM_NAME)) {
			modeType = MODE_VIDEO_C_INITIATED_INTERCOM;
			Bundle extras = intent.getExtras();
			if (extras != null) {
				roomName = extras.getString(ECGlobalConstants.CHATROOM_NAME);
				if (TextUtils.isEmpty(roomName)) {
					finish();
				} else {
					mVideoCreate = extras
							.getString(VideoconferenceConversation.CONFERENCE_CREATOR);
					isVideoConCreate = CCPAppManager.getUserId().equals(
							mVideoCreate);
					if (!isVideoConCreate && instructionsView != null)
						instructionsView.setVisibility(View.GONE);
				}

			}
		}

		if (intent.hasExtra(ECGlobalConstants.CONFERENCE_ID)) {
			// To invite voice group chat
			modeType = MODE_VIDEO_C_INVITATION;
			Bundle extras = intent.getExtras();
			if (extras != null) {
				mVideoConferenceId = extras
						.getString(ECGlobalConstants.CONFERENCE_ID);
				if (TextUtils.isEmpty(mVideoConferenceId)) {
					finish();
				}
			}

		}

		initUIKey();

		mVideoTips.setText(R.string.top_tips_connecting_wait);

		ECDevice.getECVoIPSetupManager().setVideoView(
				mVideoConUI.getMainSurfaceView(), null);
		if (modeType == MODE_VIDEO_C_INITIATED_INTERCOM) {// 自动创建、加入

			ECCreateMeetingParams.Builder builder = new ECCreateMeetingParams.Builder();
			builder.setMeetingName(roomName).setSquare(5)
					.setVoiceMod(getToneMode(voiceMode))
					.setIsAutoDelete(autoDelete == 1 ? true : false)
					.setIsAutoJoin(true).setKeywords("").setMeetingPwd("")
					.setIsAutoClose(is_auto_close);
			ECCreateMeetingParams params = builder.create();

			if (!checkSDK()) {
				return;
			}

			ECDevice.getECMeetingManager().createMultiMeetingByType(params,
					ECMeetingType.MEETING_MULTI_VIDEO,
					new OnCreateOrJoinMeetingListener() {

						@Override
						public void onCreateOrJoinMeeting(ECError reason,
								String meetingNo) {

							if (reason.errorCode == SdkErrorCode.REQUEST_SUCCESS) {
								mVideoConferenceId = meetingNo;

								refreshUIAfterjoinSuccess(reason, meetingNo);
							} else {

								ToastUtil.showMessage("加入会议失败reason="
										+ reason.errorCode);
								finish();

							}

						}
					});

		} else if (modeType == MODE_VIDEO_C_INVITATION) {// 加入会议
			checkSDK();
			ECDevice.getECMeetingManager().joinMeetingByType(
					mVideoConferenceId, "", ECMeetingType.MEETING_MULTI_VIDEO,
					new OnCreateOrJoinMeetingListener() {

						public void onCreateOrJoinMeeting(ECError arg0,
								String arg1) {
							// TODO Auto-generated method stub

							if (arg0.errorCode == SdkErrorCode.REQUEST_SUCCESS) {

								ECDevice.getECVoIPSetupManager()
										.enableLoudSpeaker(true);
								refreshUIAfterjoinSuccess(arg0, arg1);

							} else {

								ToastUtil.showMessage("加入会议失败reason="
										+ arg0.errorCode);
								finish();
							}

						}
					});

		}

	}

	@SuppressWarnings("unchecked")
	protected void refreshUIAfterjoinSuccess(ECError reason, String conferenceId) {

		if (reason.errorCode == SdkErrorCode.REQUEST_SUCCESS) {

			isVideoChatting = true;
			mVideoConferenceId = conferenceId;
			mExitVideoCon.setEnabled(true);
			mCameraControl.setEnabled(true);
			mMuteControl.setEnabled(true);
			isMute = ECDevice.getECVoIPSetupManager().getMuteStatus();
			initMute();

			updateVideoNoticeTipsUI(getString(R.string.video_tips_joining,
					conferenceId));

			if (!checkSDK()) {
				return;
			}
			ECDevice.getECMeetingManager().queryMeetingMembersByType(
					mVideoConferenceId, ECMeetingType.MEETING_MULTI_VIDEO,
					new OnQueryMeetingMembersListener() {

						@Override
						public void onQueryMeetingMembers(ECError reason,
								List members) {

							if (reason.errorCode == SdkErrorCode.REQUEST_SUCCESS) {

								if (mulitMembers == null) {
									mulitMembers = new ArrayList<MulitVideoMember>();
								}
								mulitMembers.clear();

								if (members == null || members.size() <= 0) {
									return;
								}

								ArrayList<ECVideoMeetingMember> membersNew = (ArrayList<ECVideoMeetingMember>) members;
								for (ECVideoMeetingMember member : membersNew) {
									MulitVideoMember mulitMember = new MulitVideoMember(
											member);
									mulitMembers.add(mulitMember);
								}

								initMembersOnVideoUI(mulitMembers);
							}

						}

					});

		} else {
			isVideoChatting = false;
			checkSDK();
			ECDevice.getECMeetingManager().exitMeeting(
					ECMeetingType.MEETING_MULTI_VIDEO);

			ToastUtil.showMessage(R.string.str_join_video_c_failed_content);
			finish();
		}

	}

	private void initMute() {
		if (isMute) {
			mMuteControl.setImageResource(R.drawable.mute_forbid_selector);
		} else {
			mMuteControl.setImageResource(R.drawable.mute_enable_selector);
		}
	}

	private void setMuteUI() {

		try {
			ECDevice.getECVoIPSetupManager().setMute(!isMute);
			isMute = ECDevice.getECVoIPSetupManager().getMuteStatus();
			initMute();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateVideoNoticeTipsUI(CharSequence text) {

		if (!TextUtils.isEmpty(text)) {
			getBaseHandle().removeMessages(WHAT_ON_VIDEO_NOTIFY_TIPS);
			mVideoTips.setText(text);
			TransitionDrawable transition = (TransitionDrawable) mVideoTips
					.getBackground();
			transition.resetTransition();
			transition.startTransition(ANIMATION_DURATION);
			Message msg = getBaseHandle().obtainMessage(
					WHAT_ON_VIDEO_NOTIFY_TIPS);
			getBaseHandle().sendMessageDelayed(msg, 6000);
		}
	}

	ArrayList<Integer> UIKey = new ArrayList<Integer>();

	private List<MulitVideoMember> mulitMembers;

	private ECAlertDialog buildAlert;

	/**
	 *
	 * <p>
	 * Title: getCCPMulitVideoUIKey
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 *
	 * @return
	 */
	public synchronized Integer getCCPMulitVideoUIKey() {

		if (UIKey.isEmpty()) {
			return null;
		}
		return UIKey.remove(0);
	}

	public synchronized void putCCPMulitVideoUIKey(Integer key) {

		if (UIKey.size() > 4) {
			return;
		}

		if (key <= 2) {
			return;
		}

		UIKey.add(/* key - 3, */key);
		Collections.sort(UIKey, new Comparator<Integer>() {
			@Override
			public int compare(Integer lsdKey, Integer rsdKey) {

				// Apply sort mode
				return lsdKey.compareTo(rsdKey);
			}
		});
	}

	public void putVideoUIMemberCache(MulitVideoMember member, Integer key) {
		synchronized (mVideoMemberUI) {
			putVideoUIMemberCache(member.getNumber(), key);
			if (mulitMembers != null) {
				mulitMembers.add(member);
			}
		}
	}

	/**
	 *
	 * <p>
	 * Title: putVideoUIMemberCache
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 *
	 * @param who
	 * @param key
	 */
	public void putVideoUIMemberCache(String who, Integer key) {
		synchronized (mVideoMemberUI) {
			if (key == CCPMulitVideoUI.LAYOUT_KEY_MAIN_SURFACEVIEW) {
				mVideoMainScreenVoIP = who;
			} else {
				mVideoMemberUI.put(who, key);
			}
		}
	}

	/**
	 *
	 * <p>
	 * Title: isVideoUIMemberCacheEmpty
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 *
	 * @return
	 */
	public boolean isVideoUIMemberCacheEmpty() {
		synchronized (mVideoMemberUI) {
			return mVideoMemberUI.isEmpty();
		}
	}

	/**
	 *
	 * <p>
	 * Title: removeVideoUIMemberFormCache
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 *
	 * @param who
	 * @return
	 */
	public Integer removeVideoUIMemberFormCache(String who) {
		synchronized (mVideoMemberUI) {
			Integer key = mVideoMemberUI.remove(who);
			return key;
		}
	}

	/**
	 *
	 * <p>
	 * Title: queryVideoUIMemberFormCache
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 *
	 * @param who
	 * @return
	 */
	public Integer queryVideoUIMemberFormCache(String who) {
		synchronized (mVideoMemberUI) {
			if (!mVideoMemberUI.containsKey(who)
					&& !who.equals(mVideoMainScreenVoIP)) {
				return null;
			}
			Integer key = null;

			if (mVideoMemberUI.containsKey(who)) {
				key = mVideoMemberUI.get(who);
			} else {

				if (who.equals(mVideoMainScreenVoIP)) {
					key = CCPMulitVideoUI.LAYOUT_KEY_MAIN_SURFACEVIEW;
				}
			}

			return key;
		}
	}

	/**
	 *
	 * <p>
	 * Title: getVideoVoIPByCCPUIKey
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 *
	 * @param CCPUIKey
	 * @return
	 */
	private String getVideoVoIPByCCPUIKey(Integer CCPUIKey) {
		synchronized (mVideoMemberUI) {
			if (CCPUIKey == CCPMulitVideoUI.LAYOUT_KEY_MAIN_SURFACEVIEW) {
				return mVideoMainScreenVoIP;
			}
			if (CCPUIKey != null) {
				for (Map.Entry<String, Integer> entry : mVideoMemberUI
						.entrySet()) {
					if (CCPUIKey.intValue() == entry.getValue().intValue()) {
						return entry.getKey();
					}
				}
			}
			return null;
		}
	}

	/**
	 *
	 * <p>
	 * Title: isVideoUIMemberExist
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 *
	 * @param who
	 * @return
	 */
	private boolean isVideoUIMemberExist(String who) {
		synchronized (mVideoMemberUI) {
			if (TextUtils.isEmpty(who)) {
				return false;
			}

			if (mVideoMemberUI.containsKey(who)) {
				return true;
			}

			return false;
		}
	}

	public synchronized void initUIKey() {
		UIKey.clear();
		UIKey.add(CCPMulitVideoUI.LAYOUT_KEY_SUB_VIEW_1);
		UIKey.add(CCPMulitVideoUI.LAYOUT_KEY_SUB_VIEW_2);
		UIKey.add(CCPMulitVideoUI.LAYOUT_KEY_SUB_VIEW_3);
		UIKey.add(CCPMulitVideoUI.LAYOUT_KEY_SUB_VIEW_4);

	}

	@Override
	protected void onResume() {
		super.onResume();
		DisplayLocalSurfaceView();

		lockScreen();

	}

	private void DisplayLocalSurfaceView() {
		SurfaceView localView = ViERenderer.CreateLocalRenderer(this);
		localView.setZOrderOnTop(false);
		mVideoConUI.setSubSurfaceView(localView);

		cameraCurrentlyLocked = defaultCameraId;

		ECDevice.getECVoIPSetupManager().selectCamera(cameraCurrentlyLocked,
				mCameraCapbilityIndex, 15, Rotate.ROTATE_AUTO, true);

	}

	@Override
	protected boolean isEnableSwipe() {
		// TODO Auto-generated method stub
		return false;
	}

	public void exitOrDismissVideoConference(boolean dismiss) {

		if (dismiss && isVideoConCreate) {

			if (!checkSDK()) {
				return;
			}
			showCustomProcessDialog(getString(R.string.common_progressdialog_title));
			ECDevice.getECMeetingManager().deleteMultiMeetingByType(
					ECMeetingType.MEETING_MULTI_VIDEO, mVideoConferenceId,
					new OnDeleteMeetingListener() {

						public void onMeetingDismiss(ECError reason,
								String meetingNo) {
							// TODO Auto-generated method stub
							dismissPostingDialog();

						}
					});

		} else {
			checkSDK();
			ECDevice.getECMeetingManager().exitMeeting(
					ECMeetingType.MEETING_MULTI_VIDEO);

			if (!isVideoConCreate && dismiss) {
				Intent disIntent = new Intent(
						ECGlobalConstants.INTENT_VIDEO_CONFERENCE_DISMISS);
				disIntent.putExtra(ECGlobalConstants.CONFERENCE_ID,
						mVideoConferenceId);
				sendBroadcast(disIntent);
			}

		}
		finish();
	}

	@Override
	protected void onPause() {
		super.onPause();

		releaseLockScreen();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.out_video_c_submit:

			mExitVideoCon.setEnabled(false);
			doVideoConferenceDisconnect();
			mExitVideoCon.setEnabled(true);
			break;

		case R.id.btn_left:

			mExitVideoCon.setEnabled(false);
			doVideoConferenceDisconnect();
			mExitVideoCon.setEnabled(true);

			break;

		case R.id.camera_control:

			mCameraControl.setEnabled(false);
			// check for availability of multiple cameras
			if (cameraInfos.length == 1) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(this.getString(R.string.camera_alert))
						.setNeutralButton(R.string.dialog_alert_close, null);
				AlertDialog alert = builder.create();
				alert.show();
				return;
			}

			// OK, we have multiple cameras.
			// Release this camera -> cameraCurrentlyLocked
			cameraCurrentlyLocked = (cameraCurrentlyLocked + 1)
					% numberOfCameras;
			mCameraCapbilityIndex = CommomUtil.comportCapabilityIndex(cameraInfos[cameraCurrentlyLocked].caps, 352 * 288);

			if (cameraCurrentlyLocked == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
				defaultCameraId = android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT;
				Toast.makeText(MultiVideoconference.this,
						R.string.camera_switch_front, Toast.LENGTH_SHORT)
						.show();
				mCameraControl
						.setImageResource(R.drawable.camera_switch_back_selector);
			} else {
				Toast.makeText(MultiVideoconference.this,
						R.string.camera_switch_back, Toast.LENGTH_SHORT).show();
				defaultCameraId = android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK;
				mCameraControl
						.setImageResource(R.drawable.camera_switch_font_selector);

			}

			ECDevice.getECVoIPSetupManager().selectCamera(
					cameraCurrentlyLocked, mCameraCapbilityIndex, 15,
					Rotate.ROTATE_AUTO, false);

			mCameraControl.setEnabled(true);
			break;

		case R.id.mute_control:

			setMuteUI();
			break;
		case R.id.text_right:

			controlPlusSubMenu();

			break;

		default:
			break;
		}
	}

	public void showIOSAlert(String content) {

		buildAlert = ECAlertDialog.buildAlert(this, content,
				getString(R.string.dialog_btn_confim),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						buildAlert.dismiss();
					}
				});
		buildAlert.setTitle("提示");
		buildAlert.setCanceledOnTouchOutside(true);
		buildAlert.show();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mVideoMemberUI != null) {
			mVideoMemberUI.clear();
			mVideoMemberUI = null;
		}

		if (mVideoConUI != null) {
			mVideoConUI.release();
			mVideoConUI = null;
		}

		instructionsView = null;
		videoMainView = null;

		mVideoConferenceId = null;
		mVideoCreate = null;
		cameraInfos = null;

		// The first rear facing camera
		isMute = false;
		isVideoConCreate = false;
		isVideoChatting = false;
		ECDevice.getECVoIPSetupManager().enableLoudSpeaker(false);

	}

	/**
	 * Set the main video, if the main video new set of main video server ID and
	 * current ID the same, will cancel the compulsory primary video, becomes
	 * automatic switching mode. So you can also use this method to achieve the
	 * "abolition of compulsory primary video" function.
	 */
	@Override
	public void onVideoUIItemClick(int CCPUIKey) {

		int[] ccpAlertResArray = new int[2];
		int title = 0;
		if (CCPUIKey == CCPMulitVideoUI.LAYOUT_KEY_SUB_SURFACEVIEW) {
			// If he is the create of Video Conference
			// The main object is not myself
			if (!mPubish) {
				ccpAlertResArray[0] = R.string.video_publish_video_frame;
			} else {
				ccpAlertResArray[0] = R.string.video_unpublish_video_frame;
			}
			if (isVideoConCreate) {
				ccpAlertResArray[1] = R.string.video_c_dismiss;
			} else {
				ccpAlertResArray[1] = R.string.video_c_logout;
			}
		} else {
			String who = getVideoVoIPByCCPUIKey(CCPUIKey);
			MulitVideoMember mulitVideoMember = getMulitVideoMember(who);
			if (mulitVideoMember == null) {
				return;
			}

			ccpAlertResArray = new int[2];
			if (mulitVideoMember.isRequestVideoFrame()) {
				ccpAlertResArray[0] = R.string.str_quxiao_sp;
			} else {
				ccpAlertResArray[0] = R.string.str_request_sp;
			}
			if (who.equals(mVideoMainScreenVoIP)) {
				ccpAlertResArray[1] = R.string.str_xiao;
			} else {
				ccpAlertResArray[1] = R.string.str_da;
			}
			if (isVideoConCreate) {
				int[] _arr = new int[] { ccpAlertResArray[0],
						ccpAlertResArray[1], R.string.str_video_manager_remove };
				ccpAlertResArray = _arr;
			}
		}
		CCPAlertDialog ccpAlertDialog = new CCPAlertDialog(
				MultiVideoconference.this, title, ccpAlertResArray, 0,
				R.string.dialog_cancle_btn);

		// set CCP UIKey
		ccpAlertDialog.setUserData(CCPUIKey);
		ccpAlertDialog.setOnItemClickListener(this);
		ccpAlertDialog.create();
		ccpAlertDialog.show();
	}

	/**
	 *
	 * @param who
	 * @return
	 */
	public MulitVideoMember getMulitVideoMember(String who) {

		if (mulitMembers == null) {
			return null;
		}
		for (MulitVideoMember member : mulitMembers) {
			if (member != null && who.equals(member.getNumber())) {
				return member;
			}
		}
		return null;
	}

	public void onItemClick(ListView parent, View view, int position,
			int resourceId) {
		switch (resourceId) {
		case R.string.video_c_logout:
			exitOrDismissVideoConference(false);
			break;
		case R.string.video_c_dismiss:
			exitOrDismissVideoConference(isVideoConCreate);
			break;
		case R.string.video_publish_video_frame:
		case R.string.video_unpublish_video_frame:

			if (mPubish) {// 如果是发布状态、就取消发布、反之亦然

				if (!checkSDK()) {
					return;
				}
				showProcessDialog();
				ECDevice.getECMeetingManager()
						.cancelPublishSelfVideoFrameInVideoMeeting(
								mVideoConferenceId,
								new OnSelfVideoFrameChangedListener() {

									public void onSelfVideoFrameChanged(
											boolean isPublish, ECError reason) {
										dismissPostingDialog();

										if (reason.errorCode == SdkErrorCode.REQUEST_SUCCESS) {
											mPubish = false;
										} else {
											ToastUtil.showMessage("操作失败,错误码"
													+ reason.errorCode);
										}

									}
								});

			} else {

				if (!checkSDK()) {
					return;
				}
				showProcessDialog();

				ECDevice.getECMeetingManager()
						.publishSelfVideoFrameInVideoMeeting(
								mVideoConferenceId,
								new OnSelfVideoFrameChangedListener() {

									public void onSelfVideoFrameChanged(
											boolean isPublish, ECError reason) {
										dismissPostingDialog();

										if (reason.errorCode == SdkErrorCode.REQUEST_SUCCESS) {
											mPubish = true;
										} else {
											ToastUtil.showMessage("操作失败");
										}

									}
								});

			}

			break;
		case R.string.str_da:
		case R.string.str_xiao:

			Integer CCPUIKey = (Integer) view.getTag();
			String voipSwitch = getVideoVoIPByCCPUIKey(CCPUIKey);
			if (TextUtils.isEmpty(voipSwitch)) {
				return;
			}
			MulitVideoMember mulitVideoMember = getMulitVideoMember(voipSwitch);
			if (mulitVideoMember == null) {
				return;
			}
			if (TextUtils.isEmpty(mVideoMainScreenVoIP)) {
				doChangeVideoFrameSurfaceViewRequest(
						mVideoConUI.getMainSurfaceView(), mulitVideoMember);
				SurfaceView surfaceView = mVideoConUI.getSurfaceView(CCPUIKey);
				clearScreen(surfaceView);
				mVideoMainScreenVoIP = mulitVideoMember.getNumber();
				return;
			}

			if (!TextUtils.isEmpty(mVideoMainScreenVoIP)
					&& mVideoMainScreenVoIP.equals(voipSwitch)) {
				SurfaceView surfaceView = mVideoConUI.getSurfaceView(CCPUIKey
						.intValue());
				clearScreen(mVideoConUI.getMainSurfaceView());
				doChangeVideoFrameSurfaceViewRequest(surfaceView,
						mulitVideoMember);
				clearScreen(mVideoConUI.getMainSurfaceView());
				mVideoMainScreenVoIP = null;
				surfaceView.refreshDrawableState();

				return;
			}

			if (!TextUtils.isEmpty(mVideoMainScreenVoIP)
					&& !mVideoMainScreenVoIP.equals(voipSwitch)) {
				// remove src
				Integer indexKey = queryVideoUIMemberFormCache(mVideoMainScreenVoIP);
				SurfaceView surfaceView = mVideoConUI.getSurfaceView(indexKey);
				doChangeVideoFrameSurfaceViewRequest(surfaceView,
						getMulitVideoMember(mVideoMainScreenVoIP));

				// set
				doChangeVideoFrameSurfaceViewRequest(
						mVideoConUI.getMainSurfaceView(), mulitVideoMember);
				SurfaceView srcSurfaceView = mVideoConUI
						.getSurfaceView(CCPUIKey);
				clearScreen(srcSurfaceView);
				mVideoMainScreenVoIP = mulitVideoMember.getNumber();

			}
			break;
		case R.string.str_quxiao_sp:
		case R.string.str_request_sp: // ??
			Integer CCPUIKey2 = (Integer) view.getTag();
			String voipSwitch2 = null;
			if (CCPUIKey2.intValue() == CCPMulitVideoUI.LAYOUT_KEY_SUB_SURFACEVIEW) {
				voipSwitch2 = CCPAppManager.getUserId();
			} else {
				voipSwitch2 = getVideoVoIPByCCPUIKey(CCPUIKey2);
			}

			if (TextUtils.isEmpty(voipSwitch2)) {
				return;
			}
			MulitVideoMember mulitVideoMember2 = getMulitVideoMember(voipSwitch2);
			doHandlerMemberVideoFrameRequest(CCPUIKey2, mulitVideoMember2);
			break;
		// The members will be removed from the video conference
		case R.string.str_video_manager_remove:
			Integer CCPUIKeyRemove = (Integer) view.getTag();
			String voipRemove = getVideoVoIPByCCPUIKey(CCPUIKeyRemove);
			MulitVideoMember mulitVideoMember1 = getMulitVideoMember(voipRemove);
			boolean isMobile = (mulitVideoMember1 == null)? false : mulitVideoMember1.isMobile();
			if(isMobile && voipRemove != null) {
				voipRemove = voipRemove.replace("m" , "");
			}
			if (!TextUtils.isEmpty(voipRemove)) {
				showCustomProcessDialog(getString(R.string.common_progressdialog_title));
				checkSDK();
				ECDevice.getECMeetingManager()
						.removeMemberFromMultiMeetingByType(
								ECMeetingType.MEETING_MULTI_VIDEO,
								mVideoConferenceId, voipRemove, isMobile,
								new OnRemoveMemberFromMeetingListener() {

									public void onRemoveMemberFromMeeting(
											ECError reason, String member) {

										dismissPostingDialog();
										if (reason.errorCode == SdkErrorCode.REQUEST_SUCCESS) {

										} else {
											ToastUtil
													.showMessage("移除成员失败reason="
															+ reason.errorCode);
										}

									}
								});

			}
			break;
		case R.string.dialog_cancle_btn:

			break;
		}
	}

	/**
	 * @param CCPUIKey
	 * @param mulitVideoMember
	 */
	private void doHandlerMemberVideoFrameRequest(Integer CCPUIKey,
			MulitVideoMember mulitVideoMember) {
		if (mulitVideoMember != null) {
			if (mVideoConferenceId.length() > 8) {

				if (!mulitVideoMember.isRequestVideoFrame()) {
					SurfaceView surfaceView = mVideoConUI
							.getSurfaceView(CCPUIKey.intValue());

					if (!checkSDK()) {
						return;
					}
					int result = ECDevice.getECMeetingManager()
							.requestMemberVideoInVideoMeeting(
									mVideoConferenceId, "",
									mulitVideoMember.getNumber(), surfaceView,
									mulitVideoMember.getIp(),
									mulitVideoMember.getPort(),
									new OnMemberVideoFrameChangedListener() {

										@Override
										public void onMemberVideoFrameChanged(
												boolean arg0, ECError arg1,
												String arg2, String arg3) {
											// TODO Auto-generated method stub

										}


									});

					LogUtil.e(TAG, "result=" + result);
					if (result == 0) {
						mulitVideoMember.setRequestVideoFrame(true);
					}

				} else {
					checkSDK();
					int num = ECDevice.getECMeetingManager()
							.cancelRequestMemberVideoInVideoMeeting(
									mVideoConferenceId, "",
									mulitVideoMember.getNumber(),
									new OnMemberVideoFrameChangedListener() {

										@Override
										public void onMemberVideoFrameChanged(
												boolean arg0, ECError arg1,
												String arg2, String arg3) {
											// TODO Auto-generated method stub

										}



									});
					if (num == 0) {
						mulitVideoMember.setRequestVideoFrame(false);
					}

				}
			}

		}
	}

	private void clearScreen(SurfaceView surfaceView) {
		Paint p = new Paint();
		// 清屏
		p.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
		Canvas lockCanvas = surfaceView.getHolder().lockCanvas();
		lockCanvas.drawPaint(p);
		p.setXfermode(new PorterDuffXfermode(Mode.SRC));
		surfaceView.getHolder().unlockCanvasAndPost(lockCanvas);
		surfaceView.invalidate();
	}

	/**
	 *
	 * @param view
	 * @param mulitVideoMember
	 */
	private void doChangeVideoFrameSurfaceViewRequest(SurfaceView view,
			MulitVideoMember mulitVideoMember) {
		if (mulitVideoMember != null) {
			if (mVideoConferenceId.length() > 8) {
				String id = mVideoConferenceId.substring(
						mVideoConferenceId.length() - 8,
						mVideoConferenceId.length());
				if (mulitVideoMember.isRequestVideoFrame()) {
					SurfaceView surfaceView = view;

					surfaceView.getHolder().setFixedSize(
							mulitVideoMember.getWidth(),
							mulitVideoMember.getHeight());

					if (!checkSDK()) {
						return;
					}

					ECDevice.getECMeetingManager().resetVideoMeetingWindow(
							mulitVideoMember.getNumber(), surfaceView);
				}

			}

		}
	}

	/**
	 *
	 * <p>
	 * Title: doVideoConferenceDisconnect
	 * </p>
	 * <p>
	 * Description: The end of processing video conference popu menu list
	 * </p>
	 *
	 * @see CCPAlertDialog#CCPAlertDialog(Context, int, int[], int, int)
	 */
	private void doVideoConferenceDisconnect() {
		int videoTips = R.string.video_c_logout_warning_tip;
		int videoExit = R.string.video_c_logout;

		CCPAlertDialog ccpAlertDialog = new CCPAlertDialog(
				MultiVideoconference.this, videoTips, null, videoExit,
				R.string.dialog_cancle_btn);
		ccpAlertDialog.setOnItemClickListener(this);
		ccpAlertDialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
			}
		});
		ccpAlertDialog.create();
		ccpAlertDialog.show();
	}

	/**
	 *
	 * <p>
	 * Title: setVideoUITextOperable
	 * </p>
	 * <p>
	 * Description: Unified setting for VideoUI text display, and set whether
	 * can click operation
	 * </p>
	 *
	 * @param CCPMulitVideoUIKey
	 * @see CCPMulitVideoUI#setOnVideoUIItemClickListener(OnVideoUIItemClickListener
	 *      l)
	 */
	private void setVideoUITextOperable(Integer CCPMulitVideoUIKey,
			MulitVideoMember member) {

		mVideoConUI.setVideoMember(CCPMulitVideoUIKey, member);

	}

	/**
	 *
	 * <p>
	 * Title: removeMemberFormVideoUI
	 * </p>
	 * <p>
	 * Description: remove the member of Video Conference form VideoUI
	 * </p>
	 *
	 * @param who
	 */
	private void removeMemberFormVideoUI(String who) {
		Integer CCPMulitVideoUIKey = removeVideoUIMemberFormCache(who);
		removeMemberMulitCache(who);
		removeMemberFromVideoUI(CCPMulitVideoUIKey);

	}

	private void removeMemberMulitCache(String who) {

		if (mulitMembers == null) {
			return;
		}
		MulitVideoMember removeMember = null;
		for (MulitVideoMember mulitVideoMember : mulitMembers) {
			if (mulitVideoMember != null
					&& mulitVideoMember.getNumber().equals(who)) {
				removeMember = mulitVideoMember;
				break;
			}
		}

		if (removeMember != null) {
			mulitMembers.remove(removeMember);
		}
	}

	private void removeMemberFromVideoUI(Integer CCPMulitVideoUIKey) {
		if (CCPMulitVideoUIKey != null) {
			putCCPMulitVideoUIKey(CCPMulitVideoUIKey);
			setVideoUITextOperable(CCPMulitVideoUIKey, null);
		}
	}

	private void initMembersOnVideoUI(List<MulitVideoMember> members) {
		synchronized (mVideoMemberUI) {

			for (final MulitVideoMember member : members) {
				Integer CCPMulitVideoUIKey = null;
				if (CCPAppManager.getUserId().equals(member.getNumber())) {
					CCPMulitVideoUIKey = CCPMulitVideoUI.LAYOUT_KEY_SUB_SURFACEVIEW;
				} else {

					CCPMulitVideoUIKey = getCCPMulitVideoUIKey();
				}

				if (CCPMulitVideoUIKey == null) {
					break;
				}

				putVideoUIMemberCache(member.getNumber(), CCPMulitVideoUIKey);
				if (!CCPAppManager.getUserId().equals(member.getNumber())) {
					doHandlerMemberVideoFrameRequest(CCPMulitVideoUIKey, member);

				}

				final int key = CCPMulitVideoUIKey.intValue();
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						setVideoUITextOperable(key, member);
					}
				});
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			if (isVideoChatting) {
				doVideoConferenceDisconnect();
			}
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void handleDialogOkEvent(int requestKey) {
		super.handleDialogOkEvent(requestKey);

		if (DIALOG_SHOW_KEY_DISSMISS_VIDEO == requestKey) {
			exitOrDismissVideoConference(true);
		} else if (DIALOG_SHOW_KEY_REMOVE_VIDEO == requestKey) {
			// The removed member of the video conference is self.
			exitOrDismissVideoConference(false);
		}
	}

	// -----------------------------------------------------SDK Callback
	// ---------------------------

	@Override
	protected void handleVideoConferenceDismiss(int reason, String conferenceId) {
		super.handleVideoConferenceDismiss(reason, conferenceId);

		dismissPostingDialog();

		if (reason != 0 && (reason != 111805)) {
			Toast.makeText(MultiVideoconference.this,
					getString(R.string.toast_video_dismiss_result, reason),
					Toast.LENGTH_SHORT).show();
			return;
		}

		exitOrDismissVideoConference(false);
	}

	@Override
	protected void handleReceiveVideoConferenceMsg(ECVideoMeetingMsg VideoMsg) {
		super.handleReceiveVideoConferenceMsg(VideoMsg);

		synchronized (MultiVideoconference.class) {
			try {

				if (TextUtils.isEmpty(mVideoConferenceId)) {
					return;
				}

				// if the Video Conference ID is empty .then The next step
				if (VideoMsg == null
						|| !mVideoConferenceId.equals(VideoMsg.getMeetingNo())) {
					return;
				}

				if (VideoMsg instanceof ECVideoMeetingJoinMsg) {

					ECVideoMeetingJoinMsg videoJoinMessage = (ECVideoMeetingJoinMsg) VideoMsg;
					String[] whos = videoJoinMessage.getWhos();

					for (String who : whos) {
						if(videoJoinMessage.isMobile()) {
							who = "m" + who;
						}
						if (isVideoUIMemberExist(who)) {
							continue;
						}
						if (CCPAppManager.getUserId().equals(who)) {
							continue;
						}

						// has Somebody join
						Integer CCPMulitVideoUIKey = getCCPMulitVideoUIKey();
						if (CCPMulitVideoUIKey == null) {
							return;
						}

						MulitVideoMember member = new MulitVideoMember();
						member.setNumber(who);
						member.setIp(videoJoinMessage.getIp());
						member.setPort(videoJoinMessage.getPort());
						member.setIsMobile(videoJoinMessage.isMobile());
						member.setPublish(videoJoinMessage.isPublish());
						putVideoUIMemberCache(member, CCPMulitVideoUIKey);

						// If there is no image, then show the account
						// information also.
						setVideoUITextOperable(CCPMulitVideoUIKey, member);
						updateVideoNoticeTipsUI(getString(
								R.string.str_video_conference_join, who));
						doHandlerMemberVideoFrameRequest(CCPMulitVideoUIKey,
								member);
					}

					// some one exit Video Conference..
				} else if (VideoMsg instanceof ECVideoMeetingExitMsg) {
					ECVideoMeetingExitMsg videoExitMessage = (ECVideoMeetingExitMsg) VideoMsg;
					String[] whos = videoExitMessage.getWhos();
					for (String who : whos) {
						if(videoExitMessage.isMobile()) {
							who = "m" + who;
						}
						// remove the member of Video Conference form VideoUI
						removeMemberFormVideoUI(who);
						updateVideoNoticeTipsUI(getString(
								R.string.str_video_conference_exit, who));

					}

				} else if (VideoMsg instanceof ECVideoMeetingDeleteMsg) {

					if (isVideoConCreate) {

						return;
					}
					ECVideoMeetingDeleteMsg videoConferenceDismissMsg = (ECVideoMeetingDeleteMsg) VideoMsg;
					if (videoConferenceDismissMsg.getMeetingNo().equals(
							mVideoConferenceId)) {
						showAlertTipsDialog(
								DIALOG_SHOW_KEY_DISSMISS_VIDEO,
								getString(R.string.dialog_title_be_dissmiss_video_conference),
								getString(R.string.dialog_message_be_dissmiss_video_conference),
								getString(R.string.dialog_btn), null);
					}

				} else if (VideoMsg instanceof ECVideoMeetingRemoveMemberMsg) {
					// The creator to remove a member(PUSH to all staff room)
					ECVideoMeetingRemoveMemberMsg vCRemoveMemberMsg = (ECVideoMeetingRemoveMemberMsg) VideoMsg;

					if (CCPAppManager.getUserId().equals(vCRemoveMemberMsg.getWho()) && !vCRemoveMemberMsg.isMobile()) {
						// The removed member of the video conference is self.
						showAlertTipsDialog(
								DIALOG_SHOW_KEY_REMOVE_VIDEO,
								getString(R.string.str_system_message_remove_v_title),
								getString(R.string.str_system_message_remove_v_message),
								getString(R.string.dialog_btn), null);
					} else {
						if(vCRemoveMemberMsg.isMobile()) {
							removeMemberFormVideoUI("m" + vCRemoveMemberMsg.getWho());
						} else {
							removeMemberFormVideoUI(vCRemoveMemberMsg.getWho());
						}
					}

				} else if (VideoMsg instanceof ECVideoMeetingVideoFrameActionMsg) {

					ECVideoMeetingVideoFrameActionMsg msg = (ECVideoMeetingVideoFrameActionMsg) VideoMsg;

					if (msg != null
							&& msg.getMember()
									.equals(CCPAppManager.getUserId())) {
						return;
					}

					if (msg.isPublish()) {

						showIOSAlert(getString(
								R.string.videomeeting_member_publish,
								msg.getMember()));

					} else {
						showIOSAlert(getString(
								R.string.videomeeting_member_unpublish,
								msg.getMember()));

					}
				} else if (VideoMsg instanceof ECVideoMeetingRejectMsg) {
					onVideoMeetingRejectMsg((ECVideoMeetingRejectMsg) VideoMsg);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void onVideoMeetingRejectMsg(ECVideoMeetingRejectMsg msg) {
		if(msg == null) {
			return ;
		}
		ECAlertDialog buildAlert = ECAlertDialog.buildAlert(MultiVideoconference.this
				, getString(R.string.meeting_invite_reject , msg.getWho()),
				getString(R.string.dialog_btn_confim), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		buildAlert.setTitle(R.string.app_tip);
		buildAlert.setCanceledOnTouchOutside(false);
		buildAlert.show();
	}

	@Override
	protected void handleSwitchRealScreenToVoip(int reason) {
		super.handleSwitchRealScreenToVoip(reason);
		dismissPostingDialog();
		if (reason != 0) {
			Toast.makeText(getApplicationContext(),
					getString(R.string.str_video_switch_failed, reason),
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void handleVideoConferenceRemoveMember(int reason, String member) {
		super.handleVideoConferenceRemoveMember(reason, member);
		dismissPostingDialog();
		if (reason != 0) {
			Toast.makeText(
					getApplicationContext(),
					getString(R.string.str_video_remove_failed, member, reason),
					Toast.LENGTH_SHORT).show();
			return;
		}
	}

	@Override
	protected void handleNotifyMessage(Message msg) {
		super.handleNotifyMessage(msg);

		int what = msg.what;
		if (what == WHAT_ON_VIDEO_NOTIFY_TIPS) {
			mVideoTips.setText(getString(R.string.video_tips_joining,
					mVideoConferenceId));
			TransitionDrawable transition = (TransitionDrawable) mVideoTips
					.getBackground();
			transition.reverseTransition(ANIMATION_DURATION_RESET);

		}

	}

	/**
	 *
	 * <p>
	 * Title: MultiVideoconference.java
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 * <p>
	 * Copyright: Copyright (c) 2007
	 * </p>
	 * <p>
	 * Company: http://www.cloopen.com
	 * </p>
	 *
	 * @author zhanjichun
	 * @date 2013-11-7
	 * @version 1.0
	 */
	class CCPFilenameFilter implements FilenameFilter {

		String fileName = null;

		public CCPFilenameFilter(String fileNoExtensionNoDot) {
			fileName = fileNoExtensionNoDot;
		}

		@Override
		public boolean accept(File dir, String filename) {

			return filename.startsWith(fileName);
		}

	}

	@Override
	protected int getLayoutId() {
		return R.layout.mulit_video_conference;
	}

	@Override
	protected void handleVideoRatioChanged(String voip, int width, int height) {

		if (TextUtils.isEmpty(voip)) {
			return;
		}
		final Integer key = queryVideoUIMemberFormCache(voip);
		if(key == null) {
			return ;
		}
		MulitVideoMember mulitVideoMember = getMulitVideoMember(voip);
		if (mulitVideoMember != null) {
			mulitVideoMember.setWidth(width);
			mulitVideoMember.setHeight(height);
		}
		final int _width = width;
		final int _height = height;
		getHandler().post(new Runnable() {

			@Override
			public void run() {
				if (mVideoConUI != null) {
					SurfaceView surfaceView = mVideoConUI.getSurfaceView(key
							.intValue());
					if (surfaceView != null) {
						surfaceView.getHolder().setFixedSize(_width, _height);
						LayoutParams layoutParams = surfaceView
								.getLayoutParams();
						// layoutParams.width=_width;
						// layoutParams.height=_height;
						// surfaceView.setLayoutParams(layoutParams);
					}
				}
			}
		});
	}

}
