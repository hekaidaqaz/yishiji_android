package com.yishiji.angelplatform.ui.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.yishiji.angelplatform.R;
import com.yuntongxun.ecdemo.ui.BaseFragment;

import org.simple.eventbus.EventBus;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by heshun on 2016/1/4.
 */
public class InputMobileFragment extends BaseFragment {

    @Bind(R.id.login_password)
    EditText loginPassword;
    @Bind(R.id.radioButton1)
    CheckBox radioButton1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ButterKnife.bind(this, super.onCreateView(inflater, container, savedInstanceState));
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /**
     * 每个页面需要实现该方法返回一个该页面所对应的资源ID
     *
     * @return 页面资源ID
     */

    @Override
    protected int getLayoutId() {
        return R.layout.ysj_fragment_register_input_mobile;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getTopBarView().setVisibility(View.GONE);
    }


    @OnClick(R.id.get_code_btn)
    public void getCode(View view) {
        EventBus.getDefault().post(1, RegisterActivity.REGISTER_TAB_CHANGE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
