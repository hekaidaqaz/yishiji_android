package com.yishiji.angelplatform.ui.account;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.yishiji.angelplatform.R;
import com.yuntongxun.ecdemo.ui.BaseFragment;

import org.simple.eventbus.EventBus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by heshun on 2016/1/4.
 * 输入验证码
 */
public class InputAuthCodeFragment extends BaseFragment {
    @Bind(R.id.sumbit_auth_code_btn)
    Button sumbitAuthCodeBtn;

    @Bind(R.id.again_get_auth_btn)
    Button againGetAuthBtn;
    private int time;
    private boolean timeOut;
    private boolean flag;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * 每个页面需要实现该方法返回一个该页面所对应的资源ID
     *
     * @return 页面资源ID
     */
    @Override
    protected int getLayoutId() {
        return R.layout.ysj_fragment_input_register_authcode;
    }

    @Override
    public int getTitleLayoutId() {
        return -1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState); time=60;
        flag=true;
        againGetAuthBtn.setClickable(false);
        timeOut=false;

        time=60;
        requestAuth();
    }

    private Handler handler=new Handler(){

        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0:
                    if(time==0){
                        againGetAuthBtn.setClickable(true);
                        againGetAuthBtn.setText("获取验证码");
                        timeOut=true;
                    }else{
                        againGetAuthBtn.setText(time+"秒后重试");
                    }
                    break;
            }
        };
    };
    /**
     * **********************************************************************
     * @Title: requestAuth
     * @Description: 请求验证码及业务逻辑处理
     * @author HeShun (何顺)
     * @date 2014-8-29 下午3:45:24
     ************************************************************************
     */
    public void requestAuth(){
        new Thread(){

            @Override
            public void run() {
                while (flag) {
                    try {
                        if(time>=0&&time<=60){
                            handler.sendEmptyMessage(0);
                            sleep(1000);
                            time-=1;
                        }else{
                            flag=false;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
//    /**
//     * ******************************************************
//     * @Title: getVerifyNum
//     * @Description: TODO(获取验证码)
//     * @author zhaofeng@ytmusic.cn
//     * @date 2015-5-13 下午3:36:51 void  返回类型
//     ********************************************************
//     */
//    private void getVerifyNum(){
//        String phone = phoneNum.getText().toString().trim();
//        if (null == phone || !validatePhoneNumber(phone)) {
//            phoneNum.setError(android.text.Html.fromHtml("<font color=#FFFFFF>号码格式错误</font>"));
//            flag = true;
//            return;
//        }else{
//            flag = false;
//        }
//        if(!flag){
//            getVerificationRequest(phone);
//
//            requestAuth();
//        }
//    }
    /**
     * 提交验证码
     *
     * @param view
     */
    @OnClick(R.id.sumbit_auth_code_btn)
    public void submitCode(View view) {
        EventBus.getDefault().post(2, RegisterActivity.REGISTER_TAB_CHANGE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
