package com.tracqi.fsensorapp.gauge;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.hardware.SensorManager;
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
 * Draws an analog gauge for displaying acceleration measurements in two-space
 * from device sensors.
 * 
 * Note that after Android 4.0 TextureView exists, as does SurfaceView for
 * Android 3.0 which won't hog the UI thread like View will. This should only be
 * used with devices or certain libraries that require View.
 * 
 * @author Kaleb
 * @version %I%, %G%
 * http://developer.android.com/reference/android/view/View.html
 */
public final class GaugeAcceleration extends View
{
	private static final String tag = GaugeAcceleration.class.getSimpleName();

	// holds the cached static part
	private Bitmap background;

	private Paint backgroundPaint;
	private Paint pointPaint;
	private Paint rimPaint;
	private Paint rimShadowPaint;

	private RectF faceRect;
	private RectF rimRect;
	// added by Scott
	private RectF rimOuterRect;
	private RectF innerRim;
	private RectF innerFace;
	private RectF innerMostDot;

	private float x;
	private float y;

	private float scaleX;
	private float scaleY;

	/**
	 * Create a new instance.
	 * 
	 * @param context
	 */
	public GaugeAcceleration(Context context)
	{
		super(context);
		init();
	}

	/**
	 * Create a new instance.
	 * 
	 * @param context
	 * @param attrs
	 */
	public GaugeAcceleration(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	/**
	 * Create a new instance.
	 * 
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public GaugeAcceleration(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init();
	}

	/**
	 * Update the measurements for the point.
	 * 
	 * @param x
	 *            the x-axis
	 * @param y
	 *            the y-axis

	 */
	public void updatePoint(float x, float y)
	{
		// Enforce a limit of 1g or 9.8 m/s^2
		if (x > SensorManager.GRAVITY_EARTH)
		{
			x = SensorManager.GRAVITY_EARTH;
		}
		if (x < -SensorManager.GRAVITY_EARTH)
		{
			x = -SensorManager.GRAVITY_EARTH;
		}
		if (y > SensorManager.GRAVITY_EARTH)
		{
			y = SensorManager.GRAVITY_EARTH;
		}
		if (y < -SensorManager.GRAVITY_EARTH)
		{
			y = -SensorManager.GRAVITY_EARTH;
		}

		this.x = scaleX * -x + rimRect.centerX();
		this.y = scaleY * y + rimRect.centerY();

		this.invalidate();
	}

	/**
	 * Initialize the members of the instance.
	 */
	private void init()
	{
		initDrawingTools();
	}

	/**
	 * Initialize the drawing related members of the instance.
	 */
	private void initDrawingTools()
	{
		rimRect = new RectF(0.1f, 0.1f, 0.9f, 0.9f);

		scaleX = ((rimRect.right - rimRect.left) / (SensorManager.GRAVITY_EARTH * 2));
		scaleY = ((rimRect.bottom - rimRect.top) / (SensorManager.GRAVITY_EARTH * 2));

		// inner rim oval
		innerRim = new RectF(0.25f, 0.25f, 0.75f, 0.75f);

		// inner most white dot
		innerMostDot = new RectF(0.47f, 0.47f, 0.53f, 0.53f);

		// the linear gradient is a bit skewed for realism
		rimPaint = new Paint();
		rimPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		rimPaint.setColor(getContext().getColor(R.color.md_theme_outline));

		float rimSize = 0.02f;
		faceRect = new RectF();
		faceRect.set(rimRect.left + rimSize, rimRect.top + rimSize,
				rimRect.right - rimSize, rimRect.bottom - rimSize);

		rimShadowPaint = new Paint();
		rimShadowPaint.setStyle(Paint.Style.FILL);
		rimShadowPaint.setAntiAlias(true);
		rimShadowPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));

		// set the size of the outside white with the rectangles.
		// a 'bigger' negative will increase the size.
		float rimOuterSize = -0.04f;
		rimOuterRect = new RectF();
		rimOuterRect.set(rimRect.left + rimOuterSize, rimRect.top
				+ rimOuterSize, rimRect.right - rimOuterSize, rimRect.bottom
				- rimOuterSize);

		// inner rim declarations the black oval/rect
		float rimInnerSize = 0.02f;
		innerFace = new RectF();
		innerFace.set(innerRim.left + rimInnerSize,
				innerRim.top + rimInnerSize, innerRim.right - rimInnerSize,
				innerRim.bottom - rimInnerSize);

		pointPaint = new Paint();
		pointPaint.setAntiAlias(true);
		pointPaint.setColor(getContext().getColor(R.color.md_theme_primaryFixedDim));
		pointPaint.setStyle(Paint.Style.FILL_AND_STROKE);

		backgroundPaint = new Paint();
		backgroundPaint.setFilterBitmap(true);
	}

	/**
	 * Measure the device screen size to scale the canvas correctly.
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);

		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		int chosenWidth = chooseDimension(widthMode, widthSize);
		int chosenHeight = chooseDimension(heightMode, heightSize);

		int chosenDimension = Math.min(chosenWidth, chosenHeight);

		setMeasuredDimension(chosenDimension, chosenDimension);
	}

	/**
	 * Indicate the desired canvas dimension.
	 * 
	 * @param mode
	 * @param size
	 * @return
	 */
	private int chooseDimension(int mode, int size)
	{
		if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY)
		{
			return size;
		}
		else
		{ // (mode == MeasureSpec.UNSPECIFIED)
			return getPreferredSize();
		}
	}

	/**
	 * In case there is no size specified.
	 * 
	 * @return default preferred size.
	 */
	private int getPreferredSize()
	{
		return 300;
	}

	/**
	 * Draw the gauge.
	 * 
	 * @param canvas
	 */
	private void drawGauge(Canvas canvas)
	{

		// first, draw the metallic body
		canvas.drawOval(rimRect, rimPaint);

		// draw the rim shadow inside the face
		canvas.drawOval(faceRect, rimShadowPaint);

		// draw the inner white rim circle
		canvas.drawOval(innerRim, rimPaint);

		// draw the inner black oval
		canvas.drawOval(innerFace, rimShadowPaint);

		// draw inner white dot
		canvas.drawOval(innerMostDot, rimPaint);
	}

	/**
	 * Draw the measurement point.
	 * 
	 * @param canvas
	 */
	private void drawPoint(Canvas canvas)
	{
		canvas.save();
		canvas.drawCircle(this.x, this.y, 0.025f, pointPaint);
		canvas.restore();
	}

	/**
	 * Draw the background of the canvas.
	 * 
	 * @param canvas
	 */
	private void drawBackground(Canvas canvas)
	{
		// Use the cached background bitmap.
		if (background == null)
		{
			Log.w(tag, "Background not created");
		}
		else
		{
			canvas.drawBitmap(background, 0, 0, backgroundPaint);
		}
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		drawBackground(canvas);

		float scale = (float) getWidth();
		canvas.save();
		canvas.scale(scale, scale);

		drawPoint(canvas);

		canvas.restore();
	}

	/**
	 * Indicate the desired size of the canvas has changed.
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		Log.d(tag, "Size changed to " + w + "x" + h);

		regenerateBackground();
	}

	/**
	 * Regenerate the background image. This should only be called when the size
	 * of the screen has changed. The background will be cached and can be
	 * reused without needing to redraw it.
	 */
	private void regenerateBackground()
	{
		// free the old bitmap
		if (background != null)
		{
			background.recycle();
		}

		background = Bitmap.createBitmap(getWidth(), getHeight(),
				Bitmap.Config.ARGB_8888);
		Canvas backgroundCanvas = new Canvas(background);
		float scale = (float) getWidth();
		backgroundCanvas.scale(scale, scale);

		drawGauge(backgroundCanvas);
	}

}
