package com.ai.roboteacher

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView

class ThinkingViewRoot:FrameLayout {

    var mWidth = 0
    var mHeight = 0
    var mPath:Path? = Path()
    var nPath:Path? = Path()
    var lPath:Path? = Path()
    var mPaint:Paint? = Paint()
    var isStarted = false
    var c:Context?=null
    var MAX_X = 0f

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

        //setWillNotDraw(false)

        c = context

//        setBackgroundColor(Color.RED)
//
//        this.post {
//
//            setupPath()
//            invalidate()
//
//
//        }

//        Handler().post {
//
//            setupPath()
//            //invalidate()
//
//        }

    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {

        c = context



//        setBackgroundColor(Color.RED)
//
//        this.post {
//
//            setupPath()
//            invalidate()
//
//
//        }


    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {

        c = context

//        setBackgroundColor(Color.RED)

//        this.post {
//
//            setupPath()
//            invalidate()
//
//
//        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        setBackgroundColor(Color.TRANSPARENT)

        setupPath()
        //invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        var dsp = resources.displayMetrics

        mWidth = (dsp.widthPixels *0.10f).toInt()
        mHeight = (dsp.heightPixels *0.26f).toInt()

        MAX_X = dsp.widthPixels *0.10f


        val width = resolveSize(mWidth, widthMeasureSpec)
        val height = resolveSize(mHeight, heightMeasureSpec)

        setMeasuredDimension(width, height)

//        setupPath()
//        invalidate()

    }

    private fun setupPath() {

        Log.d(ThinkingViewRoot::class.java.name, "setupPath: ${mWidth*0.35f}")

        mPath?.moveTo(0f,mHeight.toFloat())
        nPath?.moveTo(0f,mHeight.toFloat()-20f)
        lPath?.moveTo(0f,mHeight.toFloat()-40f)

        //mPath?.quadTo(mWidth*0.50f,mHeight.toFloat(),mWidth*0.70f,0f)
        //mPath?.cubicTo(mWidth*0.35f,180f,mWidth*0.40f,mHeight-30f,mWidth*0.70f,0f)

//        mPath?.rQuadTo(110f,0f,100f,-mHeight.toFloat())

        //mPath?.rCubicTo(150f,0f,60f,-50f,110f,-mHeight.toFloat())

        mPath?.lineTo(mWidth*0.35f,mHeight*0.20f)

        //mPath?.moveTo(0f,mHeight.toFloat())

        nPath?.lineTo(mWidth*0.35f,mHeight*0.15f)

        //mPath?.moveTo(0f,mHeight.toFloat())

        lPath?.lineTo(mWidth*0.35f,mHeight*0.10f)

        mPaint?.isAntiAlias = true
        mPaint?.color = Color.BLACK
        mPaint?.style = Paint.Style.STROKE
        mPaint?.strokeWidth = 5f

        //invalidate()

    }

//    override fun onDraw(canvas: Canvas) {
//        super.onDraw(canvas)
//
////        mPath?.let {
////
////            canvas.drawPath(mPath!!,mPaint!!)
////        }
//    }
//
    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

//        mPath?.let {
//
//            canvas.drawPath(mPath!!,mPaint!!)
//            canvas.drawPath(nPath!!,mPaint!!)
//            canvas.drawPath(lPath!!,mPaint!!)
//        }
    }

    public fun startAnims() {

        isStarted = true

        Thread(object : Runnable{
            override fun run() {

                while (isStarted) {

                    for (i in 0 until 3) {

                        var img = ImageView(c)
                        img.x = 0f
                        img.y = mHeight.toFloat()
                        img.setImageDrawable(c?.getDrawable(R.drawable.question_mark))

                        Handler(Looper.getMainLooper()).post({

                            addView(img)

                            var path = when (i) {
                                0 -> {

                                    //lPath

                                    mPath
                                }
                                1 -> {

                                    nPath
                                }
                                else -> {

                                    nPath
                                }
                            }

                            val animator:ObjectAnimator = ObjectAnimator.ofFloat(img,View.X,View.Y,path)
                            animator.duration = 2800
                            animator.startDelay = 350*(i+1).toLong()
                            animator.interpolator = LinearInterpolator()
                            animator.start()

                            animator.addListener(object :Animator.AnimatorListener{
                                override fun onAnimationStart(animation: Animator) {

                                }

                                override fun onAnimationEnd(animation: Animator) {

                                    img.visibility = View.GONE

                                }

                                override fun onAnimationCancel(animation: Animator) {

                                }

                                override fun onAnimationRepeat(animation: Animator) {

                                }


                            })

                            animator.addUpdateListener {

                                //val animX:Float = Math.abs(MAX_X - it.getAnimatedValue() as Float)

                                val animValue = it.animatedValue as Float

//                                if (animValue>30) {

                                    val animX:Float = animValue/MAX_X

                                    img.scaleX = Math.abs(1f-animX-0.07f)
                                    img.scaleY = Math.abs(1f-animX-0.07f)

                                    img.alpha = Math.abs(1f-animX-0.07f)

                               // }




//                            if (animX>10f) {
//
//                                img.alpha = animX/80
//
//                            } else {
//
//                                if (animX<2.0) {
//
//                                    img.alpha =
//                                }
//
//                                img.alpha = animX/10
//
//
//                            }

                                Log.d(ThinkingViewRoot::class.java.name, "run: "+it.getAnimatedValue())
                            }


                        })


                    }

                    Thread.sleep(1800)
                }


            }


        }).start()
    }

    public fun stopAnim() {

        isStarted = false
        removeAllViews()


    }
}