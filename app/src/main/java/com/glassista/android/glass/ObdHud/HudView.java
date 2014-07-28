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
    private int mSpeed = 0;
    private int mRpm = 0;
    private int mGear = 0;
    private String mTimestamp;

    public HudView(Context context) {
        this(context, null, 0);
    }

    public HudView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HudView(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);

        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        //mPaint.setTextSize(192);
        mPaint.setTextSize(42);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(2);
    }

    /**
     * Set the OBD data.
     *
     * @param timestamp Timestamp of obd sample
     * @param rpm RPM as measured by ECU
     * @param speed Speed in MPH as measured by ECU
     * @param gear Valid only for F800ST: computed from speed/rpm using F800ST specs.
     */
    public synchronized void setObdData(String timestamp, int rpm, int speed, int gear) {
        mTimestamp = timestamp;
        mRpm = rpm;
        mSpeed = speed;
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

        String timestamp = (mTimestamp != null) ? mTimestamp : "Bluetooth connection?";
        //Log.v(TAG, "od [" + timestamp + "]");
        //canvas.drawText(t , 290, 160, mPaint);
        canvas.drawText(timestamp, 10, 160, mPaint);
    }

}
