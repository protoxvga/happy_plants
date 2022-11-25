package com.pperrin54.happy_plants.plant_graph_activity

import android.content.Context
import android.graphics.*
import android.text.SpannableStringBuilder
import android.text.style.SuperscriptSpan
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.pperrin54.happy_plants.R
import kotlin.math.roundToInt


class CustomGraph : View {
    private var title: String = "happiness (%)"
    private var type: String = "happiness"
    private var axisMultiplier: Int = 10
    private var data: ArrayList<Float>? = null
    private var dateArray: ArrayList<String>? = null
    private var mPaint: Paint? = null
    private var textPaint: Paint? = null
    private var mIsInit = false
    private var mPath: Path? = null
    private var mOriginX = 0f
    private var mOriginY = 0f
    private var mWidth = 0
    private var mHeight = 0
    private var mXUnit = 0f
    private var mYUnit = 0f
    private var mBlackPaint: Paint? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun changeData(data: ArrayList<Float>, dateArray: ArrayList<String>, title: String, type: String, axisMultiplier: Int) {
        this.data = data
        this.dateArray = dateArray
        this.title = title
        this.type = type
        this.axisMultiplier = axisMultiplier
        mIsInit = false
        invalidate()
    }

    fun setData(_data: ArrayList<Float>, dates: ArrayList<String>) {
        data = _data
        dateArray = dates
    }

    private fun init() {
        textPaint = Paint()
        mPaint = Paint()
        mPath = Path()
        mWidth = width
        mHeight = height
        mXUnit = (mWidth / 12).toFloat() //for 10 plots on x axis, 2 kept for padding;
        mYUnit = (mHeight / 12).toFloat()
        mOriginX = mXUnit
        mOriginY = mHeight - mYUnit
        mBlackPaint = Paint()
        mIsInit = true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (!mIsInit) {
            init()
        }
        textPaint!!.color = Color.BLACK
        textPaint!!.style = Paint.Style.FILL
        mBlackPaint!!.color = Color.BLACK
        mBlackPaint!!.style = Paint.Style.STROKE
        mBlackPaint!!.strokeWidth = 5f
        mPaint!!.style = Paint.Style.STROKE
        mPaint!!.strokeWidth = 9f
        mPaint!!.color = ContextCompat.getColor(context, R.color.default_green)
        drawAxis(canvas!!, mBlackPaint!!)
        drawGraphPlotLines(canvas, mPaint!!, textPaint!!)
        drawGraphPaper(canvas, mBlackPaint!!)
        drawTextOnXaxis(canvas, textPaint!!)
        drawTextOnYaxis(canvas, textPaint!!)
    }

    private fun drawTextOnXaxis(canvas: Canvas, paint: Paint) {
        paint.textSize = 30f
        var originX = mXUnit
        for (i in dateArray!!.indices) {
            if (i % 2 == 0)
                canvas.drawText(dateArray!![i], originX + (mXUnit / 2f), mHeight + 0f, paint)
            else
                canvas.drawText(dateArray!![i], originX + (mXUnit / 2f), mHeight - 40f, paint)
            originX += mXUnit
        }
    }

    private fun drawTextOnYaxis(canvas: Canvas, paint: Paint) {
        paint.textSize = 40f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Plant", 0f, 30f, paint)
        canvas.drawText(title, 0f, 60f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        var cy = mHeight - (mYUnit + 60f)
        paint.strokeWidth = 1f
        for (i in 1..10) {
            canvas.drawText("${i * axisMultiplier}", 0f, cy + 5f, paint)
            cy -= mYUnit
        }
    }

    private fun drawAxis(canvas: Canvas, paint: Paint) {
        canvas.drawLine(mXUnit, mYUnit, mXUnit, (mHeight - 10).toFloat(), paint) //y-axis
        canvas.drawLine(
            10f, mHeight - mYUnit,
            mWidth - mXUnit, mHeight - mYUnit, paint
        ) //x-axis
    }

    private fun drawGraphPlotLines(canvas: Canvas, paint: Paint, textPaint: Paint) {
        var originX = mXUnit
        val originY = mHeight - mYUnit
        textPaint.textSize = 25f
        textPaint.style = Paint.Style.FILL
        paint.alpha = 200
        mPath!!.moveTo(originX + mXUnit, originY - data!![0] * mYUnit) //shift origin to graph's origin

        for (i in 0 until data!!.size) {
            mPath!!.lineTo(originX + mXUnit, originY - data!![i] * mYUnit)
            paint.alpha = 255
            canvas.drawCircle(originX + mXUnit, originY - data!![i] * mYUnit, 5f, paint)
            paint.alpha = 150
            if (type == "happiness" || type == "humidity") {
                canvas.drawText("${(data!![i] * 10).roundToInt()}%", originX + mXUnit - 10f, originY - data!![i] * mYUnit + 60f, textPaint)
            } else if (type == "temperature") {
                canvas.drawText("${(data!![i] * 3).roundToInt()}°C", originX + mXUnit - 10f, originY - data!![i] * mYUnit + 60f, textPaint)
            }
            originX += mXUnit
        }
        canvas.drawPath(mPath!!, paint)
    }

    private fun drawGraphPaper(canvas: Canvas, blackPaint: Paint) {
        var cx = mXUnit
        var cy = mHeight - mYUnit
        blackPaint.strokeWidth = 1f
        blackPaint.alpha = 50
        for (i in 1..11) {
            canvas.drawLine(cx, mYUnit, cx, cy, blackPaint)
            cx += mXUnit
        } //drawing points on x axis(vertical lines)
        cx = mXUnit
        for (i in 1..11) {
            canvas.drawLine(cx, cy, mWidth - mXUnit, cy, blackPaint)
            cy -= mYUnit
        } //drawing points on y axis
    }
}
