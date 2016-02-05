/*
 *  Copyright (c) 2015 The CCP project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a Beijing Speedtong Information Technology Co.,Ltd license
 *  that can be found in the LICENSE file in the root of the web site.
 *
 *   http://www.yuntongxun.com
 *
 *  An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */
package com.yishiji.angelplatform.ui.mainfragment;

import android.content.BroadcastReceiver;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.umeng.analytics.MobclickAgent;
import com.yishiji.angelplatform.ui.MainActivity;
import com.yishiji.angelplatform.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.ECContentObservers;
import com.yuntongxun.ecdemo.common.base.CCPCustomViewPager;
import com.yuntongxun.ecdemo.common.base.CCPLauncherUITabView;
import com.yuntongxun.ecdemo.common.base.OverflowAdapter;
import com.yuntongxun.ecdemo.common.base.OverflowAdapter.OverflowItem;
import com.yuntongxun.ecdemo.common.base.OverflowHelper;
import com.yuntongxun.ecdemo.common.dialog.ECAlertDialog;
import com.yuntongxun.ecdemo.common.dialog.ECProgressDialog;
import com.yuntongxun.ecdemo.common.utils.CrashHandler;
import com.yuntongxun.ecdemo.common.utils.DemoUtils;
import com.yuntongxun.ecdemo.common.utils.ECNotificationManager;
import com.yuntongxun.ecdemo.common.utils.ECPreferenceSettings;
import com.yuntongxun.ecdemo.common.utils.ECPreferences;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecdemo.core.ClientUser;
import com.yuntongxun.ecdemo.core.ContactsCache;
import com.yuntongxun.ecdemo.storage.ContactSqlManager;
import com.yuntongxun.ecdemo.storage.GroupNoticeSqlManager;
import com.yuntongxun.ecdemo.storage.GroupSqlManager;
import com.yuntongxun.ecdemo.storage.IMessageSqlManager;
import com.yuntongxun.ecdemo.ui.ActivityTransition;
import com.yuntongxun.ecdemo.ui.BaseFragment;
import com.yuntongxun.ecdemo.ui.ConversationListFragment;
import com.yuntongxun.ecdemo.ui.DiscussionListFragment;
import com.yuntongxun.ecdemo.ui.ECFragmentActivity;
import com.yuntongxun.ecdemo.ui.GroupListFragment;
import com.yuntongxun.ecdemo.ui.SDKCoreHelper;
import com.yuntongxun.ecdemo.ui.TabFragment;
import com.yuntongxun.ecdemo.ui.account.LoginActivity;
import com.yuntongxun.ecdemo.ui.chatting.ChattingActivity;
import com.yuntongxun.ecdemo.ui.chatting.CustomerServiceHelper;
import com.yuntongxun.ecdemo.ui.chatting.IMChattingHelper;
import com.yuntongxun.ecdemo.ui.contact.ECContacts;
import com.yuntongxun.ecdemo.ui.contact.MobileContactActivity;
import com.yuntongxun.ecdemo.ui.contact.MobileContactSelectActivity;
import com.yuntongxun.ecdemo.ui.group.BaseSearch;
import com.yuntongxun.ecdemo.ui.group.CreateGroupActivity;
import com.yuntongxun.ecdemo.ui.group.ECDiscussionActivity;
import com.yuntongxun.ecdemo.ui.group.GroupNoticeActivity;
import com.yuntongxun.ecdemo.ui.interphone.InterPhoneListActivity;
import com.yuntongxun.ecdemo.ui.meeting.MeetingListActivity;
import com.yuntongxun.ecdemo.ui.settings.SettingPersionInfoActivity;
import com.yuntongxun.ecdemo.ui.settings.SettingsActivity;
import com.yuntongxun.ecdemo.ui.videomeeting.VideoconferenceConversation;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.im.ECGroup;
import com.yuntongxun.ecsdk.platformtools.ECHandlerHelper;

import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;

import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 主界面（消息会话界面、联系人界面、群组界面）
 */
@ActivityTransition(3)
public class ChatFragment extends BaseFragment implements
		View.OnClickListener,
		ConversationListFragment.OnUpdateMsgUnreadCountsListener {

	private static final String TAG = "ChatFragment";

	/**
	 * 当前ECLauncherUI实例产生个数
	 */
	public static int mLauncherInstanceCount = 0;

	/**
	 * 当前主界面RootView
	 */
	public View mLauncherView;

	/**
	 * LauncherUI 主界面导航控制View ,包含三个View Tab按钮
	 */
	public CCPLauncherUITabView mLauncherUITabView;
	/**
	 * 三个TabView所对应的三个页面的适配器
	 */
	private CCPCustomViewPager mCustomViewPager;

	/**
	 * 沟通、联系人、群组适配器
	 */
	public LauncherViewPagerAdapter mLauncherViewPagerAdapter;

	private OverflowHelper mOverflowHelper;

	/**
	 * 当前显示的TabView Fragment
	 */
	private int mCurrentItemPosition = -1;

	/**
	 * 会话界面(沟通)
	 */
	private static final int TAB_CONVERSATION = 0;

	/**
	 * 通讯录界面(联系人)
	 */
	private static final int TAB_ADDRESS = 1;

	/**
	 * 群组界面
	 */
	private static final int TAB_GROUP = 2;
	private static final int TAB_DISCUSSION_GROUP = 3;

	/**
	 * {@link CCPLauncherUITabView} 是否已经被初始化
	 */
	private boolean mTabViewInit = false;

	/**
	 * 缓存三个TabView
	 */
	private final HashMap<Integer, Fragment> mTabViewCache = new HashMap<Integer, Fragment>();
	private OverflowItem[] mItems;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
//		View view=inflater.inflate(R.layout.main_tab,null);
//		return view;
	}
	@Override
	protected int getLayoutId() {
		return R.layout.main_tab;
	}

	@Override
	public int getTitleLayoutId() {
		return -1;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		EventBus.getDefault().register(this);
		mOverflowHelper = new OverflowHelper(getActivity());
		initLauncherUIView();
	}

	@Override
	protected void handleReceiver(Context context, Intent intent) {
		super.handleReceiver(context, intent);
		if (intent == null || TextUtils.isEmpty(intent.getAction())) {
			return;
		}
		LogUtil.d(TAG, "[onReceive] action:" + intent.getAction());
		if (IMChattingHelper.INTENT_ACTION_SYNC_MESSAGE.equals(intent
				.getAction())) {
			disPostingLoading();
		} else if (SDKCoreHelper.ACTION_SDK_CONNECT.equals(intent
				.getAction())) {
			doInitAction();
			// tetstMesge();
			BaseFragment tabView = getTabView(TAB_CONVERSATION);
			if (tabView != null
					&& tabView instanceof ConversationListFragment) {
				((ConversationListFragment) tabView).updateConnectState();
			}
		} else if (SDKCoreHelper.ACTION_KICK_OFF.equals(intent.getAction())) {
			String kickoffText = intent.getStringExtra("kickoffText");
//			handlerKickOff(kickoffText);
		}
	}

//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		int pid = android.os.Process.myPid();
//		mLauncherInstanceCount++;
//		super.onCreate(savedInstanceState);
//		initWelcome();
//		mOverflowHelper = new OverflowHelper(this);
//		// umeng
//		MobclickAgent.updateOnlineConfig(this);
//		MobclickAgent.setDebugMode(true);
//		// 设置页面默认为竖屏
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//		ECContentObservers.getInstance().initContentObserver();
//	}


	private boolean mInit = false;

//	private Runnable initRunnable = new Runnable() {
//		@Override
//		public void run() {
//			mInit = false;
//			initLauncherUIView();
//		}
//	};
	/**
	 * 初始化主界面UI视图
	 */
	private void initLauncherUIView() {
		mTabViewInit = true;
		mCustomViewPager = (CCPCustomViewPager) findViewById(R.id.pager);
		mCustomViewPager.setOffscreenPageLimit(4);

		if (mLauncherUITabView != null) {
			mLauncherUITabView.setOnUITabViewClickListener(null);
			mLauncherUITabView.setVisibility(View.VISIBLE);
		}
		mLauncherUITabView = (CCPLauncherUITabView) findViewById(R.id.laucher_tab_top);
		mCustomViewPager.setSlideEnabled(true);
		mLauncherViewPagerAdapter = new LauncherViewPagerAdapter(getActivity(),
				mCustomViewPager);
		mLauncherUITabView
				.setOnUITabViewClickListener(mLauncherViewPagerAdapter);

		findViewById(R.id.btn_plus).setOnClickListener(this);
		ctrlViewTab(0);
	}

	private void settingPersionInfo() {
		if (IMChattingHelper.getInstance().mServicePersonVersion == 0
				&& CCPAppManager.getClientUser().getpVersion() == 0) {
			Intent settingAction = new Intent(getActivity(),
					SettingPersionInfoActivity.class);
			settingAction.putExtra("from_regist", true);
			startActivityForResult(settingAction, 0x2a);
			return;
		}
	}

	/**
	 * 检测离线消息
	 */
	private void checkOffineMessage() {
		if (SDKCoreHelper.getConnectState() != ECDevice.ECConnectState.CONNECT_SUCCESS) {
			return;
		}
		ECHandlerHelper handlerHelper = new ECHandlerHelper();
		handlerHelper.postDelayedRunnOnThead(new Runnable() {
			@Override
			public void run() {
				boolean result = IMChattingHelper.isSyncOffline();
				if (!result) {
					ECHandlerHelper.postRunnOnUI(new Runnable() {
						@Override
						public void run() {
							disPostingLoading();
						}
					});
					IMChattingHelper.checkDownFailMsg();
				}
			}
		}, 1000);
	}

	private boolean isFirstUse() {
		boolean firstUse = ECPreferences.getSharedPreferences().getBoolean(
				ECPreferenceSettings.SETTINGS_FIRST_USE.getId(),
				((Boolean) ECPreferenceSettings.SETTINGS_FIRST_USE
						.getDefaultValue()).booleanValue());
		return firstUse;
	}

//	private void checkFirstUse() {
//		boolean firstUse = isFirstUse();
//
//		// Display the welcome message?
//		if (firstUse) {
//			if (IMChattingHelper.isSyncOffline()) {
//				mPostingdialog = new ECProgressDialog(this,
//						R.string.tab_loading);
//				mPostingdialog.setCanceledOnTouchOutside(false);
//				mPostingdialog.setCancelable(false);
//				mPostingdialog.show();
//			}
//			// Don't display again this dialog
//			try {
//				ECPreferences.savePreference(
//						ECPreferenceSettings.SETTINGS_FIRST_USE, Boolean.FALSE,
//						true);
//			} catch (Exception e) {
//				/** NON BLOCK **/
//			}
//		}
//	}

	/**
	 * 根据TabFragment Index 查找Fragment
	 *
	 * @param tabIndex
	 * @return
	 */
	public final BaseFragment getTabView(int tabIndex) {
		LogUtil.d(LogUtil.getLogUtilsTag(ChatFragment.class),
				"get tab index " + tabIndex);
		if (tabIndex < 0) {
			return null;
		}

		if (mTabViewCache.containsKey(Integer.valueOf(tabIndex))) {
			return (BaseFragment) mTabViewCache.get(Integer.valueOf(tabIndex));
		}

		BaseFragment mFragment = null;
		switch (tabIndex) {
		case TAB_CONVERSATION:
			if(getActivity()==null){
				break;
			}
			mFragment = (TabFragment) Fragment.instantiate(getActivity(),
					ConversationListFragment.class.getName(), null);
			break;
		case TAB_ADDRESS:
			mFragment = (TabFragment) Fragment
					.instantiate(getActivity(),
							MobileContactActivity.MobileContactFragment.class
									.getName(), null);
			break;
		case TAB_GROUP:
			
			
			mFragment = (TabFragment) Fragment.instantiate(getActivity(),
					GroupListFragment.class.getName(), null);
			break;
		case TAB_DISCUSSION_GROUP:
			
			mFragment = (TabFragment) Fragment.instantiate(getActivity(),
					DiscussionListFragment.class.getName(), null);
			break;

		default:
			break;
		}

		if (mFragment != null) {
			mFragment.setActionBarActivity(getActivity());
		}
		mTabViewCache.put(Integer.valueOf(tabIndex), mFragment);
		return mFragment;
	}

	/**
	 * 根据提供的子Fragment index 切换到对应的页面
	 *
	 * @param index
	 *            子Fragment对应的index
	 */
	public void ctrlViewTab(int index) {

		LogUtil.d(LogUtil.getLogUtilsTag(ChatFragment.class),
				"change tab to " + index + ", cur tab " + mCurrentItemPosition
						+ ", has init tab " + mTabViewInit
						+ ", tab cache size " + mTabViewCache.size());
		if ((!mTabViewInit || index < 0)
				|| (mLauncherViewPagerAdapter != null && index > mLauncherViewPagerAdapter
						.getCount() - 1)) {
			return;
		}

		if (mCurrentItemPosition == index) {
			return;
		}
		mCurrentItemPosition = index;

		if (mLauncherUITabView != null) {
			mLauncherUITabView.doChangeTabViewDisplay(mCurrentItemPosition);
		}

		if (mCustomViewPager != null) {
			mCustomViewPager.setCurrentItem(mCurrentItemPosition, false);
		}

	}
	/**
	 * 根据底层库是否支持voip加载相应的子菜单
	 */
	void initOverflowItems() {
		if (mItems == null) {
			if (SDKCoreHelper.getInstance().isSupportMedia()) {
				mItems = new OverflowItem[7];
				mItems[0] = new OverflowItem(
						getString(R.string.main_plus_inter_phone));
				mItems[1] = new OverflowItem(
						getString(R.string.main_plus_meeting_voice));
				mItems[2] = new OverflowItem(
						getString(R.string.main_plus_meeting_video));
				mItems[3] = new OverflowItem(
						getString(R.string.main_plus_groupchat));
				mItems[4] = new OverflowItem(
						getString(R.string.main_plus_querygroup));
				
				mItems[5] = new OverflowItem(
						getString(R.string.create_discussion));
				
				
				
				mItems[6] = new OverflowItem(
						getString(R.string.main_plus_settings));

			} else {
				mItems = new OverflowItem[4];
				mItems[0] = new OverflowItem(
						getString(R.string.main_plus_groupchat));
				mItems[1] = new OverflowItem(
						getString(R.string.main_plus_querygroup));
				
				
				mItems[2] = new OverflowItem(
						getString(R.string.create_discussion));
				
				
				mItems[3] = new OverflowItem(
						getString(R.string.main_plus_settings));
				

			}
		}

	}

//	@Override
//	public boolean onMenuOpened(int featureId, Menu menu) {
//		controlPlusSubMenu();
//		return false;
//	}

//	@Override
//	public boolean dispatchKeyEvent(KeyEvent event) {
//		if ((event.getKeyCode() == KeyEvent.KEYCODE_BACK)
//				&& event.getAction() == KeyEvent.ACTION_UP) {
//			// dismiss PlusSubMenuHelper
//			if (mOverflowHelper != null && mOverflowHelper.isOverflowShowing()) {
//				mOverflowHelper.dismiss();
//				return true;
//			}
//		}
//
//		// 这里可以进行设置全局性的menu菜单的判断
//		if ((event.getKeyCode() == KeyEvent.KEYCODE_BACK)
//				&& event.getAction() == KeyEvent.ACTION_DOWN) {
//			doTaskToBackEvent();
//		}
//
//		try {
//
//			return super.dispatchKeyEvent(event);
//		} catch (Exception e) {
//			LogUtil.e(LogUtil.getLogUtilsTag(LauncherActivity.class),
//					"dispatch key event catch exception " + e.getMessage());
//		}
//
//		return false;
//	}
//
//	@Override
//	protected void onResume() {
//		LogUtil.i(LogUtil.getLogUtilsTag(LauncherActivity.class),
//				"onResume start");
//		super.onResume();
//		CrashHandler.getInstance().setContext(this);
//		// 统计时长
//		MobclickAgent.onResume(this);
//
//		boolean fullExit = ECPreferences.getSharedPreferences().getBoolean(
//				ECPreferenceSettings.SETTINGS_FULLY_EXIT.getId(), false);
//		if (fullExit) {
//			try {
//				ECHandlerHelper.removeCallbacksRunnOnUI(initRunnable);
//				ECPreferences.savePreference(
//						ECPreferenceSettings.SETTINGS_FULLY_EXIT, false, true);
//				ContactsCache.getInstance().stop();
//				CCPAppManager.setClientUser(null);
//				ECDevice.unInitial();
//				finish();
//				android.os.Process.killProcess(android.os.Process.myPid());
//				return;
//			} catch (InvalidClassException e) {
//				e.printStackTrace();
//			}
//		}
//		if (mLauncherUITabView == null) {
//			String account = getAutoRegistAccount();
//			if (TextUtils.isEmpty(account)) {
//				startActivity(new Intent(this, LoginActivity.class));
//				finish();
//				return;
//			}
//			// 注册第一次登陆同步消息
//			registerReceiver(new String[] {
//					IMChattingHelper.INTENT_ACTION_SYNC_MESSAGE,
//					SDKCoreHelper.ACTION_SDK_CONNECT });
//			ClientUser user = new ClientUser("").from(account);
//			CCPAppManager.setClientUser(user);
//			if (!ContactSqlManager.hasContact(user.getUserId())) {
//				ECContacts contacts = new ECContacts();
//				contacts.setClientUser(user);
//				ContactSqlManager.insertContact(contacts);
//			}
//
//			if (SDKCoreHelper.getConnectState() != ECDevice.ECConnectState.CONNECT_SUCCESS
//					&& !SDKCoreHelper.isKickOff()) {
//
//				ContactsCache.getInstance().load();
//
//				if(!TextUtils.isEmpty(getAutoRegistAccount())){
//				SDKCoreHelper.init(this);
//				}
//			}
//			// 初始化主界面Tab资源
//			if (!mInit) {
//				initLauncherUIView();
//			}
//		}
//		OnUpdateMsgUnreadCounts();
//	}



	public void restartAPP() {
		
		ECDevice.unInitial();
		Intent intent = new Intent(getActivity(), MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	/**
	 * 检查是否需要自动登录
	 *
	 * @return
	 */
	private String getAutoRegistAccount() {
		SharedPreferences sharedPreferences = ECPreferences
				.getSharedPreferences();
		ECPreferenceSettings registAuto = ECPreferenceSettings.SETTINGS_REGIST_AUTO;
		String registAccount = sharedPreferences.getString(registAuto.getId(),
				(String) registAuto.getDefaultValue());
		return registAccount;
	}

	private void controlPlusSubMenu() {
		if (mOverflowHelper == null) {
			return;
		}

		if (mOverflowHelper.isOverflowShowing()) {
			mOverflowHelper.dismiss();
			return;
		}
		
		if(mItems == null) {
			initOverflowItems();
		}
		
		mOverflowHelper.setOverflowItems(mItems);
		mOverflowHelper
				.setOnOverflowItemClickListener(mOverflowItemCliclListener);
		mOverflowHelper.showAsDropDown(findViewById(R.id.btn_plus));
	}

	@Override
	public void OnUpdateMsgUnreadCounts() {
		int unreadCount = IMessageSqlManager.qureyAllSessionUnreadCount();
		int notifyUnreadCount = IMessageSqlManager.getUnNotifyUnreadCount();
		int count = unreadCount;
		if (unreadCount >= notifyUnreadCount) {
			count = unreadCount - notifyUnreadCount;
		}
		if (mLauncherUITabView != null) {
			mLauncherUITabView.updateMainTabUnread(count);
		}
	}

	/**
	 * TabView 页面适配器
	 *
	 * @author 容联•云通讯
	 * @version 4.0
	 * @date 2014-12-4
	 */
	private class LauncherViewPagerAdapter extends FragmentStatePagerAdapter
			implements ViewPager.OnPageChangeListener,
			CCPLauncherUITabView.OnUITabViewClickListener {
		/**
         *
         */
		private int mClickTabCounts;
		private GroupListFragment mGroupListFragment;
		private DiscussionListFragment mDissListFragment;

		private final ViewPager mViewPager;
		private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

		final class TabInfo {
			private final String tag;
			private final Class<?> clss;
			private final Bundle args;

			TabInfo(String _tag, Class<?> _class, Bundle _args) {
				tag = _tag;
				clss = _class;
				args = _args;
			}
		}

		public LauncherViewPagerAdapter(FragmentActivity fm, ViewPager pager) {
			super(fm.getSupportFragmentManager());
			mViewPager = pager;
			mViewPager.setAdapter(this);
			mViewPager.setOnPageChangeListener(this);
		}

		public void addTab(String tabSpec, Class<?> clss, Bundle args) {
			String tag = tabSpec;

			TabInfo info = new TabInfo(tag, clss, args);
			mTabs.add(info);
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return 4;
		}

		@Override
		public Fragment getItem(int position) {
			return getTabView(position);
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			LogUtil.d(LogUtil.getLogUtilsTag(LauncherViewPagerAdapter.class),
					"onPageScrollStateChanged state = " + state);

			if (state != ViewPager.SCROLL_STATE_IDLE
					|| mGroupListFragment == null) {
				return;
			}
			if (mGroupListFragment != null) {
				mGroupListFragment.onGroupFragmentVisible(true);
				mGroupListFragment = null;
			}
		}

		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
			LogUtil.d(LogUtil.getLogUtilsTag(LauncherViewPagerAdapter.class),
					"onPageScrolled " + position + " " + positionOffset + " "
							+ positionOffsetPixels);
			if (mLauncherUITabView != null) {
				mLauncherUITabView.doTranslateImageMatrix(position,
						positionOffset);
			}
			if (positionOffset != 0.0F&&position==CCPLauncherUITabView.TAB_VIEW_THIRD) {
				if (mGroupListFragment == null) {
					mGroupListFragment = (GroupListFragment) getTabView(CCPLauncherUITabView.TAB_VIEW_THIRD);
					mGroupListFragment.onGroupFragmentVisible(true);
				}
			}
			if (positionOffset != 0.0F&&position==CCPLauncherUITabView.TAB_VIEW_FOUR) {
				if (mDissListFragment == null) {
					mDissListFragment = (DiscussionListFragment) getTabView(CCPLauncherUITabView.TAB_VIEW_FOUR);
					mDissListFragment.onDisGroupFragmentVisible(true);
				}
			}
		
			
			
			
			
		}

		@Override
		public void onPageSelected(int position) {
			LogUtil.d(LogUtil.getLogUtilsTag(LauncherViewPagerAdapter.class),
					"onPageSelected");
			if (mLauncherUITabView != null) {
				mLauncherUITabView.doChangeTabViewDisplay(position);
				mCurrentItemPosition = position;
			}
		}

		@Override
		public void onTabClick(int tabIndex) {
			if (tabIndex == mCurrentItemPosition) {
				LogUtil.d(
						LogUtil.getLogUtilsTag(LauncherViewPagerAdapter.class),
						"on click same index " + tabIndex);
				// Perform a rolling
				TabFragment item = (TabFragment) getItem(tabIndex);
				item.onTabFragmentClick();
				return;
			}

			mClickTabCounts += mClickTabCounts;
			LogUtil.d(LogUtil.getLogUtilsTag(LauncherViewPagerAdapter.class),
					"onUITabView Click count " + mClickTabCounts);
			mViewPager.setCurrentItem(tabIndex);
		}

	}

	/**
	 * 网络注册状态改变
	 *
	 * @param connect
	 */
	@Subscriber(tag = "connect_state_refresh")
	public void onNetWorkNotify(ECDevice.ECConnectState connect) {
		BaseFragment tabView = getTabView(TAB_CONVERSATION);
		if (tabView instanceof ConversationListFragment && tabView.isAdded()) {
			((ConversationListFragment) tabView).updateConnectState();
		}
	}
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_plus) {
			controlPlusSubMenu();
		}
	}

	private final AdapterView.OnItemClickListener mOverflowItemCliclListener = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			controlPlusSubMenu();
			
			OverflowItem overflowItem= mItems[position];
			String title=overflowItem.getTitle();
			
			if (getString(R.string.main_plus_inter_phone).equals(title)) {
				// 实时对讲
				startActivity(new Intent(getActivity(),
						InterPhoneListActivity.class));
			} else if (getString(R.string.main_plus_meeting_voice).equals(title)) {
				// 语音会议
				startActivity(new Intent(getActivity(),
						MeetingListActivity.class));
			} else if (getString(R.string.main_plus_groupchat).equals(title)) {
				// 创建群组
				startActivity(new Intent(getActivity(),
						CreateGroupActivity.class));
			} else if (getString(R.string.main_plus_querygroup).equals(title)) {
				// 群组搜索
				startActivity(new Intent(getActivity(),BaseSearch.class));
			} else if (getString(R.string.main_plus_mcmessage).equals(title)) {
				handleStartServiceEvent();
				
			} else if (getString(R.string.main_plus_settings).equals(title)) {
				// 设置;
				startActivity(new Intent(getActivity(),SettingsActivity.class));
				
				
			} else if (getString(R.string.main_plus_meeting_video).equals(title)) {
				startActivity(new Intent(getActivity(),
						VideoconferenceConversation.class));

			}else if(getString(R.string.create_discussion).equals(title)){
				
				Intent intent=new Intent(getActivity(), MobileContactSelectActivity.class);
				intent.putExtra("is_discussion", true);
				intent.putExtra("isFromCreateDiscussion", true);
				intent.putExtra("group_select_need_result", true);
				
				startActivity(intent);
				
				
			}else if(getString(R.string.query_discussion).equals(title)){
				
				Intent intent=new Intent(getActivity(), ECDiscussionActivity.class);
				intent.putExtra("is_discussion", true);
				
				startActivity(intent);
				
			}
		}

	};

	@Override
	public void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}

	/**
	 * 在线客服
	 */
	private void handleStartServiceEvent() {
		showProcessDialog();
		CustomerServiceHelper.startService("KF4008818600668603",
				new CustomerServiceHelper.OnStartCustomerServiceListener() {
					@Override
					public void onError(ECError error) {
						dismissPostingDialog();
					}

					@Override
					public void onServiceStart(String event) {
						dismissPostingDialog();
						CCPAppManager.startCustomerServiceAction(
								getActivity(), event);
					}
				});
	}

	private InternalReceiver internalReceiver;

	private class InternalReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

		}
	}

	private boolean mInitActionFlag;

	/**
	 * 处理一些初始化操作
	 */
	private void doInitAction() {
		if (SDKCoreHelper.getConnectState() == ECDevice.ECConnectState.CONNECT_SUCCESS
				&& !mInitActionFlag) {

			// 检测当前的版本
			SDKCoreHelper.SoftUpdate mSoftUpdate = SDKCoreHelper.mSoftUpdate;
			if (mSoftUpdate != null) {
				if (DemoUtils.checkUpdater(mSoftUpdate.version)) {
					boolean force = mSoftUpdate.force;
					showUpdaterTips(mSoftUpdate.desc , force);
					if (force) {
						return;
					}
				}
			}

			IMChattingHelper.getInstance().getPersonInfo();
			settingPersionInfo();
			// 检测离线消息
			checkOffineMessage();
			mInitActionFlag = true;
		}
	}

	private void disPostingLoading() {
		if (mPostingdialog != null && mPostingdialog.isShowing()) {
			mPostingdialog.dismiss();
		}
	}

	ECAlertDialog showUpdaterTipsDialog = null;

	private void showUpdaterTips(String updateDesc ,final boolean force) {
		if (showUpdaterTipsDialog != null) {
			return;
		}
		String negativeText = getString(force ? R.string.settings_logout : R.string.update_next);
		String msg = getString(R.string.new_update_version);
		if(!TextUtils.isEmpty(updateDesc)) {
			msg = updateDesc;
		}
		showUpdaterTipsDialog = ECAlertDialog.buildAlert(getActivity(), msg,
				negativeText, getString(R.string.app_update),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						showUpdaterTipsDialog = null;
						if (force) {
							try {
								ECPreferences
										.savePreference(
												ECPreferenceSettings.SETTINGS_FULLY_EXIT,
												true, true);
							} catch (InvalidClassException e) {
								e.printStackTrace();
							}
							restartAPP();
						}
					}
				}, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						CCPAppManager.startUpdater(getActivity());
						// restartAPP();
						showUpdaterTipsDialog = null;
					}
				});

		showUpdaterTipsDialog.setTitle(R.string.app_tip);
		showUpdaterTipsDialog.setDismissFalse();
		showUpdaterTipsDialog.setCanceledOnTouchOutside(false);
		showUpdaterTipsDialog.setCancelable(false);
		showUpdaterTipsDialog.show();
	}

	private ECProgressDialog mPostingdialog;

	void showProcessDialog() {
		mPostingdialog = new ECProgressDialog(getActivity(),
				R.string.login_posting_submit);
		mPostingdialog.show();
	}

	/**
	 * 关闭对话框
	 */
	private void dismissPostingDialog() {
		if (mPostingdialog == null || !mPostingdialog.isShowing()) {
			return;
		}
		mPostingdialog.dismiss();
		mPostingdialog = null;
	}
}
