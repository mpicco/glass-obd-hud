/*
 * Copyright (C) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.glassista.android.glass.ObdHud;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * View used to draw HUD
 */
public class HudView extends View {

    private static final String TAG = HudRenderer.class.getSimpleName();

    private Paint mPaint = new Paint();
    private Paint lPaint = new Paint();

    private Integer mSpeed = 0;
    private Integer mRpm = 0;
    private Integer mGear = 0;
    private Integer mThrottle;

    public HudView(Context context) {
        this(context, null, 0);
    }

    public HudView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HudView(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);

        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextSize(224);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(2);

        lPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        lPaint.setTextSize(128);
        lPaint.setColor(Color.WHITE);
        lPaint.setStrokeWidth(1);

    }

    /**
     * Set the OBD data.
     *
     * @param timestamp Timestamp of obd sample
     * @param rpm RPM as measured by ECU
     * @param speed Speed in MPH as measured by ECU
     * @param gear Valid only for F800ST: computed from speed/rpm using F800ST specs.
     */
    public synchronized void setObdData(int rpm, int speed, int throttle, int gear) {
        mRpm = rpm;
        mSpeed = speed;
        mThrottle = throttle;
        mGear = gear;

        // Redraw the data.
        invalidate();
    }

    public int getRpm() {
        return mRpm;
    }

    public int getSpeed() {
        return mSpeed;
    }

    public int getGear() {
        return mGear;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = canvas.getWidth();
        int height = canvas.getHeight() / 2;

        //Log.v(TAG, "od [" + timestamp + "]");
        //canvas.drawText(t , 290, 160, mPaint);
        canvas.drawText(mRpm.toString(), 30, 120, lPaint);
        canvas.drawText(mThrottle.toString(), 500, 120, lPaint);
        canvas.drawText(mSpeed.toString(), 20, 340, mPaint);
        canvas.drawText((mGear == 0) ? "-" : mGear.toString(), 490, 340, mPaint);

    }

}
