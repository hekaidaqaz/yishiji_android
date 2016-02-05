package com.yishiji.angelplatform.ui.account;

import android.os.Bundle;
import android.view.View;

import com.yishiji.angelplatform.R;
import com.yuntongxun.ecdemo.ui.BaseFragment;

/**
 * Created by heshun on 2016/1/4.
 */
public class SetingPwdFragment extends BaseFragment {
    /**
     * 每个页面需要实现该方法返回一个该页面所对应的资源ID
     *
     * @return 页面资源ID
     */
    @Override
    protected int getLayoutId() {
        return R.layout.ysj_fragment_setingpwd;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void refresh() {
        super.refresh();
    }
    @Override
    public int getTitleLayoutId() {
        return -1;
    }
}
