package com.ai.roboteacher.roboUis

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class RoboLip : View {

    var path:Path?=null
    var path1:Path?=null
    var c:Context?=null
    var paint:Paint?=null
    var scFactor = 0f
    var mouthCurveUP = 80
    var mouthCurveDOWN = 120
    var valueAnim:ValueAnimator?=null

    var paint1:Paint?=null

    constructor(context: Context?) : super(context) {

        c = context

        init()


    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {

        c = context

        init()

    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {

        c = context

        init()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        var width = resolveSize(210*scFactor.toInt(),widthMeasureSpec)
        var height = resolveSize(210*scFactor.toInt(),heightMeasureSpec)

        setMeasuredDimension(width,height)

        //setBackgroundColor(Color.BLUE)


    }



    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        path!!.moveTo(100f,100f)



        path!!.quadTo(120f,70f,150f,80f)
        path!!.quadTo(170f,70f,200f,100f)
        path!!.quadTo(150f,mouthCurveUP.toFloat(),100f,100f)



        path1!!.moveTo(100f,100f)
        path1!!.quadTo(150f,mouthCurveDOWN.toFloat(),200f,100f)
        path1!!.quadTo(150f,150f,100f,100f)


        canvas.drawPath(path!!,paint!!)
        canvas.drawPath(path1!!,paint1!!)










//        path!!.moveTo(0f*scFactor,100f*scFactor)
//
//        //path!!.lineTo(200f,200f)
//        //path!!.close()
//
//        //path!!.quadTo(110f,190f,120f,180f)
//
//        path!!.quadTo(50f*scFactor
//            ,110f*scFactor
//            ,100f*scFactor
//            ,90f*scFactor)
//
//        path!!.quadTo(150f*scFactor
//            ,110f*scFactor
//            ,200f*scFactor
//            ,100f*scFactor)
//
//        path!!.close()
//
//        path1!!.moveTo(0f*scFactor,100f*scFactor)
////
//        path1!!.lineTo(100f*scFactor,130f*scFactor)
//        path1!!.lineTo(200f*scFactor,100f*scFactor)
////
////        //path1!!.moveTo(100f,200f)
////
//        path1!!.quadTo(100f*scFactor
//            ,mouthCurve*scFactor
//            ,0f*scFactor
//            ,100f*scFactor)
//        path1!!.close()
//
//
////        path!!.close()
//
//        canvas.drawPath(path!!,paint!!)
//        canvas.drawPath(path1!!,paint!!)


    }

    var INC = false


    private fun init() {

        scFactor = c!!.resources.displayMetrics.density

        path = Path()
        path1 = Path()

        paint = Paint()
        paint1 = Paint()

        paint!!.isAntiAlias = true
        paint!!.setColor(Color.RED)
        paint!!.style = Paint.Style.FILL
        paint!!.strokeWidth = 1f

        paint1!!.isAntiAlias = true
        paint1!!.setColor(Color.RED)
        paint1!!.style = Paint.Style.FILL
        paint1!!.strokeWidth = 1f

        valueAnim = ValueAnimator.ofInt(0,20,0)
        valueAnim!!.addUpdateListener {

            if (it.animatedValue as Int == 0) {

                INC = true

            }

            if (it.animatedValue as Int == 20) {

                INC = false

            }

            if (INC) {

                mouthCurveUP = 80+it.animatedValue as Int
                mouthCurveDOWN = 120 - it.animatedValue as Int

            } else {

                mouthCurveUP = 100-(20-it.animatedValue as Int)
                mouthCurveDOWN = 100 + (20-it.animatedValue as Int)


            }



            path1!!.reset()
            path!!.reset()


            invalidate()

        }

        valueAnim!!.duration = 600
        valueAnim!!.repeatCount = ValueAnimator.INFINITE


    }

    fun startTalk() {

        valueAnim?.start()


    }
}