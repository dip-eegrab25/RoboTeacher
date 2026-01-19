package com.ai.roboteacher.miko

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class DimpleView:View {

    var c:Context?=null
    var mPaint: Paint?=null
    var vAnim:ValueAnimator?=null


    constructor(context: Context?) : super(context) {

        this.c = context
        init()
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {

        this.c = context
        init()

    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {

        this.c = context
        init()
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {

        this.c = context
        init()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        var scFactor:Float = c!!.resources.displayMetrics.density

        var mWidth = 100*scFactor
        var mHeight = 100*scFactor

        var width = resolveSize(mWidth.toInt(),widthMeasureSpec)
        var height = resolveSize(mHeight.toInt(),heightMeasureSpec)

        setMeasuredDimension(width,height)
        scaleX = 0f
        scaleY = 0f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawCircle(width/2f,height/2f,30f,mPaint!!)
    }

    private fun init() {

        mPaint = Paint()
        mPaint!!.isAntiAlias = true
        mPaint!!.style = Paint.Style.FILL
        mPaint!!.color = Color.YELLOW

        vAnim = ValueAnimator.ofFloat(0f,0.5f)
        vAnim!!.duration = 400
        vAnim!!.addUpdateListener {

            scaleX = it.animatedValue as Float
            scaleY = it.animatedValue as Float

        }
    }

    public fun startAnim() {

        vAnim?.start()
    }


}