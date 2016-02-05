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



import com.yishiji.angelplatform.R;
import com.yuntongxun.ecdemo.common.utils.CommomUtil;
import com.yuntongxun.ecdemo.ui.videomeeting.CCPMulitVideoUI.OnVideoUIItemClickListener;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class SubVideoSurfaceView extends FrameLayout {

	private SurfaceView mSurfaceView;
	private TextView mSubText;
	private TextView mSubStatus;
	private RelativeLayout mContainer;
	
	private Drawable mOpreableDraw;
	private int mIndex;
	
	private MulitVideoMember member;
	
	private OnVideoUIItemClickListener mVideoUIItemClickListener;
	
	/**
	 * @param context
	 */
	public SubVideoSurfaceView(Context context) {
		this(context , null);
	}
	
	/**
	 * @param context
	 * @param attrs
	 */
	public SubVideoSurfaceView(Context context, AttributeSet attrs) {
		this(context, attrs , 0);
	}
	
	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public SubVideoSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		initView();
	}

	/**
	 * 
	 */
	private void initView() {
		inflate(getContext(), R.layout.mulit_video_surfaceview, this);
		mSurfaceView = (SurfaceView) findViewById(R.id.surface_view);
		mContainer = (RelativeLayout) findViewById(R.id.container);
		mSubStatus = (TextView) findViewById(R.id.status);
		mSubText = (TextView) findViewById(R.id.text);
		
		mSurfaceView.setVisibility(View.GONE);
		mSubStatus.setText(R.string.mulit_video_unjoin);
		mSubStatus.setVisibility(View.VISIBLE);
		
		initThreePoint();
	}
	
	public void setIndex(int index) {
		mIndex = index;
	}
	
	/**
	 * 
	 */
	private void initThreePoint() {
		mOpreableDraw = getResources().getDrawable(R.drawable.three_point);
		mOpreableDraw.setBounds(0, 0, mOpreableDraw.getMinimumWidth(), mOpreableDraw.getMinimumHeight());
	}
	
	public SurfaceView getVideoSurfaceView() {
		return mSurfaceView;
	}
	
	public TextView getDisplayTextView() {
		return mSubText;
	}
	
	/**
	 * 
	 * @param member
	 */
	public void setVideoUIMember(MulitVideoMember member) {
		if(member != null) {
			mSurfaceView.setVisibility(View.VISIBLE);
			mSubStatus.setVisibility(View.GONE);
		} else {
			mSurfaceView.setVisibility(View.GONE);
			mSubStatus.setVisibility(View.VISIBLE);
		}
		
		setVideoUIText(member == null ? null : member.getNumber(), true);
	}
	
	/**
	 * 
	 * @param text
	 */
	public void setVideoUIText(CharSequence text) {
		
		if(!TextUtils.isEmpty(text)) {
			mSurfaceView.setVisibility(View.VISIBLE);
			mSubStatus.setVisibility(View.GONE);
		} else {
			mSurfaceView.setVisibility(View.GONE);
			mSubStatus.setVisibility(View.VISIBLE);
		}
		
		setVideoUIText(text, true);
		
	}
	
	/**
	 * 
	 * @param text
	 * @param Operable
	 */
	private void setVideoUIText(CharSequence text , boolean Operable) {
		
		mSubText.setText(text);
		
		if(text == null) {
			mSubText.setCompoundDrawables(null, null, null, null);
			setOnClickListener(null);
			return ;
		}
		mSubText.setCompoundDrawables(null, null, mOpreableDraw, null);
		// Set the members item the click listener callback
		setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mVideoUIItemClickListener != null) {
					mVideoUIItemClickListener.onVideoUIItemClick(mIndex);
				}
			}
		});
		
	}
	
	public void setOnVideoUIItemClickListener(OnVideoUIItemClickListener l) {
		mVideoUIItemClickListener = l;
	}

}
