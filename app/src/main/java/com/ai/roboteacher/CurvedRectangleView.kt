package com.ai.roboteacher

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class CurvedRectangleView : View {

    var WIDTH = 0
    var HEIGHT = 0
    lateinit var paint: Paint

    constructor(c: Context) : super(c) {

        init(c)


    }

    constructor(a: AttributeSet, c: Context) : super(c,a) {




    }

    constructor(a: AttributeSet, c: Context , defstyle:Int) : super(c,a,defstyle) {


    }

    private fun init(c:Context) {

        WIDTH = (Utils.getScreenWidth(c) * 0.30).toInt()
        HEIGHT = (Utils.getScreenWidth(c) * 0.20).toInt()

        paint = Paint()
        paint.isAntiAlias = true


    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //canvas.drawRou
    }
}