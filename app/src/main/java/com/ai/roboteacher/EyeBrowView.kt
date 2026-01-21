package com.ai.roboteacher

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class EyeBrowView : View {

    var path:Path?=null
    var paint:Paint?=null
    var c:Context?=null

    constructor(c:Context):super(c) {

        this.c = c


    }

    constructor(c:Context,a:AttributeSet):super(c,a) {

        this.c = c

        path = Path()
        paint = Paint()
        paint!!.isAntiAlias = true
        paint!!.setColor(Color.WHITE)
        paint!!.style = Paint.Style.FILL_AND_STROKE
        paint!!.strokeCap = Paint.Cap.ROUND




    }

    constructor(c:Context,a:AttributeSet,defStyle:Int):super(c,a,defStyle) {

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

        var scWidth = Utils.getScreenWidth(c!!)
        var scFactor = c!!.resources.displayMetrics.density



//        path!!.moveTo(0f,300f*scFactor) //300f
//
//        path!!.quadTo(150f*scFactor,200f*scFactor,300f*scFactor,300f*scFactor)
//
//        path!!.cubicTo(350f*scFactor,400f*scFactor,150f*scFactor,200f*scFactor,0f,300f*scFactor)



        path!!.moveTo(0f,100f*scFactor) //300f

        path!!.quadTo(150f*scFactor,5f*scFactor,200f*scFactor,100f*scFactor)

        path!!.cubicTo(250f*scFactor,200f*scFactor,150f*scFactor,5f*scFactor,0f,100f*scFactor)

//        matrix.postRotate(180f)

        canvas.drawPath(path!!,paint!!)


//        path!!.moveTo(0f,100f*scFactor)
//
//        path!!.quadTo(150f*scFactor,5f*scFactor,300f*scFactor,100f*scFactor)
//
//        path!!.quadTo(150f*scFactor,20f*scFactor,0f,100f*scFactor)
//
//        canvas.drawPath(path!!,paint!!)

    }
}