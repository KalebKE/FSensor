package com.tracqi.fsensorapp.gauge

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.hardware.SensorManager
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.tracqi.fsensorapp.R
import kotlin.math.min

/*
* Copyright 2024, Tracqi Technology, LLC
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
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
 */
class GaugeAcceleration : View {
    // holds the cached static part
    private var background: Bitmap? = null

    private lateinit var backgroundPaint: Paint
    private lateinit var pointPaint: Paint
    private lateinit var rimPaint: Paint
    private lateinit var rimShadowPaint: Paint

    private lateinit var faceRect: RectF
    private lateinit var rimRect: RectF

    // added by Scott
    private lateinit var rimOuterRect: RectF
    private lateinit var innerRim: RectF
    private lateinit var innerFace: RectF
    private lateinit var innerMostDot: RectF

    private var x = 0f
    private var y = 0f

    private var scaleX = 0f
    private var scaleY = 0f

    /**
     * Create a new instance.
     *
     * @param context
     */
    constructor(context: Context?) : super(context) {
        init()
    }

    /**
     * Create a new instance.
     *
     * @param context
     * @param attrs
     */
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    /**
     * Create a new instance.
     *
     * @param context
     * @param attrs
     * @param defStyle
     */
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    /**
     * Update the measurements for the point.
     *
     * @param x
     * the x-axis
     * @param y
     * the y-axis
     */
    fun updatePoint(x: Float, y: Float) {
        // Enforce a limit of 1g or 9.8 m/s^2
        var x = x
        var y = y
        if (x > SensorManager.GRAVITY_EARTH) {
            x = SensorManager.GRAVITY_EARTH
        }
        if (x < -SensorManager.GRAVITY_EARTH) {
            x = -SensorManager.GRAVITY_EARTH
        }
        if (y > SensorManager.GRAVITY_EARTH) {
            y = SensorManager.GRAVITY_EARTH
        }
        if (y < -SensorManager.GRAVITY_EARTH) {
            y = -SensorManager.GRAVITY_EARTH
        }

        this.x = scaleX * -x + rimRect.centerX()
        this.y = scaleY * y + rimRect.centerY()

        this.invalidate()
    }

    /**
     * Initialize the members of the instance.
     */
    private fun init() {
        initDrawingTools()
    }

    /**
     * Initialize the drawing related members of the instance.
     */
    private fun initDrawingTools() {
        rimRect = RectF(0.1f, 0.1f, 0.9f, 0.9f)

        scaleX = (rimRect.right - rimRect.left) / (SensorManager.GRAVITY_EARTH * 2)
        scaleY = (rimRect.bottom - rimRect.top) / (SensorManager.GRAVITY_EARTH * 2)

        // inner rim oval
        innerRim = RectF(0.25f, 0.25f, 0.75f, 0.75f)

        // inner most white dot
        innerMostDot = RectF(0.47f, 0.47f, 0.53f, 0.53f)

        // the linear gradient is a bit skewed for realism
        rimPaint = Paint()
        rimPaint.flags = Paint.ANTI_ALIAS_FLAG
        rimPaint.color = context.getColor(R.color.md_theme_outline)

        val rimSize = 0.02f
        faceRect = RectF()
        faceRect[rimRect.left + rimSize, rimRect.top + rimSize, rimRect.right - rimSize] = rimRect.bottom - rimSize

        rimShadowPaint = Paint()
        rimShadowPaint.style = Paint.Style.FILL
        rimShadowPaint.isAntiAlias = true
        rimShadowPaint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.CLEAR))

        // set the size of the outside white with the rectangles.
        // a 'bigger' negative will increase the size.
        val rimOuterSize = -0.04f
        rimOuterRect = RectF()
        rimOuterRect[rimRect.left + rimOuterSize, rimRect.top
                + rimOuterSize, rimRect.right - rimOuterSize] = (rimRect.bottom
                - rimOuterSize)

        // inner rim declarations the black oval/rect
        val rimInnerSize = 0.02f
        innerFace = RectF()
        innerFace[innerRim.left + rimInnerSize, innerRim.top + rimInnerSize, innerRim.right - rimInnerSize] = innerRim.bottom - rimInnerSize

        pointPaint = Paint()
        pointPaint.isAntiAlias = true
        pointPaint.color = context.getColor(R.color.md_theme_primaryFixedDim)
        pointPaint.style = Paint.Style.FILL_AND_STROKE

        backgroundPaint = Paint()
        backgroundPaint.isFilterBitmap = true
    }

    /**
     * Measure the device screen size to scale the canvas correctly.
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val chosenWidth = chooseDimension(widthMode, widthSize)
        val chosenHeight = chooseDimension(heightMode, heightSize)

        val chosenDimension = min(chosenWidth.toDouble(), chosenHeight.toDouble()).toInt()

        setMeasuredDimension(chosenDimension, chosenDimension)
    }

    /**
     * Indicate the desired canvas dimension.
     *
     * @param mode
     * @param size
     * @return
     */
    private fun chooseDimension(mode: Int, size: Int): Int {
        return if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY) {
            size
        } else { // (mode == MeasureSpec.UNSPECIFIED)
            preferredSize
        }
    }

    private val preferredSize: Int
        /**
         * In case there is no size specified.
         *
         * @return default preferred size.
         */
        get() = 300

    /**
     * Draw the gauge.
     *
     * @param canvas
     */
    private fun drawGauge(canvas: Canvas) {
        // first, draw the metallic body

        canvas.drawOval(rimRect, rimPaint)

        // draw the rim shadow inside the face
        canvas.drawOval(faceRect, rimShadowPaint)

        // draw the inner white rim circle
        canvas.drawOval(innerRim, rimPaint)

        // draw the inner black oval
        canvas.drawOval(innerFace, rimShadowPaint)

        // draw inner white dot
        canvas.drawOval(innerMostDot, rimPaint)
    }

    /**
     * Draw the measurement point.
     *
     * @param canvas
     */
    private fun drawPoint(canvas: Canvas) {
        canvas.save()
        canvas.drawCircle(this.x, this.y, 0.025f, pointPaint)
        canvas.restore()
    }

    /**
     * Draw the background of the canvas.
     *
     * @param canvas
     */
    private fun drawBackground(canvas: Canvas) {
        // Use the cached background bitmap.
        if (background != null) {
            canvas.drawBitmap(background!!, 0f, 0f, backgroundPaint)
        }
    }

    override fun onDraw(canvas: Canvas) {
        drawBackground(canvas)

        val scale = width.toFloat()
        canvas.save()
        canvas.scale(scale, scale)

        drawPoint(canvas)

        canvas.restore()
    }

    /**
     * Indicate the desired size of the canvas has changed.
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        regenerateBackground()
    }

    /**
     * Regenerate the background image. This should only be called when the size
     * of the screen has changed. The background will be cached and can be
     * reused without needing to redraw it.
     */
    private fun regenerateBackground() {
        // free the old bitmap
        if (background != null) {
            background!!.recycle()
        }

        background = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888)
        val backgroundCanvas = Canvas(background!!)
        val scale = width.toFloat()
        backgroundCanvas.scale(scale, scale)

        drawGauge(backgroundCanvas)
    }
}
