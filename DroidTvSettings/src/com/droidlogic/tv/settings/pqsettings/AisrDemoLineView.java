/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.droidlogic.tv.settings.pqsettings;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Color;

import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import android.graphics.Canvas;
import android.graphics.Paint;

import android.util.AttributeSet;
import android.util.Log;

public class AisrDemoLineView extends View {
    private static final String TAG = "LineView";
    private float startX;
    private int moveCnt = 80;
    private boolean isLineVisible = false;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mParams;

    private static AisrDemoLineView mAisrDemoLineView;

    public static AisrDemoLineView getInstance(final Context context) {
        if (mAisrDemoLineView == null) {
            mAisrDemoLineView = new AisrDemoLineView(context, null);
        }
        return mAisrDemoLineView;
    }

    public AisrDemoLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
    }

    public void showLine() {
        Log.d(TAG, "showLine isLineVisible: " + isLineVisible);
        if (!isLineVisible) {
            mWindowManager.addView(this, mParams);
            DisplayMetrics outMetrics = new DisplayMetrics();
            mWindowManager.getDefaultDisplay().getMetrics(outMetrics);
            int widthPixels = outMetrics.widthPixels;
            setInitPosition(widthPixels/2);
            isLineVisible = true;
        }
    }

    public void hideLine() {
        Log.d(TAG, "hideLine isLineVisible: " + isLineVisible);
        if (isLineVisible) {
            mWindowManager.removeView(this);
            isLineVisible = false;
        }
    }

    public void setInitPosition(float x) {
        startX = x;
        Log.i(TAG, "LineView init position, startX:" + startX);
        invalidate();
    }

    public void setInitPosition() {
        startX = getWidth() / 2;
        Log.i(TAG, "LineView init position default, startX:" + startX);
    }

    public void moveLeft() {
        startX -= getWidth() / moveCnt;
        if (startX <= 0) {
            startX = 2;
        }
        invalidate();
    }

    public void moveRight() {
        startX += getWidth() / moveCnt;
        if (startX >= getWidth()) {
            startX = getWidth() - 3;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //paint
        Log.i(TAG, "onDraw,startX:" + startX);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1.0f);
        canvas.drawLine(startX, 0, startX, getHeight(), paint);

    }
}
