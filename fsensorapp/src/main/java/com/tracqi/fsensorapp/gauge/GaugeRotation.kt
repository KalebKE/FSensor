package com.tracqi.fsensorapp.gauge

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.tracqi.fsensorapp.R
import com.tracqi.fsensorapp.gauge.GaugeRotation
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
 * Draws an analog gauge for displaying rotation measurements in three-space
 * from device sensors.
 *
 */
class GaugeRotation : View {
    // drawing tools
    private lateinit var rimOuterRect: RectF
    private lateinit var rimOuterPaint: Paint

    // Keep static bitmaps of the gauge so we only have to redraw if we have to
    // Static bitmap for the bezel of the gauge
    private var bezelBitmap: Bitmap? = null

    // Static bitmap for the face of the gauge
    private var faceBitmap: Bitmap? = null
    private var skyBitmap: Bitmap? = null
    private var mutableBitmap: Bitmap? = null

    private var faceCanvas: Canvas? = null
    private var mutableCanvas: Canvas? = null

    // Keep track of the rotation of the device
    private val rotation = FloatArray(3)

    // Rectangle to draw the rim of the gauge
    private lateinit var rimRect: RectF

    // Rectangle to draw the sky section of the gauge face
    private lateinit var faceBackgroundRect: RectF
    private lateinit var skyBackgroundRect: RectF

    // Paint to draw the gauge bitmaps
    private lateinit var backgroundPaint: Paint

    // Paint to draw the rim of the bezel
    private lateinit var rimPaint: Paint

    // Paint to draw the sky portion of the gauge face
    private lateinit var skyPaint: Paint

    /**
     * Create a new instance.
     *
     * @param context
     */
    constructor(context: Context?) : super(context) {
        initDrawingTools()
    }

    /**
     * Create a new instance.
     *
     * @param context
     * @param attrs
     */
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initDrawingTools()
    }

    /**
     * Create a new instance.
     *
     * @param context
     * @param attrs
     * @param defStyle
     */
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        initDrawingTools()
    }

    /**
     * Update the rotation of the device.
     *
     * @param rotation
     */
    fun updateRotation(rotation: FloatArray) {
        System.arraycopy(rotation, 0, this.rotation, 0, this.rotation.size)
        this.invalidate()
    }

    private fun initDrawingTools() {
        // Rectangle for the rim of the gauge bezel
        rimRect = RectF(0.12f, 0.12f, 0.88f, 0.88f)

        // Paint for the rim of the gauge bezel
        rimPaint = Paint()
        rimPaint.flags = Paint.ANTI_ALIAS_FLAG
        // The linear gradient is a bit skewed for realism
        rimPaint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.CLEAR))

        val rimOuterSize = -0.04f
        rimOuterRect = RectF()
        rimOuterRect[rimRect.left + rimOuterSize, rimRect.top
                + rimOuterSize, rimRect.right - rimOuterSize] = (rimRect.bottom
                - rimOuterSize)

        rimOuterPaint = Paint()
        rimOuterPaint.flags = Paint.ANTI_ALIAS_FLAG
        rimOuterPaint.color = context.getColor(R.color.md_theme_outline)

        val rimSize = 0.02f

        faceBackgroundRect = RectF()
        faceBackgroundRect[rimRect.left, rimRect.top, rimRect.right] = rimRect.bottom

        skyBackgroundRect = RectF()
        skyBackgroundRect[rimRect.left + rimSize, rimRect.top + rimSize, rimRect.right - rimSize] = rimRect.bottom - rimSize

        skyPaint = Paint()
        skyPaint.isAntiAlias = true
        skyPaint.flags = Paint.ANTI_ALIAS_FLAG
        skyPaint.color = context.getColor(R.color.md_theme_primaryFixedDim)

        backgroundPaint = Paint()
        backgroundPaint.isFilterBitmap = true
    }

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

    private fun chooseDimension(mode: Int, size: Int): Int {
        return if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY) {
            size
        } else { // (mode == MeasureSpec.UNSPECIFIED)
            preferredSize
        }
    }

    private val preferredSize: Int
        // in case there is no size specified
        get() = 300

    /**
     * Draw the gauge rim.
     *
     * @param canvas
     */
    private fun drawRim(canvas: Canvas) {
        // First draw the most back rim
        canvas.drawOval(rimOuterRect, rimOuterPaint)
        // Then draw the small black line
        canvas.drawOval(rimRect, rimPaint)
    }

    /**
     * Draw the gauge face.
     *
     * @param canvas
     */
    private fun drawFace(canvas: Canvas) {
        val halfHeight = ((rimRect.top - rimRect.bottom) / 2)

        val top = rimRect.top - halfHeight + (rotation[1] * halfHeight)

        if (rimRect.left <= rimRect.right && top <= rimRect.bottom) {
            // free the old bitmap
            if (faceBitmap == null) {
                faceBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                faceCanvas = Canvas(faceBitmap!!)
            }

            if (skyBitmap == null) {
                skyBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            }

            if (mutableBitmap == null) {
                mutableBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                mutableCanvas = Canvas(mutableBitmap!!)
            }

            skyPaint.isFilterBitmap = false

            skyBitmap!!.eraseColor(Color.TRANSPARENT)

            val skyCanvas = Canvas(skyBitmap!!)

            val scale = width.toFloat()
            faceCanvas!!.scale(scale, scale)
            skyCanvas.scale(scale, scale)

            skyBackgroundRect[rimRect.left, top, rimRect.right] = rimRect.bottom

            faceCanvas!!.drawArc(faceBackgroundRect, 0f, 360f, true, skyPaint)
            skyCanvas.drawRect(skyBackgroundRect, skyPaint)

            val angle = -Math.toDegrees(rotation[2].toDouble()).toFloat()

            canvas.save()
            canvas.rotate(angle, faceBitmap!!.width / 2f, faceBitmap!!.height / 2f)

            mutableCanvas!!.drawBitmap(faceBitmap!!, 0f, 0f, skyPaint)
            skyPaint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.DST_IN))
            mutableCanvas!!.drawBitmap(skyBitmap!!, 0f, 0f, skyPaint)
            skyPaint.setXfermode(null)

            canvas.drawBitmap(mutableBitmap!!, 0f, 0f, backgroundPaint)
            canvas.restore()
        }
    }

    /**
     * Draw the gauge bezel.
     *
     * @param canvas
     */
    private fun drawBezel(canvas: Canvas) {
        if (bezelBitmap != null) {
            canvas.drawBitmap(bezelBitmap!!, 0f, 0f, backgroundPaint)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        regenerateBezel()
    }

    /**
     * Regenerate the background image. This should only be called when the size
     * of the screen has changed. The background will be cached and can be
     * reused without needing to redraw it.
     */
    private fun regenerateBezel() {
        // free the old bitmap
        if (bezelBitmap != null) {
            bezelBitmap!!.recycle()
        }

        bezelBitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888)
        val bezelCanvas = Canvas(bezelBitmap!!)
        val scale = width.toFloat()
        bezelCanvas.scale(scale, scale)

        drawRim(bezelCanvas)
    }

    override fun onDraw(canvas: Canvas) {
        drawBezel(canvas)
        drawFace(canvas)

        val scale = width.toFloat()
        canvas.save()
        canvas.scale(scale, scale)

        canvas.restore()
    }
}