package com.yishiji.angelplatform.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.yishiji.angelplatform.R;
import com.yuntongxun.ecdemo.ui.ECSuperActivity;

/**
 * Created by heshun on 2015/12/31.
 */
public class LoginActivity extends ECSuperActivity implements View.OnClickListener{
    /**
     * The sub Activity implement, set the Ui Layout
     *
     * @return
     */
    @Override
    protected int getLayoutId() {
        return R.layout.ysj_activity_login;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        getTopBarView().setTopBarToStatus(1, R.drawable.topbar_back_bt,
                R.drawable.transparent, null,
                getString(R.string.register),
                getString(R.string.login), null, this);

    }
    private void initView(){

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
            case R.id.text_right:
                startActivity(new Intent(this , RegisterActivity.class));
                break;
            default:
                break;
        }
    }
}
