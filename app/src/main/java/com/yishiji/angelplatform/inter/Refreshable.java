package com.yishiji.angelplatform.inter;

import android.content.Context;
import android.content.Intent;

public interface Refreshable {

	public void refresh();

	public Context getContext();

	public void startActivityForResult(Intent intent, int i);

}