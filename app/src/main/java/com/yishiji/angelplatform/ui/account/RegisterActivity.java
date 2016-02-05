package com.yishiji.angelplatform.ui.account;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;

import com.yishiji.angelplatform.R;
import com.yishiji.angelplatform.adapter.MyPagerAdapter;
import com.yishiji.angelplatform.widget.MyViewPager;
import com.yuntongxun.ecdemo.ui.BaseFragment;
import com.yuntongxun.ecdemo.ui.ECSuperActivity;

import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;

/**
 * Created by heshun on 2015/12/30.
 */
public class RegisterActivity extends ECSuperActivity implements View.OnClickListener {
    public static final String REGISTER_TAB_CHANGE = "register_tab_change";
    @Bind(R.id.content_viewpager)
    MyViewPager contentViewpager;
    @Bind(R.id.prompt_mobile_txt)
    TextView promptMobileTxt;
    @Bind(R.id.prompt_auth_code_txt)
    TextView promptAuthCodeTxt;
    @Bind(R.id.prompt_pwd_txt)
    TextView promptPwdTxt;
    @BindColor(R.color.gray) int gray;
    @BindColor(R.color.red_btn_color_normal) int red;
    private SparseArray<BaseFragment> fragmentArray = new SparseArray<BaseFragment>();
    private FragmentManager fragmentManager;

    /**
     * The sub Activity implement, set the Ui Layout
     *
     * @return
     */
    @Override
    protected int getLayoutId() {
        return R.layout.ysj_activity_register;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        initView();
        getTopBarView().setTopBarToStatus(1, R.drawable.topbar_back_bt,
                R.drawable.transparent, null,
                "",
                getString(R.string.register), null, this);

    }

    /**
     * 注册步骤监听
     */
    @Subscriber(tag = REGISTER_TAB_CHANGE)
    public void registerTabChange(int index) {
        switch (index) {
            case 0:
                promptMobileTxt.setTextColor(red);
                promptAuthCodeTxt.setTextColor(gray);
                promptPwdTxt.setTextColor(gray);
                break;
            case 1:
                promptMobileTxt.setTextColor(gray);
                promptAuthCodeTxt.setTextColor(red);
                promptPwdTxt.setTextColor(gray);
                break;
            case 2:
                promptMobileTxt.setTextColor(gray);
                promptAuthCodeTxt.setTextColor(gray);
                promptPwdTxt.setTextColor(red);
                break;
        }
        contentViewpager.setCurrentItem(index);
    }

    private void initView() {
        fragmentArray.put(R.id.fragment_chat, new InputMobileFragment());
        fragmentArray.put(R.id.fragment_entity_firm, new InputAuthCodeFragment());
        fragmentArray.put(R.id.fragment_user_center, new SetingPwdFragment());
        fragmentManager = getSupportFragmentManager();
        contentViewpager.setOffscreenPageLimit(3);// 设置缓存数量
        contentViewpager.setAdapter(new MyPagerAdapter(fragmentArray, fragmentManager));
        EventBus.getDefault().post(0, RegisterActivity.REGISTER_TAB_CHANGE);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_left:
                hideSoftKeyboard();
                finish();
                break;

            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
