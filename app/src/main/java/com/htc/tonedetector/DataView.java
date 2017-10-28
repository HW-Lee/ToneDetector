package com.htc.tonedetector;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by HWLee on 28/10/2017.
 */

public class DataView extends View {
    static final private String TAG = "DataView";

    private int mBgColor;
    private Paint mGridPaint;
    private Paint mDataPaint;

    private ArrayList<Double> mDataBuffer;

    public DataView(Context context) {
        super(context);
        init();
    }

    public DataView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DataView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public DataView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mBgColor = Color.BLACK;
        mGridPaint = new Paint();
        mDataPaint = new Paint();
        mDataBuffer = new ArrayList<>(0);

        mGridPaint.setStrokeWidth(5.0f);
        mGridPaint.setColor(Color.GRAY);

        mDataPaint.setStrokeWidth(5.0f);
        mDataPaint.setColor(Color.GREEN);
    }

    public Paint getGridPaint() {
        return mGridPaint;
    }

    public Paint getDataPaint() {
        return mDataPaint;
    }

    public void plot(Collection<? extends Double> data) {
        mDataBuffer.clear();
        mDataBuffer.addAll(data);

        this.postInvalidate();
    }

    private float convertToViewPosition(double dataY, int height) {
        return height/2.0f * (1 - (float) dataY);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Log.d(TAG, "onDraw, plot data(" + mDataBuffer.size() + ")");

        int viewHeight = this.getMeasuredHeight();
        int viewWidth = this.getMeasuredWidth();

        canvas.drawColor(mBgColor);
        canvas.drawLine(0, viewHeight/2.0f, viewWidth, viewHeight/2.0f, mGridPaint);

        for (int i = 0; i < mDataBuffer.size()-1; i++) {
            float startX = (float) viewWidth/mDataBuffer.size() * i;
            float endX = (float) viewWidth/mDataBuffer.size() * (i+1);
            float startY = this.convertToViewPosition(mDataBuffer.get(i), viewHeight);
            float endY = this.convertToViewPosition(mDataBuffer.get(i+1), viewHeight);

            canvas.drawLine(startX, startY, endX, endY, mDataPaint);
        }
    }
}
