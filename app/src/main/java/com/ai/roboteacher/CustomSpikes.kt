package com.ai.roboteacher

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import kotlin.random.Random

class CustomSpikes:View {

     var animList = ArrayList<ValueAnimator>()
     var pointFList = ArrayList<PointF>()
     var scaleYArr:Array<Float>?=null

    lateinit var c:Context

    lateinit var path: Path

    lateinit var paint: Paint

    var mWidth:Int = 0
    var mHeight:Int = 0

    var intervalCount = 0

    var MAX_SPIKE_WIDTH:Float = 0f
    var MAX_SPIKE_HEIGHT:Float = 0f

    lateinit var animators:ValueAnimator

    lateinit var pointF: PointF


    private constructor(c: Context) : super(c) {


    }

    constructor(c: Context, a: AttributeSet) : super(c,a) {

        this.c = c

        init()

    }

    constructor(c: Context, a: AttributeSet, defStyle:Int) : super(c,a,defStyle) {

        this.c = c

        init()

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        mWidth = w
        mHeight = h

        MAX_SPIKE_WIDTH = w*0.031106000125f

        //MAX_SPIKE_WIDTH = w*0.021599f
        MAX_SPIKE_HEIGHT = h*0.43f

        plotPoints()


    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        path.reset()


        canvas.translate(0f,mHeight/1.87f)

        var i = 0
        var spikeIndex = 0

        if (isStart) {

            for (i in 0 until pointFList.size) {

                if (i == 0) {

                    path.moveTo(pointFList.get(i).x, pointFList.get(i).y)

                } else if (i % 2 != 0) {

                    try {

                        path.lineTo(pointFList.get(i).x, pointFList.get(i).y * scaleYArr!![spikeIndex])
                        spikeIndex++

                    } catch (e:Exception) {


                    }




                } else {

                    path.lineTo(pointFList.get(i).x, pointFList.get(i).y)


                }

//            if (i%2!=0) {
//
//
//
//                path.lineTo()
//                path.lineTo()
//
//
//            }
            }

            canvas.drawPath(path, paint)

        }



//        while (i + 2 < pointFList.size && spikeIndex < scaleYArr!!.size) {
//            val p0 = pointFList[i]
//            val p1 = pointFList[i + 1]
//            val p2 = pointFList[i + 2]
//
//            path.moveTo(p0.x, p0.y * scaleYArr!![spikeIndex])
//            path.lineTo(p1.x, p1.y * scaleYArr!![spikeIndex])
//            path.lineTo(p2.x, p2.y * scaleYArr!![spikeIndex])
//
//            i += 2
//            spikeIndex++
//        }
//
//        canvas.drawPath(path, paint)




//        if (isStart) {
//
//            for (p in pointFList) {
//
//                drawCount++
//
//                if (drawCount == 0) {
//
//                    path.moveTo(p.x,p.y)
//
//                }
//
//
//
//                if (drawCount == 2) {
//
//                    drawCount = -1
//
//                    animCount++
//
//                    try {
//
//                        path.lineTo(p.x,p.y)
//
//                        canvas.scale(1f,scaleYArr!![animCount])
//
//                        canvas.drawPath(path,paint)
//
//                    } catch (e:Exception) {
//
//w
//                    }
//
//                } else if (drawCount==1) {
//
//                    path.lineTo(p.x,p.y)
//
//
//                }
//
//            }
//
//        }


    }

    var isStart = false

    private fun init() {

        paint = Paint()
        paint.isAntiAlias = true
        paint.setColor(ContextCompat.getColor(c,R.color.spike_color))
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.strokeCap = Paint.Cap.ROUND

        path = Path()



        pointF = PointF()
        pointF.x = 0f
        pointF.y = 0f




    }

    var count = 0;

    private fun plotPoints() {

        pointFList.add(PointF(0f,0f))



        while (pointF.x<=mWidth) {

            intervalCount++



            val x = Random.nextDouble(MAX_SPIKE_WIDTH.toDouble())
            val y = Random.nextDouble(MAX_SPIKE_HEIGHT.toDouble())



            pointF.x = pointF.x + x.toFloat()
            pointF.y =  y.toFloat()



            pointFList.add(PointF(pointF.x,pointF.y))

            if (intervalCount == 2) {

                count++

                animators = ValueAnimator().apply {

                    setFloatValues(1f,0f,1f)
                    setDuration(700)
                    interpolator = LinearInterpolator()
                    repeatCount = ValueAnimator.INFINITE
                    startDelay = 100*count.toLong()

                }

                animList.add(animators)

                intervalCount = 0

            }

        }

        pointF.x = 0f
        pointF.y = 0f

        scaleYArr = Array<Float>(animList.size,{

            0f
        })

        for (i in animList.indices) {

            var anim = animList.get(i)

            anim.addUpdateListener {



                isStart = true

                scaleYArr!![i] = it.getAnimatedValue() as Float
                Log.d(CustomSpikes::class.java.name, "plotPoints: " + scaleYArr!![i])

                invalidate()


            }

            animList.set(i,anim)

        }

        Handler().post {

            for (anim in animList) {

                anim.start()
            }


        }




    }

//    private fun drawPaths() {
//
//
//
//
//
//
//    }

//    private fun plotPoints(cnv:Canvas) {
//
//
//        while (pointF.x<=mWidth) {
//
//            intervalCount++
//
//            path.moveTo(pointF.x,pointF.y)
//
//            val x = Random.nextDouble(MAX_SPIKE_WIDTH.toDouble())
//            val y = Random.nextDouble(MAX_SPIKE_HEIGHT.toDouble())
//
////            if (pointF.y+y>MAX_SPIKE_HEIGHT) {
////
////                val diff = (MAX_SPIKE_HEIGHT - pointF.y)
////            }
//
//            pointF.x = pointF.x + x.toFloat()
//            pointF.y =  y.toFloat()
//
//            path.lineTo(pointF.x,pointF.y)
//
//            cnv.drawPath(path,paint)
//
//            pointFList.add(PointF(pointF.x,pointF.y))
//
//            if (intervalCount == 2) {
//
//                count++
//
//                animators = ValueAnimator().apply {
//
//                    setFloatValues(1f,0f,1f)
//                    setDuration(500)
//                    interpolator = LinearInterpolator()
//                    repeatCount = ValueAnimator.INFINITE
//                    startDelay = 100*count.toLong()
//
//                    addUpdateListener {
//
//                        isStart = true
//
//                        mScaleY = it.getAnimatedValue() as Float
//
//                        invalidate()
//                    }
//                }
//
//                animList.add(animators)
//
//                intervalCount = 0
//
//
//            }
//
//
//        }
//
//
//    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        Log.d(CustomSpikes::class.java.name, "onDetachedFromWindow: ")

        for (anim in animList) {

            anim.pause()

        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (isStart) {

            for (anim in animList) {

                anim.resume()

            }


        }

    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)



    }

    fun pauseAnim() {

        for (anim in animList) {

            anim.pause()

        }

    }

    fun resumeAnim() {

        if (isStart) {

            for (anim in animList) {

                anim.resume()

            }


        }
    }



}