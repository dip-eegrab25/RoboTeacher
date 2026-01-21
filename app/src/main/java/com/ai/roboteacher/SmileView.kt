package com.ai.roboteacher

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class SmileView : View {

    var path: Path?=null
    var tPath:Path?=null
    var paint: Paint?=null
    var tPaint: Paint?=null
    var c: Context?=null
    var m: Matrix?=null

    constructor(c: Context):super(c) {

        this.c = c


    }

    constructor(c: Context, a: AttributeSet):super(c,a) {

        this.c = c

        path = Path()
        paint = Paint()
        paint!!.isAntiAlias = true
        paint!!.setColor(Color.WHITE)
        paint!!.style = Paint.Style.FILL_AND_STROKE
        paint!!.strokeCap = Paint.Cap.ROUND

        tPath = Path()
        tPaint = Paint()
        tPaint!!.isAntiAlias = true
        tPaint!!.setColor(Color.RED)
        tPaint!!.style = Paint.Style.FILL_AND_STROKE
        tPaint!!.strokeCap = Paint.Cap.ROUND




    }

    constructor(c: Context, a: AttributeSet, defStyle:Int):super(c,a,defStyle) {

        this.c = c


    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        var scFactor = c!!.resources.displayMetrics.density

        val width = resolveSize((350*scFactor).toInt(),widthMeasureSpec)
        val height = resolveSize((250*scFactor).toInt(),heightMeasureSpec)
        setMeasuredDimension(width,height)
        //setBackgroundColor(Color.BLUE)



    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        path?.moveTo(0f,0f)

        path?.quadTo(175f,100f,350f,0f)

        //path?.moveTo(0f,0f)

        path?.quadTo(175f,300f,0f,0f)

        tPath!!.moveTo(125f,65f)

        tPath?.quadTo(175f,85f,225f,65f)

        //path?.moveTo(0f,0f)

        tPath?.quadTo(175f,150f,125f,65f)



        //tPath!!.addArc(125f,100f,225f,150f,0f,180f)

//        tPath!!.quadTo(175f,100f,225f,145f)

        canvas.drawPath(path!!,paint!!)
        canvas.drawPath(tPath!!,tPaint!!)


    }


}