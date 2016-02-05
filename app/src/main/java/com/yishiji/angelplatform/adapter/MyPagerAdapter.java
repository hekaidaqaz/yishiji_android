package com.yishiji.angelplatform.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.yuntongxun.ecdemo.ui.BaseFragment;

/**
 * 
 * @ClassName: MyPagerAdapter
 * @Description: TODO(首页tab切换adapter)
 * @author heshun@wishcloud.com.cn
 * @date 2015-9-25 上午9:46:57
 */
public class MyPagerAdapter extends PagerAdapter {
	private SparseArray<BaseFragment> fragmentList;
	private FragmentManager fragmentManager;
	private int datasize;
	public MyPagerAdapter(SparseArray<BaseFragment> fragmentList, FragmentManager fragmentManager) {
		super();
		this.fragmentList = fragmentList;
		this.fragmentManager = fragmentManager;
		datasize = fragmentList.size();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	@Override
	public int getCount() {
		return fragmentList == null ? 0 : fragmentList.size();
	}

	@Override
	public void destroyItem(View container, int position, Object object) {
		((ViewPager) container).removeView(fragmentList.get(position).getView());
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		Fragment fragment = fragmentList.valueAt(position);
		if (!fragment.isAdded()) { // 如果fragment还没有added
			FragmentTransaction ft = fragmentManager.beginTransaction();
			ft.add(fragment, fragment.getClass().getSimpleName());
			ft.commit();
			fragmentManager.executePendingTransactions();
		}

		if (fragment.getView().getParent() == null) {
			container.addView(fragment.getView()); // 为viewpager增加布局
		}
		return fragment.getView();
	}
};
