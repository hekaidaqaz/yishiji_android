package com.yishiji.angelplatform.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Process;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.umeng.analytics.MobclickAgent;
import com.yishiji.angelplatform.R;
import com.yishiji.angelplatform.adapter.MyPagerAdapter;
import com.yishiji.angelplatform.ui.mainfragment.AngelplatformFragment;
import com.yishiji.angelplatform.ui.mainfragment.ChatFragment;
import com.yishiji.angelplatform.ui.mainfragment.EntityFirmFragment;
import com.yishiji.angelplatform.ui.mainfragment.UserCenterFragment;
import com.yishiji.angelplatform.widget.MyViewPager;

import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.ECContentObservers;
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
import com.yuntongxun.ecdemo.ui.BaseFragment;
import com.yuntongxun.ecdemo.ui.ConversationListFragment;
import com.yuntongxun.ecdemo.ui.ECFragmentActivity;
import com.yuntongxun.ecdemo.ui.SDKCoreHelper;
import com.yuntongxun.ecdemo.ui.account.LoginActivity;
import com.yuntongxun.ecdemo.ui.chatting.ChattingActivity;
import com.yuntongxun.ecdemo.ui.chatting.IMChattingHelper;
import com.yuntongxun.ecdemo.ui.contact.ECContacts;
import com.yuntongxun.ecdemo.ui.group.GroupNoticeActivity;
import com.yuntongxun.ecdemo.ui.settings.SettingPersionInfoActivity;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.im.ECGroup;
import com.yuntongxun.ecsdk.platformtools.ECHandlerHelper;

import org.simple.eventbus.EventBus;

import java.io.InvalidClassException;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by heshun on 2015/12/24.
 */
public class MainActivity extends ECFragmentActivity implements ConversationListFragment.OnUpdateMsgUnreadCountsListener {
    @Bind(R.id.content_viewpager)
    MyViewPager contentViewpager;
    @Bind(R.id.fragment_angelplatform)
    RadioButton fragmentAngelplatform;
    @Bind(R.id.fragment_chat)
    RadioButton fragmentChat;
    @Bind(R.id.fragment_entity_firm)
    RadioButton fragmentEntityFirm;
    @Bind(R.id.fragment_user_center)
    RadioButton fragmentUserCenter;
    @Bind(R.id.tab)
    RadioGroup tab;
    private SparseArray<BaseFragment> fragmentArray = new SparseArray<BaseFragment>();
    private static final String TAG = "MainActivity";
    private FragmentManager fragmentManager;
    /**
     * 当前ECLauncherUI 实例
     */
    public static MainActivity mLauncherUI;
    /**
     * 当前ECLauncherUI实例产生个数
     */
    public static int mLauncherInstanceCount = 0;
    /**
     * 会话界面(沟通)
     */
    private static final int TAB_CONVERSATION = 0;
    BaseFragment currentFragment = null;
    /**
     * 当前主界面RootView
     */
    public View mLauncherView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        // 设置页面默认为竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        initWelcome();
        fragmentArray.put(R.id.fragment_angelplatform, new AngelplatformFragment());
        fragmentArray.put(R.id.fragment_chat, new ChatFragment());
        fragmentArray.put(R.id.fragment_entity_firm, new EntityFirmFragment());
        fragmentArray.put(R.id.fragment_user_center, new UserCenterFragment());
        mLauncherUI = this;
        mLauncherInstanceCount++;
        fragmentManager = getSupportFragmentManager();
        contentViewpager.setOffscreenPageLimit(4);// 设置缓存数量
        contentViewpager.setAdapter(new MyPagerAdapter(fragmentArray, fragmentManager));
        tab.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                FragmentTransaction transaction = fragmentManager.beginTransaction();
                currentFragment = fragmentArray.get(checkedId);
                if (currentFragment != null) {
                    if (currentFragment instanceof AngelplatformFragment) {
                        contentViewpager.setCurrentItem(0);
                    } else if (currentFragment instanceof ChatFragment) {
                        contentViewpager.setCurrentItem(1);
                    } else if (currentFragment instanceof EntityFirmFragment) {
                        contentViewpager.setCurrentItem(2);
                    } else if (currentFragment instanceof UserCenterFragment) {
                        contentViewpager.setCurrentItem(3);
                    }
                    if (currentFragment.isAdded()) {// 判断是否添加
                                currentFragment.refresh();
                    }
                }
            }

        });
        tab.check(R.id.fragment_chat);

        // umeng
        MobclickAgent.updateOnlineConfig(this);
        MobclickAgent.setDebugMode(true);
        ECContentObservers.getInstance().initContentObserver();
    }
    @Override
    public void OnUpdateMsgUnreadCounts() {
        int unreadCount = IMessageSqlManager.qureyAllSessionUnreadCount();
        int notifyUnreadCount = IMessageSqlManager.getUnNotifyUnreadCount();
        int count = unreadCount;
        if (unreadCount >= notifyUnreadCount) {
            count = unreadCount - notifyUnreadCount;
        }
        if (((ChatFragment) fragmentArray.get(R.id.fragment_chat)).mLauncherUITabView != null) {
            ((ChatFragment) fragmentArray.get(R.id.fragment_chat)).mLauncherUITabView.updateMainTabUnread(count);
        }
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

    private boolean mInit = false;
    private Runnable initRunnable = new Runnable() {
        @Override
        public void run() {
            mInit = false;
            initLauncherUIView();
        }
    };

    private void initWelcome() {
        if (!mInit) {
            mInit = true;
            setContentView(R.layout.splash_activity);

            // 程序启动开始创建一个splash用来初始化程序基本数据
            ECHandlerHelper.postDelayedRunnOnUI(initRunnable, 3000);
        }
    }

    /**
     * 初始化主界面UI视图
     */
    private void initLauncherUIView() {
//        mLauncherView = getLayoutInflater().inflate(R.layout.activity_main, null);
//        setContentView(mLauncherView);

        Intent intent = getIntent();
        if (intent != null && intent.getIntExtra("launcher_from", -1) == 1) {
            // 检测从登陆界面过来，判断是不是第一次安装使用
            checkFirstUse();
        }

        // 如果是登陆过来的
        doInitAction();
    }

    private boolean isFirstUse() {
        boolean firstUse = ECPreferences.getSharedPreferences().getBoolean(
                ECPreferenceSettings.SETTINGS_FIRST_USE.getId(),
                ((Boolean) ECPreferenceSettings.SETTINGS_FIRST_USE
                        .getDefaultValue()).booleanValue());
        return firstUse;
    }

    private void checkFirstUse() {
        boolean firstUse = isFirstUse();

        // Display the welcome message?
        if (firstUse) {
            if (IMChattingHelper.isSyncOffline()) {
                mPostingdialog = new ECProgressDialog(this,
                        R.string.tab_loading);
                mPostingdialog.setCanceledOnTouchOutside(false);
                mPostingdialog.setCancelable(false);
                mPostingdialog.show();
            }
            // Don't display again this dialog
            try {
                ECPreferences.savePreference(
                        ECPreferenceSettings.SETTINGS_FIRST_USE, Boolean.FALSE,
                        true);
            } catch (Exception e) {
                /** NON BLOCK **/
            }
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
                    showUpdaterTips(mSoftUpdate.desc, force);
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

    private void settingPersionInfo() {
        if (IMChattingHelper.getInstance().mServicePersonVersion == 0
                && CCPAppManager.getClientUser().getpVersion() == 0) {
            Intent settingAction = new Intent(this,
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

    private void disPostingLoading() {
        if (mPostingdialog != null && mPostingdialog.isShowing()) {
            mPostingdialog.dismiss();
        }
    }

    ECAlertDialog showUpdaterTipsDialog = null;

    private void showUpdaterTips(String updateDesc, final boolean force) {
        if (showUpdaterTipsDialog != null) {
            return;
        }
        String negativeText = getString(force ? R.string.settings_logout : R.string.update_next);
        String msg = getString(R.string.new_update_version);
        if (!TextUtils.isEmpty(updateDesc)) {
            msg = updateDesc;
        }
        showUpdaterTipsDialog = ECAlertDialog.buildAlert(this, msg,
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
                        CCPAppManager.startUpdater(MainActivity.this);
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
        mPostingdialog = new ECProgressDialog(MainActivity.this,
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

    public void restartAPP() {

        ECDevice.unInitial();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        Process.killProcess(Process.myPid());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Intent actionIntent = intent;
        String userName = actionIntent.getStringExtra("Main_FromUserName");
        String mSession = actionIntent.getStringExtra("Main_Session");
        ECContacts contacts = ContactSqlManager
                .getContactLikeUsername(userName);
        if (contacts != null) {
            LogUtil.d(LogUtil.getLogUtilsTag(getClass()),
                    "[onNewIntent] userName = " + userName + " , contact_id "
                            + contacts.getContactid());

            if (GroupNoticeSqlManager.CONTACT_ID
                    .equals(contacts.getContactid())) {
                Intent noticeintent = new Intent(this,
                        GroupNoticeActivity.class);
                startActivity(noticeintent);
                return;
            }

            Intent chatIntent = new Intent(this, ChattingActivity.class);
            String recipinets;
            String username;
            if (!TextUtils.isEmpty(mSession) && mSession.startsWith("g")) {
                ECGroup ecGroup = GroupSqlManager.getECGroup(mSession);
                if (ecGroup == null) {
                    return;
                }
                recipinets = mSession;
                username = ecGroup.getName();
            } else {
                recipinets = contacts.getContactid();
                username = contacts.getNickname();
            }
            startActivity(chatIntent);

            CCPAppManager.startChattingAction(this, recipinets, username);
            return;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (internalReceiver != null) {
            unregisterReceiver(internalReceiver);
        }
    }

    @Override
    protected void onResume() {
        LogUtil.i(LogUtil.getLogUtilsTag(MainActivity.class),
                "onResume start");
        super.onResume();
        CrashHandler.getInstance().setContext(this);
        // 统计时长
        MobclickAgent.onResume(this);

        boolean fullExit = ECPreferences.getSharedPreferences().getBoolean(
                ECPreferenceSettings.SETTINGS_FULLY_EXIT.getId(), false);
        if (fullExit) {
            try {
                ECHandlerHelper.removeCallbacksRunnOnUI(initRunnable);
                ECPreferences.savePreference(
                        ECPreferenceSettings.SETTINGS_FULLY_EXIT, false, true);
                ContactsCache.getInstance().stop();
                CCPAppManager.setClientUser(null);
                ECDevice.unInitial();
                finish();
                Process.killProcess(Process.myPid());
                return;
            } catch (InvalidClassException e) {
                e.printStackTrace();
            }
        }
        if (((ChatFragment) fragmentArray.get(R.id.fragment_chat)).mLauncherUITabView == null) {
            String account = getAutoRegistAccount();
            if (TextUtils.isEmpty(account)) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return;
            }
            // 注册第一次登陆同步消息
            registerReceiver(new String[]{
                    IMChattingHelper.INTENT_ACTION_SYNC_MESSAGE,
                    SDKCoreHelper.ACTION_SDK_CONNECT});
            ClientUser user = new ClientUser("").from(account);
            CCPAppManager.setClientUser(user);
            if (!ContactSqlManager.hasContact(user.getUserId())) {
                ECContacts contacts = new ECContacts();
                contacts.setClientUser(user);
                ContactSqlManager.insertContact(contacts);
            }

            if (SDKCoreHelper.getConnectState() != ECDevice.ECConnectState.CONNECT_SUCCESS
                    && !SDKCoreHelper.isKickOff()) {

                ContactsCache.getInstance().load();

                if (!TextUtils.isEmpty(getAutoRegistAccount())) {
                    SDKCoreHelper.init(this);
                }
            }
            // 初始化主界面Tab资源
            if (!mInit) {
                initLauncherUIView();
            }
        }
        OnUpdateMsgUnreadCounts();
    }
    /**
     * 网络注册状态改变
     *
     * @param connect
     */
    public void onNetWorkNotify(ECDevice.ECConnectState connect) {
        EventBus.getDefault().post(connect,"connect_state_refresh");
    }
    private InternalReceiver internalReceiver;

    /**
     * 注册广播
     *
     * @param actionArray
     */
    protected final void registerReceiver(String[] actionArray) {
        if (actionArray == null) {
            return;
        }
        IntentFilter intentfilter = new IntentFilter();
        for (String action : actionArray) {
            intentfilter.addAction(action);
        }
        if (internalReceiver == null) {
            internalReceiver = new InternalReceiver();
        }
        registerReceiver(internalReceiver, intentfilter);
    }

    @Override
    public void onStart() {
        super.onStart();
    }
    private class InternalReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
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
                BaseFragment tabView = ((ChatFragment) fragmentArray.get(R.id.fragment_chat)).getTabView(TAB_CONVERSATION);
                if (tabView != null
                        && tabView instanceof ConversationListFragment) {
                    ((ConversationListFragment) tabView).updateConnectState();
                }
            } else if (SDKCoreHelper.ACTION_KICK_OFF.equals(intent.getAction())) {
                String kickoffText = intent.getStringExtra("kickoffText");
                handlerKickOff(kickoffText);
            }
        }
    }

    public void handlerKickOff(String kickoffText) {
        if (isFinishing()) {
            return;
        }
        ECAlertDialog buildAlert = ECAlertDialog.buildAlert(this, kickoffText,
                getString(R.string.dialog_btn_confim),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ECNotificationManager.getInstance()
                                .forceCancelNotification();
                        restartAPP();
                    }
                });
        buildAlert.setTitle("异地登陆");
        buildAlert.setCanceledOnTouchOutside(false);
        buildAlert.setCancelable(false);
        buildAlert.show();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if ((event.getKeyCode() == KeyEvent.KEYCODE_BACK)
                && event.getAction() == KeyEvent.ACTION_UP) {
            // dismiss PlusSubMenuHelper
//            if (mOverflowHelper != null && mOverflowHelper.isOverflowShowing()) {
//                mOverflowHelper.dismiss();
//                return true;
//            }
        }

        // 这里可以进行设置全局性的menu菜单的判断
        if ((event.getKeyCode() == KeyEvent.KEYCODE_BACK)
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            doTaskToBackEvent();
        }

        try {

            return super.dispatchKeyEvent(event);
        } catch (Exception e) {
            LogUtil.e(LogUtil.getLogUtilsTag(MainActivity.class),
                    "dispatch key event catch exception " + e.getMessage());
        }

        return false;
    }
    /**
     * 返回隐藏到后台
     */
    public void doTaskToBackEvent() {
        moveTaskToBack(true);

    }
}
