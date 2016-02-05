package com.yishiji.angelplatform.ui.mainfragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yishiji.angelplatform.R;
import com.yishiji.angelplatform.ui.account.LoginActivity;
import com.yuntongxun.ecdemo.ui.BaseFragment;

/**
 * Created by heshun on 2015/12/25.
 * 用户中心
 */
public class UserCenterFragment extends BaseFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_blank;
    }

    @Override
    protected void onFragmentInit() {
        super.onFragmentInit();
        setActionBarTitle(R.string.fragment_user_center);

    }

    @Override
    public void refresh() {
        super.refresh();
        Intent intent=new Intent(getActivity(),LoginActivity.class);
        startActivity(intent);
    }
}
