package com.puzzleworld.onecolor;

import java.util.List;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

/*
 * 用来装三个作品展示页
 */

class ViewPagerAdapter extends PagerAdapter {

	private List<View> list = null;

	public ViewPagerAdapter(List<View> list) {
		this.list = list;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		container.addView(list.get(position));
		return list.get(position);
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView(list.get(position));
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

}