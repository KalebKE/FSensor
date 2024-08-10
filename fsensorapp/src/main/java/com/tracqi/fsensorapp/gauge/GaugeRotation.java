package com.tracqi.fsensorapp.gauge;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.tracqi.fsensorapp.R;

/*
 * AccelerationExplorer
 * Copyright 2018 Kircher Electronics, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Draws an analog gauge for displaying rotation measurements in three-space
 * from device sensors.
 *
 * @author Kaleb
 */
public final class GaugeRotation extends View {

    private static final String TAG = GaugeRotation.class.getSimpleName();

    // drawing tools
    private RectF rimOuterRect;
    private Paint rimOuterPaint;

    // Keep static bitmaps of the gauge so we only have to redraw if we have to
    // Static bitmap for the bezel of the gauge
    private Bitmap bezelBitmap;
    // Static bitmap for the face of the gauge
    private Bitmap faceBitmap;
    private Bitmap skyBitmap;
    private Bitmap mutableBitmap;

    // Keep track of the rotation of the device
    private float[] rotation = new float[3];

    // Rectangle to draw the rim of the gauge
    private RectF rimRect;

    // Rectangle to draw the sky section of the gauge face
    private RectF faceBackgroundRect;
    private RectF skyBackgroundRect;

    // Paint to draw the gauge bitmaps
    private Paint backgroundPaint;

    // Paint to draw the rim of the bezel
    private Paint rimPaint;

    // Paint to draw the sky portion of the gauge face
    private Paint skyPaint;

    /**
     * Create a new instance.
     *
     * @param context
     */
    public GaugeRotation(Context context) {
        super(context);

        initDrawingTools();
    }

    /**
     * Create a new instance.
     *
     * @param context
     * @param attrs
     */
    public GaugeRotation(Context context, AttributeSet attrs) {
        super(context, attrs);

        initDrawingTools();
    }

    /**
     * Create a new instance.
     *
     * @param context
     * @param attrs
     * @param defStyle
     */
    public GaugeRotation(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initDrawingTools();
    }

    /**
     * Update the rotation of the device.
     *
     * @param rotation
     */
    public void updateRotation(float[] rotation) {
        System.arraycopy(rotation, 0, this.rotation, 0, this.rotation.length);
        this.invalidate();
    }

    private void initDrawingTools() {
        // Rectangle for the rim of the gauge bezel
        rimRect = new RectF(0.12f, 0.12f, 0.88f, 0.88f);

        // Paint for the rim of the gauge bezel
        rimPaint = new Paint();
        rimPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        // The linear gradient is a bit skewed for realism
        rimPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));

        float rimOuterSize = -0.04f;
        rimOuterRect = new RectF();
        rimOuterRect.set(rimRect.left + rimOuterSize, rimRect.top
                + rimOuterSize, rimRect.right - rimOuterSize, rimRect.bottom
                - rimOuterSize);

        rimOuterPaint = new Paint();
        rimOuterPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        rimOuterPaint.setColor(getContext().getColor(R.color.md_theme_outline));

        float rimSize = 0.02f;

        faceBackgroundRect = new RectF();
        faceBackgroundRect.set(rimRect.left + rimSize, rimRect.top + rimSize,
                rimRect.right - rimSize, rimRect.bottom - rimSize);

        skyBackgroundRect = new RectF();
        skyBackgroundRect.set(rimRect.left + rimSize, rimRect.top + rimSize,
                rimRect.right - rimSize, rimRect.bottom - rimSize);

        skyPaint = new Paint();
        skyPaint.setAntiAlias(true);
        skyPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        skyPaint.setColor(getContext().getColor(R.color.md_theme_primaryFixedDim));

        backgroundPaint = new Paint();
        backgroundPaint.setFilterBitmap(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int chosenWidth = chooseDimension(widthMode, widthSize);
        int chosenHeight = chooseDimension(heightMode, heightSize);

        int chosenDimension = Math.min(chosenWidth, chosenHeight);

        setMeasuredDimension(chosenDimension, chosenDimension);
    }

    private int chooseDimension(int mode, int size) {
        if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY) {
            return size;
        } else { // (mode == MeasureSpec.UNSPECIFIED)
            return getPreferredSize();
        }
    }

    // in case there is no size specified
    private int getPreferredSize() {
        return 300;
    }

    /**
     * Draw the gauge rim.
     *
     * @param canvas
     */
    private void drawRim(Canvas canvas) {
        // First draw the most back rim
        canvas.drawOval(rimOuterRect, rimOuterPaint);
        // Then draw the small black line
        canvas.drawOval(rimRect, rimPaint);
    }

    /**
     * Draw the gauge face.
     *
     * @param canvas
     */
    private void drawFace(Canvas canvas) {

        float halfHeight = ((rimRect.top - rimRect.bottom)/2);

        float top = rimRect.top - halfHeight + (rotation[0]*halfHeight);

        if(rimRect.left <= rimRect.right && top <= rimRect.bottom) {
            // free the old bitmap
            if (faceBitmap != null) {
                faceBitmap.recycle();
            }

            if (skyBitmap != null) {
                skyBitmap.recycle();
            }

            if (mutableBitmap != null) {
                mutableBitmap.recycle();
            }

            skyPaint.setFilterBitmap(false);

            faceBitmap = Bitmap.createBitmap(getWidth(), getHeight(),
                    Bitmap.Config.ARGB_8888);
            skyBitmap = Bitmap.createBitmap(getWidth(), getHeight(),
                    Bitmap.Config.ARGB_8888);
            mutableBitmap = Bitmap.createBitmap(getWidth(), getHeight(),
                    Bitmap.Config.ARGB_8888);

            Canvas faceCanvas = new Canvas(faceBitmap);
            Canvas skyCanvas = new Canvas(skyBitmap);
            Canvas mutableCanvas = new Canvas(mutableBitmap);
            float scale = (float) getWidth();
            faceCanvas.scale(scale, scale);
            skyCanvas.scale(scale, scale);

            faceBackgroundRect.set(rimRect.left, rimRect.top, rimRect.right,
                    rimRect.bottom);


            skyBackgroundRect.set(rimRect.left, top, rimRect.right,
                    rimRect.bottom);

            faceCanvas.drawArc(faceBackgroundRect, 0, 360, true, skyPaint);
            skyCanvas.drawRect(skyBackgroundRect, skyPaint);

            float angle = (float) -Math.toDegrees(rotation[1]);

            canvas.save();
            canvas.rotate(angle, faceBitmap.getWidth() / 2f,
                    faceBitmap.getHeight() / 2f);

            mutableCanvas.drawBitmap(faceBitmap, 0, 0, skyPaint);
            skyPaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
            mutableCanvas.drawBitmap(skyBitmap, 0, 0, skyPaint);
            skyPaint.setXfermode(null);

            canvas.drawBitmap(mutableBitmap, 0, 0, backgroundPaint);
            canvas.restore();
        }
    }

    /**
     * Draw the gauge bezel.
     *
     * @param canvas
     */
    private void drawBezel(Canvas canvas) {
        if (bezelBitmap == null) {
            Log.w(TAG, "Bezel not created");
        } else {
            canvas.drawBitmap(bezelBitmap, 0, 0, backgroundPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d(TAG, "Size changed to " + w + "x" + h);

        regenerateBezel();
    }

    /**
     * Regenerate the background image. This should only be called when the size
     * of the screen has changed. The background will be cached and can be
     * reused without needing to redraw it.
     */
    private void regenerateBezel() {
        // free the old bitmap
        if (bezelBitmap != null) {
            bezelBitmap.recycle();
        }

        bezelBitmap = Bitmap.createBitmap(getWidth(), getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas bezelCanvas = new Canvas(bezelBitmap);
        float scale = (float) getWidth();
        bezelCanvas.scale(scale, scale);

        drawRim(bezelCanvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawBezel(canvas);
        drawFace(canvas);

        float scale = (float) getWidth();
        canvas.save();
        canvas.scale(scale, scale);

        canvas.restore();
    }

}