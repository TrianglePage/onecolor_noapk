package com.puzzleworld.onecolor;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;

/*
 * 作品展示页面
 */

public class PicShowActivity extends Activity {
	private static final String TAG = "test";
	private ViewPager viewpager = null;
	private List<View> list = null;
	private ImageView[] img = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_picshow);
		viewpager = (ViewPager) findViewById(R.id.viewpager);
		list = new ArrayList<View>();
		list.add(getLayoutInflater().inflate(R.layout.tab1, null));
		list.add(getLayoutInflater().inflate(R.layout.tab2, null));
		list.add(getLayoutInflater().inflate(R.layout.tab3, null));

		img = new ImageView[list.size()];
		LinearLayout layout = (LinearLayout) findViewById(R.id.viewGroup);
		for (int i = 0; i < list.size(); i++) {
			img[i] = new ImageView(PicShowActivity.this);
			if (0 == i) {
				img[i].setBackgroundResource(R.drawable.dot_focus);
			} else {
				img[i].setBackgroundResource(R.drawable.dot_dark);
			}
			img[i].setPadding(0, 0, 20, 0);
			layout.addView(img[i]);
		}
		viewpager.setAdapter(new ViewPagerAdapter(list));
		viewpager.setOnPageChangeListener(new ViewPagerPageChangeListener());
	}
	
	class ViewPagerPageChangeListener implements OnPageChangeListener {

		/*
		 * state：网上通常说法：1的时候表示正在滑动，2的时候表示滑动完毕了，0的时候表示什么都没做，就是停在那；
		 * 我的认为：1是按下时，0是松开，2则是新的标签页的是否滑动了
		 * (例如：当前页是第一页，如果你向右滑不会打印出2，如果向左滑直到看到了第二页，那么就会打印出2了)； 个人认为一般情况下是不会重写这个方法的
		 */
		@Override
		public void onPageScrollStateChanged(int state) {
		}

		/*
		 * page：看名称就看得出，当前页； positionOffset：位置偏移量，范围[0,1]；
		 * positionoffsetPixels：位置像素，范围[0,屏幕宽度)； 个人认为一般情况下是不会重写这个方法的
		 */
		@Override
		public void onPageScrolled(int page, float positionOffset, int positionOffsetPixels) {
		}

		@Override
		public void onPageSelected(int page) {
			// 更新图标
			for (int i = 0; i < list.size(); i++) {
				if (page == i) {
					img[i].setBackgroundResource(R.drawable.dot_focus);
					if(i == (list.size() - 1))
					{
						ImageButton btnEnter = (ImageButton) findViewById(R.id.btnEnter);
						btnEnter.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								final Intent intent = new Intent();
								intent.setClass(PicShowActivity.this, ProcessActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								PicShowActivity.this.startActivity(intent);
								finish();
							}
						});
					}
				} else {
					img[i].setBackgroundResource(R.drawable.dot_dark);
				}
			}
		}
	}
}




