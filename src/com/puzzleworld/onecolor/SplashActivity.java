package com.puzzleworld.onecolor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/*
 * 每次打开闪屏界面
 */

public class SplashActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		boolean mFirst = isFirstEnter(SplashActivity.this, SplashActivity.this.getClass().getName());
		if (mFirst)
			mHandler.sendEmptyMessageDelayed(SWITCH_GUIDACTIVITY, 1000);
		else
			mHandler.sendEmptyMessageDelayed(SWITCH_MAINACTIVITY, 1000);
	}

	// ****************************************************************
	// 判断应用是否初次加载，读取SharedPreferences中的guide_activity字段
	// ****************************************************************
	private static final String SHAREDPREFERENCES_NAME = "my_pref";
	private static final String KEY_GUIDE_ACTIVITY = "guide_activity";

	private boolean isFirstEnter(Context context, String className) {
		if (context == null || className == null || "".equalsIgnoreCase(className))
			return false;
		String mResultStr = context.getSharedPreferences(SHAREDPREFERENCES_NAME, Context.MODE_WORLD_READABLE)
				.getString(KEY_GUIDE_ACTIVITY, "");// 取得所有类名 如com.my.MainActivity

		if (mResultStr.equalsIgnoreCase("version_1_1"))//判断是否已写入版本字符串，存在则不是第一次进入
			return false;
		else {

			SharedPreferences settings = getSharedPreferences(SHAREDPREFERENCES_NAME, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString(KEY_GUIDE_ACTIVITY, "version_1_1");//第一次进入，写入版本号，以后升级修改此版本号
			editor.commit();
			return true;
		}
	}

	// *************************************************
	// Handler:跳转至不同页面
	// *************************************************
	private final static int SWITCH_MAINACTIVITY = 1000;
	private final static int SWITCH_GUIDACTIVITY = 1001;
	public Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SWITCH_MAINACTIVITY:
				Intent mIntent = new Intent();
				mIntent.setClass(SplashActivity.this, ProcessActivity.class);
				SplashActivity.this.startActivity(mIntent);
				SplashActivity.this.finish();
				break;
			case SWITCH_GUIDACTIVITY:
				mIntent = new Intent();
				mIntent.setClass(SplashActivity.this, PicShowActivity.class);
				SplashActivity.this.startActivity(mIntent);
				SplashActivity.this.finish();
				break;
			}
			super.handleMessage(msg);
		}
	};
}
