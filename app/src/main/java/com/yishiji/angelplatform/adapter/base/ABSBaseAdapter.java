package com.yishiji.angelplatform.adapter.base;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.widget.BaseAdapter;
import android.widget.ListView;
/**
 * ]************************************************************************
 * @ClassName: ABSBaseAdapter
 * @Description: TODO(添加继承类 利于公用属性)
 * @date 2014-8-28 上午11:25:18
 ************************************************************************
 */
public abstract class ABSBaseAdapter<T> extends BaseAdapter {
	protected List<T> alObjects;
	protected Context context;
	protected ListView mListView;
/**
 * **********************************************************************
 * @Title: setList
 * @Description: TODO(添加更多数据)
 * @date 2014-8-28 上午11:28:17
 ************************************************************************
 */
//	public abstract void setList(List<Object> mlist);

	public ABSBaseAdapter(Context context,List<T> alObjects) {
		// TODO Auto-generated constructor stub
		this.context=context;
		this.alObjects=alObjects;
	}
	public ABSBaseAdapter() {
		// TODO Auto-generated constructor stub
	}
	public ABSBaseAdapter(Context context){
		this.context=context;
	}
	@Override
	public int getCount() {
		return alObjects==null ? 0 : alObjects.size();
	}

	@Override
	public Object getItem(int arg0) {
		return alObjects==null ? 0 : alObjects.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}
	public void setList(List<T> list){
		this.alObjects = list;
		notifyDataSetChanged();
	}
	
	public List<T> getList(){
		return alObjects;
	}
	
	public void setList(T[] list){
		ArrayList<T> arrayList = new ArrayList<T>(list.length);  
		for (T t : list) {  
			arrayList.add(t);  
		}  
		setList(arrayList);
	}
	
	public ListView getListView(){
		return mListView;
	}
	
	public void setListView(ListView listView){
		mListView = listView;
	}
}
