package com.puzzleworld.onecolor;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class ImageProcesser {
	private static final int MAX_LEVEL = 100;
	private static final int MAX_POINTS_COUNT = 10;

	private static ImageProcesser instance = null;
	private int mLevel = 0;
	private int mBgColor = 0;
	private int mIsBlur = 0;
	private int[] mTouchPoints;
	private int mTouchPointsCount;

	private native int[] ImgFun(int[] buf, int w, int h, int[] touchPoints, int touchPointsCount, int value,
			int bgColor, int bgBlur);

	private ImageProcesser() {
		System.loadLibrary("img_processor");
		mLevel = mBgColor = mIsBlur = mTouchPointsCount = 0;
		mTouchPoints = new int[20];
	}

	public static ImageProcesser getInstance() {
		if (instance == null) {
			instance = new ImageProcesser();
		}
		return instance;
	}

	public int setLevel(int level) {
		if (level > MAX_LEVEL) {
			return -1;
		}
		mLevel = level;
		return 0;
	}

	public void setBgColor(int color) {
		mBgColor = color;
	}

	public void setBlur(int isBlur) {
		mIsBlur = isBlur;
	}

	public int setTouchPoints(int[] points, int count) {
		if (count > MAX_POINTS_COUNT) {
			return -1;
		}
		System.arraycopy(points, 0, mTouchPoints, 0, count * 2);
		mTouchPointsCount = count;
		return 0;
	}

	public int addTouchPoint(int x, int y) {
		if (mTouchPointsCount == MAX_POINTS_COUNT) {
			return -1;
		}

		mTouchPoints[2 * mTouchPointsCount] = x;
		mTouchPoints[2 * mTouchPointsCount + 1] = y;
		mTouchPointsCount++;
		return 0;
	}

	public int delTouchPoint() {
		if (mTouchPointsCount == 0) {
			return -1;
		}

		mTouchPointsCount--;
		return 0;
	}

	public void clearTouchPoint() {
		mTouchPointsCount = 0;
		return;
	}

	public Bitmap processImage(Bitmap img) {
		if (mTouchPointsCount == 0) {
			return img;
		}

		int w = img.getWidth(), h = img.getHeight();
		// 获取bitmap像素颜色值存入pix数组，后面传入算法
		int[] pix = new int[w * h];
		img.getPixels(pix, 0, w, 0, 0, w, h);
		int[] resultInt = ImgFun(pix, w, h, mTouchPoints, mTouchPointsCount, mLevel, mBgColor, mIsBlur);
		Bitmap resultImg = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		resultImg.setPixels(resultInt, 0, w, 0, 0, w, h);
		return resultImg;
	}

	// 给图片添加文字心情
	public Bitmap addString(String str, Bitmap srcBmp) {
		int width = srcBmp.getWidth();
		int height = srcBmp.getHeight();

		Bitmap imgTemp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(imgTemp);

		Paint paint = new Paint(); // 建立画笔
		paint.setDither(true);
		paint.setFilterBitmap(true);
		Rect src = new Rect(0, 0, width, height);
		Rect dst = new Rect(0, 0, width, height);
		canvas.drawBitmap(srcBmp, src, dst, paint); // 将 previewBitmap
													// 缩放或扩大到 dst 使用的填充区
													// paint

		Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG); // 设置画笔
		textPaint.setTextSize(42.0f); // 字体大小
		// textPaint.setTypeface(Typeface.DEFAULT_BOLD); // 采用默认的宽度
		int leftX = (width - 42 * str.length()) / 2;
		textPaint.setColor(Color.WHITE);
		canvas.drawText(str, leftX, height * 19 / 20, textPaint); // 绘制上去字，开始未知x,y采用那只笔绘制
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		return imgTemp;
	}
}
