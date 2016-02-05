package com.yishiji.angelplatform.ui.mainfragment;

import android.os.Bundle;
import android.view.View;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.yishiji.angelplatform.R;
import com.yuntongxun.ecdemo.ui.BaseFragment;

import java.io.IOException;

/**
 * Created by heshun on 2015/12/25.
 * 实体厂商
 */
public class EntityFirmFragment extends BaseFragment{

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_blank;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setActionBarTitle(R.string.fragment_entity_firm);
        //创建okHttpClient对象
        OkHttpClient mOkHttpClient = new OkHttpClient();
//创建一个Request
        final Request request = new Request.Builder()
                .url("https://github.com/hongyangAndroid")
                .build();
//new call
        Call call = mOkHttpClient.newCall(request);
//请求加入调度
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
            }

            @Override
            public void onResponse(final Response response) throws IOException {
                //String htmlStr =  response.body().string();
            }
        });

    }
}
