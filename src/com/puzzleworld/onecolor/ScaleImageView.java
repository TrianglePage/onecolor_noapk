package com.puzzleworld.onecolor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;

@SuppressLint("FloatMath")
public class ScaleImageView extends ImageView {
    private Context mContext;
    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    private boolean processed = false;
    private boolean misForProcessPic = false;
    private float mTouchX = 0;
    private float mTouchY = 0;
    private int[] touchPoints = new int[20];// 保存点击座标，顺序为x1，y1，x2，y2.。。

    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    // Remember some things for zooming
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;

    public void setContext(Context c) {
        mContext = c;
    }

    /*
     * isForProcessPic表示设置的图片是否用于处理，第一次显示的是添加图片，不用于处理
     * 注意不能再oncreate函数中调用，因为函数内部获取控件宽高会变成0
     */
    public void setImageBitmapEx(Bitmap bm, boolean isForProcessPic) {
        misForProcessPic = isForProcessPic;

        float scaleX;
        float scaleY;
        float scale;

        super.setImageBitmap(bm);
        // setImageMatrix(matrix);
        matrix.reset();
        float[] values = new float[9];
        matrix.getValues(values);
        float offsetX = values[2];
        float offsetY = values[5];
        // 不是处理后的图片，是重新选择的，在此做缩放处理以适合控件
        scaleX = (float) this.getWidth() / (float) bm.getWidth();
        scaleY = (float) this.getHeight() / (float) bm.getHeight();
        // scale = scaleX < scaleY ? scaleX : scaleY;
        if (scaleX < scaleY) {
            scale = scaleX;
            offsetY = (this.getHeight() - bm.getHeight() * scale) / 2;
        } else {
            scale = scaleY;
            offsetX = (this.getWidth() - bm.getWidth() * scale) / 2;
        }

        // Log.i("chz", "init scale = " + scale + ",w = " +
        // this.getWidth() + ",h = " + this.getHeight() + ",bw ="
        // + bm.getWidth() + ",bh=" + bm.getHeight() + "offX=" + offsetX
        // + "offy=" + offsetY);
        matrix.postScale(scale, scale, 0, 0);
        matrix.postTranslate(offsetX, offsetY);

        setImageMatrix(matrix);
    }

    public ScaleImageView(Context context) {
        super(context);
    }

    public ScaleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ScaleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    // public boolean onTouchEvent(MotionEvent event) {
    // performClick();//显式调用这个函数，才会调到注册的onClick函数。
    // return true;
    // }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Bitmap showBitmap = BitmapStore.getBitmapOriginal();
        if (showBitmap == null) {
            Toast.makeText(mContext, "请选择一张图片哦😉", Toast.LENGTH_LONG).show();
            return true;
        }
        Log.d("chz", "event..." + event.getActionMasked());
        switch (event.getActionMasked()) {
        case MotionEvent.ACTION_DOWN:
            matrix.set(getImageMatrix());
            savedMatrix.set(matrix);
            start.set(event.getX(), event.getY());
            Log.d("Infor", "触摸了...");
            mode = DRAG;
            break;
        case MotionEvent.ACTION_POINTER_DOWN: // 多点触控
            oldDist = this.spacing(event);
            if (oldDist > 10f) {
                // Log.d("Infor", "oldDist" + oldDist);
                savedMatrix.set(matrix);
                midPoint(mid, event);
                mode = ZOOM;
            }
            break;
        case MotionEvent.ACTION_POINTER_UP:
            mode = NONE;
            break;
        case MotionEvent.ACTION_MOVE:
            if (mode == DRAG) { // 此实现图片的拖动功能...
                matrix.set(savedMatrix);
                matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
            } else if (mode == ZOOM) {// 此实现图片的缩放功能...
                float newDist = spacing(event);
                if (newDist > 10) {
                    matrix.set(savedMatrix);
                    float scale = newDist / oldDist;
                    matrix.postScale(scale, scale, mid.x, mid.y);
                }
            }
            break;
        case MotionEvent.ACTION_UP:
            if (start.x == event.getX() && start.y == event.getY()) {
                float[] values = new float[9];
                // 图片可能被缩放和移动，获取图片移动偏移和缩放比例
                matrix.getValues(values);
                float offsetX = values[2];
                float offsetY = values[5];
                float scaleX = values[0];
                float scaleY = values[4];

                // 计算点击坐标相对原始图片的实际坐标
                float actualX = (event.getX() - offsetX) / scaleX;
                float actualY = (event.getY() - offsetY) / scaleY;

                int w = showBitmap.getWidth(), h = showBitmap.getHeight();

                if ((int) actualX > 0 && (int) actualY > 0 && (int) actualX < w && (int) actualY < h) {
                    ImageProcesser ip = ImageProcesser.getInstance();
                    ip.addTouchPoint((int) actualX, (int) actualY);
                    Bitmap processedImg = ip.processImage(showBitmap);
                    this.setImageBitmap(processedImg);
                    BitmapStore.setBitmapProcessed(processedImg);
                    Log.i("chz", "iv onclick processed");
                }
            }
        }
        setImageMatrix(matrix);
        performClick();// 显式调用这个函数，才会调到注册的onClick函数。
        return true;
    }
}